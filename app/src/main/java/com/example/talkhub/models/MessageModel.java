package com.example.talkhub.models;

public class MessageModel {

    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;
    private boolean seen;
    private boolean delivered;

    // Empty constructor required for Firebase
    public MessageModel() {
    }

    // Constructor without seen/delivered (default false)
    public MessageModel(String senderId, String receiverId,
                        String message, long timestamp) {

        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.seen = false;
        this.delivered = false;
    }

    // Full constructor
    public MessageModel(String senderId, String receiverId,
                        String message, long timestamp,
                        boolean seen, boolean delivered) {

        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.seen = seen;
        this.delivered = delivered;
    }

    // Getters
    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public boolean isDelivered() {
        return delivered;
    }

    // Setters
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }
}