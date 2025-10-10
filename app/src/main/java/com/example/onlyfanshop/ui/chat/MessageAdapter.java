package com.example.onlyfanshop.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlyfanshop.R;
import com.example.onlyfanshop.model.chat.ChatMessage;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<ChatMessage> messages;

    public MessageAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    // onBindViewHolder implemented below (bubble binding)

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        ViewGroup containerIncoming, containerOutgoing;
        TextView tvIncoming, tvOutgoing;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            containerIncoming = itemView.findViewById(R.id.containerIncoming);
            containerOutgoing = itemView.findViewById(R.id.containerOutgoing);
            tvIncoming = itemView.findViewById(R.id.tvIncoming);
            tvOutgoing = itemView.findViewById(R.id.tvOutgoing);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        boolean isMine = "User1".equals(message.getMessageUser());
        holder.containerIncoming.setVisibility(isMine ? View.GONE : View.VISIBLE);
        holder.containerOutgoing.setVisibility(isMine ? View.VISIBLE : View.GONE);
        if (isMine) {
            holder.tvOutgoing.setText(message.getMessageText());
        } else {
            holder.tvIncoming.setText(message.getMessageText());
        }
    }
}


