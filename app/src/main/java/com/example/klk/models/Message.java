package com.example.klk.models;

import com.google.firebase.firestore.PropertyName;

public class Message {
    private String id;
    private String chatId;
    private String senderId;
    private String senderName;
    private String content;
    private MessageType messageType;
    private long timestamp;
    private String imageUrl;
    // Campo adicional para notificaciones (texto sin cifrar)
    private String contentForNotification;

    public Message() {
    }

    public Message(String chatId, String senderId, String senderName, String content, MessageType messageType) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = System.currentTimeMillis();
        this.imageUrl = "";
        this.contentForNotification = content; // Guardar sin cifrar
    }

    public Message(String id, String chatId, String senderId, String senderName, String content, 
                   MessageType messageType, long timestamp, String imageUrl) {
        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
    }

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("chatId")
    public String getChatId() {
        return chatId;
    }

    @PropertyName("chatId")
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    @PropertyName("senderId")
    public String getSenderId() {
        return senderId;
    }

    @PropertyName("senderId")
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    @PropertyName("senderName")
    public String getSenderName() {
        return senderName;
    }

    @PropertyName("senderName")
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    @PropertyName("content")
    public String getContent() {
        return content;
    }

    @PropertyName("content")
    public void setContent(String content) {
        this.content = content;
    }

    @PropertyName("messageType")
    public MessageType getMessageType() {
        return messageType;
    }

    @PropertyName("messageType")
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("contentForNotification")
    public String getContentForNotification() {
        return contentForNotification;
    }

    @PropertyName("contentForNotification")
    public void setContentForNotification(String contentForNotification) {
        this.contentForNotification = contentForNotification;
    }
}