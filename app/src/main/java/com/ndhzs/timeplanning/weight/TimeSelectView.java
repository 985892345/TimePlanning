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
import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.timeselectview.ChildLayout;
import com.ndhzs.timeplanning.weight.timeselectview.TimeTools;
import com.ndhzs.timeplanning.weight.timeselectview.NowTimeLine;
import com.ndhzs.timeplanning.weight.timeselectview.RectImgView;
import com.ndhzs.timeplanning.weight.timeselectview.RectView;
import com.ndhzs.timeplanning.weight.timeselectview.FrameView;

import java.util.HashMap;
import java.util.List;

public class TimeSelectView extends ScrollView {

    private Context context;
    private IRectView mIRectView;
    private IIsAllowDraw mIChildLayout;
    private int mStartHour = 3;
    private int mEndHour = 24 + 3;
    private int mMoveX, mMoveY;//从MOVE事件中得到的x、y值，用来给自动滑动使用
    private int mTotalHeight;//ScrollView的内部总高度
    private final int mIntervalLeft;//左边的文字间隔宽度
    private final int mExtraHeight;//上方或下方其中一方多余的高度
    private final int mIntervalHeight;//一个小时的间隔高度
    private final int mBorderColor;//矩形边框颜色
    private final int mInsideColor;//矩形内部颜色
    private final int mTimeTextSide;//时间字体大小
    private final int mTaskTextSize;//任务字体大小
    /**
     * 计入ACTION_DOWN时的坐标，(注意！这个坐标是ScrollView外部高度的坐标系)
     */
    private int mInitialX, mInitialY;
    private float mCenterTime;
    private static final int MAX_AUTO_SLIDE_VELOCITY = 49;//最大滑动速度的平方
    private static final int AUTO_MOVE_THRESHOLD = 200;//自动滑动的阀值
    private static final float MULTIPLE = (float) MAX_AUTO_SLIDE_VELOCITY/AUTO_MOVE_THRESHOLD;

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
        TimeTools.loadData(FrameView.HORIZONTAL_LINE_WIDTH, mExtraHeight, mIntervalHeight, mStartHour);
        setVerticalScrollBarEnabled(false);
        initLayout(context);
    }
    private void initLayout(Context context) {
        FrameLayout layoutParent = new FrameLayout(context);
        LayoutParams lpLayoutParent = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        ChildLayout layoutChild= new ChildLayout(context);
        LayoutParams lpLayoutChild = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutChild.setInterval(mIntervalLeft, mExtraHeight);
        mIChildLayout = layoutChild;

        NowTimeLine nowTimeLine = new NowTimeLine(context);
        LayoutParams lpNowTimeView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        nowTimeLine.setInterval(mIntervalLeft, FrameView.INTERVAL_RIGHT);

        RectView rectView = new RectView(context);
        LayoutParams lpRectView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lpRectView.leftMargin = mIntervalLeft;
        lpRectView.topMargin = mExtraHeight;
        lpRectView.rightMargin = FrameView.INTERVAL_RIGHT;
        lpRectView.bottomMargin = mExtraHeight + FrameView.HORIZONTAL_LINE_WIDTH;
        rectView.setChildLayout(layoutChild);
        rectView.setRectColor(mBorderColor, mInsideColor);
        rectView.setTextSize((int)(0.8f * mTimeTextSide), mTaskTextSize);
        rectView.setInterval(mExtraHeight);
        layoutChild.setIRectView(rectView);
        mIRectView = rectView;

        FrameView frameView = new FrameView(context);
        LayoutParams lpTimeFrameView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        frameView.setHour(mStartHour, mEndHour);
        frameView.setTextSize(mTimeTextSide);
        frameView.setInterval(mIntervalLeft, FrameView.INTERVAL_RIGHT, mExtraHeight, mIntervalHeight);

        //addView()，顺序不能调换
        layoutChild.addView(rectView, lpRectView);
        layoutChild.addView(frameView, lpTimeFrameView);
        layoutParent.addView(layoutChild, lpLayoutChild);
        layoutParent.addView(nowTimeLine, lpNowTimeView);
        addView(layoutParent, lpLayoutParent);
    }

    private boolean mIsLongPress;//是否是长按
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
                postDelayed(mLongPressRun, 250);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsLongPress) {//长按时间未到时或已被自身处理所有事件
                    //只要大于滑动阀值就自身在onInterceptTouchEvent的MOVE事件拦截
                    if (Math.abs(x - mInitialX) > MOVE_THRESHOLD || Math.abs(y - mInitialY) > MOVE_THRESHOLD) {
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
                        return true;//要么等到时间到，要么你滑动超过阀值
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
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (mTotalHeight == 0) {
                    mTotalHeight = getChildAt(0).getHeight();
                }
                //点击的是左部区域，直接拦截
                if (mInitialX < mIntervalLeft + 3) {
                    return true;
                }
                //对ScrollView的外部大小的上下mExtraHeight距离进行拦截
                if (mInitialY < mExtraHeight || mInitialY > getHeight() - mExtraHeight - 5) {
                    removeCallbacks(mLongPressRun);
                    return true;
                }
                //所有的点击都往下传
                mNowCenterY = mInitialY;
                mUpperLimit = Integer.MIN_VALUE;
                mLowerLimit = Integer.MAX_VALUE;

                /*
                 * 如果不在DOWN事件手动调用onTouchEvent(), ScrollView就不会移动,
                 * 因为子View的onTouchEvent()已经把DOWN事件拦截了, ScrollView中
                 * 不执行onTouchEvent()的DOWN事件，将不会滑动
                 * */
                onTouchEvent(ev);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mIsLongPress) {//如果是长按
                    if (mUpperLimit == Integer.MIN_VALUE) {//保证值只赋值一次
                        mUpperLimit = mIRectView.getUpperLimit() + mExtraHeight;
                        mLowerLimit = mIRectView.getLowerLimit() + mExtraHeight;
                    }
                    mMoveX = x;
                    mMoveY = y;
                    automaticSlide(mMoveY);//只有在长按时才会调用自动滑动
                }else {//如果直接滑动的ScrollView，不是长按，直接拦截
                    super.onInterceptTouchEvent(ev);
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                // DOWN：false | MOVE：true 时，UP事件不会被调用，但onTouchEvent()的UP会被系统调用
                // 我找规律发现onInterceptTouchEvent()一旦在某时return true，以后的所有事件直接从
                // dispatchTouchEvent ——> onTouchEvent()，再也不会出现onInterceptTouchEvent()被调用
                if (mIsRun) {
                    mIsRun = false;
                    removeCallbacks(mScrollRunnable);
                    int dY = 0;
                    switch (RectView.WHICH_CONDITION) {
                        case RectView.TOP:
                        case RectView.BOTTOM:
                        case RectView.EMPTY_AREA:
                            dY = (dy > 0) ? y - (getHeight() - AUTO_MOVE_THRESHOLD) + 10 : y - AUTO_MOVE_THRESHOLD - 10;
                            break;
                        case RectView.INSIDE:

                    }
                    smoothScrollBy(0, dY);
                }
                break;
            }
        }
        return false;
    }

    private int dy;
    private boolean mIsRun;
    private int mLastMoveY;
    private int mNowCenterY;
    private int mUpperLimit = Integer.MIN_VALUE;
    private int mLowerLimit = Integer.MAX_VALUE;
    private final int CENTER_DISTANCE_THRESHOLD = 30;//距离mNowCenterY位置的距离阀值，移动的y在这个距离内是不会自动滑动的
    private void automaticSlide(int y) {
        int uniformY = y + getScrollY();//统一坐标系
        boolean isWithinLimit = uniformY > mUpperLimit && uniformY < mLowerLimit;//是否在上下线以内
        boolean isWithin = y > AUTO_MOVE_THRESHOLD && y < getHeight() - AUTO_MOVE_THRESHOLD;//是否在内部区域
        switch (RectView.WHICH_CONDITION) {
            case RectView.TOP:
            case RectView.BOTTOM:
            case RectView.EMPTY_AREA:
                if (isWithin || !isWithinLimit) {//在内部或在上下限以外，不再滑动
                    removeCallbacks(mScrollRunnable);
                    mIRectView.isAllowDraw(true);
                    mIsRun = false;//刷新，为false时Run才可以执行
                }else {//在滑动区域且在上下限以内
                    //先计算dy值，控制速度
                    if (y <= AUTO_MOVE_THRESHOLD) {//手指在顶部区域，ScrollView应该向下滑
                        dy = (int) -Math.sqrt((AUTO_MOVE_THRESHOLD - y) * MULTIPLE);
                        Log.d(TAG, "dy = " + dy + "   y = " + y + "   d = " + (AUTO_MOVE_THRESHOLD - y));
                    }else {//手指在底部区域，ScrollView应该向上滑
                        dy = (int) Math.sqrt((y - (getHeight() - AUTO_MOVE_THRESHOLD)) * MULTIPLE);
                        Log.d(TAG, "dy = " + dy + "   y = " + y + "   d = " + (y - (getHeight() - AUTO_MOVE_THRESHOLD)));
                    }
                    //再判断是否重复调用延时Runnable
                    if (!mIsRun) {//mIsRun控制只调用一次Runnable
                        mIsRun = true;
                        mIRectView.isAllowDraw(false);
                        post(mScrollRunnable);
                    }
                }
                break;
            case RectView.INSIDE:
                int top = mIRectView.getImgViewRect().getTop();
                int bottom = mIRectView.getImgViewRect().getBottom();
                boolean isWithinThreshold = y < mNowCenterY + CENTER_DISTANCE_THRESHOLD && y > mNowCenterY - CENTER_DISTANCE_THRESHOLD;
                boolean isBottomSlide = bottom - getScrollY() >= getHeight() - AUTO_MOVE_THRESHOLD;
                boolean isTopSlide = top - getScrollY() <= AUTO_MOVE_THRESHOLD;
                if (isWithinThreshold || (!isBottomSlide && !isTopSlide)) {
                    removeCallbacks(mScrollRunnable);
                    mIChildLayout.isAllowDraw(true);
                    mIsRun = false;
                }else {
                    if (mNowCenterY == mInitialY) {
                        //为了刷新mNowCenterY值，防止滑到不自动滑动区后再去滑动isWithinThreshold的判断问题
                        mNowCenterY = (mInitialY == getHeight()/2) ? getHeight()/2 : getHeight()/2 + 1;
                    }
                    if (isTopSlide) {
                        dy = (int) -Math.sqrt((AUTO_MOVE_THRESHOLD - (top - getScrollY())) * MULTIPLE);
                        if (getScrollY() == 0) {
                            dy = 0;
                        }
                    }else {
                        dy = (int) Math.sqrt((bottom - getScrollY() - (getHeight() - AUTO_MOVE_THRESHOLD)) * MULTIPLE);
                        if (getScrollY() + getHeight() == mTotalHeight) {
                            dy = 0;
                        }
                    }
                    if (!mIsRun) {//控制只调用一次Runnable
                        mIsRun = true;
                        mLastMoveY = y;//记录第一次开始滑动的值
                        mIChildLayout.isAllowDraw(false);
                        post(mScrollRunnable);
                    }
                }
                break;
        }
    }
    private int i = 0;
    private final Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {
            int nowScrollY = getScrollY() + dy;
            Log.d(TAG, "run: ");
            scrollTo(0, nowScrollY);
            switch (RectView.WHICH_CONDITION) {
                case RectView.TOP:
                case RectView.BOTTOM:
                case RectView.EMPTY_AREA:
                    mIRectView.refresh(nowScrollY + mMoveY - mExtraHeight);
                    break;
                case RectView.INSIDE:
                    mIRectView.getImgViewRect().autoSlideLayout(mMoveX - mInitialX, dy + mMoveY - mLastMoveY);
                    mLastMoveY = mMoveY;
                    break;
            }
            postDelayed(this, 20);
            if (mMoveY == mLastMoveY) {
                i++;
            }
            if (i == 10) {
                i = 0;
                automaticSlide(mMoveY);//刷新，防止一直不判断，一直移动
            }
        }
    };

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
                    timeMove(TimeTools.getNowTime());
                    postDelayed(this, TimeTools.DELAY_RUN_TIME);
                }
            });
        }else {//设置CenterTime以CenterTime为中线，不随时间移动
            post(new Runnable() {
                @Override
                public void run() {
                    timeMove(mCenterTime);
                }
            });
        }

    }
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
        void isAllowDraw(boolean isAllowDraw);
        void longPress(int y);
        void refresh(int y);
        int getUpperLimit();
        int getLowerLimit();
        RectImgView getImgViewRect();
        List<Rect> getRects();
        HashMap<Rect, String> getRectAndName();
        HashMap<Rect, String> getRectAndDTime();
    }
    public interface IIsAllowDraw {
        void isAllowDraw(boolean isAllowDraw);
    }
    public interface onScrollViewListener {
        /**
         * 设置了CenterTime后的滑动值不会返回
         * @param y 当前ScrollView的ScrollY
         * @param dy 移动差值，ScrollView向上移动为负值
         */
        void onScrollChanged(int y, int dy);
    }

    private static final String TAG = "123";
}
