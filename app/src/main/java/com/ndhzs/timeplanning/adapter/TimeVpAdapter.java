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

import java.util.HashSet;
import java.util.List;

public class TimeVpAdapter extends RecyclerView.Adapter<TimeVpAdapter.TimeViewHolder> {

    private final Context mContext;
    private final ViewPager2 mViewPager;
    private boolean mIsShowTopBottomTime = true;
    private boolean mIsShowDifferentTime = false;
    private HashSet<TaskBean> mTaskBeans;
    private List<HashSet<TaskBean>> mEveryDayData;

    public TimeVpAdapter(Context context, ViewPager2 vp, List<HashSet<TaskBean>> everyDayData) {
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
        processTimeView(holder.leftTimeView, holder.rightTimeView, position);
        processTimeView(holder.rightTimeView, holder.leftTimeView, position);
    }
    @Override
    public int getItemCount() {
        return mEveryDayData.size();
    }

    public void setEveryDayData(List<HashSet<TaskBean>> everyDayData) {
        this.mEveryDayData = everyDayData;
        notifyDataSetChanged();
    }
    public void setIsShowTopBottomTime(boolean is) {
        this.mIsShowTopBottomTime = is;
        notifyDataSetChanged();
    }
    public void setIsShowDifferentTime(boolean is) {
        this.mIsShowDifferentTime = is;
        notifyDataSetChanged();
    }

    private void processTimeView(TimeSelectView v1, TimeSelectView v2, int position) {
        v1.setLinkTimeSelectView(v2);
        v1.setLinkViewPager2(mViewPager);
        v1.setData(mEveryDayData.get(position));
        v1.setIsShowTopBottomTime(mIsShowTopBottomTime);
        v1.setIsShowDifferentTime(mIsShowDifferentTime);
        v1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click(v1);
            }
        });
        v1.setOnScrollViewListener(new TimeSelectView.OnScrollViewListener() {
            @Override
            public void onScrollChanged(int y) {
                v2.scrollTo(0, y);
            }
        });
        v1.setOnDataChangeListener(new TimeSelectView.OnDataChangeListener() {
            @Override
            public void onDataIncrease(TaskBean newData) {
                mEveryDayData.get(position).add(newData);
            }

            @Override
            public void onDataDelete(TaskBean deletedData) {
                mEveryDayData.get(position).remove(deletedData);
            }
        });
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
