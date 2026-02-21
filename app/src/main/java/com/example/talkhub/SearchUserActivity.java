package com.example.talkhub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class SearchUserActivity extends AppCompatActivity {

    EditText etSearchUsername;
    Button btnSearchUser, btnChatWithUser;

    DatabaseReference reference;
    String foundUserId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        etSearchUsername = findViewById(R.id.etSearchUsername);
        btnSearchUser = findViewById(R.id.btnSearchUser);
        btnChatWithUser = findViewById(R.id.btnChatWithUser);

        reference = FirebaseDatabase.getInstance().getReference("Users");

        btnSearchUser.setOnClickListener(v -> searchUser());
        btnChatWithUser.setOnClickListener(v -> openChat());
    }

    private void searchUser() {

        String username = etSearchUsername.getText().toString().trim();

        reference.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            for (DataSnapshot ds : snapshot.getChildren()) {

                                foundUserId = ds.getKey(); // keep this for chat opening

                                String username = ds.child("username").getValue(String.class);

                                showCustomToast("Username: " + username, true);

                                btnChatWithUser.setVisibility(View.VISIBLE);
                            }

                        } else {

                            showCustomToast("User not found", false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void openChat() {

        String currentUserId = FirebaseAuth.getInstance().getUid();

        assert currentUserId != null;
        DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("Chats")
                .child(currentUserId)
                .child(foundUserId);

        chatRef.child("userId").setValue(foundUserId);
        chatRef.child("lastMessage").setValue("Tap to chat");
        chatRef.child("timestamp").setValue(System.currentTimeMillis());

        Intent intent = new Intent(SearchUserActivity.this, ChatActivity.class);
        intent.putExtra("receiverId", foundUserId);
        startActivity(intent);
    }

    private void showCustomToast(String message, boolean isSuccess) {

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView tvMessage = layout.findViewById(R.id.tvToastMessage);
        LinearLayout container = layout.findViewById(R.id.toastContainer);

        tvMessage.setText(message);

        if (isSuccess) {
            container.setBackgroundResource(R.drawable.toast_success_bg);
        } else {
            container.setBackgroundResource(R.drawable.toast_error_bg);
        }

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        // Show at TOP
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 150);

        toast.show();
    }
}
