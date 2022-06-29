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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.example.pagerapp.R;
import com.example.pagerapp.adapters.ChatAdapter;
import com.example.pagerapp.databinding.ActivityChatBinding;
import com.example.pagerapp.models.ChatMessage;
import com.example.pagerapp.models.User;
import com.example.pagerapp.network.ApiClient;
import com.example.pagerapp.network.ApiService;
import com.example.pagerapp.utilities.Keys;
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

    ActivityChatBinding binding;  // Binding //
    User receiverUser; // Receiver Object //
    private List<ChatMessage> chatMessages; //List of ChatMessage class object //
    ChatAdapter chatAdapter; //Chat Adapter to display the data //
    PreferenceManager preferenceManager; // instance of preference manager for data persistence //
    FirebaseFirestore database; // Instance of firebase to provide cloud messaging //
    String conversationId = null; // conversation ID to uniquely identify each conversation //
    private Boolean isReceiverAvailable = false; //Boolean value to check if the user is online or not //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners(); // method to set the listeners to trigger the methods //
        loadReceiverDetails(); // to retrieve the receiver's data //
        init(); // initialising the variables //
        listenMessages(); // method to check for the incoming messages //
    }

    void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Keys.USER_ID)
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

    void listenMessages(){
        database.collection(Keys.COLLECTION_CHAT)
                .whereEqualTo(Keys.SENDER_ID,preferenceManager.getString(Keys.USER_ID))
                .whereEqualTo(Keys.RECEIVER_ID,receiverUser.id)
                .addSnapshotListener(eventListener);

        database.collection(Keys.COLLECTION_CHAT)
                .whereEqualTo(Keys.SENDER_ID,receiverUser.id)
                .whereEqualTo(Keys.RECEIVER_ID,preferenceManager.getString(Keys.USER_ID))
                .addSnapshotListener(eventListener);
    }


    void sendMessage(View v){
        if(isConnectedToInternet()){
            EmojiTextView emojiTextView = (EmojiTextView) LayoutInflater // Google Emoji Keyboard//
                    .from(v.getContext())
                    .inflate(R.layout.emoji_text_view,binding.chatLayout,false);
            emojiTextView.setText(binding.message.getText().toString());

            HashMap<String, Object> message = new HashMap<>();
            message.put(Keys.SENDER_ID, preferenceManager.getString(Keys.USER_ID));
            message.put(Keys.RECEIVER_ID,receiverUser.id);
            message.put(Keys.MESSAGE, binding.message.getText().toString());
            message.put(Keys.TIMESTAMP,new Date());
            database.collection(Keys.COLLECTION_CHAT).add(message);

            if(conversationId != null){
                updateConversation(binding.message.getText().toString());
            }else{
                HashMap<String, Object> conversation = new HashMap<>();
                conversation.put(Keys.SENDER_ID,preferenceManager.getString(Keys.USER_ID));
                conversation.put(Keys.SENDERS_NAME,preferenceManager.getString(Keys.KEY_NAME));
                conversation.put(Keys.SENDERS_IMAGE,preferenceManager.getString(Keys.IMAGE));
                conversation.put(Keys.RECEIVER_ID,receiverUser.id);
                conversation.put(Keys.RECEIVERS_NAME,receiverUser.name);
                conversation.put(Keys.RECEIVERS_IMAGE,receiverUser.image);
                conversation.put(Keys.LAST_MESSAGE,binding.message.getText().toString());
                conversation.put(Keys.TIMESTAMP,new Date());
                addConversation(conversation);
            }
            if (!isReceiverAvailable){
                try{
                    JSONArray tokens = new JSONArray();
                    tokens.put(receiverUser.token);

                    JSONObject data = new JSONObject();
                    data.put(Keys.USER_ID,preferenceManager.getString(Keys.USER_ID));
                    data.put(Keys.KEY_NAME, preferenceManager.getString(Keys.KEY_NAME));
                    data.put(Keys.FCM_TOKEN,preferenceManager.getString(Keys.FCM_TOKEN));
                    data.put(Keys.MESSAGE, binding.message.getText().toString());

                    JSONObject body = new JSONObject();
                    body.put(Keys.REMOTE_MSG_DATA, data);
                    body.put(Keys.REMOTE_MSG_REGISTRATION_IDS,tokens);

                    sendNotification(body.toString());

                }catch (Exception e){
                    e.printStackTrace();
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
                Keys.getRemoteMsgHeaders(),
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
                                Log.e("error",error.getString("error") );
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
                Log.e("error",t.getMessage());
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
                    chatMessage.senderId = documentChange.getDocument().getString(Keys.SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Keys.RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Keys.MESSAGE);
                    chatMessage.dateTime = getFormattedDate(documentChange.getDocument().getDate(Keys.TIMESTAMP));
                    chatMessage.date = documentChange.getDocument().getDate(Keys.TIMESTAMP);
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
        database.collection(Keys.COLLECTION_USERS)
                .document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this,(value, error)->{
                    if(error != null){
                        return;
                    }
                    if(value != null){
                        if(value.getLong(Keys.AVAILABILITY) != null){
                            int availability = Objects.requireNonNull(
                                    value.getLong(Keys.AVAILABILITY)
                            ).intValue();
                            isReceiverAvailable = availability == 1;
                        }
                        receiverUser.token = value.getString(Keys.FCM_TOKEN);
                        if(receiverUser.image == null){
                            receiverUser.image = value.getString(Keys.IMAGE);
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
        receiverUser = (User) getIntent().getSerializableExtra(Keys.USER);
        binding.userName.setText(receiverUser.name);
        binding.userImage.setImageBitmap(getBitmapFromEncodedString(receiverUser.image));
    }

    String getFormattedDate(Date date){
        return (new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault())).format(date);
    }

    void addConversation(HashMap<String,Object> conversation){
        database.collection(Keys.COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    void updateConversation(String message){
        DocumentReference documentReference =
                database.collection(Keys.COLLECTION_CONVERSATIONS).document(conversationId);

        documentReference.update(
                Keys.LAST_MESSAGE, message,
                Keys.TIMESTAMP, new Date()
        );
    }


    void checkForConversations(){
        if(chatMessages.size() != 0){
            checkForConversationRemotely(
                    preferenceManager.getString(Keys.USER_ID),
                    receiverUser.id
            );
            checkForConversationRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Keys.USER_ID)
            );
        }
    }

    void checkForConversationRemotely(String senderId, String receiverId){
        database.collection(Keys.COLLECTION_CONVERSATIONS)
                .whereEqualTo(Keys.SENDER_ID,senderId)
                .whereEqualTo(Keys.RECEIVER_ID,receiverId)
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