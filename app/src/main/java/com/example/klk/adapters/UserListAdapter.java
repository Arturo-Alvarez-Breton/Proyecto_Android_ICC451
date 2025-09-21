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
import com.example.klk.models.User;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para mostrar la lista de usuarios en RecyclerView
 * Implementa el patrón ViewHolder y maneja clicks en items
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> users;
    private final String currentUserId;
    private OnUserClickListener onUserClickListener;

    public UserListAdapter(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.users = new ArrayList<>();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Actualiza la lista de usuarios
     */
    public void updateUsers(List<User> newUsers) {
        this.users.clear();
        if (newUsers != null) {
            this.users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    /**
     * Establece el listener para clicks en usuarios
     */
    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    /**
     * ViewHolder para items de usuario
     */
    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageProfile;
        private final TextView textUserName;
        private final TextView textUserEmail;
        private final View onlineIndicator;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textUserName = itemView.findViewById(R.id.textUserName);
            textUserEmail = itemView.findViewById(R.id.textUserEmail);
            onlineIndicator = itemView.findViewById(R.id.onlineIndicator);

            // Configurar click listener
            itemView.setOnClickListener(v -> {
                if (onUserClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onUserClickListener.onUserClick(users.get(position));
                    }
                }
            });
        }

        public void bind(User user) {
            // Establecer nombre del usuario
            textUserName.setText(user.getName());

            // Establecer email del usuario
            textUserEmail.setText(user.getEmail());

            // Configurar imagen de perfil
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.baseline_person_24)
                    .error(R.drawable.baseline_person_24)
                    .into(imageProfile);
            } else {
                imageProfile.setImageResource(R.drawable.baseline_person_24);
            }

            // Mostrar indicador de online/offline
            onlineIndicator.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Interface para manejar clicks en usuarios
     */
    public interface OnUserClickListener {
        void onUserClick(User user);
    }
}
