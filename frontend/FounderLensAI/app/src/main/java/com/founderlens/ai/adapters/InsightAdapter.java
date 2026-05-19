package com.founderlens.ai.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.founderlens.ai.R;
import com.founderlens.ai.models.Insight;

import java.util.List;

public class InsightAdapter extends RecyclerView.Adapter<InsightAdapter.ViewHolder> {

    private List<Insight> insights;
    private OnInsightClickListener listener;
    private final java.util.Set<Integer> expandedPositions = new java.util.HashSet<>();

    public interface OnInsightClickListener {
        void onClick(Insight insight);
    }

    public InsightAdapter(List<Insight> insights) {
        this.insights = insights;
    }

    public void setOnInsightClickListener(OnInsightClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_insight, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Insight insight = insights.get(position);

        holder.tvTitle.setText(insight.getTitle());
        holder.tvDescription.setText(insight.getDescription());
        holder.tvConfidence.setText(insight.getConfidence() + "% confidence");
        holder.tvAgent.setText(insight.getAgent());
        holder.tvImpact.setText(insight.getImpact() != null
                ? insight.getImpact().toUpperCase() : "MEDIUM");

        // Handle expand/collapse logic programmatically
        boolean isExpanded = expandedPositions.contains(position);
        if (isExpanded) {
            holder.tvDescription.setMaxLines(Integer.MAX_VALUE);
            holder.tvDescription.setEllipsize(null);
        } else {
            holder.tvDescription.setMaxLines(2);
            holder.tvDescription.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }

        // Type badge
        String type = insight.getType() != null ? insight.getType() : "insight";
        String typeLabel;
        int typeColor;
        switch (type) {
            case "risk": typeLabel = "⚠ RISK"; typeColor = Color.parseColor("#EF4444"); break;
            case "opportunity": typeLabel = "✦ OPPORTUNITY"; typeColor = Color.parseColor("#22C55E"); break;
            case "trend": typeLabel = "↗ TREND"; typeColor = Color.parseColor("#38DDF8"); break;
            case "action": typeLabel = "▶ ACTION"; typeColor = Color.parseColor("#4F8CFF"); break;
            default: typeLabel = "● INSIGHT"; typeColor = Color.parseColor("#A855F7"); break;
        }
        holder.tvTypeBadge.setText(typeLabel);
        holder.tvTypeBadge.setTextColor(typeColor);

        // Impact color
        String impact = insight.getImpact() != null ? insight.getImpact() : "medium";
        int impactColor;
        switch (impact.toLowerCase()) {
            case "high": impactColor = Color.parseColor("#EF4444"); break;
            case "medium": impactColor = Color.parseColor("#F59E0B"); break;
            default: impactColor = Color.parseColor("#22C55E"); break;
        }
        holder.tvImpact.setTextColor(impactColor);

        // Confidence bar
        if (holder.confidenceBar != null) {
            holder.confidenceBar.setProgress(insight.getConfidence());
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                if (expandedPositions.contains(pos)) {
                    expandedPositions.remove(pos);
                } else {
                    expandedPositions.add(pos);
                }
                
                // Gorgeous fluid accordion slide expand animation!
                if (holder.itemView.getParent() instanceof ViewGroup) {
                    androidx.transition.TransitionManager.beginDelayedTransition(
                            (ViewGroup) holder.itemView.getParent());
                }
                
                notifyItemChanged(pos);
                if (listener != null) listener.onClick(insight);
            }
        });
    }

    @Override
    public int getItemCount() {
        return insights != null ? insights.size() : 0;
    }

    public void updateData(List<Insight> newData) {
        this.insights = newData;
        expandedPositions.clear(); // Reset expanded states on refresh or filter change
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvConfidence, tvAgent, tvImpact, tvTypeBadge;
        android.widget.ProgressBar confidenceBar;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_insight_title);
            tvDescription = itemView.findViewById(R.id.tv_insight_description);
            tvConfidence = itemView.findViewById(R.id.tv_confidence);
            tvAgent = itemView.findViewById(R.id.tv_insight_agent);
            tvImpact = itemView.findViewById(R.id.tv_impact);
            tvTypeBadge = itemView.findViewById(R.id.tv_type_badge);
            confidenceBar = itemView.findViewById(R.id.confidence_bar);
        }
    }
}
