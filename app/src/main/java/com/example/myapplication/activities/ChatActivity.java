package com.example.myapplication.activities;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.MessagesAdapter;
import com.example.myapplication.models.ChatMessage;
import com.example.myapplication.models.Message;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.AESUtils;
import com.example.myapplication.utils.KeyConversionUtils;
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
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Timestamp;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getIntent().getStringExtra("otherUserName"));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            // Configurar RecyclerView
            messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
            messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            messagesAdapter = new MessagesAdapter(currentUserId);
            messagesRecyclerView.setAdapter(messagesAdapter);

            // Configurar input y botón
            messageEditText = findViewById(R.id.messageEditText);
            sendButton = findViewById(R.id.sendButton);
            sendButton.setOnClickListener(v -> {
                sendMessage();
                loadMessages();
            });

            // Cargar mensajes
            loadMessages();
        }
        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                finish(); // o puedes usar onBackPressed() si estás en una API anterior a 33
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
        /*private void loadMessages() {
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
                com.example.myapplication.utils.PublicKey customKey = PublicKey.fromString(otherUserPublicKey);
                java.security.PublicKey javaKey = KeyConversionUtils.toJavaKey(customKey);
                String encryptedContent = RSAUtils.encrypt(content, javaKey);



                // Crear y guardar mensaje
                ChatMessage message = new ChatMessage();
                message.setChatId(db.collection("chats").document(chatId));
                message.setSenderId(db.collection("usuarios").document(currentUserId));
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

     */
        private void sendMessage() {
            String content = messageEditText.getText().toString().trim();
            if (content.isEmpty()) return;

            try {
                // 1. Validar clave pública del destinatario
                if (otherUserPublicKey == null || otherUserPublicKey.isEmpty()) {
                    throw new IllegalArgumentException("La clave pública del destinatario no está disponible.");
                }

                // 2. Convertir clave pública personalizada a java.security.PublicKey
                com.example.myapplication.utils.PublicKey customKey = com.example.myapplication.utils.PublicKey.fromString(otherUserPublicKey);
                java.security.PublicKey standardPublicKey = KeyConversionUtils.toJavaKey(customKey);

                // 3. Generar clave AES (128 bits)
                SecretKey aesKey = AESUtils.generateKey();

                // 4. Cifrar el mensaje con AES
                String encryptedMessage = AESUtils.encrypt(content, aesKey);

                // 5. Obtener bytes de la clave AES (16 bytes para AES-128)
                byte[] aesKeyBytes = aesKey.getEncoded();

                // 6. Cifrar clave AES con RSA (solo los bytes, no base64)
                String encryptedAESKey = RSAUtils.encryptAESKey(aesKeyBytes, customKey);

                // 7. Crear y guardar mensaje en Firestore
                ChatMessage message = new ChatMessage();
                message.setChatId(db.collection("chats").document(chatId));
                message.setSenderId(db.collection("usuarios").document(currentUserId));
                message.setContent(encryptedMessage);
                message.setEncryptedKey(encryptedAESKey);
                message.setTimestamp(new Date());
                message.setStatus("sent");

                db.collection("messages").add(message)
                        .addOnSuccessListener(docRef -> {
                            messageEditText.setText("");
                            updateChatLastMessage("Nuevo mensaje cifrado");
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al enviar mensaje: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Chat", "Fallo al guardar mensaje", e);
                        });

            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Chat", "Error en sendMessage", e);
            }
        }


    private void loadMessages() {
            messagesListener = db.collection("messages")
                    .whereEqualTo("chatId", db.collection("chats").document(chatId))
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) return;

                        List<ChatMessage> messages = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessage message = doc.toObject(ChatMessage.class);
                            if (message != null && message.getSenderId().getId().equals(otherUserId)) {
                                try {
                                    // 1. Obtener clave privada
                                    PrivateKey privateKey = SecureStorage.getPrivateKey();

                                    // 2. Descifrar clave AES cifrada (como bytes)
                                    byte[] aesKeyBytes = RSAUtils.decryptAESKey(message.getEncryptedKey(), privateKey);
                                    SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

                                    // 3. Descifrar el contenido del mensaje
                                    String decrypted = AESUtils.decrypt(message.getContent(), aesKey);
                                    message.setContent(decrypted);
                                } catch (Exception e) {
                                    message.setContent("[Error al descifrar]");
                                    Log.e("Chat", "Descifrado fallido", e);
                                }
                            }
                            messages.add(message);
                        }
                        messagesAdapter.setMessages(messages);
                    });
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