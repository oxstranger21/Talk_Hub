package com.example.talkhub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.talkhub.adapters.ChatsAdapter;
import com.example.talkhub.models.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatsFragment extends Fragment {

    ImageView ivSearch;
    RecyclerView rvChats;

    List<ChatModel> chatList;
    ChatsAdapter adapter;

    DatabaseReference reference;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // Initialize Views
        ivSearch = view.findViewById(R.id.ivSearch);
        rvChats = view.findViewById(R.id.rvChats);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext());

        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));


        chatList = new ArrayList<>();
        adapter = new ChatsAdapter(getContext(), chatList);
        rvChats.setAdapter(adapter);

        // Search Button Click
        ivSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchUserActivity.class);
            startActivity(intent);
        });

        // Load Chats
        loadChats();

        return view;
    }

    private void loadChats() {

        String currentUserId = FirebaseAuth.getInstance().getUid();

        assert currentUserId != null;
        reference = FirebaseDatabase.getInstance()
                .getReference("Chats")
                .child(currentUserId);

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                chatList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatModel model = ds.getValue(ChatModel.class);
                    if (model != null) {
                        chatList.add(model);
                    }
                }
                chatList.sort((a, b) ->
                        Long.compare(b.getTimestamp(), a.getTimestamp()));


                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}