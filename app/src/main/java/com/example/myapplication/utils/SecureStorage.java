package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.RequiresApi;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.myapplication.models.MyApplication;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SecureStorage {

    private static final String AES_KEY_ALIAS = "rsa_aes_key";
    private static final String PREF_NAME = "secure_prefs";
    private static final String ENCRYPTED_KEY = "encrypted_private_key";
    private static final String ENCRYPTION_IV = "encryption_iv";
    private static final Logger logger = Logger.getLogger("SecureStorage");

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void savePrivateKey(PrivateKey privateKey) {
        try {
            Context context = getAppContext();
            SecretKey aesKey = getOrCreateAESKey();

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encryptedPrivateKey = cipher.doFinal(privateKey.getEncoded());

            SharedPreferences prefs = getEncryptedPrefs(context);
            prefs.edit()
                    .putString(ENCRYPTED_KEY, Base64.encodeToString(encryptedPrivateKey, Base64.DEFAULT))
                    .putString(ENCRYPTION_IV, Base64.encodeToString(cipher.getIV(), Base64.DEFAULT))
                    .apply();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar clave privada: " + e.getMessage(), e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static PrivateKey getPrivateKey() {
        try {
            Context context = getAppContext();
            SecretKey aesKey = getAESKey();

            if (aesKey == null) {
                logger.warning("Clave AES no disponible.");
                return null;
            }

            SharedPreferences prefs = getEncryptedPrefs(context);
            String encryptedKey = prefs.getString(ENCRYPTED_KEY, null);
            String ivStr = prefs.getString(ENCRYPTION_IV, null);

            if (encryptedKey != null && ivStr != null) {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec spec = new GCMParameterSpec(128, Base64.decode(ivStr, Base64.DEFAULT));
                cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);

                byte[] decryptedKey = cipher.doFinal(Base64.decode(encryptedKey, Base64.DEFAULT));
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedKey);
                return (PrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al recuperar clave privada: " + e.getMessage(), e);
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static SecretKey getOrCreateAESKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(AES_KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    AES_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build());

            keyGenerator.generateKey();
        }

        return (SecretKey) keyStore.getKey(AES_KEY_ALIAS, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static SecretKey getAESKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return (SecretKey) keyStore.getKey(AES_KEY_ALIAS, null);
    }

    private static SharedPreferences getEncryptedPrefs(Context context) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    private static Context getAppContext() {
        return MyApplication.getInstance().getApplicationContext();
    }
}
