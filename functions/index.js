/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onDocumentCreated} = require('firebase-functions/v2/firestore');
const {setGlobalOptions} = require('firebase-functions/v2');
const admin = require('firebase-admin');
const logger = require('firebase-functions/logger');

// Inicializar Firebase Admin
admin.initializeApp();

// Configuración global
setGlobalOptions({ maxInstances: 10 });

/**
 * Cloud Function que se ejecuta cuando se crea un nuevo mensaje
 * Envía notificación push al receptor del mensaje
 * Soporta chats individuales y grupales
 */
exports.sendMessageNotification = onDocumentCreated(
  'messages/{messageId}',
  async (event) => {
    try {
      const message = event.data.data();
      const messageId = event.params.messageId;

      logger.info('📩 Nuevo mensaje creado:', messageId);

      // Extraer datos del mensaje
      const senderId = message.senderId;
      const senderName = message.senderName || 'Usuario';
      const messageContent = message.content || '';
      const messageType = message.messageType || 'TEXT';
      const chatId = message.chatId;

      // Validar datos requeridos
      if (!senderId || !chatId) {
        logger.error('❌ Mensaje sin senderId o chatId');
        return null;
      }

      // Obtener información del chat
      const chatDoc = await admin.firestore()
        .collection('chats')
        .doc(chatId)
        .get();

      if (!chatDoc.exists) {
        logger.error('❌ Chat no encontrado:', chatId);
        return null;
      }

      const chatData = chatDoc.data();
      const participantIds = chatData.participantIds || [];
      const isGroupChat = participantIds.length > 2;

      // Filtrar participantes (excluir al remitente)
      const receiverIds = participantIds.filter(id => id !== senderId);

      if (receiverIds.length === 0) {
        logger.info('⚠️ No hay receptores para notificar');
        return null;
      }

      logger.info(`📤 Enviando notificaciones a ${receiverIds.length} receptor(es)`);

      // Preparar el contenido de la notificación
      let notificationBody;
      if (messageType === 'IMAGE') {
        notificationBody = '📷 Imagen';
      } else if (messageType === 'MIXED') {
        notificationBody = messageContent ? `📷 ${messageContent}` : '📷 Imagen';
      } else {
        notificationBody = messageContent || 'Nuevo mensaje';
      }

      // Título de la notificación
      let notificationTitle;
      if (isGroupChat) {
        // Para grupos: "Nombre del remitente en Nombre del Grupo"
        const chatName = chatData.name || 'Grupo';
        notificationTitle = `${senderName} en ${chatName}`;
      } else {
        // Para chats individuales: solo el nombre del remitente
        notificationTitle = senderName;
      }

      // Obtener tokens FCM de todos los receptores
      const tokenPromises = receiverIds.map(async (receiverId) => {
        try {
          const userDoc = await admin.firestore()
            .collection('users')
            .doc(receiverId)
            .get();

          if (userDoc.exists && userDoc.data().fcmToken) {
            return {
              userId: receiverId,
              token: userDoc.data().fcmToken
            };
          }
          return null;
        } catch (error) {
          logger.error(`❌ Error obteniendo token de ${receiverId}:`, error);
          return null;
        }
      });

      const tokenResults = await Promise.all(tokenPromises);
      const validTokens = tokenResults.filter(result => result !== null);

      if (validTokens.length === 0) {
        logger.info('⚠️ No hay tokens FCM válidos para enviar notificaciones');
        return null;
      }

      logger.info(`✅ Encontrados ${validTokens.length} token(s) válido(s)`);

      // Enviar notificaciones a todos los receptores (MÉTODO INDIVIDUAL PARA MEJOR DEBUG)
      let successCount = 0;
      let failureCount = 0;

      for (const tokenData of validTokens) {
        try {
          const payload = {
            token: tokenData.token,
            notification: {
              title: notificationTitle,
              body: notificationBody
            },
            data: {
              chatId: chatId,
              senderId: senderId,
              senderName: senderName,
              messageContent: messageContent,
              messageType: messageType,
              isGroupChat: isGroupChat.toString(),
              click_action: 'OPEN_CHAT'
            },
            android: {
              priority: 'high',
              notification: {
                sound: 'default',
                icon: 'ic_notification'
              }
            }
          };

          const response = await admin.messaging().send(payload);
          logger.info(`✅ Notificación enviada exitosamente a ${tokenData.userId}: ${response}`);
          successCount++;
        } catch (error) {
          logger.error(`❌ Error enviando a ${tokenData.userId}:`, error.code || error.message);
          logger.error(`❌ Detalles del error:`, JSON.stringify(error));
          failureCount++;

          // Limpiar token inválido
          const errorCode = error.code;
          if (errorCode === 'messaging/invalid-registration-token' ||
              errorCode === 'messaging/registration-token-not-registered') {
            logger.info(`🗑️ Limpiando token inválido del usuario: ${tokenData.userId}`);
            await admin.firestore()
              .collection('users')
              .doc(tokenData.userId)
              .update({ fcmToken: admin.firestore.FieldValue.delete() });
          }
        }
      }

      logger.info(`✅ Resumen: ${successCount} éxitos, ${failureCount} fallos`);

      return {
        success: true,
        sent: successCount,
        failed: failureCount
      };

    } catch (error) {
      logger.error('❌ Error general:', error);
      logger.error('❌ Stack trace:', error.stack);
      return null;
    }
  }
);

logger.info('🚀 Cloud Functions inicializadas - Soporta chats individuales y grupales');
