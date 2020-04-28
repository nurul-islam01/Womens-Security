package com.nit.womenssecurity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.nit.womenssecurity.receiver.LocationProviderChangedReceiver;
import com.nit.womenssecurity.services.BackgroundLocationUpdateService;
import com.nit.womenssecurity.services.TrackingService;
import com.nit.womenssecurity.utils.ShakeDetector;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.nit.womenssecurity.receiver.LocationProviderChangedReceiver.IS_LOCATION_ENABLED;

public class MainActivity extends AppCompatActivity implements ShakeDetector.Listener {
    private static final String TAG = "MainActivity";

    public static final int PERMISSIONS_REQUEST_LOCATION = 1224;

    private AppBarConfiguration mAppBarConfiguration;
    private SweetAlertDialog alertDialog;
    private LocationManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        alertDialog = new SweetAlertDialog(this);
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (!checkLocationPermission()) {
            permissionDialog();
        } else {
            if (isLocationServiceEnable()) {
                startService(new Intent(this, BackgroundLocationUpdateService.class));
            } else {
                showLocationDialog();
            }
        }
        startService(new Intent(this, TrackingService.class));
        locationBroadCast();
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
            }
        }).setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                alertDialog.dismiss();
                Toast.makeText(MainActivity.this, "Application will not working correctly", Toast.LENGTH_SHORT).show();
            }
        }).show();
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
                if (locationProviderState) {
                    startService(new Intent(MainActivity.this, BackgroundLocationUpdateService.class));
                    Toast.makeText(context, "Location Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    showLocationDialog();
                    Toast.makeText(context, "Location Disabled", Toast.LENGTH_SHORT).show();
                }
            }
        }, intentFilter);
    }

    private void permissionDialog() {
        alertDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE);
        alertDialog.setTitle("Required Permission");
        alertDialog.setContentText("Location permission");
        alertDialog.setConfirmButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                requestLocationPermission();
            }
        }).setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                Toast.makeText(MainActivity.this, "Without this permissions app will not working", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        }).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void hearShake() {
        Toast.makeText(this, "shaked", Toast.LENGTH_SHORT).show();
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return false;
        } else {
            return true;
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_LOCATION);
        alertDialog.dismiss();
    }
}
