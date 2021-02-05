package com.ndhzs.timeplanning.myview;

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

    /**
     * 必须传入这几个值才能绘制可移动的矩形，移动请调用layout(int left, int top)
     * @param context 传入context
     * @param rect 传入矩形，不用区分位置，传入后会以此矩形大小和边框厚度重绘
     * @param taskName 任务名称
     * @param stDTime 任务段时间差值
     * @param rectView 与RectView进行绑定
     */
    public RectImgView(Context context, Rect rect, String taskName, String stDTime, RectView rectView) {
        super(context);
        this.mTaskName = taskName;
        this.mStDTime = stDTime;
        this.mRectView = rectView;
        mRect = new Rect(0, 0, rect.width(), rect.height());
    }

    private static final String TAG = "123";
    @Override
    protected void onDraw(Canvas canvas) {
        mRectView.drawRect(canvas, mRect, mTaskName);
        mRectView.drawArrows(canvas, mRect, mStDTime);
        mRectView.drawTopBottomTime(canvas, mRect,
                mRectView.calculateTime(getTop() + mRectView.getScrollViewScrollY(), false),
                mRectView.calculateTime(getBottom() + mRectView.getScrollViewScrollY(), false));
    }

    /**
     * 传入左上角坐标值，会调用layout()移动
     * @param left 左上角坐标的left值
     * @param top 左上角坐标的top值
     */
    public void layout(int left, int top) {
        layout(left, top, left + getWidth(), top + getHeight());
        invalidate();
    }
}
