package com.example.pagerapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pagerapp.databinding.ActivityResetBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

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
                Snackbar.make(v,"Please Enter Reset Mail",Snackbar.LENGTH_SHORT).show();
            }else{
                mAuth.sendPasswordResetEmail(binding.email.getText().toString())
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                Snackbar.make(binding.getRoot(), "Mail sent, Check Inbox", Snackbar.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Snackbar.make(binding.getRoot(),"Error: "+ Objects.requireNonNull(task.getException()).getMessage(),Snackbar.LENGTH_LONG).show();
                            }
                        });

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