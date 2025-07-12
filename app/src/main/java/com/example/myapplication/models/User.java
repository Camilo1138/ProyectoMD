package com.example.myapplication.models;

import com.example.myapplication.utils.PrivateKey;
import com.example.myapplication.utils.PublicKey;

import java.math.BigInteger;

public class User {
    // Atributos básicos
    private String id;                  // ID único (Firebase o generado)
    private String name;                // Nombre del usuario
    private String email;               // Email (opcional, para registro)
    private String publicKey;        // Clave pública (e, n)
    private String privateKey;      // Clave privada (d, n)
    private boolean isHacked = false;
    private String bigInteger1;
    private String bigInteger2;// ¿Fue hackeado? (para el modo ataque)


    // Constructor para registro


    public User() {
    }

    public User(String id, String name, String email, String bigInteger1,
                String bigInteger2, String publicKey, String privateKey) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.bigInteger1 = bigInteger1;
        this.bigInteger2 = bigInteger2;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /*public User(String name, String email, PublicKey publicKey, PrivateKey privateKey) {
        this.name = name;
        this.email = email;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
*/
    // Getters y Setters (necesarios para Firebase)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

    public boolean isHacked() { return isHacked; }
    public void setHacked(boolean hacked) { isHacked = hacked; }
/*
    // Método para serializar claves (útil para Firebase)
    public String getPublicKeyAsString() {
        return publicKey.getE() + "," + publicKey.getN(); // Formato: "e,n"
    }


 */

    // Método para deserializar claves (desde Firebase)
    public static PublicKey parsePublicKey(String keyString) {
        String[] parts = keyString.split(",");
        BigInteger e = new BigInteger(parts[0]);
        BigInteger n = new BigInteger(parts[1]);
        return new PublicKey(e, n);
    }
}