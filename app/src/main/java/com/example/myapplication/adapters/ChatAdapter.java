package com.example.myapplication.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Chat;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(int position);
    }

    public ChatAdapter(List<Chat> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.bind(chat);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void updateChats(List<Chat> newChats) {
        chatList = newChats;
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView chatName, lastMessage, time;
        private ImageView avatar;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            chatName = itemView.findViewById(R.id.chatName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            time = itemView.findViewById(R.id.time);
        }

        public void bind(Chat chat) {
            // Aquí deberías cargar el nombre del otro participante y su avatar
            chatName.setText("Chat"); // Temporal, necesitarías cargar el nombre real

            lastMessage.setText(chat.getLastMessage());

            if (chat.getLastUpdate() != null) {
                time.setText(DateUtils.formatDateTime(itemView.getContext(),
                        chat.getLastUpdate().getTime(),
                        DateUtils.FORMAT_SHOW_TIME));
            }
        }
    }
}