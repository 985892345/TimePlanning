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

public class LeftTimeVPAdapter extends RecyclerView.Adapter<LeftTimeVPAdapter.LeftViewHolder> {

    private ViewPager2 mSelfViewPager;
    private ViewPager2 mLinkViewPager;
    private TimeSelectView mLeftTimeView;

    @NonNull
    @Override
    public LeftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager2_left_time, parent, false);
        return new LeftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeftViewHolder holder, int position) {
        mLeftTimeView = holder.leftTimeView;
        mLeftTimeView.setLinkViewPager2(mSelfViewPager);
        mLeftTimeView.setOnScrollViewListener(new TimeSelectView.onScrollViewListener() {
            @Override
            public void onScrollChanged(int y) {
                TimeSelectView rightTimeView = ((RightTimeVPAdapter) Objects.requireNonNull(mLinkViewPager.getAdapter())).getRightTimeView();
//                if (rightTimeView != null) {
//                    rightTimeView.setIsOpenScrollCallBack(false);
//                    rightTimeView.scrollTo(0, y);
//                }
            }
        });
        mLeftTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLeftTimeView.setName("一个任务");
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

    public TimeSelectView getLeftTimeView() {
        return mLeftTimeView;
    }

    static class LeftViewHolder extends RecyclerView.ViewHolder{

        TimeSelectView leftTimeView;
        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            leftTimeView = itemView.findViewById(R.id.time_view_left);
        }
    }
}
