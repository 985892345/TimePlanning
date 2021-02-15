package com.ndhzs.timeplanning.weight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.timeselectview.FrameView;

public class RoundCornerView extends View {

    private final Paint mPaint = new Paint();
    private final int mCircle = 10;

    public RoundCornerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10);
        mPaint.setColor(0xFFDCCC48);
        mPaint.setStyle(Paint.Style.FILL);
        setClickable(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), mCircle, mCircle, mPaint);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED://处于HorizontalScrollView中
            case MeasureSpec.AT_MOST://wrap_content
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(50, MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                FrameView.HORIZONTAL_LINE_LENGTH = MeasureSpec.getSize(widthMeasureSpec);
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED://处于ScrollView中
            case MeasureSpec.AT_MOST://wrap_content，本程序不会用到这个
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(50, MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                break;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }
}
