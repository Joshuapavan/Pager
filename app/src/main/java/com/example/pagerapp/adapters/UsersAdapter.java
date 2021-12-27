package com.example.pagerapp.adapters;

import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pagerapp.databinding.UserContainerBinding;
import com.example.pagerapp.models.User;

import java.util.List;

public class UsersAdapter extends  RecyclerView.Adapter<UsersAdapter.userViewHolder> {

    public UsersAdapter(List<User> users) {
        this.users = users;
    }

    private final List<User> users;

    UserContainerBinding binding;

    @NonNull
    @Override
    public UsersAdapter.userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserContainerBinding userContainerBinding = UserContainerBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return (new userViewHolder(userContainerBinding));
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.userViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class userViewHolder extends RecyclerView.ViewHolder{
        userViewHolder(UserContainerBinding userContainerBinding){
            super(userContainerBinding.getRoot());
            binding = userContainerBinding;
        }

        void setUserData(User user){
            binding.name.setText(user.name);
            binding.message.setText(user.email);
            binding.profileImage.setImageBitmap(getuserImage(user.image));
        }
    }

    private Bitmap getuserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
