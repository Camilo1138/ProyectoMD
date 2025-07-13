package com.example.myapplication.utils;



import java.security.*;
import java.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HybridEncryptionUtils {

    // AES (128 bits) IV length
    private static final int IV_LENGTH = 16;

    public static class EncryptedMessage {
        public String encryptedAESKey; // cifrada con RSA
        public String encryptedMessage; // cifrada con AES
        public String iv; // usada en AES

        public EncryptedMessage(String encryptedAESKey, String encryptedMessage, String iv) {
            this.encryptedAESKey = encryptedAESKey;
            this.encryptedMessage = encryptedMessage;
            this.iv = iv;
        }
    }

    public static EncryptedMessage encrypt(String message, PublicKey rsaPublicKey) throws Exception {
        // 1. Generar clave AES
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey aesKey = keyGen.generateKey();

        // 2. Generar IV aleatorio
        byte[] ivBytes = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        // 3. Cifrar el mensaje con AES
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, iv);
        byte[] encryptedMessageBytes = aesCipher.doFinal(message.getBytes("UTF-8"));

        // 4. Cifrar la clave AES con RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        byte[] encryptedKeyBytes = rsaCipher.doFinal(aesKey.getEncoded());

        return new EncryptedMessage(
                Base64.getEncoder().encodeToString(encryptedKeyBytes),
                Base64.getEncoder().encodeToString(encryptedMessageBytes),
                Base64.getEncoder().encodeToString(ivBytes)
        );
    }

    public static String decrypt(EncryptedMessage encryptedMessage, PrivateKey rsaPrivateKey) throws Exception {
        // 1. Descifrar la clave AES con RSA
        byte[] encryptedKeyBytes = Base64.getDecoder().decode(encryptedMessage.encryptedAESKey);
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedKeyBytes);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // 2. Preparar IV
        byte[] ivBytes = Base64.getDecoder().decode(encryptedMessage.iv);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        // 3. Descifrar mensaje AES
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, iv);
        byte[] encryptedMessageBytes = Base64.getDecoder().decode(encryptedMessage.encryptedMessage);
        byte[] decryptedBytes = aesCipher.doFinal(encryptedMessageBytes);

        return new String(decryptedBytes, "UTF-8");
    }
}
