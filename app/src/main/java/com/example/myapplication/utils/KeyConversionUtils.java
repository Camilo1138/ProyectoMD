package com.example.myapplication.utils;

// KeyConversionUtils.java
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

public class KeyConversionUtils {
    public static PublicKey toJavaKey(com.example.myapplication.utils.PublicKey customKey) throws Exception {
        RSAPublicKeySpec spec = new RSAPublicKeySpec(customKey.getN(), customKey.getE());
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }
}
