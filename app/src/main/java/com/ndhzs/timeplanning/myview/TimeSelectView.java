package com.ndhzs.timeplanning.myview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.ndhzs.timeplanning.R;

public class TimeSelectView extends ScrollView {

    private Context context;
    private RectView mRectView;
    private int mStartHour = 3;
    private int mEndHour = 24 + 3;

    private final int mIntervalLeft;//左边的文字间隔宽度
    private final int mExtraHeight;//上方或下方其中一方多余的高度
    private final int mIntervalHeight;//一个小时的间隔高度
    private final int mBorderColor;//矩形边框颜色
    private final int mInsideColor;//矩形内部颜色
    private final int mTimeTextSide;//时间字体大小
    private final int mTaskTextSize;//任务字体大小
    private int mInitialX, mInitialY;//计入ACTION_DOWN时的坐标

    private float mCenterTime;


    /**
     * 不建议用addView()调用，因为我不打算开放一些设置字体大小、矩形颜色的set方法。
     * 调用后会以默认值绘制图形
     * @param context 传入context
     */
    public TimeSelectView(Context context) {
        this(context, null);
    }
    public TimeSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray ty = context.obtainStyledAttributes(attrs, R.styleable.TimeSelectView);
        mBorderColor = ty.getColor(R.styleable.TimeSelectView_borderColor, 0xFFFF0000);
        mInsideColor = ty.getColor(R.styleable.TimeSelectView_insideColor, 0xFFDCCC48);
        mIntervalLeft = (int)ty.getDimension(R.styleable.TimeSelectView_intervalWidth, 110);
        mIntervalHeight = (int)ty.getDimension(R.styleable.TimeSelectView_intervalHeight, 180);
        mTimeTextSide = (int)ty.getDimension(R.styleable.TimeSelectView_timeTextSize, 40);
        mTaskTextSize = (int)ty.getDimension(R.styleable.TimeSelectView_taskTextSize, 45);
        mExtraHeight = (int)(mIntervalHeight * 0.5);
        mCenterTime = ty.getFloat(R.styleable.TimeSelectView_centerTime, MyTime.getNowTime());
        ty.recycle();
        setCenterTime(mCenterTime);
        MyTime.loadData(TimeFrameView.HORIZONTAL_LINE_WIDTH, mExtraHeight, mIntervalHeight, mStartHour);
        setVerticalScrollBarEnabled(false);
        initLayout(context);
    }

    private void initLayout(Context context) {
        FrameLayout layoutParent = new FrameLayout(context);

        LayoutParams lpLayoutParent = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(layoutParent, lpLayoutParent);

        ChildFrameLayout layoutChild= new ChildFrameLayout(context);
        LayoutParams lpLayoutChild = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParent.addView(layoutChild, lpLayoutChild);

        NowTimeView nowTimeView = new NowTimeView(context);
        nowTimeView.setInterval(mIntervalLeft, TimeFrameView.INTERVAL_RIGHT);
        LayoutParams lpNowTimeView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParent.addView(nowTimeView, lpNowTimeView);

        mRectView = new RectView(context);
        LayoutParams lpRectView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lpRectView.leftMargin = mIntervalLeft;
        lpRectView.topMargin = mExtraHeight;
        lpRectView.rightMargin = TimeFrameView.INTERVAL_RIGHT;
        lpRectView.bottomMargin = mExtraHeight + TimeFrameView.HORIZONTAL_LINE_WIDTH;
        mRectView.setChildFrameLayout(layoutChild);
        mRectView.setRectColor(mBorderColor, mInsideColor);
        mRectView.setTextSize(mTimeTextSide, mTaskTextSize);
        mRectView.setInterval(mExtraHeight);
        layoutChild.addView(mRectView, lpRectView);

        TimeFrameView timeFrameView = new TimeFrameView(context);
        LayoutParams lpTimeFrameView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        timeFrameView.setHour(mStartHour, mEndHour);
        timeFrameView.setTextSize(mTimeTextSide);
        timeFrameView.setInterval(mIntervalLeft, TimeFrameView.INTERVAL_RIGHT, mExtraHeight, mIntervalHeight);
        layoutChild.addView(timeFrameView, lpTimeFrameView);
        layoutChild.setRectView(mRectView);
        layoutChild.setInterval(mIntervalLeft, mExtraHeight);
    }

    private boolean mIsLongPress;
    private boolean mIsFinishJudge;
    private boolean mIsIntervalLeft;
    private static final int MOVE_THRESHOLD = 15;//识别是长按而能移动的阀值
    private final Runnable mLongPressRun = new Runnable() {
        @Override
        public void run() {
            mIsLongPress = true;
            Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(15);
            mRectView.longPress(mInitialX, mInitialY + getScrollY() - mExtraHeight);
        }
    };

    /*
    * ScrollView的dispatchTouchEvent()、onInterceptTouchEvent()、onTouchEvent()
    * 三个方法中的ev.getY(), 都是得到ScrollView外部的高度, 不是内部子View的高度,
    * 此情况与ScrollView的getHeight()相同，都会得到ScrollView的外部高度
    *
    * */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int)ev.getX();
        int y = (int)ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialX = x;
                mInitialY = y;
                mIsLongPress = false;//刷新
                if (mInitialX < mIntervalLeft + 3) {
                    mIsFinishJudge = true;
                    mIsIntervalLeft = true;
                    return super.dispatchTouchEvent(ev);
                }
                mIsFinishJudge = false;//刷新
                mIsIntervalLeft = false;//刷新
                postDelayed(mLongPressRun, 250);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsFinishJudge)
                    return super.dispatchTouchEvent(ev);
                if (!mIsLongPress) {
                    if (Math.abs(x - mInitialX) > MOVE_THRESHOLD || Math.abs(y - mInitialY) > MOVE_THRESHOLD) {
                        mIsFinishJudge = true;
                        removeCallbacks(mLongPressRun);
                    }else {
                        /*
                        * 经过几个小时打log, 我把View和ViewGroup的事件分发、
                        * 事件拦截和事件处理都打印了出来, 这里return true可以
                        * 终止事件向下传递, 意思就是onInterceptTouchEvent()
                        * 和onTouchEvent将会收不到这个事件, 将不会被调用,
                        * 所以这里刚好可以用来等待长按时间结束。
                        *
                        * 如果你想在子View的onTouchEvent()中判断, 就会出一个问题
                        * 一旦子View的onTouchEvent()的DOWN事件return true,
                        * 而你在子View的MOVE事件中想不拦截，把事件给ScrollView处理,
                        * 那么理所当然你会在子View的MOVE事件中return false,
                        * 你会以为这样ScrollView会收到子View传来的MOVE事件
                        * 那你就大错特错了, 子View的onTouchEvent()的DOWN事件
                        * return true的前提下, 在子View的MOVE事件中return false
                        * 会直接越级将MOVE事件传递给Activity, 不会再经过ScrollView
                        *
                        * */
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                removeCallbacks(mLongPressRun);
                break;
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
            if (mInitialY < mExtraHeight || mInitialY > getHeight() - mExtraHeight - 5) {
                removeCallbacks(mLongPressRun);
                return true;
            }

            /*
            * 如果不在DOWN事件手动调用onTouchEvent(), ScrollView就不会移动,
            * 因为子View的onTouchEvent()已经把DOWN事件拦截了,
            *
            * */
            onTouchEvent(ev);
            return false;
        }
        if (!mIsLongPress) {
            super.onInterceptTouchEvent(ev);
            return true;
        }
        return false;
    }

    /**
     * 若传入的时间处于上下边界附近无法居中的位置，则会使时间线处于顶部或尾部界面内，但不居中。
     * 若不调用该方法，则会自动以当前时间居中。
     * @param centerTime 设置居中的时间，支持小数。
     */
    public void setCenterTime(float centerTime) {
        this.mCenterTime = centerTime;
        post(new Runnable() {
            @Override
            public void run() {
                float time = mCenterTime;
                int parentHalfHeight = getHeight()/2;
                time %= 24;
                if (time < mStartHour) {
                    time += 24;
                }
                int y = (int)(mExtraHeight + (time - mStartHour) * mIntervalHeight);
                int height = (mEndHour - mStartHour) * mIntervalHeight + 2 * mExtraHeight;
                if (y > parentHalfHeight && y < height - parentHalfHeight) {
                    scrollTo(0, y - parentHalfHeight);
                }else if (y >= height - parentHalfHeight) {
                    scrollTo(0, height - parentHalfHeight * 2);
                }
            }
        });
    }

    public int getSlippage() {
        return getScrollY();
    }
}
