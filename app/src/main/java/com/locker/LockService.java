package com.locker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class LockService extends Service {

    private Handler handler = new Handler();
    private Runnable runnable;
    public static boolean isAppOpen = false; 
    public static boolean isUnlocked = false; // НОВЫЙ ФЛАГ

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

        // Если уже разблокировано, не запускаем экран
        if (!isUnlocked) {
            Intent lock = new Intent(this, LockActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(lock);
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                // Если НЕ разблокировано и приложение закрыто — запускаем
                if (!isUnlocked && !isAppOpen) {
                    Intent lock = new Intent(LockService.this, LockActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            .putExtra("RETURNING_FROM_EXIT", true);
                    startActivity(lock);
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
