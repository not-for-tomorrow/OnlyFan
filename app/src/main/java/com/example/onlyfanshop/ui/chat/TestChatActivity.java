package com.example.onlyfanshop.ui.chat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

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

public class TestChatActivity extends AppCompatActivity {

    private RecyclerView rvAdmin, rvCustomer;
    private EditText edtAdmin, edtCustomer;
    private Button btnSendAdmin, btnSendCustomer;
    private final List<Message> msgsAdmin = new ArrayList<>();
    private final List<Message> msgsCustomer = new ArrayList<>();
    private RecyclerView.Adapter<?> adapterAdmin, adapterCustomer;
    private DatabaseReference messagesRef;
    private String conversationId;
    private String adminUid;
    private String customerUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_chat);

        rvAdmin = findViewById(R.id.rvAdmin);
        rvCustomer = findViewById(R.id.rvCustomer);
        edtAdmin = findViewById(R.id.edtAdmin);
        edtCustomer = findViewById(R.id.edtCustomer);
        btnSendAdmin = findViewById(R.id.btnSendAdmin);
        btnSendCustomer = findViewById(R.id.btnSendCustomer);

        rvAdmin.setLayoutManager(new LinearLayoutManager(this));
        rvCustomer.setLayoutManager(new LinearLayoutManager(this));
        adapterAdmin = new SimpleMsgAdapter(msgsAdmin, /*isRightAligned=*/true);
        adapterCustomer = new SimpleMsgAdapter(msgsCustomer, /*isRightAligned=*/false);
        rvAdmin.setAdapter(adapterAdmin);
        rvCustomer.setAdapter(adapterCustomer);

        adminUid = getString(R.string.admin_uid);
        customerUid = FirebaseAuth.getInstance().getUid();
        if (customerUid == null) customerUid = "TEST_CUSTOMER";
        conversationId = customerUid.compareTo(adminUid) < 0 ? customerUid + "_" + adminUid : adminUid + "_" + customerUid;

        messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(conversationId).child("messages");

        // Ensure persistence of listeners for realtime behavior
        FirebaseDatabase.getInstance().getReference("chats").keepSynced(true);

        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message m = snapshot.getValue(Message.class);
                if (m == null) return;
                if (m.getSenderId() != null && m.getSenderId().equals(adminUid)) {
                    msgsAdmin.add(m);
                    adapterAdmin.notifyItemInserted(msgsAdmin.size()-1);
                    rvAdmin.scrollToPosition(msgsAdmin.size()-1);
                } else {
                    msgsCustomer.add(m);
                    adapterCustomer.notifyItemInserted(msgsCustomer.size()-1);
                    rvCustomer.scrollToPosition(msgsCustomer.size()-1);
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnSendAdmin.setOnClickListener(v -> send(adminUid, edtAdmin));
        btnSendCustomer.setOnClickListener(v -> send(customerUid, edtCustomer));
    }

    private void send(String senderUid, EditText editor) {
        String text = editor.getText().toString().trim();
        if (text.isEmpty()) return;
        String id = messagesRef.push().getKey();
        long now = System.currentTimeMillis();
        Message m = new Message(id, conversationId, senderUid, null, text, now);
        if (id != null) messagesRef.child(id).setValue(m);
        // update conversation summary
        FirebaseDatabase.getInstance().getReference("conversations").child(conversationId).child("lastMessage").setValue(text);
        FirebaseDatabase.getInstance().getReference("conversations").child(conversationId).child("lastTimestamp").setValue(now);
        editor.setText("");
    }

    private static class SimpleMsgAdapter extends RecyclerView.Adapter<SimpleMsgAdapter.VH> {
        private final List<Message> data;
        private final boolean right;
        SimpleMsgAdapter(List<Message> data, boolean right) { this.data = data; this.right = right; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.widget.TextView tv = new android.widget.TextView(parent.getContext());
            tv.setPadding(24, 16, 24, 16);
            tv.setBackgroundColor(0xFFE0F7FA);
            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = 24; lp.rightMargin = 24; lp.topMargin = 8; lp.bottomMargin = 8;
            lp.gravity = right ? android.view.Gravity.END : android.view.Gravity.START;
            tv.setLayoutParams(lp);
            return new VH(tv);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            ((android.widget.TextView) h.itemView).setText(data.get(position).getText());
        }
        @Override public int getItemCount() { return data.size(); }
        static class VH extends RecyclerView.ViewHolder { VH(@NonNull android.view.View itemView) { super(itemView); } }
    }
}


