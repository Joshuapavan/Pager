package com.example.pagerapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.example.pagerapp.databinding.ActivityProfileBinding;
import com.example.pagerapp.utilities.Constants;
import com.example.pagerapp.utilities.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Profile extends AppCompatActivity {

    ActivityProfileBinding binding;

    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        setListeners();
    }
    private void loadUserDetails(){
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.userImage.setImageBitmap(bitmap);
        binding.userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
    }

    void setListeners(){
        binding.backImage.setOnClickListener(v-> onBackPressed());
        binding.logoutButton.setOnClickListener(v-> logout());
    }

    void logout(){
        Snackbar.make(binding.profileActivity,"Logging Out!",Snackbar.LENGTH_SHORT).show();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(),Login.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(binding.profileActivity,"Unable to log out, please try again later.",Snackbar.LENGTH_SHORT).show();
                });
    }
}