package com.example.myapplication.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

    public class AESUtils {

        // Constantes de configuración
        private static final String AES_ALGORITHM = "AES";
        static final String AES_MODE = "AES/GCM/NoPadding";
        private static final int AES_KEY_SIZE = 128; // bits
        private static final int IV_SIZE = 12; // 96 bits (recomendado para GCM)
        private static final int TAG_LENGTH_BIT = 128; // Tamaño del tag de autenticación

        /**
         * Genera una nueva clave AES segura
         */
        public static SecretKey generateKey() throws Exception {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGen.init(AES_KEY_SIZE);
            return keyGen.generateKey();
        }

        /**
         * Cifra texto con AES-GCM (genera IV automáticamente)
         * @return String Base64(IV + texto cifrado)
         */
        public static String encrypt(String plainText, SecretKey key) throws Exception {
            byte[] iv = generateIV();
            return encryptWithIV(plainText, key, iv);
        }

        /**
         * Cifra texto con AES-GCM usando un IV específico
         * @return String Base64(IV + texto cifrado)
         */
        public static String encryptWithIV(String plainText, SecretKey key, byte[] iv) throws Exception {
            Cipher cipher = Cipher.getInstance(AES_MODE);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combinar IV + texto cifrado
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.encodeToString(combined, Base64.NO_WRAP);
        }

        /**
         * Descifra texto cifrado con AES-GCM
         * @param encryptedData Base64(IV + texto cifrado)
         */
        public static String decrypt(String encryptedData, SecretKey key) throws Exception {
            byte[] decoded = Base64.decode(encryptedData, Base64.NO_WRAP);

            // Extraer IV (primeros IV_SIZE bytes)
            byte[] iv = Arrays.copyOfRange(decoded, 0, IV_SIZE);
            byte[] cipherText = Arrays.copyOfRange(decoded, IV_SIZE, decoded.length);

            Cipher cipher = Cipher.getInstance(AES_MODE);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        }

        /**
         * Genera un IV aleatorio seguro
         */
        public static byte[] generateIV() {
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);
            return iv;
        }

        /**
         * Extrae el IV de un mensaje cifrado
         * @param encryptedData Base64(IV + texto cifrado)
         */
        public static byte[] getIVFromEncryptedData(String encryptedData) {
            byte[] decoded = Base64.decode(encryptedData, Base64.NO_WRAP);
            return Arrays.copyOfRange(decoded, 0, IV_SIZE);
        }

        /**
         * Serializa una clave AES a Base64
         */
        /*
        public static String encodeKey(SecretKey key) {
            return Base64.encodeToString(key.getEncoded(), Base64.NO_WRAP);
        }
    */
        /**
         * Deserializa una clave AES desde Base64
         */
        public static SecretKey decodeKey(String base64Key) {
            byte[] decodedKey = Base64.decode(base64Key, Base64.NO_WRAP);
            return new SecretKeySpec(decodedKey, AES_ALGORITHM);
        }

        /**
         * Limpia de forma segura los datos sensibles en memoria
         */
        public static void secureClear(byte[] data) {
            if (data != null) {
                Arrays.fill(data, (byte) 0);
            }
        }
        // En AESUtils.java
        public static String encodeKey(SecretKey key) {
            return Base64.encodeToString(key.getEncoded(), Base64.NO_WRAP);
        }
    }