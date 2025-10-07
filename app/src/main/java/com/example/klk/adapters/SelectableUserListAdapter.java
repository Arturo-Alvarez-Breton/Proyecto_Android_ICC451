package com.example.klk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.klk.R;
import com.example.klk.models.User;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adaptador para seleccionar múltiples usuarios (creación de grupos)
 * Aplica principios SOLID: Single Responsibility, Open/Closed
 */
public class SelectableUserListAdapter extends RecyclerView.Adapter<SelectableUserListAdapter.SelectableUserViewHolder> {

    private final Context context;
    private final List<User> users;
    private final Set<String> selectedUserIds;
    private final String currentUserId;
    private OnSelectionChangedListener selectionChangedListener;

    public SelectableUserListAdapter(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.users = new ArrayList<>();
        this.selectedUserIds = new HashSet<>();
    }

    @NonNull
    @Override
    public SelectableUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_selectable, parent, false);
        return new SelectableUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectableUserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Actualiza la lista de usuarios (DRY: método reutilizable)
     */
    public void updateUsers(List<User> newUsers) {
        this.users.clear();
        if (newUsers != null) {
            // Filtrar al usuario actual (no puede agregarse a sí mismo)
            for (User user : newUsers) {
                if (!user.getId().equals(currentUserId)) {
                    this.users.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Obtiene los usuarios seleccionados
     */
    public List<User> getSelectedUsers() {
        List<User> selected = new ArrayList<>();
        for (User user : users) {
            if (selectedUserIds.contains(user.getId())) {
                selected.add(user);
            }
        }
        return selected;
    }

    /**
     * Obtiene la cantidad de usuarios seleccionados
     */
    public int getSelectedCount() {
        return selectedUserIds.size();
    }

    /**
     * Limpia todas las selecciones
     */
    public void clearSelections() {
        selectedUserIds.clear();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    /**
     * Interface para notificar cambios en la selección
     */
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    private void notifySelectionChanged() {
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selectedUserIds.size());
        }
    }

    /**
     * ViewHolder con capacidad de selección
     */
    class SelectableUserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageProfile;
        private final TextView textUserName;
        private final TextView textUserEmail;
        private final View onlineIndicator;
        private final CheckBox checkboxSelect;

        public SelectableUserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textUserName = itemView.findViewById(R.id.textUserName);
            textUserEmail = itemView.findViewById(R.id.textUserEmail);
            onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);

            // Click en el item completo para seleccionar/deseleccionar
            itemView.setOnClickListener(v -> toggleSelection());
        }

        private void toggleSelection() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                User user = users.get(position);
                if (selectedUserIds.contains(user.getId())) {
                    selectedUserIds.remove(user.getId());
                    checkboxSelect.setChecked(false);
                } else {
                    selectedUserIds.add(user.getId());
                    checkboxSelect.setChecked(true);
                }
                notifySelectionChanged();
            }
        }

        public void bind(User user) {
            // Nombre y email
            textUserName.setText(user.getName());
            textUserEmail.setText(user.getEmail());

            // Estado de selección
            checkboxSelect.setChecked(selectedUserIds.contains(user.getId()));

            // Imagen de perfil
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.baseline_person_24)
                    .error(R.drawable.baseline_person_24)
                    .into(imageProfile);
            } else {
                imageProfile.setImageResource(R.drawable.baseline_person_24);
            }

            // Indicador de online
            onlineIndicator.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);
        }
    }
}

