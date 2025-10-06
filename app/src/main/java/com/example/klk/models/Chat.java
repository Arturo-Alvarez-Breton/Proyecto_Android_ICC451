package com.example.klk.models;

import com.google.firebase.firestore.PropertyName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat {
    private String id;
    private List<String> participantIds;
    private List<String> participantNames;
    private String lastMessage;
    private String lastMessageSenderId;
    private long lastMessageTime;
    private MessageType lastMessageType;
    // Mapa para rastrear mensajes no leídos por usuario (userId -> count)
    private Map<String, Integer> unreadCount;

    public Chat() {
        this.unreadCount = new HashMap<>();
    }

    public Chat(List<String> participantIds, List<String> participantNames) {
        this.participantIds = participantIds;
        this.participantNames = participantNames;
        this.lastMessage = "";
        this.lastMessageSenderId = "";
        this.lastMessageTime = System.currentTimeMillis();
        this.lastMessageType = MessageType.TEXT;
        this.unreadCount = new HashMap<>();
    }

    public Chat(String id, List<String> participantIds, List<String> participantNames,
                String lastMessage, String lastMessageSenderId, long lastMessageTime,
                MessageType lastMessageType) {
        this.id = id;
        this.participantIds = participantIds;
        this.participantNames = participantNames;
        this.lastMessage = lastMessage;
        this.lastMessageSenderId = lastMessageSenderId;
        this.lastMessageTime = lastMessageTime;
        this.lastMessageType = lastMessageType;
        this.unreadCount = new HashMap<>();
    }

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("participantIds")
    public List<String> getParticipantIds() {
        return participantIds;
    }

    @PropertyName("participantIds")
    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    @PropertyName("participantNames")
    public List<String> getParticipantNames() {
        return participantNames;
    }

    @PropertyName("participantNames")
    public void setParticipantNames(List<String> participantNames) {
        this.participantNames = participantNames;
    }

    @PropertyName("lastMessage")
    public String getLastMessage() {
        return lastMessage;
    }

    @PropertyName("lastMessage")
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @PropertyName("lastMessageSenderId")
    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    @PropertyName("lastMessageSenderId")
    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    @PropertyName("lastMessageTime")
    public long getLastMessageTime() {
        return lastMessageTime;
    }

    @PropertyName("lastMessageTime")
    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    @PropertyName("lastMessageType")
    public MessageType getLastMessageType() {
        return lastMessageType;
    }

    @PropertyName("lastMessageType")
    public void setLastMessageType(MessageType lastMessageType) {
        this.lastMessageType = lastMessageType;
    }

    @PropertyName("unreadCount")
    public Map<String, Integer> getUnreadCount() {
        return unreadCount;
    }

    @PropertyName("unreadCount")
    public void setUnreadCount(Map<String, Integer> unreadCount) {
        this.unreadCount = unreadCount;
    }

    /**
     * Obtiene el número de mensajes no leídos para un usuario específico
     * @param userId ID del usuario
     * @return Número de mensajes no leídos
     */
    public int getUnreadCountForUser(String userId) {
        if (unreadCount == null) {
            return 0;
        }
        Integer count = unreadCount.get(userId);
        return count != null ? count : 0;
    }

    /**
     * Verifica si el usuario tiene mensajes no leídos
     * @param userId ID del usuario
     * @return true si tiene mensajes no leídos
     */
    public boolean hasUnreadMessages(String userId) {
        return getUnreadCountForUser(userId) > 0;
    }
}