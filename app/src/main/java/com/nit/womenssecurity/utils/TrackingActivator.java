package com.nit.womenssecurity.utils;

import android.content.Context;
import android.content.Intent;

import com.nit.womenssecurity.services.LocationUpdateService;

public class TrackingActivator {

    private Context context;

    public TrackingActivator(Context context) {
        this.context = context;
    }

    public void startTracking() {
        context.startService(new Intent(context, LocationUpdateService.class));
    }

    public void stopTracking() {
        context.stopService(new Intent(context, LocationUpdateService.class));
    }

}
