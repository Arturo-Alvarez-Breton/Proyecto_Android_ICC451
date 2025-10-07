package com.example.klk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.klk.models.User;
import com.example.klk.repositories.AuthRepository;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000; // 2 segundos

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        setupWindowInsets();
        initializeComponents();
        startAuthenticationCheck();
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
     * Inicializa los componentes necesarios.
     */
    private void initializeComponents() {
        authRepository = new AuthRepository(this);
    }

    /**
     * Inicia el proceso de verificación de autenticación con un delay para mostrar el splash.
     */
    private void startAuthenticationCheck() {
        new Handler().postDelayed(this::checkAuthenticationStatus, SPLASH_DELAY);
    }

    /**
     * Verifica el estado de autenticación y navega a la pantalla apropiada.
     */
    private void checkAuthenticationStatus() {
        try {
            if (authRepository.isUserAuthenticated()) {
                // Usuario autenticado, sincronizar sesión y navegar a MainActivity
                syncSessionAndNavigate();
            } else {
                // Usuario no autenticado, navegar a Login
                navigateToLogin();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar autenticación: " + e.getMessage());
            // En caso de error, navegar a Login por seguridad
            navigateToLogin();
        }
    }

    /**
     * Sincroniza la sesión con Firebase y navega a MainActivity si es exitoso.
     */
    private void syncSessionAndNavigate() {
        authRepository.syncSession(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d(TAG, "Sesión sincronizada correctamente para: " + user.getName());
                navigateToMainActivity();
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "Error al sincronizar sesión: " + error);
                // Si hay error en la sincronización, ir a Login
                navigateToLogin();
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

    /**
     * Navega a la actividad principal.
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}