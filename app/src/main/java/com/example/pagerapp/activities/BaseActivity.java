package com.example.pagerapp.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pagerapp.utilities.Keys;
import com.example.pagerapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {


    private DocumentReference documentReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        documentReference = database.collection(Keys.COLLECTION_USERS)
                .document(preferenceManager.getString(Keys.USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Keys.AVAILABILITY,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Keys.AVAILABILITY,1);
    }

}
