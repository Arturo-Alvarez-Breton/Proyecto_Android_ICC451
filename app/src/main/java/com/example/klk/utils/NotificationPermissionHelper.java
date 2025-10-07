package com.example.klk.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.klk.R;

/**
 * Helper para solicitar permisos de notificaciones en Android 13+
 * Aplica SOLID - Single Responsibility: Solo maneja permisos de notificaciones
 * Aplica KISS: Implementación simple y directa
 */
public class NotificationPermissionHelper {

    private final AppCompatActivity activity;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private PermissionCallback callback;

    /**
     * Interface para callbacks de resultado de permisos
     */
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    /**
     * Constructor
     * @param activity La actividad desde donde se solicita el permiso
     */
    public NotificationPermissionHelper(AppCompatActivity activity) {
        this.activity = activity;
        setupPermissionLauncher();
    }

    /**
     * Configura el launcher para solicitar permisos
     * Debe llamarse antes de onResume()
     */
    private void setupPermissionLauncher() {
        requestPermissionLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted && callback != null) {
                    callback.onPermissionGranted();
                } else if (!isGranted && callback != null) {
                    callback.onPermissionDenied();
                }
            }
        );
    }

    /**
     * Solicita permiso de notificaciones si es necesario
     * @param callback Callback para manejar el resultado
     */
    public void requestNotificationPermission(PermissionCallback callback) {
        this.callback = callback;

        // Solo necesario en Android 13 (TIRAMISU) o superior
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // En versiones anteriores, el permiso se otorga automáticamente
            if (callback != null) {
                callback.onPermissionGranted();
            }
            return;
        }

        // Verificar si ya tenemos el permiso
        if (hasNotificationPermission()) {
            if (callback != null) {
                callback.onPermissionGranted();
            }
            return;
        }

        // Verificar si debemos mostrar una explicación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            showPermissionRationale();
        } else {
            // Solicitar el permiso directamente
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    /**
     * Verifica si la app tiene permiso de notificaciones
     * @return true si tiene permiso, false en caso contrario
     */
    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true; // En versiones anteriores no se necesita permiso explícito
        }

        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Muestra un diálogo explicando por qué necesitamos el permiso
     * Aplica buenas prácticas de UX
     */
    private void showPermissionRationale() {
        new AlertDialog.Builder(activity)
            .setTitle(R.string.notification_permission_rationale_title)
            .setMessage(R.string.notification_permission_rationale_message)
            .setPositiveButton(R.string.notification_permission_ok, (dialog, which) -> {
                // Usuario acepta, solicitar permiso
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            })
            .setNegativeButton(R.string.notification_permission_no_thanks, (dialog, which) -> {
                // Usuario rechaza
                dialog.dismiss();
                if (callback != null) {
                    callback.onPermissionDenied();
                }
            })
            .setCancelable(false)
            .show();
    }

    /**
     * Solicita permiso sin mostrar explicación (solo para primera vez)
     * Útil para solicitar permiso al inicio de la app
     */
    public void requestPermissionSilently(PermissionCallback callback) {
        this.callback = callback;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (callback != null) {
                callback.onPermissionGranted();
            }
            return;
        }

        if (hasNotificationPermission()) {
            if (callback != null) {
                callback.onPermissionGranted();
            }
            return;
        }

        // Solicitar directamente sin explicación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}
