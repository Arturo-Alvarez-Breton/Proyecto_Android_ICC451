package com.example.klk.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.klk.models.Chat;
import com.example.klk.models.MessageType;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository simplificado para manejar chats con Firebase Firestore
 * Corrige problemas de listeners múltiples y filtrado
 */
public class ChatRepository {
    private static final String CHATS_COLLECTION = "chats";

    private final FirebaseFirestore firestore;
    private final CollectionReference chatsRef;

    // Singleton pattern
    private static ChatRepository instance;

    // UN SOLO listener y LiveData para evitar conflictos
    private ListenerRegistration activeListener;
    private MutableLiveData<List<Chat>> chatsLiveData;
    private String currentUserId;
    private List<Chat> allChats = new ArrayList<>(); // Cache local

    private ChatRepository() {
        firestore = FirebaseFirestore.getInstance();
        chatsRef = firestore.collection(CHATS_COLLECTION);
        chatsLiveData = new MutableLiveData<>();
    }

    public static synchronized ChatRepository getInstance() {
        if (instance == null) {
            instance = new ChatRepository();
        }
        return instance;
    }

    /**
     * Método principal - Obtiene todos los chats del usuario
     */
    public LiveData<List<Chat>> getUserChats(String userId) {
        // Solo crear listener si cambió el usuario o no existe
        if (!userId.equals(currentUserId) || activeListener == null) {
            startChatListener(userId);
        }
        return chatsLiveData;
    }

    /**
     * Obtiene chats filtrados (usa cache local - NO crea nuevos listeners)
     */
    public LiveData<List<Chat>> getUserChats(String userId, String filter) {
        // Asegurar que tenemos los datos base
        if (!userId.equals(currentUserId) || activeListener == null) {
            startChatListener(userId);
        }

        if (filter == null || filter.equals("todos")) {
            return chatsLiveData;
        }

        // Usar Transformations.map para evitar observeForever leaks
        return Transformations.map(chatsLiveData, all -> all == null ? new ArrayList<>() : applyFilter(all, filter));
    }

    /**
     * Inicia el listener principal (solo uno a la vez)
     */
    private void startChatListener(String userId) {
        // Limpiar listener anterior
        stopCurrentListener();
        currentUserId = userId;

        // Evitar necesidad de índice compuesto: quitar orderBy y ordenar en cliente
        activeListener = chatsRef
            .whereArrayContains("participantIds", userId)
            // .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    allChats.clear();
                    chatsLiveData.setValue(new ArrayList<>());
                    return;
                }

                if (snapshots != null) {
                    allChats.clear();
                    snapshots.forEach(document -> {
                        Chat chat = document.toObject(Chat.class);
                        if (chat != null) {
                            chat.setId(document.getId());
                            allChats.add(chat);
                        }
                    });
                    // Ordenar por lastMessageTime DESC en cliente
                    allChats.sort((a, b) -> Long.compare(b.getLastMessageTime(), a.getLastMessageTime()));
                    chatsLiveData.setValue(new ArrayList<>(allChats));
                }
            });
    }

    /**
     * Aplica filtro a la lista de chats (procesamiento local)
     */
    private List<Chat> applyFilter(List<Chat> chats, String filterType) {
        List<Chat> filtered = new ArrayList<>();

        for (Chat chat : chats) {
            switch (filterType.toLowerCase()) {
                case "grupos":
                    if (chat.getParticipantIds() != null && chat.getParticipantIds().size() > 2) {
                        filtered.add(chat);
                    }
                    break;
                case "contactos":
                    if (chat.getParticipantIds() != null && chat.getParticipantIds().size() == 2) {
                        filtered.add(chat);
                    }
                    break;
                default:
                    filtered.add(chat);
                    break;
            }
        }

        return filtered;
    }

    /**
     * Búsqueda en cache local (no crea nuevos listeners)
     */
    public LiveData<List<Chat>> searchChats(String userId, String query) {
        // Asegurar que tenemos datos
        if (!userId.equals(currentUserId) || activeListener == null) {
            startChatListener(userId);
        }
        if (query == null || query.trim().isEmpty()) {
            return chatsLiveData;
        }
        final String q = query.toLowerCase();
        // Mapear sobre el LiveData base
        return Transformations.map(chatsLiveData, chats -> {
            if (chats == null) return new ArrayList<>();
            List<Chat> results = new ArrayList<>();
            for (Chat chat : chats) {
                if (chat.getParticipantNames() != null) {
                    for (String name : chat.getParticipantNames()) {
                        if (name != null && name.toLowerCase().contains(q)) {
                            results.add(chat);
                            break;
                        }
                    }
                }
            }
            return results;
        });
    }

    /**
     * Crea un nuevo chat entre usuarios
     */
    public void createChat(List<String> participantIds, List<String> participantNames, ChatCallback callback) {
        if (participantIds == null || participantIds.size() < 2) {
            if (callback != null) callback.onError("Se requieren al menos 2 participantes");
            return;
        }

        findExistingChat(participantIds, new ChatCallback() {
            @Override
            public void onSuccess(Chat existingChat) {
                if (callback != null) callback.onSuccess(existingChat);
            }

            @Override
            public void onError(String error) {
                Chat newChat = new Chat(participantIds, participantNames);

                chatsRef.add(newChat)
                    .addOnSuccessListener(documentReference -> {
                        newChat.setId(documentReference.getId());
                        if (callback != null) callback.onSuccess(newChat);
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onError(e.getMessage());
                    });
            }
        });
    }

    /**
     * Crea un chat grupal con nombre (YAGNI: solo lo necesario para grupos)
     * Aplica SOLID: método específico para grupos, no modifica createChat existente
     */
    public void createGroupChat(String groupName, List<String> participantIds,
                                List<String> participantNames, ChatCallback callback) {
        // Validaciones (KISS: simples y claras)
        if (groupName == null || groupName.trim().isEmpty()) {
            if (callback != null) callback.onError("El nombre del grupo es requerido");
            return;
        }

        if (participantIds == null || participantIds.size() < 3) {
            if (callback != null) callback.onError("Se requieren al menos 3 participantes para un grupo");
            return;
        }

        // Crear el chat grupal (DRY: reutiliza la estructura existente)
        Chat newGroupChat = new Chat(participantIds, participantNames);
        newGroupChat.setGroupName(groupName);
        newGroupChat.setLastMessage("Grupo creado");

        chatsRef.add(newGroupChat)
            .addOnSuccessListener(documentReference -> {
                newGroupChat.setId(documentReference.getId());
                android.util.Log.d("ChatRepository", "Grupo creado exitosamente: " + documentReference.getId());
                if (callback != null) callback.onSuccess(newGroupChat);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("ChatRepository", "Error al crear grupo: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    /**
     * Busca chat existente
     */
    private void findExistingChat(List<String> participantIds, ChatCallback callback) {
        chatsRef.whereArrayContains("participantIds", participantIds.get(0))
            .get()
            .addOnSuccessListener(querySnapshot -> {
                for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot) {
                    Chat chat = document.toObject(Chat.class);
                    if (chat != null && isExactMatch(chat.getParticipantIds(), participantIds)) {
                        chat.setId(document.getId());
                        if (callback != null) callback.onSuccess(chat);
                        return;
                    }
                }
                if (callback != null) callback.onError("No existe chat");
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    /**
     * Verifica coincidencia exacta de participantes
     */
    private boolean isExactMatch(List<String> existing, List<String> required) {
        return existing != null &&
               existing.size() == required.size() &&
               existing.containsAll(required);
    }

    /**
     * Actualiza el último mensaje del chat
     */
    public void updateLastMessage(String chatId, String lastMessage, String senderId, MessageType messageType) {
        chatsRef.document(chatId)
            .update(
                "lastMessage", lastMessage,
                "lastMessageSenderId", senderId,
                "lastMessageTime", System.currentTimeMillis(),
                "lastMessageType", messageType
            );
    }

    /**
     * Actualiza el último mensaje y el contador de mensajes no leídos
     * Incrementa el contador para todos los participantes excepto el remitente
     * @param chatId ID del chat
     * @param lastMessage Texto del último mensaje
     * @param senderId ID del usuario que envió el mensaje
     * @param messageType Tipo de mensaje
     */
    public void updateLastMessageWithUnread(String chatId, String lastMessage, String senderId, MessageType messageType) {
        chatsRef.document(chatId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Chat chat = documentSnapshot.toObject(Chat.class);
                if (chat != null && chat.getParticipantIds() != null) {
                    // Incrementar contador de no leídos para todos excepto el remitente
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("lastMessage", lastMessage);
                    updates.put("lastMessageSenderId", senderId);
                    updates.put("lastMessageTime", System.currentTimeMillis());
                    updates.put("lastMessageType", messageType);

                    // Actualizar contadores de no leídos
                    for (String participantId : chat.getParticipantIds()) {
                        if (!participantId.equals(senderId)) {
                            int currentCount = chat.getUnreadCountForUser(participantId);
                            updates.put("unreadCount." + participantId, currentCount + 1);
                        }
                    }

                    chatsRef.document(chatId).update(updates);
                }
            }
        });
    }

    /**
     * Marca los mensajes de un chat como leídos para un usuario específico
     * @param chatId ID del chat
     * @param userId ID del usuario
     */
    public void markChatAsRead(String chatId, String userId) {
        chatsRef.document(chatId)
            .update("unreadCount." + userId, 0)
            .addOnFailureListener(e ->
                android.util.Log.e("ChatRepository", "Error al marcar como leído: " + e.getMessage())
            );
    }

    /**
     * Limpia el listener actual
     */
    private void stopCurrentListener() {
        if (activeListener != null) {
            activeListener.remove();
            activeListener = null;
        }
    }

    /**
     * Método de compatibilidad
     */
    public LiveData<List<Chat>> getFilteredChats(String userId, String filterType) {
        return getUserChats(userId, filterType);
    }

    /**
     * Detiene todos los listeners
     */
    public void stopListening() {
        stopCurrentListener();
        currentUserId = null;
        allChats.clear();
    }

    /**
     * Interface para callbacks
     */
    public interface ChatCallback {
        void onSuccess(Chat chat);
        void onError(String error);
    }
}
