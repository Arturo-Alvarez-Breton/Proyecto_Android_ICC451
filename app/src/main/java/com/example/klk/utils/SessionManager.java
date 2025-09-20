package com.example.klk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.klk.models.User;

/**
 * Maneja la gestión de sesión de usuario usando SharedPreferences.
 * Almacena y recupera los datos del usuario para la sesión de inicio.
 */
public class SessionManager {
    // Nombre del archivo de preferencias
    private static final String PREF_NAME = "user_session";

    // Claves para almacenar los datos del usuario
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PROFILE_IMAGE_URL = "user_profile_image_url";
    private static final String KEY_USER_IS_ONLINE = "user_is_online";
    private static final String KEY_USER_LAST_SEEN = "user_last_seen";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    /**
     * Inicializa el SessionManager con el contexto de la aplicación.
     */
    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Guarda los datos de la sesión del usuario en SharedPreferences.
     * @param user El objeto usuario a guardar.
     */
    public void saveSession(User user) {
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_PROFILE_IMAGE_URL, user.getProfileImageUrl());
        editor.putBoolean(KEY_USER_IS_ONLINE, user.isOnline());
        editor.putLong(KEY_USER_LAST_SEEN, user.getLastSeen());
        editor.apply();
    }

    /**
     * Recupera los datos del usuario desde SharedPreferences.
     * @return El objeto usuario si existe sesión, null en caso contrario.
     */
    public User getUser() {
        String id = sharedPreferences.getString(KEY_USER_ID, null);
        String email = sharedPreferences.getString(KEY_USER_EMAIL, null);
        String name = sharedPreferences.getString(KEY_USER_NAME, null);
        String profileImageUrl = sharedPreferences.getString(KEY_USER_PROFILE_IMAGE_URL, "");
        boolean isOnline = sharedPreferences.getBoolean(KEY_USER_IS_ONLINE, false);
        long lastSeen = sharedPreferences.getLong(KEY_USER_LAST_SEEN, 0);

        if (id == null || email == null || name == null) {
            return null;
        }

        return new User(id, email, name, profileImageUrl, isOnline, lastSeen);
    }

    /**
     * Elimina todos los datos de la sesión en SharedPreferences.
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    /**
     * Verifica si hay un usuario actualmente logueado.
     * @return true si existe sesión de usuario, false en caso contrario.
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getString(KEY_USER_ID, null) != null;
    }
}
