package com.example.talkhub.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.talkhub.R;
import com.example.talkhub.models.MessageModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MSG_TYPE_SENT = 1;
    private static final int MSG_TYPE_RECEIVED = 2;

    List<MessageModel> list;

    public MessagesAdapter(List<MessageModel> list){
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        String currentUserId = FirebaseAuth.getInstance().getUid();

        if(list.get(position).getSenderId().equals(currentUserId)){
            return MSG_TYPE_SENT;
        } else {
            return MSG_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_TYPE_SENT){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceiveViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageModel model = list.get(position);

        if(holder instanceof SentViewHolder){

            SentViewHolder sentHolder = (SentViewHolder) holder;
            sentHolder.tvMessage.setText(model.getMessage());

            // ✓ Tick Logic
            if(model.isSeen()){
                sentHolder.tvSeen.setText("✓✓");
                sentHolder.tvSeen.setTextColor(Color.BLUE); // Seen = Blue
            }
            else if(model.isDelivered()){
                sentHolder.tvSeen.setText("✓✓");
                sentHolder.tvSeen.setTextColor(Color.GRAY); // Delivered
            }
            else{
                sentHolder.tvSeen.setText("✓");
                sentHolder.tvSeen.setTextColor(Color.GRAY); // Sent
            }

        } else {

            ReceiveViewHolder receiveHolder = (ReceiveViewHolder) holder;
            receiveHolder.tvMessage.setText(model.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // SENT VIEW HOLDER
    static class SentViewHolder extends RecyclerView.ViewHolder {

        TextView tvMessage, tvSeen;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvSeen = itemView.findViewById(R.id.tvSeen);
        }
    }

    // RECEIVED VIEW HOLDER
    static class ReceiveViewHolder extends RecyclerView.ViewHolder {

        TextView tvMessage;

        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}