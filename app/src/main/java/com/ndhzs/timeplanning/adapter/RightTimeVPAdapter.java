package com.ndhzs.timeplanning.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.TimeSelectView;

import java.util.Objects;

public class RightTimeVPAdapter extends RecyclerView.Adapter<RightTimeVPAdapter.RightViewHolder> {

    private ViewPager2 mSelfViewPager;
    private ViewPager2 mLinkViewPager;
    private TimeSelectView mRightTimeView;

    @NonNull
    @Override
    public RightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager2_right_time, parent, false);
        return new RightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RightViewHolder holder, int position) {
        mRightTimeView = holder.rightTimeView;
        mRightTimeView.setLinkViewPager2(mSelfViewPager);
        mRightTimeView.setOnScrollViewListener(new TimeSelectView.onScrollViewListener() {
            @Override
            public void onScrollChanged(int y) {
                TimeSelectView leftTimeView = ((LeftTimeVPAdapter) Objects.requireNonNull(mLinkViewPager.getAdapter())).getLeftTimeView();
//                if (leftTimeView != null) {
//                    leftTimeView.setIsOpenScrollCallBack(false);
//                    leftTimeView.scrollTo(0, y);
//                }
            }
        });
        mRightTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRightTimeView.setName("一个任务");
            }
        });
    }

    @Override
    public int getItemCount() {
        return 21;
    }

    public void setSelfViewPager(ViewPager2 selfViewPager) {
        this.mSelfViewPager = selfViewPager;
    }
    public void setLinkViewPager(ViewPager2 linkViewPager) {
        this.mLinkViewPager = linkViewPager;
    }

    public TimeSelectView getRightTimeView() {
        return mRightTimeView;
    }

    static class RightViewHolder extends RecyclerView.ViewHolder{

        TimeSelectView rightTimeView;
        public RightViewHolder(@NonNull View itemView) {
            super(itemView);
            rightTimeView = itemView.findViewById(R.id.time_view_right);
        }
    }
}