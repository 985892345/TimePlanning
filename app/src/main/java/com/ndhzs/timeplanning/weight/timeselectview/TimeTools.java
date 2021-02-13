package com.ndhzs.timeplanning.weight.timeselectview;

import android.util.Log;

import java.util.Calendar;

public class TimeTools {

    private static int sHLineWidth;//水平线厚度
    private static int sExtraHeight;//上方或下方其中一方多余的高度
    private static int sIntervalHeight;//一个小时的间隔高度
    private static int sTopTimeHour;
    private static int sTopTimeMinute;
    private static int sBottomTimeHour;
    private static int sBottomTimeMinute;
    /**
     * 保存0~60的每分钟的相对高度，0分钟就是[0]，60分钟就是[60]。使用时一般配合 HLineTopHeight一起使用
     */
    static final float[] sEveryMinuteHeight = new float[61];
    public static final int DELAY_NOW_TIME_REFRESH = 30000;//刷新当前时间高度的间隔时间
    public static final int DELAY_BACK_CURRENT_TIME = 10000;//回到当前时间的延缓时间
    public static int TIME_INTERVAL = 15;//按下空白区域时起始时间的分钟间隔数(必须为60的因数)

    public static void loadData(int hLineWidth, int extraHeight, int intervalHeight) {
        sHLineWidth = hLineWidth;
        sExtraHeight = extraHeight;
        sIntervalHeight = intervalHeight;
        float everyMinuteWidth = intervalHeight / 60.0f;//计算出一分钟要多少格，用小数表示
        for (int i = 0; i < 60; i++) {
            sEveryMinuteHeight[i] = i * everyMinuteWidth;
        }
        sEveryMinuteHeight[60] = intervalHeight;
    }

    public static String getTime(int startHour, int y) {
        int h = getHour(startHour, y);
        int m = getMinute(y);
        return getStringTime(h, m);
    }
    public static String getDiffTime(int startHour, int top, int bottom) {
        int lastH = getHour(startHour, top);
        int lastM = getMinute(top);
        int h = getHour(startHour, bottom);
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
    public static String getTopTime(int startHour, int y) {//RectImgView中与getBottomTime(String dTime)方法一起使用，得到正确的顶部时间
        sTopTimeHour = getHour(startHour, y);
        sTopTimeMinute = getMinute(y);
        return getStringTime(sTopTimeHour, sTopTimeMinute);
    }
    public static String getBottomTime(String dTime) {//RectImgView中与getTopTime(int y)方法一起使用，用时间差得到正确的底部时间
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
    public static int getBottomTimeHeight(int startHour, int top, String dTime) {//本方法是在RectImgView放置过后在RectView中调用，通过时间差得下边界
        getTopTime(startHour, top);
        getBottomTime(dTime);//重新计算sBottomTimeHour和sBottomTimeMinute，原因在于划出上下边界，图形会回来，要重新计算边界时间
        /*
         * 后面用ceil为了粗略计算，例如：(计算bottom是要计算当前分钟最 顶 部的那根线)
         *        一、该分钟线高为 12.5 我要得到这一分钟的最顶部的线，就为13，用ceil取13即可
         *        二、该分钟线高为 12.0 我要得到这一分钟的最顶部的线，就为12，用ceil取12即可
         * 最后的bottom高度还要到RectView中的getEndTimeCorrectHeight(int bottom)进行精确计算
         * 里面的sBottomTimeHour和sBottomTimeMinute是重新计算后的值
         * */
        return sExtraHeight + (sBottomTimeHour - startHour) * sIntervalHeight - sHLineWidth + (int)Math.ceil(sEveryMinuteHeight[sBottomTimeMinute]);
    }
    public static int getTopTimeHeight(int startHour, int bottom, String dTime) {//本方法是在RectImgView放置过后在RectView中调用，通过时间差得上边界
        sBottomTimeHour = getHour(startHour, bottom);
        sBottomTimeMinute = getMinute(bottom);
        int dH = Integer.parseInt(dTime.substring(0, 2));
        int dM = Integer.parseInt(dTime.substring(3));
        sTopTimeHour = sBottomTimeHour - dH;
        sTopTimeMinute = sBottomTimeMinute - dM;
        Log.d(TAG, "getTopTimeHeight: =================" + sTopTimeMinute);
        if (sTopTimeMinute < 0) {
            sTopTimeHour--;
            sTopTimeMinute += 60;
        }
        /*
         * 后面用ceil为了粗略计算，例如：(计算top是要计算当前分钟最 底 部的那根线)
         *        我要取2分钟的最后一根线，则我可以去3分钟的顶部线在减1
         *        一、3分钟线高为 12.5 我要得到这一分钟的最顶部的线，用ceil取大于等于12.5的数13，再减 1 得到2分钟的最后一根线
         *        二、3分钟线高为 12.0 我要得到这一分钟的最顶部的线，就为12，用ceil取12，再减 1 得到2分钟的最后一根线
         * 最后的bottom高度还要到RectView中的getEndTimeCorrectHeight(int bottom)进行精确计算
         * 里面的sBottomTimeHour和sBottomTimeMinute是重新计算后的值
         * */
        Log.d(TAG, "getTopTimeHeight: " + sTopTimeMinute);
        return sExtraHeight + (sTopTimeHour - startHour) * sIntervalHeight - sHLineWidth + (int)Math.ceil(sEveryMinuteHeight[sTopTimeMinute + 1]) -1;
    }

    public static float getNowTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return hour + minute/60.0f + second/3600.0f;
    }
    public static int getNowTimeHeight(int startHour) {
        float nowTime = getNowTime();
        if (nowTime < startHour) {
            nowTime += 24;
        }
        return (int) (sExtraHeight + (nowTime - startHour) * sIntervalHeight);
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

    private static int getHour(int startHour, int y) {
        int hour = ((y - sExtraHeight + sHLineWidth) / sIntervalHeight) + startHour;
        if (y < sExtraHeight - sHLineWidth)
            return startHour - ((sExtraHeight - sHLineWidth - y) / sIntervalHeight + 1);
        return hour;
    }

    /**
     * 计算当前y值对应的分钟数，不会算出60分钟
     * @param y 传入y值(注意！此y值为加上了ExtraHeight的坐标系对应的y值，若在RectView中调用，必须加入ExtraHeight)
     * @return 当前y值对应的分钟数
     */
    public static int getMinute(int y) {
        //通过计算出一格占多少分钟来计算
        int minute = (int)(((y - sExtraHeight + sHLineWidth) % sIntervalHeight) / (float) sIntervalHeight * 60);
        if (y < sExtraHeight - sHLineWidth) {//因为有一个额外高度，时间计算方式要更改
            return (int) ((sIntervalHeight - (sExtraHeight - sHLineWidth - y) % sIntervalHeight) / (float) sIntervalHeight * 60);
        }
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

    private static final String TAG = "123";
}
