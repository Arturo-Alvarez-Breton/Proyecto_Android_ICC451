package com.example.klk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.klk.R;
import com.example.klk.models.GroupParticipant;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para mostrar participantes de un grupo
 * Aplica principios SOLID: Single Responsibility, Open/Closed
 */
public class GroupParticipantAdapter extends RecyclerView.Adapter<GroupParticipantAdapter.ParticipantViewHolder> {

    private final Context context;
    private List<GroupParticipant> participants;
    private final boolean isCurrentUserAdmin;
    private final String currentUserId;
    private OnParticipantActionListener listener;

    public interface OnParticipantActionListener {
        void onRemoveParticipant(GroupParticipant participant);
        void onMakeAdmin(GroupParticipant participant);
        void onRemoveAdmin(GroupParticipant participant);
    }

    public GroupParticipantAdapter(Context context, boolean isCurrentUserAdmin, String currentUserId) {
        this.context = context;
        this.participants = new ArrayList<>();
        this.isCurrentUserAdmin = isCurrentUserAdmin;
        this.currentUserId = currentUserId;
    }

    public void setParticipants(List<GroupParticipant> participants) {
        this.participants = participants != null ? participants : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnParticipantActionListener(OnParticipantActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_participant, parent, false);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        GroupParticipant participant = participants.get(position);
        holder.bind(participant);
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    class ParticipantViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewAvatar;
        private final TextView textViewName;
        private final TextView textViewAdminLabel;
        private final ImageButton buttonRemove;
        private final ImageButton buttonToggleAdmin;

        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewAdminLabel = itemView.findViewById(R.id.textViewAdminLabel);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
            buttonToggleAdmin = itemView.findViewById(R.id.buttonToggleAdmin);
        }

        public void bind(GroupParticipant participant) {
            // Nombre del participante
            textViewName.setText(participant.getUserName());

            // Avatar del participante con Glide
            if (participant.getPhotoUrl() != null && !participant.getPhotoUrl().isEmpty()) {
                Glide.with(context)
                        .load(participant.getPhotoUrl())
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .centerCrop()
                        .into(imageViewAvatar);
            } else {
                imageViewAvatar.setImageResource(R.drawable.ic_user_placeholder);
            }

            // Mostrar label de admin
            textViewAdminLabel.setVisibility(participant.isAdmin() ? View.VISIBLE : View.GONE);

            // Configurar botones (solo visibles para administradores)
            boolean isCurrentUser = participant.getUserId().equals(currentUserId);
            boolean showActions = isCurrentUserAdmin && !isCurrentUser;

            buttonRemove.setVisibility(showActions ? View.VISIBLE : View.GONE);
            buttonToggleAdmin.setVisibility(showActions ? View.VISIBLE : View.GONE);

            if (showActions) {
                // Botón de eliminar participante
                buttonRemove.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRemoveParticipant(participant);
                    }
                });

                // Botón de toggle admin
                if (participant.isAdmin()) {
                    buttonToggleAdmin.setImageResource(R.drawable.ic_remove_admin);
                    buttonToggleAdmin.setContentDescription("Quitar admin");
                    buttonToggleAdmin.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onRemoveAdmin(participant);
                        }
                    });
                } else {
                    buttonToggleAdmin.setImageResource(R.drawable.ic_add_admin);
                    buttonToggleAdmin.setContentDescription("Hacer admin");
                    buttonToggleAdmin.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onMakeAdmin(participant);
                        }
                    });
                }
            }
        }
    }
}
