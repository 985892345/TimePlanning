package com.ndhzs.timeplanning.weight;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.timeselectview.FrameView;

public class WeekView extends View {

    private Paint mWeekPaint;
    private final int mColorGray = 0xFF828282;
    private final float mTextSize;
    private float mTextHeight;
    private float mTextDrawHeight;
    private final String[] mWeek = {"日", "一", "二", "三", "四", "五", "六"};

    public WeekView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ty = context.obtainStyledAttributes(attrs, R.styleable.WeekView);
        mTextSize = ty.getDimension(R.styleable.WeekView_weekTextSize, 30);
        ty.recycle();
        init();
    }
    private void init() {
        mWeekPaint = new Paint();
        mWeekPaint.setColor(mColorGray);
        mWeekPaint.setAntiAlias(true);
        mWeekPaint.setTextSize(mTextSize);
        mWeekPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = mWeekPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom - fontMetrics.top;
        mTextDrawHeight = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom + mTextHeight / 2;
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
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (mTextHeight), MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                break;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < 7; i++) {
            canvas.drawText(mWeek[i], getWidth() / 14.0f * (2 * i + 1), mTextDrawHeight, mWeekPaint);
        }
    }
}
