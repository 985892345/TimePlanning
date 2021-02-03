package com.ndhzs.timeplanning.myview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.ndhzs.timeplanning.R;

public class TimeSelectView extends ScrollView {

    private RectView rectView;
    private final int mIntervalHeight;//一个小时的间隔高度
    private final int mIntervalLeft;//左边的文字间隔宽度
    private final int mExtraHeight;//上方或下方其中一方多余的高度
    private final int mBorderColor;//矩形边框颜色
    private final int mInsideColor;//矩形内部颜色
    private final int mTimeTextSide;//时间字体大小
    private final int mTaskTextSize;//任务字体大小
    private int mInitialX, mInitialY;//计入ACTION_DOWN时的坐标

    public TimeSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ty = context.obtainStyledAttributes(attrs, R.styleable.TimeSelectView);
        mBorderColor = ty.getColor(R.styleable.TimeSelectView_borderColor, 0xFFFF0000);
        mInsideColor = ty.getColor(R.styleable.TimeSelectView_insideColor, 0xFFDCCC48);
        mIntervalLeft = (int)ty.getDimension(R.styleable.TimeSelectView_intervalWidth, 126);
        mIntervalHeight = (int)ty.getDimension(R.styleable.TimeSelectView_intervalHeight, 136);
        mTimeTextSide = (int)ty.getDimension(R.styleable.TimeSelectView_timeTextSize, mIntervalLeft *0.36f);
        mTaskTextSize = (int)ty.getDimension(R.styleable.TimeSelectView_taskTextSize, mIntervalHeight*0.38f);
        mExtraHeight = (int)(mIntervalHeight * 0.5);
        ty.recycle();
        setVerticalScrollBarEnabled(false);
        init(context);
    }

    private void init(Context context) {
        int startHour = 3;
        int endHour = 24 + 3;
        rectView = new RectView(context);
        rectView.setHour(startHour, endHour);
        rectView.setRectColor(mBorderColor, mInsideColor);
        rectView.setTextSize(mTimeTextSide, mTaskTextSize);
        rectView.setInterval(mIntervalLeft, mIntervalHeight, mExtraHeight);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(rectView, lp);
    }

    private boolean mIsLongPre;
    private boolean mIsFinishJudge;
    private boolean mIsIntervalLeft;
    private static final int DISTANCE_THRESHOLD = 20;
    private final Runnable mLongPreRun = new Runnable() {
        @Override
        public void run() {
            mIsLongPre = true;
            rectView.longPress(mInitialX, mInitialY + getScrollY());
        }
    };
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int)ev.getX();
        int y = (int)ev.getY();
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mInitialX = x;
            mInitialY = y;
            mIsLongPre = false;//刷新
            if (mInitialX < mIntervalLeft + 3) {
                mIsFinishJudge = true;
                mIsIntervalLeft = true;
                return super.dispatchTouchEvent(ev);
            }
            mIsFinishJudge = false;//刷新
            mIsIntervalLeft = false;//刷新
            postDelayed(mLongPreRun, 600);
            return super.dispatchTouchEvent(ev);
        }else if (action == MotionEvent.ACTION_MOVE) {
            if (mIsFinishJudge)
                return super.dispatchTouchEvent(ev);
            if (!mIsLongPre) {
                if (Math.abs(x - mInitialX) > DISTANCE_THRESHOLD || Math.abs(y - mInitialY) > DISTANCE_THRESHOLD) {
                    mIsFinishJudge = true;
                    removeCallbacks(mLongPreRun);
                    return super.dispatchTouchEvent(ev);
                }else {
                    // 经过几个小时打log，我把View和ViewGroup的事件分发、
                    // 事件拦截和事件处理都打印了出来，这里return true可以
                    // 终止事件向下传递，意思就是onInterceptTouchEvent()
                    // 和onTouchEvent将会收不到这个事件，将不会被调用，
                    // 所以这里刚好可以用来等待长按时间结束
                    return true;
                }
            }else {
                mIsFinishJudge = true;
                return super.dispatchTouchEvent(ev);
            }
        }else if (action == MotionEvent.ACTION_UP) {
            removeCallbacks(mLongPreRun);
            return super.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsIntervalLeft) {
            super.onInterceptTouchEvent(ev);
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            onTouchEvent(ev);
            return false;
        }
        if (!mIsLongPre) {
            super.onInterceptTouchEvent(ev);
            return true;
        }
        return false;
    }
}
