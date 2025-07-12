package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import com.example.myapplication.models.MyApplication;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class SecureStorage {

    private static PrivateKey currentPrivateKey;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void savePrivateKey(PrivateKey privateKey) {
        try {
            // Para Android M+ usamos AndroidKeyStore
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            // Generar una clave AES para encriptar la clave RSA
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    "temp_aes_key",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());

            SecretKey aesKey = keyGenerator.generateKey();

            // Encriptar la clave privada RSA
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encryptedPrivateKey = cipher.doFinal(privateKey.getEncoded());

            // Guardar en SharedPreferences (solo para ejemplo, considera una solución más segura)
            SharedPreferences prefs = getAppContext().getSharedPreferences("secure_prefs", Context.MODE_PRIVATE);
            prefs.edit()
                    .putString("encrypted_private_key", Base64.encodeToString(encryptedPrivateKey, Base64.DEFAULT))
                    .putString("encryption_iv", Base64.encodeToString(cipher.getIV(), Base64.DEFAULT))
                    .apply();

        } catch (Exception e) {
            // Fallback: guardar en memoria (menos seguro)
            currentPrivateKey = privateKey;
        }
    }

    public static PrivateKey getPrivateKey() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Recuperar del AndroidKeyStore
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);

                SecretKey aesKey = (SecretKey) keyStore.getKey("temp_aes_key", null);

                SharedPreferences prefs = getAppContext().getSharedPreferences("secure_prefs", Context.MODE_PRIVATE);
                String encryptedKey = prefs.getString("encrypted_private_key", null);
                String ivStr = prefs.getString("encryption_iv", null);

                if (encryptedKey != null && ivStr != null) {
                    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                    GCMParameterSpec spec = new GCMParameterSpec(128, Base64.decode(ivStr, Base64.DEFAULT));
                    cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);

                    byte[] decryptedKey = cipher.doFinal(Base64.decode(encryptedKey, Base64.DEFAULT));
                    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedKey);
                    return (PrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
                }
            }

            // Fallback: obtener de memoria
            return currentPrivateKey;
        } catch (Exception e) {
            return null;
        }
    }

    private static Context getAppContext() {
        return MyApplication.getInstance().getApplicationContext();
    }


}