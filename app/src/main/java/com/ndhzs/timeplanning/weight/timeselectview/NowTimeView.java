package com.ndhzs.timeplanning.weight.timeselectview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class NowTimeView extends View {

    private Paint mTimeLinePaint;
    private int mIntervalLeft;//左边的时间间隔宽度
    private int mIntervalRight;//右边的间隔宽度

    private final int BALL_DIAMETER = 14;//小球直径

    public NowTimeView(Context context) {
        super(context);
        post(new Runnable() {
            @Override
            public void run() {
                timeLineMove();
            }
        });
        init();
    }
    private void init() {
        mTimeLinePaint = new Paint();
        mTimeLinePaint.setAntiAlias(true);
        mTimeLinePaint.setColor(0xFFE40000);
        mTimeLinePaint.setStyle(Paint.Style.FILL);
        mTimeLinePaint.setStrokeWidth(3);
    }
    public void setInterval(int intervalLeft, int intervalRight) {
        this.mIntervalLeft = intervalLeft;
        this.mIntervalRight = intervalRight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED://处于ScrollView中
            case MeasureSpec.AT_MOST://wrap_content
            case MeasureSpec.EXACTLY://match_parent、精确值
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(BALL_DIAMETER, MeasureSpec.EXACTLY);
                break;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float x = mIntervalLeft - TimeFrameView.VERTICAL_LINE_WIDTH/2.0f;
        float y = BALL_DIAMETER/2.0f;
        canvas.drawCircle(x, y, BALL_DIAMETER/2.0f, mTimeLinePaint);
        canvas.drawLine(x, y, getWidth() - mIntervalRight, y, mTimeLinePaint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            Log.d("123", "onLayout: ");
            timeLineMove();
        }
    }

    public void layout(int y) {
        layout(0, y, getWidth(), y + getHeight());
    }

    private void timeLineMove() {
        layout(MyTime.getNowTimeHeight() - BALL_DIAMETER/2);
    }
}
