package com.example.myapplication.utils;

public class SecureStorageException extends Exception {
    public SecureStorageException(String message) {
        super(message);
    }

    public SecureStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}