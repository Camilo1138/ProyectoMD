package com.example.myapplication.utils;
import android.annotation.SuppressLint;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }
    public static String decrypt(String encryptedText, PrivateKey privateKey) throws Exception {
        byte[] encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
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



}

