package com.example.klk.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.example.klk.ChatActivity;
import com.example.klk.R;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper class para manejar notificaciones push
 * Aplica el principio SOLID de Single Responsibility:
 * Solo se encarga de crear y mostrar notificaciones
 * Actualizado con soporte para vista previa de imágenes (BigPictureStyle)
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "klk_messages_channel";
    private static final String CHANNEL_NAME = "Mensajes de KlK";
    private static final String CHANNEL_DESCRIPTION = "Notificaciones de mensajes nuevos en tus chats";
    private static final int NOTIFICATION_ID_BASE = 1000;

    private final Context context;
    private final NotificationManager notificationManager;
    private final ExecutorService executorService; // Para operaciones asíncronas

    /**
     * Constructor privado para inicializar el helper
     * @param context Contexto de la aplicación
     */
    private NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager)
            this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.executorService = Executors.newSingleThreadExecutor();
        createNotificationChannel();
    }

    /**
     * Método estático para obtener una instancia (KISS - Keep It Simple)
     * @param context Contexto de la aplicación
     * @return Instancia de NotificationHelper
     */
    public static NotificationHelper getInstance(Context context) {
        return new NotificationHelper(context);
    }

    /**
     * Crea el canal de notificaciones para Android 8.0+
     * Este método es idempotente - se puede llamar múltiples veces sin problema
     */
    private void createNotificationChannel() {
        // Los canales solo son necesarios en Android 8.0 (API 26) y superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // Alta prioridad para mensajes
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setShowBadge(true);

            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Muestra una notificación de mensaje nuevo
     * @param chatId ID del chat
     * @param chatName Nombre del chat o remitente
     * @param messageContent Contenido del mensaje o URL de imagen
     * @param isImage Si el mensaje es una imagen
     */
    public void showMessageNotification(String chatId, String chatName,
                                       String messageContent, boolean isImage) {
        if (isImage && messageContent != null && !messageContent.isEmpty()) {
            // Si es una imagen, cargarla y mostrar con BigPictureStyle
            showImageNotification(chatId, chatName, messageContent);
        } else {
            // Mostrar notificación de texto estándar
            showTextNotification(chatId, chatName, messageContent);
        }
    }

    /**
     * Muestra una notificación de texto estándar (DRY - método extraído)
     */
    private void showTextNotification(String chatId, String chatName, String messageContent) {
        Intent intent = createChatIntent(chatId, chatName);
        PendingIntent pendingIntent = createPendingIntent(intent, chatId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(chatName)
            .setContentText(messageContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(getDefaultNotificationSound())
            .setVibrate(new long[]{0, 250, 250, 250});

        // Estilo expandido para mensajes largos (KISS)
        if (messageContent != null && messageContent.length() > 40) {
            builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(messageContent));
        }

        int notificationId = NOTIFICATION_ID_BASE + Math.abs(chatId.hashCode() % 10000);
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Muestra una notificación con imagen usando BigPictureStyle
     * Carga la imagen de forma asíncrona (YAGNI - solo lo necesario)
     */
    private void showImageNotification(String chatId, String chatName, String imageUrl) {
        // Ejecutar carga de imagen en background thread
        executorService.execute(() -> {
            try {
                // Cargar la imagen usando Glide de forma síncrona
                FutureTarget<Bitmap> futureTarget = Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .submit(512, 512); // Tamaño optimizado para notificaciones

                Bitmap bitmap = futureTarget.get(); // Obtener bitmap de forma síncrona

                // Crear y mostrar notificación con la imagen
                showNotificationWithBitmap(chatId, chatName, bitmap);

                // Limpiar recursos de Glide
                Glide.with(context).clear(futureTarget);

            } catch (Exception e) {
                Log.e(TAG, "Error al cargar imagen para notificación: " + e.getMessage());
                // Fallback: mostrar notificación de texto
                showTextNotification(chatId, chatName,
                    context.getString(R.string.notification_new_image_message));
            }
        });
    }

    /**
     * Muestra la notificación con el bitmap cargado (DRY - método extraído)
     */
    private void showNotificationWithBitmap(String chatId, String chatName, Bitmap bitmap) {
        Intent intent = createChatIntent(chatId, chatName);
        PendingIntent pendingIntent = createPendingIntent(intent, chatId);

        String contentText = context.getString(R.string.notification_new_image_message);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(chatName)
            .setContentText(contentText)
            .setLargeIcon(bitmap) // Icono grande (thumbnail)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(getDefaultNotificationSound())
            .setVibrate(new long[]{0, 250, 250, 250});

        // BigPictureStyle para mostrar imagen completa al expandir
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle()
            .bigPicture(bitmap)
            .bigLargeIcon((Bitmap) null) // Ocultar large icon cuando está expandida
            .setSummaryText(contentText);

        builder.setStyle(bigPictureStyle);

        int notificationId = NOTIFICATION_ID_BASE + Math.abs(chatId.hashCode() % 10000);
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Crea un Intent para abrir el chat específico
     * @param chatId ID del chat
     * @param chatName Nombre del chat
     * @return Intent configurado
     */
    private Intent createChatIntent(String chatId, String chatName) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CHAT_ID, chatId);
        intent.putExtra(ChatActivity.EXTRA_CHAT_NAME, chatName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    /**
     * Crea un PendingIntent con flags apropiados según la versión de Android
     * @param intent Intent a envolver
     * @param chatId ID del chat para request code único
     * @return PendingIntent configurado
     */
    private PendingIntent createPendingIntent(Intent intent, String chatId) {
        int requestCode = Math.abs(chatId.hashCode());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;

        // En Android 12+ necesitamos especificar mutabilidad
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getActivity(context, requestCode, intent, flags);
    }

    /**
     * Obtiene el sonido de notificación predeterminado del sistema
     * @return URI del sonido
     */
    private Uri getDefaultNotificationSound() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    /**
     * Cancela todas las notificaciones de la app
     * Útil cuando el usuario abre la app
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    /**
     * Cancela la notificación de un chat específico
     * @param chatId ID del chat
     */
    public void cancelChatNotification(String chatId) {
        int notificationId = NOTIFICATION_ID_BASE + Math.abs(chatId.hashCode() % 10000);
        notificationManager.cancel(notificationId);
    }

    /**
     * Limpia recursos al destruir (buena práctica de Android)
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
