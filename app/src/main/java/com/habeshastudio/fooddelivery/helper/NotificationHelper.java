package com.habeshastudio.fooddelivery.helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.habeshastudio.fooddelivery.R;

public class NotificationHelper extends ContextWrapper {

    private static final String DERASH_CHANNEL_ID = "com.habeshastudio.derash.HabeshaStudio";
    private static final String DERASH_CHANNEL_NAME = "Derash";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel derashChannel = new NotificationChannel(DERASH_CHANNEL_ID,
                DERASH_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        derashChannel.enableLights(false);
        derashChannel.enableVibration(true);
        derashChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(derashChannel);
    }

    public NotificationManager getManager() {
        if (manager == null)
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getDerashChannelNotification(String title, String body, PendingIntent contentIntent,
                                                             Uri soundUri) {
        return new Notification.Builder(getApplicationContext(), DERASH_CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_restaurant_black_24dp)
                .setSound(soundUri)
                .setAutoCancel(false);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getDerashChannelNotification(String title, String body,
                                                             Uri soundUri) {
        return new Notification.Builder(getApplicationContext(), DERASH_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_restaurant_black_24dp)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
}
