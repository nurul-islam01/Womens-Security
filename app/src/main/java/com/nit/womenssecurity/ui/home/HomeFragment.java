package com.nit.womenssecurity.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.pojos.DeviceToken;
import com.nit.womenssecurity.pojos.Notifi;
import com.nit.womenssecurity.pojos.User;
import com.nit.womenssecurity.pojos.UserLocation;
import com.nit.womenssecurity.utils.WSFirebase;
import com.nit.womenssecurity.utils.WSPreference;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.nit.womenssecurity.services.LocationUpdateService.AUTHORIZATION_KEY;
import static com.nit.womenssecurity.services.LocationUpdateService.CATEGORY;
import static com.nit.womenssecurity.services.LocationUpdateService.DANGER;
import static com.nit.womenssecurity.services.LocationUpdateService.NOTIFICATION_ID;
import static com.nit.womenssecurity.services.LocationUpdateService.maxDistance;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private Context context;
    private WSPreference preference;
    private User user;

    private RequestQueue requestQueue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    Button pressingBT;

    @Override
    public void onStart() {
        super.onStart();
        requestQueue = Volley.newRequestQueue(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        pressingBT = root.findViewById(R.id.pressingBT);




        pressingBT.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch ( arg1.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        //start timer
                        countDownTimer.start();

                        return true;
                    case MotionEvent.ACTION_UP:
                        //stop timer
                        countDownTimer.cancel();
                        pressingBT.setText("Press");
                        return true;
                }
                return false;
            }
        });

        return root;
    }

    CountDownTimer countDownTimer = new CountDownTimer(5000, 100) {
        @Override
        public void onTick(long millisUntilFinished) {
            pressingBT.setText(String.valueOf(millisUntilFinished));
        }

        @Override
        public void onFinish() {
            pressingBT.setText("Complete");
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        preference = new WSPreference(context);
        user = preference.getUser();
    }


}
