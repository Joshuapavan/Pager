package com.example.pagerapp.activities;

import static com.example.pagerapp.R.string.data_added;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pagerapp.R;
import com.example.pagerapp.databinding.ActivitySignUpBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user != null) {
            binding.userName.setText(user.getDisplayName());
            binding.email.setText(user.getEmail());
            Glide.with(getApplicationContext()).load(user.getPhotoUrl()).into(binding.userImage);
        }
    }

    private void setListeners() {
        binding.backImage.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        });
        binding.signUpButton.setOnClickListener(v->{
            if(binding.userName.getText().toString().isEmpty() || binding.email.getText().toString().isEmpty()
                    || binding.password.getText().toString().isEmpty() || binding.retypePassword.getText().toString().isEmpty()){
                Snackbar.make(v,"Please, fill all details",Snackbar.LENGTH_SHORT).show();
            }else{
                addDataToFireBase();
            }
        });
        binding.addImage.setOnClickListener(v->
                Snackbar.make(v,"Add / Change Image",Snackbar.LENGTH_SHORT).show()
        );
    }

    private void addDataToFireBase(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String,Object> data = new HashMap<>();
        data.put("first_name","Josh");
        data.put("last_name","Pavan");
        database.collection("users").add(data).addOnSuccessListener(documentReference -> {
            Toast.makeText(getApplicationContext(), data_added,Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            Toast.makeText(getApplicationContext(), exception.getMessage(),Toast.LENGTH_SHORT).show();
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