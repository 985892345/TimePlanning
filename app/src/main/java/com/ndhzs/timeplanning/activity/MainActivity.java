package com.ndhzs.timeplanning.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.adapter.TimeVPAdapter;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }

    private void initView() {
        mViewPager = findViewById(R.id.viewpager);
    }
    private void initEvent() {
        TimeVPAdapter timeVPAdapter = new TimeVPAdapter(mViewPager);
        mViewPager.setAdapter(timeVPAdapter);
    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }else {
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.d("123", "onDestroy: ");
//        List<Rect> rects = mLeftTimeView.getRects();
//        HashMap<Rect, String> rectAndName = mLeftTimeView.getRectAndName();
//        HashMap<Rect, String> rectAndDTime = mLeftTimeView.getRectAndDTime();
    }

    private final static String TAG = "123";
}