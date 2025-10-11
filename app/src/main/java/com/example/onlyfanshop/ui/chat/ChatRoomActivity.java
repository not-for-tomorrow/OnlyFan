package com.example.onlyfanshop.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.model.chat.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {

    private String conversationId;
    private RecyclerView recyclerView;
    private EditText edtMessage;
    private ImageButton btnSend;
    private TextView tvHeader;
    private final List<Message> messages = new ArrayList<>();
    private MessagesAdapter adapter;
    private DatabaseReference messagesRef;
    private String selfUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        conversationId = getIntent().getStringExtra("conversationId");
        recyclerView = findViewById(R.id.recyclerMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        tvHeader = findViewById(R.id.tvHeader);
        View back = findViewById(R.id.btnBack);
        if (back != null) back.setOnClickListener(v -> onBackPressed());
        String customerName = getIntent().getStringExtra("customerName");
        if (tvHeader != null && customerName != null) {
            tvHeader.setText(customerName);
            Log.d("ChatRoomActivity", "Customer name from intent: " + customerName);
        } else {
            Log.w("ChatRoomActivity", "No customer name provided in intent");
        }

        selfUserId = FirebaseAuth.getInstance().getUid();
        if (selfUserId == null) {
            // For admin users who don't have Firebase UID, use admin_uid
            selfUserId = "admin_uid";
            Log.d("ChatRoomActivity", "No Firebase UID, using admin_uid as selfUserId");
        } else {
            Log.d("ChatRoomActivity", "Firebase UID: " + selfUserId);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MessagesAdapter(messages, selfUserId);
        recyclerView.setAdapter(adapter);

        if (conversationId != null) {
            Log.d("ChatRoomActivity", "Connecting to Firebase with conversationId: " + conversationId);
            messagesRef = FirebaseDatabase.getInstance()
                    .getReference("chats")
                    .child(conversationId)
                    .child("messages");

            // Ensure conversation exists in conversations collection
            ensureConversationExists();

            messagesRef.addChildEventListener(new ChildEventListener() {
                @Override public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                    Log.d("ChatRoomActivity", "New message received: " + snapshot.getKey());
                    Message m = snapshot.getValue(Message.class);
                    if (m != null) {
                        messages.add(m);
                        adapter.notifyItemInserted(messages.size() - 1);
                        recyclerView.scrollToPosition(messages.size() - 1);
                        Log.d("ChatRoomActivity", "Message added to UI: " + m.getText());
                    }
                }
                @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                    Log.d("ChatRoomActivity", "Message updated: " + snapshot.getKey());
                }
                @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    Log.d("ChatRoomActivity", "Message removed: " + snapshot.getKey());
                }
                @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
                @Override public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ChatRoomActivity", "Firebase listener cancelled: " + error.getMessage());
                }
            });
        } else {
            Log.w("ChatRoomActivity", "No conversationId provided, cannot load messages");
        }

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Log.w("ChatRoomActivity", "Attempted to send empty message");
            return;
        }
        
        String uid = selfUserId;
        if (conversationId == null || messagesRef == null) {
            Log.e("ChatRoomActivity", "Cannot send message: conversationId or messagesRef is null");
            return;
        }
        
        // Get real username from JWT token
        String realUsername = getUsernameFromToken();
        Log.d("ChatRoomActivity", "Sending message: " + text + " from user: " + uid + " with username: " + realUsername);
        
        String messageId = messagesRef.push().getKey();
        long now = System.currentTimeMillis();
        Message message = new Message(messageId, conversationId, uid, realUsername, text, now);
        
        if (messageId != null) {
            messagesRef.child(messageId).setValue(message)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ChatRoomActivity", "Message sent successfully: " + messageId);
                    // Update conversation with full data
                    updateConversationData(text, now);
                    // Sync message to MySQL database
                    syncMessageToDatabase(message);
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatRoomActivity", "Failed to send message: " + e.getMessage());
                });
        } else {
            Log.e("ChatRoomActivity", "Failed to generate message ID");
        }
        
        edtMessage.setText("");
    }

    private String getUsernameFromToken() {
        try {
            // Get JWT token from SharedPreferences
            android.content.SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String token = prefs.getString("jwt_token", null);
            
            if (token == null || token.isEmpty()) {
                Log.w("ChatRoomActivity", "No JWT token found, using default username");
                return "User";
            }
            
            // Decode JWT token to get username
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                Log.w("ChatRoomActivity", "Invalid JWT token format");
                return "User";
            }
            
            // Decode payload (base64)
            String payload = parts[1];
            // Add padding if needed
            while (payload.length() % 4 != 0) {
                payload += "=";
            }
            
            byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
            String payloadJson = new String(decodedBytes);
            
            // Parse JSON to get username
            org.json.JSONObject jsonObject = new org.json.JSONObject(payloadJson);
            String username = jsonObject.optString("username", "User");
            
            Log.d("ChatRoomActivity", "Decoded username from JWT: " + username);
            return username;
            
        } catch (Exception e) {
            Log.e("ChatRoomActivity", "Error decoding JWT token: " + e.getMessage());
            return "User";
        }
    }

    private void updateConversationData(String lastMessage, long timestamp) {
        if (conversationId == null) return;
        
        Log.d("ChatRoomActivity", "Updating conversation data: " + conversationId);
        
        // Extract customer and admin IDs from conversationId
        String[] parts = conversationId.split("_");
        String customerId = parts.length > 1 ? parts[1] : selfUserId;
        String adminId = parts.length > 0 ? parts[0] : "admin_uid";
        
        // Get customer name from JWT token
        String customerName = getUsernameFromToken();
        Log.d("ChatRoomActivity", "Final customer name from JWT: " + customerName);
        
        // Create full conversation object
        com.example.onlyfanshop.model.chat.Conversation conv = new com.example.onlyfanshop.model.chat.Conversation(
                conversationId,
                customerId,
                adminId,
                customerName, // Use real customer name
                "Admin",
                lastMessage,
                timestamp
        );
        
        // Save full conversation object
        FirebaseDatabase.getInstance().getReference("conversations")
                .child(conversationId)
                .setValue(conv)
                .addOnSuccessListener(aVoid -> Log.d("ChatRoomActivity", "Conversation updated successfully"))
                .addOnFailureListener(e -> Log.e("ChatRoomActivity", "Failed to update conversation: " + e.getMessage()));
    }

    private void syncMessageToDatabase(Message message) {
        // Sync message to MySQL database via backend API
        Log.d("ChatRoomActivity", "Syncing message to database: " + message.getText());
        
        try {
            // Get JWT token for authentication
            android.content.SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String token = prefs.getString("jwt_token", null);
            
            if (token == null || token.isEmpty()) {
                Log.w("ChatRoomActivity", "No JWT token found, cannot sync to database");
                return;
            }
            
            // Extract receiver ID (admin)
            String[] parts = conversationId.split("_");
            String receiverId = parts.length > 0 ? parts[0] : "admin_uid";
            
            // Call backend API to sync message
            syncMessageToBackend(message.getSenderId(), receiverId, message.getText(), token);
            
        } catch (Exception e) {
            Log.e("ChatRoomActivity", "Error syncing message to database: " + e.getMessage());
        }
    }
    
    private void syncMessageToBackend(String senderId, String receiverId, String message, String token) {
        // TODO: Implement Retrofit API call
        // POST /api/chat/sync-message
        // Headers: Authorization: Bearer {token}
        // Body: { senderId, receiverId, message }
        
        Log.d("ChatRoomActivity", "Calling backend API to sync message:");
        Log.d("ChatRoomActivity", "  - senderId: " + senderId);
        Log.d("ChatRoomActivity", "  - receiverId: " + receiverId);
        Log.d("ChatRoomActivity", "  - message: " + message);
        Log.d("ChatRoomActivity", "  - token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        // For now, just log the API call
        // In production, implement actual Retrofit call here
    }

    private void ensureConversationExists() {
        if (conversationId == null) return;
        
        Log.d("ChatRoomActivity", "Ensuring conversation exists: " + conversationId);
        DatabaseReference conversationsRef = FirebaseDatabase.getInstance().getReference("conversations");
        DatabaseReference convRef = conversationsRef.child(conversationId);
        
        convRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Log.d("ChatRoomActivity", "Creating conversation: " + conversationId);
                // Extract customer and admin IDs from conversationId
                String[] parts = conversationId.split("_");
                String customerId = parts.length > 1 ? parts[1] : selfUserId;
                String adminId = parts.length > 0 ? parts[0] : "admin_uid";
                
                // Get customer name from JWT token
                String customerName = getUsernameFromToken();
                Log.d("ChatRoomActivity", "Final customer name from JWT: " + customerName);
                
                com.example.onlyfanshop.model.chat.Conversation conv = new com.example.onlyfanshop.model.chat.Conversation(
                        conversationId,
                        customerId,
                        adminId,
                        customerName, // Use real customer name
                        "Admin",
                        "Conversation started",
                        System.currentTimeMillis()
                );
                
                convRef.setValue(conv)
                    .addOnSuccessListener(aVoid -> Log.d("ChatRoomActivity", "Conversation created successfully"))
                    .addOnFailureListener(e -> Log.e("ChatRoomActivity", "Failed to create conversation: " + e.getMessage()));
            } else {
                Log.d("ChatRoomActivity", "Conversation already exists");
            }
        }).addOnFailureListener(e -> {
            Log.e("ChatRoomActivity", "Failed to check conversation existence: " + e.getMessage());
        });
    }

    private static class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.VH> {
        private final List<Message> data;
        private final String selfUserId;
        private final java.util.Map<String, String> reactions = new java.util.HashMap<>();
        MessagesAdapter(List<Message> data, String selfUserId) { this.data = data; this.selfUserId = selfUserId; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            Message m = data.get(position);
            // Fix: Customer messages should be on the left (incoming), Admin messages on the right (outgoing)
            boolean isMine = m.getSenderId() != null && m.getSenderId().equals(selfUserId);
            Log.d("MessagesAdapter", "Message from: " + m.getSenderId() + ", selfUserId: " + selfUserId + ", isMine: " + isMine);
            
            // For admin view: customer messages (incoming) on left, admin messages (outgoing) on right
            // For customer view: customer messages (outgoing) on right, admin messages (incoming) on left
            h.containerIncoming.setVisibility(isMine ? View.GONE : View.VISIBLE);
            h.containerOutgoing.setVisibility(isMine ? View.VISIBLE : View.GONE);
            if (isMine) {
                h.tvOutgoing.setText(m.getText());
                h.tvTimeOutgoing.setText(android.text.format.DateFormat.format("HH:mm", m.getTimestamp()));
                String r = reactions.get(m.getId());
                h.tvReactionOutgoing.setText(r != null ? r : "");
                h.bubbleOutgoing.setOnLongClickListener(v -> { showReactionPopup(v, m); return true; });
            } else {
                h.tvIncoming.setText(m.getText());
                h.tvTimeIncoming.setText(android.text.format.DateFormat.format("HH:mm", m.getTimestamp()));
                String r = reactions.get(m.getId());
                h.tvReactionIncoming.setText(r != null ? r : "");
                h.bubbleIncoming.setOnLongClickListener(v -> { showReactionPopup(v, m); return true; });
            }
        }

        private void showReactionPopup(View anchor, Message m) {
            View content = LayoutInflater.from(anchor.getContext()).inflate(R.layout.view_reaction_bar, null);
            PopupWindow popup = new PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popup.setElevation(8f);
            View.OnClickListener pick = v -> {
                String emoji = ((TextView) v).getText().toString();
                if ("＋".equals(emoji)) {
                    popup.dismiss();
                    return;
                }
                reactions.put(m.getId(), emoji);
                notifyDataSetChanged();
                popup.dismiss();
            };
            int[] ids = new int[]{R.id.r1,R.id.r2,R.id.r3,R.id.r4,R.id.r5,R.id.r6,R.id.r7};
            for (int id : ids) content.findViewById(id).setOnClickListener(pick);
            int[] loc = new int[2];
            anchor.getLocationOnScreen(loc);
            anchor.post(() -> popup.showAsDropDown(anchor, -anchor.getWidth()/2, -anchor.getHeight()*3));
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            ViewGroup containerIncoming, containerOutgoing;
            TextView tvIncoming, tvOutgoing, tvTimeIncoming, tvTimeOutgoing, tvReactionIncoming, tvReactionOutgoing;
            View bubbleIncoming, bubbleOutgoing;
            VH(@NonNull View itemView) {
                super(itemView);
                containerIncoming = itemView.findViewById(R.id.containerIncoming);
                containerOutgoing = itemView.findViewById(R.id.containerOutgoing);
                tvIncoming = itemView.findViewById(R.id.tvIncoming);
                tvOutgoing = itemView.findViewById(R.id.tvOutgoing);
                tvTimeIncoming = itemView.findViewById(R.id.tvTimeIncoming);
                tvTimeOutgoing = itemView.findViewById(R.id.tvTimeOutgoing);
                tvReactionIncoming = itemView.findViewById(R.id.tvReactionIncoming);
                tvReactionOutgoing = itemView.findViewById(R.id.tvReactionOutgoing);
                bubbleIncoming = itemView.findViewById(R.id.bubbleIncoming);
                bubbleOutgoing = itemView.findViewById(R.id.bubbleOutgoing);
            }
        }
    }
}


