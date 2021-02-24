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
import com.ndhzs.timeplanning.weight.timeselectview.bean.TaskBean;

public class ChildLayout extends FrameLayout implements TimeSelectView.IChildLayout {

    private Context mContext;
    private IUpEvent mIRectView;
    private RectImgView mImgView;
    private RectImgView mLinkImgView;
    private ChildLayout mLinkChildLayout;
    private TimeTools mLinkTimeTools;
    private TaskBean mTaskBean;
    private TimeTools mTimeTools;
    private TimeSelectView.OnDataChangeListener mOnDataChangeListener;
    private int mDiffDistance = 0;
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

    public ChildLayout(@NonNull Context context, TimeTools timeTools) {
        super(context);
        this.mContext = context;
        this.mTimeTools = timeTools;
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
                break;
            case MotionEvent.ACTION_MOVE:
                if (RectView.WHICH_CONDITION == RectView.INSIDE) {
                    return true;
                }
            case MotionEvent.ACTION_UP:
                if (RectView.WHICH_CONDITION == RectView.INSIDE) {
                    // 当只长按不移动，在松手的时候系统不会调用onTouchEvent()的UP，只能手动调用了
                    // 如果你移动后，就不会调用这个onInterceptTouchEvent里的UP了
                    onTouchEvent(ev);
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
                RectView.WHICH_CONDITION = 0;//还原
                float moveThreshold = X_MOVE_THRESHOLD * getWidth();
                int keepThreshold = RectImgView.X_KEEP_THRESHOLD;
                /*
                * 为了使逻辑更严密，以后好改需求，就分情况写了一堆
                * */
                if (mDiffDistance == 0) {//没有与另一个TimeView连接时
                    if (Math.abs(dx - keepThreshold) > moveThreshold) {
                        deleteImgView();
                    }else {
                        goBackImgView(dx);
                    }
                }else if (mDiffDistance > 0) {//该TimeView在右边时
                    if (dx >= 0) {//整体向右边移动
                        if (dx - keepThreshold > moveThreshold) {//大于右边阈值
                            deleteImgView();
                        }else {//小于右边阈值
                            goBackImgView(dx);
                        }
                    }else {//整体向左边移动
                        int cX = -dx - keepThreshold;
                        if (cX >= moveThreshold && cX <= mDiffDistance - moveThreshold) {//在两个TimeView的阈值外夹中
                            deleteImgView();
                        }else if (cX < moveThreshold) {//在自身左边阈值内
                            goBackImgView(dx);
                        }else {//超过左边的TimeView的右阈值
                            int eX = mLinkImgView.getLeft() - mIntervalLeft;
                            if (Math.abs(eX) < moveThreshold) {//在左边的TimeView的阈值内
                                if (!mLinkChildLayout.isLayDown(this, mTaskBean)) {
                                    goBackImgView(dx);
                                }else {
                                    mIRectView.deleteHashMap();
                                }
                            }else {//超过左边TimeView的左阈值
                                deleteImgView();
                            }
                        }
                    }
                }else {//mDiffDistance < 0 该TimeView在左边时
                    if (dx <= 0) {//整体向左移动
                        if (-dx - keepThreshold > moveThreshold) {//大于左边阈值
                            deleteImgView();
                        }else {//小于左边阈值
                            goBackImgView(dx);
                        }
                    }else {//整体向右移动
                        int cX = dx - keepThreshold;
                        if (cX >= moveThreshold && cX <= -mDiffDistance - moveThreshold) {//在两个TimeView的阈值外夹中
                            deleteImgView();
                        }else if (cX < moveThreshold) {//在自身右边阈值内
                            goBackImgView(dx);
                        }else {//超过右边的TimeView的左阈值
                            int eX = mIntervalLeft - mLinkImgView.getLeft();
                            if (Math.abs(eX) < moveThreshold) {//在右边的TimeView的阈值内
                                if (!mLinkChildLayout.isLayDown(this, mTaskBean)) {
                                    goBackImgView(dx);
                                }else {
                                    mIRectView.deleteHashMap();
                                }
                            }else {//超过右边TimeView的右阈值
                                deleteImgView();
                            }
                        }
                    }
                }
                break;
        }
        return true;
    }
    private void deleteImgView() {
        dataDeleted(mTaskBean);
        if (mLinkImgView != null) {
            mLinkImgView.animate()
                    .scaleX(0)
                    .scaleY(0)
                    .setDuration(350)
                    .setInterpolator(new AccelerateInterpolator());
        }
        mImgView.animate()
                .scaleX(0)
                .scaleY(0)
                .setDuration(350)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mIRectView.deleteHashMap();
                        removeView(mImgView);
                        if (mLinkChildLayout != null) {
                            mLinkChildLayout.removeView(mLinkImgView);
                        }
                    }
                });
    }
    private void goBackImgView(int dx) {

        int correctTop = mExtraHeight;
        int correctBottom = getBottom() - mExtraHeight;
        int imgViewTop = mImgView.getTop();
        int imgViewBottom = mImgView.getBottom();
        int nowUpperLimit = mIRectView.getNowUpperLimit(imgViewBottom - mExtraHeight) + mExtraHeight;
        int nowLowerLimit = mIRectView.getNowLowerLimit(imgViewTop - mExtraHeight) + mExtraHeight;
        //每个if对应了一种情况，具体请以序号看纸上的草图
        if (mImgView.getHeight() - (mTimeTools.mEveryMinuteHeight[1] - 1) <= nowLowerLimit - nowUpperLimit) {
            if (imgViewTop <= nowLowerLimit && imgViewBottom >= nowLowerLimit) {//1
                correctBottom = nowLowerLimit;
                WHERE_TO_DRAW = BOTTOM_TO_DRAW;
            }else if (imgViewTop <= nowUpperLimit && imgViewBottom >= nowUpperLimit) {//2
                correctTop = nowUpperLimit;
                WHERE_TO_DRAW = TOP_TO_DRAW;
            }else if (nowUpperLimit == mUpperLimit && nowLowerLimit == mLowerLimit) {//3
                if (imgViewBottom < mExtraHeight) {//整体移动到mExtraHeight以上
                    correctTop = mUpperLimit;
                    WHERE_TO_DRAW = TOP_TO_DRAW;
                }else if (imgViewTop > getHeight() - mExtraHeight) {//整体移动到getHeight() - mExtraHeight以下
                    correctBottom = mLowerLimit;
                    WHERE_TO_DRAW = BOTTOM_TO_DRAW;
                }else {
                    WHERE_TO_DRAW = DECIDE_BY_ONESELF;
                }
            }else if (nowUpperLimit == mUpperLimit) {//4
                correctBottom = mLowerLimit;
                WHERE_TO_DRAW = BOTTOM_TO_DRAW;
            }else if (nowUpperLimit > mUpperLimit) {//5
                int lowerLimit = mIRectView.getNowLowerLimit(nowUpperLimit - mExtraHeight) + mExtraHeight;
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
                int upperLimit = mIRectView.getNowUpperLimit(nowLowerLimit - mExtraHeight) + mExtraHeight;
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
                duration = (Math.abs(dx) < RectImgView.X_KEEP_THRESHOLD) ? 0 : Math.abs(dx);
                break;
        }
        int finalCorrectTop = correctTop;
        int finalCorrectBottom = correctBottom;
        if (mLinkImgView != null) {
            mLinkImgView.animate().x(mIntervalLeft + mDiffDistance)
                    .y(topHeight)
                    .setDuration(duration)
                    .setInterpolator(new DecelerateInterpolator());
        }
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
                                mIRectView.addDeletedRectFromTop(imgViewTop - mExtraHeight);
                                break;
                        }
                        removeView(mImgView);
                        if (mLinkChildLayout != null) {
                            mLinkChildLayout.removeView(mLinkImgView);
                        }
                    }
                });

    }
    private boolean isLayDown(ChildLayout childLayout, TaskBean taskBean) {
        int correctTop = mExtraHeight;
        int correctBottom = getBottom() - mExtraHeight;
        int linkImgViewTop = childLayout.mLinkImgView.getTop();
        int linkImgViewBottom = childLayout.mLinkImgView.getBottom();
        int nowUpperLimit = mIRectView.getNowUpperLimit(linkImgViewBottom - mExtraHeight) + mExtraHeight;
        int nowLowerLimit = mIRectView.getNowLowerLimit(linkImgViewTop - mExtraHeight) + mExtraHeight;
        if (linkImgViewBottom - linkImgViewTop  - (mTimeTools.mEveryMinuteHeight[1] - 1) <= nowLowerLimit - nowUpperLimit) {
            if (linkImgViewTop <= nowLowerLimit && linkImgViewBottom >= nowLowerLimit) {//1
                correctBottom = nowLowerLimit;
                WHERE_TO_DRAW = BOTTOM_TO_DRAW;
            }else if (linkImgViewTop <= nowUpperLimit && linkImgViewBottom >= nowUpperLimit) {//2
                correctTop = nowUpperLimit;
                WHERE_TO_DRAW = TOP_TO_DRAW;
            }else {//包括了3、4、5、6、7
                int lowerLimit = mIRectView.getNowLowerLimit(nowUpperLimit - mExtraHeight) + mExtraHeight;
                if (lowerLimit == nowLowerLimit) {//3、5-1、7-1
                    WHERE_TO_DRAW = DECIDE_BY_ONESELF;
                }else {//4、5-2、6、7-2
                    return false;
                }
            }
        }else {//包括 a 的所有情况
            Log.d(TAG, "isLayDown: ");
            return false;
        }
        int topHeight = mExtraHeight;
        int duration = 200;
        int dx = childLayout.mLinkImgView.getLeft() - mIntervalLeft;
        switch (WHERE_TO_DRAW) {
            case TOP_TO_DRAW:
                topHeight = correctTop;
                duration = (int) (Math.sqrt(Math.pow(dx, 2) + Math.pow(correctTop - linkImgViewTop, 2)) * 0.6);
                duration = Math.min(duration, 300);
                break;
            case BOTTOM_TO_DRAW:
                topHeight = correctBottom - childLayout.mImgView.getHeight();
                duration = (int) (Math.sqrt(Math.pow(dx, 2) + Math.pow(linkImgViewBottom - correctBottom, 2)) * 0.6);
                duration = Math.min(duration, 300);
                break;
            case DECIDE_BY_ONESELF:
                topHeight = linkImgViewTop;
                duration = (Math.abs(dx) < RectImgView.X_KEEP_THRESHOLD) ? 0 : Math.abs(dx);
                break;
        }
        int finalCorrectTop = correctTop;
        int finalCorrectBottom = correctBottom;
        childLayout.mImgView.animate().x(mIntervalLeft - childLayout.mDiffDistance)
                .y(topHeight)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator());
        childLayout.mLinkImgView.animate().x(mIntervalLeft)
                .y(topHeight)
                .setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        switch (WHERE_TO_DRAW) {
                            case TOP_TO_DRAW:
                                int top = finalCorrectTop - mExtraHeight;
                                mIRectView.addRectFromTop(top, taskBean);
                                break;
                            case BOTTOM_TO_DRAW:
                                int bottom = finalCorrectBottom - mExtraHeight;
                                mIRectView.addRectFromBottom(bottom, taskBean);
                                break;
                            case DECIDE_BY_ONESELF:
                                int aTop = childLayout.mLinkImgView.getTop() - mExtraHeight;
                                mIRectView.addRectFromTop(aTop, taskBean);
                                break;
                        }
                        removeView(childLayout.mLinkImgView);
                        childLayout.removeView(childLayout.mImgView);
                    }
                });
        return true;
    }

    public void addImgViewRect(RectImgView rectImgView, LayoutParams lp, int currentUpperLimit, int currentLowerLimit, TaskBean taskBean) {
        this.mImgView = rectImgView;
        this.mTaskBean = taskBean;
        this.mUpperLimit = currentUpperLimit + mExtraHeight;
        this.mLowerLimit = currentLowerLimit + mExtraHeight;
        addView(rectImgView, lp);
        if (mLinkChildLayout != null) {
            mLinkImgView = mImgView.getSameImgView(mLinkTimeTools);
            LayoutParams layoutParams = new LayoutParams(lp);
            layoutParams.leftMargin += mDiffDistance;
            mLinkChildLayout.addView(mLinkImgView, layoutParams);//在另一个ChildLayout生成一个矩形在自身的ChildLayout的底部
            mImgView.setLinkImgView(mLinkImgView);
            mImgView.setSelfImgView(mImgView);
            mImgView.setDiffDistance(mDiffDistance);
            mLinkImgView.setSelfImgView(mImgView);
            mLinkImgView.setDiffDistance(mDiffDistance);
            mLinkChildLayout.mIRectView.deleteIsInsideRect(taskBean);
        }
    }

    public void setLinkChildLayout(ChildLayout linkChildLayout, TimeTools linkTimeTools, int diffDistance) {
        if (mLinkChildLayout == null) {
            mLinkChildLayout = linkChildLayout;
            mLinkTimeTools = linkTimeTools;
            mDiffDistance = diffDistance;
        }
    }

    private void dataDeleted(TaskBean deletedData) {
        if (mOnDataChangeListener != null) {
            mOnDataChangeListener.onDataDelete(deletedData);
        }
    }

    @Override
    public void setOnDataChangeListener(TimeSelectView.OnDataChangeListener onDataChangeListener) {
        this.mOnDataChangeListener = onDataChangeListener;
    }
    @Override
    public void isAllowDraw(boolean isAllowDraw) {
        mIsAllowDraw = isAllowDraw;
    }

    interface IUpEvent {
        void deleteHashMap();
        void addDeletedRectFromTop(int top);
        void addDeletedRectFromBottom(int bottom);
        void deleteIsInsideRect(TaskBean taskBean);//用来通知另一个ChildLayout删掉正在长按移动的矩形
        void addRectFromTop(int top, TaskBean taskBean);
        void addRectFromBottom(int bottom, TaskBean taskBean);
        int getNowUpperLimit(int y);
        int getNowLowerLimit(int y);
    }

    private static final String TAG = "123";
}
