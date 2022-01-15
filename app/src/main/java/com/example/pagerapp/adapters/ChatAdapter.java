package com.example.pagerapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pagerapp.databinding.RecivedMessagesContainerBinding;
import com.example.pagerapp.databinding.SentMessagesContainerBinding;
import com.example.pagerapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;


    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    SentMessagesContainerBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else{
            return new ReceivedMessageViewHolder(
                    RecivedMessagesContainerBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId)){
            return (VIEW_TYPE_SENT);
        }else {
            return (VIEW_TYPE_RECEIVED);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));
        }else{
            ((ReceivedMessageViewHolder)holder).setData(chatMessages.get(position),receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static  class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private final SentMessagesContainerBinding binding;

        SentMessageViewHolder(SentMessagesContainerBinding sentMessagesContainerBinding) {
            super(sentMessagesContainerBinding.getRoot());
            binding = sentMessagesContainerBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
            binding.dateTime.setText(chatMessage.dateTime);

        }
    }
    static  class  ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        private  final RecivedMessagesContainerBinding binding;

        ReceivedMessageViewHolder(RecivedMessagesContainerBinding recivedMessagesContainerBinding){
            super(recivedMessagesContainerBinding.getRoot());
            binding = recivedMessagesContainerBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiversProfileImage){
            binding.textMessage.setText(chatMessage.message);
            binding.dateTime.setText(chatMessage.dateTime);
            binding.profileImage.setImageBitmap(receiversProfileImage);
        }
    }
}
