package com.nit.womenssecurity.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class LocationProviderChangedReceiver extends BroadcastReceiver {
    private final static String TAG = "LocationProviderChanged";

    public static final String LOCATION_AVAILABLE_ACTION = "com.nit.womenssecurity.LocationAvailable";
    public static final String IS_LOCATION_ENABLED = "isLocationAvailable";

    private Context context;
    private boolean isGpsEnabled = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        Intent locationStateIntent = new Intent(LOCATION_AVAILABLE_ACTION);
        locationStateIntent.putExtra(IS_LOCATION_ENABLED, locationChecked(intent));
        LocalBroadcastManager.getInstance(context).sendBroadcast(locationStateIntent);
    }

    private boolean locationChecked(Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        return isGpsEnabled;
    }
}