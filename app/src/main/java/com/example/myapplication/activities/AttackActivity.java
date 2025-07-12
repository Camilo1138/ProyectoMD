package com.example.myapplication.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.utils.MyKeyPair;
import com.example.myapplication.utils.RSAUtils;
import java.math.BigInteger;
/*
public class AttackActivity extends AppCompatActivity {
    private EditText etTargetN;
    private Button btnHack;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attack);

        etTargetN = findViewById(R.id.etTargetN);
        btnHack = findViewById(R.id.btnHack);
        tvResult = findViewById(R.id.tvResult);

        btnHack.setOnClickListener(v -> {
            BigInteger n = new BigInteger(etTargetN.getText().toString());

            // Fuerza bruta para factorizar n (solo válido para primos pequeños)
            BigInteger[] factors = factorize(n);
            if (factors != null) {
                BigInteger p = factors[0], q = factors[1];
                MyKeyPair fakeKeys = RSAUtils.generateKeys(p, q);
                tvResult.setText("¡Clave privada hackeada: d = " + fakeKeys.getPrivateKey().getD() + "!");
            } else {
                tvResult.setText("¡Ataque fallido! Prueba con primos más pequeños.");
            }
        });
    }

    // Factorización simple (para primos pequeños)
    private BigInteger[] factorize(BigInteger n) {
        for (BigInteger i = BigInteger.valueOf(2); i.compareTo(BigInteger.valueOf(100)) <= 0; i = i.add(BigInteger.ONE)) {
            if (n.mod(i).equals(BigInteger.ZERO)) {
                return new BigInteger[]{i, n.divide(i)};
            }
        }
        return null;
    }
}

 */