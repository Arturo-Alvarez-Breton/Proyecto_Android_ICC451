package com.example.klk;

import android.content.Intent;
import android.os.Bundle;


import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.klk.models.User;
import com.example.klk.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class Login extends AppCompatActivity {
    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        if(sessionManager.isLoggedIn()){
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextInputEditText emailField = findViewById(R.id.emailField);
        TextInputEditText usernameField = findViewById(R.id.usernameField);
        TextInputEditText passwordField = findViewById(R.id.passwordField);

        // Toggle between login and register modes
        btnRegister.setOnClickListener(v -> {
            isRegistering = !isRegistering;
            // change background tint to active
            androidx.core.content.ContextCompat.getColor(
                    this,
                    isRegistering ? R.color.btn_register_active : R.color.btn_register_inactive
            );

            emailField.setVisibility(isRegistering ? View.VISIBLE : View.GONE);
            btnLogin.setText(isRegistering ? "Registrarse" : "Iniciar Sesión");
            btnRegister.setText(isRegistering ? "¿Ya tienes cuenta? Inicia Sesión" : "Regístrate");
        });

        btnLogin.setOnClickListener(v -> {
            String username = usernameField.getText() != null ? usernameField.getText().toString().trim() : "";
            String password = passwordField.getText() != null ? passwordField.getText().toString().trim() : "";
            String email = emailField.getText() != null ? emailField.getText().toString().trim() : "";

            if (isRegistering) {
                if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Simular registro
                // TODO: Reemplazar con lógica real de registro
                User user = new User(username, email, username);
                sessionManager.saveSession(user);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Completa usuario y contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Simular login
                // TODO: Reemplazar con lógica real de autenticación
                User user = new User(username, username + "@mail.com", username);
                sessionManager.saveSession(user);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }
}