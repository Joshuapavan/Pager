package com.example.pagerapp.utilities;

import java.util.HashMap;

//All Keys which will be used when a new user Signs up which can be used to refer inside the app//
public class Keys {
    public static final String COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "pagerAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";

    //Generated Keys//
    public static final String USER_ID = "userId";
    public static final String IMAGE = "image";
    public static final String FCM_TOKEN = "fcmToken";
    public static final String USER = "user";

    //Realtime Chat Keys//
    public static final String COLLECTION_CHAT ="chat";
    public static final String SENDER_ID = "senderId";
    public static final String RECEIVER_ID =  "receiverId";
    public static final String MESSAGE =  "messaged";
    public static final String TIMESTAMP =  "timestamp";

    //Recent Conversations Keys//
    public static final String COLLECTION_CONVERSATIONS = "conversations";
    public static final String SENDERS_NAME = "sendersName";
    public static final String RECEIVERS_NAME = "receiversName";
    public static final String SENDERS_IMAGE = "sendersImage";
    public static final String RECEIVERS_IMAGE = "receiversImage";
    public static final String LAST_MESSAGE = "lastMessage";


    //User Status//
    public static final String AVAILABILITY = "availability";

    //Notification//
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";


    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders(){
        if(remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAKshjXoA:APA91bG16YQ-7lzS5HQi2G4u944kb_Mina0njDVVZvFMooYr6hxjCXEl3Ex44krhmPXUphpDTFwdp-p0Igu2Xq-cC5oQJs90rP9H5RBhC5-LclD1kVeFAWkxOJlNf9XTTf0OO8267Qbl"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }

}

