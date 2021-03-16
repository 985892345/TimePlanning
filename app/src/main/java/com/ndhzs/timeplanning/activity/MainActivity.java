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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.adapter.DayVpAdapter;
import com.ndhzs.timeplanning.adapter.TimeVpAdapter;
import com.ndhzs.timeplanning.db.Dates;
import com.ndhzs.timeplanning.httpservice.SendNetRequest;
import com.ndhzs.timeplanning.weight.drawerlayout.InDrawerLayout;
import com.ndhzs.timeplanning.weight.timeselectview.bean.TaskBean;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager2 mDayVp;
    private ViewPager2 mTimePlanVp;
    private DayVpAdapter mDayVpAdapter;
    private TimeVpAdapter mTimeVPAdapter;
    private ImageView mHeadImg;
    private TextView mExplainTv, mSetTv, mDonateTv, mNoIdeaTv;
    private SharedPreferences mShared;
    private SharedPreferences.Editor mEditor;
    private final List<String[]> mDates = new ArrayList<>();
    private final List<String[]> mCalender = new ArrayList<>();
    private final List<String[]> mRectDays = new ArrayList<>();
    private final List<List<TaskBean>> mEveryDayData = new ArrayList<>();

    private int mCurrentWeekPage;
    private int mCurrentWeek;
    private int mDiffDate;
    private String firstOpenWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LitePal.initialize(this);
        new InDrawerLayout(this);
        mShared = getSharedPreferences("data", MODE_PRIVATE);
        loadData();
        initView();
        initEvent();
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
        int d = mDiffDate/7;
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < 7; j++) {
                Dates dates = new Dates();
                int p = mDates.size() + i - d;
                Log.d(TAG, "onDestroy: p = " + p);
                dates.setDate(mDates.get(p)[j]);
                dates.setRest(mRectDays.get(p)[j]);
                dates.setCalender(mCalender.get(p)[j]);
                dates.save();
            }
        }
    }

    private void loadData() {
        boolean isFirstOpen = mShared.getBoolean("isFirstOpen", true);
        mEditor = mShared.edit();

        if (isFirstOpen) {
            Toast.makeText(this, "第一次打开需要加载日历数据，请稍等", Toast.LENGTH_LONG).show();
            mEditor.putBoolean("isFirstOpen", false);
            String nowDay = getNowDay();//这是第一次使用软件的那天日期
            mEditor.putString("firstOpenDate", nowDay);
            String[] loadWeek = getAllOfWeek();
            firstOpenWeek = loadWeek[0];//这是第一次使用软件的那周的第一天日期
            mEditor.putString("firstOpenWeek", firstOpenWeek);
            mEditor.putString("loadWeek", loadWeek[0]);
            mDiffDate = 14;//这个用来控制viewpager的item刷新数量
            sendHolidayNet(loadWeek);//第一次打开软件加载14天的数据
            sendCalendarNet(loadWeek);
            for (int i = 0; i < 2; i++) {
                String[] dates = new String[7];
                String[] calender = new String[7];
                String[] rectDays = new String[7];
                for (int j = 0; j < 7; j++) {
                    dates[j] = loadWeek[7 * i + j].split("-")[2];
                }
                mDates.add(dates);
                mCalender.add(calender);
                mRectDays.add(rectDays);
            }
            for (int i = 0; i < 14; i++) {
                mEveryDayData.add(new ArrayList<>());
            }
            mCurrentWeekPage = 0;
            mCurrentWeek = getDiffDate(firstOpenWeek, nowDay);//周日到周六分别对应0 ~ 6
        }else {

            //把之前的数据都读取出来
            List<Dates> allDates = LitePal.findAll(Dates.class);
            for (int i = 0; i < allDates.size()/7; i++) {
                mDates.add(new String[7]);
                mCalender.add(new String[7]);
                mRectDays.add(new String[7]);
            }
            for (int i = 0; i < allDates.size(); i++) {
                List<TaskBean> taskBeans = LitePal.where("dates_id=?", i+"").find(TaskBean.class);
                mEveryDayData.add(taskBeans);
                int j = i / 7;
                int k = i % 7;
                mDates.get(j)[k] = allDates.get(i).getDate();
                mRectDays.get(j)[k] = allDates.get(i).getRest();
                mCalender.get(j)[k] = allDates.get(i).getCalender();
            }


            String nowDay = getNowDay();
            mEditor.putString("lastOpenDate", nowDay);
            //这是上一次使用网络加载的第一天日期
            String preloadWeek = mShared.getString("loadWeek", "2021-2-14");
            mDiffDate = getDiffDate(preloadWeek, nowDay);
            String[] loadWeeks = getAllOfWeek();
            String newLoadWeek = loadWeeks[0];//这是本周第一天日期
            mEditor.putString("loadWeek", newLoadWeek);
            if (mDiffDate >= 7 && mDiffDate < 14) {
                //如果是这个判断，说明你上个星期打开过，那就加载下个星期的数据
                String[] dates = new String[7];//下个星期的号数
                String[] calender = new String[7];
                String[] rectDays = new String[7];
                String[] sendNetArray = new String[7];//用来发送请求的下个星期的数组
                for (int i = 0; i < 7; i++) {
                    sendNetArray[i] = loadWeeks[i + 7];
                    dates[i] = loadWeeks[i + 7].split("-")[2];
                    mEveryDayData.add(new ArrayList<>());
                }
                mDates.add(dates);
                mCalender.add(calender);
                mRectDays.add(rectDays);
                sendHolidayNet(sendNetArray);
                sendCalendarNet(sendNetArray);
            }else if (mDiffDate >= 14 && mDiffDate < 21) {
                //如果是这个判断，说明你有一个星期没打开过了
                for (int i = 0; i < 2; i++) {
                    String[] dates = new String[7];
                    String[] calender = new String[7];
                    String[] rectDays = new String[7];
                    for (int j = 0; j < 7; j++) {
                        dates[j] = loadWeeks[7 * i + j].split("-")[2];
                    }
                    mDates.add(dates);
                    mCalender.add(calender);
                    mRectDays.add(rectDays);
                }
                for (int i = 0; i < 14; i++) {
                    mEveryDayData.add(new ArrayList<>());
                }
                sendHolidayNet(loadWeeks);
                sendCalendarNet(loadWeeks);
            }else if (mDiffDate >= 21) {
                // 如果是这个判断，说明你有两个星期没打开过了
                // 由于在写这句时临近21号，我就暂时不写这块了，以后来补充
                // 还有一个原因是因为我的key是免费的，一天只能加载100次，你一个人用还行
                // 用的人多了就不行了，所以这里就这样暂时不写了 :)
            }
            //这是第一次使用软件的那周的第一天日期
            firstOpenWeek = mShared.getString("firstOpenWeek", "2021-2-14");
            int diffDateToFirstWeek = getDiffDate(firstOpenWeek, nowDay);
            mCurrentWeekPage = diffDateToFirstWeek / 7;
            mCurrentWeek = diffDateToFirstWeek % 7;
        }
        mEditor.apply();
    }

    /**
     * 用于发送节假日请求的方法
     * @param s 格式为：2021-2-20的数组
     */
    private void sendHolidayNet(String[] s) {
        HolidayHandler holidayHandler = new HolidayHandler(this);
        StringBuilder url = new StringBuilder("https://timor.tech/api/holiday/batch?");
        for (String st : s) {
            url.append("d=").append(st).append("&");
        }
        new SendNetRequest(holidayHandler).sendGetNetRequest(url.substring(0, url.length() - 1));
    }

    /**
     * 用于发送农历请求的方法
     * @param s 格式为：2021-2-20的数组
     */
    private void sendCalendarNet(String[] s) {
        CalendarHandler calendarHandler = new CalendarHandler(this);
        new Thread() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < s.length; i++) {
                        sleep(100);//这个接口在瞬间请求大量数据时会不返回
                        /*
                         * 这个key每天只能请求100次 :(
                         * */
                        new SendNetRequest(calendarHandler).sendGetNetRequest("https://v.juhe.cn/calendar/day?key=d3baf1e03b20a9bd4b49a0e80e66d925&date=" + s[i]);
                    }
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initView() {
        mDayVp = findViewById(R.id.vp_day_view);
        mTimePlanVp = findViewById(R.id.vp_time_view);
        mHeadImg = findViewById(R.id.img_left_head);
        mExplainTv = findViewById(R.id.tv_left_explain);
        mSetTv = findViewById(R.id.tv_left_set);
        mDonateTv = findViewById(R.id.tv_left_donate);
        mNoIdeaTv = findViewById(R.id.tv_left_no_idea);

        mDayVpAdapter = new DayVpAdapter(mDates, mRectDays, mCalender, mCurrentWeekPage, mCurrentWeek);
        int showTimeLinePosition = mCurrentWeekPage * 7 + mCurrentWeek;
        mTimeVPAdapter = new TimeVpAdapter(this, mTimePlanVp, showTimeLinePosition, mEveryDayData);
    }
    private void initEvent() {
        mDayVp.setAdapter(mDayVpAdapter);
        mDayVp.setCurrentItem(mCurrentWeekPage);

        mTimePlanVp.setAdapter(mTimeVPAdapter);
        mTimePlanVp.setCurrentItem(mCurrentWeekPage * 7 + mCurrentWeek);
        mTimePlanVp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mDayVp.setCurrentItem(position / 7);
                mDayVpAdapter.setWeekPosition(position % 7);
            }
        });

        mDayVpAdapter.setOnWeekClickListener(new DayVpAdapter.OnWeekClickListener() {
            @Override
            public void onWeekClick(int position) {
                mTimePlanVp.setCurrentItem(position);
            }
        });

        mHeadImg.setOnClickListener(this);
        mExplainTv.setOnClickListener(this);
        mSetTv.setOnClickListener(this);
        mDonateTv.setOnClickListener(this);
        mNoIdeaTv.setOnClickListener(this);
    }

    /**
     *
     * @return 当前时间，格式为：2021-2-20
     */
    private String getNowDay() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        return year + "-" + month + "-" + day;
    }

    /**
     * 获取两个日期之间的间隔天数
     * @return 两个日期之间的间隔天数
     */
    private int getDiffDate(String start, String end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
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

    /**
     * 以当前时间算出当前周和下一周的日期
     * @return 当前周和下一周的日期
     */
    private String[] getAllOfWeek(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String[] s = new String[14];
        for (int i = 0; i < 7; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, i + 1);
            Date day = calendar.getTime();
            s[i] = sdf.format(day);
        }
        calendar.add(Calendar.DATE, 7);
        for (int i = 0; i < 7; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, i + 1);
            Date day = calendar.getTime();
            s[i + 7] = sdf.format(day);
        }
        return s;
    }

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
     * 获取节假日的handler
     */
    private static class HolidayHandler extends Handler {
        private final WeakReference<MainActivity> weakReference;
        private final int SUCCEED = 0;
        public HolidayHandler(MainActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            MainActivity activity = weakReference.get();
            if (activity != null) {
                JSONObject js = (JSONObject) msg.obj;
                try {
                    int errorCode = js.getInt("code");
                    if (errorCode == SUCCEED) {
                        JSONObject holidayJS = js.getJSONObject("holiday");
                        Iterator<String> keys = holidayJS.keys();
                        while (keys.hasNext()) {
                            String n = keys.next();
                            if (!holidayJS.isNull(n)) {
                                JSONObject dayJS = holidayJS.getJSONObject(n);
                                boolean isRect = dayJS.getBoolean("holiday");
                                String date = dayJS.getString("date");
                                int diffDate = activity.getDiffDate(activity.firstOpenWeek, date);
                                if (isRect) {
                                    activity.mRectDays.get(diffDate / 7)[diffDate % 7] = "休";
                                } else {
                                    activity.mRectDays.get(diffDate / 7)[diffDate % 7] = "班";
                                }
                            }
                        }
                        activity.mDayVpAdapter.setRectDays(activity.mDiffDate/7);
                    }else {
                        Toast.makeText(activity, "节假日服务器出错！", Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e) {
                    Toast.makeText(activity, "Json读取出错！", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * 获取农历的handler
     */
    private static class CalendarHandler extends Handler {
        private final WeakReference<MainActivity> weakReference;
        private int i = 0;
        private final int SUCCEED = 0;
        private CalendarHandler(MainActivity activity) {
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
                    if (errorCode == SUCCEED) {
                        JSONObject resultJS = js.getJSONObject("result");
                        JSONObject dataJS = resultJS.getJSONObject("data");
                        String calender = dataJS.getString("holiday");
                        if (calender.equals("")) {
                            calender = dataJS.getString("lunar").substring(2);
                        }
                        String date = dataJS.getString("date");
                        int diffDate = activity.getDiffDate(activity.firstOpenWeek, date);
                        activity.mCalender.get(diffDate / 7)[diffDate % 7] = calender;
                        i++;
                        if (i == 7) {//这个接口获取单天数据只能一天一天的获取
                            i = 0;
                            activity.mDayVpAdapter.setCalender(activity.mDiffDate/7);
                        }
                    }else {
                        Toast.makeText(activity, "抱歉，农历请求超过今日限制", Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e) {
                    Toast.makeText(activity, "Json读取出错！", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public static final String TAG ="123";
}