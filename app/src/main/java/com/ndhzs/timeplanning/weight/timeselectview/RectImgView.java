package com.ndhzs.timeplanning.weight.timeselectview;

/*
 *此自定义View只用于在长按已选取的区域时生成相同的Img来移动
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class RectImgView extends View {

    private final Rect mRect;//这个是坐标从(0,0)开始的矩形
    private final String mTaskName;
    private final String mStDTime;
    private final RectView mRectView;
    private boolean mIsFirst;//如果是最开始layout时，计入此时的mInitialL, mInitialT
    private int mInitialL, mInitialT;

    /**
     * 必须传入这几个值才能绘制可移动的矩形，移动请调用layout(int dx, int dy)
     * @param context 传入context
     * @param rect 传入矩形，不用区分位置，传入后会以此矩形大小重绘
     * @param taskName 任务名称
     * @param stDTime 任务段时间差值
     * @param rectView 与RectView进行绑定
     */
    public RectImgView(Context context, Rect rect, String taskName, String stDTime, RectView rectView) {
        super(context);
        this.mTaskName = taskName;
        this.mStDTime = stDTime;
        this.mRectView = rectView;
        mIsFirst = true;
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

    private static final String TAG = "123";

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mIsFirst) {
            mIsFirst = false;
            mInitialL = left;
            mInitialT = top;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mRectView.drawRect(canvas, mRect, mTaskName);
        mRectView.drawArrows(canvas, mRect, mStDTime);
        mRectView.drawTopBottomTime(canvas, mRect,
                MyTime.getTime(getTop()),
                MyTime.getTime(getBottom()));
    }

    /**
     * 传入左上角坐标值，会调用layout()移动
     * @param dx 移动值dx
     * @param dy 移动值dy
     */
    public void layout(int dx, int dy) {
        int l = mInitialL + dx;
        int t = mInitialT + dy;
        int r = l + getWidth();
        int b = t + getHeight();
        layout(l, t, r, b);
        invalidate();
    }
}
