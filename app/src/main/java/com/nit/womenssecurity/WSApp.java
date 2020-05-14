package com.nit.womenssecurity;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nit.womenssecurity.utils.WSPreference;

import static com.nit.womenssecurity.activity.MainActivity.LOCATION_ACTION;

public class WSApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
    }
}
