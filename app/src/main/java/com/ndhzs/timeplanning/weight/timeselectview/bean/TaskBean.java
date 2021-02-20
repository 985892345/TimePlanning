package com.ndhzs.timeplanning.weight.timeselectview.bean;

import org.litepal.crud.LitePalSupport;

public class TaskBean extends LitePalSupport {

    private int dates_id;
    private int year;
    private int month;
    private int day;
    private int week;

    private String startTime;
    private String diffTime;
    private String name;
    private String describe;
    private int borderColor;
    private int insideColor;

    public int getDates_id() {
        return dates_id;
    }
    public void setDates_id(int dates_id) {
        this.dates_id = dates_id;
    }

    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }
    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }
    public void setDay(int day) {
        this.day = day;
    }

    public int getWeek() {
        return week;
    }
    public void setWeek(int week) {
        this.week = week;
    }

    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getDiffTime() {
        return diffTime;
    }
    public void setDiffTime(String diffTime) {
        this.diffTime = diffTime;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }
    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public int getBorderColor() {
        return borderColor;
    }
    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public int getInsideColor() {
        return insideColor;
    }
    public void setInsideColor(int insideColor) {
        this.insideColor = insideColor;
    }
}
