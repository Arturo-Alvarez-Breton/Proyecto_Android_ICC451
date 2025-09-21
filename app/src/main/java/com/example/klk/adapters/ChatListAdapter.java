package com.example.klk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.klk.R;
import com.example.klk.models.Chat;
import com.example.klk.models.MessageType;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para mostrar la lista de chats en RecyclerView
 * Implementa el patrón ViewHolder y maneja clicks en items
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private final Context context;
    private final List<Chat> chats;
    private final String currentUserId;
    private final SimpleDateFormat timeFormat;
    private OnChatClickListener onChatClickListener;

    public ChatListAdapter(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.chats = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    /**
     * Actualiza la lista de chats
     */
    public void updateChats(List<Chat> newChats) {
        this.chats.clear();
        if (newChats != null) {
            this.chats.addAll(newChats);
        }
        notifyDataSetChanged();
    }

    /**
     * Establece el listener para clicks en chats
     */
    public void setOnChatClickListener(OnChatClickListener listener) {
        this.onChatClickListener = listener;
    }

    /**
     * Obtiene el nombre del chat para mostrar
     */
    private String getChatDisplayName(Chat chat) {
        if (chat.getParticipantNames().size() == 2) {
            // Chat individual - mostrar nombre del otro usuario
            for (int i = 0; i < chat.getParticipantIds().size(); i++) {
                if (!chat.getParticipantIds().get(i).equals(currentUserId)) {
                    return chat.getParticipantNames().get(i);
                }
            }
        } else if (chat.getParticipantNames().size() > 2) {
            // Chat grupal - mostrar nombres de todos los participantes
            StringBuilder groupName = new StringBuilder();
            for (int i = 0; i < chat.getParticipantNames().size(); i++) {
                if (!chat.getParticipantIds().get(i).equals(currentUserId)) {
                    if (groupName.length() > 0) {
                        groupName.append(", ");
                    }
                    groupName.append(chat.getParticipantNames().get(i));
                }
            }
            return groupName.toString();
        }
        return "Chat";
    }

    /**
     * Formatea el último mensaje para mostrar
     */
    private String formatLastMessage(Chat chat) {
        if (chat.getLastMessage().isEmpty()) {
            return "Nuevo chat";
        }

        if (chat.getLastMessageType() == MessageType.IMAGE) {
            return "📷 Imagen";
        }

        return chat.getLastMessage();
    }

    /**
     * Formatea el tiempo del último mensaje
     */
    private String formatTime(long timestamp) {
        Date messageDate = new Date(timestamp);
        Date today = new Date();

        // Si es hoy, mostrar solo la hora
        if (isSameDay(messageDate, today)) {
            return timeFormat.format(messageDate);
        } else {
            // Si es otro día, mostrar fecha
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            return dateFormat.format(messageDate);
        }
    }

    /**
     * Verifica si dos fechas son del mismo día
     */
    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(date1).equals(dateFormat.format(date2));
    }

    /**
     * ViewHolder para items de chat
     */
    class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageProfile;
        private final TextView textChatName;
        private final TextView textLastMessage;
        private final TextView textTime;
        private final View unreadIndicator;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textChatName = itemView.findViewById(R.id.textChatName);
            textLastMessage = itemView.findViewById(R.id.textLastMessage);
            textTime = itemView.findViewById(R.id.textTime);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);

            // Configurar click listener
            itemView.setOnClickListener(v -> {
                if (onChatClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onChatClickListener.onChatClick(chats.get(position));
                    }
                }
            });
        }

        public void bind(Chat chat) {
            // Establecer nombre del chat
            textChatName.setText(getChatDisplayName(chat));

            // Establecer último mensaje
            textLastMessage.setText(formatLastMessage(chat));

            // Establecer tiempo
            textTime.setText(formatTime(chat.getLastMessageTime()));

            // Configurar imagen de perfil (placeholder por ahora)
            Glide.with(context)
                .load(R.drawable.baseline_person_24)
                .placeholder(R.drawable.baseline_person_24)
                .into(imageProfile);

            // Ocultar indicador de no leídos por ahora
            // TODO: Implementar lógica de mensajes no leídos
            unreadIndicator.setVisibility(View.GONE);
        }
    }

    /**
     * Interface para manejar clicks en chats
     */
    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }
}
