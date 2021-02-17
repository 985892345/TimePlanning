package com.ndhzs.timeplanning.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ndhzs.timeplanning.R;
import com.ndhzs.timeplanning.weight.DayView;

import java.util.List;

public class DayVpAdapter extends RecyclerView.Adapter<DayVpAdapter.DayViewHolder> {

    private List<String[][]> mDays;

    public DayVpAdapter(List<String[][]> days) {
        this.mDays = days;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager2_item_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        //holder.mDayView.setWeek(mDays.get(position));
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {

        DayView mDayView;
        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            mDayView = itemView.findViewById(R.id.vp_item_day);
        }
    }
}
