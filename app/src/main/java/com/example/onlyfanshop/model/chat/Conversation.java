package com.example.onlyfanshop.model.chat;

import androidx.annotation.Keep;

/**
 * Conversation model for listing chats in the inbox.
 * Path: conversations/{conversationId}
 */
@Keep
public class Conversation {
    private String id;
    private String customerId;
    private String adminId;
    private String customerName;
    private String adminName;
    private String lastMessage;
    private long lastTimestamp;

    public Conversation() {}

    public Conversation(String id,
                        String customerId,
                        String adminId,
                        String customerName,
                        String adminName,
                        String lastMessage,
                        long lastTimestamp) {
        this.id = id;
        this.customerId = customerId;
        this.adminId = adminId;
        this.customerName = customerName;
        this.adminName = adminName;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getAdminId() { return adminId; }
    public String getCustomerName() { return customerName; }
    public String getAdminName() { return adminName; }
    public String getLastMessage() { return lastMessage; }
    public long getLastTimestamp() { return lastTimestamp; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastTimestamp(long lastTimestamp) { this.lastTimestamp = lastTimestamp; }
}




