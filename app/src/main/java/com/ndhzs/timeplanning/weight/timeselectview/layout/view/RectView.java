package com.ndhzs.timeplanning.weight.timeselectview.layout.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.ndhzs.timeplanning.weight.timeselectview.TimeSelectView;
import com.ndhzs.timeplanning.weight.timeselectview.utils.TimeViewUtil;
import com.ndhzs.timeplanning.weight.timeselectview.bean.TaskBean;
import com.ndhzs.timeplanning.weight.timeselectview.layout.ChildLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class RectView extends View implements ChildLayout.IUpEvent, TimeSelectView.IRectView {

    private final Context mContext;
    private final TimeViewUtil mTimeViewUtil;

    private Paint mInsidePaint;//圆角矩形内部画笔
    private Paint mBorderPaint;//圆角矩形边框画笔
    private Paint mArrowsPaint;//时间差的箭头线画笔
    private Paint mRectTimePaint;//上下线时间画笔
    private Paint mStartTimePaint;//开始时间画笔
    private Paint mTextPaint;//任务名称画笔
    private Paint mDTimePaint;//时间差值画笔
    private final Path mArrowsPath = new Path();//箭头的路径
    private int mBorderColor, mInsideColor;
    private int mInitialRectY;//长按生成矩形时的不动的y值
    private int mUpperLimit, mLowerLimit;//当前矩形的上下限，不能移动到其他矩形区域
    private float mTextCenter, mDTimeCenter;//任务名称和时间差值的水平线
//    private float mTextAscent, mTextDescent;//任务的ascent和descent线
    private float mRectTimeAscent, mRectTimeDescent;//矩形内部时间的ascent和descent线
    private float mDTimeHalfHeight;//右侧时间的字体高度的一半
    private ChildLayout mChildLayout;
    private final Rect initialRect = new Rect(-500, -500, -500, -500);//初始矩形
    private final Rect deletedRect = new Rect();//被删掉的矩形
    private final RectF rectF = new RectF();//用来给圆角矩形转换
    private final List<Rect> mRects = new ArrayList<>();
    private final HashMap<Rect, TaskBean> mRectAndData = new HashMap<>();
    private RectImgView mImgViewRect;
    private TimeSelectView.OnDataChangeListener mOnDataChangeListener;

    private boolean mIsAllowDraw;//说明正在自动滑动，通知onTouchEvent()的MOVE不要处理，不然绘图会卡

    private int mExtraHeight;

    public static final int RADIUS = 8;//圆角矩形的圆角半径
    public static final int RECT_BORDER_WIDTH = 4;//圆角矩形边框厚度

    public static int WHICH_CONDITION = 0;//保存长按的情况
    public static final int TOP = 1;//长按的顶部区域
    public static final int INSIDE = 2;//长按的内部区域
    public static final int BOTTOM = 3;//长按的底部区域
    public static final int EMPTY_AREA = 4;//长按的空白区域

    private static final int TOP_BOTTOM_WIDTH = 17;//长按响应顶部和底部的宽度

    private float RECT_MIN_HEIGHT;//矩形最小高度，控制矩形能否保存,与字体大小有关
    private float RECT_LESSER_HEIGHT;//矩形较小高度，控制矩形上下线时间能否显示,与字体大小有关
    private float RECT_SHOE_START_TIME_HEIGHT;//矩形显示开始时间的最小高度，与字体大小有关(大于它不一定能保存矩形)

    public RectView(Context context, TimeViewUtil timeViewUtil) {
        super(context);
        this.mContext = context;
        this.mTimeViewUtil = timeViewUtil;
        initPaint();
    }
    private void initPaint() {
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
    public void setChildLayout(ChildLayout childLayout) {
        this.mChildLayout = childLayout;
    }
    public void setRectColor(int borderColor, int insideColor) {
        this.mBorderColor = borderColor;
        this.mInsideColor = insideColor;
    }
    public void setTextSize(float timeTextSize, float taskTextSize) {
        mRectTimePaint.setTextSize(timeTextSize);
        mStartTimePaint.setTextSize(timeTextSize);
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
//        mTextAscent = fontMetrics.ascent;
//        mTextDescent = fontMetrics.descent;
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
                int width = FrameView.HORIZONTAL_LINE_LENGTH;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.EXACTLY://match_parent、精确值
                FrameView.HORIZONTAL_LINE_LENGTH = MeasureSpec.getSize(widthMeasureSpec);
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
        if (initialRect.height() > RECT_LESSER_HEIGHT) {
            drawTopBottomTime(canvas, initialRect, null, null);
        }
        if (initialRect.height() > RECT_MIN_HEIGHT) {
            drawArrows(canvas, initialRect, null);
        }
        for (Rect rect : mRects) {
            drawRect(canvas, rect, mRectAndData.get(rect).getName());
            if (TimeSelectView.IS_SHOW_DIFFERENT_TIME) {
                drawArrows(canvas, rect, mRectAndData.get(rect).getDiffTime());
            }
            if (TimeSelectView.IS_SHOW_TOP_BOTTOM_TIME) {
                drawTopBottomTime(canvas, rect, null, null);
            }
        }
    }
    public void drawRect(Canvas canvas, Rect rect, String taskName) {
        if (mRectAndData.containsKey(rect)) {
            mBorderPaint.setColor(mRectAndData.get(rect).getBorderColor());
            mInsidePaint.setColor(mRectAndData.get(rect).getInsideColor());
        }else {
            mBorderPaint.setColor(mBorderColor);
            mInsidePaint.setColor(mInsideColor);
        }
        float l = rect.left + RECT_BORDER_WIDTH /2.0f;
        float t = rect.top + RECT_BORDER_WIDTH /2.0f;
        float r = rect.right - RECT_BORDER_WIDTH /2.0f;
        float b = rect.bottom - RECT_BORDER_WIDTH /2.0f;
        rectF.set(l, t, r, b);
        canvas.drawRoundRect(rectF, RADIUS, RADIUS, mInsidePaint);
        canvas.drawRoundRect(rectF, RADIUS, RADIUS, mBorderPaint);
        if (taskName != null) {
            canvas.drawText(taskName, rect.centerX(), rect.centerY() + mTextCenter, mTextPaint);
            return;
        }
        //下面的if只有在绘制initialRect才会调用
        if (WHICH_CONDITION == TOP || WHICH_CONDITION == BOTTOM) {
            canvas.drawText(mRectAndData.get(deletedRect).getName(), rect.centerX(), rect.centerY() + mTextCenter, mTextPaint);
        }else {
            if (initialRect.height() > RECT_SHOE_START_TIME_HEIGHT) {
                //绘制开始的时间，就是顶部中间那个时间
                canvas.drawText(mTimeViewUtil.getTime(initialRect.top), initialRect.centerX(),
                        initialRect.top - mRectTimeAscent + RECT_BORDER_WIDTH - 4, mStartTimePaint);
            }
        }
    }
    public void drawArrows(Canvas canvas, Rect rect, String stDTime) {
        int timeRight = rect.right - 5;
        if (stDTime != null) {
            canvas.drawText(stDTime,
                    timeRight, rect.centerY() + mDTimeCenter, mDTimePaint);
        }else {
            canvas.drawText(mTimeViewUtil.getDiffTime(rect.top, rect.bottom),
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
            canvas.drawText(mTimeViewUtil.getTime(rect.top), l, t, mRectTimePaint);
            canvas.drawText(mTimeViewUtil.getTime(rect.bottom), l, b, mRectTimePaint);
        }else {
            canvas.drawText(topTime, l, t, mRectTimePaint);
            canvas.drawText(bottomTime, l, b, mRectTimePaint);
        }
    }

    /**
     * 此为TimeSelectView的dispatchTouchEvent()在长按情况下用延时调用的方法，调用该方法代表长按已经产生。
     * 在我写的代码中，传入的y的值是大于或等于额外宽度mExtraHeight的
     * @param y 传入y值。注意！请判断是否是RectView的坐标系，若不是记得加上mExtraHeight
     */
    @Override
    public void longPress(int y) {
        WHICH_CONDITION = isContain(y);//这里会执行一些操作
        switch (WHICH_CONDITION) {
            case EMPTY_AREA: {
                int l, t, r, b;
                l = 0;
                t = getInitialTimeHeight(y);
                r = getWidth();
                b = t + FrameView.HORIZONTAL_LINE_WIDTH;
                initialRect.set(l, t, r, b);
                mInitialRectY = t;
                mUpperLimit = getUpperLimit(mInitialRectY);//在MOVE中使用
                mLowerLimit = getLowerLimit(mInitialRectY);//在MOVE中使用
                break;
            }
            case INSIDE: {
                mImgViewRect = new RectImgView(mContext, deletedRect,
                    mRectAndData.get(deletedRect), this, mTimeViewUtil);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                lp.leftMargin = getLeft();
                lp.topMargin = getTop() + deletedRect.top;
                mUpperLimit = getUpperLimit(deletedRect.top);//在下下一步使用，还在自动滑动中使用
                mLowerLimit = getLowerLimit(deletedRect.bottom);//在下一步使用，还在自动滑动中使用
                mChildLayout.addImgViewRect(mImgViewRect, lp, mUpperLimit, mLowerLimit, mRectAndData.get(deletedRect));
                break;
            }
            case TOP:
            case BOTTOM:
                mUpperLimit = getUpperLimit(mInitialRectY);
                mLowerLimit = getLowerLimit(mInitialRectY);
                break;
        }
    }
    private int isContain(int y) {//TimeSelectView的回调longPress()方法的第一步调用
        deletedRect.setEmpty();
        for (int i = 0; i < mRects.size(); i++) {
            Rect rect = mRects.get(i);
            if (y >= rect.top && y <= rect.bottom) {
                if (rect.bottom - y < TOP_BOTTOM_WIDTH) {
                    mInitialRectY = rect.top;
                    deletedRect.set(rect);
                    initialRect.set(rect);
                    mRects.remove(i);
                    return BOTTOM;
                }else if (y - rect.top < TOP_BOTTOM_WIDTH) {
                    mInitialRectY = rect.bottom;
                    deletedRect.set(rect);
                    initialRect.set(rect);
                    mRects.remove(i);
                    return TOP;
                }else {
                    deletedRect.set(rect);
                    invalidate(deletedRect);
                    mRects.remove(i);
                    return INSIDE;
                }
            }
        }
        return EMPTY_AREA;
    }
    private int getInitialTimeHeight(int y) {//长按空白区域时调用
        mUpperLimit = getUpperLimit(y);//之后会在longPress()中重新赋值
        int hLineTopHeight = mTimeViewUtil.getHLineTopHeight(y);
        int relativeHeight = y - hLineTopHeight;
        int minuteInterval = mTimeViewUtil.TIME_INTERVAL;
        for (int i = 0; i < mTimeViewUtil.mEveryMinuteHeight.length - minuteInterval; i += minuteInterval) {
            if (relativeHeight <= mTimeViewUtil.mEveryMinuteHeight[i + minuteInterval]) {
                /*
                 * 如果MyTime.sEveryMinuteHeight[i] = 12.5, 则取大于等于的整数, 取13, 此时13才是该分钟数的顶线
                 * 如果刚好等于12.0, 则取12, 此时12刚好是该分钟数的顶线
                 * */
                int correctHeight = hLineTopHeight + (int)Math.ceil(mTimeViewUtil.mEveryMinuteHeight[i]) + 1;
                return Math.max(mUpperLimit, correctHeight);
            }
        }
        return y;//never
    }
    private int getStartTimeCorrectHeight(int top) {//所有的UP事件都会调用
        int upperLimit = getUpperLimit(top);
        int hLineTopHeight = mTimeViewUtil.getHLineTopHeight(top);
        int relativeHeight = top - hLineTopHeight;
        if (relativeHeight <= FrameView.HORIZONTAL_LINE_WIDTH) {
            return hLineTopHeight + FrameView.HORIZONTAL_LINE_WIDTH;
        }else {
            //最后面的加1是控制两个区域的交界时间能否相同，加1就能相同，不加就除了00分钟以外都不能相同
            int correctHeight = hLineTopHeight + (int)Math.ceil(mTimeViewUtil.mEveryMinuteHeight[mTimeViewUtil.getMinute(top + mExtraHeight)]) + 1;
            return Math.max(upperLimit, correctHeight);
        }
    }
    private int getEndTimeCorrectHeight(int bottom) {//所有的UP事件都会调用
        int lowerLimit = getLowerLimit(bottom);
        int hLineTopHeight = mTimeViewUtil.getHLineTopHeight(bottom);
        int relativeHeight = bottom - hLineTopHeight;
        if (relativeHeight < mTimeViewUtil.mEveryMinuteHeight[1]) {//这个时候你滑到的时间为0分钟
            return hLineTopHeight;
        }else {
            /*
             * 先加1，最后又减1的原因是因为：如果我滑到了2分钟，我要将rect.bottom设置为2分钟的最底部的一格
             * 则可以加个1找到3分钟的顶部，此时有两种情况：
             *     一、如果3分钟的顶部为12.5，那么2分钟的最底部的一格为12
             *     二、如果3分钟的顶部为12，那么2分钟的最底部的一格为11
             * 于是就可以减个1，分别得到11.5和11，再向上取整，得到12和11
             *
             * 最后面的减1是控制两个区域的交界时间能否相同，就是2分钟的最底部的一格的上一格，减1就能相同，不减就除了00分钟以外都不能相同
             * */
            int correctHeight = hLineTopHeight + (int)Math.ceil(mTimeViewUtil.mEveryMinuteHeight[mTimeViewUtil.getMinute(bottom + mExtraHeight) + 1] - 1) - 1;
            return Math.min(lowerLimit, correctHeight);
        }
    }
    public int getUpperLimit(int y) {//在开始长按时调用
        List<Integer> bottoms = new ArrayList<>();
        for (int i = 0; i < mRects.size(); i++) {
            int bottom = mRects.get(i).bottom;
            if (bottom == y) {//长按时就已经拦截了y == bottom的情况，这里是为了给长按整体移动的NowUpperLimit使用的
                return bottom + FrameView.HORIZONTAL_LINE_WIDTH;
            }
            if (bottom < y) {
                bottoms.add(bottom);
            }
        }
        return (bottoms.size() == 0) ? 0 :
                Collections.max(bottoms, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                }) + FrameView.HORIZONTAL_LINE_WIDTH;
    }
    public int getLowerLimit(int y) {//在开始长按时调用
        List<Integer> tops = new ArrayList<>();
        for (int i = 0; i < mRects.size(); i++) {
            int top = mRects.get(i).top;
            if (top == y) {//长按时就已经拦截了y == top的情况，这里是为了给长按整体移动的NowLowerLimit使用的
                return top - FrameView.HORIZONTAL_LINE_WIDTH;
            }
            if (top > y) {
                tops.add(top);
            }
        }
        return (tops.size() == 0) ? getHeight() - FrameView.HORIZONTAL_LINE_WIDTH:
                Collections.min(tops, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1 - o2;
                    }
                }) - FrameView.HORIZONTAL_LINE_WIDTH;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mIsAllowDraw) {
                    theMoveEvent(y);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                theUpEvent(y);
                break;
            }
        }
        return true;
    }
    private void theMoveEvent(int y) {
        switch (WHICH_CONDITION) {
            case TOP:
            case BOTTOM:
            case EMPTY_AREA: {
                if (y >= mInitialRectY) {
                    initialRect.top = mInitialRectY;//防止从y < mInitialRectY回来top为最后一次y的值
                    initialRect.bottom = Math.min(y, mLowerLimit);
                }else {
                    initialRect.bottom = mInitialRectY;
                    initialRect.top = Math.max(y, mUpperLimit);
                }
                invalidate(initialRect);
                break;
            }
        }
    }
    private void theUpEvent(int y) {
        switch (WHICH_CONDITION) {
            case EMPTY_AREA: {
                if (initialRect.height() > RECT_MIN_HEIGHT) {
                    initialRect.top = getStartTimeCorrectHeight(initialRect.top);
                    initialRect.bottom = getEndTimeCorrectHeight(initialRect.bottom);
                    Rect re = new Rect(initialRect);
                    mRects.add(re);
                    TaskBean taskBean = new TaskBean();
                    taskBean.setStartTime(mTimeViewUtil.getTime(re.top));
                    taskBean.setDiffTime(mTimeViewUtil.getDiffTime(re.top, re.bottom));
                    taskBean.setYear(mTimeViewUtil.getYear());
                    taskBean.setMonth(mTimeViewUtil.getMonth());
                    taskBean.setDay(mTimeViewUtil.getDay());
                    taskBean.setWeek(mTimeViewUtil.getWeek());
                    taskBean.setName("点击设置任务名称");
                    taskBean.setBorderColor(mBorderColor);
                    taskBean.setInsideColor(mInsideColor);
                    mRectAndData.put(re, taskBean);
                    dataIncrease(taskBean);
                }
                WHICH_CONDITION = 0;
                Rect refreshRect = new Rect(initialRect.left, initialRect.top - 100, initialRect.right, initialRect.bottom + 100);
                invalidate(refreshRect);
                initialRect.set(-500, -500, -500, -500);
                break;
            }
            case TOP:
            case BOTTOM:
                //此时mRectAndName和mRectAndDTime中的deletedRect都没有删除，但mRects的deletedRect已经删除
                if (initialRect.height() > RECT_MIN_HEIGHT) {
                    initialRect.top = getStartTimeCorrectHeight(initialRect.top);
                    initialRect.bottom = getEndTimeCorrectHeight(initialRect.bottom);
                    Rect re = new Rect(initialRect);
                    if (!re.equals(deletedRect)) {//如果大小和位置改变
                        TaskBean taskBean = mRectAndData.get(deletedRect);
                        taskBean.setStartTime(mTimeViewUtil.getTime(re.top));
                        taskBean.setDiffTime(mTimeViewUtil.getDiffTime(re.top, re.bottom));
                        dataAlter(taskBean);
                    }
                    mRects.add(re);//之前在isContain()中被删掉了
                    mRectAndData.put(re, mRectAndData.remove(deletedRect));
                }else {
                    mRectAndData.remove(deletedRect);
                }
                WHICH_CONDITION = 0;
                Rect refreshRect = new Rect(initialRect.left, initialRect.top - 100, initialRect.right, initialRect.bottom + 100);
                invalidate(refreshRect);
                initialRect.set(-500, -500, -500, -500);
                break;
            case INSIDE://never
                //已经被ChildFrameLayout拦截，此UP事件将不会被响应
                //后面的事件处理，我放在了addDeleteRect()方法中，用接口回调调用
                break;
        }
    }

    private int mClickLocation;
    @Override
    public boolean isClick(int y) {
        for (int i = 0; i < mRects.size(); i++) {
            Rect rect = mRects.get(i);
            if (y >= rect.top && y <= rect.bottom) {
                mClickLocation = i;
                return true;
            }
        }
        return false;
    }
    @Override
    public void setOnDataChangeListener(TimeSelectView.OnDataChangeListener onDataChangeListener) {
        this.mOnDataChangeListener = onDataChangeListener;
    }
    @Override
    public void setData(List<TaskBean> taskBeans) {
        if (taskBeans == null) {
            return;
        }
        for (TaskBean taskBean : taskBeans) {
            post(new Runnable() {
                @Override
                public void run() {
                    int top = getStartTimeCorrectHeight(mTimeViewUtil.getTopHeight(taskBean.getStartTime()));
                    int bottom = getEndTimeCorrectHeight(mTimeViewUtil.getBottomHeight(taskBean.getDiffTime()));
                    Rect rect = new Rect(0, top, getWidth(), bottom);
                    mRects.add(rect);
                    mRectAndData.put(rect, taskBean);
                    invalidate(rect);
                }
            });
        }
    }
    @Override
    public void isAllowDraw(boolean isAllowDraw) {
        mIsAllowDraw = isAllowDraw;
    }
    @Override
    public void refreshRect() {
        invalidate(mRects.get(mClickLocation));
    }
    @Override
    public void refresh(int y) {//用来自动滑动中的回调刷新矩形
        if (y >= mInitialRectY) {
            initialRect.bottom = Math.min(y, mLowerLimit);
        }else {
            initialRect.top = Math.max(y, mUpperLimit);

        }
        invalidate(initialRect);
    }
    @Override
    public int getUpperLimit() {//记得转换坐标系
        return mUpperLimit;
    }
    @Override
    public int getLowerLimit() {//记得转换坐标系
        return mLowerLimit;
    }
    @Override
    public RectImgView getImgViewRect() {//当长按情况是INSIDE时调用，返回Rect对象，可以随时得到top和bottom
        return mImgViewRect;
    }
    @Override
    public TaskBean getClickTaskBean() {
        return mRectAndData.get(mRects.get(mClickLocation));
    }

    @Override
    public void deleteHashMap() {//在ChildLayout的移动矩形删除时调用
        mRectAndData.remove(deletedRect);
    }
    @Override
    public void addDeletedRectFromTop(int top) {//在ChildLayout的移动矩形回来时调用
        String dTime = mRectAndData.get(deletedRect).getDiffTime();
        top = getStartTimeCorrectHeight(top);
        //bottom以时间差值来计算高度，如果不用时间差，就会出现上下边界时间出错的问题
        int bottom = getEndTimeCorrectHeight(mTimeViewUtil.getBottomTimeHeight(top, dTime));
        Rect rect = new Rect(0, top, getWidth(), bottom);
        mRects.add(rect);
        TaskBean taskBean = mRectAndData.get(deletedRect);
        taskBean.setStartTime(mTimeViewUtil.getTime(top));
        dataAlter(taskBean);
        mRectAndData.put(rect, taskBean);
        invalidate();
        if (!rect.equals(deletedRect)) {
            deleteHashMap();
        }
    }
    @Override
    public void addDeletedRectFromBottom(int bottom) {
        String dTime = mRectAndData.get(deletedRect).getDiffTime();
        bottom = getEndTimeCorrectHeight(bottom);
        //top以时间差值来计算高度，如果不用时间差，就会出现上下边界时间出错的问题
        int top = getStartTimeCorrectHeight(mTimeViewUtil.getTopTimeHeight(bottom, dTime));
        Rect rect = new Rect(0, top, getWidth(), bottom);
        mRects.add(rect);
        TaskBean taskBean = mRectAndData.get(deletedRect);
        taskBean.setStartTime(mTimeViewUtil.getTime(top));
        dataAlter(taskBean);
        mRectAndData.put(rect, taskBean);
        invalidate();
        if (!rect.equals(deletedRect)) {
            deleteHashMap();
        }
    }
    @Override
    public void deleteIsInsideRect(TaskBean taskBean) {
        for (Rect rect : mRectAndData.keySet()) {
            if (mRectAndData.get(rect) == taskBean) {
                mRects.remove(rect);
                mRectAndData.remove(rect);
                invalidate(rect);
                return;
            }
        }
    }
    @Override
    public void addRectFromTop(int top, TaskBean taskBean) {
        String dTime = taskBean.getDiffTime();
        top = getStartTimeCorrectHeight(top);
        //bottom以时间差值来计算高度，如果不用时间差，就会出现上下边界时间出错的问题
        int bottom = getEndTimeCorrectHeight(mTimeViewUtil.getBottomTimeHeight(top, dTime));
        Rect rect = new Rect(0, top, getWidth(), bottom);
        mRects.add(rect);
        taskBean.setStartTime(mTimeViewUtil.getTime(top));
        dataAlter(taskBean);
        mRectAndData.put(rect, taskBean);
        invalidate();
    }
    @Override
    public void addRectFromBottom(int bottom, TaskBean taskBean) {
        String dTime = taskBean.getDiffTime();
        bottom = getEndTimeCorrectHeight(bottom);
        //top以时间差值来计算高度，如果不用时间差，就会出现上下边界时间出错的问题
        int top = getStartTimeCorrectHeight(mTimeViewUtil.getTopTimeHeight(bottom, dTime));
        Rect rect = new Rect(0, top, getWidth(), bottom);
        mRects.add(rect);
        taskBean.setStartTime(mTimeViewUtil.getTime(top));
        dataAlter(taskBean);
        mRectAndData.put(rect, taskBean);
        invalidate();
    }
    @Override
    public int getNowUpperLimit(int y) {//记得转换坐标系
        return getUpperLimit(y);
    }
    @Override
    public int getNowLowerLimit(int y) {//记得转换坐标系
        return getLowerLimit(y);
    }

    private void dataIncrease(TaskBean newData) {
        if (mOnDataChangeListener != null) {
            mOnDataChangeListener.onDataIncrease(newData);
        }
    }

    private void dataAlter(TaskBean alterData) {
        if (mOnDataChangeListener != null) {
            mOnDataChangeListener.onDataAlter(alterData);
        }
    }

    private static final String TAG = "123";
}
