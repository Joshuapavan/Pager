package com.example.pagerapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;

import com.example.pagerapp.R;
import com.example.pagerapp.adapters.ChatAdapter;
import com.example.pagerapp.databinding.ActivityChatBinding;
import com.example.pagerapp.models.ChatMessage;
import com.example.pagerapp.models.User;
import com.example.pagerapp.network.ApiClient;
import com.example.pagerapp.network.ApiService;
import com.example.pagerapp.utilities.Constants;
import com.example.pagerapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ChatActivity extends BaseActivity {

    ActivityChatBinding binding;
    User receiverUser;
    private List<ChatMessage> chatMessages;
    ChatAdapter chatAdapter;
    PreferenceManager preferenceManager;
    FirebaseFirestore database;
    String conversationId = null;

    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database =  FirebaseFirestore.getInstance();
    }

    void addEmoji(){
        EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(
                binding.chatLayout
        ).build(binding.message);

        binding.emoji.setOnClickListener(v-> emojiPopup.toggle());
    }

    Boolean isConnectedToInternet(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (manager != null) {
            networkInfo = manager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnected();
    }

    void sendMessage(View v){
        if(isConnectedToInternet()){
            HashMap<String, Object> message = new HashMap<>();
            EmojiTextView emojiTextView = (EmojiTextView) LayoutInflater // Google Emoji Keyboard//
                    .from(v.getContext())
                    .inflate(R.layout.emoji_text_view,binding.chatLayout,false);
            emojiTextView.setText(binding.message.getText().toString());

            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            message.put(Constants.KEY_MESSAGE, binding.message.getText().toString());
            message.put(Constants.KEY_TIMESTAMP,new Date());
            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
            if(conversationId != null){
                updateConversation(binding.message.getText().toString());
            }else{
                HashMap<String, Object> conversation = new HashMap<>();
                conversation.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
                conversation.put(Constants.KEY_SENDERS_NAME,preferenceManager.getString(Constants.KEY_NAME));
                conversation.put(Constants.KEY_SENDERS_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
                conversation.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
                conversation.put(Constants.KEY_RECEIVERS_NAME,receiverUser.name);
                conversation.put(Constants.KEY_RECEIVERS_IMAGE,receiverUser.image);
                conversation.put(Constants.KEY_LAST_MESSAGE,binding.message.getText().toString());
                conversation.put(Constants.KEY_TIMESTAMP,new Date());
                addConversation(conversation);
            }
            if (!isReceiverAvailable){
                try{
                    JSONArray tokens = new JSONArray();
                    tokens.put(receiverUser.token);

                    JSONObject data = new JSONObject();
                    data.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
                    data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                    data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                    data.put(Constants.KEY_MESSAGE, binding.message.getText().toString());

                    JSONObject body = new JSONObject();
                    body.put(Constants.REMOTE_MSG_DATA, data);
                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

                    sendNotification(body.toString());

                }catch (Exception e){
                    showSnackBar(e.getMessage());
                }
            }
            binding.message.setText(null);
        }
        else{
            showSnackBar("No Internet, please check your internet");
        }

    }

    void showSnackBar(String message){
        Snackbar.make(binding.chatLayout,message,Snackbar.LENGTH_SHORT).show();
    }


    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if(response.isSuccessful()){
                    try{
                        if(response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                showSnackBar(error.getString("error"));
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }else{
                    showSnackBar("Error: "+response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                showSnackBar(t.getMessage());
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged") //Dynamic chat assignment method //
    private final EventListener<QuerySnapshot> eventListener = (value , error) ->{
        if(error != null){
            return;
        }
        if(value != null){
            int count = chatMessages.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getFormattedDate(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.date = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, Comparator.comparing(obj -> obj.date));
            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.INVISIBLE);
        if(conversationId == null){
            checkForConversations();
        }
    };

    void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this,(value, error)->{
                    if(error != null){
                        return;
                    }
                    if(value != null){
                        if(value.getLong(Constants.KEY_AVAILABILITY) != null){
                            int availability = Objects.requireNonNull(
                                    value.getLong(Constants.KEY_AVAILABILITY)
                            ).intValue();
                            isReceiverAvailable = availability == 1;
                        }
                        receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                        if(receiverUser.image == null){
                            receiverUser.image = value.getString(Constants.KEY_IMAGE);
                            chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                            chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
                        }
                    }
                    if(isReceiverAvailable){
                        binding.userStatus.setText(R.string.online);
                    }else{
                        binding.userStatus.setText(R.string.offline);
                    }
                });
    }


    void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.id)
                .addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    Bitmap getBitmapFromEncodedString(String encodedImage){
        if(encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }else{
            return null;
        }
    }

    void setListeners(){
        binding.backImage.setOnClickListener(v-> onBackPressed());
        binding.sendButton.setOnClickListener(v->{
            if(binding.message.getText().toString().isEmpty()){
                Snackbar.make(binding.chatLayout,"Please Enter a message",Snackbar.LENGTH_SHORT).show();
                binding.message.setText(null);
            }else{
                sendMessage(v);
            }
                }
        );
        binding.emoji.setOnClickListener(v -> addEmoji());
    }


    void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.userName.setText(receiverUser.name);
        binding.userImage.setImageBitmap(getBitmapFromEncodedString(receiverUser.image));
    }

    String getFormattedDate(Date date){
        return (new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault())).format(date);
    }

    void addConversation(HashMap<String,Object> conversation){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    void updateConversation(String message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);

        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }


    void checkForConversations(){
        if(chatMessages.size() != 0){
            checkForConversationRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversationRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    void checkForConversationRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }


    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}