package com.example.klk.models;

import com.google.firebase.firestore.PropertyName;

public class User {
    private String id;
    private String email;
    private String name;
    private String profileImageUrl;
    private String fcmToken;
    private boolean isOnline;
    private long lastSeen;

    public User() {
    }

    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profileImageUrl = "";
        this.isOnline = false;
        this.lastSeen = System.currentTimeMillis();
    }

    public User(String id, String email, String name, String profileImageUrl, boolean isOnline, long lastSeen) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;
    }

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("email")
    public String getEmail() {
        return email;
    }

    @PropertyName("email")
    public void setEmail(String email) {
        this.email = email;
    }


    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("profileImageUrl")
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    @PropertyName("profileImageUrl")
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Alias para getProfileImageUrl para compatibilidad
     */
    public String getPhotoUrl() {
        return profileImageUrl;
    }

    @PropertyName("isOnline")
    public boolean isOnline() {
        return isOnline;
    }

    @PropertyName("isOnline")
    public void setOnline(boolean online) {
        isOnline = online;
    }

    @PropertyName("lastSeen")
    public long getLastSeen() {
        return lastSeen;
    }

    @PropertyName("lastSeen")
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    @PropertyName("fcmToken")
    public String getFcmToken() {
        return fcmToken;
    }

    @PropertyName("fcmToken")
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}