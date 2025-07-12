package com.example.myapplication.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;

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

        db.collection("users")
                .whereNotEqualTo("userId", currentUserId) // Excluir al usuario actual
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
                                Filter.equalTo("user1", db.collection("users").document(currentUserId)),
                                Filter.equalTo("user2", db.collection("users").document(otherUser.getUserId()))
                        ),
                        Filter.and(
                                Filter.equalTo("user1", db.collection("users").document(otherUser.getUserId())),
                                Filter.equalTo("user2", db.collection("users").document(currentUserId))
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
        chat.put("user1", db.collection("users").document(currentUserId));
        chat.put("user2", db.collection("users").document(otherUser.getUserId()));
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
        intent.putExtra("otherUserId", otherUser.getUserId());
        intent.putExtra("otherUserName", otherUser.getNombre());
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