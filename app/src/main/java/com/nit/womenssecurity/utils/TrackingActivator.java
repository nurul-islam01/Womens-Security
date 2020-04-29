package com.nit.womenssecurity.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nit.womenssecurity.activity.MainActivity;
import com.nit.womenssecurity.receiver.LocationProviderChangedReceiver;
import com.nit.womenssecurity.services.BackgroundLocationUpdateService;
import com.nit.womenssecurity.services.TrackingService;

import static com.nit.womenssecurity.receiver.LocationProviderChangedReceiver.IS_LOCATION_ENABLED;

public class TrackingActivator {

    private Context context;

    public TrackingActivator(Context context) {
        this.context = context;
    }

    public void startTracking() {
        context.startService(new Intent(context, BackgroundLocationUpdateService.class));
        context.startService(new Intent(context, TrackingService.class));
    }

    public void stopTracking() {
        context.stopService(new Intent(context, BackgroundLocationUpdateService.class));
        context.stopService(new Intent(context, TrackingService.class));
    }

}
