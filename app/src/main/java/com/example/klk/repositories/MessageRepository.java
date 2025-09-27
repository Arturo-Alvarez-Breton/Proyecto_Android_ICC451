package com.example.klk.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.klk.models.Message;
import com.example.klk.models.MessageType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Repository para manejar las operaciones de mensajes con Firebase Firestore
 * Implementa el patrón Repository para abstraer el acceso a datos
 */
public class MessageRepository {
    private static final String MESSAGES_COLLECTION = "messages";

    private final CollectionReference messagesRef;
    private ListenerRegistration messagesListener;

    // Singleton pattern
    private static MessageRepository instance;

    private MessageRepository() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        messagesRef = firestore.collection(MESSAGES_COLLECTION);
    }

    public static synchronized MessageRepository getInstance() {
        if (instance == null) {
            instance = new MessageRepository();
        }
        return instance;
    }

    /**
     * Envía un nuevo mensaje de texto
     * @param chatId ID del chat
     * @param senderId ID del usuario que envía
     * @param senderName Nombre del usuario que envía
     * @param content Contenido del mensaje
     * @param callback Callback para el resultado de la operación
     */
    public void sendTextMessage(String chatId, String senderId, String senderName,
                               String content, MessageCallback callback) {

        // Verificar autenticación antes de enviar mensaje
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError("Usuario no autenticado. Por favor, inicia sesión.");
            }
            return;
        }

        // Verificar que el senderId coincida con el usuario autenticado
        if (!currentUser.getUid().equals(senderId)) {
            if (callback != null) {
                callback.onError("Error de autenticación: ID de usuario no válido.");
            }
            return;
        }

        Message message = new Message(chatId, senderId, senderName, content, MessageType.TEXT);
        sendMessage(message, callback);
    }

    /**
     * Envía un mensaje con imagen
     * @param chatId ID del chat
     * @param senderId ID del usuario que envía
     * @param senderName Nombre del usuario que envía
     * @param imageUrl URL de la imagen
     * @param callback Callback para el resultado de la operación
     */
    public void sendImageMessage(String chatId, String senderId, String senderName,
                                String imageUrl, MessageCallback callback) {
        Message message = new Message(chatId, senderId, senderName, "", MessageType.IMAGE);
        message.setImageUrl(imageUrl);
        sendMessage(message, callback);
    }

    /**
     * Sube una imagen a Firebase Storage y envía el mensaje
     * @param chatId ID del chat
     * @param senderId ID del usuario que envía
     * @param senderName Nombre del usuario que envía
     * @param imageUri URI local de la imagen
     * @param callback Callback para el resultado de la operación
     */
    public void sendImageMessage(String chatId, String senderId, String senderName,
                                android.net.Uri imageUri, MessageCallback callback) {

        // Verificar autenticación antes de subir imagen
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError("Usuario no autenticado. Por favor, inicia sesión.");
            }
            return;
        }

        // Verificar que el senderId coincida con el usuario autenticado
        if (!currentUser.getUid().equals(senderId)) {
            if (callback != null) {
                callback.onError("Error de autenticación: ID de usuario no válido.");
            }
            return;
        }

        // Referencia a Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("chat_images");
        String fileName = System.currentTimeMillis() + "_" + senderId + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        // Subir imagen
        imageRef.putFile(imageUri)
            .addOnSuccessListener(taskSnapshot -> {
                // Obtener URL de descarga
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    // Crear y enviar mensaje con la URL de la imagen
                    Message message = new Message(chatId, senderId, senderName, "", MessageType.IMAGE);
                    message.setImageUrl(imageUrl);
                    sendMessage(message, callback);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError("Error al obtener URL de imagen: " + e.getMessage());
                    }
                });
            })
            .addOnFailureListener(e -> {
                if (callback != null) {
                    callback.onError("Error al subir imagen: " + e.getMessage());
                }
            });
    }

    /**
     * Método privado para enviar cualquier tipo de mensaje
     */
    private void sendMessage(Message message, MessageCallback callback) {
        messagesRef.add(message)
            .addOnSuccessListener(documentReference -> {
                message.setId(documentReference.getId());

                // Actualizar el último mensaje del chat
                updateChatLastMessage(message);

                if (callback != null) {
                    callback.onSuccess(message);
                }
            })
            .addOnFailureListener(e -> {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            });
    }

    /**
     * Actualiza el último mensaje del chat
     */
    private void updateChatLastMessage(Message message) {
        ChatRepository chatRepository = ChatRepository.getInstance();
        String lastMessageText = message.getMessageType() == MessageType.IMAGE ? "📷 Imagen" : message.getContent();

        chatRepository.updateLastMessage(
            message.getChatId(),
            lastMessageText,
            message.getSenderId(),
            message.getMessageType()
        );
    }

    /**
     * Obtiene los mensajes de un chat en tiempo real
     * @param chatId ID del chat
     * @return LiveData con la lista de mensajes
     */
    public LiveData<List<Message>> getChatMessages(String chatId) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();

        // Remover listener anterior si existe
        if (messagesListener != null) {
            messagesListener.remove();
        }

        // Query sin orderBy (evita índice compuesto); ordenamos en cliente
        Query query = messagesRef.whereEqualTo("chatId", chatId);

        messagesListener = query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                // Log y retornar lista vacía para evitar crasheos visibles
                android.util.Log.e("MessageRepository", "Error escuchando mensajes: ", error);
                messagesLiveData.setValue(new ArrayList<>());
                return;
            }

            if (snapshots != null) {
                List<Message> messages = new ArrayList<>();
                snapshots.forEach(document -> {
                    Message message = document.toObject(Message.class);
                    message.setId(document.getId());
                    messages.add(message);
                });
                // Ordenar por timestamp ASC para mostrar cronológicamente
                messages.sort(Comparator.comparingLong(Message::getTimestamp));
                messagesLiveData.setValue(messages);
            }
        });

        return messagesLiveData;
    }

    /**
     * Detiene el listener de mensajes en tiempo real
     */
    public void stopListening() {
        if (messagesListener != null) {
            messagesListener.remove();
            messagesListener = null;
        }
    }

    /**
     * Interface para callbacks de operaciones de mensajes
     */
    public interface MessageCallback {
        void onSuccess(Message message);
        void onError(String error);
    }
}
