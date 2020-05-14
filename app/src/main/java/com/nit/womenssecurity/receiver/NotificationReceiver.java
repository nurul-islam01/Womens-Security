package com.nit.womenssecurity.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.legacy.content.WakefulBroadcastReceiver;

import com.nit.womenssecurity.activity.DangerActivity;
import com.nit.womenssecurity.activity.MainActivity;
import com.nit.womenssecurity.pojos.Notifi;
import com.nit.womenssecurity.utils.WSNotification;
import com.nit.womenssecurity.utils.WSPreference;

import me.leolin.shortcutbadger.ShortcutBadger;

import static com.nit.womenssecurity.services.LocationUpdateService.CATEGORY;
import static com.nit.womenssecurity.services.LocationUpdateService.DANGER;
import static com.nit.womenssecurity.services.LocationUpdateService.NOTIFICATION_ID;

public class NotificationReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    private WSPreference preference;
    @Override
    public void onReceive(Context context, Intent intent) {
        preference = new WSPreference(context);

        Bundle bundle =  intent.getExtras();


        if (bundle != null) {

            preference.saveBadge(preference.getBadge() + 1);
            int badgeCount = preference.getBadge();
            ShortcutBadger.applyCount(context, badgeCount);

            String id = (String) bundle.get("notification_id");
            String receiverId = (String) bundle.get("receiverId");
            String senderId = (String) bundle.get("senderId");
            long time = Long.parseLong(bundle.get("time").toString());;
            String title =  (String) bundle.get("gcm.notification.title");
            String category = (String) bundle.get("category");
            String body = (String) bundle.get("gcm.notification.body");
            boolean seen = Boolean.parseBoolean(bundle.get("seen").toString());

            Notifi notifi = new Notifi(id, receiverId, senderId, time, title, category, body, seen);

            Intent passIntent = new Intent(context, DangerActivity.class);
            passIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Bundle bn = new Bundle();
            bn.putSerializable("notification", notifi);
            passIntent.putExtras(bn);
            WSNotification.showNotification(context,  title, body, passIntent);

            if (category.equals(DANGER)) {
                Intent in = new Intent(context, DangerActivity.class);
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName(context.getPackageName(), DangerActivity.class.getName());
                context.startActivity(intent);
            }

        }
    }
}
