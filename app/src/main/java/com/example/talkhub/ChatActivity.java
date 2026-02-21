package com.example.talkhub;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.talkhub.adapters.MessagesAdapter;
import com.example.talkhub.models.ChatModel;
import com.example.talkhub.models.MessageModel;
import com.google.common.net.InternetDomainName;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    TextView tvChatUsername, tvOnlineStatus;
    ImageView btnSend, ivBack;
    EditText etMessage;
    RecyclerView rvMessages;

    List<MessageModel> messageList;
    MessagesAdapter adapter;

    DatabaseReference reference;

    String receiverId, currentUserId, chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        tvChatUsername = findViewById(R.id.tvChatUsername);
        tvOnlineStatus = findViewById(R.id.tvOnlineStatus);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        rvMessages = findViewById(R.id.rvMessages);
        ivBack = findViewById(R.id.ivBack);

        receiverId = getIntent().getStringExtra("receiverId");
        currentUserId = FirebaseAuth.getInstance().getUid();

        assert currentUserId != null;
        chatRoomId = getChatRoomId(currentUserId, receiverId);

        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        adapter = new MessagesAdapter(messageList);
        rvMessages.setAdapter(adapter);

        reference = FirebaseDatabase.getInstance().getReference("Messages").child(chatRoomId);

        loadMessages();
        loadUsername();

        btnSend.setOnClickListener(v -> sendMessage());

        ivBack.setOnClickListener(v -> finish());


    }

    private void sendMessage() {

        String message = etMessage.getText().toString().trim();

        if (message.isEmpty()) return;

        String messageId = reference.push().getKey();

        MessageModel model = new MessageModel(
                currentUserId,
                receiverId,
                message,
                System.currentTimeMillis()
        );

        assert messageId != null;
        reference.child(messageId).setValue(model);

        // 🔥 UPDATE CHAT LIST NODE FOR BOTH USERS
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        long time = System.currentTimeMillis();

        ChatModel senderChat = new ChatModel(receiverId, message, time);
        ChatModel receiverChat = new ChatModel(currentUserId, message, time);

        chatRef.child(currentUserId)
                .child(receiverId)
                .setValue(senderChat);

        chatRef.child(receiverId)
                .child(currentUserId)
                .setValue(receiverChat);
        etMessage.setText("");

    }

    private void loadMessages() {

        reference.addValueEventListener(new ValueEventListener() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                messageList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    MessageModel model = ds.getValue(MessageModel.class);

                    if (model != null) {

                        messageList.add(model);

                        // 🔥 MARK MESSAGE AS SEEN
                        if (model.getReceiverId() != null && model.getReceiverId().equals(currentUserId)
                                && !model.isSeen()) {

                            ds.getRef().child("seen").setValue(true);
                            ds.getRef().child("delivered").setValue(true);
                        }
                    }
                }

                adapter.notifyDataSetChanged();

                if (!messageList.isEmpty()) {
                    rvMessages.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadUsername() {

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(receiverId);

        userRef.addValueEventListener(new ValueEventListener() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String username = snapshot.child("username")
                        .getValue(String.class);

                tvChatUsername.setText(username);

                String status = snapshot.child("status")
                        .getValue(String.class);

                if("online".equals(status)){
                    tvOnlineStatus.setText("online");
                }
                else{
                    Long lastSeen = snapshot.child("lastSeen")
                            .getValue(Long.class);

                    if(lastSeen != null){
                        tvOnlineStatus.setText("Last seen " +
                                getFormattedTime(lastSeen));
                    }
                    else{
                        tvOnlineStatus.setText("offline");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String getFormattedTime(long time){

        @SuppressLint("SimpleDateFormat") java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("hh:mm a");

        return sdf.format(new java.util.Date(time));
    }

    private String getChatRoomId(String user1, String user2){
        if(user1.compareTo(user2) < 0){
            return user1 + "_" + user2;
        } else {
            return user2 + "_" + user1;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateStatus("offline");
    }

    private void updateStatus(String state){

        String uid = FirebaseAuth.getInstance().getUid();
        if(uid == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid);

        if(state.equals("online")){
            userRef.child("status").setValue("online");
        } else {

            userRef.child("status").setValue("offline");
            userRef.child("lastSeen").setValue(System.currentTimeMillis());
        }
    }




}
