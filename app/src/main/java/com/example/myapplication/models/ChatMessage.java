package com.example.myapplication.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;

public class ChatMessage {
    private String messageId;
    private DocumentReference chatId;
    private DocumentReference senderId;
    private String content;
    private Date timestamp;
    private String status;

    // Constructor vacío para Firestore
    public ChatMessage() {}

    // Getters y setters...

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSenderId(DocumentReference senderId) {
        this.senderId = senderId;
    }

    public void setChatId(DocumentReference chatId) {
        this.chatId = chatId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public DocumentReference getSenderId() {
        return senderId;
    }

    public DocumentReference getChatId() {
        return chatId;
    }

    public String getMessageId() {
        return messageId;
    }
}
