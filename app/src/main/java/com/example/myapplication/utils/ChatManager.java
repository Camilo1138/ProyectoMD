package com.example.myapplication.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChatManager {
    private static ChatManager instance;
    private FirebaseFirestore db;
    private String currentUserId;


    private ChatManager() {
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static synchronized ChatManager getInstance() {
        if (instance == null) {
            instance = new ChatManager();
        }
        return instance;
    }

    public void createNewChat(String otherUserId, String otherUserName, String otherUserPublicKey,
                              OnChatCreatedListener listener) {
        // Verificar si el chat ya existe
        db.collection("chats")
                .where(Filter.or(
                        Filter.and(
                                Filter.equalTo("user1", db.collection("users").document(currentUserId)),
                                Filter.equalTo("user2", db.collection("users").document(otherUserId))
                        ),
                        Filter.and(
                                Filter.equalTo("user1", db.collection("users").document(otherUserId)),
                                Filter.equalTo("user2", db.collection("users").document(currentUserId))
                        )
                ))
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Chat ya existe
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            listener.onChatExists(doc.getId(), otherUserId, otherUserName);
                        } else {
                            // Crear nuevo chat
                            Map<String, Object> chat = new HashMap<>();
                            chat.put("user1", db.collection("users").document(currentUserId));
                            chat.put("user2", db.collection("users").document(otherUserId));
                            chat.put("lastMessage", "");
                            chat.put("lastUpdate", FieldValue.serverTimestamp());
                            chat.put("encrypted", true);

                            db.collection("chats")
                                    .add(chat)
                                    .addOnSuccessListener(documentReference -> {
                                        listener.onChatCreated(documentReference.getId(), otherUserId, otherUserName);
                                    })
                                    .addOnFailureListener(e -> {
                                        listener.onFailure(e);
                                    });
                        }
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public interface OnChatCreatedListener {
        void onChatCreated(String chatId, String otherUserId, String otherUserName);
        void onChatExists(String chatId, String otherUserId, String otherUserName);
        void onFailure(Exception e);
    }
}