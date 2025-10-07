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
    // Nombre del grupo (null para chats 1 a 1)
    private String groupName;
    // URL de la foto del grupo
    private String groupPhotoUrl;
    // Lista de IDs de usuarios administradores del grupo
    private List<String> adminIds;
    // Timestamp de creación del grupo
    private long createdAt;

    public Chat() {
        this.unreadCount = new HashMap<>();
        this.createdAt = System.currentTimeMillis();
    }

    public Chat(List<String> participantIds, List<String> participantNames) {
        this.participantIds = participantIds;
        this.participantNames = participantNames;
        this.lastMessage = "";
        this.lastMessageSenderId = "";
        this.lastMessageTime = System.currentTimeMillis();
        this.lastMessageType = MessageType.TEXT;
        this.unreadCount = new HashMap<>();
        this.groupName = null;
        this.groupPhotoUrl = null;
        this.adminIds = null;
        this.createdAt = System.currentTimeMillis();
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
        this.groupPhotoUrl = null;
        this.adminIds = null;
        this.createdAt = System.currentTimeMillis();
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

    @PropertyName("groupName")
    public String getGroupName() {
        return groupName;
    }

    @PropertyName("groupName")
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @PropertyName("groupPhotoUrl")
    public String getGroupPhotoUrl() {
        return groupPhotoUrl;
    }

    @PropertyName("groupPhotoUrl")
    public void setGroupPhotoUrl(String groupPhotoUrl) {
        this.groupPhotoUrl = groupPhotoUrl;
    }

    @PropertyName("adminIds")
    public List<String> getAdminIds() {
        return adminIds;
    }

    @PropertyName("adminIds")
    public void setAdminIds(List<String> adminIds) {
        this.adminIds = adminIds;
    }

    @PropertyName("createdAt")
    public long getCreatedAt() {
        return createdAt;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
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

    /**
     * Verifica si es un chat grupal
     */
    public boolean isGroupChat() {
        return participantIds != null && participantIds.size() > 2;
    }

    /**
     * Verifica si un usuario es administrador del grupo
     */
    public boolean isAdmin(String userId) {
        return adminIds != null && adminIds.contains(userId);
    }
}