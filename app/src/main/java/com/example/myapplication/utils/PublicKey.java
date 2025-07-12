package com.example.myapplication.utils;

import java.math.BigInteger;

// Clases para almacenar claves
public class PublicKey implements java.security.PublicKey {
    private BigInteger e, n;

    // Getters y constructor
    public BigInteger getE() {
        return e;
    }

    public BigInteger getN() {
        return n;
    }

    public PublicKey() {
    }

    public PublicKey(BigInteger e, BigInteger n) {
        this.e = e;
        this.n = n;
    }

    // Métodos para Firebase (guardamos como String)
    public String getEString() {
        return e.toString();
    }

    public String getNString() {
        return n.toString();
    }

    public void setEString(String eStr) {
        this.e = new BigInteger(eStr);
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
        return "X.509";
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }
}
