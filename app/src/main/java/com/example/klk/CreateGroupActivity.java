package com.example.klk;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.klk.adapters.SelectableUserListAdapter;
import com.example.klk.models.Chat;
import com.example.klk.models.User;
import com.example.klk.repositories.ChatRepository;
import com.example.klk.repositories.UserRepository;
import com.example.klk.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Actividad para crear grupos de chat
 * Implementa funcionalidad para seleccionar múltiples usuarios y crear chats grupales
 * Aplica principios SOLID, KISS, DRY y YAGNI
 */
public class CreateGroupActivity extends AppCompatActivity {

    // UI Components
    private TextView textTitle;
    private ImageButton buttonBack;
    private ImageButton buttonCreate;
    private EditText editTextGroupName;
    private EditText editTextSearchUsers;
    private TextView textSelectedCount;
    private TextView textEmptyState;
    private RecyclerView recyclerViewUsers;

    // Business Logic
    private SelectableUserListAdapter userAdapter;
    private UserRepository userRepository;
    private ChatRepository chatRepository;
    private SessionManager sessionManager;

    // Data
    private String currentUserId;
    private String currentUserName;
    private LiveData<List<User>> currentUsersObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_group);

        initializeComponents();
        setupWindowInsets();
        setupRecyclerView();
        setupEventListeners();
        loadUsers();
    }

    /**
     * Inicializa todos los componentes necesarios (SOLID: Single Responsibility)
     */
    private void initializeComponents() {
        // UI Components
        textTitle = findViewById(R.id.textTitle);
        buttonBack = findViewById(R.id.buttonBack);
        buttonCreate = findViewById(R.id.buttonCreate);
        editTextGroupName = findViewById(R.id.editTextGroupName);
        editTextSearchUsers = findViewById(R.id.editTextSearchUsers);
        textSelectedCount = findViewById(R.id.textSelectedCount);
        textEmptyState = findViewById(R.id.textEmptyState);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);

        // Business Logic
        sessionManager = new SessionManager(this);
        userRepository = UserRepository.getInstance();
        chatRepository = ChatRepository.getInstance();

        // User data
        currentUserId = sessionManager.getUserId();
        currentUserName = sessionManager.getUserName();

        textTitle.setText("Crear Grupo");
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
     * Configura el RecyclerView (KISS: configuración simple y clara)
     */
    private void setupRecyclerView() {
        userAdapter = new SelectableUserListAdapter(this, currentUserId);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);

        // Listener para cambios en la selección
        userAdapter.setOnSelectionChangedListener(this::updateUIState);
    }

    /**
     * Configura todos los listeners de eventos (SOLID: método coordinador)
     */
    private void setupEventListeners() {
        setupBackButtonListener();
        setupCreateButtonListener();
        setupGroupNameListener();
        setupSearchListener();
    }

    /**
     * Configura el listener del botón de retroceso
     */
    private void setupBackButtonListener() {
        buttonBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Configura el listener del botón de crear grupo
     */
    private void setupCreateButtonListener() {
        buttonCreate.setOnClickListener(v -> createGroup());
    }

    /**
     * Configura el listener del campo de nombre de grupo
     */
    private void setupGroupNameListener() {
        editTextGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateUIState(userAdapter.getSelectedCount());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Configura el listener de búsqueda de usuarios
     */
    private void setupSearchListener() {
        editTextSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Carga todos los usuarios disponibles (DRY: método reutilizable)
     */
    private void loadUsers() {
        // Remover observador anterior si existe
        if (currentUsersObservable != null) {
            currentUsersObservable.removeObservers(this);
        }

        currentUsersObservable = userRepository.getAllUsers();
        currentUsersObservable.observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                userAdapter.updateUsers(users);
                textEmptyState.setVisibility(android.view.View.GONE);
            } else {
                textEmptyState.setVisibility(android.view.View.VISIBLE);
            }
        });
    }

    /**
     * Busca usuarios por nombre o email (YAGNI: solo la funcionalidad necesaria)
     */
    private void searchUsers(String query) {
        // Remover observador anterior
        if (currentUsersObservable != null) {
            currentUsersObservable.removeObservers(this);
        }

        if (query == null || query.trim().isEmpty()) {
            loadUsers();
            return;
        }

        currentUsersObservable = userRepository.searchUsers(query);
        currentUsersObservable.observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                userAdapter.updateUsers(users);
                textEmptyState.setVisibility(android.view.View.GONE);
            } else {
                userAdapter.updateUsers(new ArrayList<>());
                textEmptyState.setVisibility(android.view.View.VISIBLE);
            }
        });
    }

    /**
     * Actualiza el estado de la UI según la selección (KISS: lógica simple)
     */
    private void updateUIState(int selectedCount) {
        // Actualizar contador
        textSelectedCount.setText(String.format("Miembros seleccionados: %d", selectedCount));

        // Validar condiciones para habilitar botón de crear
        String groupName = editTextGroupName.getText().toString().trim();
        boolean canCreate = !groupName.isEmpty() && selectedCount >= 2;

        buttonCreate.setEnabled(canCreate);
        buttonCreate.setAlpha(canCreate ? 1.0f : 0.5f);
    }

    /**
     * Crea el grupo con los usuarios seleccionados (SOLID: método con responsabilidad única)
     */
    private void createGroup() {
        String groupName = editTextGroupName.getText().toString().trim();
        List<User> selectedUsers = userAdapter.getSelectedUsers();

        // Validaciones (KISS: validaciones simples y claras)
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Ingresa un nombre para el grupo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedUsers.size() < 2) {
            Toast.makeText(this, "Selecciona al menos 2 miembros", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón mientras se crea
        buttonCreate.setEnabled(false);

        // Preparar datos del grupo
        List<String> participantIds = new ArrayList<>();
        List<String> participantNames = new ArrayList<>();

        // Agregar usuario actual primero
        participantIds.add(currentUserId);
        participantNames.add(currentUserName);

        // Agregar usuarios seleccionados
        for (User user : selectedUsers) {
            participantIds.add(user.getId());
            participantNames.add(user.getName());
        }

        // Crear el grupo en Firestore
        chatRepository.createGroupChat(groupName, participantIds, participantNames, new ChatRepository.ChatCallback() {
            @Override
            public void onSuccess(Chat chat) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateGroupActivity.this,
                        "Grupo creado exitosamente", Toast.LENGTH_SHORT).show();

                    // Abrir el chat del grupo
                    openGroupChat(chat);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateGroupActivity.this,
                        "Error al crear grupo: " + error, Toast.LENGTH_SHORT).show();
                    buttonCreate.setEnabled(true);
                });
            }
        });
    }

    /**
     * Abre el chat del grupo recién creado
     */
    private void openGroupChat(Chat chat) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CHAT_ID, chat.getId());
        intent.putExtra(ChatActivity.EXTRA_CHAT_NAME, getGroupDisplayName(chat));
        startActivity(intent);
        finish();
    }

    /**
     * Obtiene el nombre de visualización del grupo (DRY: método auxiliar reutilizable)
     */
    private String getGroupDisplayName(Chat chat) {
        // Si el chat tiene nombre de grupo, usarlo
        if (chat.getGroupName() != null && !chat.getGroupName().isEmpty()) {
            return chat.getGroupName();
        }

        // Si no, mostrar nombres de participantes
        if (chat.getParticipantNames() != null && !chat.getParticipantNames().isEmpty()) {
            List<String> names = new ArrayList<>();
            for (String name : chat.getParticipantNames()) {
                if (!name.equals(currentUserName)) {
                    names.add(name);
                }
            }
            return String.join(", ", names);
        }

        return "Grupo sin nombre";
    }
}
