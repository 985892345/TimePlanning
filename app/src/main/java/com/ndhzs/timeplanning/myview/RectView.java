package com.ndhzs.timeplanning.myview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class RectView extends View {

    private int mIntervalWidth;//左边的文字间隔宽度
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
    private float mInitialX, mInitialY;//开始点击时的位置
    private float mTextCenterDistance, mTimeCenterDistance;//文字的水平线高度
    private RectF rect = new RectF(0, 0, 0, 0);//初始矩形
    private List<RectF> rects = new ArrayList<>();

    private boolean isRemove;

    public RectView(Context context) {
        super(context);
        init();
    }

    public void setHour(int startHour, int endHour) {
        this.mStartHour = startHour;
        this.mEndHour = endHour;
    }

    public void setInterval(int intervalWidth, int intervalHeight, int extraHeight) {
        this.mIntervalWidth = intervalWidth;
        this.mIntervalHeight = intervalHeight;
        this.mExtraHeight = extraHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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

        canvas.drawLine(mIntervalWidth, 0, mIntervalWidth, mHeight, mVLinePaint);
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
            canvas.drawText(hour, mIntervalWidth/2.0f, baseline, mTimePaint);
            canvas.drawLine(mIntervalWidth, y, mWidth - 10, y, mHLinePaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        mWidth = w;
        mHeight = h;
    }

    private void init() {
        setClickable(true);
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
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d("123", "onTouchEvent: ");
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getSizeXY(x, y)) {
                    isRemove = false;
//                    performClick();//点击事件Click
                }else {
                    isRemove = true;
                    mInitialX = x;
                    mInitialY = y;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRemove) {
                    float left, right, top, bottom;
                    if (mInitialX < x) {
                        left = mInitialX;
                        right = x;
                    }else {
                        left = x;
                        right = mInitialX;
                    }
                    if (mInitialY < y) {
                        top = mInitialY;
                        bottom = y;
                    }else {
                        top = y;
                        bottom = mInitialY;
                    }
                    rect.set(left, top, right, bottom);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isRemove) {
                    float area = (rect.right - rect.left) * (rect.bottom - rect.top);
                    if (area > 2000) {
                        rects.add(new RectF(rect.left, rect.top, rect.right, rect.bottom));
                    }
                }
                rect.setEmpty();
                invalidate();
                break;
        }
        return true;
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
}
