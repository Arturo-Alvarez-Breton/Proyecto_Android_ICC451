package com.example.klk.models;

import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Chat {
    private String id;
    private List<String> participantIds;
    private List<String> participantNames;
    private String lastMessage;
    private String lastMessageSenderId;
    private long lastMessageTime;
    private MessageType lastMessageType;

    public Chat() {
    }

    public Chat(List<String> participantIds, List<String> participantNames) {
        this.participantIds = participantIds;
        this.participantNames = participantNames;
        this.lastMessage = "";
        this.lastMessageSenderId = "";
        this.lastMessageTime = System.currentTimeMillis();
        this.lastMessageType = MessageType.TEXT;
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
}