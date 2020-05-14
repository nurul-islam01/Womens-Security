package com.nit.womenssecurity.services;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nit.womenssecurity.activity.DangerActivity;
import com.nit.womenssecurity.utils.WSNotification;
import com.nit.womenssecurity.utils.WSPreference;

import java.util.HashMap;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

import static com.nit.womenssecurity.services.LocationUpdateService.CATEGORY;
import static com.nit.womenssecurity.services.LocationUpdateService.DANGER;
import static com.nit.womenssecurity.services.LocationUpdateService.NOTIFICATION_ID;

public class WSMessageService extends FirebaseMessagingService {
    private static final String TAG = "WSMessageService";
    private WSPreference preference;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

            preference = new WSPreference(this);

            String title = remoteMessage.getNotification().getTitle();
            String content = remoteMessage.getNotification().getBody();

            Map<String, String> extraData = remoteMessage.getData();
            String category = extraData.get(CATEGORY);
            Log.d(TAG, "onMessageReceived: " + category);

//            if (category.equals(DANGER)) {
//                String notificationId = extraData.get(NOTIFICATION_ID);
//                Intent intent = new Intent(this, DangerActivity.class);
//                intent.putExtra("notificationId", notificationId);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            } else {
////                WSNotification.showNotification(this, title, content);
//            }


    }

}
