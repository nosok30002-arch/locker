package com.locker;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class VolumeBlockService extends AccessibilityService {

    private static boolean isLocked = false;

    public static void setLocked(boolean locked) {
        isLocked = locked;
    }

    @Override 
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (isLocked && e.getPackageName() != null && !e.getPackageName().equals(getPackageName())) {
                Intent intent = new Intent(this, LockActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("RETURNING_FROM_EXIT", true);
                startActivity(intent);
            }
        }
    }

    @Override 
    public void onInterrupt() { }

    @Override
    protected boolean onKeyEvent(KeyEvent e) {
        int k = e.getKeyCode();
        return k == KeyEvent.KEYCODE_VOLUME_UP 
            || k == KeyEvent.KEYCODE_VOLUME_DOWN
            || k == KeyEvent.KEYCODE_BACK
            || k == KeyEvent.KEYCODE_HOME
            || k == KeyEvent.KEYCODE_APP_SWITCH;
    }
}
