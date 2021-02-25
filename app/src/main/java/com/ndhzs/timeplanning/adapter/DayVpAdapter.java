package com.ndhzs.timeplanning.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.weekview.DayView;

import java.util.List;

public class DayVpAdapter extends RecyclerView.Adapter<DayVpAdapter.DayViewHolder> {

    private final List<String[]> mDates;
    private final List<String[]> mRectDays;
    private final List<String[]> mCalender;
    private final int mCurrentWeekPage;
    private final int mCurrentWeek;
    private int mWeekPosition;
    private OnWeekClickListener mListener;

    public DayVpAdapter(List<String[]> dates, List<String[]> rectDays, List<String[]> calender, int currentWeekPage, int currentWeek) {
        this.mDates = dates;
        this.mRectDays = rectDays;
        this.mCalender = calender;
        this.mCurrentWeek = currentWeek;
        this.mWeekPosition = currentWeek;
        this.mCurrentWeekPage = currentWeekPage;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager2_item_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.mDayView.setDate(mDates.get(position));
        holder.mDayView.setRectDays(mRectDays.get(position));
        holder.mDayView.setCalender(mCalender.get(position));
        if (position != mCurrentWeekPage) {
            holder.mDayView.setCirclePosition(-1);
        }else {
            holder.mDayView.setCirclePosition(mCurrentWeek);
        }
        if (mWeekPosition != mCurrentWeek) {
            holder.mDayView.setMovePosition(mWeekPosition);
        }
        holder.mDayView.setOnWeekClickListener(new DayView.OnWeekClickListener() {
            @Override
            public void onWeekClick(int p) {
                if (mListener != null) {
                    mListener.onWeekClick(position * 7 + p);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDates.size();
    }

    public void setRectDays(int itemCount) {
        notifyItemRangeChanged(mRectDays.size() - itemCount, itemCount);
    }
    public void setCalender(int itemCount) {
        notifyItemRangeChanged(mCalender.size() - itemCount, itemCount);
    }
    public void setWeekPosition(int weekPosition) {
        this.mWeekPosition = weekPosition;
        notifyDataSetChanged();
    }
    public void setOnWeekClickListener(OnWeekClickListener l) {
        this.mListener = l;
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {

        DayView mDayView;
        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            mDayView = itemView.findViewById(R.id.vp_item_day);
        }
    }

    public interface OnWeekClickListener {
        void onWeekClick(int position);
    }
}
