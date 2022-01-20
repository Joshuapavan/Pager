package com.example.pagerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.pagerapp.adapters.UsersAdapter;
import com.example.pagerapp.databinding.ActivityUserBinding;
import com.example.pagerapp.listeners.UserListener;
import com.example.pagerapp.models.User;
import com.example.pagerapp.utilities.Keys;
import com.example.pagerapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements UserListener {

    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
        getUsers();

        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    void setListeners(){
        binding.back.setOnClickListener(v-> onBackPressed());
        binding.chat.setOnClickListener(v-> onBackPressed());
    }

    void getUsers(){
        isLoading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Keys.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    isLoading(false);
                    String currentUserId = preferenceManager.getString(Keys.USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Keys.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Keys.KEY_EMAIL);
                            user.image = (queryDocumentSnapshot.getString(Keys.IMAGE));
                            user.token = queryDocumentSnapshot.getString(Keys.FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size() > 0){
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.userRecyclerView.setAdapter(usersAdapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        }else{
                            showErrorMessage();
                        }
                    }else{
                        showErrorMessage();
                    }
                });
    }


    void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No Users available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    void isLoading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserListenerClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Keys.USER,user);
        startActivity(intent);
        finish();
    }
}