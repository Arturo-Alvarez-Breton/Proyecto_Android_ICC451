package com.example.klk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.klk.models.User;
import com.example.klk.repositories.AuthRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Login extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    // UI Components
    private TextInputEditText emailField;
    private TextInputEditText usernameField;
    private TextInputEditText passwordField;
    private TextInputLayout emailLayout;
    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private Button btnLogin;
    private Button btnRegister;
    private ProgressBar progressBar;

    // Business Logic
    private AuthRepository authRepository;
    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initializeComponents();
        setupWindowInsets();
        checkExistingSession();
        setupClickListeners();
    }

    /**
     * Inicializa todos los componentes de la UI y el repositorio.
     */
    private void initializeComponents() {
        authRepository = new AuthRepository(this);

        // UI Components
        emailField = findViewById(R.id.emailField);
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        emailLayout = findViewById(R.id.emailLayout);
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
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
     * Verifica si ya hay una sesión activa.
     */
    private void checkExistingSession() {
        if (authRepository.isUserAuthenticated()) {
            navigateToMainActivity();
        }
    }

    /**
     * Configura los listeners de los botones.
     */
    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> toggleMode());
        btnLogin.setOnClickListener(v -> handleAuthAction());
    }

    /**
     * Alterna entre modo login y registro.
     */
    private void toggleMode() {
        isRegistering = !isRegistering;
        updateUIForMode();
    }

    /**
     * Actualiza la UI basada en el modo actual (login/registro).
     */
    private void updateUIForMode() {
        if (isRegistering) {
            // Modo registro
            emailLayout.setVisibility(View.VISIBLE);
            usernameLayout.setHint("Nombre completo");
            btnLogin.setText("Registrarse");
            btnRegister.setText("¿Ya tienes cuenta? Inicia Sesión");
        } else {
            // Modo login
            emailLayout.setVisibility(View.GONE);
            usernameLayout.setHint("Email");
            btnLogin.setText("Iniciar Sesión");
            btnRegister.setText("¿No tienes cuenta? Regístrate");
        }
        clearFields();
        clearErrors();
    }

    /**
     * Maneja la acción de autenticación (login o registro).
     */
    private void handleAuthAction() {
        if (isRegistering) {
            handleRegistration();
        } else {
            handleLogin();
        }
    }

    /**
     * Maneja el proceso de registro.
     */
    private void handleRegistration() {
        String email = getTextFromField(emailField);
        String name = getTextFromField(usernameField);
        String password = getTextFromField(passwordField);

        if (!validateRegistrationInput(email, name, password)) {
            return;
        }

        setLoadingState(true);

        authRepository.registerUser(email, password, name, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    showMessage("Registro exitoso. ¡Bienvenido " + user.getName() + "!");
                    navigateToMainActivity();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    showMessage(error);
                    Log.e(TAG, "Error en registro: " + error);
                });
            }
        });
    }

    /**
     * Maneja el proceso de inicio de sesión.
     */
    private void handleLogin() {
        String email = getTextFromField(usernameField); // En modo login, usernameField contiene el email
        String password = getTextFromField(passwordField);

        if (!validateLoginInput(email, password)) {
            return;
        }

        setLoadingState(true);

        authRepository.loginUser(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    showMessage("¡Bienvenido de vuelta, " + user.getName() + "!");
                    navigateToMainActivity();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    showMessage(error);
                    Log.e(TAG, "Error en login: " + error);
                });
            }
        });
    }

    /**
     * Valida la entrada para registro.
     */
    private boolean validateRegistrationInput(String email, String name, String password) {
        clearErrors();
        boolean isValid = true;

        if (email.isEmpty()) {
            emailLayout.setError("Email es requerido");
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailLayout.setError("Formato de email inválido");
            isValid = false;
        }

        if (name.isEmpty()) {
            usernameLayout.setError("Nombre es requerido");
            isValid = false;
        } else if (name.length() < 2) {
            usernameLayout.setError("Nombre debe tener al menos 2 caracteres");
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Contraseña es requerida");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Contraseña debe tener al menos 6 caracteres");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Valida la entrada para login.
     */
    private boolean validateLoginInput(String email, String password) {
        clearErrors();
        boolean isValid = true;

        if (email.isEmpty()) {
            usernameLayout.setError("Email es requerido");
            isValid = false;
        } else if (!isValidEmail(email)) {
            usernameLayout.setError("Formato de email inválido");
            isValid = false;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Contraseña es requerida");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Valida formato de email.
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Obtiene texto de un campo de entrada.
     */
    private String getTextFromField(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    /**
     * Limpia todos los campos de entrada.
     */
    private void clearFields() {
        emailField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }

    /**
     * Limpia todos los errores de los campos.
     */
    private void clearErrors() {
        emailLayout.setError(null);
        usernameLayout.setError(null);
        passwordLayout.setError(null);
    }

    /**
     * Establece el estado de carga de la UI.
     */
    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        btnRegister.setEnabled(!isLoading);
        emailField.setEnabled(!isLoading);
        usernameField.setEnabled(!isLoading);
        passwordField.setEnabled(!isLoading);
    }

    /**
     * Muestra un mensaje al usuario.
     */
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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