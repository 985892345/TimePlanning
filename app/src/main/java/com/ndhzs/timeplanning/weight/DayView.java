package com.ndhzs.timeplanning.weight;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.timeselectview.FrameView;

public class DayView extends View {

    private Paint mDayPaint;
    private Paint mCalenderPaint;
    private Paint mCirclePaint;
    private final int mExtraHeight = 20;
    private final int mColorWhite = 0xFFFFFFFF;
    private final int mColorBlack = 0xFF000000;
    private final int mCircleColor;
    private final int mCalenderColor = 0xFF828282;//阴历的文字颜色，默认灰色
    private float mCirclePosition = 4;
    private float mCircleRadius;
    private float mCircleDrawHeight;
    private final float mTextSize;
    private float mDayTextHeight;
    private float mDayTextDrawHeight;
    private float mCalenderTextDrawHeight;
    private String[] mWeek = new String[]{"14", "15", "16", "17", "18", "19", "20"};
    private String[] mCalender = new String[]{"情人节", "初四", "初五", "初六", "雨水", "初八", "初九"};

    public DayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ty = context.obtainStyledAttributes(attrs, R.styleable.DayView);
        mCircleColor = ty.getColor(R.styleable.DayView_circleColor, ContextCompat.getColor(context, R.color.default_color));
        mTextSize = ty.getDimension(R.styleable.DayView_dayTextSize, 60);
        ty.recycle();
        init();
    }

    private void init() {
        mDayPaint = new Paint();
        mDayPaint.setColor(mColorBlack);
        mDayPaint.setAntiAlias(true);
        mDayPaint.setTextSize(mTextSize);
        mDayPaint.setFakeBoldText(true);
        mDayPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = mDayPaint.getFontMetrics();
        mDayTextHeight = fontMetrics.descent - fontMetrics.ascent;
        mDayTextDrawHeight = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom + mDayTextHeight / 2 + mExtraHeight;
        mCircleDrawHeight = mDayTextDrawHeight + mDayTextHeight * 0.03f;
        mCircleRadius = mDayTextHeight * 0.8f;
        mCalenderPaint = new Paint();
        mCalenderPaint.setColor(mCalenderColor);
        mCalenderPaint.setAntiAlias(true);
        mCalenderPaint.setTextSize(0.5f * mTextSize);
        mCalenderPaint.setTextAlign(Paint.Align.CENTER);
        fontMetrics = mCalenderPaint.getFontMetrics();
        mCalenderTextDrawHeight = mDayTextHeight - fontMetrics.ascent + mExtraHeight;

        mCirclePaint = new Paint();
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeWidth(10);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED://处于HorizontalScrollView中
            case MeasureSpec.AT_MOST://wrap_content
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(1000, MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                FrameView.HORIZONTAL_LINE_LENGTH = MeasureSpec.getSize(widthMeasureSpec);
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED://处于ScrollView中
            case MeasureSpec.AT_MOST://wrap_content
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (mCircleRadius * 2 + mExtraHeight), MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                break;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        mCirclePaint.setColor(mCircleColor);
        canvas.drawCircle(getWidth()/14.0f * (2 * mCirclePosition - 1), mCircleDrawHeight, mCircleRadius, mCirclePaint);
        for (int i = 0; i < 7; i++) {
            String calender = mCalender[i];
            if (calender.startsWith("初") || calender.startsWith("十") || calender.startsWith("廿")) {
                mCalenderPaint.setColor(mCalenderColor);
            }else {
                mCalenderPaint.setColor(mCircleColor);
            }
            if (i + 1 == mCirclePosition) {
                mDayPaint.setColor(mColorWhite);
                mCalenderPaint.setColor(mColorWhite);
            }else {
                mDayPaint.setColor(mColorBlack);
            }
            canvas.drawText(mWeek[i], getWidth() / 14.0f * (2 * i + 1), mDayTextDrawHeight, mDayPaint);
            canvas.drawText(calender, getWidth() / 14.0f * (2 * i + 1), mCalenderTextDrawHeight, mCalenderPaint);
        }
    }

    /**
     * 设置当前周的日期数和阴历或者是节日，节日和阴历会自动识别改变颜色
     * @param week 传入二维数组，[0][]代表当前周对应的日期数，[1][]代表当前周对应的阴历数或节日
     */
    public void settWeek(String[][] week) {
        mWeek = week[0];
        mCalender = week[1];
        invalidate();
    }

    /**
     * 设置当前该显示周几
     * @param position 周日到周六分别对应1 ~ 7
     */
    public void setCirclePosition(int position) {
        mCirclePosition = position;
        invalidate();
    }

    /**
     * 设置日期的移动动画效果
     * @param position 传入从1 ~ 7的小数
     */
    public void setMobileEffect(float position) {
        mCircleRadius = (float) (0.5 * -Math.abs(Math.sin(position * Math.PI)) + 1);
        mCirclePosition = position;
    }
}
