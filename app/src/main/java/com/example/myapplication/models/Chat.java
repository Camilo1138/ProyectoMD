package com.example.myapplication.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;

public class Chat {
    private String chatId;
    private DocumentReference user1;
    private DocumentReference user2;
    private String lastMessage;
    private Date lastUpdate;
    private boolean encrypted;

    // Constructor vacío para Firestore
    public Chat() {}

    public String getChatId() {
        return chatId;
    }
// Getters y setters...

    public DocumentReference getUser1() {
        return user1;
    }

    public DocumentReference getUser2() {
        return user2;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setUser1(DocumentReference user1) {
        this.user1 = user1;
    }

    public void setUser2(DocumentReference user2) {
        this.user2 = user2;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

}