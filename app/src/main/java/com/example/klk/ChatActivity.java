package com.example.klk;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.klk.adapters.MessageAdapter;
import com.example.klk.models.Message;
import com.example.klk.repositories.ChatRepository;
import com.example.klk.repositories.MessageRepository;
import com.example.klk.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

/**
 * Actividad principal del chat que maneja la mensajería en tiempo real
 * Implementa los principios SOLID y patrones de diseño
 */
public class ChatActivity extends AppCompatActivity {

    // UI Components
    private TextView textChatTitle;
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSendMessage;
    private ImageButton buttonAttachImage;
    private ImageButton buttonBack;

    // Image preview components
    private View imagePreviewContainer;
    private ImageView imagePreview;
    private Button buttonSendImage;
    private Button buttonCancelImage;
    private MessageAdapter messageAdapter;
    private MessageRepository messageRepository;
    private SessionManager sessionManager;

    // Chat data
    private String chatId;
    private String chatName;
    private String currentUserId;
    private String currentUserName;

    public static final String EXTRA_CHAT_ID = "chat_id";
    private Uri selectedImageUri;
    public static final String EXTRA_CHAT_NAME = "chat_name";
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_PERMISSION_READ_IMAGES = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        // Verificar autenticación antes de inicializar la actividad
        if (!validateUserAuthentication()) {
            return; // La actividad se cerrará si no está autenticado
        }

        initializeComponents();
        getChatDataFromIntent();
        setupRecyclerView();
        setupWindowInsets();
        setupEventListeners();
        loadMessages();
    }

    /**
     * Valida que el usuario esté autenticado antes de acceder al chat
     */
    private boolean validateUserAuthentication() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado. Redirigiendo al login...", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return false;
        }

        // Verificar que el usuario de la sesión coincida con Firebase Auth
        SessionManager sessionManager = new SessionManager(this);
        String sessionUserId = sessionManager.getUserId();
        if (sessionUserId == null || !sessionUserId.equals(currentUser.getUid())) {
            Toast.makeText(this, "Sesión inválida. Redirigiendo al login...", Toast.LENGTH_LONG).show();
            sessionManager.clearSession();
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return false;
        }

        return true;
    }

    /**
     * Inicializa todos los componentes necesarios
     */
    private void initializeComponents() {
        // UI Components
        textChatTitle = findViewById(R.id.textChatTitle);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        buttonAttachImage = findViewById(R.id.buttonAttachImage);
        buttonBack = findViewById(R.id.buttonBack);

        // Image preview components
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        imagePreview = findViewById(R.id.imagePreview);
//        buttonSendImage = findViewById(R.id.buttonSendImage);
        buttonCancelImage = findViewById(R.id.buttonCancelImage);
        messageRepository = MessageRepository.getInstance();
        sessionManager = new SessionManager(this);

        // User data
        currentUserId = sessionManager.getUserId();
        currentUserName = sessionManager.getUserName();
    }

    /**
     * Obtiene los datos del chat desde el Intent
     */
    private void getChatDataFromIntent() {
        chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);
        chatName = getIntent().getStringExtra(EXTRA_CHAT_NAME);

        if (chatId == null || chatName == null) {
            Toast.makeText(this, "Error: Datos del chat no válidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textChatTitle.setText(chatName);
    }

    /**
     * Configura el RecyclerView con el adaptador
     */
    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(this, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    /**
     * Configura los window insets para edge-to-edge display
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });
    }

    /**
     * Configura todos los listeners de eventos
     */
    private void setupEventListeners() {
        setupSendButtonListener();
        setupMessageInputListener();
        setupBackButtonListener();
        setupAttachImageListener();
        setupCancelImageButtonListener();
    }

    /**
     * Configura el listener del botón de enviar - maneja texto, imagen o ambos
     */
    private void setupSendButtonListener() {
        buttonSendMessage.setOnClickListener(v -> sendMessage());
    }

    /**
     * Configura el listener del botón de cancelar imagen
     */
    private void setupCancelImageButtonListener() {
        buttonCancelImage.setOnClickListener(v -> cancelImageSelection());
    }

    private void setupMessageInputListener() {
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Configura el listener del botón de retroceso
     */
    private void setupBackButtonListener() {
        buttonBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Configura el listener del botón de adjuntar imagen
     */
    private void setupAttachImageListener() {
        buttonAttachImage.setOnClickListener(v -> attachImage());
    }

    /**
     * Actualiza el estado del botón de envío basado en el contenido del mensaje y/o imagen
     * Un mensaje puede enviarse si tiene texto O imagen (o ambos)
     */
    private void updateSendButtonState() {
        String messageText = editTextMessage.getText().toString().trim();
        boolean hasText = !messageText.isEmpty();
        boolean hasImage = selectedImageUri != null;

        // Habilitar botón si hay texto O imagen
        boolean canSend = hasText || hasImage;
        buttonSendMessage.setEnabled(canSend);
        buttonSendMessage.setAlpha(canSend ? 1.0f : 0.5f);
    }

    /**
     * Envía un mensaje - determina automáticamente el tipo basado en el contenido
     */
    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        boolean hasText = !messageText.isEmpty();
        boolean hasImage = selectedImageUri != null;

        // Validar que hay contenido para enviar
        if (!hasText && !hasImage) {
            return;
        }

        // Deshabilitar temporalmente el botón para evitar envíos múltiples
        buttonSendMessage.setEnabled(false);

        // Determinar tipo de mensaje y enviar
        if (hasImage && hasText) {
            // Mensaje combinado: texto + imagen
            sendImageWithTextMessage(messageText);
        } else if (hasImage) {
            // Solo imagen
            sendImageOnlyMessage();
        } else {
            // Solo texto
            sendTextOnlyMessage(messageText);
        }
    }

    /**
     * Envía un mensaje de solo texto
     */
    private void sendTextOnlyMessage(String messageText) {
        messageRepository.sendTextMessage(
            chatId,
            currentUserId,
            currentUserName,
            messageText,
            new MessageRepository.MessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    runOnUiThread(() -> {
                        clearInput();
                        updateSendButtonState();
                        scrollToBottom();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this,
                            "Error al enviar mensaje: " + error,
                            Toast.LENGTH_SHORT).show();
                        updateSendButtonState();
                    });
                }
            }
        );
    }

    /**
     * Envía un mensaje de solo imagen
     */
    private void sendImageOnlyMessage() {
        messageRepository.sendImageMessage(
            chatId,
            currentUserId,
            currentUserName,
            selectedImageUri,
            new MessageRepository.MessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    runOnUiThread(() -> {
                        clearInput();
                        updateSendButtonState();
                        scrollToBottom();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this,
                            "Error al enviar imagen: " + error,
                            Toast.LENGTH_SHORT).show();
                        updateSendButtonState();
                    });
                }
            }
        );
    }

    /**
     * Envía un mensaje combinado de texto e imagen
     */
    private void sendImageWithTextMessage(String messageText) {
        messageRepository.sendImageWithText(
            chatId,
            currentUserId,
            currentUserName,
            selectedImageUri,
            messageText,
            new MessageRepository.MessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    runOnUiThread(() -> {
                        clearInput();
                        updateSendButtonState();
                        scrollToBottom();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this,
                            "Error al enviar mensaje: " + error,
                            Toast.LENGTH_SHORT).show();
                        updateSendButtonState();
                    });
                }
            }
        );
    }

    /**
     * Limpia el input de texto e imagen
     */
    private void clearInput() {
        editTextMessage.setText("");
        cancelImageSelection();
    }

    /**
     * Cancela la selección de imagen
     */
    private void cancelImageSelection() {
        selectedImageUri = null;
        imagePreviewContainer.setVisibility(View.GONE);
    }

    /**
     * Maneja la funcionalidad de adjuntar imagen con permisos
     */
    private void attachImage() {
        String permission;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = android.Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_PERMISSION_READ_IMAGES);
        } else {
            openGallery();
        }
    }

    /**
     * Abre la galería para seleccionar una imagen
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_READ_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permiso denegado para acceder a la galería", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Carga los mensajes del chat en tiempo real
     */
    private void loadMessages() {
        messageRepository.getChatMessages(chatId).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                if (messages != null) {
                    messageAdapter.updateMessages(messages);
                    scrollToBottom();
                }
            }
        });

        // Marcar mensajes como leídos cuando el usuario abre el chat
        markMessagesAsRead();
    }

    /**
     * Marca los mensajes del chat como leídos para el usuario actual
     */
    private void markMessagesAsRead() {
        ChatRepository chatRepository = ChatRepository.getInstance();
        chatRepository.markChatAsRead(chatId, currentUserId);
    }

    /**
     * Hace scroll al final del RecyclerView para mostrar el mensaje más reciente
     */
    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            recyclerViewMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageRepository != null) {
            messageRepository.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (messageRepository != null) {
            messageRepository.stopListening();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            android.net.Uri imageUri = data.getData();
            if (imageUri != null) {
                selectedImageUri = imageUri;
                imagePreview.setImageURI(selectedImageUri);
                imagePreviewContainer.setVisibility(View.VISIBLE);
                updateSendButtonState(); // Actualizar estado del botón cuando se selecciona imagen
            }
        }
    }
}
