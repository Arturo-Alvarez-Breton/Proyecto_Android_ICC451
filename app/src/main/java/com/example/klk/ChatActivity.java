package com.example.klk;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.klk.adapters.MessageAdapter;
import com.example.klk.models.Message;
import com.example.klk.repositories.MessageRepository;
import com.example.klk.utils.SessionManager;
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

    // Business Logic
    private MessageAdapter messageAdapter;
    private MessageRepository messageRepository;
    private SessionManager sessionManager;

    // Chat data
    private String chatId;
    private String chatName;
    private String currentUserId;
    private String currentUserName;

    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_CHAT_NAME = "chat_name";
    private static final int REQUEST_IMAGE_PICK = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        initializeComponents();
        getChatDataFromIntent();
        setupRecyclerView();
        setupWindowInsets();
        setupEventListeners();
        loadMessages();
    }

    /**
     * Inicializa todos los componentes necesarios
     * Principio de Single Responsibility
     */
    private void initializeComponents() {
        // UI Components
        textChatTitle = findViewById(R.id.textChatTitle);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        buttonAttachImage = findViewById(R.id.buttonAttachImage);
        buttonBack = findViewById(R.id.buttonBack);

        // Business Logic Components
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
        layoutManager.setStackFromEnd(true); // Mostrar mensajes más recientes al final

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
    }

    /**
     * Configura el listener del botón de enviar
     */
    private void setupSendButtonListener() {
        buttonSendMessage.setOnClickListener(v -> sendTextMessage());
    }

    /**
     * Configura el listener del campo de texto para habilitar/deshabilitar el botón de envío
     */
    private void setupMessageInputListener() {
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState(s.toString().trim());
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
     * Actualiza el estado del botón de envío basado en el contenido del mensaje
     * Principio DRY
     */
    private void updateSendButtonState(String messageText) {
        boolean hasText = !messageText.isEmpty();
        buttonSendMessage.setEnabled(hasText);
        buttonSendMessage.setAlpha(hasText ? 1.0f : 0.5f);
    }

    /**
     * Envía un mensaje de texto
     */
    private void sendTextMessage() {
        String messageText = editTextMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        // Deshabilitar temporalmente el botón para evitar envíos múltiples
        buttonSendMessage.setEnabled(false);

        messageRepository.sendTextMessage(
            chatId,
            currentUserId,
            currentUserName,
            messageText,
            new MessageRepository.MessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    runOnUiThread(() -> {
                        editTextMessage.setText("");
                        buttonSendMessage.setEnabled(true);
                        scrollToBottom();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this,
                            "Error al enviar mensaje: " + error,
                            Toast.LENGTH_SHORT).show();
                        buttonSendMessage.setEnabled(true);
                    });
                }
            }
        );
    }

    /**
     * Maneja la funcionalidad de adjuntar imagen
     */
    private void attachImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
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
        // Detener el listener de Firebase para evitar memory leaks
        if (messageRepository != null) {
            messageRepository.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Detener el listener al salir de la actividad
        if (messageRepository != null) {
            messageRepository.stopListening();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            // Aquí obtienes la URI de la imagen seleccionada
            android.net.Uri imageUri = data.getData();
            if (imageUri != null) {
                Toast.makeText(this, "Imagen seleccionada: " + imageUri.toString(), Toast.LENGTH_SHORT).show();
                // Aquí puedes continuar con la subida a Firebase en el siguiente paso
            }
        }
    }
}
