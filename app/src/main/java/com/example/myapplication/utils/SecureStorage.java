package com.example.myapplication.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.myapplication.models.MyApplication;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SecureStorage {

    private static final String AES_KEY_ALIAS = "rsa_aes_key";
    private static final String PREF_NAME = "secure_prefs";
    public static final String ENCRYPTED_KEY = "encrypted_private_key";
    private static final String ENCRYPTION_IV = "encryption_iv";
    private static final Logger logger = Logger.getLogger("SecureStorage");

    // Constantes para algoritmos criptográficos
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final String RSA_ALGORITHM = "RSA";

    /*
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void savePrivateKey(PrivateKey privateKey) {
       try {
            Context context = getAppContext();
            SecretKey aesKey = getOrCreateAESKey();

            Cipher cipher = Cipher.getInstance(AES_MODE);
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


     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static PrivateKey getPrivateKey(Context context) throws SecureStorageException {
        try {
            if (!initializeSecureStorage(context)) {
                throw new SecureStorageException("Almacenamiento seguro no inicializado");
            }

            SharedPreferences prefs = getEncryptedPrefs(context);
            String encryptedKey = prefs.getString(ENCRYPTED_KEY, null);
            String ivStr = prefs.getString(ENCRYPTION_IV, null);

            if (encryptedKey == null || ivStr == null) {
                return null;
            }

            SecretKey aesKey = getAESKey();
            Cipher cipher = Cipher.getInstance(AES_MODE);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, Base64.decode(ivStr, Base64.NO_WRAP));
            cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);

            byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedKey, Base64.NO_WRAP));
            String decrypted = new String(decryptedBytes, StandardCharsets.UTF_8);

            // Separar D y N
            String[] parts = decrypted.split("\\|");
            if (parts.length != 2) {
                throw new SecureStorageException("Formato inválido de clave privada almacenada");
            }

            BigInteger d = new BigInteger(parts[0]);
            BigInteger n = new BigInteger(parts[1]);

            return new PrivateKey(d, n);

        } catch (Exception e) {
            throw new SecureStorageException("Error al obtener clave privada", e);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void savePrivateKey(PrivateKey privateKey, Context context) throws SecureStorageException {
        try {
            if (!initializeSecureStorage(context)) {
                throw new SecureStorageException("Almacenamiento seguro no inicializado");
            }

            SecretKey aesKey = getAESKey();
            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            String keyString = privateKey.getD().toString() + "|" + privateKey.getN().toString();
            byte[] encrypted = cipher.doFinal(keyString.getBytes(StandardCharsets.UTF_8));

            getEncryptedPrefs(context).edit()
                    .putString(ENCRYPTED_KEY, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                    .putString(ENCRYPTION_IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
                    .apply();

        } catch (Exception e) {
            throw new SecureStorageException("Error al guardar clave privada", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static SecretKey getOrCreateAESKey() throws Exception {
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

    public static SharedPreferences getEncryptedPrefs(Context context) throws Exception {
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

    public static boolean hasAESKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return keyStore.containsAlias(AES_KEY_ALIAS);
        } catch (Exception e) {
            Log.e("SecureStorage", "Error checking AES key existence", e);
            return false;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static SecretKey getAESKey() {
        try {
            return getOrCreateAESKey(); // Reutiliza la lógica existente
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener clave AES", e);
            throw new RuntimeException("Failed to get AES key", e);
        }
    }
    /**
     * Verifica y prepara el almacenamiento seguro al iniciar la aplicación
     * @return true si las claves están disponibles, false si hay problemas
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean initializeSecureStorage(Context context) {
        try {
            if (!hasAESKey()) {
                logger.info("Generando nueva clave AES...");
                getAESKey(); // Esto generará una nueva clave si no existe

                // Verificar que realmente se creó
                if (!hasAESKey()) {
                    logger.severe("Fallo al verificar la clave AES recién generada");
                    return false;
                }
            }

            // Verificar que podemos acceder a las preferencias encriptadas
            getEncryptedPrefs(context);
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inicializando SecureStorage", e);
            return false;
        }
    }
    /**
     * Obtiene la clave privada de forma segura con manejo robusto de errores
     * @return PrivateKey la clave privada o null si no está disponible
     * @throws SecureStorageException si ocurre un error crítico de seguridad
     */
  /*  @RequiresApi(api = Build.VERSION_CODES.M)
    public static PrivateKey obtenerClavePrivadaSegura() throws SecureStorageException {
        try {
            // 1. Verificar inicialización del SecureStorage
            if (!initializeSecureStorage(getAppContext())) {
                logger.severe("SecureStorage no está inicializado correctamente");
                throw new SecureStorageException("Almacenamiento seguro no inicializado");
            }

            // 2. Obtener clave privada
            PrivateKey key = getPrivateKey(getApplicationContext());

            if (key == null) {
                logger.warning("Clave privada no encontrada en almacenamiento local");

                // 3. Intento de recuperación desde backup (opcional)
                key = recuperarClavePrivadaDesdeBackup();

                if (key == null) {
                    logger.warning("No se pudo recuperar la clave privada desde ninguna fuente");
                    return null;
                }

                // Guardar la clave recuperada para futuros accesos
                savePrivateKey(key);
            }

            return key;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error crítico al obtener clave privada", e);
            throw new SecureStorageException("Error al obtener clave privada", e);
        }
    }

   */

    // Método opcional para recuperación desde backup (Firestore, etc.)
    private static PrivateKey recuperarClavePrivadaDesdeBackup() {
        try {
            // Implementación específica para tu aplicación
            // Ejemplo: recuperar desde Firestore o servidor seguro
            logger.info("Intentando recuperar clave privada desde backup...");
            return null; // Cambiar por tu lógica real
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error al recuperar clave desde backup", e);
            return null;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static PrivateKey obtenerClavePrivadaSegura(Context context) throws SecureStorageException {
        try {
            // 1. Verificar inicialización del SecureStorage
            if (!SecureStorage.initializeSecureStorage(context)) {
                throw new SecureStorageException("Almacenamiento seguro no inicializado");
            }

            // 2. Intentar obtener la clave privada almacenada
            PrivateKey privateKey = SecureStorage.getPrivateKey(context);

            if (privateKey == null) {
                Log.w(TAG, "Clave privada no encontrada en almacenamiento local. Generando nuevo par...");

                // 3. Generar nuevo par de claves RSA
                MyKeyPair newKeyPair = RSAUtils.generateKeyPair(
                        new BigInteger(1024, new SecureRandom()),
                        new BigInteger(1024, new SecureRandom())
                );

                // 4. Guardar la nueva clave privada
                SecureStorage.savePrivateKey(newKeyPair.getPrivateKey(), context);

                privateKey = newKeyPair.getPrivateKey();

                Log.i(TAG, "Nuevo par de claves generado y almacenado");
            }



            return privateKey;

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error en parámetros para generación de claves", e);
            throw new SecureStorageException("Error en parámetros de claves", e);
        } catch (Exception e) {
            Log.e(TAG, "Error crítico al obtener clave privada", e);
            throw new SecureStorageException("Error al obtener clave privada", e);
        }
    }
}