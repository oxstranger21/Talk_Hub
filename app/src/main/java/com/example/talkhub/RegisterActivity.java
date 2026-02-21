package com.example.talkhub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etUsername;
    Button btnRegister;
    FirebaseAuth auth;
    AlertDialog loadingDialog;
    TextView tvLogin;
    DatabaseReference databaseReference;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        LayoutInflater inflater = getLayoutInflater();
        loadingDialog = new AlertDialog.Builder(this)
                .setView(inflater.inflate(R.layout.dialog_loading, null))
                .setCancelable(false)
                .create();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        btnRegister.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String username = etUsername.getText().toString().trim().toLowerCase();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show();

            // 🔥 Check username unique
            DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("usernames");

            usernameRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    runOnUiThread(() -> {
                        if (snapshot.exists()) {
                            loadingDialog.dismiss();
                            Toast.makeText(RegisterActivity.this,
                                    "Username already taken",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Username available → create account
                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            FirebaseUser firebaseUser = auth.getCurrentUser();
                                            if (firebaseUser != null) {
                                                String userId = firebaseUser.getUid();

                                                // Save user data
                                                databaseReference.child(userId).child("name").setValue(name);
                                                databaseReference.child(userId).child("email").setValue(email);
                                                databaseReference.child(userId).child("username").setValue(username);

                                                // Save username reference
                                                usernameRef.child(username).setValue(userId);

                                                // Send verification email
                                                firebaseUser.sendEmailVerification()
                                                        .addOnCompleteListener(verificationTask -> {
                                                            loadingDialog.dismiss();
                                                            if (verificationTask.isSuccessful()) {
                                                                Toast.makeText(RegisterActivity.this,
                                                                        "Verification email sent. Please verify before login.",
                                                                        Toast.LENGTH_LONG).show();

                                                                auth.signOut();

                                                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                                finish();

                                                            } else {
                                                                Toast.makeText(RegisterActivity.this,
                                                                        "Failed to send verification email",
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                // This case should not happen if task is successful, but as a fallback
                                                loadingDialog.dismiss();
                                                Toast.makeText(RegisterActivity.this,
                                                        "Registration Failed: User is null",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            loadingDialog.dismiss();
                                            Toast.makeText(RegisterActivity.this,
                                                    "Registration Failed",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,
                                "Database Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}
