package com.scand.realmbrowser;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

/**
 * Created by Slabodeniuk on 2/18/16.
 */
public class RealmBrowserService extends Service {

    private static final int NOTIFICATION_ID = 9696;

    public static void startService(Context context) {
        Intent intent = new Intent(context, RealmBrowserService.class);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String appName = getApplicationInfo()
                .loadLabel(getPackageManager()).toString();
        Intent notifyIntent = new Intent(this, BrowserActivity.class);

        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.realm_browser_notification_icon)
                .setColor(getResources().getColor(R.color.realm_browser_notification_color))
                .setContentTitle(TextUtils.isEmpty(appName)
                        ? getString(R.string.realm_browser_notification_title)
                        : appName)
                .setContentText(getString(R.string.realm_browser_notification_text))
                .setAutoCancel(false)
                .setLocalOnly(true)
                .setContentIntent(notifyPendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }
}
