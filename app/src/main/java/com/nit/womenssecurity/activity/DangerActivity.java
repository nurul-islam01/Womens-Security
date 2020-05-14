package com.nit.womenssecurity.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.listener.DangerDataListener;
import com.nit.womenssecurity.listener.DangerListener;
import com.nit.womenssecurity.pojos.Notifi;
import com.nit.womenssecurity.pojos.User;
import com.nit.womenssecurity.pojos.UserLocation;
import com.nit.womenssecurity.ui.LocationFragment;
import com.nit.womenssecurity.ui.TrackerProfile;
import com.nit.womenssecurity.utils.WSFirebase;

public class DangerActivity extends AppCompatActivity {

    private static final String TAG = "DangerActivity";

    private DangerDataListener listener;
    private DangerListener dangerListener;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager viewPager = findViewById(R.id.viewPager);

        tabLayout.addTab(tabLayout.newTab().setText("Location"));
        tabLayout.addTab(tabLayout.newTab().setText("Profile"));

        Intent data = getIntent();
        Bundle bundle = data.getExtras();

        Notifi notifi;
        if (bundle.get("notification_id") != null) {

            String id = (String) bundle.get("notification_id");
            String receiverId = (String) bundle.get("receiverId");
            String senderId = (String) bundle.get("senderId");
            long time = Long.parseLong(bundle.get("time").toString());;
            String title =  (String) bundle.get("gcm.notification.title");
            String category = (String) bundle.get("category");
            String body = (String) bundle.get("gcm.notification.body");
            boolean seen = Boolean.parseBoolean(bundle.get("seen").toString());

            notifi = new Notifi(id, receiverId, senderId, time, title, category, body, seen);
            setNotifi(notifi);

        } else if (bundle.getSerializable("notification") != null) {
            notifi = (Notifi) bundle.getSerializable("notification");
            setNotifi(notifi);
        }

        PageChangeAdapter adapter = new PageChangeAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        if (bundle.getSerializable("notification") != null) {
            Notifi notifi = (Notifi) bundle.getSerializable("notification");
            setNotifi(notifi);
        }

    }

    private void setNotifi(Notifi notifi) {
        WSFirebase.userLocation().child(notifi.getSenderId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserLocation location = dataSnapshot.getValue(UserLocation.class);
                    assert location != null;
                    if (listener != null && dangerListener != null) {
                        listener.trackerLocation(new LatLng(location.getLat(), location.getLon()));
                        dangerListener.trackerLocation(new LatLng(location.getLat(), location.getLon()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DangerActivity.this, "Error", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });

        WSFirebase.user().child(notifi.getSenderId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (listener != null && dangerListener != null) {
                        listener.trackerProfile(user);
                        dangerListener.trackerProfile(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
    }

    public void setDangerDataListener(DangerDataListener listener) {
        this.listener = listener;
    }

    public void setDangerListener(DangerListener listener) {
        this.dangerListener = listener;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        try {
            this.listener = (DangerDataListener) fragment;
        }catch (Exception e){
            Log.d(TAG, "onAttachFragment: " + e.getMessage());
        }
        try {
            this.dangerListener = (DangerListener) fragment;
        }catch (Exception e){
            Log.d(TAG, "onAttachFragment: " + e.getMessage());
        }
    }

    @Override
    public void onAttachFragment(android.app.Fragment fragment) {

    }

    private class PageChangeAdapter extends FragmentStatePagerAdapter {

        public PageChangeAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new LocationFragment();
                case 1:
                    return new TrackerProfile();
            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Location";
                case 1:
                    return "Profile";
            }
            return null;
        }

    }



}
