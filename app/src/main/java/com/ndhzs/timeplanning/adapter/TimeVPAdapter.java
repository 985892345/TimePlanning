package com.ndhzs.timeplanning.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.TimeSelectView;

public class TimeVPAdapter extends RecyclerView.Adapter<TimeVPAdapter.LeftViewHolder> {

    private TimeSelectView mLTimeView;
    private TimeSelectView mRTimeView;
    private ViewPager2 mViewPager;

    public TimeVPAdapter(ViewPager2 vp) {
        this.mViewPager = vp;
    }

    @NonNull
    @Override
    public LeftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager2_item, parent, false);
        return new LeftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeftViewHolder holder, int position) {
        mLTimeView = holder.leftTimeView;
        mRTimeView = holder.rightTimeView;
        mLTimeView.setLinkViewPager2(mViewPager);
        mLTimeView.setOnScrollViewListener(new TimeSelectView.onScrollViewListener() {
            @Override
            public void onScrollChanged(int y) {
                mRTimeView.setIsOpenScrollCallBack(false);
                mRTimeView.scrollTo(0, y);
            }
        });
        mRTimeView.setLinkViewPager2(mViewPager);
        mRTimeView.setOnScrollViewListener(new TimeSelectView.onScrollViewListener() {
            @Override
            public void onScrollChanged(int y) {
                mLTimeView.setIsOpenScrollCallBack(false);
                mLTimeView.scrollTo(0, y);
            }
        });

        mLTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLTimeView.setName("点击的左边");
            }
        });
        mRTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRTimeView.setName("点击的右边");
            }
        });
    }

    @Override
    public int getItemCount() {
        return 21;
    }

    static class LeftViewHolder extends RecyclerView.ViewHolder{

        TimeSelectView leftTimeView;
        TimeSelectView rightTimeView;
        public LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "LeftViewHolder: ");
            leftTimeView = itemView.findViewById(R.id.time_view_left);
            rightTimeView = itemView.findViewById(R.id.time_view_right);
        }
    }

    public static final String TAG = "123";
}
