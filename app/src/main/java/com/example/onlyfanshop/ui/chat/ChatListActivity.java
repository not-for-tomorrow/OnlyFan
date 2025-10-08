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

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private final List<Conversation> conversations = new ArrayList<>();
    private ConversationAdapter adapter;
    private DatabaseReference conversationsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        recyclerView = findViewById(R.id.recyclerConversations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter(conversations, c -> {
            Intent i = new Intent(this, ChatRoomActivity.class);
            i.putExtra("conversationId", c.getId());
            i.putExtra("customerName", c.getCustomerName());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getUid();
        conversationsRef = FirebaseDatabase.getInstance().getReference("conversations");

        ensureConversationExistsWithAdmin();

        conversationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                conversations.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Conversation c = child.getValue(Conversation.class);
                    if (c != null) {
                        if (currentUserId == null || currentUserId.equals(c.getCustomerId()) || currentUserId.equals(c.getAdminId())) {
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

    private static String buildConversationId(@NonNull String a, @NonNull String b) {
        // Deterministic pair id to avoid duplicates
        return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
    }

    private static class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.VH> {
        interface OnClick { void onClick(Conversation c); }
        private final List<Conversation> data;
        private final OnClick onClick;
        ConversationAdapter(List<Conversation> data, OnClick onClick) {
            this.data = data; this.onClick = onClick;
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
            h.itemView.setOnClickListener(v -> onClick.onClick(c));
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvLastMessage;
            VH(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            }
        }
    }
}


