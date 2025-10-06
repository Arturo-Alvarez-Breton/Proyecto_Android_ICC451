package com.example.klk.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.klk.R;
import com.example.klk.ImageViewerActivity;
import com.example.klk.models.Message;
import com.example.klk.models.MessageType;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para mostrar mensajes en RecyclerView
 * Implementa el patrón ViewHolder y soporta diferentes tipos de mensajes
 * Actualizado con funcionalidad de vista expandida de imágenes
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TEXT_SENT = 1;
    private static final int VIEW_TYPE_TEXT_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_SENT = 3;
    private static final int VIEW_TYPE_IMAGE_RECEIVED = 4;
    private static final int VIEW_TYPE_MIXED_SENT = 5;
    private static final int VIEW_TYPE_MIXED_RECEIVED = 6;

    private final Context context;
    private final List<Message> messages;
    private final String currentUserId;
    private final SimpleDateFormat timeFormat;
    private boolean isGroupChat = false; // Nuevo campo para detectar chats grupales

    /**
     * Interfaz para callbacks de click en imágenes (Open/Closed Principle)
     */
    public interface OnImageClickListener {
        void onImageClick(String imageUrl, String senderName);
    }

    private OnImageClickListener imageClickListener;

    public MessageAdapter(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.messages = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.imageClickListener = this::openImageViewer;
    }

    /**
     * Establece si el chat es grupal o individual
     * En chats grupales se muestra el nombre del remitente
     */
    public void setGroupChat(boolean isGroupChat) {
        this.isGroupChat = isGroupChat;
        notifyDataSetChanged(); // Actualizar vista
    }

    /**
     * Método helper para abrir el visor de imágenes (DRY - reutilizado por todos los ViewHolders)
     */
    private void openImageViewer(String imageUrl, String senderName) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, imageUrl);
        intent.putExtra(ImageViewerActivity.EXTRA_SENDER_NAME, senderName);
        context.startActivity(intent);
    }

    /**
     * Permite establecer un listener personalizado (Open/Closed Principle)
     */
    public void setOnImageClickListener(OnImageClickListener listener) {
        this.imageClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        boolean isSent = message.getSenderId().equals(currentUserId);

        switch (message.getMessageType()) {
            case TEXT:
                return isSent ? VIEW_TYPE_TEXT_SENT : VIEW_TYPE_TEXT_RECEIVED;
            case IMAGE:
                return isSent ? VIEW_TYPE_IMAGE_SENT : VIEW_TYPE_IMAGE_RECEIVED;
            case MIXED:
                return isSent ? VIEW_TYPE_MIXED_SENT : VIEW_TYPE_MIXED_RECEIVED;
            default:
                return isSent ? VIEW_TYPE_TEXT_SENT : VIEW_TYPE_TEXT_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case VIEW_TYPE_TEXT_SENT:
                return new TextMessageSentViewHolder(
                    inflater.inflate(R.layout.item_message_text_sent, parent, false));
            case VIEW_TYPE_TEXT_RECEIVED:
                return new TextMessageReceivedViewHolder(
                    inflater.inflate(R.layout.item_message_text_received, parent, false));
            case VIEW_TYPE_IMAGE_SENT:
                return new ImageMessageSentViewHolder(
                    inflater.inflate(R.layout.item_message_image_sent, parent, false));
            case VIEW_TYPE_IMAGE_RECEIVED:
                return new ImageMessageReceivedViewHolder(
                    inflater.inflate(R.layout.item_message_image_received, parent, false));
            case VIEW_TYPE_MIXED_SENT:
                return new MixedMessageSentViewHolder(
                    inflater.inflate(R.layout.item_message_mixed_sent, parent, false));
            case VIEW_TYPE_MIXED_RECEIVED:
                return new MixedMessageReceivedViewHolder(
                    inflater.inflate(R.layout.item_message_mixed_received, parent, false));
            default:
                throw new IllegalArgumentException("Invalid view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof TextMessageSentViewHolder) {
            ((TextMessageSentViewHolder) holder).bind(message);
        } else if (holder instanceof TextMessageReceivedViewHolder) {
            ((TextMessageReceivedViewHolder) holder).bind(message);
        } else if (holder instanceof ImageMessageSentViewHolder) {
            ((ImageMessageSentViewHolder) holder).bind(message);
        } else if (holder instanceof ImageMessageReceivedViewHolder) {
            ((ImageMessageReceivedViewHolder) holder).bind(message);
        } else if (holder instanceof MixedMessageSentViewHolder) {
            ((MixedMessageSentViewHolder) holder).bind(message);
        } else if (holder instanceof MixedMessageReceivedViewHolder) {
            ((MixedMessageReceivedViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * Actualiza la lista de mensajes (DRY principle)
     */
    public void updateMessages(List<Message> newMessages) {
        this.messages.clear();
        if (newMessages != null) {
            this.messages.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    /**
     * Agrega un nuevo mensaje a la lista
     */
    public void addMessage(Message message) {
        this.messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    /**
     * Método helper para formatear la hora (DRY principle)
     */
    private String formatTime(long timestamp) {
        return timeFormat.format(new Date(timestamp));
    }

    /**
     * ViewHolder para mensajes de texto enviados
     */
    class TextMessageSentViewHolder extends RecyclerView.ViewHolder {
        private final TextView textContent;
        private final TextView textTime;

        public TextMessageSentViewHolder(@NonNull View itemView) {
            super(itemView);
            textContent = itemView.findViewById(R.id.textMessageContent);
            textTime = itemView.findViewById(R.id.textMessageTime);
        }

        public void bind(Message message) {
            textContent.setText(message.getContent());
            textTime.setText(formatTime(message.getTimestamp()));
        }
    }

    /**
     * ViewHolder para mensajes de texto recibidos
     */
    class TextMessageReceivedViewHolder extends RecyclerView.ViewHolder {
        private final TextView textContent;
        private final TextView textSenderName;
        private final TextView textTime;

        public TextMessageReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            textContent = itemView.findViewById(R.id.textMessageContent);
            textSenderName = itemView.findViewById(R.id.textSenderName);
            textTime = itemView.findViewById(R.id.textMessageTime);
        }

        public void bind(Message message) {
            textContent.setText(message.getContent());
            textTime.setText(formatTime(message.getTimestamp()));

            // Mostrar nombre solo en chats grupales
            if (isGroupChat) {
                textSenderName.setText(message.getSenderName());
                textSenderName.setVisibility(View.VISIBLE);
            } else {
                textSenderName.setVisibility(View.GONE);
            }
        }
    }

    /**
     * ViewHolder para mensajes de imagen enviados
     */
    class ImageMessageSentViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageMessage;
        private final TextView textTime;

        public ImageMessageSentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMessage = itemView.findViewById(R.id.imageMessage);
            textTime = itemView.findViewById(R.id.textMessageTime);
        }

        public void bind(Message message) {
            textTime.setText(formatTime(message.getTimestamp()));

            // Cargar imagen usando Glide
            Glide.with(context)
                .load(message.getImageUrl())
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error)
                .into(imageMessage);

            // Configurar click listener para la imagen
            imageMessage.setOnClickListener(v -> imageClickListener.onImageClick(message.getImageUrl(), ""));
        }
    }

    /**
     * ViewHolder para mensajes de imagen recibidos
     */
    class ImageMessageReceivedViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageMessage;
        private final TextView textSenderName;
        private final TextView textTime;

        public ImageMessageReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMessage = itemView.findViewById(R.id.imageMessage);
            textSenderName = itemView.findViewById(R.id.textSenderName);
            textTime = itemView.findViewById(R.id.textMessageTime);
        }

        public void bind(Message message) {
            textTime.setText(formatTime(message.getTimestamp()));

            // Mostrar nombre solo en chats grupales
            if (isGroupChat) {
                textSenderName.setText(message.getSenderName());
                textSenderName.setVisibility(View.VISIBLE);
            } else {
                textSenderName.setVisibility(View.GONE);
            }

            // Cargar imagen usando Glide
            Glide.with(context)
                .load(message.getImageUrl())
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error)
                .into(imageMessage);

            // Configurar click listener para la imagen
            imageMessage.setOnClickListener(v -> imageClickListener.onImageClick(message.getImageUrl(), message.getSenderName()));
        }
    }

    /**
     * ViewHolder para mensajes mixtos enviados
     */
    class MixedMessageSentViewHolder extends RecyclerView.ViewHolder {
        private final TextView textContent;
        private final TextView textTime;
        private final ImageView imageMessage;

        public MixedMessageSentViewHolder(@NonNull View itemView) {
            super(itemView);
            textContent = itemView.findViewById(R.id.textMessageContent);
            textTime = itemView.findViewById(R.id.textMessageTime);
            imageMessage = itemView.findViewById(R.id.imageMessage);
        }

        public void bind(Message message) {
            textContent.setText(message.getContent());
            textTime.setText(formatTime(message.getTimestamp()));

            // Cargar imagen usando Glide
            Glide.with(context)
                .load(message.getImageUrl())
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error)
                .into(imageMessage);

            // Configurar click listener para la imagen
            imageMessage.setOnClickListener(v -> imageClickListener.onImageClick(message.getImageUrl(), ""));
        }
    }

    /**
     * ViewHolder para mensajes mixtos recibidos
     */
    class MixedMessageReceivedViewHolder extends RecyclerView.ViewHolder {
        private final TextView textContent;
        private final TextView textSenderName;
        private final TextView textTime;
        private final ImageView imageMessage;

        public MixedMessageReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            textContent = itemView.findViewById(R.id.textMessageContent);
            textSenderName = itemView.findViewById(R.id.textSenderName);
            textTime = itemView.findViewById(R.id.textMessageTime);
            imageMessage = itemView.findViewById(R.id.imageMessage);
        }

        public void bind(Message message) {
            textContent.setText(message.getContent());
            textTime.setText(formatTime(message.getTimestamp()));

            // Mostrar nombre solo en chats grupales
            if (isGroupChat) {
                textSenderName.setText(message.getSenderName());
                textSenderName.setVisibility(View.VISIBLE);
            } else {
                textSenderName.setVisibility(View.GONE);
            }

            // Cargar imagen usando Glide
            Glide.with(context)
                .load(message.getImageUrl())
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_error)
                .into(imageMessage);

            // Configurar click listener para la imagen
            imageMessage.setOnClickListener(v -> imageClickListener.onImageClick(message.getImageUrl(), message.getSenderName()));
        }
    }
}
