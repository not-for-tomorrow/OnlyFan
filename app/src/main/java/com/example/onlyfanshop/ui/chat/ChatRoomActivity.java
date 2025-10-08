package com.example.onlyfanshop.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    private Button btnSend;
    private final List<Message> messages = new ArrayList<>();
    private MessagesAdapter adapter;
    private DatabaseReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        conversationId = getIntent().getStringExtra("conversationId");
        recyclerView = findViewById(R.id.recyclerMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessagesAdapter(messages);
        recyclerView.setAdapter(adapter);

        messagesRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(conversationId)
                .child("messages");

        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message m = snapshot.getValue(Message.class);
                if (m != null) {
                    messages.add(m);
                    adapter.notifyItemInserted(messages.size() - 1);
                    recyclerView.scrollToPosition(messages.size() - 1);
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        String uid = FirebaseAuth.getInstance().getUid();
        String messageId = messagesRef.push().getKey();
        long now = System.currentTimeMillis();
        Message message = new Message(messageId, conversationId, uid, null, text, now);
        if (messageId != null) {
            messagesRef.child(messageId).setValue(message);
            // Also update conversation last message for the list screen
            FirebaseDatabase.getInstance().getReference("conversations")
                    .child(conversationId)
                    .child("lastMessage").setValue(text);
            FirebaseDatabase.getInstance().getReference("conversations")
                    .child(conversationId)
                    .child("lastTimestamp").setValue(now);
        }
        edtMessage.setText("");
    }

    private static class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.VH> {
        private final List<Message> data;
        MessagesAdapter(List<Message> data) { this.data = data; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.text1.setText(data.get(position).getText());
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView text1;
            VH(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}


