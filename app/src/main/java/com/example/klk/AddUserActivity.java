package com.example.klk;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.klk.adapters.UserListAdapter;
import com.example.klk.models.Chat;
import com.example.klk.models.User;
import com.example.klk.repositories.ChatRepository;
import com.example.klk.repositories.UserRepository;
import com.example.klk.utils.SessionManager;
import java.util.Arrays;
import java.util.List;

/**
 * Actividad para buscar y agregar usuarios, crear chats
 * Implementa principios SOLID y patrones de diseño
 */
public class AddUserActivity extends AppCompatActivity {

    // UI Components
    private TextView textTitle;
    private ImageButton buttonBack;
    private EditText editTextSearch;
    private RecyclerView recyclerViewUsers;
    private ProgressBar progressBar;
    private TextView textEmptyState;

    // Business Logic
    private UserListAdapter userAdapter;
    private UserRepository userRepository;
    private ChatRepository chatRepository;
    private SessionManager sessionManager;

    // User data
    private String currentUserId;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_user);

        initializeComponents();
        setupRecyclerView();
        setupWindowInsets();
        setupEventListeners();
        loadAllUsers();
    }

    /**
     * Inicializa todos los componentes necesarios
     */
    private void initializeComponents() {
        // UI Components
        textTitle = findViewById(R.id.textTitle);
        buttonBack = findViewById(R.id.buttonBack);
        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        progressBar = findViewById(R.id.progressBar);
        textEmptyState = findViewById(R.id.textEmptyState);

        // Business Logic Components
        userRepository = UserRepository.getInstance();
        chatRepository = ChatRepository.getInstance();
        sessionManager = new SessionManager(this);

        // User data
        currentUserId = sessionManager.getUserId();
        currentUserName = sessionManager.getUserName();

        textTitle.setText("Agregar Usuario");
    }

    /**
     * Configura el RecyclerView con el adaptador
     */
    private void setupRecyclerView() {
        userAdapter = new UserListAdapter(this, currentUserId);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);

        // Configurar listener para clicks en usuarios
        userAdapter.setOnUserClickListener(user -> createChatWithUser(user));
    }

    /**
     * Configura los window insets para edge-to-edge display
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura todos los listeners de eventos
     */
    private void setupEventListeners() {
        setupBackButtonListener();
        setupSearchListener();
    }

    /**
     * Configura el listener del botón de retroceso
     */
    private void setupBackButtonListener() {
        buttonBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Configura el listener del campo de búsqueda
     */
    private void setupSearchListener() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Restaurar funcionalidad de búsqueda corregida
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Carga todos los usuarios registrados
     */
    private void loadAllUsers() {
        showLoading(true);
        android.util.Log.d("AddUserActivity", "Loading all users...");

        userRepository.getAllUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                showLoading(false);
                android.util.Log.d("AddUserActivity", "Received " + (users != null ? users.size() : 0) + " users");

                if (users != null && !users.isEmpty()) {
                    // Filtrar usuario actual
                    users.removeIf(user -> user.getId().equals(currentUserId));
                    android.util.Log.d("AddUserActivity", "After filtering current user: " + users.size() + " users");

                    userAdapter.updateUsers(users);
                    showEmptyState(users.isEmpty());

                    // Mostrar mensaje informativo si hay usuarios
                    if (!users.isEmpty()) {
                        android.util.Log.d("AddUserActivity", "Showing users list");
                    }
                } else {
                    android.util.Log.d("AddUserActivity", "No users found or null list");
                    showEmptyState(true);
                }
            }
        });
    }

    /**
     * Busca usuarios por nombre o email
     */
    private void searchUsers(String query) {
        if (query.trim().isEmpty()) {
            loadAllUsers();
            return;
        }

        showLoading(true);
        android.util.Log.d("AddUserActivity", "Searching users with query: " + query);

        userRepository.searchUsers(query).observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                showLoading(false);
                android.util.Log.d("AddUserActivity", "Search returned " + (users != null ? users.size() : 0) + " users");

                if (users != null) {
                    // Filtrar usuario actual
                    users.removeIf(user -> user.getId().equals(currentUserId));
                    android.util.Log.d("AddUserActivity", "After filtering current user from search: " + users.size() + " users");

                    userAdapter.updateUsers(users);
                    showEmptyState(users.isEmpty());
                } else {
                    android.util.Log.d("AddUserActivity", "Search returned null");
                    showEmptyState(true);
                }
            }
        });
    }

    /**
     * Crea un chat con el usuario seleccionado
     */
    private void createChatWithUser(User user) {
        showLoading(true);

        // Validate that the selected user has a valid name
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            showLoading(false);
            Toast.makeText(this, "El usuario seleccionado no tiene nombre válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> participantIds = Arrays.asList(currentUserId, user.getId());
        List<String> participantNames = Arrays.asList(currentUserName, user.getName());

        chatRepository.createChat(participantIds, participantNames, new ChatRepository.ChatCallback() {
            @Override
            public void onSuccess(Chat chat) {
                runOnUiThread(() -> {
                    showLoading(false);
                    openChat(chat);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(AddUserActivity.this,
                        "Error al crear chat: " + error,
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Abre el chat creado
     */
    private void openChat(Chat chat) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CHAT_ID, chat.getId());

        // Obtener nombre del chat (nombre del otro usuario)
        String chatName = "Chat";
        for (int i = 0; i < chat.getParticipantIds().size(); i++) {
            if (!chat.getParticipantIds().get(i).equals(currentUserId)) {
                chatName = chat.getParticipantNames().get(i);
                break;
            }
        }

        intent.putExtra(ChatActivity.EXTRA_CHAT_NAME, chatName);
        startActivity(intent);
        finish();
    }

    /**
     * Muestra u oculta el indicador de carga
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Muestra u oculta el estado vacío
     */
    private void showEmptyState(boolean show) {
        textEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewUsers.setVisibility(show ? View.GONE : View.VISIBLE);

        String searchQuery = editTextSearch.getText().toString().trim();
        if (show) {
            if (searchQuery.isEmpty()) {
                textEmptyState.setText("No hay usuarios registrados");
            } else {
                textEmptyState.setText("No se encontraron usuarios");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRepository != null) {
            userRepository.stopListening();
        }
    }
}
