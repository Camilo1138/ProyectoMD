package com.example.myapplication.activities;

import static android.content.ContentValues.TAG;
import static com.example.myapplication.utils.SecureStorage.ENCRYPTED_KEY;
import static com.example.myapplication.utils.SecureStorage.getEncryptedPrefs;
import static com.example.myapplication.utils.SecureStorage.getOrCreateAESKey;
import static com.example.myapplication.utils.SecureStorage.hasAESKey;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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
import com.example.myapplication.utils.SecureStorageException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Timestamp;
import java.security.spec.PKCS8EncodedKeySpec;
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

            PrivateKey privateKey = null;
            try {
                privateKey = SecureStorage.getPrivateKey(getApplicationContext());
                if (privateKey == null) {
                    Toast.makeText(this, "⚠️ No se encontró clave privada en este dispositivo.\nNo podrás leer mensajes cifrados.", Toast.LENGTH_LONG).show();
                }
            } catch (SecureStorageException e) {
                Log.e("SecureStorage", "Error accediendo a la clave privada", e);
                Toast.makeText(this, "⚠️ Error al acceder a la clave privada.\nNo podrás leer mensajes cifrados.", Toast.LENGTH_LONG).show();
            }

            // Verifica la disponibilidad de claves al iniciar:
            if (!hasAESKey()) {
                try {
                    SecureStorage.getAESKey();
                } catch (Exception e) {
                    Log.e("Security", "Failed to generate AES key", e);
                    // Mostrar alerta al usuario sobre funcionalidad limitada
                }
            }
        }
        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                finish(); // o puedes usar onBackPressed() si estás en una API anterior a 33
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

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
                    if (error != null) {
                        Log.e("ChatActivity", "Error al cargar mensajes", error);
                        showErrorToast("Error al cargar mensajes");
                        return;
                    }

                    List<ChatMessage> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        if (message != null) {
                            processMessage(message);
                        }
                        messages.add(message);
                    }
                    messagesAdapter.setMessages(messages);
                    scrollToLastMessage();
                });
    }
    private void processMessage(ChatMessage message) {
        // Solo desciframos mensajes recibidos (del otro usuario)
        if (message.getSenderId().getId().equals(otherUserId)) {
            try {
                // 1. Obtener clave privada de forma segura
                PrivateKey privateKey = SecureStorage.obtenerClavePrivadaSegura(getApplicationContext());

                if (privateKey == null) {
                    // 2. Si no existe localmente, intentar recuperar de Firebase
                    recoverPrivateKeyFromFirestore(message);
                    return;
                }
                // 2. Descifrar clave AES
                byte[] aesKeyBytes = RSAUtils.decryptAESKey(message.getEncryptedKey(), privateKey);
                SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

                // 3. Descifrar contenido
                String decryptedContent = AESUtils.decrypt(message.getContent(), aesKey);
                message.setContent(decryptedContent);

            } catch (SecureStorageException e) {
                Log.e("ChatActivity", "Error de seguridad al descifrar", e);
                message.setContent("[Error: clave privada no disponible]");
                showKeyWarning();
            } catch (Exception e) {
                Log.e("ChatActivity", "Error al descifrar mensaje", e);
                message.setContent("[Mensaje cifrado]");
            }
        }
    }

    private void showErrorToast(String message) {
        runOnUiThread(() -> Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void scrollToLastMessage() {
        if (messagesAdapter.getItemCount() > 0) {
            messagesRecyclerView.post(() ->
                    messagesRecyclerView.smoothScrollToPosition(messagesAdapter.getItemCount() - 1));
        }
    }

    private void showKeyWarning() {
        runOnUiThread(() -> {
            new AlertDialog.Builder(ChatActivity.this)
                    .setTitle("Advertencia de seguridad")
                    .setMessage("No se puede acceder a las claves de cifrado. Algunos mensajes no serán legibles.")
                    .setPositiveButton("Entendido", null)
                    .show();
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
    private void recoverPrivateKeyFromFirestore(ChatMessage message) {
        runOnUiThread(() ->
                Toast.makeText(this, "Recuperando clave privada de Firebase...", Toast.LENGTH_SHORT).show());

        // 1. Obtener el documento del usuario actual
        db.collection("usuarios").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        handleKeyNotFound(message);
                        return;
                    }

                    User user = documentSnapshot.toObject(User.class);
                    if (user == null || user.getPrivateKeyEncrypted() == null) {
                        handleKeyNotFound(message);
                        return;
                    }

                    try {
                        // 2. Generar la clave de encriptación (debe ser la MISMA usada durante el registro)
                        String encryptionKey = generateEncryptionKey();

                        // 3. Convertir a SecretKey
                        SecretKey aesKey = new SecretKeySpec(
                                encryptionKey.getBytes(StandardCharsets.UTF_8),
                                "AES");

                        // 4. Descifrar la clave privada
                        String decryptedPrivateKeyStr = AESUtils.decrypt(
                                user.getPrivateKeyEncrypted(),
                                aesKey);

                        // 5. Parsear los componentes RSA
                        String[] parts = decryptedPrivateKeyStr.split("\\|");
                        if (parts.length != 2) {
                            throw new IllegalArgumentException("Formato de clave privada inválido");
                        }

                        BigInteger d = new BigInteger(parts[0]);
                        BigInteger n = new BigInteger(parts[1]);
                        PrivateKey privateKey = new PrivateKey(d, n);

                        // 6. Guardar localmente para futuros usos
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            SecureStorage.savePrivateKey(privateKey, this);
                        }

                        // 7. Si hay un mensaje específico, descifrarlo
                        if (message != null) {
                            decryptMessage(message, privateKey);
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "Clave privada recuperada exitosamente",
                                            Toast.LENGTH_SHORT).show());
                        }

                    } catch (Exception e) {
                        handleKeyRecoveryError(message, e);
                    }
                })
                .addOnFailureListener(e -> handleFirestoreError(message, e));
    }

    // Método auxiliar para generar la clave de encriptación (debe ser IDÉNTICO al usado en registro)
    private String generateEncryptionKey() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        // Ejemplo: email + UID (debes usar el mismo método que durante el registro)
        return firebaseUser.getEmail() + firebaseUser.getUid();
    }

    // Método para descifrar un mensaje con la clave recuperada
    private void decryptMessage(ChatMessage message, PrivateKey privateKey) throws Exception {
        // 1. Descifrar clave AES
        byte[] aesKeyBytes = RSAUtils.decryptAESKey(message.getEncryptedKey(), privateKey);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // 2. Descifrar contenido
        String decryptedContent = AESUtils.decrypt(message.getContent(), aesKey);
        message.setContent(decryptedContent);

        // 3. Actualizar UI
        runOnUiThread(() -> messagesAdapter.notifyDataSetChanged());
    }
    private void handleKeyRecoveryError(ChatMessage message, Exception e) {
        Log.e("ChatActivity", "Error al recuperar clave", e);
        if (message != null) {
            message.setContent("[Error: No se pudo descifrar]");
            runOnUiThread(() -> messagesAdapter.notifyDataSetChanged());
        }
        showErrorToast("Error al recuperar clave de seguridad");
    }
    private void handleKeyNotFound(ChatMessage message) {
        runOnUiThread(() -> {
            // Mostrar alerta al usuario
            new AlertDialog.Builder(this)
                    .setTitle("Clave no encontrada")
                    .setMessage("No se encontró tu clave privada segura en la nube. " +
                            "No podrás leer mensajes cifrados antiguos.")
                    .setPositiveButton("Entendido", null)
                    .show();

            // Si es un mensaje específico, marcarlo como no legible
            if (message != null) {
                message.setContent("[Mensaje cifrado - clave no disponible]");
                messagesAdapter.notifyDataSetChanged();
            } else {
                // Registro para diagnóstico
                Log.w(TAG, "Clave privada no encontrada en Firestore para el usuario: " + currentUserId);
            }
        });
    }
    private void handleFirestoreError(ChatMessage message, Exception e) {
        runOnUiThread(() -> {
            // Mostrar error al usuario
            Toast.makeText(this,
                    "Error de conexión al recuperar clave segura",
                    Toast.LENGTH_LONG).show();

            // Mensaje de depuración más detallado
            Log.e(TAG, "Firestore error al recuperar clave privada", e);
            FirebaseCrashlytics.getInstance().recordException(e);

            // Manejo específico para mensajes
            if (message != null) {
                message.setContent("[Error al cargar clave - Intenta más tarde]");
                messagesAdapter.notifyDataSetChanged();
            }


        });
    }

}