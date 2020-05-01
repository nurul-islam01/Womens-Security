package com.nit.womenssecurity.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.nit.womenssecurity.services.LocationUpdateService;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.nit.womenssecurity.activity.MainActivity.LOCATION_ACTION;

public class TrackingActivator {

    private Context context;

    public TrackingActivator(Context context) {
        this.context = context;
    }

    public void startTracking() {
        if (!checkServiceRunning(LocationUpdateService.class)) {
            Intent intent = new Intent(LOCATION_ACTION);
            intent.setPackage(context.getPackageName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }

    public void stopTracking() {
        if (checkServiceRunning(LocationUpdateService.class)) {
            Intent intent = new Intent(LOCATION_ACTION);
            intent.setPackage(context.getPackageName());
            context.stopService(intent);
        }
    }

    public boolean checkServiceRunning(Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

}
