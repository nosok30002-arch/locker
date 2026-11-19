package com.locker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.VideoView;

public class LockActivity extends Activity {

    private VideoView video;
    private AudioManager am;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_lock);

        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        video = findViewById(R.id.lockVideo);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lockvideo);
        video.setVideoURI(uri);
        video.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(1f, 1f);
            video.start();
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.KEYCODE_VOLUME_UP || k == KeyEvent.KEYCODE_VOLUME_DOWN
                || k == KeyEvent.KEYCODE_POWER || k == KeyEvent.KEYCODE_BACK
                || k == KeyEvent.KEYCODE_HOME || k == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.dispatchKeyEvent(e);
    }

    @Override
    public void onBackPressed() { }

    @Override
    protected void onPause() {
        super.onPause();
        startActivity(getIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
                                    }
