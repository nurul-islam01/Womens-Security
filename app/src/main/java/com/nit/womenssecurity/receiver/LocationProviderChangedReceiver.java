package com.nit.womenssecurity.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.nit.womenssecurity.R;
import com.nit.womenssecurity.utils.TrackingActivator;
import com.nit.womenssecurity.utils.WSNotification;
import com.nit.womenssecurity.utils.WSPreference;

import java.util.Objects;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class LocationProviderChangedReceiver extends BroadcastReceiver {

    private final static String TAG = "LocationProviderChanged";

    private Context context;
    private boolean isGpsEnabled = false;
    private boolean userIsOnline = false;
    private TrackingActivator activator;
    private WSPreference wsPreference;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        activator = new TrackingActivator(context);
        wsPreference = new WSPreference(context);
        boolean ac = wsPreference.getTracking();

        if (Objects.requireNonNull(intent.getAction()).matches("android.location.PROVIDERS_CHANGED")) {
            if (!locationEnabled() && ac) {
                activator.stopTracking();
                Toast.makeText(context, "Enable location for WS", Toast.LENGTH_SHORT).show();
                WSNotification.showRequired(context, "Required!!!", "Enable location for WS", R.drawable.locaton_icon);
            } else if (locationEnabled() && online() && ac) {
                activator.startTracking();
            }
        } else if (intent.getAction().matches("android.net.conn.CONNECTIVITY_CHANGE")) {
            if (!online() && ac) {
                activator.stopTracking();
                Toast.makeText(context, "Please online for WS", Toast.LENGTH_SHORT).show();
                WSNotification.showRequired(context, "Required!!!", "Enable internet connection for WS", R.drawable.ic_internet_required);
            } else if (online() && locationEnabled() && ac) {
                activator.startTracking();
            }
        }
    }


    private boolean locationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isGpsEnabled;
    }


    private boolean online() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        userIsOnline = (netInfo != null && netInfo.isConnected());
        return userIsOnline;
    }
}