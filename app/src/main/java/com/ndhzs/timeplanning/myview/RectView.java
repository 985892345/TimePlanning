package com.ndhzs.timeplanning.myview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class RectView extends View {

    private final Context context;
    private int mIntervalLeft;//左边的文字间隔宽度
    private final int mIntervalRight = 10;//右边的间隔宽度
    private int mIntervalHeight;//一个小时的间隔高度
    private int mExtraHeight;//上方或下方其中一方多余的高度
    private int mStartHour, mEndHour;
    private int mLastHour, mLastMinute;//上一次计算时间的值
    private String mStDTime;
    private HashMap<Rect, String> mRectAndDTime = new HashMap<>();//矩形与时间段差值

    private final int radius = 8;//圆角矩形的圆角半径
    private Paint mInsidePaint;//圆角矩形内部画笔

    private Paint mBorderPaint;//圆角矩形边框画笔
    private final int mBorderWidth = 4;//圆角矩形边框厚度

    private Paint mVLinePaint;//Vertical Line 垂直线画笔
    private final int mVLineWidth = 2;//垂直线厚度

    private Paint mHLinePaint;//Horizontal Line 水平线画笔
    private final int mHLineWidth = 1;//水平线厚度

    private Paint mArrowsPaint;//时间差的箭头线画笔

    private Paint mLeftTimePaint;//左侧时间画笔
    private Paint mRectTimePaint;//上下线时间画笔
    private Paint mStartTimePaint;//开始时间画笔
    private Paint mTextPaint;//任务名称画笔
    private Paint mDTimePaint;//时间差值画笔
    private final Path mArrowsPath = new Path();//箭头的路径
    private int mInitialRectY;//长按生成矩形时的不动的y值
    private int mUpperLimit, mLowerLimit;//当前矩形的上下限，不能移动到其他矩形区域
    private int mContainedX, mContainedY;//长按已选择的区域时的坐标
    private float mTextCenter, mLeftTimeCenter, mDTimeCenter;//文字的水平线高度
    private float mRectTimeAscent, mRectTimeDescent;//矩形内部时间的ascent和descent线
    private float mDTimeHalfHeight;//右侧时间的高度的一半
    private TimeSelectView mMyScrollView;
    private ViewGroup mMyScrViewParent;
    private RectImgView mImgView;
    private final Rect initialRect = new Rect(-100, -100, -100, -100);//初始矩形
    private final Rect deletedRect = new Rect();//被删掉的矩形
    private final RectF rectF = new RectF();;//用来给圆角矩形转换
    private final List<Rect> rects = new ArrayList<>();
    private final HashMap<Rect, String> mRectAndName = new HashMap<>();

    private boolean mIsFromHLine;//起点是从水平线开始的

    private int WHICH_CONDITION = 0;//保存长按的情况

    private static final int X_KEEP_THRESHOLD = 50;//长按后左右移动时保持水平不移动的阀值
    private static final float X_MOVE_THRESHOLD = 0.4f;//长按后左右移动删除的阀值，为垂直线右边宽度的倍数
    private static final int TOP = 1;//长按的顶部区域
    private static final int INSIDE = 2;//长按的内部区域
    private static final int BOTTOM = 3;//长按的底部区域
    private static final int EMPTY_AREA = 4;//长按的空白区域
    private static final int TOP_BOTTOM_WIDTH = 10;//顶部和底部区域的宽度
    private static final int START_TIME_INTERVAL = 5;//按下空白区域时起始时间的分钟间隔数(必须为60的因数)

    private static float RECT_MIN_HEIGHT;//矩形最小高度，控制矩形能否保存,与字体大小有关
    private static float RECT_LESSER_HEIGHT;//矩形较小高度，控制矩形上下线时间能否显示,与字体大小有关
    private static float RECT_SHOE_START_TIME_HEIGHT;//矩形显示开始时间的最小高度，与字体大小有关

    /**
     * 注意！该View是镶嵌于TimeSelectView中，请不要自己用addView()调用，除非你搞懂了原理
     * @param context 传入context
     */
    public RectView(Context context) {
        super(context);
        this.context = context;
        init();
    }
    private void init() {
        mRectAndDTime.put(initialRect, "");
        //圆角矩形边框画笔
        mBorderPaint = generatePaint(true, Paint.Style.STROKE, mBorderWidth);
        //圆角矩形内部画笔
        mInsidePaint = generatePaint(true, Paint.Style.FILL, 10);
        //Vertical Line 垂直线画笔
        mVLinePaint = generatePaint(false, Paint.Style.FILL, mVLineWidth);
        mVLinePaint.setColor(0xFFC8C8C8);
        //Horizontal Line 水平线画笔
        mHLinePaint = generatePaint(false, Paint.Style.FILL, mHLineWidth);
        mHLinePaint.setColor(0xFF9C9C9C);
        //时间差的箭头线画笔
        mArrowsPaint = generatePaint(false, Paint.Style.STROKE, 2);
        mArrowsPaint.setColor(0xFF000000);
        //左侧时间画笔
        mLeftTimePaint = new Paint();
        mLeftTimePaint.setColor(0xFF505050);
        mLeftTimePaint.setAntiAlias(true);
        mLeftTimePaint.setTextAlign(Paint.Align.CENTER);
        //上下线时间画笔
        mRectTimePaint = new Paint(mLeftTimePaint);
        mRectTimePaint.setColor(0xFF000000);
        mRectTimePaint.setTextAlign(Paint.Align.LEFT);
        //开始时间画笔
        mStartTimePaint = new Paint(mRectTimePaint);
        mStartTimePaint.setTextAlign(Paint.Align.CENTER);
        //任务名称画笔
        mTextPaint = new Paint(mLeftTimePaint);
        //时间差值画笔
        mDTimePaint = new Paint(mRectTimePaint);
        mDTimePaint.setTextAlign(Paint.Align.RIGHT);
    }

    private Paint generatePaint(boolean antiAlias, Paint.Style style, int strokeWidth) {
        Paint paint = new Paint();
        paint.setAntiAlias(antiAlias);//抗锯齿
        paint.setStyle(style);
        paint.setStrokeWidth(strokeWidth);
        return paint;
    }
    public void setHour(int startHour, int endHour) {
        this.mStartHour = startHour;
        this.mEndHour = endHour;
    }
    public void setInterval(int intervalWidth, int intervalHeight, int extraHeight) {
        this.mIntervalLeft = intervalWidth;
        this.mIntervalHeight = intervalHeight;
        this.mExtraHeight = extraHeight;
    }
    public void setRectColor(int borderColor, int insideColor) {
        mBorderPaint.setColor(borderColor);
        mInsidePaint.setColor(insideColor);
    }
    public void setTextSize(int timeTextSize, int taskTextSize) {
        mLeftTimePaint.setTextSize(timeTextSize);
        mRectTimePaint.setTextSize(timeTextSize * 0.8f);
        mStartTimePaint.setTextSize(timeTextSize * 0.8f);
        mTextPaint.setTextSize(taskTextSize);
        mDTimePaint.setTextSize(timeTextSize * 0.75f);

        Paint.FontMetrics fontMetrics = mLeftTimePaint.getFontMetrics();
        mLeftTimeCenter = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;

        fontMetrics = mRectTimePaint.getFontMetrics();
        mRectTimeAscent = fontMetrics.ascent;
        mRectTimeDescent = fontMetrics.descent;
        RECT_LESSER_HEIGHT = (mRectTimeDescent - mRectTimeAscent) * 2;

        fontMetrics = mStartTimePaint.getFontMetrics();
        RECT_SHOE_START_TIME_HEIGHT = fontMetrics.descent - fontMetrics.ascent - 2 * mBorderWidth;

        fontMetrics = mTextPaint.getFontMetrics();
        mTextCenter = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        RECT_MIN_HEIGHT = fontMetrics.descent - fontMetrics.ascent - 2 * mBorderWidth;

        fontMetrics = mDTimePaint.getFontMetrics();
        mDTimeHalfHeight = (fontMetrics.bottom - fontMetrics.top)/2;
        mDTimeCenter = mDTimeHalfHeight - fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawRect(canvas, initialRect, null);
        if (initialRect.height() > RECT_SHOE_START_TIME_HEIGHT) {
            canvas.drawText(calculateTime(initialRect.top - mBorderWidth / 2, false),
                    initialRect.centerX(), initialRect.top - mRectTimeAscent - mBorderWidth, mStartTimePaint);
        }
        if (initialRect.height() > RECT_LESSER_HEIGHT) {
            drawTopBottomTime(canvas, initialRect, null, null);
        }
        if (initialRect.height() > RECT_MIN_HEIGHT) {
            drawArrows(canvas, initialRect, null);
        }
        for (Rect rect : rects) {
            drawRect(canvas, rect, mRectAndName.get(rect));
            drawArrows(canvas, rect, mRectAndDTime.get(rect));
            drawTopBottomTime(canvas, rect, null, null);
        }
        canvas.drawLine(mIntervalLeft, 0, mIntervalLeft, getHeight(), mVLinePaint);//这是左边的竖线
        for (int i = mStartHour; i <= mEndHour; i++) {
            String hour;
            if (i < 10) {
                hour = "0" + i + ":00";
            }else if (i < 24){
                hour = i + ":00";
            }else {
                hour = "0" + i%24 + ":00";
            }
            int y = mExtraHeight + mIntervalHeight * (i - mStartHour);
            float baseline = y + mLeftTimeCenter;//时间文字相对于矩形的水平线高度
            canvas.drawText(hour, mIntervalLeft /2.0f, baseline, mLeftTimePaint);
            canvas.drawLine(mIntervalLeft, y - mHLineWidth/2.0f, getWidth() - mIntervalRight, y - mHLineWidth/2.0f, mHLinePaint);
        }
    }
    public void drawRect(Canvas canvas, Rect rect, String taskName) {
        rectF.set(rect);
        canvas.drawRoundRect(rectF, radius, radius, mInsidePaint);
        canvas.drawRoundRect(rectF, radius, radius, mBorderPaint);
        if (taskName != null)
            canvas.drawText(taskName, rect.centerX(), rect.centerY() + mTextCenter, mTextPaint);
    }
    public void drawArrows(Canvas canvas, Rect rect, String stDTime) {
        int timeRight = rect.right -3;
        if (stDTime != null) {
            canvas.drawText(stDTime,
                    timeRight, rect.centerY() + mDTimeCenter, mDTimePaint);
        }else {
            calculateTime(rect.top - mBorderWidth / 2, false);
            canvas.drawText(calculateTime(rect.bottom + mBorderWidth / 2, true),
                    timeRight, rect.centerY() + mDTimeCenter, mDTimePaint);
        }
        {
            int horizontalInterval = 5;
            int verticalInterval = 9;
            int verticalCenter = rect.right - 25;
            int left = verticalCenter - horizontalInterval;
            int top = rect.top + 4;
            int right = verticalCenter + horizontalInterval;
            int bottom = rect.bottom - 4;

            mArrowsPath.moveTo(verticalCenter, top);
            mArrowsPath.lineTo(verticalCenter, rect.centerY() - mDTimeHalfHeight);

            mArrowsPath.moveTo(verticalCenter, rect.centerY() + mDTimeHalfHeight);
            mArrowsPath.lineTo(verticalCenter, bottom);

            mArrowsPath.moveTo(left, top + verticalInterval);
            mArrowsPath.lineTo(verticalCenter, top);
            mArrowsPath.lineTo(right, top + verticalInterval);

            mArrowsPath.moveTo(left, bottom - verticalInterval);
            mArrowsPath.lineTo(verticalCenter, bottom);
            mArrowsPath.lineTo(right, bottom - verticalInterval);

            canvas.drawPath(mArrowsPath, mArrowsPaint);
            mArrowsPath.rewind();
        }
    }
    public void drawTopBottomTime(Canvas canvas, Rect rect, String topTime, String bottomTime) {
        int left = rect.left + 3;
        if (topTime == null) {
            canvas.drawText(calculateTime(rect.top - mBorderWidth / 2, false),
                    left, rect.top - mRectTimeAscent - mBorderWidth, mRectTimePaint);
            canvas.drawText(calculateTime(rect.bottom + mBorderWidth / 2, false),
                    left, rect.bottom - mRectTimeDescent + mBorderWidth, mRectTimePaint);
        }else {
            canvas.drawText(topTime,
                    left, rect.top - mRectTimeAscent - mBorderWidth, mRectTimePaint);
            canvas.drawText(bottomTime,
                    left, rect.bottom - mRectTimeDescent + mBorderWidth, mRectTimePaint);
        }
    }

    public String calculateTime(int y, boolean isCalculateDifference) {
        int hour = ((y - mExtraHeight + mHLineWidth) / mIntervalHeight) + mStartHour;
        int minute = (int)(((y - mExtraHeight + mHLineWidth) % mIntervalHeight) / (float)mIntervalHeight * 60);//计算出一格占多少分钟
        if (y < mExtraHeight - mHLineWidth) {
            hour = mStartHour - 1;
            minute = (int)(((mIntervalHeight - (mExtraHeight - mHLineWidth - y)) % mIntervalHeight) / (float)mIntervalHeight * 60);
        }
        if (isCalculateDifference) {
            if (minute >= mLastMinute) {
                minute = minute - mLastMinute;
                hour -= mLastHour;
            }else {
                minute = minute + 60 - mLastMinute;
                hour -= mLastHour + 1;
            }
        }else {
            mLastHour = hour;
            mLastMinute = minute;
        }
        String stHour;
        String stMinute;
        if (hour < 10) {
            stHour = "0" + hour;
        }else if (hour < 24){
            stHour = hour + "";
        }else {
            stHour = "0" + hour%24;
        }
        if (minute < 10) {
            stMinute = "0" + minute;
        }else {
            stMinute = minute + "";
        }
        if (isCalculateDifference) {
            mStDTime = stHour + ":" + stMinute;
            return mStDTime;
        }
        return stHour + ":" + stMinute;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                int height = 2 * mExtraHeight + (mEndHour - mStartHour) * mIntervalHeight;
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY:
                break;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int isContain(int x, int y) {
        for (int i = 0; i < rects.size(); i++) {
            Rect rect = rects.get(i);
            if (rect.contains(x, y)) {
                if (y + TOP_BOTTOM_WIDTH > rect.bottom + mBorderWidth/2) {
                    return BOTTOM;
                }else if (y - TOP_BOTTOM_WIDTH < rect.top - mBorderWidth/2) {
                    return TOP;
                }else {
                    deletedRect.set(rects.get(i));
                    rects.remove(i);
                    invalidate();
                    return INSIDE;
                }
            }
        }
        return EMPTY_AREA;
    }
//    private int getTrueTop(int y) {
//        int hLineHeight = (y - mExtraHeight) / mIntervalHeight * mIntervalHeight + mExtraHeight;
//        int top = y - mBorderWidth/2;
//        float everyMinuteWidth = 1 / 60.0f * mIntervalHeight;//计算出一分钟要多少格，用小数表示
//        float[] everyMinuteHeight = new float[]{61};//保存0~60的每分钟高度
//        for (int i = 0; i < 60; i++) {
//            everyMinuteHeight[i] = i * everyMinuteWidth;
//        }
//        everyMinuteHeight[61] = mIntervalHeight;
//        if (top <= hLineHeight - mHLineWidth) {
//            top = hLineHeight;
//        }else {
//            int i =
//        }
//    }
    private int getUpperLimit(int top) {
        List<Integer> bottoms = new ArrayList<>();
        for (int i = 0; i < rects.size(); i++) {
            int bottom = rects.get(i).bottom;
            if (bottom < top) {
                bottoms.add(bottom);
            }
        }
        return (bottoms.size() == 0) ? mExtraHeight :
                Collections.max(bottoms, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                }) + mBorderWidth/2 + mHLineWidth;
    }
    private int getLowerLimit(int bottom) {
        List<Integer> tops = new ArrayList<>();
        for (int i = 0; i < rects.size(); i++) {
            int top = rects.get(i).top;
            if (top > bottom) {
                tops.add(top);
            }
        }
        return (tops.size() == 0) ? getHeight() - mExtraHeight - mHLineWidth:
                Collections.min(tops, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                }) - mBorderWidth/2 - mHLineWidth;
    }

    /**
     * 此为TimeSelectView的dispatchTouchEvent()在长按情况下用延时调用的方法，调用该方法代表长按已经产生。
     * 在我写的代码中，传入的y的值是大于或等于额外宽度mExtraHeight的
     * @param x 传入x值
     * @param y 传入y值。注意！请判断是否是RectView的坐标系，若不是记得加上TimeSelectView.getScrollY()
     */
    public void longPress(int x, int y) {
        switch (WHICH_CONDITION = isContain(x, y)) {
            case EMPTY_AREA: {
                //长按开启选取，先识别了位置，再加载了动画
                int left, top, right, bottom;
                left = mIntervalLeft + mVLineWidth / 2 + mBorderWidth / 2;
                top = y + mBorderWidth / 2;
                mIsFromHLine = false;
                int hLineHeight = (y - mExtraHeight) / mIntervalHeight * mIntervalHeight + mExtraHeight;
                if (y < hLineHeight + 1 / 60.0f * mIntervalHeight + 1) {//此处是判断如果y是在十分接近水平线时，就从水平线开始
                    mIsFromHLine = true;
                    top = hLineHeight + mBorderWidth / 2;
                }
                right = getWidth() - mIntervalRight - mBorderWidth / 2;
                bottom = top + 1;
                initialRect.set(left, top, right, bottom);
                mInitialRectY = top;
                mUpperLimit = getUpperLimit(mInitialRectY);
                mLowerLimit = getLowerLimit(mInitialRectY);
                break;
            }
            case INSIDE: {
                //长按移动整体，先生成一个可以移动的矩形
                mImgView = new RectImgView(context, deletedRect, mBorderWidth,
                        mRectAndName.get(deletedRect), mRectAndDTime.get(deletedRect), this);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(deletedRect.width() + mBorderWidth, deletedRect.height() + mBorderWidth);
                lp.leftMargin = deletedRect.left - mBorderWidth / 2;
                lp.topMargin = deletedRect.top - mBorderWidth / 2 - mMyScrollView.getScrollY();
                mMyScrViewParent.addView(mImgView, lp);
                mContainedX = x;//记录长按坐标
                mContainedY = y;
                mUpperLimit = getUpperLimit(deletedRect.top);
                mLowerLimit = getLowerLimit(deletedRect.bottom);
                break;
            }
            case TOP: {}
            case BOTTOM: {
                break;
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (mMyScrollView == null) {
                    mMyScrollView = (TimeSelectView) getParent();
                    mMyScrViewParent = (ViewGroup) mMyScrollView.getParent();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                theMoveEvent(x, y);
                break;
            }
            case MotionEvent.ACTION_UP: {
                theUpEvent(x, y);
                break;
            }
        }
        return true;
    }
    private void theMoveEvent(int x, int y) {
        switch (WHICH_CONDITION) {
            case EMPTY_AREA: {
                if (y >= mInitialRectY) {
                    initialRect.bottom = y;
                    initialRect.top = mInitialRectY;//防止从y < mInitialRectY回来top为最后一次y的值
                    if (y + mBorderWidth / 2 >= mLowerLimit) {
                        initialRect.bottom = mLowerLimit - mBorderWidth / 2;
                    }
                }else {
                    initialRect.bottom = mInitialRectY - mBorderWidth;
                    if (mIsFromHLine)
                        initialRect.bottom = mInitialRectY - mBorderWidth - mHLineWidth;
                    initialRect.top = y;
                    if (y < mUpperLimit) {
                        initialRect.top = mUpperLimit + mBorderWidth / 2;
                    }
                }
                invalidate();
                break;
            }
            case INSIDE: {
                int dx = x - mContainedX;
                dx = (Math.abs(dx) < X_KEEP_THRESHOLD) ? 0 : ((dx > 0) ? dx - X_KEEP_THRESHOLD : dx + X_KEEP_THRESHOLD);
                mImgView.layout(dx + deletedRect.left - mBorderWidth / 2,
                        y - mContainedY + deletedRect.top - mBorderWidth / 2 - mMyScrollView.getScrollY());
                break;
            }
            case TOP: {}
            case BOTTOM: {
                break;
            }
        }
    }
    private void theUpEvent(int x, int y) {
        switch (WHICH_CONDITION) {
            case EMPTY_AREA: {
                if (initialRect.height() > RECT_MIN_HEIGHT) {
                    Rect re = new Rect(initialRect);
                    rects.add(re);
                    mRectAndName.put(re, "请设置任务名称！");
                    mRectAndDTime.put(re, mStDTime);
                }
                initialRect.set(-40, -40, -40, -40);
                invalidate();
                break;
            }
            case INSIDE: {
                int dx = x - mContainedX;
                if (Math.abs(dx) > (getWidth() - mIntervalLeft) * X_MOVE_THRESHOLD) {
                    mImgView.animate().x((dx > 0) ? getWidth() : -getWidth())
                            .scaleX(0)
                            .scaleY(0)
                            .setDuration((int) ((getWidth() - Math.abs(dx)) * 1.2f))
                            .setInterpolator(new AccelerateInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mMyScrViewParent.removeView(mImgView);
                                }
                            });
                    mRectAndName.remove(deletedRect);
                    mRectAndDTime.remove(deletedRect);
                }else {
                    int imgViewTop = mImgView.getTop();

                    boolean isOverUpperLimit = imgViewTop + mMyScrollView.getScrollY() < mUpperLimit;
                    boolean isOverLowerLimit = mImgView.getBottom() + mMyScrollView.getScrollY() > mLowerLimit;

                    int trueY = isOverUpperLimit ?
                            mUpperLimit - mMyScrollView.getScrollY() :
                            (isOverLowerLimit ?
                                    mLowerLimit - mImgView.getHeight() - mMyScrollView.getScrollY() :
                                    imgViewTop);
                    mImgView.animate().x(mIntervalLeft + mVLineWidth / 2.0f)
                            .y(trueY)
                            .setDuration((int) (Math.abs(dx) * 1.2f))
                            .setInterpolator(new DecelerateInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    int left, top, right, bottom;
                                    left = deletedRect.left;
                                    top = trueY + mMyScrollView.getScrollY() + mBorderWidth / 2;
                                    right = deletedRect.right;
                                    bottom = top + deletedRect.height();
                                    Rect re = new Rect(left, top, right, bottom);
                                    rects.add(re);
                                    mRectAndName.put(re, mRectAndName.get(deletedRect));
                                    mRectAndDTime.put(re, mRectAndDTime.get(deletedRect));
                                    mMyScrViewParent.removeView(mImgView);
                                    invalidate();
                                    if (!re.equals(deletedRect)) {
                                        mRectAndName.remove(deletedRect);
                                        mRectAndDTime.remove(deletedRect);
                                    }
                                }
                            });
                }
                break;
            }
            case TOP: {}
            case BOTTOM: {
                break;
            }
        }
    }

    public int getScrollViewScrollY() {
        return mMyScrollView.getScrollY();
    }

    private static final String TAG = "123";
}
