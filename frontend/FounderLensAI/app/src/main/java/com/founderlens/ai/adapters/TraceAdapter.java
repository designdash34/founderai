package com.founderlens.ai.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.founderlens.ai.R;
import com.founderlens.ai.models.TraceEntry;

import java.util.List;

public class TraceAdapter extends RecyclerView.Adapter<TraceAdapter.ViewHolder> {

    private List<TraceEntry> entries;

    public TraceAdapter(List<TraceEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trace, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TraceEntry entry = entries.get(position);
        if (entry == null) return;

        holder.tvAgent.setText(entry.getAgent());
        holder.tvMessage.setText(entry.getMessage());
        holder.tvTimestamp.setText(entry.getTimestamp());
        if (holder.tvReasoning != null && entry.getReasoning() != null && !entry.getReasoning().isEmpty()) {
            holder.tvReasoning.setVisibility(View.VISIBLE);
            holder.tvReasoning.setText(entry.getReasoning());
        } else if (holder.tvReasoning != null) {
            holder.tvReasoning.setVisibility(View.GONE);
        }
        if (holder.tvDuration != null && entry.getDurationMs() > 0) {
            holder.tvDuration.setText(entry.getDurationMs() + "ms");
        }

        // Type badge
        String type = entry.getType() != null ? entry.getType() : "info";
        String typeLabel;
        int typeColor;
        switch (type) {
            case "decision": typeLabel = "⚡ DECISION"; typeColor = Color.parseColor("#4F8CFF"); break;
            case "agent_action": typeLabel = "▶ ACTION"; typeColor = Color.parseColor("#38DDF8"); break;
            case "conflict": typeLabel = "⚔ CONFLICT"; typeColor = Color.parseColor("#F59E0B"); break;
            case "error": typeLabel = "✕ ERROR"; typeColor = Color.parseColor("#EF4444"); break;
            case "recovery": typeLabel = "↺ RECOVERY"; typeColor = Color.parseColor("#22C55E"); break;
            default: typeLabel = "● LOG"; typeColor = Color.parseColor("#A855F7"); break;
        }
        holder.tvTypeBadge.setText(typeLabel);
        holder.tvTypeBadge.setTextColor(typeColor);

        // Left border indicator
        if (holder.typeBorder != null) {
            holder.typeBorder.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(typeColor));
        }
    }

    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    public void updateData(List<TraceEntry> newData) {
        this.entries = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAgent, tvMessage, tvTimestamp, tvTypeBadge, tvReasoning, tvDuration;
        View typeBorder;

        ViewHolder(View itemView) {
            super(itemView);
            tvAgent = itemView.findViewById(R.id.tv_trace_agent);
            tvMessage = itemView.findViewById(R.id.tv_trace_message);
            tvTimestamp = itemView.findViewById(R.id.tv_trace_timestamp);
            tvTypeBadge = itemView.findViewById(R.id.tv_trace_type);
            tvReasoning = itemView.findViewById(R.id.tv_trace_reasoning);
            tvDuration = itemView.findViewById(R.id.tv_trace_duration);
            typeBorder = itemView.findViewById(R.id.trace_type_border);
        }
    }
}
