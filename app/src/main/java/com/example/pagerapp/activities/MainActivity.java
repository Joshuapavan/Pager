package com.example.pagerapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;


import com.example.pagerapp.adapters.RecentConversationsAdapter;
import com.example.pagerapp.databinding.ActivityMainBinding;
import com.example.pagerapp.listeners.ConversationListener;
import com.example.pagerapp.models.ChatMessage;
import com.example.pagerapp.models.User;
import com.example.pagerapp.utilities.Keys;
import com.example.pagerapp.utilities.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversationListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversations();
    }



    void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations , this);
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    void listenConversations(){
        database.collection(Keys.COLLECTION_CONVERSATIONS)
                .whereEqualTo(Keys.SENDER_ID,preferenceManager.getString(Keys.USER_ID))
                .addSnapshotListener(eventListener);

        database.collection(Keys.COLLECTION_CONVERSATIONS)
                .whereEqualTo(Keys.RECEIVER_ID,preferenceManager.getString(Keys.USER_ID))
                .addSnapshotListener(eventListener);
    }


    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Keys.SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Keys.RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;

                    if(preferenceManager.getString(Keys.USER_ID).equals(senderId)){
                        chatMessage.conversationImage = documentChange.getDocument().getString(Keys.RECEIVERS_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Keys.RECEIVERS_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Keys.RECEIVER_ID);
                    }else{
                        chatMessage.conversationImage = documentChange.getDocument().getString(Keys.SENDERS_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Keys.SENDERS_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Keys.SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Keys.LAST_MESSAGE);
                    chatMessage.date = documentChange.getDocument().getDate(Keys.TIMESTAMP);
                    conversations.add(chatMessage);
                }
                else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0 ; i < conversations.size() ; i++){
                        String senderId = documentChange.getDocument().getString(Keys.SENDER_ID);
                        String receiversId = documentChange.getDocument().getString(Keys.RECEIVER_ID);

                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiversId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Keys.LAST_MESSAGE);
                            conversations.get(i).date = documentChange.getDocument().getDate(Keys.TIMESTAMP);
                            break;
                        }
                    }
                }
            }

            Collections.sort(conversations,(obj1 , obj2 )-> obj2.date.compareTo(obj1.date));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    });


    private void setListeners(){
        binding.searchIcon.setOnClickListener(v -> Snackbar.make(binding.mainLayout, "Search", Snackbar.LENGTH_SHORT).show());
        binding.userImage.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),Profile.class)));
        binding.newChat.setOnClickListener(v-> startActivity(new Intent(getApplicationContext(),UserActivity.class)));
    }

    private void loadUserDetails(){
        byte[] bytes = Base64.decode(preferenceManager.getString(Keys.IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.userImage.setImageBitmap(bitmap);
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(Keys.FCM_TOKEN, token);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection(Keys.COLLECTION_USERS)
                .document(preferenceManager.getString(Keys.USER_ID));
        documentReference.update(Keys.FCM_TOKEN,token)
                .addOnSuccessListener(task -> Snackbar.make(binding.mainLayout,"Welcome "+preferenceManager.getString(Keys.KEY_NAME)+"!",Snackbar.LENGTH_SHORT).show())
                .addOnFailureListener(exception -> Snackbar.make(binding.mainLayout, "Unable to update token, Please restart the app.",Snackbar.LENGTH_SHORT).show());
    }

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Keys.USER,user);
        startActivity(intent);
    }
}