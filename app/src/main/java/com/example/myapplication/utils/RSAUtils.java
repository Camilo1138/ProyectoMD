package com.example.myapplication.utils;
import android.annotation.SuppressLint;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

public class RSAUtils {

    // Generar claves RSA
    public static MyKeyPair generateKeyPair(BigInteger p, BigInteger q) throws IllegalArgumentException {
        if (p.equals(q)) {
            throw new IllegalArgumentException("p y q no deben ser iguales");
        }
        if(!esPrimo(p)) { p = primoMasCercano(p); }
        if(!esPrimo(q)) { q = primoMasCercano(q); }

        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = BigInteger.valueOf(65537);
        BigInteger d = e.modInverse(phi);

        return new MyKeyPair(new PublicKey(e, n), new PrivateKey(d, n));
    }
    // Cifrar mensaje
    /*
    public static List<BigInteger> encryptLong(String message, PublicKey publicKey) {
        byte[] bytes = message.getBytes();
        int blockSize = (publicKey.getN().bitLength() - 1) / 8; // Tamaño seguro en bytes

        List<BigInteger> encryptedBlocks = new ArrayList<>();

        for (int i = 0; i < bytes.length; i += blockSize) {
            int length = Math.min(blockSize, bytes.length - i);
            byte[] block = Arrays.copyOfRange(bytes, i, i + length);
            BigInteger blockInt = new BigInteger(1, block); // evita signo negativo
            encryptedBlocks.add(blockInt.modPow(publicKey.getE(), publicKey.getN()));
        }

        return encryptedBlocks;
    }
*/
    /*
    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

     */
    /*
    public static String encrypt(String message, java.security.PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    public static String decrypt(String encryptedText, PrivateKey privateKey) throws Exception {
        byte[] encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
    */

    // Descifrar mensaje
    /*
    public static String decryptLong(List<BigInteger> encryptedBlocks, PrivateKey privateKey) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (BigInteger block : encryptedBlocks) {
            BigInteger decrypted = block.modPow(privateKey.getD(), privateKey.getN());
            byte[] blockBytes = decrypted.toByteArray();

            // Quitar posible byte extra de padding por signo
            if (blockBytes[0] == 0) {
                blockBytes = Arrays.copyOfRange(blockBytes, 1, blockBytes.length);
            }

            baos.write(blockBytes, 0, blockBytes.length);
        }

        return new String(baos.toByteArray());
    }

     */
    public static String encrypt(String message, java.security.PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        int maxBlockSize = 245; // Para RSA-2048 con PKCS1Padding
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (int i = 0; i < messageBytes.length; i += maxBlockSize) {
            int end = Math.min(messageBytes.length, i + maxBlockSize);
            byte[] block = cipher.doFinal(messageBytes, i, end - i);
            outputStream.write(block);
        }

        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
    }

    public static String decrypt(String encryptedText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedBytes = Base64.decode(encryptedText, Base64.NO_WRAP);
        int blockSize = 256; // Tamaño de bloque cifrado RSA-2048
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (int i = 0; i < encryptedBytes.length; i += blockSize) {
            int end = Math.min(encryptedBytes.length, i + blockSize);
            byte[] block = cipher.doFinal(encryptedBytes, i, end - i);
            outputStream.write(block);
        }

        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }
    @SuppressLint("NewApi")
    public static boolean esPrimo(BigInteger n) {
        if (n.compareTo(BigInteger.TWO) < 0) {
            return false;
        }
        return n.isProbablePrime(15);
    }


    @SuppressLint("NewApi")
    public static BigInteger primoMasCercano(BigInteger x) {
        if (x.isProbablePrime(15)) {
            return x;
        }
        // Buscar el siguiente primo hacia arriba
        BigInteger arriba = x.nextProbablePrime();

        // Buscar el primo hacia abajo (manual)
        BigInteger abajo = x.subtract(BigInteger.ONE);
        while (abajo.compareTo(BigInteger.TWO) >= 0 && !abajo.isProbablePrime(15)) {
            abajo = abajo.subtract(BigInteger.ONE);
        }

        BigInteger distanciaArriba = arriba.subtract(x);
        BigInteger distanciaAbajo = x.subtract(abajo);

        if (abajo.compareTo(BigInteger.TWO) < 0 || distanciaArriba.compareTo(distanciaAbajo) < 0) {
            return arriba;
        } else {
            return abajo;
        }
    }

    // Convierte una lista de BigInteger a una lista de Strings (para guardar en Firebase)
    public static List<String> bigIntListToStringList(List<BigInteger> list) {
        List<String> result = new ArrayList<>();
        for (BigInteger b : list) {
            result.add(b.toString());
        }
        return result;
    }

    // Convierte una lista de Strings a una lista de BigInteger (al leer de Firebase)
    public static List<BigInteger> stringListToBigIntList(List<String> list) {
        List<BigInteger> result = new ArrayList<>();
        for (String s : list) {
            result.add(new BigInteger(s));
        }
        return result;
    }

    // En RSAUtils
    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    public static String encryptAESKey(byte[] aesKey, com.example.myapplication.utils.PublicKey customPublicKey) throws Exception {
        java.security.PublicKey publicKey = convertToJavaPublicKey(customPublicKey);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(aesKey);
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    public static byte[] decryptAESKey(String encryptedAESKeyBase64, com.example.myapplication.utils.PrivateKey customPrivateKey) throws Exception {
        java.security.PrivateKey privateKey = convertToJavaPrivateKey(customPrivateKey);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encryptedBytes = Base64.decode(encryptedAESKeyBase64, Base64.DEFAULT);
        return cipher.doFinal(encryptedBytes);
    }
    public static java.security.PrivateKey convertToJavaPrivateKey(com.example.myapplication.utils.PrivateKey customPrivateKey) throws Exception {
        BigInteger modulus = customPrivateKey.getN();
        BigInteger privateExponent = customPrivateKey.getD();
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, privateExponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public static java.security.PublicKey convertToJavaPublicKey(com.example.myapplication.utils.PublicKey customPublicKey) throws Exception {
        BigInteger modulus = customPublicKey.getN();
        BigInteger exponent = customPublicKey.getE();
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

}

