package com.example.klk.repositories;

import android.content.Context;
import com.example.klk.models.User;
import com.example.klk.services.AuthService;
import com.example.klk.services.FirebaseAuthService;
import com.example.klk.utils.SessionManager;

/**
 * Repositorio que maneja la autenticación y la sesión del usuario.
 * Implementa el patrón Repository y sigue principios SOLID.
 * Combina Firebase Authentication con gestión local de sesión.
 */
public class AuthRepository {

    private final AuthService authService;
    private final SessionManager sessionManager;

    public AuthRepository(Context context) {
        this.authService = new FirebaseAuthService();
        this.sessionManager = new SessionManager(context);
    }

    /**
     * Registra un nuevo usuario.
     */
    public void registerUser(String email, String password, String name, AuthCallback callback) {
        authService.registerUser(email, password, name, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                sessionManager.saveSession(user);
                callback.onSuccess(user);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Inicia sesión del usuario.
     */
    public void loginUser(String email, String password, AuthCallback callback) {
        authService.loginUser(email, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                sessionManager.saveSession(user);
                callback.onSuccess(user);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Cierra la sesión del usuario.
     */
    public void logoutUser(AuthCallback callback) {
        authService.logoutUser(new AuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                sessionManager.clearSession();
                callback.onSuccess(null);
            }

            @Override
            public void onError(String error) {
                // Aún así limpiamos la sesión local
                sessionManager.clearSession();
                callback.onError(error);
            }
        });
    }

    /**
     * Verifica si el usuario está autenticado.
     */
    public boolean isUserAuthenticated() {
        return authService.isUserAuthenticated() && sessionManager.isLoggedIn();
    }

    /**
     * Obtiene el usuario actual desde la sesión local.
     */
    public User getCurrentUser() {
        return sessionManager.getUser();
    }

    /**
     * Sincroniza la sesión local con Firebase.
     */
    public void syncSession(AuthCallback callback) {
        if (authService.isUserAuthenticated()) {
            User firebaseUser = authService.getCurrentUser();
            if (firebaseUser != null) {
                sessionManager.saveSession(firebaseUser);
                callback.onSuccess(firebaseUser);
            } else {
                sessionManager.clearSession();
                callback.onError("Error al sincronizar sesión");
            }
        } else {
            sessionManager.clearSession();
            callback.onError("Usuario no autenticado");
        }
    }

    /**
     * Interfaz para callbacks del repositorio.
     */
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String error);
    }
}
