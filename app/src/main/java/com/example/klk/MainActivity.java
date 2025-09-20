package com.example.klk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.klk.models.User;
import com.example.klk.repositories.AuthRepository;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private AuthRepository authRepository;
    private TextView welcomeText;
    private TextView userEmailText;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeComponents();
        setupWindowInsets();
        checkAuthentication();
        setupClickListeners();
        loadUserData();
    }

    /**
     * Inicializa todos los componentes de la UI y el repositorio.
     */
    private void initializeComponents() {
        authRepository = new AuthRepository(this);
        welcomeText = findViewById(R.id.welcomeText);
        userEmailText = findViewById(R.id.userEmailText);
        btnLogout = findViewById(R.id.btnLogout);
    }

    /**
     * Configura los window insets para edge-to-edge display.
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Verifica si el usuario está autenticado.
     */
    private void checkAuthentication() {
        if (!authRepository.isUserAuthenticated()) {
            navigateToLogin();
            return;
        }

        // Sincronizar sesión con Firebase
        authRepository.syncSession(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Sesión sincronizada correctamente");
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "Error al sincronizar sesión: " + error);
                // Si hay error, redirigir al login
                navigateToLogin();
            }
        });
    }

    /**
     * Configura los listeners de los botones.
     */
    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> handleLogout());
    }

    /**
     * Carga y muestra los datos del usuario.
     */
    private void loadUserData() {
        User currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
                        welcomeText.setText(getString(R.string.welcome_user, currentUser.getName()));
            userEmailText.setText(currentUser.getEmail());
        } else {
            welcomeText.setText("¡Bienvenido!");
            userEmailText.setText("");
        }
    }

    /**
     * Maneja el proceso de cierre de sesión.
     */
    private void handleLogout() {
        authRepository.logoutUser(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error al cerrar sesión: " + error);
                    Toast.makeText(MainActivity.this, "Error al cerrar sesión: " + error, Toast.LENGTH_SHORT).show();
                    // Aún así navegar al login ya que la sesión local se limpió
                    navigateToLogin();
                });
            }
        });
    }

    /**
     * Navega a la pantalla de login.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos del usuario cada vez que se resume la actividad
        loadUserData();
    }
}