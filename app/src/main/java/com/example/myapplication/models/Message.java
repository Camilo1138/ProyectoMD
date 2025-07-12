package com.example.myapplication.models;

import java.math.BigInteger;
import java.util.Date;

public class Message {
    // Atributos básicos
    private String id;              // ID único del mensaje (opcional, útil para Firebase)
    private String senderId;        // ID del usuario que envía
    private String receiverId;      // ID del usuario que recibe
    private String cipherText;      // Mensaje cifrado (en formato String o BigInteger)
    private Date timestamp;         // Fecha y hora de envío
    private boolean isDecrypted;    // ¿Está descifrado? (para UI)
    private String plainText;       // Mensaje descifrado (opcional, si se guarda localmente)

    // Constructor para mensaje cifrado
    public Message(String senderId, String receiverId, String cipherText) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.cipherText = cipherText;
        this.timestamp = new Date(); // Fecha actual al crear el mensaje
        this.isDecrypted = false;
    }

    // Getters y Setters (necesarios para Firebase)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getCipherText() { return cipherText; }
    public void setCipherText(String cipherText) { this.cipherText = cipherText; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public boolean isDecrypted() { return isDecrypted; }
    public void setDecrypted(boolean decrypted) { isDecrypted = decrypted; }

    public String getPlainText() { return plainText; }
    public void setPlainText(String plainText) { this.plainText = plainText; }


}