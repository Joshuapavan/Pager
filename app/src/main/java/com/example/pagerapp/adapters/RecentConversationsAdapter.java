package com.example.pagerapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pagerapp.databinding.RecentChatsContainerBinding;
import com.example.pagerapp.listeners.ConversationListener;
import com.example.pagerapp.models.ChatMessage;
import com.example.pagerapp.models.User;

import java.util.List;

public class RecentConversationsAdapter extends  RecyclerView.Adapter<RecentConversationsAdapter.ConversationViewHolder>{

    private final List<ChatMessage> chatMessages;

    private final ConversationListener conversationListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages , ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationViewHolder(
                RecentChatsContainerBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


    //View holder class//
    class ConversationViewHolder extends RecyclerView.ViewHolder{
        RecentChatsContainerBinding binding;

        ConversationViewHolder(RecentChatsContainerBinding recentChatsContainerBinding){
            super(recentChatsContainerBinding.getRoot());
            binding = recentChatsContainerBinding;
        }

        void setData (ChatMessage chatMessage){
            binding.profileImage.setImageBitmap(getConversationImage(chatMessage.conversationImage));
            binding.name.setText(chatMessage.conversationName);
            binding.message.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v->{
                User user = new User();
                user.id = chatMessage.conversationId;
                user.name = chatMessage.conversationName;
                user.image = chatMessage.conversationImage;
                conversationListener.onConversationClicked(user);
            });
        }
    }

    private Bitmap getConversationImage(String encodedImage){
        byte[] bytes = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
