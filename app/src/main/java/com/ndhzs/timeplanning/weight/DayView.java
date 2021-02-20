package com.ndhzs.timeplanning.weight;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.timeselectview.FrameView;

public class DayView extends View {

    private Paint mDayPaint;
    private Paint mCalenderPaint;
    private Paint mCirclePaint;
    private Paint mRestDayPaint;
    private final int mExtraHeight = 24;
    private final int mColorWhite = 0xFFFFFFFF;
    private final int mColorBlack = 0xFF000000;
    private final int mColorGray = 0xFFDDDDDD;
    private final int mColorGreen = 0xFF4CAF50;
    private final int mColorRed = 0xFFFF0000;
    private final int mCircleColor;
    private final int mCalenderColor = 0xFF828282;//阴历的文字颜色，默认灰色
    private int mNowCirclePosition = -1;//圆实际在的位置
    private float mCirclePosition = -1;//当天圆该在的位置
    private float mCircleRadius;
    private float mInitialCircleRadius;
    private float mCircleDrawHeight;
    private final float mTextSize;
    private float mDayTextHeight;
    private float mDayTextDrawHeight;
    private float mRestDayTextDrawHeight;
    private float mCalenderTextDrawHeight;
    private OnWeekClickListener mOnWeekClickListener;
    private String[] mDate = new String[]{"14", "15", "16", "17", "18", "19", "20"};
    private String[] mCalender = new String[7];
    private String[] mRectDay = new String[7];

    public void setDate(String[] dates) {
        mDate = dates;
        invalidate();
    }
    public void setCalender(String[] calender) {
        mCalender = calender;
        invalidate();
    }
    public void setRectDays(String[] rectDays) {
        mRectDay = rectDays;
        invalidate();
    }

    /**
     * 设置当前该显示周几，如果不在该周显示，请设置成 -1
     * @param position 周日到周六分别对应 0 ~ 6
     */
    public void setCirclePosition(int position) {
        mNowCirclePosition = position + 1;
        mCirclePosition = mNowCirclePosition;
        invalidate();
    }

    /**
     * 设置与其他的联动
     * @param position 移动的位置，周日到周六分别对应 0 ~ 6
     */
    public void setMovePosition(int position) {
        mCirclePosition = position + 1;
    }

    /**
     * 设置点击的日期监听
     * @param l 传入OnWeekClickListener
     */
    public void setOnWeekClickListener(OnWeekClickListener l) {
        this.mOnWeekClickListener = l;
    }

    public DayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ty = context.obtainStyledAttributes(attrs, R.styleable.DayView);
        mCircleColor = ty.getColor(R.styleable.DayView_circleColor, ContextCompat.getColor(context, R.color.default_color));
        mTextSize = ty.getDimension(R.styleable.DayView_dayTextSize, 60);
        ty.recycle();
        init();
    }

    private void init() {
        mDayPaint = new Paint();
        mDayPaint.setAntiAlias(true);
        mDayPaint.setTextSize(mTextSize);
        mDayPaint.setFakeBoldText(true);
        mDayPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = mDayPaint.getFontMetrics();
        mDayTextHeight = fontMetrics.descent - fontMetrics.ascent;
        mDayTextDrawHeight = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom + mDayTextHeight / 2 + mExtraHeight;
        mCircleDrawHeight = mDayTextHeight;
        mCircleRadius = mDayTextHeight * 0.88f;
        mInitialCircleRadius = mCircleRadius;

        mCalenderPaint = new Paint();
        mCalenderPaint.setAntiAlias(true);
        mCalenderPaint.setTextSize(0.46f * mTextSize);
        mCalenderPaint.setTextAlign(Paint.Align.CENTER);
        fontMetrics = mCalenderPaint.getFontMetrics();
        mCalenderTextDrawHeight = mDayTextHeight - fontMetrics.ascent + 0.5f * mExtraHeight;

        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeWidth(10);

        mRestDayPaint = new Paint(mCalenderPaint);
        mRestDayPaint.setTextSize(0.3f * mTextSize);
        mRestDayTextDrawHeight = 1.9f * mExtraHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED://处于HorizontalScrollView中
            case MeasureSpec.AT_MOST://wrap_content
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(1000, MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                FrameView.HORIZONTAL_LINE_LENGTH = MeasureSpec.getSize(widthMeasureSpec);
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED://处于ScrollView中
            case MeasureSpec.AT_MOST://wrap_content
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (mCircleRadius * 2 + mExtraHeight), MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                break;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (mCirclePosition == mNowCirclePosition) {
            mCirclePaint.setColor(mCircleColor);
        }else {
            mCirclePaint.setColor(mColorGray);
        }
        canvas.drawCircle(getWidth()/14.0f * (2 * mCirclePosition - 1), mCircleDrawHeight, mCircleRadius, mCirclePaint);
        for (int i = 0; i < 7; i++) {
            String calender = mCalender[i];
            String rest = mRectDay[i];
            if (calender == null) {
                calender = "";
            }
            if (rest == null) {
                rest = "";
            }
            if (calender.startsWith("初") || calender.startsWith("十") || calender.startsWith("廿")) {
                mCalenderPaint.setColor(mCalenderColor);
            }else {
                mCalenderPaint.setColor(mCircleColor);
            }
            if (rest.equals("休")) {
                mRestDayPaint.setColor(mColorGreen);
            }else if (rest.equals("班")) {
                mRestDayPaint.setColor(mColorRed);
            }
            if (i + 1 == mNowCirclePosition) {
                if (mCirclePosition == mNowCirclePosition) {
                    mDayPaint.setColor(mColorWhite);
                    mRestDayPaint.setColor(mColorWhite);
                    mCalenderPaint.setColor(mColorWhite);
                }else {
                    mDayPaint.setColor(mCircleColor);
                }
            }else {
                mDayPaint.setColor(mColorBlack);
            }
            float x = getWidth() / 14.0f * (2 * i + 1);
            canvas.drawText(mDate[i], x, mDayTextDrawHeight, mDayPaint);
            canvas.drawText(calender, x, mCalenderTextDrawHeight, mCalenderPaint);
            canvas.drawText(rest, x + 1.6f * mExtraHeight, mRestDayTextDrawHeight, mRestDayPaint);
        }
    }

    private int mInitialX, mInitialY;
    private final int MOVE_THRESHOLD = 15;//识别是点击的阈值
    private boolean mIsClick;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialX = x;
                mInitialY = y;
                mIsClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(x - mInitialX) > MOVE_THRESHOLD || Math.abs(y - mInitialY) > MOVE_THRESHOLD) {
                    mIsClick = false;
                }else {
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsClick) {
                    if (Math.abs(mInitialY - mCircleDrawHeight) < 2 * mCircleRadius) {
                        for (int i = 0; i < 7; i++) {
                            if (Math.abs(mInitialX - getWidth() / 14.0f * (2 * i + 1)) < 2 * mCircleRadius) {
                                if (mOnWeekClickListener != null) {
                                    mOnWeekClickListener.onWeekClick(i);
                                }
                                if (i + 1 == mCirclePosition) {//点击相同的位置
                                    return false;
                                }
                                clickMove(i + 1);
                            }
                        }
                    }
                }else {
                    return false;
                }
        }
        return true;
    }

    private void clickMove(int clickPosition) {
        ValueAnimator animator = ValueAnimator.ofFloat(mCirclePosition, clickPosition);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCirclePosition = (float) animation.getAnimatedValue();
                mCircleRadius = 0.5f * mInitialCircleRadius;
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCircleRadius = mInitialCircleRadius;
                mCirclePosition = clickPosition;
                invalidate();
            }
        });
        animator.setDuration(200);
        animator.start();
    }

    public interface OnWeekClickListener {
        /**
         * 点击日期的回调
         * @param position 周日到周六分别对应1 ~ 7
         */
        void onWeekClick(int position);
    }

    public static final String TAG = "123";
}
