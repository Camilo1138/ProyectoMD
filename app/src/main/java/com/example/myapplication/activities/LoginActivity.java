package com.example.myapplication.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.myapplication.utils.SecureStorage;

import com.example.myapplication.R;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.AESUtil;
import com.example.myapplication.utils.MyKeyPair;
import com.example.myapplication.utils.RSAUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigInteger;
import java.security.KeyPair;

public class LoginActivity extends AppCompatActivity {
    private EditText etName, etPassword,etEmail, etP, etQ;
    private Button btnRegister;
    // En tu clase Application o Activity principal
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etP = findViewById(R.id.etP);
        etQ = findViewById(R.id.etQ);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            // Validar campos
            String password = etPassword.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String bigInt1Str = etP.getText().toString().trim();
            String bigInt2Str = etQ.getText().toString().trim();
            BigInteger p = new BigInteger(etP.getText().toString());
            BigInteger q = new BigInteger(etQ.getText().toString());

            if (!RSAUtils.esPrimo(p)) {
                Toast.makeText(this, "p no es primo, se cambiará por el primo más cercano", Toast.LENGTH_SHORT).show();
                p = RSAUtils.primoMasCercano(p);
            }

            if (!RSAUtils.esPrimo(q)) {
                Toast.makeText(this, "q no es primo, se cambiará por el primo más cercano", Toast.LENGTH_SHORT).show();
                q = RSAUtils.primoMasCercano(q);
            }

            if (p.equals(q)) {
                Toast.makeText(this, "p y q no deben ser iguales", Toast.LENGTH_SHORT).show();
                return;
            }

// Convertir a String
            bigInt1Str = p.toString();
            bigInt2Str = q.toString();

            if (name.isEmpty() || email.isEmpty() ||
                    bigInt1Str.isEmpty() || bigInt2Str.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Convertir Strings a BigIntegers
                BigInteger bigInt1 = new BigInteger(bigInt1Str);
                BigInteger bigInt2 = new BigInteger(bigInt2Str);
                String encryptionKey = name+password;
                // Registrar usuario
                registrarUsuario(name, email, password, bigInt1, bigInt2, encryptionKey);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Los valores numéricos no son válidos", Toast.LENGTH_SHORT).show();
            }
        });


    }

    boolean userregister=false;
    public void registrarUsuario(String nombre, String email, String password,
                                 BigInteger bigInteger1, BigInteger bigInteger2,
                                 String claveEncriptacion) {

        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Registrando usuario...");
        progress.setCancelable(false);
        progress.show();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Registro", "Usuario autenticado en FirebaseAuth");

                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        String userId = firebaseUser.getUid();

                        try {
                            MyKeyPair mykeyPair = RSAUtils.generateKeyPair(bigInteger1, bigInteger2);
                            //String publicKeyStr = Base64.encodeToString(mykeyPair.getPublicKey().getEncoded(), Base64.DEFAULT);
                            String publicKeyStr = mykeyPair.getPublicKey().toStringRepresentation();
                            String privateKeyStr = mykeyPair.getPrivateKey().getD().toString() + "|" + mykeyPair.getPrivateKey().getN().toString();
                            String privateKeyEncrypted = AESUtil.encrypt(privateKeyStr, claveEncriptacion);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                SecureStorage.savePrivateKey(mykeyPair.getPrivateKey());
                            }
                            User usuario = new User(
                                    userId, nombre, email,
                                    bigInteger1.toString(), bigInteger2.toString(),
                                    publicKeyStr, privateKeyEncrypted
                            );

                            db.collection("usuarios").document(userId)
                                    .set(usuario)
                                    .addOnSuccessListener(aVoid -> {
                                        progress.dismiss();  // ✅ Cerrar al completar
                                        Log.d("Registro", "Usuario guardado en Firestore");
                                        startActivity(new Intent(LoginActivity.this, ChatListActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progress.dismiss();
                                        Log.e("Registro", "Error al guardar en Firestore", e);
                                        Toast.makeText(this, "Error guardando usuario", Toast.LENGTH_SHORT).show();
                                        firebaseUser.delete();
                                    });

                        } catch (Exception e) {
                            progress.dismiss();
                            Log.e("Registro", "Error al generar claves RSA", e);
                            Toast.makeText(this, "Error generando claves", Toast.LENGTH_SHORT).show();
                            firebaseUser.delete();
                        }

                    } else {
                        progress.dismiss();
                        Log.e("Registro", "Error FirebaseAuth", task.getException());
                        Toast.makeText(this, "Error creando usuario: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }




}