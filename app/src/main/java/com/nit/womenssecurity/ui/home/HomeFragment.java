package com.nit.womenssecurity.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.activity.SettingActivity;
import com.nit.womenssecurity.pojos.Contact;
import com.nit.womenssecurity.pojos.User;
import com.nit.womenssecurity.ui.adapter.ContactAdapter;
import com.nit.womenssecurity.utils.TrackingActivator;
import com.nit.womenssecurity.utils.WSPreference;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.Context.LOCATION_SERVICE;


public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    public static final String MESSAGE_RECEIVER = "com.nit.womenssecurity.messagelist";
    public static final String NOTIFICATION_SENDS = "com.nit.womenssecurity.notification_count";
    private Context context;
    private WSPreference preference;
    private User user;

    private RequestQueue requestQueue;
    private Button pressingBT;
    private Handler timerHandler = new Handler();
    private long starttime = 0;
    private int percent = 0;

    private TrackingActivator trackingActivator;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView notificationCountTV;
    private RecyclerView contactRC;
    private TextView smsNotSentTV;
    private Activity activity;
    private SweetAlertDialog alertDialog;
    private LocationManager lm;


    @Override
    public void onStart() {
        super.onStart();
        requestQueue = Volley.newRequestQueue(context);
        lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);

    }


    @Override
    public void onStop() {
        super.onStop();
        context.unregisterReceiver(mMessageReceiver);
        context.unregisterReceiver(notificationReceive);
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        alertDialog = new SweetAlertDialog(context);
        pressingBT = root.findViewById(R.id.pressingBT);
        progressBar = root.findViewById(R.id.progressBar);
        progressText = root.findViewById(R.id.progressText);
        contactRC = root.findViewById(R.id.contactRC);
        notificationCountTV = root.findViewById(R.id.notificationCountTV);
        smsNotSentTV = root.findViewById(R.id.smsNotSentTV);

        pressingBT.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (checkMessagePermission() && isLocationServiceEnable()) {
                    switch ( arg1.getAction() ) {
                        case MotionEvent.ACTION_DOWN:
                            //start timer
                            percent = 0;
                            progressBar.setProgress(percent);
                            starttime = System.currentTimeMillis();
                            timerHandler.postDelayed(timer, 0);
                            pressingBT.setText("Loading...");
                            return true;
                        case MotionEvent.ACTION_UP:
                            //stop timer
                            timerHandler.removeCallbacks(timer);

                            if (percent < 100) {
                                pressingBT.setText("Press");
                                setProgress(0);
                            }
                            return true;
                    }
                } else {
                    showLocationDialog();
                }
                return false;
            }
        });



        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        context.registerReceiver(mMessageReceiver, new IntentFilter(MESSAGE_RECEIVER));
        context.registerReceiver(notificationReceive, new IntentFilter(NOTIFICATION_SENDS));

    }


    private boolean isLocationServiceEnable() {
        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            return false;
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
                Toast.makeText(context, "Application will not working correctly", Toast.LENGTH_SHORT).show();
            }
        }).show();
    }

   BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getSerializableExtra("smssendinglist") != null) {
                List<Contact> smsReceiverList = (List<Contact>) intent.getSerializableExtra("smssendinglist");
                ContactAdapter adapter = new ContactAdapter(context, smsReceiverList);
                contactRC.setAdapter(adapter);
                contactRC.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);

            }else if (intent.getBooleanExtra("error", true)) {
                Toast.makeText(context, "Message Sending failed", Toast.LENGTH_SHORT).show();
                smsNotSentTV.setVisibility(View.VISIBLE);
                smsNotSentTV.setText("Message Sending failed");
            }

        }
    };

    BroadcastReceiver notificationReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("sendTo", 0) > 0) {
                int count = intent.getIntExtra("sendTo", 0);
                Toast.makeText(context, "Send notification to " + count, Toast.LENGTH_SHORT).show();
                notificationCountTV.setText("Send notification to " + count + " people");
                notificationCountTV.setVisibility(View.VISIBLE);
            }else {
                Toast.makeText(context, "No people found", Toast.LENGTH_SHORT).show();
                notificationCountTV.setText("Notification Sending failed");
                notificationCountTV.setVisibility(View.VISIBLE);
            }
            pressingBT.setText("Loaded");
        }
    };


    private Runnable timer = new Runnable() {
        @Override
        public void run() {

            long millis = System.currentTimeMillis() - starttime;
            int seconds = (int) (millis / 1000);
            seconds %= 60;
            percent = 2 * (seconds*10);
            setProgress(percent);
            if (seconds == 5) {
                timerHandler.removeCallbacks(timer);
                trackingActivator.actionWarning();
                return;
            }
            timerHandler.postDelayed(this, 500);
        }
    };

    private void setProgress(int progress) {
        progressBar.setProgress(progress);
        progressText.setText(progress + "%");
    }

    public boolean checkMessagePermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS);
            if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.SEND_SMS},229);
                return false;
            }else{
               return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        trackingActivator = new TrackingActivator(context);
        preference = new WSPreference(context);
        user = preference.getUser();
    }

}
