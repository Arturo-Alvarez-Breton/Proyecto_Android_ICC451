package com.example.klk.services;

import android.util.Log;
import com.example.klk.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Implementación del servicio de autenticación usando Firebase Authentication.
 * Sigue los principios SOLID y DRY.
 */
public class FirebaseAuthService implements AuthService {

    private static final String TAG = "FirebaseAuthService";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public FirebaseAuthService() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void registerUser(String email, String password, String name, AuthCallback callback) {
        if (!isValidInput(email, password, name)) {
            callback.onError("Datos inválidos. Verifica email, contraseña y nombre.");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = createUserFromFirebaseUser(firebaseUser, name);
                            saveUserToFirestore(user, callback);
                        } else {
                            callback.onError("Error al crear usuario");
                        }
                    } else {
                        String error = task.getException() != null
                            ? task.getException().getMessage()
                            : "Error desconocido al registrar";
                        Log.e(TAG, "Error en registro: " + error);
                        callback.onError(getCustomErrorMessage(error));
                    }
                });
    }

    @Override
    public void loginUser(String email, String password, AuthCallback callback) {
        if (!isValidEmailAndPassword(email, password)) {
            callback.onError("Email y contraseña son requeridos");
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            loadUserFromFirestore(firebaseUser.getUid(), callback);
                        } else {
                            callback.onError("Error al iniciar sesión");
                        }
                    } else {
                        String error = task.getException() != null
                            ? task.getException().getMessage()
                            : "Error desconocido al iniciar sesión";
                        Log.e(TAG, "Error en login: " + error);
                        callback.onError(getCustomErrorMessage(error));
                    }
                });
    }

    @Override
    public void logoutUser(AuthCallback callback) {
        try {
            auth.signOut();
            callback.onSuccess(null);
        } catch (Exception e) {
            Log.e(TAG, "Error al cerrar sesión: " + e.getMessage());
            callback.onError("Error al cerrar sesión");
        }
    }

    @Override
    public User getCurrentUser() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            return createUserFromFirebaseUser(firebaseUser, firebaseUser.getDisplayName());
        }
        return null;
    }

    @Override
    public boolean isUserAuthenticated() {
        return auth.getCurrentUser() != null;
    }

    /**
     * Guarda el usuario en Firestore.
     */
    private void saveUserToFirestore(User user, AuthCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario guardado en Firestore");
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar usuario: " + e.getMessage());
                    callback.onError("Error al guardar datos del usuario");
                });
    }

    /**
     * Carga el usuario desde Firestore.
     */
    private void loadUserFromFirestore(String userId, AuthCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(userId);
                            callback.onSuccess(user);
                        } else {
                            callback.onError("Error al cargar datos del usuario");
                        }
                    } else {
                        // Usuario no existe en Firestore, crear uno básico
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = createUserFromFirebaseUser(firebaseUser, firebaseUser.getDisplayName());
                            saveUserToFirestore(user, callback);
                        } else {
                            callback.onError("Usuario no encontrado");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar usuario: " + e.getMessage());
                    callback.onError("Error al cargar datos del usuario");
                });
    }

    /**
     * Crea un objeto User desde FirebaseUser.
     */
    private User createUserFromFirebaseUser(FirebaseUser firebaseUser, String name) {
        String displayName = name != null && !name.trim().isEmpty()
            ? name.trim()
            : firebaseUser.getEmail() != null
                ? firebaseUser.getEmail().split("@")[0]
                : "Usuario";

        return new User(
            firebaseUser.getUid(),
            firebaseUser.getEmail(),
            displayName
        );
    }

    /**
     * Valida entrada para registro.
     */
    private boolean isValidInput(String email, String password, String name) {
        return isValidEmailAndPassword(email, password) &&
               name != null && !name.trim().isEmpty();
    }

    /**
     * Valida email y contraseña.
     */
    private boolean isValidEmailAndPassword(String email, String password) {
        return email != null && !email.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               password.length() >= 6;
    }

    /**
     * Convierte errores de Firebase a mensajes amigables.
     */
    private String getCustomErrorMessage(String firebaseError) {
        if (firebaseError == null) return "Error desconocido";

        if (firebaseError.contains("email address is already in use")) {
            return "Este email ya está registrado. Intenta iniciar sesión.";
        } else if (firebaseError.contains("password is invalid")) {
            return "Contraseña incorrecta.";
        } else if (firebaseError.contains("no user record")) {
            return "No existe una cuenta con este email.";
        } else if (firebaseError.contains("email address is badly formatted")) {
            return "Formato de email inválido.";
        } else if (firebaseError.contains("password should be at least 6 characters")) {
            return "La contraseña debe tener al menos 6 caracteres.";
        } else if (firebaseError.contains("network error")) {
            return "Error de conexión. Verifica tu internet.";
        }

        return "Error de autenticación. Intenta nuevamente.";
    }
}
