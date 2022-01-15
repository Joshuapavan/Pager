package com.example.pagerapp;

import android.app.Application;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

public class EmojiApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Emoji manager//
        EmojiManager.install(new GoogleEmojiProvider());
    }
}
