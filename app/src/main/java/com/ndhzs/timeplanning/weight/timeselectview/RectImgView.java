package com.ndhzs.timeplanning.weight.timeselectview;

/*
 *此自定义View只用于在长按已选取的区域时生成相同的Img来移动
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.ndhzs.timeplanning.weight.TimeSelectView;

public class RectImgView extends View {

    private Context mContext;
    private final Rect mRect;//这个是坐标从(0,0)开始的矩形
    private final String mTaskName;
    private final String mStDTime;
    private final RectView mRectView;
    private final int mStartHour;
    private int mInitialL = Integer.MAX_VALUE, mInitialT;//如果是最开始layout时，计入此时的mInitialL, mInitialT
    public static final int X_KEEP_THRESHOLD = 50;//长按后左右移动时保持水平不移动的阀值

    /**
     * 必须传入这几个值才能绘制可移动的矩形，移动请调用layout(int dx, int dy)
     * @param context 传入context
     * @param rect 传入矩形，不用区分位置，传入后会以此矩形大小重绘
     * @param taskName 任务名称
     * @param stDTime 任务段时间差值
     * @param rectView 与RectView进行绑定
     */
    public RectImgView(Context context, Rect rect, String taskName, String stDTime, RectView rectView, int startHour) {
        super(context);
        this.mContext = context;
        this.mTaskName = taskName;
        this.mStDTime = stDTime;
        this.mRectView = rectView;
        this.mStartHour = startHour;
        mRect = new Rect(0, 0, rect.width(), rect.height());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED://处于HorizontalScrollView中
            case MeasureSpec.AT_MOST://wrap_content
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(mRect.width(), MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED://处于ScrollView中
            case MeasureSpec.AT_MOST://wrap_content
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(mRect.height(), MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                break;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mInitialL == Integer.MAX_VALUE) {//如果是最开始layout时，计入此时的mInitialL, mInitialT
            mInitialL = left;
            mInitialT = top;
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        mRectView.drawRect(canvas, mRect, mTaskName);
        mRectView.drawTopBottomTime(canvas, mRect,
                TimeTools.getTopTime(mStartHour, getTop()),
                TimeTools.getBottomTime(mStDTime));
        if (TimeSelectView.IS_SHOW_DIFFERENT_TIME) {
            mRectView.drawArrows(canvas, mRect, mStDTime);
        }
    }

    /**
     * 传入移动量，会调用layout()移动
     * @param dx 移动值dx，是与最初的位置的差值
     * @param dy 移动值dy，是与最初的位置的差值
     */
    public void layout(int dx, int dy) {
        dx = (Math.abs(dx) < X_KEEP_THRESHOLD) ? 0 :
                ((dx > 0) ? dx - X_KEEP_THRESHOLD : dx + X_KEEP_THRESHOLD);
        int l = mInitialL + dx;
        int t = mInitialT + dy;
        int r = l + getWidth();
        int b = t + getHeight();
        layout(l, t, r, b);
        invalidate();
    }

    /**
     * 传入移动量，会调用layout()移动，(注意！dx与dy不同)
     * @param dx 移动值dx，是与最初的位置的差值
     * @param dy 移动值dy，是与上一次位置的差值
     */
    public void autoSlideLayout(int dx, int dy) {
        dx = (Math.abs(dx) < X_KEEP_THRESHOLD) ? 0 :
                ((dx > 0) ? dx - X_KEEP_THRESHOLD : dx + X_KEEP_THRESHOLD);
        int l = mInitialL + dx;
        int t = getTop() + dy;
        int r = l + getWidth();
        int b = t + getHeight();
        layout(l, t, r, b);
        invalidate();
    }

    /**
     * 用于FissureView添加相同的RectImgView
     * @return 返回new的一样的副本
     */
    public RectImgView getSameImgView() {
        return new RectImgView(mContext, mRect, mTaskName, mStDTime, mRectView, mStartHour);
    }

    private static final String TAG = "123";
}
