package com.ndhzs.timeplanning.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.NameDialog;
import com.ndhzs.timeplanning.weight.TimeSelectView;
import com.ndhzs.timeplanning.weight.timeselectview.bean.TaskBean;

import java.util.HashMap;
import java.util.HashSet;

public class TimeVpAdapter extends RecyclerView.Adapter<TimeVpAdapter.TimeViewHolder> {

    private Context mContext;
    private ViewPager2 mViewPager;
    private HashSet<TaskBean> mTaskBeans;
    private HashMap<Integer, HashSet<TaskBean>> mEveryDayData;

    public TimeVpAdapter(Context context, ViewPager2 vp, HashMap<Integer, HashSet<TaskBean>> everyDayData) {
        this.mContext = context;
        this.mViewPager = vp;
        this.mEveryDayData = everyDayData;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager2_item_timeplan, parent, false);
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        holder.leftTimeView.setLinkViewPager2(mViewPager);
        holder.leftTimeView.setLinkTimeSelectView(holder.rightTimeView);

        holder.leftTimeView.setData(mEveryDayData.get(position));
        holder.rightTimeView.setData(mEveryDayData.get(position));

        holder.leftTimeView.setOnScrollViewListener(new TimeSelectView.onScrollViewListener() {
            @Override
            public void onScrollChanged(int y) {
                holder.rightTimeView.scrollTo(0, y);
            }
        });
        holder.rightTimeView.setLinkViewPager2(mViewPager);
        holder.rightTimeView.setLinkTimeSelectView(holder.leftTimeView);
        holder.rightTimeView.setOnScrollViewListener(new TimeSelectView.onScrollViewListener() {
            @Override
            public void onScrollChanged(int y) {
                holder.leftTimeView.scrollTo(0, y);
            }
        });

        holder.leftTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click(holder.leftTimeView);
            }
        });
        holder.rightTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click(holder.rightTimeView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEveryDayData.size();
    }

    public void setEveryDayData(HashMap<Integer, HashSet<TaskBean>> everyDayData) {
        this.mEveryDayData = everyDayData;
        notifyDataSetChanged();
    }

    private void click(TimeSelectView timeView) {
        NameDialog nameDialog = new NameDialog(mContext, R.style.dialog, timeView.getClickTaskBean());
        nameDialog.setOnDlgCloseListener(new NameDialog.onDlgCloseListener() {
            @Override
            public void onClose() {
                timeView.refreshName();
            }
        });
        nameDialog.show();
    }

    static class TimeViewHolder extends RecyclerView.ViewHolder {

        TimeSelectView leftTimeView;
        TimeSelectView rightTimeView;
        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            leftTimeView = itemView.findViewById(R.id.vp_item_time_left);
            rightTimeView = itemView.findViewById(R.id.vp_item_time_right);
        }
    }

    public static final String TAG = "123";
}
