package com.ndhzs.timeplanning.myview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class RectView extends View {

    private final Context mContext;
    private final HashMap<Rect, String> mRectAndDTime = new HashMap<>();//矩形与时间段差值

    private Paint mInsidePaint;//圆角矩形内部画笔
    private Paint mBorderPaint;//圆角矩形边框画笔
    private Paint mArrowsPaint;//时间差的箭头线画笔
    private Paint mRectTimePaint;//上下线时间画笔
    private Paint mStartTimePaint;//开始时间画笔
    private Paint mTextPaint;//任务名称画笔
    private Paint mDTimePaint;//时间差值画笔
    private final Path mArrowsPath = new Path();//箭头的路径
    private int mInitialRectY;//长按生成矩形时的不动的y值
    private int mUpperLimit, mLowerLimit;//当前矩形的上下限，不能移动到其他矩形区域
    private float mTextCenter, mDTimeCenter;//文字的水平线高度
    private float mRectTimeAscent, mRectTimeDescent;//矩形内部时间的ascent和descent线
    private float mDTimeHalfHeight;//右侧时间的字体高度的一半
    private ChildFrameLayout mChildFrameLayout;
    private final Rect initialRect = new Rect(-100, -100, -100, -100);//初始矩形
    private final Rect deletedRect = new Rect();//被删掉的矩形
    private final RectF rectF = new RectF();//用来给圆角矩形转换
    private final List<Rect> mRects = new ArrayList<>();
    private final HashMap<Rect, String> mRectAndName = new HashMap<>();//矩形与任务名称

    private boolean mIsFromHLine;//起点是从水平线开始的

    private int mExtraHeight;

    private static final int RADIUS = 8;//圆角矩形的圆角半径
    private static final int RECT_BORDER_WIDTH = 4;//圆角矩形边框厚度

    private int WHICH_CONDITION = 0;//保存长按的情况
    private static final int TOP = 1;//长按的顶部区域
    private static final int INSIDE = 2;//长按的内部区域
    private static final int BOTTOM = 3;//长按的底部区域
    private static final int EMPTY_AREA = 4;//长按的空白区域

    private static final int TOP_BOTTOM_WIDTH = 10;//长按响应顶部和底部的宽度
    private static int START_TIME_INTERVAL = 5;//按下空白区域时起始时间的分钟间隔数(必须为60的因数)

    private static float RECT_MIN_HEIGHT;//矩形最小高度，控制矩形能否保存,与字体大小有关
    private static float RECT_LESSER_HEIGHT;//矩形较小高度，控制矩形上下线时间能否显示,与字体大小有关
    private static float RECT_SHOE_START_TIME_HEIGHT;//矩形显示开始时间的最小高度，与字体大小有关

    /**
     * 注意！该View是镶嵌于ChildFrameLayout中，请不要自己用addView()调用，除非你搞懂了原理
     * @param context 传入context
     */
    public RectView(Context context) {
        super(context);
        this.mContext = context;
        initPaint();
    }
    private void initPaint() {
        mRectAndDTime.put(initialRect, "");
        //圆角矩形边框画笔
        mBorderPaint = generatePaint(true, Paint.Style.STROKE, RECT_BORDER_WIDTH);
        //圆角矩形内部画笔
        mInsidePaint = generatePaint(true, Paint.Style.FILL, 10);
        //时间差的箭头线画笔
        mArrowsPaint = generatePaint(false, Paint.Style.STROKE, 2);
        mArrowsPaint.setColor(0xFF000000);
        //上下线时间画笔
        mRectTimePaint = new Paint();
        mRectTimePaint.setColor(0xFF000000);
        mRectTimePaint.setAntiAlias(true);
        mRectTimePaint.setTextAlign(Paint.Align.LEFT);
        //开始时间画笔
        mStartTimePaint = new Paint(mRectTimePaint);
        mStartTimePaint.setTextAlign(Paint.Align.CENTER);
        //任务名称画笔
        mTextPaint = new Paint(mStartTimePaint);
        //时间差值画笔
        mDTimePaint = new Paint(mRectTimePaint);
        mDTimePaint.setTextAlign(Paint.Align.RIGHT);
    }

    private Paint generatePaint(boolean antiAlias, Paint.Style style, int strokeWidth) {
        Paint paint = new Paint();
        paint.setAntiAlias(antiAlias);//抗锯齿
        paint.setStyle(style);
        paint.setStrokeWidth(strokeWidth);
        return paint;
    }
    public void setChildFrameLayout(ChildFrameLayout childFrameLayout) {
        this.mChildFrameLayout = childFrameLayout;
    }
    public void setRectColor(int borderColor, int insideColor) {
        this.mBorderPaint.setColor(borderColor);
        this.mInsidePaint.setColor(insideColor);
    }
    public void setTextSize(int timeTextSize, int taskTextSize) {
        mRectTimePaint.setTextSize(timeTextSize);
        mStartTimePaint.setTextSize(mRectTimePaint.getTextSize());
        mTextPaint.setTextSize(taskTextSize);
        mDTimePaint.setTextSize(timeTextSize * 0.9f);

        Paint.FontMetrics fontMetrics = mRectTimePaint.getFontMetrics();
        mRectTimeAscent = fontMetrics.ascent;
        mRectTimeDescent = fontMetrics.descent;
        RECT_LESSER_HEIGHT = (mRectTimeDescent - mRectTimeAscent) * 2;

        fontMetrics = mStartTimePaint.getFontMetrics();
        RECT_SHOE_START_TIME_HEIGHT = fontMetrics.descent - fontMetrics.ascent;

        fontMetrics = mTextPaint.getFontMetrics();
        mTextCenter = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        RECT_MIN_HEIGHT = fontMetrics.descent - fontMetrics.ascent;

        fontMetrics = mDTimePaint.getFontMetrics();
        mDTimeHalfHeight = (fontMetrics.bottom - fontMetrics.top)/2;
        mDTimeCenter = mDTimeHalfHeight - fontMetrics.bottom;
    }
    public void setInterval(int extraHeight) {
        this.mExtraHeight = extraHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED://处于HorizontalScrollView中
            case MeasureSpec.AT_MOST://wrap_content
                int width = TimeFrameView.HORIZONTAL_LINE_LENGTH;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                TimeFrameView.HORIZONTAL_LINE_LENGTH = MeasureSpec.getSize(widthMeasureSpec);
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED://处于ScrollView中
            case MeasureSpec.AT_MOST://wrap_content，本程序不会用到这个
                int height = 300;
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                break;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawRect(canvas, initialRect, null);
        if (initialRect.height() > RECT_SHOE_START_TIME_HEIGHT) {
            //绘制开始的时间
            canvas.drawText(MyTime.getTime(initialRect.top),
                    initialRect.centerX(), initialRect.top - mRectTimeAscent + RECT_BORDER_WIDTH - 4, mStartTimePaint);
        }
        if (initialRect.height() > RECT_LESSER_HEIGHT) {
            drawTopBottomTime(canvas, initialRect, null, null);
        }
        if (initialRect.height() > RECT_MIN_HEIGHT) {
            drawArrows(canvas, initialRect, null);
        }
        for (Rect rect : mRects) {
            drawRect(canvas, rect, mRectAndName.get(rect));
            drawArrows(canvas, rect, mRectAndDTime.get(rect));
            drawTopBottomTime(canvas, rect, null, null);
        }
    }
    public void drawRect(Canvas canvas, Rect rect, String taskName) {
        float l = rect.left + RECT_BORDER_WIDTH /2.0f;
        float t = rect.top + RECT_BORDER_WIDTH /2.0f;
        float r = rect.right - RECT_BORDER_WIDTH /2.0f;
        float b = rect.bottom - RECT_BORDER_WIDTH /2.0f;
        rectF.set(l, t, r, b);
        canvas.drawRoundRect(rectF, RADIUS, RADIUS, mInsidePaint);
        canvas.drawRoundRect(rectF, RADIUS, RADIUS, mBorderPaint);
        if (taskName != null)
            canvas.drawText(taskName, rect.centerX(), rect.centerY() + mTextCenter, mTextPaint);
    }
    public void drawArrows(Canvas canvas, Rect rect, String stDTime) {
        int timeRight = rect.right - 5;
        if (stDTime != null) {
            canvas.drawText(stDTime,
                    timeRight, rect.centerY() + mDTimeCenter, mDTimePaint);
        }else {
            canvas.drawText(MyTime.getDiffTime(rect.top, rect.bottom),
                    timeRight, rect.centerY() + mDTimeCenter, mDTimePaint);
        }
        {
            int horizontalInterval = 5;
            int verticalInterval = 9;
            int verticalCenter = rect.right - 25;
            int l = verticalCenter - horizontalInterval;
            int t = rect.top + 4;
            int r = verticalCenter + horizontalInterval;
            int b = rect.bottom - 4;

            mArrowsPath.moveTo(verticalCenter, t);
            mArrowsPath.lineTo(verticalCenter, rect.centerY() - mDTimeHalfHeight);

            mArrowsPath.moveTo(verticalCenter, rect.centerY() + mDTimeHalfHeight);
            mArrowsPath.lineTo(verticalCenter, b);

            mArrowsPath.moveTo(l, t + verticalInterval);
            mArrowsPath.lineTo(verticalCenter, t);
            mArrowsPath.lineTo(r, t + verticalInterval);

            mArrowsPath.moveTo(l, b - verticalInterval);
            mArrowsPath.lineTo(verticalCenter, b);
            mArrowsPath.lineTo(r, b - verticalInterval);

            canvas.drawPath(mArrowsPath, mArrowsPaint);
            mArrowsPath.rewind();
        }
    }
    public void drawTopBottomTime(Canvas canvas, Rect rect, String topTime, String bottomTime) {
        int l = rect.left + RECT_BORDER_WIDTH + 1;
        float t = rect.top - mRectTimeAscent + RECT_BORDER_WIDTH - 4;
        float b = rect.bottom - mRectTimeDescent - RECT_BORDER_WIDTH + 4;
        if (topTime == null) {
            canvas.drawText(MyTime.getTime(rect.top + mExtraHeight), l, t, mRectTimePaint);
            canvas.drawText(MyTime.getTime(rect.bottom + mExtraHeight), l, b, mRectTimePaint);
        }else {
            canvas.drawText(topTime, l, t, mRectTimePaint);
            canvas.drawText(bottomTime, l, b, mRectTimePaint);
        }
    }

    private int isContain(int x, int y) {
        for (int i = 0; i < mRects.size(); i++) {
            Rect rect = mRects.get(i);
            if (rect.contains(x, y)) {
                if (y + TOP_BOTTOM_WIDTH > rect.bottom) {
                    mInitialRectY = rect.top;
                    return BOTTOM;
                }else if (y - TOP_BOTTOM_WIDTH < rect.top) {
                    mInitialRectY = rect.bottom;
                    return TOP;
                }else {
                    deletedRect.set(mRects.get(i));
                    mRects.remove(i);
                    invalidate(deletedRect);
                    return INSIDE;
                }
            }
        }
        return EMPTY_AREA;
    }
    private int getCorrectTop(int y) {
        int hLineTopHeight = MyTime.getHLineTopHeight(y + mExtraHeight) - mExtraHeight;
        int height = y - hLineTopHeight;
        int minuteInterval = (60 % START_TIME_INTERVAL == 0) ? START_TIME_INTERVAL : 5;
        if (height <= MyTime.sEveryMinuteHeight[minuteInterval]) {
            mIsFromHLine = true;
            return hLineTopHeight + TimeFrameView.HORIZONTAL_LINE_WIDTH;
        }else {
            mIsFromHLine = false;//刷新
            for (int i = minuteInterval; i < MyTime.sEveryMinuteHeight.length - minuteInterval; i += minuteInterval) {
                if (height <= MyTime.sEveryMinuteHeight[i + minuteInterval]) {
                    return hLineTopHeight + (int) MyTime.sEveryMinuteHeight[i] + 1;
                }
            }
        }
        Log.d(TAG, "getTrueTop: 数组出错！");
        return y;
    }
//    private int getEndTimeCorrectHeight(int minute) {
//        if (minute == 0)
//            return (int)mEveryMinuteHeight[60];
//        return
//    }
    private int getUpperLimit(int top) {
        List<Integer> bottoms = new ArrayList<>();
        for (int i = 0; i < mRects.size(); i++) {
            int bottom = mRects.get(i).bottom;
            if (bottom < top) {
                bottoms.add(bottom);
            }
        }
        return (bottoms.size() == 0) ? 0 :
                Collections.max(bottoms, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                }) + TimeFrameView.HORIZONTAL_LINE_WIDTH;
    }
    private int getLowerLimit(int bottom) {
        List<Integer> tops = new ArrayList<>();
        for (int i = 0; i < mRects.size(); i++) {
            int top = mRects.get(i).top;
            if (top > bottom) {
                tops.add(top);
            }
        }
        return (tops.size() == 0) ? getHeight():
                Collections.min(tops, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                }) - TimeFrameView.HORIZONTAL_LINE_WIDTH;
    }

    /**
     * 此为TimeSelectView的dispatchTouchEvent()在长按情况下用延时调用的方法，调用该方法代表长按已经产生。
     * 在我写的代码中，传入的y的值是大于或等于额外宽度mExtraHeight的
     * @param x 传入x值
     * @param y 传入y值。注意！请判断是否是RectView的坐标系，若不是记得加上TimeSelectView.getScrollY()
     */
    public void longPress(int x, int y) {
        WHICH_CONDITION = isContain(x, y);
        Log.d(TAG, "longPress: " + WHICH_CONDITION);
        switch (WHICH_CONDITION) {
            case EMPTY_AREA: {
                //长按开启选取，先识别了位置，再加载了动画
                int l, t, r, b;
                l = 0;
                t = getCorrectTop(y);
                r = getWidth();
                b = t + TimeFrameView.HORIZONTAL_LINE_WIDTH;
                initialRect.set(l, t, r, b);
                mInitialRectY = t;
                mUpperLimit = getUpperLimit(mInitialRectY);
                mLowerLimit = getLowerLimit(mInitialRectY);
                break;
            }
            case INSIDE: {
                RectImgView imgView = new RectImgView(mContext, deletedRect,
                    mRectAndName.get(deletedRect), mRectAndDTime.get(deletedRect), this);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                lp.leftMargin = getLeft();
                lp.topMargin = getTop() + deletedRect.top;
                mUpperLimit = getUpperLimit(deletedRect.top);
                mLowerLimit = getLowerLimit(deletedRect.bottom);
                mChildFrameLayout.addRectImgView(imgView, lp, mUpperLimit, mLowerLimit);
                break;
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                Log.d(TAG, "onTouchEvent: RectView");
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                theMoveEvent(x, y);
                break;
            }
            case MotionEvent.ACTION_UP: {
                theUpEvent(x, y);
                break;
            }
        }
        return true;
    }
    private void theMoveEvent(int x, int y) {
        Log.d(TAG, "theMoveEvent: " + WHICH_CONDITION);
        switch (WHICH_CONDITION) {
            case EMPTY_AREA: {
                if (y >= mInitialRectY) {
                    initialRect.bottom = y;
                    initialRect.top = mInitialRectY;//防止从y < mInitialRectY回来top为最后一次y的值
                    if (y >= mLowerLimit) {
                        initialRect.bottom = mLowerLimit;
                    }
                }else {
                    initialRect.bottom = mInitialRectY;
                    if (mIsFromHLine)
                        initialRect.bottom = mInitialRectY - TimeFrameView.HORIZONTAL_LINE_WIDTH;
                    initialRect.top = Math.max(y, mUpperLimit);
                }
                invalidate(initialRect);
                break;
            }
            case INSIDE: {}
            case TOP: {}
            case BOTTOM: {
                break;
            }
        }
    }
    private void theUpEvent(int x, int y) {
        Log.d(TAG, "theUpEvent: ");
        switch (WHICH_CONDITION) {
            case EMPTY_AREA: {
                if (initialRect.height() > RECT_MIN_HEIGHT) {
                    Rect re = new Rect(initialRect);
                    mRects.add(re);
                    mRectAndName.put(re, "请设置任务名称！");
                    mRectAndDTime.put(re, MyTime.getDiffTime(re.top, re.bottom));
                }
                invalidate(initialRect);
                initialRect.set(-100, -100, -100, -100);
                break;
            }
            case INSIDE: {
                break;
            }
            case TOP: {}
            case BOTTOM: {
                break;
            }
        }
    }

    public void deleteHashMap() {
        mRectAndName.remove(deletedRect);
        mRectAndDTime.remove(deletedRect);
    }
    public void addDeleteRect(int top, int height) {
        Rect rect = new Rect(0, top, getWidth(), top + height);
        mRects.add(rect);
        mRectAndName.put(rect, mRectAndName.get(deletedRect));
        mRectAndDTime.put(rect, mRectAndDTime.get(deletedRect));
        invalidate(deletedRect);
        if (!rect.equals(deletedRect)) {
            deleteHashMap();
        }
    }

    private static final String TAG = "123";
}
