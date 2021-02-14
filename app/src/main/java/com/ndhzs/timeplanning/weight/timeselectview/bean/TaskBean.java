package com.ndhzs.timeplanning.weight.timeselectview.bean;

import java.util.ArrayList;
import java.util.List;

public class TaskBean {

    private String mStartTime;
    private String mDiffTime;
    private String mName;
    private String mDescribe;
    private int mBorderColor;
    private int mInsideColor;
    private final List<Integer> mRepeatDay = new ArrayList<>();

    public TaskBean(String startTime, String diffTime) {
        this.mStartTime = startTime;
        this.mDiffTime = diffTime;
    }

    public String getStartTime() {
        return mStartTime;
    }
    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    public String getDiffTime() {
        return mDiffTime;
    }
    public void setDiffTime(String diffTime) {
        mDiffTime = diffTime;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }

    public String getDescribe() {
        return mDescribe;
    }
    public void setDescribe(String describe) {
        mDescribe = describe;
    }

    public int getBorderColor() {
        return mBorderColor;
    }
    public void setBorderColor(int borderColor) {
        mBorderColor = borderColor;
    }

    public int getInsideColor() {
        return mInsideColor;
    }
    public void setInsideColor(int insideColor) {
        mInsideColor = insideColor;
    }

    public List<Integer> getRepeatDay() {
        return mRepeatDay;
    }
}
