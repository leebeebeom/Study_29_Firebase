package com.example.a29_firebase;

import android.net.Uri;

public class ChatMessage {
    private String id;
    private String text;
    private String name;
    private String profilePhotoUrl;
    private String imageUrl;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ChatMessage() {

    }

    public ChatMessage(String text, String name, String profilePhotoUrl, String imageUrl, String email) {
        this.text = text;
        this.name = name;
        this.profilePhotoUrl = profilePhotoUrl;
        this.imageUrl = imageUrl;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
