package com.nit.womenssecurity.activity;

import android.Manifest;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.pojos.User;
import com.nit.womenssecurity.receiver.LocationProviderChangedReceiver;
import com.nit.womenssecurity.utils.ShakeDetector;
import com.nit.womenssecurity.utils.TrackingActivator;
import com.nit.womenssecurity.utils.WSFirebase;
import com.nit.womenssecurity.utils.WSNotification;
import com.nit.womenssecurity.utils.WSPreference;

import androidx.annotation.NonNull;
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
import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String LOCATION_ACTION = "com.nit.womenssecurity.location_action";

    public static final int PERMISSIONS_REQUEST_LOCATION = 1224;

    private AppBarConfiguration mAppBarConfiguration;
    private SweetAlertDialog alertDialog;
    private LocationManager lm;
    private WSPreference wsPreference;
    private TrackingActivator trackingActivator;
    private User user;

    private TextView notificationTV;
    private int notificationCount = 0;

    public static final String CHANNEL_ID = "ws_notification_id";
    public static final String CHANNEL_NAME = "ws_notification_channel";
    public static final String CHANNEL_DESC = "ws_notification_des";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginCheck();
        setContentView(R.layout.activity_main);
        getUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        alertDialog = new SweetAlertDialog(this);
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        notificationCount = wsPreference.getBadge();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(channel);
        }

        FirebaseMessaging.getInstance().subscribeToTopic("updates");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        trackingActivator = new TrackingActivator(this);

        if (!checkLocationPermission()) {
            permissionDialog();
        } else if (checkLocationPermission()){
            if (wsPreference.getTracking() && locationEnabled() && online()) {
                trackingActivator.startTracking();
            }
        }


    }

    private boolean locationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    private boolean online() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return  (netInfo != null && netInfo.isConnected());
    }

    private void getUser() {
        Intent data = getIntent();
        if (data.getSerializableExtra("user") != null) {
            User user = (User) data.getSerializableExtra("user");
            this.user = user;
        }
    }

     private void loginCheck() {
        wsPreference = new WSPreference(this);

        if (wsPreference.getUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            this.user = wsPreference.getUser();
        }
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

        final MenuItem menuItem = menu.findItem(R.id.action_notification);

        View actionView = menuItem.getActionView();
        notificationTV = (TextView) actionView.findViewById(R.id.cart_badge);
        setupBadge();
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });
        return true;
    }

    private void setupBadge() {

        if (notificationTV != null) {
            if (notificationCount == 0) {
                if (notificationTV.getVisibility() != View.GONE) {
                    notificationTV.setVisibility(View.GONE);
                }
            } else {
                notificationTV.setText(String.valueOf(notificationCount));
                if (notificationTV.getVisibility() != View.VISIBLE) {
                    notificationTV.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.action_logout:
                logoutManage();
                break;
            case R.id.action_notification:
                startActivity(new Intent(this, NotificationActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutManage() {
        WSFirebase.getAuth().signOut();
        wsPreference.removeWsPref();
        trackingActivator.stopTracking();
        Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
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
