package com.example.pagerapp.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pagerapp.databinding.ActivityMainBinding;
import com.example.pagerapp.utilities.Constants;
import com.example.pagerapp.utilities.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        loadUserDetails();
        getToken();
    }
    private void setListeners(){
        binding.searchIcon.setOnClickListener(v->{
            Snackbar.make(binding.mainLayout,"Search",Snackbar.LENGTH_SHORT).show();
        });
        binding.userImage.setOnClickListener(v->{
            Snackbar.make(binding.mainLayout,"User Image",Snackbar.LENGTH_SHORT).show();
        });
    }

    private void loadUserDetails(){
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.userImage.setImageBitmap(bitmap);
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnSuccessListener(task -> Snackbar.make(binding.mainLayout,"Welcome "+preferenceManager.getString(Constants.KEY_NAME)+"!",Snackbar.LENGTH_SHORT).show())
                .addOnFailureListener(exception -> Snackbar.make(binding.mainLayout, "Unable to update token, Please restart the app.",Snackbar.LENGTH_SHORT).show());
    }
}