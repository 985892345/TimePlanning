package com.ndhzs.timeplanning.myview;

/*
 *此自定义View只用于在长按已选取的区域时生成相同的Img来移动
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

public class RectImgView extends View {

    private final Paint mInsidePaint;//圆角矩形内部画笔
    private final Paint mBorderPaint;//圆角矩形边框画笔
    private final Paint mTextPaint;//任务名称文字画笔
    private final Paint mRectTimePaint;//矩形内部时间文字画笔
    private final float mRectTimeAscent, mRectTimeDescent;//矩形内部时间的ascent和descent线
    private final RectF mRect;//这个是坐标从(0,0)开始的矩形
    private final RectF rect;//这个是ScrollView布局中的坐标，减去移动量就是上面这个mRect
    private final int mRadius;//圆角矩形的圆角半径
    private final float mTextCenterDistance;//文字中心线
    private final String mTaskName;//任务名字
    private final RectView mRectView;

    public RectImgView(Context context, RectF rect, int radius, Paint borderPaint, Paint insidePaint,
                       Paint textPaint, Paint rectTimePaint, float rectTimeAscent, float rectTimeDescent,
                       String taskName, RectView rectView) {
        super(context);
        this.mRadius = radius;
        this.mTaskName = taskName;
        this.mBorderPaint = borderPaint;
        this.mInsidePaint = insidePaint;
        this.mTextPaint = textPaint;
        this.mRectTimePaint = rectTimePaint;
        this.mRectTimeAscent = rectTimeAscent;
        this.mRectTimeDescent = rectTimeDescent;
        this.rect = rect;
        this.mRectView = rectView;
        mIsFirst = true;
        float left, top, right, bottom;
        left = mBorderPaint.getStrokeWidth()/2;
        top = left;
        right = rect.width() + left;
        bottom = rect.height() + top;
        mRect = new RectF(left, top, right, bottom);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextCenterDistance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
    }

    private static final String TAG = "123";
    @Override
    protected void onDraw(Canvas canvas) {
        //先画矩形的过程
        float baseline = mRect.centerY() + mTextCenterDistance;//任务文字相对于矩形的水平线高度
        canvas.drawRoundRect(mRect, mRadius, mRadius, mInsidePaint);
        canvas.drawRoundRect(mRect, mRadius, mRadius, mBorderPaint);
        canvas.drawText(mTaskName, mRect.centerX(), baseline, mTextPaint);
        canvas.drawText(mRectView.calculateImgViewTime(true), mRect.left + 6, mRect.top - mRectTimeAscent, mRectTimePaint);
        canvas.drawText(mRectView.calculateImgViewTime(false), mRect.left + 6, mRect.bottom - mRectTimeDescent, mRectTimePaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec((int)(mRect.width() + mBorderPaint.getStrokeWidth()), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int)(mRect.height() + mBorderPaint.getStrokeWidth()), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private boolean mIsFirst;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mIsFirst) {
            mIsFirst = false;
            layout(rect.left - mBorderPaint.getStrokeWidth()/2, rect.top - mBorderPaint.getStrokeWidth()/2 - mRectView.getScrollViewScrollY());
        }
    }

    public void layout(float left, float top) {
        int l = (int)left;
        int t = (int)top;
        int r = l + getWidth();
        int b = t + getHeight();
        layout(l, t, r, b);
    }
}
