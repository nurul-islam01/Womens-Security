package com.nit.womenssecurity.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nit.womenssecurity.pojos.Contact;
import com.nit.womenssecurity.pojos.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WSPreference {

    public static final String PREF_KEY = "womenssecurity_pref";

    SharedPreferences mPref;
    SharedPreferences.Editor editor;

    public WSPreference(Context mContext) {
        mPref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        editor=mPref.edit();
    }

    public void setUser(User user){
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString("user", json);
        editor.apply();
    }

    public User getUser(){
        Gson gson = new Gson();
        String json = mPref.getString("user", null);
        User user = gson.fromJson(json, User.class);
        return user;
    }

    public void saveContacts(List<Contact> contacts){
        Gson gson = new Gson();
        String json = gson.toJson(contacts);
        editor.putString("contacts", json);
        editor.apply();
    }

    public List<Contact> getContacts(){
        Gson gson = new Gson();
        String json = mPref.getString("contacts", null);
        Type type = new TypeToken<ArrayList<Contact>>() {}.getType();
        List<Contact> contacts = gson.fromJson(json, type);
        return contacts;
    }

    public void saveTacking(boolean isTraking) {
        editor.putBoolean("tracking", isTraking);
        editor.apply();
    }

    public boolean getTracking() {
        return mPref.getBoolean("tracking", true);
    }

    public void saveBadge(int i) {
        editor.putInt("badge", i);
        editor.apply();
    }



    public int getBadge() {
        return mPref.getInt("badge", 0);
    }

    public void removeWsPref() {
        editor.clear().apply();
    }
}
