package com.example.pagerapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;

//Shared Preference to store and Cache data//
public class PreferenceManager {
    private final SharedPreferences sharedPreferences;

    //constructor for the preference Class//
    public PreferenceManager(Context context){
        sharedPreferences = context.getSharedPreferences(Keys.KEY_PREFERENCE_NAME,Context.MODE_PRIVATE);
    }

    //Getters and setters for the shared preference data //
    public void putBoolean(String key, Boolean value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }

    public Boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key,false);
    }

    public void putString(String key,String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }
    public String getString(String key){
        return sharedPreferences.getString(key,null);
    }

    public void clear(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
