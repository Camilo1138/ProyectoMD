package com.example.myapplication.models;

import java.util.HashMap;
import java.util.Map;

public class Conversation {
    public String conversationId;
    public String participant1;
    public String participant2;
    public Map<String, Message> messages;
    public long lastUpdate;

    public Conversation() {}

    public Conversation(String participant1, String participant2) {
        this.participant1 = participant1;
        this.participant2 = participant2;
        this.messages = new HashMap<>();
        this.lastUpdate = System.currentTimeMillis();
    }
}