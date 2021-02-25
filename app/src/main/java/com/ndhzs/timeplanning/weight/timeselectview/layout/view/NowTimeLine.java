package com.ndhzs.timeplanning.weight.timeselectview.layout.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.ndhzs.timeplanning.weight.timeselectview.utils.TimeUtil;

public class NowTimeLine extends View {

    private Paint mTimeLinePaint;
    private int mIntervalLeft;//左边的时间间隔宽度
    private int mIntervalRight;//右边的间隔宽度
    private final TimeUtil mTimeUtil;

    public static final int BALL_DIAMETER = 14;//小球直径

    public NowTimeLine(Context context, TimeUtil timeUtil) {
        super(context);
        this.mTimeUtil = timeUtil;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                timeLineMove();
                postDelayed(this, TimeUtil.DELAY_NOW_TIME_REFRESH);
            }
        }, TimeUtil.DELAY_NOW_TIME_REFRESH);
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
    public void layout(int l, int t, int r, int b) {
        // 放在这里是有原因的，为什么不在addView()时直接设置LayoutParams.topMargin使时间线加载时处于开始位置？
        // 因为如果我长按已选择了的区域，那么在ChildLayout就会调用addView()，又因为时间线应在最顶层，
        // 所以系统会重新layout()，如果你在LayoutParams.topMargin直接设置了开始位置，那么此时layout()就
        // 会跑回去，所以只有在layout()中设置当前时间的高度，就不会重新返回以前的位置，可以避免这个问题
        int nowTimeHeight = mTimeUtil.getNowTimeHeight() - BALL_DIAMETER/2;
        t = nowTimeHeight;
        b = nowTimeHeight + BALL_DIAMETER;
        super.layout(l, t, r, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float x = mIntervalLeft - FrameView.VERTICAL_LINE_WIDTH/2.0f;
        float y = BALL_DIAMETER/2.0f;
        canvas.drawCircle(x, y, BALL_DIAMETER/2.0f, mTimeLinePaint);
        canvas.drawLine(x, y, getWidth() - mIntervalRight, y, mTimeLinePaint);
    }

    private void layout(int y) {
        layout(0, y, getWidth(), y + getHeight());
    }
    private void timeLineMove() {
        layout(mTimeUtil.getNowTimeHeight() - BALL_DIAMETER/2);
    }
}
