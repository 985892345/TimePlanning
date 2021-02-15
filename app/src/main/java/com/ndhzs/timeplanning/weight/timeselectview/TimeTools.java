package com.ndhzs.timeplanning.weight.timeselectview;

import android.util.Log;

import java.util.Calendar;

public class TimeTools {

    private final int mHLineWidth;//水平线厚度
    private final int mExtraHeight;//上方或下方其中一方多余的高度
    private final int mIntervalHeight;//一个小时的间隔高度
    private final int mStartHour;
    private int mTopTimeHour;
    private int mTopTimeMinute;
    private int mBottomTimeHour;
    private int mBottomTimeMinute;
    /**
     * 保存0~60的每分钟的相对高度，0分钟就是[0]，60分钟就是[60]。使用时一般配合 HLineTopHeight一起使用
     */
    public final float[] mEveryMinuteHeight = new float[61];
    public static final int DELAY_NOW_TIME_REFRESH = 30000;//刷新当前时间高度的间隔时间
    public static final int DELAY_BACK_CURRENT_TIME = 10000;//回到当前时间的延缓时间
    public int TIME_INTERVAL = 15;//按下空白区域时起始时间的分钟间隔数(必须为60的因数)

    public TimeTools(int hLineWidth, int extraHeight, int intervalHeight, int startHour) {
        this.mHLineWidth = hLineWidth;
        this.mExtraHeight = extraHeight;
        this.mIntervalHeight = intervalHeight;
        this.mStartHour = startHour;
        float everyMinuteWidth = intervalHeight / 60.0f;//计算出一分钟要多少格，用小数表示
        for (int i = 0; i < 60; i++) {
            mEveryMinuteHeight[i] = i * everyMinuteWidth;
        }
        mEveryMinuteHeight[60] = intervalHeight;
    }

    /**
     * RectView坐标系，禁止其他使用
     * @param startTime 开始时间
     * @return 已减去mExtraHeight
     */
    public int getTopHeight(String startTime) {//开始加载数据时使用
        mTopTimeHour = Integer.parseInt(startTime.substring(0, 2));
        mTopTimeMinute = Integer.parseInt(startTime.substring(3));
        return (mTopTimeHour - mStartHour) * mIntervalHeight - mHLineWidth + (int)Math.ceil(mEveryMinuteHeight[mTopTimeMinute + 1]) - 1;
    }

    /**
     * RectView坐标系，禁止其他使用
     * @param dTime 时间差
     * @return 已减去mExtraHeight
     */
    public int getBottomHeight(String dTime) {//开始加载数据时使用
        int dH = Integer.parseInt(dTime.substring(0, 2));
        int dM = Integer.parseInt(dTime.substring(3));
        mBottomTimeHour = mTopTimeHour + dH;
        mBottomTimeMinute = mTopTimeMinute + dM;
        if (mBottomTimeMinute >= 60) {
            mBottomTimeHour++;
            mBottomTimeMinute -= 60;
        }
        return (mBottomTimeHour - mStartHour) * mIntervalHeight - mHLineWidth + (int)Math.ceil(mEveryMinuteHeight[mBottomTimeMinute]);
    }

    /**
     * RectView坐标系，禁止其他使用
     * @param y 已加上mExtraHeight
     * @return 该y值对应时间
     */
    public String getTime(int y) {
        int h = getHour(y + mExtraHeight);
        int m = getMinute(y + mExtraHeight);
        return getStringTime(h, m);
    }
    /**
     * RectView坐标系，禁止其他使用
     * @param top 已加上mExtraHeight
     * @param bottom 已加上mExtraHeight
     * @return 时间差
     */
    public String getDiffTime(int top, int bottom) {
        int lastH = getHour(top + mExtraHeight);
        int lastM = getMinute(top + mExtraHeight);
        int h = getHour(bottom + mExtraHeight);
        int m = getMinute(bottom + mExtraHeight);
        if (m >= lastM) {
            m -= lastM;
            h -= lastH;
        }else {
            m += 60 - lastM;
            h -= lastH + 1;
        }
        return getStringTime(h, m);
    }

    /**
     * ScrollView坐标系
     * @param y 该y值对应时间
     * @return 时间
     */
    public String getTopTime(int y) {//RectImgView中与getBottomTime(String dTime)方法一起使用，得到正确的顶部时间
        mTopTimeHour = getHour(y);
        mTopTimeMinute = getMinute(y);
        return getStringTime(mTopTimeHour, mTopTimeMinute);
    }

    /**
     * ScrollView坐标系
     * @param dTime 时间差
     * @return 时间
     */
    public String getBottomTime(String dTime) {//RectImgView中与getTopTime(int y)方法一起使用，用时间差得到正确的底部时间
        int dH = Integer.parseInt(dTime.substring(0, 2));
        int dM = Integer.parseInt(dTime.substring(3));
        mBottomTimeHour = mTopTimeHour + dH;
        mBottomTimeMinute = mTopTimeMinute + dM;
        if (mBottomTimeMinute >= 60) {
            mBottomTimeHour++;
            mBottomTimeMinute -= 60;
        }
        return getStringTime(mBottomTimeHour, mBottomTimeMinute);
    }

    /**
     * RectView坐标系，禁止其他使用
     * @param top 已加上mExtraHeight
     * @param dTime 已加上mExtraHeight
     * @return 已减去mExtraHeight
     */
    public int getBottomTimeHeight(int top, String dTime) {//本方法是在RectImgView放置过后在RectView中调用，通过时间差得下边界
        getTopTime(top + mExtraHeight);
        getBottomTime(dTime);//重新计算mBottomTimeHour和mBottomTimeMinute，原因在于划出上下边界，图形会回来，要重新计算边界时间
        /*
         * 后面用ceil为了粗略计算，例如：(计算bottom是要计算当前分钟最 顶 部的那根线)
         *        一、该分钟线高为 12.5 我要得到这一分钟的最顶部的线，就为13，用ceil取13即可
         *        二、该分钟线高为 12.0 我要得到这一分钟的最顶部的线，就为12，用ceil取12即可
         * 最后的bottom高度还要到RectView中的getEndTimeCorrectHeight(int bottom)进行精确计算
         * 里面的mBottomTimeHour和sBottomTimeMinute是重新计算后的值
         * 减mHLineWidth时为了转换为RectView的坐标系，因为RectView是从mExtraHeight开始的
         * */
        return (mBottomTimeHour - mStartHour) * mIntervalHeight - mHLineWidth + (int)Math.ceil(mEveryMinuteHeight[mBottomTimeMinute]);
    }

    /**
     * RectView坐标系，禁止其他使用
     * @param bottom 已加上mExtraHeight
     * @param dTime 已加上mExtraHeight
     * @return 已减去mExtraHeight
     */
    public int getTopTimeHeight(int bottom, String dTime) {//本方法是在RectImgView放置过后在RectView中调用，通过时间差得上边界
        mBottomTimeHour = getHour(bottom + mExtraHeight);
        mBottomTimeMinute = getMinute(bottom + mExtraHeight);
        int dH = Integer.parseInt(dTime.substring(0, 2));
        int dM = Integer.parseInt(dTime.substring(3));
        mTopTimeHour = mBottomTimeHour - dH;
        mTopTimeMinute = mBottomTimeMinute - dM;
        if (mTopTimeMinute < 0) {
            mTopTimeHour--;
            mTopTimeMinute += 60;
        }
        /*
         * 后面用ceil为了粗略计算，例如：(计算top是要计算当前分钟最 底 部的那根线)
         *        我要取2分钟的最后一根线，则我可以去3分钟的顶部线再减1
         *        一、3分钟线高为 12.5 我要得到这一分钟的最顶部的线，用ceil取大于等于12.5的数13，再减 1 得到2分钟的最后一根线
         *        二、3分钟线高为 12.0 我要得到这一分钟的最顶部的线，就为12，用ceil取12，再减 1 得到2分钟的最后一根线
         * 最后的bottom高度还要到RectView中的getEndTimeCorrectHeight(int bottom)进行精确计算
         * 里面的mBottomTimeHour和mBottomTimeMinute是重新计算后的值
         * 减mHLineWidth时为了转换为RectView的坐标系，因为RectView是从mExtraHeight开始的
         * */
        return (mTopTimeHour - mStartHour) * mIntervalHeight - mHLineWidth + (int)Math.ceil(mEveryMinuteHeight[mTopTimeMinute + 1]) - 1;
    }

    public float getNowTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return hour + minute/60.0f + second/3600.0f;
    }
    public int getNowTimeHeight() {
        float nowTime = getNowTime();
        if (nowTime < mStartHour) {
            nowTime += 24;
        }
        return (int) (mExtraHeight + (nowTime - mStartHour) * mIntervalHeight);
    }

    /**
     * 计算当前y值对应的一小时间隔内的水平线顶部对应高度
     *                                                  /--------0分钟的起始点
     *                                                /
     * HLineTopHeight         -------------------------------    ------------
     * HLineBottomHeight      -------------------------------      *
     *                            /                                *
     *       ExtraHeight -------/                                  *
     * (ExtraHeight是最顶部的 HLineBottomHeight)                    --->是这个区域内的y值，但不包括NextHLineTopHeight
     * (IntervalHeight = NextHLineTopHeight - HLineTopHeight)      *
     *           = NextHLineBottomHeight - HLineBottomHeight)      *
     *                                                             *
     * NextHLineTopHeight     -------------------------------    ------------y为此线时得到NextHLineTopHeight
     * NextHLineBottomHeight  -------------------------------      *
     *                                                             *
     *                                                             *
     *                                                             --->这个区域的y值算下一条线，会返回NextHLineTopHeight
     *                                                             *
     *    60分钟点，但它的上一格为59分钟---------\                   *
     *                                         \                  *
     * NextNextTopHeight      -------------------------------  ---------------y为此线时得到NextNextTopHeight
     * NextNextBottomHeight   -------------------------------
     * @param y 传入y值(注意！此y值为加上了ExtraHeight的坐标系对应的y值，若在RectView中调用，必须加入ExtraHeight)
     * @return 当前y值对应的区域的水平线顶部对应高度
     */
    public int getHLineTopHeight(int y) {
        return  (y + mHLineWidth) / mIntervalHeight * mIntervalHeight - mHLineWidth;
    }

    private int getHour(int y) {
        int hour = ((y - mExtraHeight + mHLineWidth) / mIntervalHeight) + mStartHour;
        if (y < mExtraHeight - mHLineWidth)
            return mStartHour - ((mExtraHeight - mHLineWidth - y) / mIntervalHeight + 1);
        return hour;
    }

    /**
     * ScrollView坐标系
     * 计算当前y值对应的分钟数，不会算出60分钟
     * @param y 传入y值(注意！此y值为加上了ExtraHeight的坐标系对应的y值，若在RectView中调用，必须加入ExtraHeight)
     * @return 当前y值对应的分钟数
     */
    public int getMinute(int y) {
        //通过计算出一格占多少分钟来计算
        int minute = (int)(((y - mExtraHeight + mHLineWidth) % mIntervalHeight) / (float) mIntervalHeight * 60);
        if (y < mExtraHeight - mHLineWidth) {//因为有一个额外高度，时间计算方式要更改
            return (int) ((mIntervalHeight - (mExtraHeight - mHLineWidth - y) % mIntervalHeight) / (float) mIntervalHeight * 60);
        }
        return minute;
    }

    private String getStringTime(int hour, int minute) {
        String stH;
        String stM;
        if (hour < 10) {
            stH = "0" + hour;
        }else if (hour < 24){
            stH = hour + "";
        }else {
            stH = "0" + hour % 24;
        }
        if (minute < 10) {
            stM = "0" + minute;
        }else {
            stM = minute + "";
        }
        return stH + ":" + stM;
    }

    private static final String TAG = "123";
}
