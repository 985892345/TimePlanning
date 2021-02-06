package com.ndhzs.timeplanning.myview;

import java.util.Calendar;

public class MyTime {

    private static int sHLineWidth;//水平线厚度
    private static int sExtraHeight;//上方或下方其中一方多余的高度
    private static int sIntervalHeight;//一个小时的间隔高度
    private static int sStartHour;
    public static final float[] sEveryMinuteHeight = new float[61];//保存0~60的每分钟的相对高度

    public static void loadData(int hLineWidth, int extraHeight, int intervalHeight, int startHour) {
        sHLineWidth = hLineWidth;
        sExtraHeight = extraHeight;
        sIntervalHeight = intervalHeight;
        sStartHour = startHour;
        float everyMinuteWidth = 1 / 60.0f * intervalHeight;//计算出一分钟要多少格，用小数表示
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

    public static float getNowTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return hour + minute/60.0f + second/3600.0f;
    }

    public static int getHLineTopHeight(int y) {
        return  (y - sExtraHeight + sHLineWidth) / sIntervalHeight * sIntervalHeight + sExtraHeight - sHLineWidth;
    }

    private static int getHour(int y) {
        int hour = ((y - sExtraHeight + sHLineWidth) / sIntervalHeight) + sStartHour;
        if (y < sExtraHeight)
            return sStartHour - 1;
        return hour;
    }
    private static int getMinute(int y) {
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
