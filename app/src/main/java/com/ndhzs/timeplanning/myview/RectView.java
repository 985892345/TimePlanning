package com.ndhzs.timeplanning.myview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

public class RectView extends View {

    private Context context;
    private int mIntervalLeft;//左边的文字间隔宽度
    private int mIntervalRight = 10;//右边的间隔宽度
    private int mIntervalHeight;//一个小时的间隔高度
    private int mExtraHeight;//上方或下方其中一方多余的高度
    private int mWidth, mHeight;//View的长宽
    private int mStartHour, mEndHour;

    private final int radius = 8;//圆角矩形的圆角半径
    private Paint mInsidePaint;//圆角矩形内部画笔

    private Paint mBorderPaint;//圆角矩形边框画笔
    private final float mBorderWidth = 4;//圆角矩形边框厚度

    private Paint mVLinePaint;//Vertical Line 垂直线画笔
    private final float mVLineWidth = 2;//垂直线厚度

    private Paint mHLinePaint;//Horizontal Line 水平线画笔
    private final float mHLineWidth = 1;//水平线厚度

    private Paint mTextPaint;//任务名称文字画笔
    private Paint mTimePaint;//侧边时间文字画笔
    private float mInitialY;//长按生成矩形时的最高点和最低点
    private float mTextCenterDistance, mTimeCenterDistance;//文字的水平线高度
    private TimeSelectView mMyScrollView;
    private GestureDetector mGesture;
    private RectF rect = new RectF(0, 0, 0, 0);//初始矩形
    private List<RectF> rects = new ArrayList<>();

    public RectView(Context context) {
        super(context);
        this.context = context;
        init();
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

    @Override
    protected void onDraw(Canvas canvas) {
        //先画矩形的过程
        canvas.drawRoundRect(rect, radius, radius, mInsidePaint);
        canvas.drawRoundRect(rect, radius, radius, mBorderPaint);

        for (RectF rect : rects) {
            //画矩形 ，成品
            //Log.d("123", "FOR");
            float baseline = rect.centerY() + mTextCenterDistance;//任务文字相对于矩形的水平线高度
            canvas.drawRoundRect(rect, radius, radius, mInsidePaint);
            canvas.drawRoundRect(rect, radius, radius, mBorderPaint);
            canvas.drawText("任务名称", rect.centerX(), baseline, mTextPaint);
        }

        canvas.drawLine(mIntervalLeft, 0, mIntervalLeft, mHeight, mVLinePaint);
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
            float baseline = y + mTimeCenterDistance;//时间文字相对于矩形的水平线高度
            canvas.drawText(hour, mIntervalLeft /2.0f, baseline, mTimePaint);
            canvas.drawLine(mIntervalLeft, y, mWidth - mIntervalRight, y, mHLinePaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        mWidth = w;
        mHeight = h;
    }

    private void init() {
        setClickable(true);
        mGesture = new GestureDetector(context, new GestureListener());
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

        mTimePaint = new Paint();
        mTimePaint.setColor(Color.parseColor("#505050"));
        mTimePaint.setAntiAlias(true);
        mTimePaint.setTextAlign(Paint.Align.CENTER);
    }

    private Paint generatePaint(boolean antiAlias, Paint.Style style, float strokeWidth) {
        Paint paint = new Paint();
        paint.setAntiAlias(antiAlias);//抗锯齿
        paint.setStyle(style);
        paint.setStrokeWidth(strokeWidth);
        return paint;
    }

    public void setRectColor(int borderColor, int insideColor) {
        mBorderPaint.setColor(borderColor);
        mInsidePaint.setColor(insideColor);
    }

    public void setTimeTextSize(int timeTextSize, int taskTextSize) {
        mTimePaint.setTextSize(50);
        mTextPaint.setTextSize(60);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextCenterDistance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        fontMetrics = mTimePaint.getFontMetrics();
        mTimeCenterDistance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
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

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public boolean getSizeXY(float x, float y) {
        for (int i = 0; i < rects.size(); i++) {
            if (rects.get(i).contains(x, y)) {
                rects.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGesture.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                if (y > mInitialY) {
                    rect.bottom = y;
                    rect.top = mInitialY + mBorderWidth/2 + mHLineWidth/2;
                }else {
                    rect.bottom = mInitialY - mBorderWidth/2 - mHLineWidth/2;
                    rect.top = y;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                float area = (rect.right - rect.left) * (rect.bottom - rect.top);
                if (area > 2000) {
                    rects.add(new RectF(rect.left, rect.top, rect.right, rect.bottom));
                }
                rect.setEmpty();
                invalidate();
                break;
        }
        return true;
    }

    private static String TAG = "123";
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            if (mMyScrollView == null) {
                mMyScrollView = (TimeSelectView) getParent();
            }
            mMyScrollView.setIsShortPress(true);
            return super.onDown(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mMyScrollView.setIsShortPress(false);
            if (getSizeXY(e.getX(), e.getY())) {
                //长按移动整体
            }else {
                //长按开启选取
                float y = e.getY();
                mInitialY = (int)((y - mExtraHeight)/mIntervalHeight) * mIntervalHeight + mExtraHeight;
                float left, top, right, bottom;
                left = mIntervalLeft + mVLineWidth/2 + mBorderWidth/2;
                top = mInitialY + mHLineWidth/2 + mBorderWidth/2;
                right = mWidth - mIntervalRight - mBorderWidth/2;
                bottom = y;
                rect.set(left, top, right, top);
                ValueAnimator animator = ValueAnimator.ofFloat(top, y);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        rect.bottom = (float) animation.getAnimatedValue();
                        invalidate();
                    }
                });
                animator.setDuration((int)Math.sqrt(bottom - top)*20);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.start();
            }
        }
    }
}
