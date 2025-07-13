package com.example.myapplication.adapters;

import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activities.ChatActivity;
import com.example.myapplication.models.Chat;
import com.example.myapplication.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnChatClickListener listener;
    private String currentUserId;


    public interface OnChatClickListener {
        void onChatClick(int position);
    }

    public ChatAdapter(List<Chat> chatList, String currentUserId, OnChatClickListener listener) {
        this.chatList = chatList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }
   /* public ChatAdapter(List<Chat> chatList,  OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    */


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

        DocumentReference otherUserRef = chat.getUser1().getId().equals(currentUserId)
                ? chat.getUser2() : chat.getUser1();

         FirebaseFirestore db = FirebaseFirestore.getInstance();
         otherUserRef = db.collection("usuarios").document(otherUserRef.getId());

        // Obtener y mostrar el nombre del otro usuario
        otherUserRef.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                holder.chatName.setText(user.getName());
            } else {
                holder.chatName.setText("Usuario desconocido");
            }
        });

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
            // El nombre lo seteamos aparte al cargar el User

            if (chat.getLastMessage() != null) {
                lastMessage.setText(chat.getLastMessage());
            } else {
                lastMessage.setText("Sin mensajes");
            }

            if (chat.getLastUpdate() != null) {
                time.setText(DateUtils.formatDateTime(itemView.getContext(),
                        chat.getLastUpdate().getTime(),
                        DateUtils.FORMAT_SHOW_TIME));
            } else {
                time.setText("");
            }
        }
    }
}
