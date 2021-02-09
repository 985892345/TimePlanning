package com.ndhzs.timeplanning.weight.timeselectview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;

import com.ndhzs.timeplanning.weight.TimeSelectView;

public class ChildLayout extends FrameLayout implements TimeSelectView.IIsAllowDraw {

    private Context mContext;
    private IUpEvent mIRectView;
    private RectImgView mImgView;
    private int mInitialX, mInitialY;//长按已选择的区域时的坐标
    private int mUpperLimit, mLowerLimit;//当前矩形的上下限，不能移动到其他矩形区域
    private int mIntervalLeft;//左边的时间间隔宽度、
    private int mExtraHeight;//上方或下方其中一方多余的高度
    private boolean mIsAllowDraw;//说明正在自动滑动，通知onTouchEvent()的MOVE不要处理，不然绘图会卡

    private final float X_MOVE_THRESHOLD = 0.4f;//长按后左右移动删除的阀值，为getWidth()的倍数

    public ChildLayout(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    public void setIRectView(IUpEvent IRectView) {
        this.mIRectView = IRectView;
    }
    public void setInterval(int intervalLeft, int extraHeight) {
        this.mIntervalLeft = intervalLeft;
        this.mExtraHeight = extraHeight;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialX = (int) ev.getX();
                mInitialY = (int) ev.getY();
                onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (RectView.WHICH_CONDITION == RectView.INSIDE) {
                    return true;
                }
            case MotionEvent.ACTION_UP:
                if (RectView.WHICH_CONDITION == RectView.INSIDE) {
                    onTouchEvent(ev);//当只长按不移动，在松手的时候系统不会调用onTouchEvent()的UP，只能手动调用了
                    return true;
                }
                break;
        }
        return false;
    }

    private int dx, dy;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        dx = x - mInitialX;
        dy = y - mInitialY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsAllowDraw) {
                    mImgView.layout(dx, dy);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(dx) > X_MOVE_THRESHOLD * getWidth()) {
                    mImgView.animate().x((dx > 0) ? getWidth() : -getWidth())
                            .scaleX(0)
                            .scaleY(0)
                            .setDuration((int) (Math.abs(getWidth() - Math.abs(dx)) * 1.2f))
                            .setInterpolator(new AccelerateInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mIRectView.deleteHashMap();
                                    removeView(mImgView);
                                }
                            });
                }else {
                    int imgViewTop = mImgView.getTop();
                    int imgViewBottom = mImgView.getBottom();
                    int newUpperLimit = mIRectView.getNewUpperLimit(imgViewBottom - mExtraHeight);
                    int newLowerLimit = mIRectView.getNewLowerLimit(imgViewTop - mExtraHeight);
                    boolean isOverUpperLimit = imgViewTop < mUpperLimit;
                    boolean isOverLowerLimit = mImgView.getBottom() > mLowerLimit;

                    int correctTop = isOverUpperLimit ?
                            mUpperLimit :
                            (isOverLowerLimit ?
                                    mLowerLimit - mImgView.getHeight() :
                                    imgViewTop);
                    mImgView.animate().x(mIntervalLeft)
                            .y(correctTop)
                            .setDuration((int) (Math.abs(dx) * 1.2f))
                            .setInterpolator(new DecelerateInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    int top = correctTop - mExtraHeight;
                                    mIRectView.addDeletedRect(top);
                                    removeView(mImgView);
                                }
                            });
                }
                break;
        }
        return true;
    }

    public void addImgViewRect(RectImgView rectImgView, LayoutParams lp, int currentUpperLimit, int currentLowerLimit) {
        this.mImgView = rectImgView;
        this.mUpperLimit = currentUpperLimit + mExtraHeight;
        this.mLowerLimit = currentLowerLimit + mExtraHeight;
        addView(rectImgView, lp);
    }

    @Override
    public void isAllowDraw(boolean isAllowDraw) {
        mIsAllowDraw = isAllowDraw;
    }

    interface IUpEvent {
        void deleteHashMap();
        void addDeletedRect(int top);
        int getNewUpperLimit(int y);
        int getNewLowerLimit(int y);
    }

    private static final String TAG = "123";
}
