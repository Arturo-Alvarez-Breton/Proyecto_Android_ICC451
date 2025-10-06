package com.example.klk.services;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.klk.utils.CryptoUtil;
import com.example.klk.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.List;
import java.util.Map;

/**
 * Servicio de Firebase Cloud Messaging para manejar notificaciones push
 * Aplica principios SOLID:
 * - Single Responsibility: Solo maneja FCM y notificaciones
 * - Open/Closed: Extensible para nuevos tipos de notificaciones
 * - Dependency Inversion: Depende de abstracciones (NotificationHelper)
 */
public class FCMNotificationService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    // Keys para los datos del mensaje (DRY - definidos una sola vez)
    private static final String KEY_CHAT_ID = "chatId";
    private static final String KEY_CHAT_NAME = "chatName";
    private static final String KEY_SENDER_ID = "senderId";
    private static final String KEY_SENDER_NAME = "senderName";
    private static final String KEY_MESSAGE_CONTENT = "messageContent";
    private static final String KEY_MESSAGE_TYPE = "messageType";
    private static final String KEY_IMAGE_URL = "imageUrl";
    private static final String KEY_IS_GROUP_CHAT = "isGroupChat";
    private static final String MESSAGE_TYPE_IMAGE = "IMAGE";
    private static final String MESSAGE_TYPE_MIXED = "MIXED";

    /**
     * Se llama cuando se recibe un nuevo mensaje FCM
     * Este método se ejecuta en un thread en segundo plano
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Mensaje recibido de: " + remoteMessage.getFrom());

        // Verificar que el mensaje contiene datos
        Map<String, String> data = remoteMessage.getData();
        if (data.isEmpty()) {
            Log.w(TAG, "Mensaje recibido sin datos");
            return;
        }

        // Log de los datos recibidos para debugging
        Log.d(TAG, "Datos del mensaje: " + data.toString());

        // Procesar el mensaje
        handleMessageReceived(data);
    }

    /**
     * Procesa el mensaje recibido y muestra la notificación si es necesario
     * Aplica KISS: lógica simple y directa
     */
    private void handleMessageReceived(Map<String, String> data) {
        // Extraer información del mensaje
        String chatId = data.get(KEY_CHAT_ID);
        String chatName = data.get(KEY_CHAT_NAME);
        String senderId = data.get(KEY_SENDER_ID);
        String senderName = data.get(KEY_SENDER_NAME);
        String messageContent = data.get(KEY_MESSAGE_CONTENT);
        String messageType = data.get(KEY_MESSAGE_TYPE);
        String imageUrl = data.get(KEY_IMAGE_URL);
        String isGroupChat = data.get(KEY_IS_GROUP_CHAT);

        // Validar datos requeridos
        if (chatId == null || senderName == null) {
            Log.w(TAG, "Mensaje con datos incompletos: chatId o senderName es null");
            return;
        }

        // No mostrar notificación si el mensaje es del usuario actual
        if (isMessageFromCurrentUser(senderId)) {
            Log.d(TAG, "Mensaje del usuario actual, no se muestra notificación");
            return;
        }

        // No mostrar notificación si la app está en primer plano y el chat está abierto
        // (YAGNI: por ahora solo verificamos si está en primer plano)
        if (isAppInForeground()) {
            Log.d(TAG, "App en primer plano, no se muestra notificación");
            return;
        }

        // IMPORTANTE: Descifrar el mensaje antes de mostrarlo
        String decryptedContent = messageContent;
        if (messageContent != null && !messageContent.isEmpty() && chatId != null) {
            // Solo descifrar si es texto (no si es imagen)
            if (!MESSAGE_TYPE_IMAGE.equals(messageType)) {
                String decrypted = CryptoUtil.decrypt(messageContent, chatId);
                if (decrypted != null) {
                    decryptedContent = decrypted;
                    Log.d(TAG, "Mensaje descifrado correctamente");
                }
            }
        }

        // Mostrar la notificación con el contenido descifrado
        showNotification(chatId, chatName, senderName, decryptedContent, messageType, imageUrl, isGroupChat);
    }

    /**
     * Muestra la notificación usando el NotificationHelper
     */
    private void showNotification(String chatId, String chatName, String senderName,
                                  String messageContent, String messageType, String imageUrl, String isGroupChat) {
        // Determinar el título correcto de la notificación
        String notificationTitle;
        boolean isGroup = "true".equals(isGroupChat);

        if (isGroup && chatName != null && !chatName.isEmpty()) {
            // Para grupos: "Nombre del remitente en Nombre del Grupo"
            notificationTitle = senderName + " en " + chatName;
        } else {
            // Para chats individuales: solo el nombre del remitente
            notificationTitle = senderName;
        }

        // Determinar el contenido de la notificación
        String notificationBody;
        if (MESSAGE_TYPE_IMAGE.equals(messageType)) {
            notificationBody = "📷 Imagen";
        } else if (MESSAGE_TYPE_MIXED.equals(messageType)) {
            notificationBody = (messageContent != null && !messageContent.isEmpty())
                ? "📷 " + messageContent
                : "📷 Imagen";
        } else {
            // Mensaje de texto descifrado
            notificationBody = (messageContent != null && !messageContent.isEmpty())
                ? messageContent
                : "Nuevo mensaje";
        }

        // Determinar si es una imagen o mensaje mixto
        boolean isImage = MESSAGE_TYPE_IMAGE.equals(messageType) || MESSAGE_TYPE_MIXED.equals(messageType);

        // Usar el helper para mostrar la notificación (SOLID - Separation of Concerns)
        NotificationHelper notificationHelper = NotificationHelper.getInstance(this);
        notificationHelper.showMessageNotification(
            chatId,
            notificationTitle,
            notificationBody,
            isImage
        );

        Log.d(TAG, "Notificación mostrada - Título: " + notificationTitle + ", Contenido: " + notificationBody);
    }

    /**
     * Verifica si el mensaje es del usuario actual
     */
    private boolean isMessageFromCurrentUser(String senderId) {
        if (senderId == null) {
            return false;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && senderId.equals(currentUser.getUid());
    }

    /**
     * Verifica si la app está en primer plano
     * Útil para decidir si mostrar notificación o no
     */
    private boolean isAppInForeground() {
        ActivityManager activityManager =
            (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager == null) {
            return false;
        }

        List<ActivityManager.RunningAppProcessInfo> appProcesses =
            activityManager.getRunningAppProcesses();

        if (appProcesses == null) {
            return false;
        }

        String packageName = getApplicationContext().getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance ==
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName.equals(packageName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Se llama cuando se genera un nuevo token FCM o cuando un token existente se actualiza
     * Esto puede ocurrir en:
     * 1. Primera instalación de la app
     * 2. Usuario desinstala y reinstala la app
     * 3. Usuario limpia los datos de la app
     * 4. App se restaura en un nuevo dispositivo
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.d(TAG, "Nuevo token FCM generado: " + token);

        // Actualizar el token en Firestore para el usuario actual
        updateTokenInFirestore(token);
    }

    /**
     * Actualiza el token FCM del usuario en Firestore
     * Esto permite que otros usuarios puedan enviarle notificaciones
     */
    private void updateTokenInFirestore(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.w(TAG, "No hay usuario autenticado, no se puede actualizar el token");
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Actualizar el campo fcmToken del documento del usuario
        db.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener(aVoid ->
                Log.d(TAG, "Token FCM actualizado en Firestore exitosamente"))
            .addOnFailureListener(e ->
                Log.e(TAG, "Error al actualizar token FCM en Firestore", e));
    }
}
