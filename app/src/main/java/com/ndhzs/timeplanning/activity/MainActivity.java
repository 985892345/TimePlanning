package com.ndhzs.timeplanning.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.adapter.TimeVPAdapter;
import com.ndhzs.timeplanning.weight.MyDrawerLayout;
import com.ndhzs.timeplanning.weight.timeselectview.bean.TaskBean;

import java.util.HashMap;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager2 mTimePlanVp;
    private HashMap<Integer, HashSet<TaskBean>> mEveryDayData = new HashMap<>();
    private ImageView mHeadImg;
    private TextView mExplainTv, mSetTv, mDonateTv, mNoIdeaTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new MyDrawerLayout(this);
        initView();
        initEvent();
    }

    private void initView() {
        mTimePlanVp = findViewById(R.id.vp_time_view);
        mHeadImg = findViewById(R.id.img_left_head);
        mExplainTv = findViewById(R.id.tv_left_explain);
        mSetTv = findViewById(R.id.tv_left_set);
        mDonateTv = findViewById(R.id.tv_left_donate);
        mNoIdeaTv = findViewById(R.id.tv_left_no_idea);
    }
    private void initEvent() {
        for (int i = 0; i < 12; i++) {
            mEveryDayData.put(i, new HashSet<>());
        }
        TimeVPAdapter timeVPAdapter = new TimeVPAdapter(this, mTimePlanVp, mEveryDayData);
        mTimePlanVp.setAdapter(timeVPAdapter);

        mHeadImg.setOnClickListener(this);
        mExplainTv.setOnClickListener(this);
        mSetTv.setOnClickListener(this);
        mDonateTv.setOnClickListener(this);
        mNoIdeaTv.setOnClickListener(this);
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_left_head:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_left_explain:
                Intent intent2 = new Intent(this, ExplainActivity.class);
                startActivity(intent2);
                break;
            case R.id.tv_left_set:
                Intent intent3 = new Intent(this, SetActivity.class);
                startActivity(intent3);
                break;
            case R.id.tv_left_donate:
                Toast.makeText(this, "作业不能加入第三方库，就没实现该功能", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_left_no_idea:
                Toast.makeText(this, "这个不知道写什么比较好", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}