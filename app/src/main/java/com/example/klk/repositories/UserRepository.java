package com.example.klk.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.klk.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository para manejar las operaciones de usuarios con Firebase Firestore
 * Implementa el patrón Repository y Singleton
 */
public class UserRepository {
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore firestore;
    private final CollectionReference usersRef;
    private ListenerRegistration usersListener;

    // Singleton pattern - volatile para asegurar la visibilidad entre hilos
    private static volatile UserRepository instance;

    private UserRepository() {
        firestore = FirebaseFirestore.getInstance();
        usersRef = firestore.collection(USERS_COLLECTION);
    }

    public static UserRepository getInstance() {
        // Double-checked locking para un singleton eficiente y seguro en hilos
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Agrega un nuevo usuario al sistema de forma robusta.
     */
    public void addUser(User user, UserCallback callback) {
        if (user == null || user.getId() == null || user.getId().trim().isEmpty()) {
            String errorMsg = "Error: Intento de agregar un usuario nulo o con ID inválido.";
            android.util.Log.e("UserRepository", errorMsg);
            if (callback != null) {
                callback.onError(errorMsg);
            }
            return;
        }

        usersRef.document(user.getId())
            .set(user)
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("UserRepository", "Usuario agregado/actualizado exitosamente: " + user.getId());
                if (callback != null) {
                    callback.onSuccess(user);
                }
            })
            .addOnFailureListener(e -> {
                String errorMsg = "Error al agregar usuario en Firestore: " + e.getMessage();
                android.util.Log.e("UserRepository", errorMsg, e);
                if (callback != null) {
                    callback.onError(errorMsg);
                }
            });
    }

    /**
     * Obtiene un usuario por su ID de forma robusta.
     */
    public void getUserById(String userId, UserCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            String errorMsg = "Error: El ID de usuario proporcionado es nulo o vacío.";
            android.util.Log.e("UserRepository", errorMsg);
            if (callback != null) {
                callback.onError(errorMsg);
            }
            return;
        }

        usersRef.document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        user.setId(documentSnapshot.getId());
                        android.util.Log.d("UserRepository", "Usuario encontrado: " + userId);
                        if (callback != null) {
                            callback.onSuccess(user);
                        }
                    } else {
                        String errorMsg = "Error: El documento del usuario existe pero no se pudo deserializar: " + userId;
                        android.util.Log.e("UserRepository", errorMsg);
                        if (callback != null) {
                            callback.onError(errorMsg);
                        }
                    }
                } else {
                    String errorMsg = "Usuario no encontrado en Firestore: " + userId;
                    android.util.Log.w("UserRepository", errorMsg);
                    if (callback != null) {
                        callback.onError(errorMsg);
                    }
                }
            })
            .addOnFailureListener(e -> {
                String errorMsg = "Error al obtener usuario por ID: " + e.getMessage();
                android.util.Log.e("UserRepository", errorMsg, e);
                if (callback != null) {
                    callback.onError(errorMsg);
                }
            });
    }

    /**
     * Busca usuarios por nombre o email (versión robusta y simplificada)
     */
    public LiveData<List<User>> searchUsers(String query) {
        MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();

        if (query == null || query.trim().isEmpty()) {
            usersLiveData.setValue(new ArrayList<>());
            return usersLiveData;
        }

        String queryLowerCase = query.toLowerCase().trim();
        android.util.Log.d("UserRepository", "Buscando usuarios con query: '" + queryLowerCase + "'");

        usersRef.get()
            .addOnSuccessListener(querySnapshot -> {
                List<User> matchingUsers = new ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                    try {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            user.setId(document.getId());

                            String userName = user.getName() != null ? user.getName().toLowerCase() : "";
                            String userEmail = user.getEmail() != null ? user.getEmail().toLowerCase() : "";

                            if (userName.contains(queryLowerCase) || userEmail.contains(queryLowerCase)) {
                                matchingUsers.add(user);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("UserRepository", "Error al procesar documento en búsqueda: " + document.getId(), e);
                    }
                }
                android.util.Log.d("UserRepository", "Búsqueda encontró " + matchingUsers.size() + " usuarios.");
                usersLiveData.setValue(matchingUsers);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("UserRepository", "Error en la consulta de búsqueda de usuarios.", e);
                usersLiveData.setValue(new ArrayList<>());
            });

        return usersLiveData;
    }

    /**
     * Obtiene todos los usuarios registrados (versión robusta)
     */
    public LiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
        android.util.Log.d("UserRepository", "Obteniendo todos los usuarios...");

        usersRef.get()
            .addOnSuccessListener(querySnapshot -> {
                List<User> users = new ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                    try {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            user.setId(document.getId());
                            users.add(user);
                        } else {
                            android.util.Log.w("UserRepository", "Documento de usuario nulo o malformado: " + document.getId());
                        }
                    } catch (Exception e) {
                        android.util.Log.e("UserRepository", "Error al procesar documento de usuario: " + document.getId(), e);
                    }
                }

                users.sort((u1, u2) -> {
                    String name1 = u1.getName() != null ? u1.getName() : "";
                    String name2 = u2.getName() != null ? u2.getName() : "";
                    return name1.compareToIgnoreCase(name2);
                });

                android.util.Log.d("UserRepository", "Se obtuvieron y procesaron " + users.size() + " usuarios.");
                usersLiveData.setValue(users);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("UserRepository", "Fallo al obtener todos los usuarios de Firestore.", e);
                usersLiveData.setValue(new ArrayList<>());
            });

        return usersLiveData;
    }

    /**
     * Actualiza el estado online del usuario de forma robusta.
     */
    public void updateUserOnlineStatus(String userId, boolean isOnline) {
        if (userId == null || userId.trim().isEmpty()) {
            android.util.Log.w("UserRepository", "Intento de actualizar estado con ID de usuario nulo.");
            return;
        }
        usersRef.document(userId)
            .update("online", isOnline, "lastSeen", System.currentTimeMillis())
            .addOnFailureListener(e -> {
                android.util.Log.e("UserRepository", "Fallo al actualizar estado online para usuario: " + userId, e);
            });
    }

    /**
     * Detiene el listener de usuarios para prevenir memory leaks.
     */
    public void stopListening() {
        if (usersListener != null) {
            usersListener.remove();
            usersListener = null;
            android.util.Log.d("UserRepository", "Listener de usuarios detenido.");
        }
    }

    /**
     * Interface para callbacks de operaciones de usuarios
     */
    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    /**
     * Interface para callbacks de lista de usuarios
     */
    public interface UsersCallback {
        void onSuccess(List<User> users);
        void onError(String error);
    }
}
