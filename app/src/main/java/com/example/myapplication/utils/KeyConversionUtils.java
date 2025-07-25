package com.example.myapplication.utils;

// KeyConversionUtils.java
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.interfaces.RSAPrivateKey;
public class KeyConversionUtils {
    public static PublicKey toJavaKey(com.example.myapplication.utils.PublicKey customKey) throws Exception {
        RSAPublicKeySpec spec = new RSAPublicKeySpec(customKey.getN(), customKey.getE());
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    public static com.example.myapplication.utils.PrivateKey fromJavaKey(java.security.PrivateKey javaKey) throws Exception {
        if (!(javaKey instanceof RSAPrivateKey)) {
            throw new IllegalArgumentException("Solo se admite RSAPrivateKey");
        }

        RSAPrivateKey rsaKey = (RSAPrivateKey) javaKey;
        BigInteger d = rsaKey.getPrivateExponent();
        BigInteger n = rsaKey.getModulus();
        return new com.example.myapplication.utils.PrivateKey(d, n);
    }
    public static java.security.PrivateKey toJavaPrivateKey(com.example.myapplication.utils.PrivateKey customKey) throws Exception {
        // Crear la especificación de la clave privada RSA
        RSAPrivateKeySpec spec = new RSAPrivateKeySpec(customKey.getN(), customKey.getD());

        // Obtener la fábrica de claves para RSA
        KeyFactory factory = KeyFactory.getInstance("RSA");

        // Generar y retornar la clave privada
        return factory.generatePrivate(spec);
    }

}
