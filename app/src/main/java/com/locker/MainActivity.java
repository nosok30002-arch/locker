package com.locker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
        } else {
            startService(new Intent(this, LockService.class));
            finish();
        }
    }
    @Override protected void onResume() {
        super.onResume();
        if (Settings.canDrawOverlays(this)) {
            startService(new Intent(this, LockService.class));
            finish();
        }
    }
}
