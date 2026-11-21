package com.locker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

public class LockService extends Service {

    private Handler handler = new Handler();
    private Runnable runnable;
    public static boolean isUnlocked = false; 

    @Override
    public int onStartCommand(Intent i, int f, int s) {
        SharedPreferences prefs = getSharedPreferences("locker_prefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean("is_unlocked_by_user", false)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        NotificationChannel ch = new NotificationChannel("lk", "sys",
                NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .createNotificationChannel(ch);

        Notification n = new Notification.Builder(this, "lk")
                .setContentTitle("System")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .build();
        startForeground(1, n);

        if (!isUnlocked) {
            Intent lock = new Intent(this, LockActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(lock);
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                if (!isUnlocked) {
                    // Принудительно возвращаем экран без проверок
                    Intent lock = new Intent(LockService.this, LockActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .putExtra("RETURNING_FROM_EXIT", true);
                    startActivity(lock);
                }
                handler.postDelayed(this, 200); // Проверяем каждые 200 миллисекунд
            }
        };
        handler.postDelayed(runnable, 200);

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
