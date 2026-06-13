package com.smstofred;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class KeepAliveService extends Service {
    private static final String CHANNEL_ID = "sms_call_channel";
    private static final int NOTIF_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIF_ID, createNotification());
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SMS/Call Forwarder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Уведомления о работе приложения");
            channel.enableVibration(true);
            channel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
            return new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("SMS/Call Forwarder")
                    .setContentText("Активен, перехват работает")
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .build();
        } else {
            return new Notification.Builder(this)
                    .setContentTitle("SMS/Call Forwarder")
                    .setContentText("Активен, перехват работает")
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .build();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
