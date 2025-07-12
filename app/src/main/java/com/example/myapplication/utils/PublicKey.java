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
    public static PublicKey fromString(String keyString) throws IllegalArgumentException {
        if (keyString == null || keyString.isEmpty()) {
            throw new IllegalArgumentException("El string de la clave pública no puede ser nulo o vacío");
        }

        String[] parts = keyString.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato de clave pública inválido. Se esperaba 'e|n'");
        }

        try {
            BigInteger e = new BigInteger(parts[0]);
            BigInteger n = new BigInteger(parts[1]);
            return new PublicKey(e, n);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Los componentes de la clave pública deben ser números válidos", ex);
        }
    }
    public String toStringRepresentation() {
        return e.toString() + "|" + n.toString();
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
