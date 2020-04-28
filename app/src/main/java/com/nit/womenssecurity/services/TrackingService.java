package com.nit.womenssecurity.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.nit.womenssecurity.utils.ShakeDetector;

public class TrackingService extends Service implements ShakeDetector.Listener {
    private static final String TAG = "TrackingService";
    private SensorManager manager;
    private ShakeDetector shakeDetector;
    private int counting = 0;
    private boolean alert = false;

    public TrackingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shakeDetector.start(manager);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shakeDetector.stop();
    }

    @Override
    public void hearShake() {

        if (counting > 0 && !alert) {
            Toast.makeText(this, "Alert", Toast.LENGTH_SHORT).show();
            alert = true;
        } else {
            new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) {
                    counting = (int) millisUntilFinished / 1000;
                }

                public void onFinish() {
                    counting = 0;
                    alert = false;
                }

            }.start();
        }

    }
}
