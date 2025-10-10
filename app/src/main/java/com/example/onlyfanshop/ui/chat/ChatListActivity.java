package com.example.onlyfanshop.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.model.chat.Conversation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private android.widget.EditText edtSearch;
    private final List<Conversation> conversations = new ArrayList<>();
    private ConversationAdapter adapter;
    private DatabaseReference conversationsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        recyclerView = findViewById(R.id.recyclerConversations);
        edtSearch = findViewById(R.id.edtSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter(conversations, c -> {
            Intent i = new Intent(this, ChatRoomActivity.class);
            i.putExtra("conversationId", c.getId());
            i.putExtra("customerName", c.getCustomerName());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                adapter.filter(s.toString());
            }
        });

        currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            // No login/Firebase yet -> show mock conversations
            loadMockConversations();
        } else {
            conversationsRef = FirebaseDatabase.getInstance().getReference("conversations");
            ensureConversationExistsWithAdmin();
            conversationsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    conversations.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Conversation c = child.getValue(Conversation.class);
                        if (c != null) {
                            if (currentUserId.equals(c.getCustomerId()) || currentUserId.equals(c.getAdminId())) {
                                conversations.add(c);
                            }
                        }
                    }
                    Collections.sort(conversations, Comparator.comparingLong(Conversation::getLastTimestamp).reversed());
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void ensureConversationExistsWithAdmin() {
        if (currentUserId == null) return;
        final String adminUid = getString(R.string.admin_uid); // configure in strings.xml
        if (adminUid == null || adminUid.isEmpty()) return;
        String conversationId = buildConversationId(currentUserId, adminUid);

        DatabaseReference convRef = conversationsRef.child(conversationId);
        convRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                com.example.onlyfanshop.model.chat.Conversation conv = new com.example.onlyfanshop.model.chat.Conversation(
                        conversationId,
                        currentUserId,
                        adminUid,
                        null,
                        "Admin",
                        "Hãy bắt đầu trò chuyện với chúng tôi",
                        System.currentTimeMillis()
                );
                convRef.setValue(conv);
            }
        });
    }

    private void loadMockConversations() {
        conversations.clear();
        long now = System.currentTimeMillis();
        conversations.add(new Conversation(
                UUID.randomUUID().toString(),
                "u_me", "u_doc1",
                null,
                "Dr. Denton Cooley",
                "Hello, how can I help you?",
                now - 2 * 60 * 1000
        ));
        conversations.add(new Conversation(
                UUID.randomUUID().toString(),
                "u_me", "u_doc2",
                null,
                "Dr. Valentin Fuster",
                "Please take rest and drink water.",
                now - 60 * 60 * 1000
        ));
        conversations.add(new Conversation(
                UUID.randomUUID().toString(),
                "u_me", "u_doc3",
                null,
                "Dr. Sarah Jarvis",
                "We can schedule a check-up tomorrow.",
                now - 24 * 60 * 60 * 1000
        ));
        Collections.sort(conversations, Comparator.comparingLong(Conversation::getLastTimestamp).reversed());
        adapter.notifyDataSetChanged();
    }

    private static String buildConversationId(@NonNull String a, @NonNull String b) {
        // Deterministic pair id to avoid duplicates
        return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
    }

    private static class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.VH> {
        interface OnClick { void onClick(Conversation c); }
        private final List<Conversation> data;
        private final List<Conversation> all;
        private final OnClick onClick;
        ConversationAdapter(List<Conversation> data, OnClick onClick) {
            this.data = data; this.all = new ArrayList<>(data); this.onClick = onClick;
        }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            Conversation c = data.get(position);
            String name = c.getCustomerName() != null ? c.getCustomerName() : (c.getAdminName() != null ? c.getAdminName() : "");
            h.tvName.setText(name);
            h.tvLastMessage.setText(c.getLastMessage());
            // simple time formatting similar to "Yesterday" or date
            java.text.DateFormat df = android.text.format.DateFormat.getDateFormat(h.itemView.getContext());
            java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(h.itemView.getContext());
            java.util.Calendar cal = java.util.Calendar.getInstance();
            java.util.Calendar msg = java.util.Calendar.getInstance();
            msg.setTimeInMillis(c.getLastTimestamp());
            String timeLabel;
            if (android.text.format.DateUtils.isToday(c.getLastTimestamp())) {
                timeLabel = tf.format(new java.util.Date(c.getLastTimestamp()));
            } else {
                cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
                if (msg.get(java.util.Calendar.YEAR) == cal.get(java.util.Calendar.YEAR)
                        && msg.get(java.util.Calendar.DAY_OF_YEAR) == cal.get(java.util.Calendar.DAY_OF_YEAR)) {
                    timeLabel = "Yesterday";
                } else {
                    timeLabel = df.format(new java.util.Date(c.getLastTimestamp()));
                }
            }
            h.tvTime.setText(timeLabel);
            h.itemView.setOnClickListener(v -> onClick.onClick(c));
        }
        @Override public int getItemCount() { return data.size(); }

        void filter(String query) {
            data.clear();
            if (query == null || query.trim().isEmpty()) {
                data.addAll(all);
            } else {
                String q = query.toLowerCase();
                for (Conversation c : all) {
                    String name = c.getCustomerName() != null ? c.getCustomerName() : (c.getAdminName() != null ? c.getAdminName() : "");
                    if (name.toLowerCase().contains(q) || (c.getLastMessage() != null && c.getLastMessage().toLowerCase().contains(q))) {
                        data.add(c);
                    }
                }
            }
            notifyDataSetChanged();
        }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvLastMessage, tvTime;
            VH(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
                tvTime = itemView.findViewById(R.id.tvTime);
            }
        }
    }
}


