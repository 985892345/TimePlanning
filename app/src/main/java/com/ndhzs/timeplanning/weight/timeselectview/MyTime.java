package com.ndhzs.timeplanning.weight.timeselectview;

import java.util.Calendar;

public class MyTime {

    private static int sHLineWidth;//水平线厚度
    private static int sExtraHeight;//上方或下方其中一方多余的高度
    private static int sIntervalHeight;//一个小时的间隔高度
    private static int sStartHour;
    /**
     * 保存0~60的每分钟的相对高度，0分钟就是[0]，60分钟就是[60]。使用时一般配合 HLineTopHeight一起使用
     */
    static final float[] sEveryMinuteHeight = new float[61];

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

    static String getTime(int y) {
        int h = getHour(y);
        int m = getMinute(y);
        return getStringTime(h, m);
    }
    static String getDiffTime(int top, int bottom) {
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

    public static float getNowTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return hour + minute/60.0f;
    }
    static int getNowTimeHeight() {
        float nowTime = getNowTime();
        int hour = (int) nowTime;
        int minute = (int) ((nowTime - hour) * 60 + 0.5f);
        return sExtraHeight + (hour - 3) * sIntervalHeight + (int)sEveryMinuteHeight[minute];
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
    static int getHLineTopHeight(int y) {
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
    static int getMinute(int y) {
        //先计算出一格占多少分钟
        int minute = (int)(((y - sExtraHeight + sHLineWidth) % sIntervalHeight) / (float) sIntervalHeight * 60);
        if (y < sExtraHeight)
            return (int)(((sIntervalHeight - (sExtraHeight - sHLineWidth - y)) % sIntervalHeight) / (float) sIntervalHeight * 60);
        return minute;
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
