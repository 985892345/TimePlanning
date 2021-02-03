package com.ndhzs.timeplanning.myview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

    private final int radius = 8;//圆角矩形的圆角半径
    private Paint mInsidePaint;//圆角矩形内部画笔

    private Paint mBorderPaint;//圆角矩形边框画笔
    private final int mBorderWidth = 4;//圆角矩形边框厚度

    private Paint mVLinePaint;//Vertical Line 垂直线画笔
    private final int mVLineWidth = 2;//垂直线厚度

    private Paint mHLinePaint;//Horizontal Line 水平线画笔
    private final int mHLineWidth = 1;//水平线厚度

    private Paint mTextPaint;//任务名称文字画笔
    private Paint mLeftTimePaint;//左侧时间文字画笔
    private Paint mRectTimePaint;//矩形内部时间文字画笔
    private int mInitialRectY;//长按生成矩形时的不动的y值
    private int mUpperLimit, mLowerLimit;//当前矩形的上下限，不能移动到其他矩形区域
    private float mTextCenter, mLeftTimeCenter;//文字的水平线高度
    private float mRectTimeAscent, mRectTimeDescent;//矩形内部时间的ascent和descent线
    private TimeSelectView mMyScrollView;
    private ViewGroup mMyScrViewParent;
    private RectImgView mImgView;
    private final Rect initialRect = new Rect(-40, -40, -40, -40);//初始矩形
    private Rect deletedRect;//被删掉的矩形
    private RectF rectF;
    private final List<Rect> rects = new ArrayList<>();
    private final HashMap<Rect, String> rectAndName = new HashMap<>();

    private boolean mIsContain;//长按已选择的区域后为true
    private int mContainedX, mContainedY;//长按已选择的区域时的坐标

    private static final int X_MOVE_THRESHOLD = 50;

    public RectView(Context context) {
        super(context);
        this.context = context;
        init();
    }
    private void init() {
        rectF = new RectF();
        deletedRect = new Rect();
        //圆角矩形内部画笔
        mInsidePaint = generatePaint(true, Paint.Style.FILL, 10);
        //圆角矩形边框画笔
        mBorderPaint = generatePaint(true, Paint.Style.STROKE, mBorderWidth);
        //Vertical Line 垂直线画笔
        mVLinePaint = generatePaint(false, Paint.Style.FILL, mVLineWidth);
        mVLinePaint.setColor(0xFFC8C8C8);
        //Horizontal Line 水平线画笔
        mHLinePaint = generatePaint(false, Paint.Style.FILL, mHLineWidth);
        mHLinePaint.setColor(0xFF9C9C9C);
        //任务名称文字画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.parseColor("#505050"));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        //左侧时间文字画笔
        mLeftTimePaint = new Paint(mTextPaint);
        //上方时间文字画笔
        mRectTimePaint = new Paint(mTextPaint);
        mRectTimePaint.setTextAlign(Paint.Align.LEFT);
    }

    private Paint generatePaint(boolean antiAlias, Paint.Style style, float strokeWidth) {
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
        mTextPaint.setTextSize(taskTextSize);
        mRectTimePaint.setTextSize(timeTextSize * 0.85f);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextCenter = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        fontMetrics = mLeftTimePaint.getFontMetrics();
        mLeftTimeCenter = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        fontMetrics = mRectTimePaint.getFontMetrics();
        mRectTimeAscent = fontMetrics.ascent;
        mRectTimeDescent = fontMetrics.descent;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //先画矩形的过程
        rectF.set(initialRect);
        canvas.drawRoundRect(rectF, radius, radius, mInsidePaint);
        canvas.drawRoundRect(rectF, radius, radius, mBorderPaint);
        canvas.drawText(calculateTime(initialRect.top - mBorderWidth/2), initialRect.left + 6, initialRect.top - mRectTimeAscent, mRectTimePaint);
        canvas.drawText(calculateTime(initialRect.bottom + mBorderWidth/2), initialRect.left + 6, initialRect.bottom - mRectTimeDescent, mRectTimePaint);

        for (Rect rect : rects) {
            rectF.set(rect);
            canvas.drawRoundRect(rectF, radius, radius, mInsidePaint);
            canvas.drawRoundRect(rectF, radius, radius, mBorderPaint);
            canvas.drawText(rectAndName.get(rect), rect.centerX(), rect.centerY() + mTextCenter, mTextPaint);
            canvas.drawText(calculateTime(rect.top - mBorderWidth/2), rect.left + 6, rect.top - mRectTimeAscent, mRectTimePaint);
            canvas.drawText(calculateTime(rect.bottom + mBorderWidth/2), rect.left + 6, rect.bottom - mRectTimeDescent, mRectTimePaint);
        }

        canvas.drawLine(mIntervalLeft, 0, mIntervalLeft, getHeight(), mVLinePaint);
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
            canvas.drawLine(mIntervalLeft, y, getWidth() - mIntervalRight, y, mHLinePaint);
        }
    }

    private String calculateTime(int y) {
        int hour = ((y - mExtraHeight + mHLineWidth) / mIntervalHeight) + mStartHour;
        int minute = (int)(((y - mExtraHeight + mHLineWidth) % mIntervalHeight) / (float)mIntervalHeight * 60);
        if (y < mExtraHeight - mHLineWidth) {
            hour = mStartHour - 1;
            minute = (int)(((mIntervalHeight - (mExtraHeight - mHLineWidth - y)) % mIntervalHeight) / (float)mIntervalHeight * 60);
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
        return stHour +":" + stMinute;
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

    public void longPress(int x, int y) {
        if (isContain(x, y)) {
            //长按移动整体
            mImgView = new RectImgView(context, deletedRect, radius, mBorderPaint, mInsidePaint, mTextPaint,
                    mRectTimePaint, mRectTimeAscent, mRectTimeDescent, rectAndName.get(deletedRect), this);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = deletedRect.left - mBorderWidth/2;
            lp.topMargin = deletedRect.top - mBorderWidth/2 - mMyScrollView.getScrollY();
            mMyScrViewParent.addView(mImgView, lp);

            mUpperLimit = getUpperLimit(deletedRect.top);
            mLowerLimit = getLowerLimit(deletedRect.bottom);
        }else {
            //长按开启选取
            int left, top, right, bottom;
            left = mIntervalLeft + mVLineWidth/2 + mBorderWidth/2;
            top = (y - mExtraHeight)/mIntervalHeight * mIntervalHeight + mExtraHeight + mBorderWidth/2;
            if (isContainTop(top)) {
                top = mInitialRectY;
            }
            right = getWidth() - mIntervalRight - mBorderWidth/2;
            bottom = y;
            initialRect.set(left, top, right, top);
            mInitialRectY = top;
            ValueAnimator animator = ValueAnimator.ofInt(top, y);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    initialRect.bottom = (int)animation.getAnimatedValue();
                    invalidate();
                }
            });
            animator.setDuration((int)Math.sqrt(bottom - top)*8);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
            mUpperLimit = getUpperLimit(mInitialRectY);
            mLowerLimit = getLowerLimit(mInitialRectY);
        }
    }
    private boolean isContainTop(int y) {
        int x = getWidth()/2;
        y -= mBorderWidth/2;
        for (int i = 0; i < rects.size(); i++) {
            if (rects.get(i).contains(x, y)) {
                mInitialRectY = rects.get(i).bottom + mBorderWidth + 1;
                return true;
            }
        }
        return false;
    }
    private boolean isContain(int x, int y) {
        for (int i = 0; i < rects.size(); i++) {
            if (rects.get(i).contains(x, y)) {
                deletedRect.set(rects.get(i));
                rects.remove(i);
                mIsContain = true;
                mContainedX = x;
                mContainedY = y;
                invalidate();
                return true;
            }
        }
        mIsContain = false;
        return false;
    }
    private int getUpperLimit(int top) {
        List<Integer> bottoms = new ArrayList<>();
        for (int i = 0; i < rects.size(); i++) {
            int bottom = rects.get(i).bottom;
            if (bottom < top) {
                bottoms.add(bottom);
            }
        }
        return (bottoms.size() == 0) ? mExtraHeight:
                Collections.max(bottoms, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                }) + mBorderWidth/2;
    }
    private int getLowerLimit(int bottom) {
        List<Integer> tops = new ArrayList<>();
        for (int i = 0; i < rects.size(); i++) {
            int top = rects.get(i).top;
            if (top > bottom) {
                tops.add(top);
            }
        }
        return (tops.size() == 0) ? getHeight() - mExtraHeight:
                Collections.min(tops, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                }) - mBorderWidth/2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mMyScrollView == null) {
                    mMyScrollView = (TimeSelectView) getParent();
                    mMyScrViewParent = (ViewGroup) mMyScrollView.getParent();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsContain) {
                    if (y > mInitialRectY) {
                        initialRect.bottom = y;
                        initialRect.top = mInitialRectY;
                        if (y > mLowerLimit) {
                            initialRect.bottom = mLowerLimit - mBorderWidth/2 - mHLineWidth;
                        }
                    }else if (y < mInitialRectY) {
                        initialRect.bottom = mInitialRectY - mBorderWidth - mHLineWidth;
                        initialRect.top = y;
                        if (y < mUpperLimit) {
                            initialRect.top = mUpperLimit + mBorderWidth/2 + mHLineWidth;
                        }
                    }
                    invalidate();
                }else {
                    int dx = x - mContainedX;
                    dx = (Math.abs(dx) < X_MOVE_THRESHOLD) ? 0 : (dx > 0) ? dx - X_MOVE_THRESHOLD : dx + X_MOVE_THRESHOLD;
                    mImgView.layout(dx + deletedRect.left - mBorderWidth/2,
                            y - mContainedY + deletedRect.top - mBorderWidth/2 - mMyScrollView.getScrollY());
                    mImgView.invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mIsContain) {
                    if (initialRect.height() > mIntervalHeight * 0.4f) {
                        Rect re = new Rect(initialRect);
                        rects.add(re);
                        rectAndName.put(re, "请设置任务名称！");
                    }
                }else {
                    float dx = x - mContainedX;
                    if (Math.abs(dx) > (getWidth() - mIntervalLeft)*0.35f) {
                        mImgView.animate().x((dx > 0) ? getWidth() : -getWidth())
                                .scaleX(0)
                                .scaleY(0)
                                .setDuration((int)((getWidth() - Math.abs(dx)) * 1.2f))
                                .setInterpolator(new AccelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mMyScrViewParent.removeView(mImgView);
                                    }
                                });
                    }else {
                        int imgViewTop = mImgView.getTop();

                        boolean isOverUpperLimit = imgViewTop + mMyScrollView.getScrollY() < mUpperLimit + mHLineWidth;
                        boolean isOverLowerLimit = mImgView.getBottom() + mMyScrollView.getScrollY() > mLowerLimit - mHLineWidth;

                        int trueY  = isOverUpperLimit ?
                                mUpperLimit + mHLineWidth - mMyScrollView.getScrollY():
                                (isOverLowerLimit ?
                                        mLowerLimit - mHLineWidth - mImgView.getHeight() - mMyScrollView.getScrollY() :
                                        imgViewTop);
                        mImgView.animate().x(mIntervalLeft + mVLineWidth/2.0f)
                                .y(trueY)
                                .setDuration((int)(Math.abs(dx) * 1.2f))
                                .setInterpolator(new DecelerateInterpolator())
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        int left, top, right, bottom;
                                        left = deletedRect.left;
                                        top = isOverUpperLimit ?
                                                trueY + mMyScrollView.getScrollY() + mBorderWidth/2 :
                                                (isOverLowerLimit ?
                                                        mLowerLimit - mHLineWidth - deletedRect.height() - mBorderWidth/2 :
                                                        trueY + mMyScrollView.getScrollY() + mBorderWidth/2);
                                        right = deletedRect.right;
                                        bottom = top + deletedRect.height();
                                        Rect re = new Rect(left, top, right, bottom);
                                        rects.add(re);
                                        rectAndName.put(re, "请设置任务名称！");
                                        mMyScrViewParent.removeView(mImgView);
                                        invalidate();
                                    }
                                });
                    }
                }
                initialRect.set(-40, -40, -40, -40);
                invalidate();
                break;
        }
        return true;
    }

    public int getScrollViewScrollY() {
        return mMyScrollView.getScrollY();
    }
    public String calculateImgViewTime(boolean isTop) {
        return calculateTime((isTop ? mImgView.getTop() : mImgView.getBottom()) + mMyScrollView.getScrollY());
    }

    private static final String TAG = "123";
}
