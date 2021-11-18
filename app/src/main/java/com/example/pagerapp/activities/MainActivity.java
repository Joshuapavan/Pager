package com.example.pagerapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pagerapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    FirebaseAuth mAuth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user == null){
            Intent loginIntent = new Intent(getApplicationContext(), Login.class);
            startActivity(loginIntent);
            finish();
        }

        if(user != null){
            binding.username.setText(user.getDisplayName());
            binding.email.setText(user.getEmail());
            Glide.with(getApplicationContext()).load(user.getPhotoUrl()).into(binding.userImage);
        }
    }

    private void setListeners() {
        binding.logout.setOnClickListener(v->{
            mAuth.signOut(); //Sign out code//
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
        });
    }
}