package com.nit.womenssecurity.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.nit.womenssecurity.R;
import com.nit.womenssecurity.receiver.LocationProviderChangedReceiver;
import com.nit.womenssecurity.utils.TrackingActivator;
import com.nit.womenssecurity.utils.WSPreference;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.nit.womenssecurity.receiver.LocationProviderChangedReceiver.IS_LOCATION_ENABLED;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";

    private RadioGroup trackingRG;
    private WSPreference preference;
    private RadioButton start, stop;
    private TrackingActivator activator;
    private LocationManager lm;

    private SweetAlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        alertDialog = new SweetAlertDialog(this);

        preference = new WSPreference(this);
        activator = new TrackingActivator(this);
        trackingRG = findViewById(R.id.trackingRG);

        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);

        if (preference.getTracking()) {
             start.setChecked(true);
             stop.setChecked(false);
             setRG(true);
        } else {
            stop.setChecked(true);
            start.setChecked(false);
            setRG(false);
        }

        Log.d(TAG, "onCreate: " + preference.getTracking());


        trackingRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.start:
                        preference.saveTacking(true);
                        if (isLocationServiceEnable()) {
                            activator.startTracking();
                        } else {
                            showLocationDialog();
                        }
                        setRG(true);
                        break;
                    case R.id.stop:
                        preference.saveTacking(false);
                        activator.stopTracking();
                        setRG(false);
                        break;
                }
            }
        });

        locationBroadCast();
    }

    private boolean isLocationServiceEnable() {
        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            return false;
        }
    }

    private void locationBroadCast() {
        IntentFilter intentFilter = new IntentFilter(LocationProviderChangedReceiver.LOCATION_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean locationProviderState = intent.getBooleanExtra(IS_LOCATION_ENABLED, false);
                if (locationProviderState && preference.getTracking()) {
                    activator.startTracking();
                }
                String text = locationProviderState ? "Enabled" : "Disabled";
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        }, intentFilter);
    }

    private void showLocationDialog() {
        alertDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
        alertDialog.setTitle("Enabled location");
        alertDialog.setContentText("Location is required for this app");
        alertDialog.setConfirmButton("Settings", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                alertDialog.dismiss();
                activator.startTracking();
            }
        }).setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                alertDialog.dismiss();
                Toast.makeText(SettingActivity.this, "Application will not working correctly", Toast.LENGTH_SHORT).show();
            }
        }).show();
    }


    private void setRG(boolean isTracking) {
        if (isTracking) {
            stop.setTextColor(getResources().getColor( R.color.colorAccent));
            start.setTextColor(getResources().getColor( R.color.white));
        } else {
            start.setTextColor(getResources().getColor( R.color.colorAccent));
            stop.setTextColor(getResources().getColor( R.color.white));
        }
    }
}
