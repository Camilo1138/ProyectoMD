package com.example.myapplication.activities;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.MessagesAdapter;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.Message;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.PrivateKey;
import com.example.myapplication.utils.PublicKey;
import com.example.myapplication.utils.RSAUtils;
import com.example.myapplication.utils.SecureStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.Timestamp;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private MessagesAdapter messagesAdapter;
    private String chatId, currentUserId, otherUserId, otherUserPublicKey;
    private FirebaseFirestore db;
    private ListenerRegistration messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Obtener datos del intent
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserPublicKey = getIntent().getStringExtra("otherUserPublicKey");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("otherUserName"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configurar RecyclerView
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesAdapter = new MessagesAdapter(currentUserId);
        messagesRecyclerView.setAdapter(messagesAdapter);

        // Configurar input y botón
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> sendMessage());

        // Cargar mensajes
        loadMessages();
    }

    private void loadMessages() {
        messagesListener = db.collection("messages")
                .whereEqualTo("chatId", db.collection("chats").document(chatId))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error al cargar mensajes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<ChatMessage> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        if (message != null) {
                            message.setMessageId(doc.getId());

                            // Desencriptar mensajes recibidos
                            if (message.getSenderId().getId().equals(otherUserId)) {
                                try {
                                    PrivateKey privateKey = SecureStorage.getPrivateKey();
                                    String decrypted = RSAUtils.decrypt(message.getContent(), privateKey);
                                    message.setContent(decrypted);
                                } catch (Exception e) {
                                    message.setContent("[Mensaje cifrado no legible]");
                                    Log.e("Chat", "Error al desencriptar", e);
                                }
                            }
                            messages.add(message);
                        }
                    }
                    messagesAdapter.setMessages(messages);
                    messagesRecyclerView.scrollToPosition(messages.size() - 1);
                });
    }

    private void sendMessage() {
        String content = messageEditText.getText().toString().trim();
        if (content.isEmpty()) return;

        try {
            // Verificar que la clave pública es válida
            if (otherUserPublicKey == null || otherUserPublicKey.isEmpty()) {
                throw new IllegalArgumentException("La clave pública del destinatario no está disponible");
            }

            // Cifrar mensaje
            PublicKey publicKey = PublicKey.fromString(otherUserPublicKey);
            String encryptedContent = RSAUtils.encrypt(content, publicKey);

            // Crear y guardar mensaje
            ChatMessage message = new ChatMessage();
            message.setChatId(db.collection("chats").document(chatId));
            message.setSenderId(db.collection("users").document(currentUserId));
            message.setContent(encryptedContent);
            message.setTimestamp(new Date());
            message.setStatus("sent");

            db.collection("messages").add(message)
                    .addOnSuccessListener(documentReference -> {
                        messageEditText.setText("");
                        updateChatLastMessage(encryptedContent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al enviar mensaje: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Chat", "Error al enviar mensaje", e);
        }
    }

    private void updateChatLastMessage(String lastMessage) {
        db.collection("chats").document(chatId)
                .update(
                        "lastMessage", lastMessage,
                        "lastUpdate", FieldValue.serverTimestamp()
                );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}