package com.ndhzs.timeplanning.myview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;

public class ChildFrameLayout extends FrameLayout {

    private Context mContext;
    private RectView mRectView;
    private RectImgView mImgView;
    private boolean mIsIntercept;
    private int mInitialX, mInitialY;//长按已选择的区域时的坐标
    private int mUpperLimit, mLowerLimit;//当前矩形的上下限，不能移动到其他矩形区域
    private int mIntervalLeft;//左边的时间间隔宽度、
    private int mExtraHeight;//上方或下方其中一方多余的高度

    private final int X_KEEP_THRESHOLD = 50;//长按后左右移动时保持水平不移动的阀值
    private final float X_MOVE_THRESHOLD = 0.4f;//长按后左右移动删除的阀值，为getWidth()的倍数

    public ChildFrameLayout(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public void setRectView(RectView rectView) {
        this.mRectView = rectView;
    }

    public void setInterval(int intervalLeft, int extraHeight) {
        this.mIntervalLeft = intervalLeft;
        this.mExtraHeight = extraHeight;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsIntercept) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int dx = x - mInitialX;
        int dy = y - mInitialY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("123", "onTouchEvent: ChildLayout");
                mInitialX = x;
                mInitialY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("123", "onTouchEvent: MOVE_ChildLayout");
                dx = (Math.abs(dx) < X_KEEP_THRESHOLD) ? 0 : ((dx > 0) ? dx - X_KEEP_THRESHOLD : dx + X_KEEP_THRESHOLD);
                mImgView.layout(dx, dy);
                break;
            case MotionEvent.ACTION_UP:
                mIsIntercept= false;
                if (Math.abs(dx) > X_MOVE_THRESHOLD * getWidth()) {
                    mImgView.animate().x((dx > 0) ? getWidth() : -getWidth())
                            .scaleX(0)
                            .scaleY(0)
                            .setDuration((int) ((getWidth() - Math.abs(dx)) * 1.2f))
                            .setInterpolator(new AccelerateInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    removeView(mImgView);
                                }
                            });
                    mRectView.deleteHashMap();
                }else {
                    int imgViewTop = mImgView.getTop();

                    boolean isOverUpperLimit = imgViewTop < mUpperLimit;
                    boolean isOverLowerLimit = mImgView.getBottom() > mLowerLimit;

                    int trueY = isOverUpperLimit ?
                            mUpperLimit :
                            (isOverLowerLimit ?
                                    mLowerLimit - mImgView.getHeight() :
                                    imgViewTop);
                    mImgView.animate().x(mIntervalLeft)
                            .y(trueY)
                            .setDuration((int) (Math.abs(dx) * 1.2f))
                            .setInterpolator(new DecelerateInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mRectView.addDeleteRect(trueY - mExtraHeight, mImgView.getHeight());
                                    removeView(mImgView);
                                }
                            });
                }
                break;
        }
        return true;
    }

    public void addRectImgView(RectImgView rectImgView, LayoutParams lp, int upperLimit, int lowerLimit) {
        this.mImgView = rectImgView;
        this.mUpperLimit = upperLimit + mExtraHeight;
        this.mLowerLimit = lowerLimit + mExtraHeight;
        addView(rectImgView, lp);
        mIsIntercept = true;
    }
}
