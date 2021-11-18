package com.example.pagerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pagerapp.databinding.ActivityResetBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class Reset extends AppCompatActivity {

    private ActivityResetBinding binding;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
        mAuth = FirebaseAuth.getInstance();
    }

    private void setListeners() {
        binding.getmailButton.setOnClickListener(v->{
            if(binding.email.getText().toString().isEmpty()){
                Snackbar.make(binding.getRoot().getRootView(),"Enter mail",Snackbar.LENGTH_SHORT).show();

            }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()) {
                Snackbar.make(binding.getRoot().getRootView(), "Invalid Email", Snackbar.LENGTH_SHORT).show();
            }
            else{
                mAuth.sendPasswordResetEmail(binding.email.getText().toString());
                Snackbar.make(binding.getRoot().getRootView(),"Please check Inbox",Snackbar.LENGTH_SHORT).show();
            }
        });
        binding.backImage.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),Login.class);
        startActivity(intent);
        finish();
    }
}