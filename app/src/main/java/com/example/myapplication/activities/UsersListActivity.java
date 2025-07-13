package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.UsersAdapter;
import com.example.myapplication.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersListActivity extends AppCompatActivity implements UsersAdapter.OnUserClickListener {

    private RecyclerView usersRecyclerView;
    private UsersAdapter usersAdapter;
    private FirebaseFirestore db;
    private String currentUserId;
    private List<User> usersList = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Todos los usuarios");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Obtener usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        db = FirebaseFirestore.getInstance();

        // Configurar vistas
        progressBar = findViewById(R.id.progressBar);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersAdapter = new UsersAdapter(usersList, this);
        usersRecyclerView.setAdapter(usersAdapter);

        // Cargar usuarios
        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("usuarios")
                .whereNotEqualTo("id", currentUserId) // Excluir al usuario actual
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        usersList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                usersList.add(user);
                            }
                        }
                        usersAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onUserClick(int position) {
        User selectedUser = usersList.get(position);
        startChatWithUser(selectedUser);
    }

    private void startChatWithUser(User otherUser) {
        // Verificar si ya existe un chat con este usuario
        db.collection("chats")
                .where(Filter.or(
                        Filter.and(
                                Filter.equalTo("user1", db.collection("usuarios").document(currentUserId)),
                                Filter.equalTo("user2", db.collection("usuarios").document(otherUser.getId()))
                        ),
                        Filter.and(
                                Filter.equalTo("user1", db.collection("usuarios").document(otherUser.getId())),
                                Filter.equalTo("user2", db.collection("usuarios").document(currentUserId))
                        )
                ))
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Chat existente - abrirlo
                        DocumentSnapshot chatDoc = task.getResult().getDocuments().get(0);
                        openChatActivity(chatDoc.getId(), otherUser);
                    } else {
                        // Crear nuevo chat
                        createNewChat(otherUser);
                    }
                });
    }

    private void createNewChat(User otherUser) {
        Map<String, Object> chat = new HashMap<>();
        chat.put("user1", db.collection("usuarios").document(currentUserId));
        chat.put("user2", db.collection("usuarios").document(otherUser.getId()));
        chat.put("lastMessage", "");
        chat.put("lastUpdate", FieldValue.serverTimestamp());
        chat.put("encrypted", true);

        db.collection("chats")
                .add(chat)
                .addOnSuccessListener(documentReference -> {
                    openChatActivity(documentReference.getId(), otherUser);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al crear chat", Toast.LENGTH_SHORT).show();
                });
    }

    private void openChatActivity(String chatId, User otherUser) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("otherUserId", otherUser.getId());
        intent.putExtra("otherUserName", otherUser.getName());
        intent.putExtra("otherUserPublicKey", otherUser.getPublicKey());
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}