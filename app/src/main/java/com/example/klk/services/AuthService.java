package com.example.klk.services;

import com.example.klk.models.User;

/**
 * Interfaz que define los métodos de autenticación.
 * Sigue el principio de inversión de dependencias (DIP) del SOLID.
 */
public interface AuthService {

    /**
     * Registra un nuevo usuario con email y contraseña.
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param name Nombre del usuario
     * @param callback Callback para manejar el resultado
     */
    void registerUser(String email, String password, String name, AuthCallback callback);

    /**
     * Inicia sesión con email y contraseña.
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param callback Callback para manejar el resultado
     */
    void loginUser(String email, String password, AuthCallback callback);

    /**
     * Cierra la sesión del usuario actual.
     * @param callback Callback para manejar el resultado
     */
    void logoutUser(AuthCallback callback);

    /**
     * Obtiene el usuario actualmente autenticado.
     * @return User si está autenticado, null si no
     */
    User getCurrentUser();

    /**
     * Verifica si hay un usuario autenticado.
     * @return true si hay usuario autenticado, false si no
     */
    boolean isUserAuthenticated();

    /**
     * Interfaz para callbacks de autenticación.
     */
    interface AuthCallback {
        void onSuccess(User user);
        void onError(String error);
    }
}
