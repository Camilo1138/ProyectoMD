package com.example.myapplication.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.MyKeyPair;
import com.example.myapplication.utils.RSAUtils;

import java.math.BigInteger;
import java.security.KeyPair;

public class LoginActivity extends AppCompatActivity {
    private EditText etName, etEmail, etP, etQ;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etP = findViewById(R.id.etP);
        etQ = findViewById(R.id.etQ);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            BigInteger p = new BigInteger(etP.getText().toString());
            BigInteger q = new BigInteger(etQ.getText().toString());

            MyKeyPair keys = RSAUtils.generateKeys(p, q);
            User user = new User(
                    etName.getText().toString(),
                    etEmail.getText().toString(),
                    keys.getPublicKey(),
                    keys.getPrivateKey(),
            );

            // Guardar usuario en Firebase o SQLite
            saveUserToFirebase(user);
        });
    }
    // Obtener clave pública desde KeyPair


}