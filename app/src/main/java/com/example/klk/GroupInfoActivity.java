package com.example.klk;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.klk.adapters.GroupParticipantAdapter;
import com.example.klk.adapters.SelectableUserListAdapter;
import com.example.klk.models.Chat;
import com.example.klk.models.GroupParticipant;
import com.example.klk.models.User;
import com.example.klk.repositories.ChatRepository;
import com.example.klk.repositories.UserRepository;
import com.example.klk.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Actividad para gestionar la información de un grupo
 * Permite ver/editar foto, agregar/quitar miembros, gestionar admins
 * Aplica principios SOLID, KISS, DRY y YAGNI
 */
public class GroupInfoActivity extends AppCompatActivity {

    // Constants
    public static final String EXTRA_CHAT_ID = "chat_id";

    // UI Components
    private ImageButton buttonBack;
    private ImageView imageViewGroupPhoto;
    private ImageButton buttonEditPhoto;
    private ImageButton buttonRemovePhoto;
    private TextView textViewGroupName;
    private TextView textViewParticipantCount;
    private RecyclerView recyclerViewParticipants;
    private FloatingActionButton fabAddParticipant;

    // Business Logic
    private GroupParticipantAdapter participantAdapter;
    private ChatRepository chatRepository;
    private UserRepository userRepository;
    private SessionManager sessionManager;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    // Data
    private String chatId;
    private Chat currentChat;
    private String currentUserId;
    private boolean isCurrentUserAdmin;
    private Uri selectedImageUri;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_info);

        initializeComponents();
        setupWindowInsets();
        setupImagePicker();
        setupRecyclerView();
        setupEventListeners();
        loadGroupInfo();
    }

    private void initializeComponents() {
        // UI Components
        buttonBack = findViewById(R.id.buttonBack);
        imageViewGroupPhoto = findViewById(R.id.imageViewGroupPhoto);
        buttonEditPhoto = findViewById(R.id.buttonEditPhoto);
        buttonRemovePhoto = findViewById(R.id.buttonRemovePhoto);
        textViewGroupName = findViewById(R.id.textViewGroupName);
        textViewParticipantCount = findViewById(R.id.textViewParticipantCount);
        recyclerViewParticipants = findViewById(R.id.recyclerViewParticipants);
        fabAddParticipant = findViewById(R.id.fabAddParticipant);

        // Business Logic
        sessionManager = new SessionManager(this);
        chatRepository = ChatRepository.getInstance();
        userRepository = UserRepository.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Data
        currentUserId = sessionManager.getUserId();
        chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);

        if (chatId == null) {
            Toast.makeText(this, "Error: ID del grupo inválido", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            uploadGroupPhoto(selectedImageUri);
                        }
                    }
                }
        );
    }

    private void setupRecyclerView() {
        participantAdapter = new GroupParticipantAdapter(this, false, currentUserId);
        recyclerViewParticipants.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewParticipants.setAdapter(participantAdapter);

        participantAdapter.setOnParticipantActionListener(new GroupParticipantAdapter.OnParticipantActionListener() {
            @Override
            public void onRemoveParticipant(GroupParticipant participant) {
                confirmRemoveParticipant(participant);
            }

            @Override
            public void onMakeAdmin(GroupParticipant participant) {
                makeAdmin(participant);
            }

            @Override
            public void onRemoveAdmin(GroupParticipant participant) {
                removeAdmin(participant);
            }
        });
    }

    private void setupEventListeners() {
        buttonBack.setOnClickListener(v -> finish());

        buttonEditPhoto.setOnClickListener(v -> {
            if (isCurrentUserAdmin) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Solo los administradores pueden cambiar la foto", Toast.LENGTH_SHORT).show();
            }
        });

        buttonRemovePhoto.setOnClickListener(v -> {
            if (isCurrentUserAdmin) {
                confirmRemovePhoto();
            } else {
                Toast.makeText(this, "Solo los administradores pueden quitar la foto", Toast.LENGTH_SHORT).show();
            }
        });

        fabAddParticipant.setOnClickListener(v -> {
            if (isCurrentUserAdmin) {
                openAddParticipantsDialog();
            } else {
                Toast.makeText(this, "Solo los administradores pueden agregar participantes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupInfo() {
        firestore.collection("chats").document(chatId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentChat = documentSnapshot.toObject(Chat.class);
                        if (currentChat != null) {
                            currentChat.setId(documentSnapshot.getId());
                            updateUI();
                        }
                    } else {
                        Toast.makeText(this, "Grupo no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar información del grupo", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateUI() {
        // Verificar si el usuario actual es admin
        isCurrentUserAdmin = currentChat.isAdmin(currentUserId);

        // Actualizar nombre del grupo
        textViewGroupName.setText(currentChat.getGroupName() != null ? currentChat.getGroupName() : "Grupo sin nombre");

        // Actualizar foto del grupo
        updateGroupPhoto();

        // Actualizar contador de participantes
        int participantCount = currentChat.getParticipantIds() != null ? currentChat.getParticipantIds().size() : 0;
        textViewParticipantCount.setText(participantCount + " participantes");

        // Cargar participantes
        loadParticipants();

        // Mostrar/ocultar botones según permisos
        updatePhotoButtonsVisibility();
        fabAddParticipant.setVisibility(isCurrentUserAdmin ? View.VISIBLE : View.GONE);

        // Actualizar adaptador con el estado de admin
        participantAdapter = new GroupParticipantAdapter(this, isCurrentUserAdmin, currentUserId);
        recyclerViewParticipants.setAdapter(participantAdapter);
        setupRecyclerView();
        loadParticipants();
    }

    private void updateGroupPhoto() {
        if (currentChat.getGroupPhotoUrl() != null && !currentChat.getGroupPhotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentChat.getGroupPhotoUrl())
                    .placeholder(R.drawable.ic_group_placeholder)
                    .error(R.drawable.ic_group_placeholder)
                    .centerCrop()
                    .into(imageViewGroupPhoto);
            buttonRemovePhoto.setVisibility(isCurrentUserAdmin ? View.VISIBLE : View.GONE);
        } else {
            imageViewGroupPhoto.setImageResource(R.drawable.ic_group_placeholder);
            buttonRemovePhoto.setVisibility(View.GONE);
        }
    }

    private void updatePhotoButtonsVisibility() {
        buttonEditPhoto.setVisibility(isCurrentUserAdmin ? View.VISIBLE : View.GONE);
    }

    private void loadParticipants() {
        if (currentChat.getParticipantIds() == null || currentChat.getParticipantIds().isEmpty()) {
            return;
        }

        List<GroupParticipant> participants = new ArrayList<>();
        final int totalParticipants = currentChat.getParticipantIds().size();

        // Cargar información de cada participante
        for (int i = 0; i < currentChat.getParticipantIds().size(); i++) {
            final int index = i;
            String participantId = currentChat.getParticipantIds().get(i);
            String participantName = currentChat.getParticipantNames().get(i);
            boolean isAdmin = currentChat.isAdmin(participantId);

            // Cargar foto del usuario desde Firestore
            firestore.collection("users").document(participantId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        User user = doc.toObject(User.class);
                        String photoUrl = user != null ? user.getPhotoUrl() : null;

                        GroupParticipant participant = new GroupParticipant(
                                participantId,
                                participantName,
                                photoUrl,
                                isAdmin
                        );

                        // Insertar en la posición correcta
                        synchronized (participants) {
                            participants.add(participant);

                            // Actualizar adaptador cuando todos los participantes estén cargados
                            if (participants.size() == totalParticipants) {
                                runOnUiThread(() -> {
                                    // Ordenar por nombre antes de mostrar
                                    participants.sort((p1, p2) -> {
                                        // Admins primero
                                        if (p1.isAdmin() && !p2.isAdmin()) return -1;
                                        if (!p1.isAdmin() && p2.isAdmin()) return 1;
                                        // Luego por nombre
                                        return p1.getUserName().compareTo(p2.getUserName());
                                    });
                                    participantAdapter.setParticipants(participants);
                                });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Agregar participante sin foto si falla
                        GroupParticipant participant = new GroupParticipant(
                                participantId,
                                participantName,
                                null,
                                isAdmin
                        );

                        synchronized (participants) {
                            participants.add(participant);

                            if (participants.size() == totalParticipants) {
                                runOnUiThread(() -> {
                                    participants.sort((p1, p2) -> {
                                        if (p1.isAdmin() && !p2.isAdmin()) return -1;
                                        if (!p1.isAdmin() && p2.isAdmin()) return 1;
                                        return p1.getUserName().compareTo(p2.getUserName());
                                    });
                                    participantAdapter.setParticipants(participants);
                                });
                            }
                        }
                    });
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadGroupPhoto(Uri imageUri) {
        // Mostrar progreso
        Toast.makeText(this, "Subiendo foto...", Toast.LENGTH_SHORT).show();

        // Referencia al storage
        String fileName = "group_photos/" + chatId + "_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference photoRef = storage.getReference().child(fileName);

        // Subir imagen
        photoRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener URL de descarga
                    photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateGroupPhotoUrl(uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir la foto", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateGroupPhotoUrl(String photoUrl) {
        firestore.collection("chats").document(chatId)
                .update("groupPhotoUrl", photoUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show();
                    currentChat.setGroupPhotoUrl(photoUrl);
                    updateGroupPhoto();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar la foto", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmRemovePhoto() {
        new AlertDialog.Builder(this)
                .setTitle("Quitar foto del grupo")
                .setMessage("¿Estás seguro de que quieres quitar la foto del grupo?")
                .setPositiveButton("Quitar", (dialog, which) -> removeGroupPhoto())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void removeGroupPhoto() {
        firestore.collection("chats").document(chatId)
                .update("groupPhotoUrl", null)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Foto eliminada", Toast.LENGTH_SHORT).show();
                    currentChat.setGroupPhotoUrl(null);
                    updateGroupPhoto();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar la foto", Toast.LENGTH_SHORT).show();
                });
    }

    private void openAddParticipantsDialog() {
        // Cargar usuarios que no están en el grupo
        userRepository.getAllUsers().observe(this, allUsers -> {
            if (allUsers != null) {
                List<User> availableUsers = new ArrayList<>();
                for (User user : allUsers) {
                    if (!currentChat.getParticipantIds().contains(user.getId())) {
                        availableUsers.add(user);
                    }
                }

                if (availableUsers.isEmpty()) {
                    Toast.makeText(this, "No hay usuarios disponibles para agregar", Toast.LENGTH_SHORT).show();
                    return;
                }

                showAddParticipantsDialog(availableUsers);
            }
        });
    }

    private void showAddParticipantsDialog(List<User> availableUsers) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_participants, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewUsers);

        SelectableUserListAdapter adapter = new SelectableUserListAdapter(this, currentUserId);
        adapter.updateUsers(availableUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Agregar participantes")
                .setView(dialogView)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    List<User> selectedUsers = adapter.getSelectedUsers();
                    if (!selectedUsers.isEmpty()) {
                        addParticipants(selectedUsers);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void addParticipants(List<User> newUsers) {
        List<String> newParticipantIds = new ArrayList<>(currentChat.getParticipantIds());
        List<String> newParticipantNames = new ArrayList<>(currentChat.getParticipantNames());

        for (User user : newUsers) {
            newParticipantIds.add(user.getId());
            newParticipantNames.add(user.getName());
        }

        firestore.collection("chats").document(chatId)
                .update(
                        "participantIds", newParticipantIds,
                        "participantNames", newParticipantNames
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Participantes agregados", Toast.LENGTH_SHORT).show();
                    loadGroupInfo();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al agregar participantes", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmRemoveParticipant(GroupParticipant participant) {
        new AlertDialog.Builder(this)
                .setTitle("Quitar participante")
                .setMessage("¿Quieres quitar a " + participant.getUserName() + " del grupo?")
                .setPositiveButton("Quitar", (dialog, which) -> removeParticipant(participant))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void removeParticipant(GroupParticipant participant) {
        List<String> newParticipantIds = new ArrayList<>(currentChat.getParticipantIds());
        List<String> newParticipantNames = new ArrayList<>(currentChat.getParticipantNames());

        int index = newParticipantIds.indexOf(participant.getUserId());
        if (index != -1) {
            newParticipantIds.remove(index);
            newParticipantNames.remove(index);

            // Si era admin, quitarlo también de la lista de admins
            List<String> newAdminIds = currentChat.getAdminIds() != null ?
                    new ArrayList<>(currentChat.getAdminIds()) : new ArrayList<>();
            newAdminIds.remove(participant.getUserId());

            firestore.collection("chats").document(chatId)
                    .update(
                            "participantIds", newParticipantIds,
                            "participantNames", newParticipantNames,
                            "adminIds", newAdminIds
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Participante eliminado", Toast.LENGTH_SHORT).show();
                        loadGroupInfo();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al eliminar participante", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void makeAdmin(GroupParticipant participant) {
        List<String> newAdminIds = currentChat.getAdminIds() != null ?
                new ArrayList<>(currentChat.getAdminIds()) : new ArrayList<>();

        if (!newAdminIds.contains(participant.getUserId())) {
            newAdminIds.add(participant.getUserId());

            firestore.collection("chats").document(chatId)
                    .update("adminIds", newAdminIds)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, participant.getUserName() + " es ahora administrador", Toast.LENGTH_SHORT).show();
                        loadGroupInfo();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al hacer administrador", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void removeAdmin(GroupParticipant participant) {
        List<String> newAdminIds = currentChat.getAdminIds() != null ?
                new ArrayList<>(currentChat.getAdminIds()) : new ArrayList<>();

        // Verificar que haya al menos un admin después de quitar este
        if (newAdminIds.size() <= 1) {
            Toast.makeText(this, "Debe haber al menos un administrador", Toast.LENGTH_SHORT).show();
            return;
        }

        newAdminIds.remove(participant.getUserId());

        firestore.collection("chats").document(chatId)
                .update("adminIds", newAdminIds)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, participant.getUserName() + " ya no es administrador", Toast.LENGTH_SHORT).show();
                    loadGroupInfo();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al quitar administrador", Toast.LENGTH_SHORT).show();
                });
    }
}
