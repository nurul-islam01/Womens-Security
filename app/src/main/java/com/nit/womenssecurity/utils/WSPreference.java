package com.nit.womenssecurity.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.nit.womenssecurity.pojos.User;

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

    public void saveTacking(boolean isTraking) {
        editor.putBoolean("tracking", isTraking);
        editor.apply();
    }

    public boolean getTracking() {
        return mPref.getBoolean("tracking", true);
    }

    public void removeUser() {
        if (mPref.getString("user", null) != null) {
            editor.putString("user", null);
        }
    }

    public void removeWsPref() {
        editor.clear().apply();
    }
}
