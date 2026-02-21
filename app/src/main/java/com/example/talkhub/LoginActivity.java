package com.example.talkhub;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import android.app.Activity;


import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegister;
    FirebaseAuth auth;
    AlertDialog loadingDialog;
    TextView tvForgot;



    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgot = findViewById(R.id.tvForgot);

        LayoutInflater inflater = getLayoutInflater();
        loadingDialog = new AlertDialog.Builder(this)
                .setView(inflater.inflate(R.layout.dialog_loading, null))
                .setCancelable(false)
                .create();

        tvForgot.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Enter your email first");
                etEmail.requestFocus();
                return;
            }

            loadingDialog.show();

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        loadingDialog.dismiss();

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Reset email sent. Check your inbox.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Error: " + Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });


        btnLogin.setOnClickListener(v -> {
            loadingDialog.show();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        loadingDialog.dismiss();
                        if (task.isSuccessful()) {

                            FirebaseUser user = auth.getCurrentUser();

                            if (user != null) {

                                if (user.isEmailVerified()) {

                                    FirebaseMessaging.getInstance().getToken()
                                            .addOnSuccessListener(token -> {
                                                String uid = user.getUid();
                                                FirebaseDatabase.getInstance()
                                                        .getReference("Users")
                                                        .child(uid)
                                                        .child("fcmToken")
                                                        .setValue(token);
                                            });

                                    // ✅ Email verified → allow login
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();

                                } else {

                                    // Email not verified → resend verification link
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verificationTask -> {

                                                if (verificationTask.isSuccessful()) {

                                                    Toast.makeText(LoginActivity.this,
                                                            "Email not verified. Verification link sent again. Please check your inbox.",
                                                            Toast.LENGTH_LONG).show();

                                                } else {

                                                    Toast.makeText(LoginActivity.this,
                                                            "Failed to resend verification email.",
                                                            Toast.LENGTH_LONG).show();
                                                }

                                                auth.signOut(); // Force logout until verified
                                            });
                                }

                            }

                        } else {

                            Toast.makeText(LoginActivity.this,
                                    "Login Failed",
                                    Toast.LENGTH_SHORT).show();
                        }


                    });
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
