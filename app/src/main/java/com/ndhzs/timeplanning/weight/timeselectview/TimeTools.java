package com.ndhzs.timeplanning.weight.timeselectview;

import android.util.Log;

import java.util.Calendar;

public class TimeTools {

    private static int sHLineWidth;//水平线厚度
    private static int sExtraHeight;//上方或下方其中一方多余的高度
    private static int sIntervalHeight;//一个小时的间隔高度
    private static int sStartHour;
    private static int sTopTimeHour;
    private static int sTopTimeMinute;
    private static int sBottomTimeHour;
    private static int sBottomTimeMinute;
    /**
     * 保存0~60的每分钟的相对高度，0分钟就是[0]，60分钟就是[60]。使用时一般配合 HLineTopHeight一起使用
     */
    static final float[] sEveryMinuteHeight = new float[61];
    public static final int DELAY_RUN_TIME = 30000;//刷新当前时间高度的间隔时间
    public static int START_TIME_INTERVAL = 15;//按下空白区域时起始时间的分钟间隔数(必须为60的因数)

    public static void loadData(int hLineWidth, int extraHeight, int intervalHeight, int startHour) {
        sHLineWidth = hLineWidth;
        sExtraHeight = extraHeight;
        sIntervalHeight = intervalHeight;
        sStartHour = startHour;
        float everyMinuteWidth = intervalHeight / 60.0f;//计算出一分钟要多少格，用小数表示
        for (int i = 0; i < 60; i++) {
            sEveryMinuteHeight[i] = i * everyMinuteWidth;
        }
        sEveryMinuteHeight[60] = intervalHeight;
    }
    public static void refreshData(int startHour) {
        sStartHour = startHour;
    }

    public static String getTime(int y) {
        int h = getHour(y);
        int m = getMinute(y);
        return getStringTime(h, m);
    }
    public static String getDiffTime(int top, int bottom) {
        int lastH = getHour(top);
        int lastM = getMinute(top);
        int h = getHour(bottom);
        int m = getMinute(bottom);
        if (m >= lastM) {
            m -= lastM;
            h -= lastH;
        }else {
            m += 60 - lastM;
            h -= lastH + 1;
        }
        return getStringTime(h, m);
    }
    public static String getTopTime(int y) {
        sTopTimeHour = getHour(y);
        sTopTimeMinute = getMinute(y);
        return getStringTime(sTopTimeHour, sTopTimeMinute);
    }
    public static String getBottomTime(String dTime) {
        int dH = Integer.parseInt(dTime.substring(0, 2));
        int dM = Integer.parseInt(dTime.substring(3));
        sBottomTimeHour = sTopTimeHour + dH;
        sBottomTimeMinute = sTopTimeMinute + dM;
        if (sBottomTimeMinute >= 60) {
            sBottomTimeHour++;
            sBottomTimeMinute -= 60;
        }
        return getStringTime(sBottomTimeHour, sBottomTimeMinute);
    }
    public static int getBottomTimeHeight(int top, String dTime) {//本方法是在RectImgView放置过后才调用的
        top += sExtraHeight;//转换坐标系
        getTopTime(top);
        getBottomTime(dTime);//重新计算sBottomTimeHour和sBottomTimeMinute，原因在于划出上下边界，图形会回来，要重新计算时间
        // 后面加个1是为了粗略计算，最后的高度还要到RectView中的getEndTimeCorrectHeight(int bottom)进行精确计算
        // 里面的sBottomTimeHour和sBottomTimeMinute是重新计算后的值
        return (sBottomTimeHour - sStartHour)* sIntervalHeight - sHLineWidth + (int)sEveryMinuteHeight[sBottomTimeMinute] + 1;
    }

    public static float getNowTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return hour + minute/60.0f + second/3600.0f;
    }
    public static int getNowTimeHeight() {
        float nowTime = getNowTime();
        if (nowTime < sStartHour) {
            nowTime += 24;
        }
        return (int) (sExtraHeight + (nowTime - sStartHour) * sIntervalHeight);
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
    public static int getHLineTopHeight(int y) {
        return  (y - sExtraHeight + sHLineWidth) / sIntervalHeight * sIntervalHeight + sExtraHeight - sHLineWidth;
    }

    private static int getHour(int y) {
        int hour = ((y - sExtraHeight + sHLineWidth) / sIntervalHeight) + sStartHour;
        if (y < sExtraHeight)
            return sStartHour - 1;
        return hour;
    }

    /**
     * 计算当前y值对应的分钟数，不会算出60分钟
     * @param y 传入y值(注意！此y值为加上了ExtraHeight的坐标系对应的y值，若在RectView中调用，必须加入ExtraHeight)
     * @return 当前y值对应的分钟数
     */
    public static int getMinute(int y) {
        //先计算出一格占多少分钟
        int minute = (int)(((y - sExtraHeight + sHLineWidth) % sIntervalHeight) / (float) sIntervalHeight * 60);
        if (y < sExtraHeight)
            return (int)(((sIntervalHeight - (sExtraHeight - sHLineWidth - y)) % sIntervalHeight) / (float) sIntervalHeight * 60);
        return minute;
    }

    public static void setStartTimeInterval(int startTimeInterval) {
        START_TIME_INTERVAL = startTimeInterval;
    }

    private static String getStringTime(int hour, int minute) {
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
}
