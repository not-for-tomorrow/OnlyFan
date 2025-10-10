package com.example.onlyfanshop.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
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
        }

        selfUserId = FirebaseAuth.getInstance().getUid();
        if (selfUserId == null) selfUserId = "me";

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MessagesAdapter(messages, selfUserId);
        recyclerView.setAdapter(adapter);

        if (conversationId != null) {
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
        } else {
            loadMockMessages();
        }

        // Fallback: if after a short delay there is still no message, seed mock so UI shows bottom aligned bubbles
        recyclerView.postDelayed(() -> {
            if (messages.isEmpty()) {
                loadMockMessages();
            }
        }, 300);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        String uid = selfUserId;
        if (conversationId == null || messagesRef == null) {
            // Offline/mock mode
            Message local = new Message(java.util.UUID.randomUUID().toString(), "mock", uid, null, text, System.currentTimeMillis());
            messages.add(local);
            adapter.notifyItemInserted(messages.size() - 1);
            recyclerView.scrollToPosition(messages.size() - 1);
            edtMessage.setText("");
            return;
        }
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

    private void loadMockMessages() {
        long now = System.currentTimeMillis();
        String me = selfUserId;
        String other = "admin";
        messages.clear();
        messages.add(new Message("1", "mock", other, "Doctor", "Chào bạn, mình có thể giúp gì?", now - 5 * 60 * 1000));
        messages.add(new Message("2", "mock", me, "Me", "Em bị đau đầu 3 ngày nay.", now - 4 * 60 * 1000));
        messages.add(new Message("3", "mock", other, "Doctor", "Bạn nên nghỉ ngơi và uống nhiều nước.", now - 3 * 60 * 1000));
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(messages.size() - 1);
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
            boolean isMine = m.getSenderId() != null && m.getSenderId().equals(selfUserId);
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


