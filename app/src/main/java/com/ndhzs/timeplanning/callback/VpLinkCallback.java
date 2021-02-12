package com.ndhzs.timeplanning.callback;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class VpLinkCallback extends ViewPager2.OnPageChangeCallback {

    private final ViewPager2 selfViewPager2;
    private final ViewPager2 linkViewPager2;
    private float previousOffset;
    private final String TAG = "123";

    public VpLinkCallback(ViewPager2 selfViewPager2, ViewPager2 linkViewPager2) {
        this.linkViewPager2 = linkViewPager2;
        this.selfViewPager2 = selfViewPager2;
        previousOffset = selfViewPager2.getCurrentItem();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (linkViewPager2.isFakeDragging()) {
            int difference = 0;
            View childView = linkViewPager2.getChildAt(0);
            //当你的linkViewPager2开启旁边的界面也加载时，会用到padding
            if (linkViewPager2.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL) {
                difference = linkViewPager2.getWidth() - childView.getPaddingLeft() - childView.getPaddingRight();
            }else if (linkViewPager2.getOrientation() == ViewPager2.ORIENTATION_VERTICAL) {
                difference = linkViewPager2.getHeight() - childView.getPaddingTop() - childView.getPaddingBottom();
            }
            //关键代码，因为每次positionOffset都是从0开始刷新（比如0->0.4->0.99999->0），为了好计算，加一个position就可以滑动到了具体的某个位置
            positionOffset += position;

            float differentOffset = previousOffset - positionOffset;
            float differentWidth = differentOffset * difference;

            linkViewPager2.fakeDragBy(differentWidth);
//            Log.d(TAG, "position = "+position);
//            Log.d(TAG, "previousOffset = "+previousOffset+"    positionOffset = "+positionOffset+"    differentOffset = " + differentOffset);
//            Log.d(TAG, "fakeDragBy is working and differentWidth = " + differentWidth);
            previousOffset = positionOffset;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);

        switch (state) {
            case ViewPager2.SCROLL_STATE_DRAGGING://开始滑动时，这个不能区分是否是虚拟滑动
                if (!selfViewPager2.isFakeDragging()){//如果自己不在虚拟滑动，判断原因是beginFakeDrag方法会调用自己的adapter，使另一个开启虚拟滑动
                    linkViewPager2.setUserInputEnabled(false);//设置这个禁止linkViewPager2用手滑动
                    linkViewPager2.beginFakeDrag();//之前打了log发现这方法会调用linkViewPager2的adapter
//                    Log.d(TAG, linkViewPager2.toString()+"=======================\n\n beginFakeDrag is working");
                }
                break;
            case ViewPager2.SCROLL_STATE_IDLE://页面完全稳定时
                if (linkViewPager2.isFakeDragging()){//如果另一个在虚拟滑动
                    linkViewPager2.endFakeDrag();
                    if (linkViewPager2.getCurrentItem() != selfViewPager2.getCurrentItem()){//防止有人恶意快速反向滑动，导致页面出问题
                        linkViewPager2.setCurrentItem(selfViewPager2.getCurrentItem(), false);
                    }
                    linkViewPager2.setUserInputEnabled(true);
//                    Log.d(TAG, linkViewPager2.toString()+"endFakeDrag is working");
                }
                break;
            case ViewPager2.SCROLL_STATE_SETTLING://用户手指离开，页面即将依靠惯性继续滑动时
                break;
        }
    }
}