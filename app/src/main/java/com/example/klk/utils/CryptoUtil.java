package com.example.klk.utils;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Utilidad para cifrado y descifrado de mensajes usando AES-256-GCM
 * Implementa el principio de Responsabilidad Única (SOLID)
 * Uso de algoritmos seguros y probados (buenas prácticas)
 */
public class CryptoUtil {
    private static final String TAG = "CryptoUtil";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int AES_KEY_SIZE = 256;

    // Clave maestra derivada del chatId (en producción, usar un sistema de gestión de claves más robusto)
    private static final String MASTER_SALT = "KLK_CHAT_APP_2025_SECURE";

    /**
     * Cifra un texto plano usando AES-256-GCM
     * @param plainText Texto a cifrar
     * @param chatId ID del chat (usado para derivar la clave)
     * @return Texto cifrado en Base64, o null si hay error
     */
    public static String encrypt(String plainText, String chatId) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            // Generar clave a partir del chatId
            SecretKey secretKey = deriveKeyFromChatId(chatId);

            // Generar IV aleatorio para GCM
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // Configurar cifrado
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Cifrar el texto
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combinar IV + texto cifrado para transmisión
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            // Codificar en Base64 para almacenamiento seguro
            return Base64.encodeToString(combined, Base64.NO_WRAP);

        } catch (Exception e) {
            Log.e(TAG, "Error al cifrar mensaje: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Descifra un texto cifrado usando AES-256-GCM
     * @param encryptedText Texto cifrado en Base64
     * @param chatId ID del chat (usado para derivar la clave)
     * @return Texto descifrado, o el texto original si hay error
     */
    public static String decrypt(String encryptedText, String chatId) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            // Decodificar de Base64
            byte[] combined = Base64.decode(encryptedText, Base64.NO_WRAP);

            // Separar IV del texto cifrado
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedBytes = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            // Generar clave a partir del chatId
            SecretKey secretKey = deriveKeyFromChatId(chatId);

            // Configurar descifrado
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Descifrar el texto
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            Log.e(TAG, "Error al descifrar mensaje: " + e.getMessage(), e);
            // Retornar el texto original si no se puede descifrar (compatibilidad con mensajes antiguos)
            return encryptedText;
        }
    }

    /**
     * Deriva una clave AES-256 a partir del chatId usando SHA-256
     * Implementa un método determinístico para que todos los participantes del chat
     * puedan generar la misma clave
     * @param chatId ID del chat
     * @return SecretKey derivada
     */
    private static SecretKey deriveKeyFromChatId(String chatId) throws Exception {
        // Combinar chatId con salt para mayor seguridad
        String keyMaterial = chatId + MASTER_SALT;

        // Usar SHA-256 para derivar una clave de 256 bits
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(keyMaterial.getBytes(StandardCharsets.UTF_8));

        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Verifica si un texto está cifrado (formato Base64 válido con longitud correcta)
     * @param text Texto a verificar
     * @return true si parece estar cifrado, false en caso contrario
     */
    public static boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        try {
            byte[] decoded = Base64.decode(text, Base64.NO_WRAP);
            // Verificar que tenga al menos el IV + algunos bytes de datos
            return decoded.length > GCM_IV_LENGTH;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Limpia recursos sensibles de la memoria (buena práctica de seguridad)
     * @param key Clave a limpiar
     */
    public static void clearKey(SecretKey key) {
        if (key != null && key.getEncoded() != null) {
            byte[] encoded = key.getEncoded();
            for (int i = 0; i < encoded.length; i++) {
                encoded[i] = 0;
            }
        }
    }
}

