package com.locker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LockService extends Service {
    @Override
    public int onStartCommand(Intent i, int f, int s) {
        NotificationChannel ch = new NotificationChannel("lk", "sys",
                NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .createNotificationChannel(ch);

        Notification n = new Notification.Builder(this, "lk")
                .setContentTitle("System")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build();
        startForeground(1, n);

        Intent lock = new Intent(this, LockActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(lock);
        return START_STICKY;
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
