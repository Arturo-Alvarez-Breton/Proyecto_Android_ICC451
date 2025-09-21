package com.example.klk;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Actividad para crear grupos de chat
 * Implementa funcionalidad para seleccionar múltiples usuarios y crear chats grupales
 */
public class CreateGroupActivity extends AppCompatActivity {

    // UI Components
    private TextView textTitle;
    private ImageButton buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_group);

        initializeComponents();
        setupWindowInsets();
        setupEventListeners();
    }

    /**
     * Inicializa todos los componentes necesarios
     */
    private void initializeComponents() {
        textTitle = findViewById(R.id.textTitle);
        buttonBack = findViewById(R.id.buttonBack);

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
     * Configura todos los listeners de eventos
     */
    private void setupEventListeners() {
        setupBackButtonListener();
    }

    /**
     * Configura el listener del botón de retroceso
     */
    private void setupBackButtonListener() {
        buttonBack.setOnClickListener(v -> onBackPressed());
    }
}
