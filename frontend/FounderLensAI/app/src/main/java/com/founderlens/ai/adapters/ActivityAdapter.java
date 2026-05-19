package com.founderlens.ai.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.founderlens.ai.R;
import com.founderlens.ai.models.ActivityItem;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private List<ActivityItem> items;

    public ActivityAdapter(List<ActivityItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityItem item = items.get(position);
        holder.tvAgent.setText(item.getAgent());
        holder.tvMessage.setText(item.getMessage());
        holder.tvTime.setText(item.getTimestamp());

        // Color timeline dot by type
        int dotColor;
        String type = item.getType() != null ? item.getType() : "";
        switch (type) {
            case "error": dotColor = Color.parseColor("#EF4444"); break;
            case "warning": dotColor = Color.parseColor("#F59E0B"); break;
            case "success": dotColor = Color.parseColor("#22C55E"); break;
            default: dotColor = Color.parseColor("#4F8CFF"); break;
        }
        holder.timelineDot.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(dotColor));

        // Hide line on last item
        if (holder.timelineLine != null) {
            holder.timelineLine.setVisibility(
                    position == items.size() - 1 ? View.INVISIBLE : View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateData(List<ActivityItem> newData) {
        this.items = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAgent, tvMessage, tvTime;
        View timelineDot, timelineLine;

        ViewHolder(View itemView) {
            super(itemView);
            tvAgent = itemView.findViewById(R.id.tv_activity_agent);
            tvMessage = itemView.findViewById(R.id.tv_activity_message);
            tvTime = itemView.findViewById(R.id.tv_activity_time);
            timelineDot = itemView.findViewById(R.id.timeline_dot);
            timelineLine = itemView.findViewById(R.id.timeline_line);
        }
    }
}
