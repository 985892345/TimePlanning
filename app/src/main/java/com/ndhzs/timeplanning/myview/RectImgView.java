package com.ndhzs.timeplanning.myview;

/*
 *此自定义View只用于在长按已选取的区域时生成相同的Img来移动
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

public class RectImgView extends View {

    private final Rect mRect;//这个是坐标从(0,0)开始的矩形
    private final Rect rect;//这个是ScrollView布局中的坐标，减去移动量就是上面这个mRect
    private Rect aRect = new Rect();
    private final int mBorderWidth;//圆角矩形边框厚度
    private final String mTaskName;
    private final String mStDTime;
    private final RectView mRectView;

    public RectImgView(Context context, Rect rect, int borderWidth, String taskName, String stDTime, RectView rectView) {
        super(context);
        this.mBorderWidth = borderWidth;
        this.rect = rect;
        this.mTaskName = taskName;
        this.mStDTime = stDTime;
        this.mRectView = rectView;
        int left, top, right, bottom;
        left = mBorderWidth/2;
        top = left;
        right = rect.width() + left;
        bottom = rect.height() + top;
        mRect = new Rect(left, top, right, bottom);
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

    public void layout(int left, int top) {
        layout(left, top, left + getWidth(), top + getHeight());
    }
}
