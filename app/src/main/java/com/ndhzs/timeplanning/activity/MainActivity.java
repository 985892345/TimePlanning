package com.ndhzs.timeplanning.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.adapter.DayVpAdapter;
import com.ndhzs.timeplanning.adapter.TimeVpAdapter;
import com.ndhzs.timeplanning.weight.MyDrawerLayout;
import com.ndhzs.timeplanning.weight.timeselectview.bean.TaskBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager2 mDayVp;
    private ViewPager2 mTimePlanVp;
    private ImageView mHeadImg;
    private TextView mExplainTv, mSetTv, mDonateTv, mNoIdeaTv;
    private SharedPreferences mShared;
    private SharedPreferences.Editor mEditor;
    private final List<String[][]> mDays = new ArrayList<>();
    private final List<HashSet<TaskBean>> mEveryDayData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new MyDrawerLayout(this);
        mShared = getSharedPreferences("data", MODE_PRIVATE);
        loadData();
        initView();
        initEvent();
    }

    private void loadData() {
        boolean isFirstOpen = mShared.getBoolean("isFirstOpen", false);
        Calendar calendar = Calendar.getInstance();
        mEditor = mShared.edit();
        if (isFirstOpen) {
            mEditor.putBoolean("isFirstOpen", true);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DATE);
            mEditor.putString("firstOpenDate", year + "-" + month + "-" + day);

            // 这里加载1-31到3-6的数据
            // 用免费的接口加载日期、节假日，用另一个获取农历


        }else {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DATE);
            String lastOpenDate = year + "-" + month + "-" + day;
            mEditor.putString("lastOpenDate", lastOpenDate);
            String firstOpenDate = mShared.getString("firstOpenDate", "2021-2-17");
            int diffDate = getDiffDate(firstOpenDate, lastOpenDate);
            if (diffDate > 30) {
                //这里重新加载数据
            }
        }
        mEditor.apply();
    }

    private void initView() {
        mDayVp = findViewById(R.id.vp_day_view);
        mTimePlanVp = findViewById(R.id.vp_time_view);
        mHeadImg = findViewById(R.id.img_left_head);
        mExplainTv = findViewById(R.id.tv_left_explain);
        mSetTv = findViewById(R.id.tv_left_set);
        mDonateTv = findViewById(R.id.tv_left_donate);
        mNoIdeaTv = findViewById(R.id.tv_left_no_idea);
    }
    private void initEvent() {

        for (int i = 0; i < 12; i++) {
            mEveryDayData.add(new HashSet<>());
        }
//        for (int i = 0; i < 5; i++) {
//        }
        DayVpAdapter dayVpAdapter = new DayVpAdapter(mDays);
        mDayVp.setAdapter(dayVpAdapter);

        TimeVpAdapter timeVPAdapter = new TimeVpAdapter(this, mTimePlanVp, mEveryDayData);
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

    /**
     * 获取两个日期之间的间隔天数
     * @return 两个日期之间的间隔天数
     */
    private static int getDiffDate(String start, String end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            Calendar fromCalendar = Calendar.getInstance();
            fromCalendar.setTime(startDate);
            fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            fromCalendar.set(Calendar.MILLISECOND, 0);

            Calendar toCalendar = Calendar.getInstance();
            toCalendar.setTime(endDate);
            toCalendar.set(Calendar.HOUR_OF_DAY, 0);
            toCalendar.set(Calendar.MINUTE, 0);
            toCalendar.set(Calendar.SECOND, 0);
            toCalendar.set(Calendar.MILLISECOND, 0);
            return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
        }catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static class MHandler extends Handler {

        private final WeakReference<MainActivity> weakReference;
        private final int SUCCEED = 0;
        private final int FAIL_1 = 10003;//KEY过期
        private final int FAIL_2 = 10005;//应用未审核超时，请提交认证
        private final int FAIL_3 = 10008;//被禁止的IP
        private final int FAIL_4 = 10009;//被禁止的KEY
        private final int FAIL_5 = 10011;//当前IP请求超过限制
        private final int FAIL_6 = 10012;//请求超过次数限制
        private final int FAIL_7 = 10013;//测试KEY超过请求限制
        private final int FAIL_8 = 10014;//系统内部异常
        private final int FAIL_9 = 10020;//接口维护

        public MHandler(MainActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            MainActivity activity = weakReference.get();
            if (activity != null) {
                JSONObject js = (JSONObject) msg.obj;
                try {
                    int errorCode = js.getInt("error_code");
                    switch (errorCode) {
                        case SUCCEED:
                            break;
                        case FAIL_1:
                            Toast.makeText(activity, "KEY过期！", Toast.LENGTH_LONG).show();
                            break;
                        case FAIL_2:
                            Toast.makeText(activity, "应用未审核超时，请提交认证！", Toast.LENGTH_LONG).show();
                            break;
                        case FAIL_3:
                            Toast.makeText(activity, "被禁止的IP！", Toast.LENGTH_LONG).show();
                            break;
                        case FAIL_4:
                            Toast.makeText(activity, "被禁止的KEY！", Toast.LENGTH_LONG).show();
                            break;
                        case FAIL_5:
                            Toast.makeText(activity, "当前IP请求超过限制！", Toast.LENGTH_LONG).show();
                            break;
                        case FAIL_6:
                            Toast.makeText(activity, "请求超过次数限制！", Toast.LENGTH_LONG).show();
                            break;
                        case FAIL_7:
                            Toast.makeText(activity, "测试KEY超过请求限制！", Toast.LENGTH_LONG).show();
                            break;
                        case FAIL_8:
                            Toast.makeText(activity, "系统内部异常！", Toast.LENGTH_LONG).show();
                            break;
                        case FAIL_9:
                            Toast.makeText(activity, "接口维护！", Toast.LENGTH_LONG).show();
                            break;
                    }
                }catch (JSONException e) {
                    Toast.makeText(activity, "Json读取出错！", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}