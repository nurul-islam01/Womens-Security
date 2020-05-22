package com.nit.womenssecurity.services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.solver.widgets.Helper;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.activity.DangerActivity;
import com.nit.womenssecurity.activity.MainActivity;
import com.nit.womenssecurity.activity.SettingActivity;
import com.nit.womenssecurity.pojos.Contact;
import com.nit.womenssecurity.pojos.DeviceToken;
import com.nit.womenssecurity.pojos.Notifi;
import com.nit.womenssecurity.pojos.User;
import com.nit.womenssecurity.pojos.UserLocation;
import com.nit.womenssecurity.utils.ShakeDetector;
import com.nit.womenssecurity.utils.WSFirebase;
import com.nit.womenssecurity.utils.WSNotification;
import com.nit.womenssecurity.utils.WSPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.nit.womenssecurity.ui.home.HomeFragment.MESSAGE_RECEIVER;
import static com.nit.womenssecurity.ui.home.HomeFragment.NOTIFICATION_SENDS;


public class LocationUpdateService extends Service implements ShakeDetector.Listener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    public static final String TAG = "BackgroundLocationUpdateService";
    public final String TAG_LOCATION = "TAG_LOCATION";
    public static final String AUTHORIZATION_KEY = "authorization";
    public static final String CATEGORY = "category";
    public static final String DANGER = "danger";
    public static final String NOTIFICATION_ID = "notification_id";

    private Context context;
    private boolean stopService = false;
    public static int interVal = 50;
    public static int maxDistance = 500;

    private static final int NOTIF_ID = 2;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";

    /* For Google Fused API */
    protected GoogleApiClient mGoogleApiClient;
    protected LocationSettingsRequest mLocationSettingsRequest;
    private String latitude = "0.0", longitude = "0.0";
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    /* For Google Fused API */

    private LocationManager lm;

    private SensorManager manager;
    private ShakeDetector shakeDetector;
    private int counting = 0;
    private boolean alert = false;

    private User user;
    private WSPreference preference;

    private RequestQueue requestQueue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    private LocalBroadcastManager localBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        shakeDetector = new ShakeDetector(this);
        preference = new WSPreference(this);
        requestQueue = Volley.newRequestQueue(this);
        user = preference.getUser();
    }

    @SuppressLint("LongLogTag")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preference.saveTacking(true);
        shakeDetector.start(manager);

        buildGoogleApiClient();
        if (preference.getTracking()) {
            startForeground();
        }
        try {
            if (intent.getBooleanExtra("warning", false)){
                if (online() && locationEnabled()) {
                    startAlertProcess();
                } else {
                    sendSMSToPhone();
                }
            }
        }catch (Exception e){
            Log.d(TAG, "onStartCommand: " + e.getMessage());
        }

        return  super.onStartCommand(intent, flags, startId);
    }

    private void startForeground() {
        Intent notificationIntent = new Intent(this, SettingActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                MainActivity.CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ws_front)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("WS is running background")
                .setContentIntent(pendingIntent)
                .build());
    }


    @SuppressLint("LongLogTag")
    @Override
    public void hearShake() {

        counting = counting + 1;

        if (counting > 1 && !alert) {
            counting = 0;
            alert = true;
            if (online() && locationEnabled()) {
                startAlertProcess();
            } else {
                sendSMSToPhone();
            }
        }


        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                counting = 0;
                alert = false;
//              LocationUpdateService.interVal = 50;
            }

        }.start();

    }

    @SuppressLint("LongLogTag")
    private void sendSMSToPhone() {

        List<Contact> contacts = preference.getContacts();
        List<Contact> sendList = new ArrayList<>();

        if (contacts != null) {
            for (Contact contact: contacts) {
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(contact.getNumber(), null, user.getFullName() + " is in danger, please help her hurry", null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                    sendList.add(contact);

                    Log.d(TAG, "sendSMSToPhone: " + contact.toString());

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed, please try again.",
                            Toast.LENGTH_LONG).show();
                    Log.d(TAG, "sendSMSToPhone: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent(MESSAGE_RECEIVER);
            if (sendList.size() > 0) {
                intent.putExtra("smssendinglist", (Serializable) sendList);
            } else {
                intent.putExtra("error", true);
            }
            sendBroadcast(intent);
        }
    }

    private boolean online() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return  (netInfo != null && netInfo.isConnected());
    }

    private boolean locationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void startAlertProcess() {

        WSFirebase.userLocation().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Date date = new Date(System.currentTimeMillis() - 500 * 1000);
                    long time = date.getTime();
                    List<UserLocation>  locations = new ArrayList<>();

                    for (DataSnapshot d: dataSnapshot.getChildren()) {
                        UserLocation location = d.getValue(UserLocation.class);
                        assert location != null;
                        int distance = distanceTo(Double.parseDouble(latitude), Double.parseDouble(longitude), location.getLat(), location.getLon());
                        if (!location.getUserId().equals(user.getId()) && time < location.getTime() && distance < maxDistance) {
                            locations.add(location);
                            getToken(location.getUserId());
                        }
                    }

                    Intent intent = new Intent(NOTIFICATION_SENDS);
                    if (locations.size() > 0) {
                        intent.putExtra("sendTo", locations.size());
                    } else {
                        sendSMSToPhone();
                        intent.putExtra("sendTo", 0);
                    }
                    sendBroadcast(intent);
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                sendSMSToPhone();
                Log.d(TAG, "onCancelled: " + databaseError.getDetails());
            }
        });
    }

    private void getToken(String toId) {
        WSFirebase.userToken().child(toId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DeviceToken token = dataSnapshot.getValue(DeviceToken.class);
                    sendNotification(token);
                }
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getDetails());
            }
        });
    }


    @SuppressLint({"StaticFieldLeak", "LongLogTag"})
    private void sendNotification(DeviceToken token) {

        JSONObject json = new JSONObject();
        JSONObject jsonData = new JSONObject();
        try {

            String title = user.getFullName() + " In danger";
            String body = "This notification was from " + user.getFullName() + ". she is in danger. Please help her";
            Date date = new Date();
            long time = date.getTime();

            jsonData.put("body", body);
            jsonData.put("title", title);

            JSONObject extraData = new JSONObject();
            String pushKey = WSFirebase.notifications().push().getKey();
            extraData.put(NOTIFICATION_ID, pushKey);
            extraData.put(CATEGORY, DANGER);
            extraData.put("receiverId", token.getUserId());
            extraData.put("senderId", user.getId());
            extraData.put("time", time);
            extraData.put("seen", false);

            json.put("to", token.getToken());
            json.put("notification", jsonData);
            json.put("data", extraData);

            Notifi notifi = new Notifi(pushKey, token.getUserId(), user.getId(), time, title, DANGER, body, false);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(context, "Notification send", Toast.LENGTH_SHORT).show();

                            WSFirebase.notifications().child(pushKey).setValue(notifi);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "onErrorResponse: " + error.getMessage());
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("content-type", "application/json; charset=utf-8");
                    headers.put(AUTHORIZATION_KEY, "key=" + getResources().getString(R.string.server_key));
                    return headers;
                }
            };

            requestQueue.add(request);

        }catch (Exception e){
            Log.d(TAG, "sendNotification: " + e.getMessage());
        }
    }

    private int distanceTo(double currentlatitude, double currentlongitude, double originLat, double originLon) {

        float[] results = new float[1];
        Location.distanceBetween(currentlatitude, currentlongitude, originLat, originLon, results);
        float distanceInMeters = results[0];

        return (int) distanceInMeters;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onDestroy() {
        Log.e(TAG, "Service Stopped");
        stopService = true;
        preference.saveTacking(false);
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Log.e(TAG_LOCATION, "Location Update Callback Removed");
        }
        shakeDetector.stop();
        if (!preference.getTracking()) {
            NotificationManager mNotificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIF_ID);
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onLocationChanged(Location location) {

        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());

        if (latitude.equalsIgnoreCase("0.0") && longitude.equalsIgnoreCase("0.0")) {
            requestLocationUpdate();
        } else {
            Date date = new Date();
            long time = date.getTime();
            String userId = user.getId();
            UserLocation loc = new UserLocation(userId, time, location.getLatitude(), location.getLongitude());
            WSFirebase.userLocation().child(userId).setValue(loc);
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(interVal * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();

        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.e(TAG_LOCATION, "GPS Success");
                        requestLocationUpdate();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            int REQUEST_CHECK_SETTINGS = 214;
                            ResolvableApiException rae = (ResolvableApiException) e;
                            rae.startResolutionForResult((Activity) getApplicationContext(), REQUEST_CHECK_SETTINGS);
                        } catch (Exception e1) {
                            Log.e(TAG_LOCATION, "Unable to execute request." + e1.getMessage());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.e(TAG_LOCATION, "Location settings are inadequate, and cannot be fixed here. Fix in Settings.");
                }
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.e(TAG_LOCATION, "checkLocationSettings -> onCanceled");
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectGoogleClient();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mSettingsClient = LocationServices.getSettingsClient(context);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        connectGoogleClient();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e(TAG_LOCATION, "Location Received");
                mCurrentLocation = locationResult.getLastLocation();
                onLocationChanged(mCurrentLocation);
            }
        };
    }

    private void connectGoogleClient() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(context);
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
        }
    }

    private void requestLocationUpdate() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

}
