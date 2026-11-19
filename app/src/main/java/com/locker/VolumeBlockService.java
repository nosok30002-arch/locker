package com.locker;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class VolumeBlockService extends AccessibilityService {
    @Override 
    public void onAccessibilityEvent(AccessibilityEvent e) {
        // Можно принудительно возвращать LockActivity на передний план
        // но лучше это делать через LockActivity.onPause()
    }

    @Override 
    public void onInterrupt() {}

    @Override
    protected boolean onKeyEvent(KeyEvent e) {
        int k = e.getKeyCode();
        // Блокируем громкость, домой, назад, обзор
        return k == KeyEvent.KEYCODE_VOLUME_UP 
            || k == KeyEvent.KEYCODE_VOLUME_DOWN
            || k == KeyEvent.KEYCODE_BACK
            || k == KeyEvent.KEYCODE_HOME
            || k == KeyEvent.KEYCODE_APP_SWITCH;
    }
}
