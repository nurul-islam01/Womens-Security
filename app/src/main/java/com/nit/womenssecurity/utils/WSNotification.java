package com.nit.womenssecurity.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nit.womenssecurity.R;
import com.nit.womenssecurity.activity.DangerActivity;
import com.nit.womenssecurity.activity.MainActivity;

import static com.nit.womenssecurity.activity.MainActivity.CHANNEL_ID;


public class WSNotification {

    public static void showNotification(Context context, String title, String content, Intent intent) {
        Uri notificationSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.danger_sms);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 141, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.danger_skull)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setDefaults(Notification.DEFAULT_VIBRATE);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(1, builder.build());
    }

    public static void showRequired(Context context, String title, String des, int icon) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 141, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Uri notificationSound = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(des)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSound(notificationSound);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(1, builder.build());

    }


}
