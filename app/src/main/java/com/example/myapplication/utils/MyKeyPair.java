package com.example.myapplication.utils;

public class MyKeyPair {
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public MyKeyPair(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

}
