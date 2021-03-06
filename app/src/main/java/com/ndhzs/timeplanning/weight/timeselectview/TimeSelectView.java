package com.ndhzs.timeplanning.weight.timeselectview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ScrollView;

import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.timeselectview.layout.ChildLayout;
import com.ndhzs.timeplanning.weight.timeselectview.layout.view.RectView;
import com.ndhzs.timeplanning.weight.timeselectview.utils.TimeViewUtil;
import com.ndhzs.timeplanning.weight.timeselectview.layout.view.NowTimeLine;
import com.ndhzs.timeplanning.weight.timeselectview.layout.view.RectImgView;
import com.ndhzs.timeplanning.weight.timeselectview.layout.view.FrameView;
import com.ndhzs.timeplanning.weight.timeselectview.bean.TaskBean;

import java.util.List;

public class TimeSelectView extends ScrollView {

    private Context context;
    private IRectView mRectView;
    private ViewPager2 mViewPager;
    private ChildLayout mLayoutChild;
    private int mStartHour;
    private int mEndHour;
    private int mMoveX, mMoveY;//从MOVE事件中得到的x、y值，用来给自动滑动使用
    private int mTotalHeight;//ScrollView的内部总高度
    private int mCenterTimeHeight;//设置的中心时间应滑的距离，减去了外部高度的一半windowHalfHeight
    private final int mIntervalLeft;//左边的文字间隔宽度
    private final int mExtraHeight;//上方或下方其中一方多余的高度
    private final int mIntervalHeight;//一个小时的间隔高度
    private final int mBorderColor;//矩形边框颜色
    private final int mInsideColor;//矩形内部颜色
    private final float mTimeTextSide;//时间字体大小
    private final float mTaskTextSize;//任务字体大小
    private final TimeViewUtil mTimeViewUtil;
    private boolean mIsOpenScrollCallBack = true;//设置mIsCloseUserActionJudge，将在被其他非触摸操作滑动时不会回调滑动的接口

    /**
     * 计入ACTION_DOWN时的坐标，(注意！这个坐标是ScrollView外部高度的坐标系)
     */
    private int mInitialX, mInitialY;
    private float mCenterTime;
    private static final int MAX_AUTO_SLIDE_VELOCITY = 50;//最大滑动速度的平方
    private static final int AUTO_MOVE_THRESHOLD = 150;//自动滑动的阀值
    private static final float MULTIPLE = (float) MAX_AUTO_SLIDE_VELOCITY/AUTO_MOVE_THRESHOLD;

    public static boolean IS_SHOW_TOP_BOTTOM_TIME = true;//是否绘制上下边界时间
    public static boolean IS_SHOW_DIFFERENT_TIME = true;//是否绘制时间差

    public static boolean IS_LONG_PRESS;//是否是长按

    private OnScrollViewListener mOnScrollViewListener;
    private OnDataChangeListener mOnDataChangeListener;

    /**
     * 得到点击的数据对象
     * @return 点击的数据对象
     */
    public TaskBean getClickTaskBean() {
        return mRectView.getClickTaskBean();
    }

    /**
     * 加载数据时使用
     * @param taskBeans 传入新的数据
     */
    public void setData(List<TaskBean> taskBeans) {
        mRectView.setData(taskBeans);
    }

    /**
     * 设置滑动接口，默认所有引起的滑动都会回调滑动接口，可以setIsCloseUserActionJudge(boolean)
     * 来打开只有用户触摸时才能回调滑动接口
     * @param l 滑动监听
     */
    public void setOnScrollViewListener(OnScrollViewListener l) {
        this.mOnScrollViewListener = l;
    }

    /**
     * 设置为false时，可以关闭滑动的回调，
     * 用于在非用户触摸而调用scrollTo等方法前关闭滑动回调，
     * 可以不用还原，在用户点击时，自动还原
     * @param is true or false
     */
    public void setIsOpenScrollCallBack(boolean is) {
        this.mIsOpenScrollCallBack = is;
    }

    /**
     * 时间间隔数
     * @param timeInterval 必须为60的因数，若不是，将以15为间隔数
     */
    public void setTimeInterval(int timeInterval) {
        mTimeViewUtil.TIME_INTERVAL = (60 % timeInterval == 0) ? timeInterval : 15;
    }

    /**
     * 最终的任务区域是否展示上下边界时间，在移动和改变大小时任然会展示
     * (该boolean为static，一旦设置，所有的TimeSelectView都会设置)
     * @param is true：展示上下边界时间，false：不展示上下边界时间
     */
    public void setIsShowTopBottomTime(boolean is) {
        IS_SHOW_TOP_BOTTOM_TIME = is;
    }

    /**
     * 最终的任务区域是否展示时间差，在移动和改变大小时任然会展示
     * (该boolean为static，一旦设置，所有的TimeSelectView都会设置)
     * @param is true：展示时间差，false：不展示时间差
     */
    public void setIsShowDifferentTime(boolean is) {
        IS_SHOW_DIFFERENT_TIME = is;
    }

    /**
     * 设置当前点击区域的任务名称
     */
    public void refreshRect() {
        mRectView.refreshRect();
    }

    /**
     * 解决与ViewPager2的同向滑动冲突
     * @param viewPager2 传入ViewPager2，不是ViewPager
     */
    public void setLinkViewPager2(ViewPager2 viewPager2) {
        this.mViewPager = viewPager2;
    }

    /**
     * 若你使用了两个并排的TimeSelectView，又想实现整体移动互相传递数据，可以使用该方法。
     * (使用前提：请设置TimeSelectView 和 父布局的 android:clipChildren="false"，如果你父布局为
     * CardView，该属性会设置无效，请使用其他布局。如果你的确想使用CardView，可以尝试将CardView单独设置
     * 为背景，用另一个Layout覆盖在其上，但请将该Layout的android:elevation设置为合适值，不然将无法覆盖
     * 在CardView上方)
     * @param linkTimeView 传入要连接的另一个TimeSelectView，请保证他们的高度相同
     */
    public void setLinkTimeSelectView(TimeSelectView linkTimeView) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                int[] selfPosition = new int[2];
                getLocationInWindow(selfPosition);
                int[] linkPosition = new int[2];
                linkTimeView.getLocationInWindow(linkPosition);
                int diffDistance = selfPosition[0] - linkPosition[0];
                mLayoutChild.setLinkChildLayout(linkTimeView.mLayoutChild, linkTimeView.mTimeViewUtil, diffDistance);
            }
        }, 200);
    }

    /**
     * 设置是否显示时间线
     */
    public void setIsShowTimeLine(boolean is) {
        NowTimeLine mNowTimeLine = new NowTimeLine(context, mTimeViewUtil);
        if (is) {
            LayoutParams lpNowTimeView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mNowTimeLine.setInterval(mIntervalLeft, FrameView.INTERVAL_RIGHT);
            mNowTimeLine.setElevation(1);
            mLayoutChild.addView(mNowTimeLine, lpNowTimeView);
        }else {
            mLayoutChild.removeView(mNowTimeLine);
        }
    }

    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener) {
        this.mOnDataChangeListener = onDataChangeListener;
        mRectView.setOnDataChangeListener(mOnDataChangeListener);
        mLayoutChild.setOnDataChangeListener(mOnDataChangeListener);
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
        mBorderColor = ty.getColor(R.styleable.TimeSelectView_borderColor, ContextCompat.getColor(context, R.color.inside_color));
        mInsideColor = ty.getColor(R.styleable.TimeSelectView_insideColor, ContextCompat.getColor(context, R.color.border_color));
        mIntervalLeft = (int)ty.getDimension(R.styleable.TimeSelectView_intervalWidth, 110);
        mIntervalHeight = (int)ty.getDimension(R.styleable.TimeSelectView_intervalHeight, 194);
        mExtraHeight = (int)(mIntervalHeight * 0.5f);
        mTimeTextSide = ty.getDimension(R.styleable.TimeSelectView_timeTextSize, 40);
        mTaskTextSize = ty.getDimension(R.styleable.TimeSelectView_taskTextSize, 40);
        mCenterTime = ty.getFloat(R.styleable.TimeSelectView_centerTime, -1);
        mStartHour = ty.getInteger(R.styleable.TimeSelectView_startHour, 2);
        mEndHour = ty.getInteger(R.styleable.TimeSelectView_endHour, 24 + 2);
        IS_SHOW_TOP_BOTTOM_TIME = ty.getBoolean(R.styleable.TimeSelectView_isShowTopBottomTime, true);
        IS_SHOW_DIFFERENT_TIME = ty.getBoolean(R.styleable.TimeSelectView_isShowDifferentTime, false);
        ty.recycle();
        setCenterTime(mCenterTime);
        mTimeViewUtil = new TimeViewUtil(FrameView.HORIZONTAL_LINE_WIDTH, mExtraHeight, mIntervalHeight, mStartHour);
        setVerticalScrollBarEnabled(false);
        initLayout(context);
    }
    private void initLayout(Context context) {
        mLayoutChild = new ChildLayout(context, mTimeViewUtil);
        LayoutParams lpLayoutChild = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mLayoutChild.setInterval(mIntervalLeft, mExtraHeight);

        RectView rectView = new RectView(context, mTimeViewUtil);
        LayoutParams lpRectView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lpRectView.leftMargin = mIntervalLeft;
        lpRectView.topMargin = mExtraHeight;
        lpRectView.rightMargin = FrameView.INTERVAL_RIGHT;
        lpRectView.bottomMargin = mExtraHeight + FrameView.HORIZONTAL_LINE_WIDTH;
        rectView.setChildLayout(mLayoutChild);
        rectView.setRectColor(mBorderColor, mInsideColor);
        rectView.setTextSize((int)(0.8f * mTimeTextSide), mTaskTextSize);
        rectView.setInterval(mExtraHeight);
        mLayoutChild.setIRectView(rectView);
        mRectView = rectView;

        FrameView frameView = new FrameView(context);
        LayoutParams lpFrameView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        frameView.setHour(mStartHour, mEndHour);
        frameView.setTextSize(mTimeTextSide);
        frameView.setInterval(mIntervalLeft, FrameView.INTERVAL_RIGHT, mExtraHeight, mIntervalHeight);

        //addView()，顺序不能调换，也可以设置elevation来控制高度
        mLayoutChild.addView(rectView, lpRectView);
        mLayoutChild.addView(frameView, lpFrameView);

        addView(mLayoutChild, lpLayoutChild);

        if (!getClipChildren()) {
            mLayoutChild.setClipChildren(false);
        }
    }

    private final int MOVE_THRESHOLD = 5;//识别是长按而能移动的阈值
    private final Runnable mLongPressRun = new Runnable() {
        @Override
        public void run() {
            IS_LONG_PRESS = true;
            Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(30);
            mRectView.longPress(mInitialY + getScrollY() - mExtraHeight);
        }
    };
    private final Runnable mGoBackCenterTimeRun = new Runnable() {
        @Override
        public void run() {
            slowlyTimeMove();
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
                IS_LONG_PRESS = false;//刷新
                postDelayed(mLongPressRun, 250);
                removeCallbacks(mGoBackCenterTimeRun);//防止多次点击多次调用自动滑动
                break;
            case MotionEvent.ACTION_MOVE:
                if (!IS_LONG_PRESS) {//长按时间未到时或已被自身处理所有事件
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
                if (y != mInitialY) {//这个自动回到CenterTime不能写在onInterceptTouchEvent()的UP，详细看下方注释
                    postDelayed(mGoBackCenterTimeRun, TimeViewUtil.DELAY_BACK_CURRENT_TIME);
                }
                IS_LONG_PRESS = false;
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
                mIsOpenScrollCallBack = true;
                if (mAnimator != null && mAnimator.isRunning()) {
                    mAnimator.cancel();
                }
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
                if (IS_LONG_PRESS) {//如果是长按
                    if (mUpperLimit == Integer.MIN_VALUE) {//保证值只赋值一次
                        mUpperLimit = mRectView.getUpperLimit() + mExtraHeight;
                        mLowerLimit = mRectView.getLowerLimit() + mExtraHeight;
                    }
                    mMoveX = x;
                    mMoveY = y;
                    automaticSlide(mMoveY);//只有在长按时才会调用自动滑动
                }else {//如果直接滑动的ScrollView，不是长按，直接拦截
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                // DOWN：false | MOVE：true 时，onInterceptTouchEvent()的UP事件不会被调用，
                // 但onTouchEvent()的UP会被系统调用，我找规律发现onInterceptTouchEvent()一旦
                // 在某时return true，以后的所有事件直接从dispatchTouchEvent ——> onTouchEvent()，
                // 再也不会出现onInterceptTouchEvent()被调用
                if (mIsRun) {//在滑动区域停下
                    mIsRun = false;
                    removeCallbacks(mScrollRunnable);
                    int dY = 0;
                    switch (RectView.WHICH_CONDITION) {
                        case RectView.TOP:
                        case RectView.BOTTOM:
                        case RectView.EMPTY_AREA:
                            if (dy > 0) {
                                dY = y - (getHeight() - AUTO_MOVE_THRESHOLD) + 10;
                            }else if (dy < 0) {
                                dY = y - AUTO_MOVE_THRESHOLD - 10;
                            }
                            break;
                        case RectView.INSIDE:
                            if (dy > 0) {
                                int bottom = mRectView.getImgViewRect().getBottom() - getScrollY();
                                dY = bottom - (getHeight() - AUTO_MOVE_THRESHOLD) + 10;
                            }else {
                                int top = mRectView.getImgViewRect().getTop() - getScrollY();
                                dY = top - AUTO_MOVE_THRESHOLD - 10;
                            }
                            break;
                    }
                    slowlyMoveBy(dY);
                }
                if (!IS_LONG_PRESS && Math.abs(x - mInitialX) < MOVE_THRESHOLD && Math.abs(y - mInitialY) < MOVE_THRESHOLD && mRectView.isClick(y + getScrollY() - mExtraHeight)) {
                    performClick();
                }
                break;
            }
        }
        return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        if (mViewPager != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mViewPager.setUserInputEnabled(false);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (getScrollY() ==  0 && y > mInitialY) {
                        mViewPager.setUserInputEnabled(true);
                        return false;
                    }
                    if (getScrollY() + getHeight() == mTotalHeight && y < mInitialY) {
                        mViewPager.setUserInputEnabled(true);
                        return false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
        }
        return super.onTouchEvent(ev);
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
                    mRectView.isAllowDraw(true);
                    mIsRun = false;//刷新，为false时Run才可以执行
                }else {//在滑动区域且在上下限以内
                    //判断是否重复调用延时Runnable
                    if (!mIsRun) {//mIsRun控制只调用一次Runnable
                        mIsRun = true;
                        mRectView.isAllowDraw(false);
                        post(mScrollRunnable);
                    }
                    //计算dy值，控制速度
                    if (y <= AUTO_MOVE_THRESHOLD) {//手指在顶部区域，ScrollView应该向下滑
                        dy = (int) -Math.sqrt((AUTO_MOVE_THRESHOLD - y) * MULTIPLE);
                        if (mMoveY > mLastMoveY + 5) {//如果在顶部区域往下滑，但你往下滑后还停在滑动区仍然会滑动
                            dy = 0;
                        }
                    }else {//手指在底部区域，ScrollView应该向上滑
                        dy = (int) Math.sqrt((y - (getHeight() - AUTO_MOVE_THRESHOLD)) * MULTIPLE);
                        if (mMoveY < mLastMoveY - 5) {//如果在底部区域往上滑，但你往上滑后还停在滑动区仍然会滑动
                            dy = 0;
                        }
                    }
                }
                break;
            case RectView.INSIDE:
                int top = mRectView.getImgViewRect().getTop();
                int bottom = mRectView.getImgViewRect().getBottom();
                //下面这个是：当长按下去，矩形的顶部或底部已经在滑动区，则必须移动一个范围才能开启滑动，在成功滑动后NowCenterY会设置成中心值
                boolean isWithinThreshold = y < mNowCenterY + CENTER_DISTANCE_THRESHOLD && y > mNowCenterY - CENTER_DISTANCE_THRESHOLD;
                boolean isBottomSlide = bottom - getScrollY() >= getHeight() - AUTO_MOVE_THRESHOLD * 0.4f;//乘以0.4限制滑动区
                boolean isTopSlide = top - getScrollY() <= AUTO_MOVE_THRESHOLD * 0.4f;//乘以0.4限制滑动区
                if (isWithinThreshold || (!isBottomSlide && !isTopSlide)) {
                    removeCallbacks(mScrollRunnable);
                    mLayoutChild.isAllowDraw(true);
                    mIsRun = false;
                }else {
                    if (mNowCenterY == mInitialY) {
                        //为了刷新mNowCenterY值，防止滑到不自动滑动区后再去滑动isWithinThreshold的判断问题
                        mNowCenterY = (mInitialY == getHeight()/2) ? getHeight()/2 : getHeight()/2 + 1;
                    }
                    if (!mIsRun) {//控制只调用一次Runnable
                        mIsRun = true;
                        mLastMoveY = y;//记录第一次开始滑动的值
                        mLayoutChild.isAllowDraw(false);
                        post(mScrollRunnable);
                    }
                    if (isTopSlide) {
                        dy = (int) -Math.sqrt((AUTO_MOVE_THRESHOLD - (top - getScrollY())) * MULTIPLE);
                        if (getScrollY() == 0) {//如果ScrollView在顶部则速度为0，必须设置这个，因为下面的ImgViewRect的y轴移动量加了dy
                            dy = 0;
                        }
                        if (mMoveY > mLastMoveY + 5) {//如果在顶部区域往下滑，但你往下滑后还停在滑动区仍然会滑动
                            dy = 0;
                        }
                    }else {
                        dy = (int) Math.sqrt((bottom - getScrollY() - (getHeight() - AUTO_MOVE_THRESHOLD)) * MULTIPLE);
                        if (getScrollY() + getHeight() == mTotalHeight) {//如果ScrollView在底部则速度为0，必须设置这个，因为下面的ImgViewRect的y轴移动量加了dy
                            dy = 0;
                        }
                        if (mMoveY < mLastMoveY - 5) {//如果在底部区域往上滑，但你往上滑后还停在滑动区仍然会滑动
                            dy = 0;
                        }
                    }
                }
                break;
        }
    }
    private int i = 0;//用来刷新，防止一直不判断，一直调用
    private final Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {
            scrollBy(0, dy);
            switch (RectView.WHICH_CONDITION) {
                case RectView.TOP:
                case RectView.BOTTOM:
                case RectView.EMPTY_AREA:
                    mRectView.refresh(getScrollY() + mMoveY - mExtraHeight);
                    break;
                case RectView.INSIDE:
                    mRectView.getImgViewRect().autoSlideLayout(mMoveX - mInitialX, dy + mMoveY - mLastMoveY);
                    break;
            }
            mLastMoveY = mMoveY;
            postDelayed(this, 20);//重复调用自身，持续滑动
            if (mMoveY == mLastMoveY) {
                i++;
            }
            if (i == 10) {
                i = 0;
                automaticSlide(mMoveY);//刷新，防止一直不判断，一直调用
            }
        }
    };

    /**
     * 若传入的时间处于上下边界附近无法居中的位置，则会使时间线处于顶部或尾部界面内，但不居中。
     * 若不调用该方法，则会自动以当前时间居中。(xml中也可设置centerTime)
     * @param centerTime 设置居中的时间，支持小数。
     */
    public void setCenterTime(float centerTime) {
        if (centerTime == -1) {//不设置CenterTime，以当前时间线为中线
            post(new Runnable() {
                @Override
                public void run() {
                    fastTimeMove(mTimeViewUtil.getNowTime());
                    postDelayed(mTimeMoveRun, TimeViewUtil.DELAY_NOW_TIME_REFRESH);
                }
            });
        }else {//设置CenterTime，以CenterTime为中线，不随时间移动
            post(new Runnable() {
                @Override
                public void run() {
                    fastTimeMove(mCenterTime);
                }
            });
        }
    }
    private void fastTimeMove(float time) {
        mCenterTimeHeight = getCenterTimeHeight(time);
        scrollTo(0, mCenterTimeHeight);
    }
    private final Runnable mTimeMoveRun = new Runnable() {
        @Override
        public void run() {
            slowlyTimeMove();
            postDelayed(this, TimeViewUtil.DELAY_NOW_TIME_REFRESH);
        }
    };
    private ValueAnimator mAnimator;
    private void slowlyTimeMove() {
        int height;
        if (mCenterTime == -1) {//以当前时间线为中线
            height = getCenterTimeHeight(mTimeViewUtil.getNowTime());
        }else {//以CenterTime为中线，不随时间移动
            height = mCenterTimeHeight;
        }
        slowlyMoveTo(height);
    }
    private int getCenterTimeHeight(float time) {
        int windowHalfHeight = getHeight()/2;
        time %= 24;
        if (time < mStartHour) {
            time += 24;
        }
        //下面用TimeTools中的getNowTimeHeight可能会有十分短暂的时间差
        int nowTimeHeight = (int) (mExtraHeight + (time - mStartHour) * mIntervalHeight);
        int totalHeight = (mEndHour - mStartHour) * mIntervalHeight + 2 * mExtraHeight;
        if (nowTimeHeight > windowHalfHeight && nowTimeHeight < totalHeight - windowHalfHeight) {
            return nowTimeHeight - windowHalfHeight;
        }else if (nowTimeHeight >= totalHeight - windowHalfHeight) {
            return totalHeight - windowHalfHeight * 2;
        }else {
            return  0;
        }
    }
    public void slowlyMoveTo(int y) {
        mAnimator = ValueAnimator.ofInt(getScrollY(), y);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int nowY = (int) animation.getAnimatedValue();
                scrollTo(0, nowY);
            }
        });
        mAnimator.setDuration((long) Math.abs(getScrollY() - y));
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.start();
    }
    public void slowlyMoveBy(int dy) {
        slowlyMoveTo(getScrollY() + dy);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollViewListener != null) {
            if (oldt == 0) {//在加载时有个CenterTime，这个是用来防止开始加载就回调滑动值
                return;
            }
            if (mIsOpenScrollCallBack) {
                mOnScrollViewListener.onScrollChanged(t);
            }
        }
    }

    public interface IRectView {
        boolean isClick(int y);
        void setOnDataChangeListener(OnDataChangeListener l);
        void setData(List<TaskBean> taskBeans);
        void isAllowDraw(boolean isAllowDraw);
        void longPress(int y);
        void refresh(int y);
        void refreshRect();
        int getUpperLimit();
        int getLowerLimit();
        RectImgView getImgViewRect();
        TaskBean getClickTaskBean();
    }
    public interface IChildLayout {
        void setOnDataChangeListener(OnDataChangeListener l);
        void isAllowDraw(boolean isAllowDraw);
    }
    public interface OnScrollViewListener {
        /**
         * 设置了CenterTime后的滑动值不会返回
         * @param y 当前ScrollView的ScrollY
         */
        void onScrollChanged(int y);
    }
    public interface OnDataChangeListener {
        void onDataIncrease(TaskBean newData);
        void onDataDelete(TaskBean deletedData);
        void onDataAlter(TaskBean alterData);
    }

    private static final String TAG = "123";
}
