package com.ndhzs.timeplanning.db;


import com.ndhzs.timeplanning.weight.timeselectview.bean.TaskBean;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

public class Dates extends LitePalSupport {

    private String date;
    private String calender;
    private String rest;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCalender() {
        return calender;
    }

    public void setCalender(String calender) {
        this.calender = calender;
    }

    public String getRest() {
        return rest;
    }

    public void setRest(String rest) {
        this.rest = rest;
    }
}
