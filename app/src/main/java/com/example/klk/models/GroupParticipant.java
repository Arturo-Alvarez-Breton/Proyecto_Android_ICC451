package com.example.klk.models;

/**
 * Modelo para representar un participante en un grupo
 * Aplica principios SOLID y KISS
 */
public class GroupParticipant {
    private String userId;
    private String userName;
    private String photoUrl;
    private boolean isAdmin;

    public GroupParticipant() {
    }

    public GroupParticipant(String userId, String userName, String photoUrl, boolean isAdmin) {
        this.userId = userId;
        this.userName = userName;
        this.photoUrl = photoUrl;
        this.isAdmin = isAdmin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}

