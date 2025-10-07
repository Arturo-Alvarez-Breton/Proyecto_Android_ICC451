package com.example.klk;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.klk.models.User;
import com.example.klk.repositories.UserRepository;
import com.example.klk.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class user_profile extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    // UI Components
    private ImageView btnBack;
    private ImageView profileImage;
    private MaterialButton btnChangePhoto;
    private TextInputEditText emailField;
    private TextInputEditText nameField;
    private TextInputEditText currentPasswordField;
    private TextInputEditText newPasswordField;
    private TextInputLayout nameLayout;
    private TextInputLayout currentPasswordLayout;
    private TextInputLayout newPasswordLayout;
    private MaterialButton btnUpdateProfile;
    private MaterialButton btnCancel;
    private ProgressBar progressBar;

    // Business Logic
    private FirebaseAuth auth;
    private UserRepository userRepository;
    private SessionManager sessionManager;
    private User currentUser;
    private Uri selectedImageUri;
    private boolean hasChanges = false;

    // Image picker launcher
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        initializeComponents();
        setupWindowInsets();
        setupImagePicker();
        setupClickListeners();
        setupBackPressedHandler();
        loadUserProfile();
    }

    /**
     * Configura el manejo moderno del botón de regreso
     */
    private void setupBackPressedHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /**
     * Inicializa todos los componentes de la UI y servicios
     */
    private void initializeComponents() {
        // UI Components
        btnBack = findViewById(R.id.btnBack);
        profileImage = findViewById(R.id.profileImage);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        emailField = findViewById(R.id.emailField);
        nameField = findViewById(R.id.nameField);
        currentPasswordField = findViewById(R.id.currentPasswordField);
        newPasswordField = findViewById(R.id.newPasswordField);
        nameLayout = findViewById(R.id.nameLayout);
        currentPasswordLayout = findViewById(R.id.currentPasswordLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);

        // Services
        auth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();
        sessionManager = SessionManager.getInstance(this);

        Log.d(TAG, "Componentes inicializados correctamente");
    }

    /**
     * Configura el manejo de insets de ventana
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura el launcher para selección de imágenes
     */
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    hasChanges = true;

                    // Mostrar la imagen seleccionada
                    Glide.with(this)
                        .load(uri)
                        .centerCrop()
                        .into(profileImage);

                    Log.d(TAG, "Imagen seleccionada: " + uri.toString());
                }
            }
        );
    }

    /**
     * Configura los listeners de clicks
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> handleBackPress());

        btnCancel.setOnClickListener(v -> handleBackPress());

        btnChangePhoto.setOnClickListener(v -> {
            Log.d(TAG, "Abriendo selector de imágenes");
            imagePickerLauncher.launch("image/*");
        });

        btnUpdateProfile.setOnClickListener(v -> updateProfile());

        // Detectar cambios en los campos de texto
        setupTextChangeListeners();
    }

    /**
     * Configura listeners para detectar cambios en los campos
     */
    private void setupTextChangeListeners() {
        nameField.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hasChanges = true;
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        currentPasswordField.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hasChanges = true;
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        newPasswordField.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hasChanges = true;
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    /**
     * Carga el perfil del usuario actual
     */
    private void loadUserProfile() {
        showProgress(true);

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            Log.e(TAG, "Usuario no autenticado");
            Toast.makeText(this, R.string.error_loading_profile, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRepository.getUserById(firebaseUser.getUid(), new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    currentUser = user;
                    populateUserData(user);
                    showProgress(false);
                    Log.d(TAG, "Perfil de usuario cargado exitosamente");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error al cargar perfil: " + error);
                    Toast.makeText(user_profile.this, R.string.error_loading_profile, Toast.LENGTH_SHORT).show();
                    showProgress(false);
                });
            }
        });
    }

    /**
     * Puebla los campos con los datos del usuario
     */
    private void populateUserData(User user) {
        emailField.setText(user.getEmail());
        nameField.setText(user.getName());

        // Cargar imagen de perfil si existe
        if (!TextUtils.isEmpty(user.getProfileImageUrl())) {
            Glide.with(this)
                .load(user.getProfileImageUrl())
                .centerCrop()
                .placeholder(R.drawable.baseline_person_24)
                .into(profileImage);
        }

        hasChanges = false; // Resetear flag de cambios después de cargar datos
    }

    /**
     * Valida los datos del formulario
     */
    private boolean validateForm() {
        clearErrors();

        String name = nameField.getText().toString().trim();
        String currentPassword = currentPasswordField.getText().toString();
        String newPassword = newPasswordField.getText().toString();

        boolean isValid = true;

        // Validar nombre
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.error_name_required));
            isValid = false;
        }

        // Si hay nueva contraseña, validar contraseña actual
        if (!TextUtils.isEmpty(newPassword)) {
            if (TextUtils.isEmpty(currentPassword)) {
                currentPasswordLayout.setError(getString(R.string.error_current_password_required));
                isValid = false;
            }

            if (newPassword.length() < 6) {
                newPasswordLayout.setError(getString(R.string.error_weak_password));
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Limpia los errores de los campos
     */
    private void clearErrors() {
        nameLayout.setError(null);
        currentPasswordLayout.setError(null);
        newPasswordLayout.setError(null);
    }

    /**
     * Actualiza el perfil del usuario
     */
    private void updateProfile() {
        if (!validateForm()) {
            return;
        }

        showProgress(true);

        String newName = nameField.getText().toString().trim();
        String currentPassword = currentPasswordField.getText().toString();
        String newPassword = newPasswordField.getText().toString();

        // Actualizar nombre en Firestore
        currentUser.setName(newName);

        // Si hay cambio de contraseña, actualizar en Firebase Auth primero
        if (!TextUtils.isEmpty(newPassword)) {
            updatePassword(currentPassword, newPassword, () -> {
                // Después de actualizar contraseña, actualizar perfil
                updateUserProfile();
            });
        } else {
            // Solo actualizar perfil
            updateUserProfile();
        }
    }

    /**
     * Actualiza la contraseña del usuario
     */
    private void updatePassword(String currentPassword, String newPassword, Runnable onSuccess) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            showProgress(false);
            Toast.makeText(this, R.string.error_updating_profile, Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-autenticar al usuario
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Actualizar contraseña
                    user.updatePassword(newPassword)
                        .addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Log.d(TAG, "Contraseña actualizada exitosamente");
                                onSuccess.run();
                            } else {
                                showProgress(false);
                                String error = updateTask.getException() != null ?
                                    updateTask.getException().getMessage() : "Error desconocido";
                                Toast.makeText(this, getString(R.string.error_updating_profile, error),
                                    Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error al actualizar contraseña", updateTask.getException());
                            }
                        });
                } else {
                    showProgress(false);
                    currentPasswordLayout.setError("Contraseña actual incorrecta");
                    Log.e(TAG, "Error en re-autenticación", task.getException());
                }
            });
    }

    /**
     * Actualiza el perfil del usuario en Firestore
     */
    private void updateUserProfile() {
        if (selectedImageUri != null) {
            // Subir imagen primero, luego actualizar perfil
            uploadProfileImage(() -> saveUserProfile());
        } else {
            // Solo actualizar datos del perfil
            saveUserProfile();
        }
    }

    /**
     * Sube la imagen de perfil a Firebase Storage
     */
    private void uploadProfileImage(Runnable onSuccess) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showProgress(false);
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + user.getUid() + ".jpg");

        profileImageRef.putFile(selectedImageUri)
            .addOnSuccessListener(taskSnapshot -> {
                profileImageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        currentUser.setProfileImageUrl(uri.toString());
                        Log.d(TAG, "Imagen de perfil subida exitosamente");
                        onSuccess.run();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al obtener URL de descarga", e);
                        showProgress(false);
                        Toast.makeText(this, getString(R.string.error_updating_profile, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al subir imagen de perfil", e);
                showProgress(false);
                Toast.makeText(this, getString(R.string.error_updating_profile, e.getMessage()),
                    Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Guarda el perfil del usuario en Firestore
     */
    private void saveUserProfile() {
        userRepository.addUser(currentUser, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(user_profile.this, R.string.success_profile_updated, Toast.LENGTH_SHORT).show();
                    hasChanges = false;

                    // Actualizar sesión
                    sessionManager.updateUserSession(user);

                    Log.d(TAG, "Perfil actualizado exitosamente");
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(user_profile.this, getString(R.string.error_updating_profile, error),
                        Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al guardar perfil: " + error);
                });
            }
        });
    }

    /**
     * Maneja el botón de regreso con confirmación si hay cambios
     */
    private void handleBackPress() {
        if (hasChanges) {
            new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_discard_changes)
                .setMessage(R.string.confirm_discard_changes)
                .setPositiveButton(R.string.btn_discard, (dialog, which) -> finish())
                .setNegativeButton(R.string.btn_keep_editing, null)
                .show();
        } else {
            finish();
        }
    }

    /**
     * Muestra u oculta el indicador de progreso
     */
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdateProfile.setEnabled(!show);
        btnChangePhoto.setEnabled(!show);
    }
}