package com.ndhzs.timeplanning.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ndhzs.timeplanning.R;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Window window = this.getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        TextView textView = findViewById(R.id.version);
        PackageManager packageManager = this.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(),0);
            textView.setText(("V " + packageInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            textView.setText("无法获取版本号");
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}