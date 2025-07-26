package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
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
import com.example.myapplication.adapters.ChatAdapter;
import com.example.myapplication.models.Chat;
import com.example.myapplication.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity implements ChatAdapter.OnChatClickListener {

    private RecyclerView chatsRecyclerView;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore db;
    private String currentUserId;
    private List<Chat> chatList = new ArrayList<>();

    FloatingActionButton fabMain, fabHackear, fabAgg;
    LinearLayout fabHackearLayout, fabAggLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        fabAgg=findViewById(R.id.fabAgg);
        fabMain = findViewById(R.id.fabMain);
        //fabHackear = findViewById(R.id.fabHackear);
        //fabHackearLayout = findViewById(R.id.fabHackearLayout);
        fabAggLayout = findViewById(R.id.fabAggLayout);

        fabMain.setOnClickListener(v -> {
            if (fabAggLayout.getVisibility() == View.GONE) {
                fabMain.animate()
                        .rotation(0f)
                        .setDuration(200)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();



                //fabHackearLayout.setVisibility(View.VISIBLE);
                fabAggLayout.setVisibility(View.VISIBLE);
            } else {
                fabMain.animate().rotation(0f).setDuration(200).start();
                //fabHackearLayout.setVisibility(View.GONE);
                fabAggLayout.setVisibility(View.GONE);
            }
        });

        /*fabHackear.setOnClickListener(v ->
                Toast.makeText(this, "Hackear_user presionado", Toast.LENGTH_SHORT).show()
        );

         */

        fabAgg.setOnClickListener(v ->
                startActivity(new Intent(this, UsersListActivity.class))
        );

        // Obtener usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        db = FirebaseFirestore.getInstance();

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chats");
        }

        // Configurar RecyclerView
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(chatList,currentUserId ,this);
        chatsRecyclerView.setAdapter(chatAdapter);

        // Botón para nuevo chat
        FloatingActionButton fabNewChat = findViewById(R.id.fabAgg);
        fabNewChat.setOnClickListener(v -> startActivity(new Intent(this, UsersListActivity.class)));

        // Cargar chats
        loadChats();
    }

    private void loadChats() {
       db.collection("chats")
                .where(Filter.or(
                        Filter.equalTo("user1", db.collection("usuarios").document(currentUserId)),
                        Filter.equalTo("user2", db.collection("usuarios").document(currentUserId))
                ))
                .orderBy("lastUpdate", Query.Direction.DESCENDING)

                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FIRESTORE", "Error al cargar chats", error);
                        Toast.makeText(this, "Error al cargar chats", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    chatList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Chat chat = doc.toObject(Chat.class);
                        if (chat != null) {
                            chat.setChatId(doc.getId());
                            chatList.add(chat);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                });




    }

    @Override
    public void onChatClick(int position) {
        Chat chat = chatList.get(position);
        String otherUserId = chat.getUser1().getId().equals(currentUserId) ?
                chat.getUser2().getId() : chat.getUser1().getId();

        // Obtener detalles del otro usuario
        db.collection("usuarios").document(otherUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User otherUser = documentSnapshot.toObject(User.class);
                    if (otherUser != null) {
                        openChatActivity(chat.getChatId(), otherUser);
                    }
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadChats(); // 🔄 Se actualiza la lista al volver a la ventana
    }

    private void openChatActivity(String chatId, User otherUser) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("otherUserId", otherUser.getId());
        intent.putExtra("otherUserName", otherUser.getName());
        intent.putExtra("otherUserPublicKey", otherUser.getPublicKey());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Opcional: Modificar ítems del menú dinámicamente
        //MenuItem searchItem = menu.findItem(R.id.action_search);
        //searchItem.setVisible(true); // Podrías ocultarlo en ciertas condiciones

        return true; // Mostrar el menú
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Botón de retroceso/home
            finish();
            return true;
        } else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}