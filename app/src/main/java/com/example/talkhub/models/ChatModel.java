package com.example.talkhub.models;

public class ChatModel {

    String userId, lastMessage;
    long timestamp;

    public ChatModel() {}

    public ChatModel(String userId, String lastMessage, long timestamp) {
        this.userId = userId;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public String getLastMessage() { return lastMessage; }
    public long getTimestamp() { return timestamp; }
}