package com.ndhzs.timeplanning.myview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.ndhzs.timeplanning.R;

public class TimeSelectView extends ScrollView {

    private RectView rectView;
    private int mScreenWidth, mScreenHeight;
    private final int mIntervalHeight;//一个小时的间隔高度
    private final int mIntervalWidth;//左边的文字间隔宽度
    private final int mExtraHeight;//上方或下方其中一方多余的高度
    private final int mBorderColor;//矩形边框颜色
    private final int mInsideColor;//矩形内部颜色
    private final int mTimeTextSide;//时间字体大小
    private final int mTaskTextSize;//任务字体大小

    public TimeSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ty = context.obtainStyledAttributes(attrs, R.styleable.TimeSelectView);
        mBorderColor = ty.getColor(R.styleable.TimeSelectView_borderColor, 0xFFFF0000);
        mInsideColor = ty.getColor(R.styleable.TimeSelectView_insideColor, 0xFFDCCC48);
        mTimeTextSide = (int)ty.getDimension(R.styleable.TimeSelectView_timeTextSize, 50);
        mTaskTextSize = (int)ty.getDimension(R.styleable.TimeSelectView_taskTextSize, 60);
        mIntervalWidth = (int)ty.getDimension(R.styleable.TimeSelectView_intervalWidth, 126);
        mIntervalHeight = (int)ty.getDimension(R.styleable.TimeSelectView_intervalHeight, 136);
        mExtraHeight = (int)(mIntervalHeight * 0.5);
        ty.recycle();
        setVerticalScrollBarEnabled(false);
        getScreenSize();
        init(context);
    }

    private boolean mIsShortPress = true;
    public void setIsShortPress(boolean isShortPress) {
        this.mIsShortPress = isShortPress;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (ev.getX() < mIntervalWidth + 3) {
                super.onInterceptTouchEvent(ev);
                return true;
            }else {
                onTouchEvent(ev);
            }
        }else if (mIsShortPress) {
            super.onInterceptTouchEvent(ev);
            return true;
        }
        return false;
    }

    private void getScreenSize() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
            mScreenWidth = dm.widthPixels;
            mScreenHeight = dm.heightPixels;
        }else {
            mScreenWidth = ((Activity) getContext()).getWindowManager().getCurrentWindowMetrics().getBounds().width();
            mScreenHeight = ((Activity) getContext()).getWindowManager().getCurrentWindowMetrics().getBounds().height();
        }
    }

    private void init(Context context) {
        int startHour = 3;
        int endHour = 24 + 3;
        rectView = new RectView(context);
        rectView.setHour(startHour, endHour);
        rectView.setRectColor(mBorderColor, mInsideColor);
        rectView.setTimeTextSize(mTimeTextSide, mTaskTextSize);
        rectView.setInterval(mIntervalWidth, mIntervalHeight, mExtraHeight);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(rectView, lp);
    }
}
