package com.locker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class LockService extends Service {

    private Handler handler = new Handler();
    private Runnable runnable;
    public static boolean isAppOpen = false; 
    public static boolean isUnlocked = false; 
    private WindowManager windowManager;
    private View overlayView;

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
                if (!isUnlocked && !isAppOpen) {
                    Intent lock = new Intent(LockService.this, LockActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            .putExtra("RETURNING_FROM_EXIT", true);
                    startActivity(lock);
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 500);

        return START_STICKY;
    }

    public void createDeadScreen() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager == null) return;

        overlayView = new View(this);
        overlayView.setBackgroundColor(0xFF000000);

        int layoutType;
        if (Build.VERSION.SDK_INT >= 26) {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;

        try {
            windowManager.addView(overlayView, params);
        } catch (Exception ignored) {}
    }

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception ignored) {}
        }
    }
    }
