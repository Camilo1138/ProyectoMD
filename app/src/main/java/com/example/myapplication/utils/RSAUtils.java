package com.example.myapplication.utils;
import android.annotation.SuppressLint;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RSAUtils {

    // Generar claves RSA
    public static KeyPair generateKeys(BigInteger p, BigInteger q) {
        if (p.equals(q)) {throw new IllegalArgumentException("p y q no deben ser iguales");}
        if(!esPrimo(p)) {p =  primoMasCercano(p);}
        if(!esPrimo(q)) {q =  primoMasCercano(q);}
        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = BigInteger.valueOf(65537); // Clave pública común
        BigInteger d = e.modInverse(phi); // Clave privada
        return new KeyPair(new PublicKey(e, n), new PrivateKey(d, n));
    }

    // Cifrar mensaje
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


    // Descifrar mensaje
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

}

// Clases para almacenar claves
class PublicKey implements java.security.PublicKey {
    private BigInteger e, n;
    // Getters y constructor
    public BigInteger getE() {return e;}
    public BigInteger getN() {return n;}

    public PublicKey(BigInteger e, BigInteger n) {
        this.e = e;
        this.n = n;
    }

    @Override
    public String getAlgorithm() {
        return "";
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }
}
class PrivateKey implements java.security.PrivateKey {
    private BigInteger d, n;
    // Getters y constructor

    public BigInteger getN() {
        return n;
    }

    public BigInteger getD() {
        return d;
    }


    public PrivateKey(BigInteger d, BigInteger n) {
        this.d = d;
        this.n = n;

    }

    @Override
    public String getAlgorithm() {
        return "";
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }
}