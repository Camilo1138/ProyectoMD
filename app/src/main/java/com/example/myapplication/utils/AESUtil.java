package com.example.myapplication.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    static final int GCM_TAG_LENGTH = 128;

    public static String encrypt(String plaintext, String password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        SecretKey secretKey = generateKey(password, salt);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, salt);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[salt.length + encryptedBytes.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(encryptedBytes, 0, combined, salt.length, encryptedBytes.length);

        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    public static String decrypt(String encryptedText, String password) throws Exception {
        byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);
        byte[] salt = new byte[16];
        byte[] encryptedBytes = new byte[combined.length - salt.length];

        System.arraycopy(combined, 0, salt, 0, salt.length);
        System.arraycopy(combined, salt.length, encryptedBytes, 0, encryptedBytes.length);

        SecretKey secretKey = generateKey(password, salt);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, salt);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private static SecretKey generateKey(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }
}