package com.example.myapplication.utils;

import java.math.BigInteger;
import java.security.KeyFactory;

public class PrivateKey implements java.security.PrivateKey {
    private BigInteger d, n;
    // Getters y constructor

    public BigInteger getN() {
        return n;
    }

    public BigInteger getD() {
        return d;
    }

    public PrivateKey() {
    }

    public PrivateKey(BigInteger d, BigInteger n) {
        this.d = d;
        this.n = n;

    }

    // Métodos para Firebase (guardamos como String)
    public String getDString() {
        return d.toString();
    }

    public String getNString() {
        return n.toString();
    }

    public void setDString(String eStr) {
        this.d = new BigInteger(eStr);
    }

    public void setNString(String nStr) {
        this.n = new BigInteger(nStr);
    }

    @Override
    public String getAlgorithm() {
        return "RSA";
    }

    @Override
    public String getFormat() {
        return "PKCS#8";
    }

    /*@Override
    public byte[] getEncoded() {
        return new byte[0];
    }

     */
    @Override
    public byte[] getEncoded() {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            java.security.spec.RSAPrivateKeySpec spec = new java.security.spec.RSAPrivateKeySpec(n, d);
            return factory.generatePrivate(spec).getEncoded();
        } catch (Exception ex) {
            return null;
        }
    }

}
