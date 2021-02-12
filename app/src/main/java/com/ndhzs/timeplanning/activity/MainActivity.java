package com.ndhzs.timeplanning.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.adapter.LeftTimeVPAdapter;
import com.ndhzs.timeplanning.adapter.RightTimeVPAdapter;
import com.ndhzs.timeplanning.callback.VpLinkCallback;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 mLeftTimeVP;
    private ViewPager2 mRightTimeVP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }

    private void initView() {
        mLeftTimeVP = findViewById(R.id.vp_left);
        mRightTimeVP = findViewById(R.id.vp_right);
    }
    private void initEvent() {
        LeftTimeVPAdapter leftTimeVPAdapter = new LeftTimeVPAdapter();
        RightTimeVPAdapter rightTimeVPAdapter = new RightTimeVPAdapter();
        mLeftTimeVP.setAdapter(leftTimeVPAdapter);
        mRightTimeVP.setAdapter(rightTimeVPAdapter);
        leftTimeVPAdapter.setSelfViewPager(mLeftTimeVP);
        leftTimeVPAdapter.setLinkViewPager(mRightTimeVP);
        rightTimeVPAdapter.setSelfViewPager(mRightTimeVP);
        rightTimeVPAdapter.setLinkViewPager(mLeftTimeVP);
//        mLeftTimeVP.registerOnPageChangeCallback(new VpLinkCallback(mLeftTimeVP, mRightTimeVP));
//        mRightTimeVP.registerOnPageChangeCallback(new VpLinkCallback(mRightTimeVP, mLeftTimeVP));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mLeftTimeVP.requestDisallowInterceptTouchEvent(true);
//                mRightTimeVP.requestDisallowInterceptTouchEvent(true);
//                break;
//        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: ACTIVITY_MOVE==========================");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: ACTIVITY_UP============================");
                break;
        }
        return super.onTouchEvent(event);
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