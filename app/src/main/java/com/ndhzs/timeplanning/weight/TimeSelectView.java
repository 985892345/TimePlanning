package com.ndhzs.timeplanning.weight;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ScrollView;;
import androidx.core.view.ViewCompat;
import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.timeselectview.ChildFrameLayout;
import com.ndhzs.timeplanning.weight.timeselectview.MyTime;
import com.ndhzs.timeplanning.weight.timeselectview.NowTimeView;
import com.ndhzs.timeplanning.weight.timeselectview.RectView;
import com.ndhzs.timeplanning.weight.timeselectview.TimeFrameView;

import java.util.HashMap;
import java.util.List;

public class TimeSelectView extends ScrollView {

    private Context context;
    private IRectView mIRectView;
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
    private static final int MAX_AUTO_SLIDE_VELOCITY = 10;
    private onScrollViewListener mOnScrollViewListener;
    public List<Rect> getRects() {
        return mIRectView.getRects();
    }
    public HashMap<Rect, String> getRectAndName() {
        return mIRectView.getRectAndName();
    }
    public HashMap<Rect, String> getRectAndDTime() {
        return mIRectView.getRectAndDTime();
    }
    public void setOnScrollViewListener(onScrollViewListener onScrollViewListener) {
        this.mOnScrollViewListener = onScrollViewListener;
    }

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
        mIntervalHeight = (int)ty.getDimension(R.styleable.TimeSelectView_intervalHeight, 194);
        mTimeTextSide = (int)ty.getDimension(R.styleable.TimeSelectView_timeTextSize, 40);
        mTaskTextSize = (int)ty.getDimension(R.styleable.TimeSelectView_taskTextSize, 45);
        mExtraHeight = (int)(mIntervalHeight * 0.5);
        mCenterTime = ty.getFloat(R.styleable.TimeSelectView_centerTime, -1);
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
        lpNowTimeView.topMargin = MyTime.getNowTimeHeight() - NowTimeView.BALL_DIAMETER;
        layoutParent.addView(nowTimeView, lpNowTimeView);

        RectView rectView = new RectView(context);
        mIRectView = rectView;
        LayoutParams lpRectView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lpRectView.leftMargin = mIntervalLeft;
        lpRectView.topMargin = mExtraHeight;
        lpRectView.rightMargin = TimeFrameView.INTERVAL_RIGHT;
        lpRectView.bottomMargin = mExtraHeight + TimeFrameView.HORIZONTAL_LINE_WIDTH;
        rectView.setChildFrameLayout(layoutChild);
        rectView.setRectColor(mBorderColor, mInsideColor);
        rectView.setTextSize((int)(0.8f * mTimeTextSide), mTaskTextSize);
        rectView.setInterval(mExtraHeight);
        layoutChild.addView(rectView, lpRectView);

        TimeFrameView timeFrameView = new TimeFrameView(context);
        LayoutParams lpTimeFrameView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        timeFrameView.setHour(mStartHour, mEndHour);
        timeFrameView.setTextSize(mTimeTextSide);
        timeFrameView.setInterval(mIntervalLeft, TimeFrameView.INTERVAL_RIGHT, mExtraHeight, mIntervalHeight);
        layoutChild.addView(timeFrameView, lpTimeFrameView);
        layoutChild.setRectView(rectView);
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
            mIRectView.longPress(mInitialY + getScrollY() - mExtraHeight);
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
                if (mIsFinishJudge) {
                    return super.dispatchTouchEvent(ev);
                }
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
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                /*
                 * ScrollView的外部大小的上下mExtraHeight距离进行拦截
                 * */
                if (mInitialY < mExtraHeight || mInitialY > getHeight() - mExtraHeight - 5) {
                    removeCallbacks(mLongPressRun);
                    return true;
                }

                /*
                 * 如果不在DOWN事件手动调用onTouchEvent(), ScrollView就不会移动,
                 * 因为子View的onTouchEvent()已经把DOWN事件拦截了,
                 * */
                onTouchEvent(ev);
                return false;
            case MotionEvent.ACTION_MOVE:
                Log.d("123", "onInterceptTouchEvent: MOVE");
                if (!mIsLongPress) {//直接滑动ScrollView
                    super.onInterceptTouchEvent(ev);
                    return true;
                }
                automaticSlide((int)ev.getY());
                break;
            case MotionEvent.ACTION_UP:
                removeCallbacks(mScrollRunnable);
                
                break;
        }
        return false;
    }

    int dy = -1;
    private void automaticSlide(int y) {
        switch (RectView.WHICH_CONDITION) {
            case RectView.TOP:
            case RectView.BOTTOM:
                double multiple = Math.sqrt(MAX_AUTO_SLIDE_VELOCITY)/mExtraHeight;
                if (y <= mExtraHeight) {
                    dy = -Math.abs((int)Math.pow((y - mExtraHeight) * multiple, 2));
                }else if (y >= getHeight() - mExtraHeight) {
                    dy = Math.abs((int)Math.pow(getHeight() - mExtraHeight - y, 2));
                }
                if (dy != -1) {
                    post(mScrollRunnable);
                }
                break;
        }
    }

    TimeSelectView mTimeSelectView = this;
    private final Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run()
        {

            ViewCompat.postOnAnimation(mTimeSelectView, mScrollRunnable);
        }
    };

    private void timeMove(float time) {
        int windowHalfHeight = getHeight()/2;
        time %= 24;
        if (time < mStartHour) {
            time += 24;
        }
        int nowTimeHeight = (int)(mExtraHeight + (time - mStartHour) * mIntervalHeight);
        int totalHeight = (mEndHour - mStartHour) * mIntervalHeight + 2 * mExtraHeight;
        if (nowTimeHeight > windowHalfHeight && nowTimeHeight < totalHeight - windowHalfHeight) {
            scrollTo(0, nowTimeHeight - windowHalfHeight);
        }else if (nowTimeHeight >= totalHeight - windowHalfHeight) {
            scrollTo(0, totalHeight - windowHalfHeight * 2);
        }
    }

    /**
     * 若传入的时间处于上下边界附近无法居中的位置，则会使时间线处于顶部或尾部界面内，但不居中。
     * 若不调用该方法，则会自动以当前时间居中。
     * @param centerTime 设置居中的时间，支持小数。
     */
    public void setCenterTime(float centerTime) {
        if (centerTime == -1) {//不设置CenterTime以当前时间线为中线
            post(new Runnable() {
                @Override
                public void run() {
                    timeMove(MyTime.getNowTime());
                }
            });
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    timeMove(MyTime.getNowTime());
                    postDelayed(this, NowTimeView.DELAY_RUN_TIME);
                }
            }, NowTimeView.DELAY_RUN_TIME);
        }else {//设置CenterTime以CenterTime为中线，不随时间移动
            post(new Runnable() {
                @Override
                public void run() {
                    timeMove(mCenterTime);
                }
            });
        }

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollViewListener != null) {
            if (oldt == 0) {
                return;
            }
            mOnScrollViewListener.onScrollChanged(t, oldt - t);
        }
    }

    public interface IRectView {
        void longPress(int y);
        List<Rect> getRects();
        HashMap<Rect, String> getRectAndName();
        HashMap<Rect, String> getRectAndDTime();
    }

    public interface onScrollViewListener {
        void onScrollChanged(int y, int dy);
    }
}
