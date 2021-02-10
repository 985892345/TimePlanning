package com.ndhzs.timeplanning.weight.timeselectview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.Log;
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

    private int WHERE_TO_DRAW;
    private final int TOP_TO_DRAW = -1;
    private final int BOTTOM_TO_DRAW = 1;
    private final int DECIDE_BY_ONESELF = 0;

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
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int dx = x - mInitialX;
        int dy = y - mInitialY;
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
                    int correctTop = mExtraHeight;
                    int correctBottom = getBottom() - mExtraHeight;
                    int imgViewTop = mImgView.getTop();
                    int imgViewBottom = mImgView.getBottom();
                    int nowUpperLimit = mIRectView.getNowUpperLimit(imgViewBottom - mExtraHeight) + mExtraHeight;
                    int nowLowerLimit = mIRectView.getNowLowerLimit(imgViewTop - mExtraHeight) + mExtraHeight;
                    //每个if对应了一种情况，具体请以序号看纸上的草图
                    if (mImgView.getHeight() <= nowLowerLimit - nowUpperLimit) {
                        if (imgViewTop <= nowLowerLimit && imgViewBottom >= nowLowerLimit) {//1
                            correctBottom = nowLowerLimit;
                            WHERE_TO_DRAW = BOTTOM_TO_DRAW;
                        }else if (imgViewTop <= nowUpperLimit && imgViewBottom >= nowUpperLimit) {//2
                            correctTop = nowUpperLimit;
                            WHERE_TO_DRAW = TOP_TO_DRAW;
                        }else if (nowUpperLimit == mUpperLimit && nowLowerLimit == mLowerLimit) {//3
                            WHERE_TO_DRAW = DECIDE_BY_ONESELF;
                        }else if (nowUpperLimit == mUpperLimit) {//4
                            correctBottom = mLowerLimit;
                            WHERE_TO_DRAW = BOTTOM_TO_DRAW;
                        }else if (nowUpperLimit > mUpperLimit) {//5
                            int lowerLimit = mIRectView.getNowLowerLimit(nowUpperLimit) + mExtraHeight;
                            if (lowerLimit == nowLowerLimit) {//5-1
                                WHERE_TO_DRAW = DECIDE_BY_ONESELF;
                            }else {//5-2
                                if (mImgView.getHeight() <= lowerLimit - nowUpperLimit) {
                                    correctBottom = lowerLimit;
                                }else {
                                    correctBottom = mLowerLimit;
                                }
                                WHERE_TO_DRAW = BOTTOM_TO_DRAW;
                            }
                        }else if (nowLowerLimit == mLowerLimit) {//6
                            correctTop = mUpperLimit;
                            WHERE_TO_DRAW = TOP_TO_DRAW;
                        }else if (nowLowerLimit < mLowerLimit) {//7
                            int upperLimit = mIRectView.getNowUpperLimit(nowLowerLimit) + mExtraHeight;
                            if (upperLimit == nowUpperLimit) {//7-1
                                WHERE_TO_DRAW = DECIDE_BY_ONESELF;
                            }else {//7-2
                                if (mImgView.getHeight() <= nowLowerLimit - upperLimit) {
                                    correctTop = upperLimit;
                                }else {
                                    correctTop = mUpperLimit;
                                }
                                WHERE_TO_DRAW = TOP_TO_DRAW;
                            }
                        }
                    }else {
                        if (nowLowerLimit == mLowerLimit) {//a-1
                            correctBottom = mLowerLimit;
                            WHERE_TO_DRAW = BOTTOM_TO_DRAW;
                        }else if (nowUpperLimit == mUpperLimit) {//a-2
                            correctTop = mUpperLimit;
                            WHERE_TO_DRAW = TOP_TO_DRAW;
                        }else if (nowLowerLimit > mLowerLimit) {//a-3
                            correctBottom = mLowerLimit;
                            WHERE_TO_DRAW = BOTTOM_TO_DRAW;
                        }else if (nowUpperLimit < mUpperLimit) {//a-4
                            correctTop = mUpperLimit;
                            WHERE_TO_DRAW = TOP_TO_DRAW;
                        }
                    }
                    int topHeight = mExtraHeight;
                    int duration = 200;
                    switch (WHERE_TO_DRAW) {
                        case TOP_TO_DRAW:
                            topHeight = correctTop;
                            duration = (int) (Math.sqrt(Math.pow(dx, 2) + Math.pow(correctTop - imgViewTop, 2)) * 0.6);
                            duration = Math.min(duration, 300);
                            break;
                        case BOTTOM_TO_DRAW:
                            topHeight = correctBottom - mImgView.getHeight();
                            duration = (int) (Math.sqrt(Math.pow(dx, 2) + Math.pow(imgViewBottom - correctBottom, 2)) * 0.6);
                            duration = Math.min(duration, 300);
                            break;
                        case DECIDE_BY_ONESELF:
                            topHeight = imgViewTop;
                            duration = 10;
                            break;
                    }
                    Log.d(TAG, "onTouchEvent: duration = " + duration);
                    int finalCorrectTop = correctTop;
                    int finalCorrectBottom = correctBottom;
                    mImgView.animate().x(mIntervalLeft)
                            .y(topHeight)
                            .setDuration(duration)
                            .setInterpolator(new DecelerateInterpolator())
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    switch (WHERE_TO_DRAW) {
                                        case TOP_TO_DRAW:
                                            int top = finalCorrectTop - mExtraHeight;
                                            mIRectView.addDeletedRectFromTop(top);
                                            break;
                                        case BOTTOM_TO_DRAW:
                                            int bottom = finalCorrectBottom - mExtraHeight;
                                            mIRectView.addDeletedRectFromBottom(bottom);
                                            break;
                                        case DECIDE_BY_ONESELF:
                                            Log.d(TAG, "onAnimationEnd: ");
                                            mIRectView.addDeletedRectFromTop(imgViewTop - mExtraHeight);
                                            break;
                                    }
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
        void addDeletedRectFromTop(int top);
        void addDeletedRectFromBottom(int bottom);
        int getNowUpperLimit(int y);
        int getNowLowerLimit(int y);
    }

    private static final String TAG = "123";
}
