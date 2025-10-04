package com.example.klk;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.klk.adapters.ChatListAdapter;
import com.example.klk.models.Chat;
import com.example.klk.models.User;
import com.example.klk.repositories.ChatRepository;
import com.example.klk.repositories.UserRepository;
import com.example.klk.utils.SessionManager;
import com.example.klk.utils.FabMenuHelper;
import com.example.klk.utils.NotificationPermissionHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private EditText chatSearchField;
    private Button btnAllChatsFilter, btnGroupChatsFilter, btnContactsChatsFilter;
    private Button btnSettings, btnLogout;
    private RecyclerView recyclerViewChats;

    // FAB Menu Components
    private FloatingActionButton fabMain, fabIndividualChat, fabCreateGroup;
    private View labelIndividualChat, labelCreateGroup, fabMenuOverlay;
    private FabMenuHelper fabMenuHelper;

    // Business Logic Components
    private ChatListAdapter chatAdapter;
    private ChatRepository chatRepository;
    private UserRepository userRepository;

    // Un solo observer para evitar conflictos (CORREGIDO)
    private String currentFilter = "todos";
    private LiveData<List<Chat>> currentChatsObservable;

    // User data
    private String currentUserId;
    private String currentUserName;

    // Notification Permission Helper
    private NotificationPermissionHelper notificationPermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Verificar autenticación antes de inicializar la actividad
        if (!validateUserAuthentication()) {
            return; // La actividad se cerrará si no está autenticado
        }

        initializeComponents();
        checkUserSession();
        setupRecyclerView();
        setupFabMenu();
        setupWindowInsets();
        setupEventListeners();
        registerCurrentUser();
        loadChats();
        getFCMToken();
        requestNotificationPermission();
    }

    /**
     * Solicita permiso para mostrar notificaciones (Android 13+)
     * Aplica buenas prácticas de UX: se solicita al inicio de la app
     */
    private void requestNotificationPermission() {
        notificationPermissionHelper = new NotificationPermissionHelper(this);

        notificationPermissionHelper.requestNotificationPermission(
            new NotificationPermissionHelper.PermissionCallback() {
                @Override
                public void onPermissionGranted() {
                    Log.d("MainActivity", "Permiso de notificaciones concedido");
                    // El usuario puede recibir notificaciones
                }

                @Override
                public void onPermissionDenied() {
                    Log.d("MainActivity", "Permiso de notificaciones denegado");
                    // La app funciona pero sin notificaciones
                }
            }
        );
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                Log.i("FCM Token", token);
                // Actualizar el FCMToken del usuario actual
                FirebaseFirestore.getInstance().collection("users")
                    .document(currentUserId)
                    .update("fcmToken", token)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i("FCM Token", "Token updated successfully");
                            } else {
                                Log.e("FCM Token", "Error updating token", task.getException());
                            }
                        }
                    });
            }
        });
    }

    /**
     * Valida que el usuario esté autenticado antes de acceder a la app
     */
    private boolean validateUserAuthentication() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return false;
        }

        // Verificar que el usuario de la sesión coincida con Firebase Auth
        SessionManager tempSessionManager = new SessionManager(this);
        String sessionUserId = tempSessionManager.getUserId();
        if (sessionUserId == null || !sessionUserId.equals(currentUser.getUid())) {
            // Limpiar sesión y redirigir al login
            tempSessionManager.clearSession();
            navigateToLogin();
            return false;
        }

        return true;
    }

    /**
     * Inicializa todos los componentes de la UI y servicios necesarios
     */
    private void initializeComponents() {
        sessionManager = new SessionManager(this);

        // Referencias a elementos de UI
        chatSearchField = findViewById(R.id.chatSearchField);
        btnAllChatsFilter = findViewById(R.id.btnAllChatsFilter);
        btnGroupChatsFilter = findViewById(R.id.btnGroupChatsFilter);
        btnContactsChatsFilter = findViewById(R.id.btnContactsChatsFilter);
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerViewChats = findViewById(R.id.recyclerViewChats);

        // FAB Menu Components
        fabMain = findViewById(R.id.fabMain);
        fabIndividualChat = findViewById(R.id.fabIndividualChat);
        fabCreateGroup = findViewById(R.id.fabCreateGroup);
        labelIndividualChat = findViewById(R.id.cardIndividualChat);
        labelCreateGroup = findViewById(R.id.cardCreateGroup);
        fabMenuOverlay = findViewById(R.id.fabMenuOverlay);

        // Business Logic Components
        chatRepository = ChatRepository.getInstance();
        userRepository = UserRepository.getInstance();

        // User data
        currentUserId = sessionManager.getUserId();
        currentUserName = sessionManager.getUserName();
    }

    /**
     * Configura el menú FAB desplegable
     */
    private void setupFabMenu() {
        fabMenuHelper = new FabMenuHelper(
            fabMain, fabIndividualChat, fabCreateGroup,
            labelIndividualChat, labelCreateGroup, fabMenuOverlay
        );
    }

    /**
     * Configura el RecyclerView para la lista de chats
     */
    private void setupRecyclerView() {
        chatAdapter = new ChatListAdapter(this, currentUserId);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChats.setAdapter(chatAdapter);

        // Configurar listener para clicks en chats
        chatAdapter.setOnChatClickListener(chat -> openChat(chat));
    }

    /**
     * Verifica si el usuario tiene sesión válida
     */
    private void checkUserSession() {
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
        }
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
        setupSearchListener();
        setupFilterButtons();
        setupActionButtons();
    }

    /**
     * Configura el listener para el campo de búsqueda
     */
    private void setupSearchListener() {
        chatSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Configura los botones de filtro
     */
    private void setupFilterButtons() {
        btnAllChatsFilter.setOnClickListener(v -> {
            setActiveFilter(btnAllChatsFilter);
            currentFilter = "todos";
            loadChats();
        });
        btnGroupChatsFilter.setOnClickListener(v -> {
            setActiveFilter(btnGroupChatsFilter);
            currentFilter = "grupos";
            loadChats();
        });
        btnContactsChatsFilter.setOnClickListener(v -> {
            setActiveFilter(btnContactsChatsFilter);
            currentFilter = "contactos";
            loadChats();
        });
    }

    /**
     * Configura los botones de acción
     */
    private void setupActionButtons() {
        btnLogout.setOnClickListener(v -> handleLogout());
        btnSettings.setOnClickListener(v -> handleSettings());

        // Configurar FAB Menu
        setupFabEventListeners();
    }

    /**
     * Configura los listeners del menú FAB
     */
    private void setupFabEventListeners() {
        // Botón principal - toggle del menú
        fabMain.setOnClickListener(v -> fabMenuHelper.toggleMenu());

        // Overlay - cerrar menú al tocar fuera
        fabMenuOverlay.setOnClickListener(v -> fabMenuHelper.closeMenu());

        // Chat individual
        fabIndividualChat.setOnClickListener(v -> {
            fabMenuHelper.closeMenu();
            openAddUserActivity();
        });

        labelIndividualChat.setOnClickListener(v -> {
            fabMenuHelper.closeMenu();
            openAddUserActivity();
        });

        // Crear grupo
        fabCreateGroup.setOnClickListener(v -> {
            fabMenuHelper.closeMenu();
            openCreateGroupActivity();
        });

        labelCreateGroup.setOnClickListener(v -> {
            fabMenuHelper.closeMenu();
            openCreateGroupActivity();
        });
    }

    /**
     * Carga los chats del usuario según el filtro activo (Simplificado - DRY)
     */
    private void loadChats() {
        String searchQuery = chatSearchField.getText().toString().trim();

        if (!searchQuery.isEmpty()) {
            // Búsqueda usa los datos ya cargados (más eficiente)
            chatRepository.searchChats(currentUserId, searchQuery)
                .observe(this, chats -> {
                    if (chats != null) {
                        chatAdapter.updateChats(chats);
                    }
                });
        } else {
            // Un solo método para todos los casos (KISS principle)
            chatRepository.getUserChats(currentUserId, currentFilter)
                .observe(this, chats -> {
                    if (chats != null) {
                        chatAdapter.updateChats(chats);
                    }
                });
        }
    }

    /**
     * Maneja el filtrado de chats por búsqueda (Simplificado)
     */
    private void filterChats(String query) {
        if (query.trim().isEmpty()) {
            loadChats(); // Reutilizar lógica existente
        } else {
            // Usar búsqueda optimizada
            chatRepository.searchChats(currentUserId, query)
                .observe(this, chats -> {
                    if (chats != null) {
                        chatAdapter.updateChats(chats);
                    }
                });
        }
    }

    /**
     * Cambia el filtro activo
     */
    private void setActiveFilter(Button activeButton) {
        // Resetear todos los botones
        resetFilterButtons();

        // Activar el botón seleccionado
        activeButton.setBackgroundTintList(getColorStateList(R.color.btn_chats_filters_active));
    }

    /**
     * Resetea el estado visual de todos los botones de filtro
     */
    private void resetFilterButtons() {
        int inactiveColor = R.color.btn_chats_filters_inactive;
        btnAllChatsFilter.setBackgroundTintList(getColorStateList(inactiveColor));
        btnGroupChatsFilter.setBackgroundTintList(getColorStateList(inactiveColor));
        btnContactsChatsFilter.setBackgroundTintList(getColorStateList(inactiveColor));
    }

    /**
     * Maneja el cierre de sesión
     */
    private void handleLogout() {
        sessionManager.clearSession();
        navigateToLogin();
    }

    /**
     * Maneja el acceso a configuraciones
     */
    private void handleSettings() {
        // TODO: Implementar navegación a configuraciones
    }

    /**
     * Abre la actividad para agregar usuarios (chat individual)
     */
    private void openAddUserActivity() {
        Intent intent = new Intent(this, AddUserActivity.class);
        startActivity(intent);
    }

    /**
     * Abre la actividad para crear grupos
     */
    private void openCreateGroupActivity() {
        Intent intent = new Intent(this, CreateGroupActivity.class);
        startActivity(intent);
    }

    /**
     * Registra al usuario actual en la base de datos
     */
    private void registerCurrentUser() {
        String userEmail = sessionManager.getUserEmail();
        User currentUser = new User(currentUserId, userEmail, currentUserName);
        currentUser.setOnline(true);

        userRepository.addUser(currentUser, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                // Usuario registrado exitosamente
            }

            @Override
            public void onError(String error) {
                // Error al registrar usuario - continuar normalmente
            }
        });
    }

    /**
     * Abre un chat específico
     */
    private void openChat(Chat chat) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CHAT_ID, chat.getId());

        // Obtener nombre del chat para mostrar
        String chatName = getChatDisplayName(chat);
        intent.putExtra(ChatActivity.EXTRA_CHAT_NAME, chatName);

        startActivity(intent);
    }

    /**
     * Obtiene el nombre del chat para mostrar
     */
    private String getChatDisplayName(Chat chat) {
        if (chat.getParticipantNames().size() == 2) {
            // Chat individual - mostrar nombre del otro usuario
            for (int i = 0; i < chat.getParticipantIds().size(); i++) {
                if (!chat.getParticipantIds().get(i).equals(currentUserId)) {
                    return chat.getParticipantNames().get(i);
                }
            }
        } else if (chat.getParticipantNames().size() > 2) {
            // Chat grupal - mostrar nombres de participantes
            StringBuilder groupName = new StringBuilder();
            for (int i = 0; i < chat.getParticipantNames().size(); i++) {
                if (!chat.getParticipantIds().get(i).equals(currentUserId)) {
                    if (groupName.length() > 0) {
                        groupName.append(", ");
                    }
                    groupName.append(chat.getParticipantNames().get(i));
                }
            }
            return groupName.toString();
        }
        return "Chat";
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar estado online del usuario
        if (currentUserId != null) {
            userRepository.updateUserOnlineStatus(currentUserId, true);
        }
        // Recargar chats al volver a la actividad
        loadChats();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Actualizar estado offline del usuario
        if (currentUserId != null) {
            userRepository.updateUserOnlineStatus(currentUserId, false);
        }
    }

    @Override
    public void onBackPressed() {
        // Si el menú FAB está abierto, cerrarlo en lugar de salir
        if (fabMenuHelper != null && fabMenuHelper.isMenuOpen()) {
            fabMenuHelper.closeMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener listeners para evitar memory leaks
        if (chatRepository != null) {
            chatRepository.stopListening();
        }
        if (userRepository != null) {
            userRepository.stopListening();
        }
        // Forzar cierre del menú FAB
        if (fabMenuHelper != null) {
            fabMenuHelper.forceCloseMenu();
        }
    }

    /**
     * Navega a la pantalla de login
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
