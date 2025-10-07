package com.example.klk;

import com.example.klk.utils.CryptoUtil;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la clase CryptoUtil
 * Verifica el correcto funcionamiento del cifrado y descifrado de mensajes
 */
public class CryptoUtilTest {

    @Test
    public void testEncryptDecrypt_SimpleMessage() {
        // Arrange
        String originalMessage = "Hola, ¿cómo estás?";
        String chatId = "chat123";

        // Act
        String encrypted = CryptoUtil.encrypt(originalMessage, chatId);
        String decrypted = CryptoUtil.decrypt(encrypted, chatId);

        // Assert
        assertNotNull("El mensaje cifrado no debe ser null", encrypted);
        assertNotEquals("El mensaje cifrado debe ser diferente al original", originalMessage, encrypted);
        assertEquals("El mensaje descifrado debe ser igual al original", originalMessage, decrypted);
    }

    @Test
    public void testEncryptDecrypt_EmptyMessage() {
        // Arrange
        String originalMessage = "";
        String chatId = "chat123";

        // Act
        String encrypted = CryptoUtil.encrypt(originalMessage, chatId);
        String decrypted = CryptoUtil.decrypt(encrypted, chatId);

        // Assert
        assertEquals("Los mensajes vacíos deben permanecer vacíos", originalMessage, encrypted);
        assertEquals("Los mensajes vacíos deben permanecer vacíos", originalMessage, decrypted);
    }

    @Test
    public void testEncryptDecrypt_NullMessage() {
        // Arrange
        String originalMessage = null;
        String chatId = "chat123";

        // Act
        String encrypted = CryptoUtil.encrypt(originalMessage, chatId);
        String decrypted = CryptoUtil.decrypt(encrypted, chatId);

        // Assert
        assertNull("Los mensajes null deben permanecer null", encrypted);
        assertNull("Los mensajes null deben permanecer null", decrypted);
    }

    @Test
    public void testEncryptDecrypt_LongMessage() {
        // Arrange
        String originalMessage = "Este es un mensaje muy largo que contiene múltiples caracteres, " +
                "incluyendo números 123456, símbolos !@#$%^&*(), y emojis 😊🔐💬. " +
                "El objetivo es verificar que el cifrado funciona correctamente con textos extensos.";
        String chatId = "chat456";

        // Act
        String encrypted = CryptoUtil.encrypt(originalMessage, chatId);
        String decrypted = CryptoUtil.decrypt(encrypted, chatId);

        // Assert
        assertNotNull("El mensaje cifrado no debe ser null", encrypted);
        assertEquals("El mensaje descifrado debe ser igual al original", originalMessage, decrypted);
    }

    @Test
    public void testEncryptDecrypt_SpecialCharacters() {
        // Arrange
        String originalMessage = "Caracteres especiales: áéíóú ñ ¿? ¡! @#$%^&*() {} [] <>";
        String chatId = "chat789";

        // Act
        String encrypted = CryptoUtil.encrypt(originalMessage, chatId);
        String decrypted = CryptoUtil.decrypt(encrypted, chatId);

        // Assert
        assertEquals("Debe manejar correctamente caracteres especiales", originalMessage, decrypted);
    }

    @Test
    public void testDifferentChats_DifferentEncryption() {
        // Arrange
        String message = "Mensaje secreto";
        String chatId1 = "chat_alice_bob";
        String chatId2 = "chat_alice_charlie";

        // Act
        String encrypted1 = CryptoUtil.encrypt(message, chatId1);
        String encrypted2 = CryptoUtil.encrypt(message, chatId2);

        // Assert
        assertNotEquals("El mismo mensaje en diferentes chats debe cifrarse diferente",
                encrypted1, encrypted2);

        // Verify both decrypt correctly
        assertEquals(message, CryptoUtil.decrypt(encrypted1, chatId1));
        assertEquals(message, CryptoUtil.decrypt(encrypted2, chatId2));
    }

    @Test
    public void testDecrypt_WrongChatId() {
        // Arrange
        String originalMessage = "Mensaje confidencial";
        String correctChatId = "chat_correct";
        String wrongChatId = "chat_wrong";

        // Act
        String encrypted = CryptoUtil.encrypt(originalMessage, correctChatId);
        String decryptedWithWrongKey = CryptoUtil.decrypt(encrypted, wrongChatId);

        // Assert
        assertNotEquals("No debe descifrar correctamente con chatId incorrecto",
                originalMessage, decryptedWithWrongKey);
    }

    @Test
    public void testIsEncrypted() {
        // Arrange
        String plainText = "Texto plano";
        String chatId = "chat123";
        String encryptedText = CryptoUtil.encrypt(plainText, chatId);

        // Act & Assert
        assertFalse("Texto plano no debe detectarse como cifrado",
                CryptoUtil.isEncrypted(plainText));
        assertTrue("Texto cifrado debe detectarse como cifrado",
                CryptoUtil.isEncrypted(encryptedText));
        assertFalse("String vacío no debe detectarse como cifrado",
                CryptoUtil.isEncrypted(""));
        assertFalse("Null no debe detectarse como cifrado",
                CryptoUtil.isEncrypted(null));
    }

    @Test
    public void testEncryption_Deterministic() {
        // El mismo mensaje con el mismo chatId debería producir resultados diferentes
        // debido al IV aleatorio (esto es una característica de seguridad)

        // Arrange
        String message = "Test message";
        String chatId = "chat123";

        // Act
        String encrypted1 = CryptoUtil.encrypt(message, chatId);
        String encrypted2 = CryptoUtil.encrypt(message, chatId);

        // Assert
        assertNotEquals("Dos cifrados del mismo mensaje deben ser diferentes (IV aleatorio)",
                encrypted1, encrypted2);

        // But both should decrypt to the same original message
        assertEquals(message, CryptoUtil.decrypt(encrypted1, chatId));
        assertEquals(message, CryptoUtil.decrypt(encrypted2, chatId));
    }

    @Test
    public void testEncryptDecrypt_Unicode() {
        // Arrange
        String originalMessage = "Hello مرحبا 你好 Привет 🌍";
        String chatId = "chat_unicode";

        // Act
        String encrypted = CryptoUtil.encrypt(originalMessage, chatId);
        String decrypted = CryptoUtil.decrypt(encrypted, chatId);

        // Assert
        assertEquals("Debe manejar correctamente Unicode y emojis", originalMessage, decrypted);
    }
}

