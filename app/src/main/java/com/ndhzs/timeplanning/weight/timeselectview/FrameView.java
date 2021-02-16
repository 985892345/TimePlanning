package com.ndhzs.timeplanning.weight.timeselectview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class FrameView extends View {

    private int mStartHour, mEndHour;

    private int mIntervalLeft;//左边的时间间隔宽度、
    private int mIntervalRight;//右边的间隔宽度
    private int mExtraHeight;//上方或下方其中一方多余的高度
    private int mIntervalHeight;//一个小时的间隔高度

    private Paint mVLinePaint;//Vertical Line 垂直线画笔
    private Paint mHLinePaint;//Horizontal Line 水平线画笔
    private Paint mLeftTimePaint;//左侧时间画笔
    private float mLeftTimeCenter;//时间文字水平线高度

    /**
     * 右边的间隔宽度
     */
    public static int INTERVAL_RIGHT = 10;

    /**
     * HORIZONTAL_LINE_LENGTH 水平线长度。
     * 在wrap_content时，水平线默认长度400，match_parent和精确值时，水平线长度会随之改变。
     * (注意！此值在RectView的onMeasure的宽度为EXACTLY是会修改)
     */
    public static int HORIZONTAL_LINE_LENGTH = 400;

    /**
     * 垂直线厚度
     */
    public static final int VERTICAL_LINE_WIDTH = 2;
    /**
     * 水平线厚度
     */
    public static final int HORIZONTAL_LINE_WIDTH = 1;

    public FrameView(Context context) {
        super(context);
        init();
    }
    private void init() {
        //Vertical Line 垂直线画笔
        mVLinePaint = new Paint();
        mVLinePaint.setAntiAlias(true);
        mVLinePaint.setColor(0xFFC8C8C8);
        mVLinePaint.setStyle(Paint.Style.FILL);
        mVLinePaint.setStrokeWidth(VERTICAL_LINE_WIDTH);
        //Horizontal Line 水平线画笔
        mHLinePaint = new Paint(mVLinePaint);
        mHLinePaint.setColor(0xFF9C9C9C);
        mHLinePaint.setStrokeWidth(HORIZONTAL_LINE_WIDTH);
        //左侧时间画笔
        mLeftTimePaint = new Paint();
        mLeftTimePaint.setColor(0xFF505050);
        mLeftTimePaint.setAntiAlias(true);
        mLeftTimePaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setHour(int startHour, int endHour) {
        this.mStartHour = startHour;
        this.mEndHour = endHour;
    }
    public void setTextSize(float timeTextSize) {
        mLeftTimePaint.setTextSize(timeTextSize);
        Paint.FontMetrics fontMetrics = mLeftTimePaint.getFontMetrics();
        mLeftTimeCenter = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
    }
    public void setInterval(int intervalLeft, int intervalRight, int extraHeight, int intervalHeight) {
        this.mIntervalLeft = intervalLeft;
        this.mIntervalRight = intervalRight;
        this.mExtraHeight = extraHeight;
        this.mIntervalHeight = intervalHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED://处于HorizontalScrollView中
            case MeasureSpec.AT_MOST://wrap_content
                int width = mIntervalLeft + HORIZONTAL_LINE_LENGTH + mIntervalRight;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                HORIZONTAL_LINE_LENGTH = MeasureSpec.getSize(widthMeasureSpec) - mIntervalLeft - mIntervalRight;
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED://处于ScrollView中
            case MeasureSpec.AT_MOST://wrap_content
            case MeasureSpec.EXACTLY://match_parent、精确值
                int height = mExtraHeight * 2 + (mEndHour - mStartHour) * mIntervalHeight;
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                break;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(mIntervalLeft - VERTICAL_LINE_WIDTH/2.0f, 0,
                mIntervalLeft - VERTICAL_LINE_WIDTH/2.0f, getHeight(), mVLinePaint);//这是左边的竖线
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
            canvas.drawText(hour, (mIntervalLeft - VERTICAL_LINE_WIDTH)/2.0f, baseline, mLeftTimePaint);
            canvas.drawLine(mIntervalLeft, y - HORIZONTAL_LINE_WIDTH /2.0f,
                    getWidth() - mIntervalRight, y - HORIZONTAL_LINE_WIDTH /2.0f, mHLinePaint);
        }
    }
}
