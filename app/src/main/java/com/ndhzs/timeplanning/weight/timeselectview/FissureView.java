package com.ndhzs.timeplanning.weight.timeselectview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ndhzs.timeplanning.weight.TimeSelectView;

public class FissureView extends FrameLayout {

    private RectImgView mImgView;
    private RectImgView mSelfImgView;
    private ChildLayout mLeftLayout;
    private ChildLayout mRightLayout;

    public FissureView(@NonNull Context context) {
        super(context);
    }
    public FissureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTimeSelectView(TimeSelectView leftTimeView, TimeSelectView rightTimeView) {
        int[] leftViewPosition = new int[2];
        leftTimeView.getLocationInWindow(leftViewPosition);
        int[] rightViewPosition = new int[2];
        rightTimeView.getLocationInWindow(rightViewPosition);
        int leftBoundary = leftViewPosition[0] + leftTimeView.getWidth();
        int rightBoundary = rightViewPosition[0];
        mLeftLayout = leftTimeView.getChildLayout();
        mRightLayout = rightTimeView.getChildLayout();
    }

    public RectImgView setRectImgView(RectImgView imgView) {
        this.mImgView = imgView;
        addSameRectImgView();
        return mSelfImgView;
    }

    private void addSameRectImgView() {
        int[] selfPosition = new int[2];
        getLocationInWindow(selfPosition);
        int[] imgViewPosition = new int[2];
        mImgView.getLocationInWindow(imgViewPosition);
        int left = imgViewPosition[0] - selfPosition[0];
        int top = imgViewPosition[1] - selfPosition[1];
        mSelfImgView = mImgView.getSameImgView();
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.leftMargin = left;
        lp.topMargin = top;
        addView(mSelfImgView, lp);
    }

    public interface ITimeView {
        void addOtherRectImgView(RectImgView imgView);
    }
}
