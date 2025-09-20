package com.example.klk;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.klk.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private EditText chatSearchField;
    private Button btnAllChatsFilter, btnGroupChatsFilter, btnContactsChatsFilter;
    private Button btnSettings, btnLogout, btnNewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeComponents();
        checkUserSession();
        setupWindowInsets();
        setupEventListeners();
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
        btnNewGroup = findViewById(R.id.btnNewGroup);
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
        btnAllChatsFilter.setOnClickListener(v -> setActiveFilter(btnAllChatsFilter));
        btnGroupChatsFilter.setOnClickListener(v -> setActiveFilter(btnGroupChatsFilter));
        btnContactsChatsFilter.setOnClickListener(v -> setActiveFilter(btnContactsChatsFilter));
    }

    /**
     * Configura los botones de acción
     */
    private void setupActionButtons() {
        btnLogout.setOnClickListener(v -> handleLogout());
        btnSettings.setOnClickListener(v -> handleSettings());
        btnNewGroup.setOnClickListener(v -> handleNewGroup());
    }

    /**
     * Maneja el filtrado de chats
     */
    private void filterChats(String query) {
        // TODO: Implementar lógica de filtrado cuando se agregue la funcionalidad de chats
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
     * Maneja la creación de nuevo grupo
     */
    private void handleNewGroup() {
        // TODO: Implementar navegación a creación de grupo
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