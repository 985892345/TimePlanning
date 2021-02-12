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
        holder.leftTimeView.setLinkViewPager2(mViewPager);
        holder.leftTimeView.setOnScrollViewListener(new TimeSelectView.onScrollViewListener() {
            @Override
            public void onScrollChanged(int y) {
                holder.rightTimeView.setIsOpenScrollCallBack(false);
                holder.rightTimeView.scrollTo(0, y);
            }
        });
        holder.rightTimeView.setLinkViewPager2(mViewPager);
        holder.rightTimeView.setOnScrollViewListener(new TimeSelectView.onScrollViewListener() {
            @Override
            public void onScrollChanged(int y) {
                holder.leftTimeView.setIsOpenScrollCallBack(false);
                holder.leftTimeView.scrollTo(0, y);
            }
        });

        holder.leftTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.leftTimeView.setName("点击的左边");
            }
        });
        holder.rightTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.rightTimeView.setName("点击的右边");
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
