package com.example.myapplication.utils;

import java.math.BigInteger;

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
        return "X.509";
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }
}
