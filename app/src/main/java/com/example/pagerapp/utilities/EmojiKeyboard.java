package com.example.pagerapp.utilities;

import android.app.Application;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

public class EmojiKeyboard extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Emoji manager//
        EmojiManager.install(new GoogleEmojiProvider());
    }
}
