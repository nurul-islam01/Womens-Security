package com.nit.womenssecurity.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.activity.adapter.NotificationAdapter;
import com.nit.womenssecurity.pojos.Notifi;
import com.nit.womenssecurity.pojos.User;
import com.nit.womenssecurity.utils.WSFirebase;
import com.nit.womenssecurity.utils.WSPreference;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import me.leolin.shortcutbadger.ShortcutBadger;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";

    private TextView noNotificationTV;
    private RecyclerView notificationRC;
    private NotificationAdapter adapter;
    private WSPreference preference;
    private User user;
    private ProgressBar spin_kit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        preference = new WSPreference(this);
        user = preference.getUser();

        noNotificationTV = findViewById(R.id.noNotificationTV);
        notificationRC = findViewById(R.id.notificationRC);
        spin_kit = findViewById(R.id.spin_kit);

        DoubleBounce bounce = new DoubleBounce();
        spin_kit.setIndeterminateDrawable(bounce);

        WSFirebase.notifications().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Notifi> notifis = new ArrayList<>();
                    int count = 0;
                    for (DataSnapshot d: dataSnapshot.getChildren()) {
                        Notifi notifi = d.getValue(Notifi.class);
                        if (notifi.getReceiverId().equals(user.getId())) {
                            notifis.add(notifi);
                            if (!notifi.isSeen()) {
                               count = count + 1;
                            }
                        }
                    }
                    if (notifis.size() > 0) {

                        adapter = new NotificationAdapter(NotificationActivity.this, notifis);
                        notificationRC.setAdapter(adapter);

                        noNotificationTV.setVisibility(View.GONE);
                        notificationRC.setVisibility(View.VISIBLE);
                    } else {
                        noNotificationTV.setVisibility(View.VISIBLE);
                        notificationRC.setVisibility(View.GONE);
                    }
                    preference.saveBadge(count);
                    int badgeCount = count;
                    ShortcutBadger.applyCount(NotificationActivity.this, badgeCount);
                    spin_kit.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                spin_kit.setVisibility(View.GONE);
                Toast.makeText(NotificationActivity.this, "Notification loading error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
