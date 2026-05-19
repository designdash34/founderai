package com.founderlens.ai.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.founderlens.ai.R;
import com.founderlens.ai.models.Agent;

import java.util.List;
import java.util.Map;

public class AgentAdapter extends RecyclerView.Adapter<AgentAdapter.ViewHolder> {

    private final List<Agent> agents;
    private static final String[] EMOJIS = {"📥","🔍","⚖️","📋","📊","🛡️"};

    public AgentAdapter(List<Agent> agents) {
        this.agents = agents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_agent, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Agent agent = agents.get(position);
        holder.tvName.setText(agent.getName());
        holder.tvDesc.setText(agent.getDescription());
        if (position < EMOJIS.length) holder.tvEmoji.setText(EMOJIS[position]);

        String status = agent.getStatus() != null ? agent.getStatus() : "idle";
        holder.tvStatus.setText(capitalize(status));

        int dotColor, bgColor;
        switch (status) {
            case "running":
                dotColor = Color.parseColor("#4F8CFF");
                bgColor  = Color.parseColor("#334F8CFF");
                holder.tvStatus.setTextColor(Color.parseColor("#4F8CFF"));
                break;
            case "complete":
                dotColor = Color.parseColor("#22C55E");
                bgColor  = Color.parseColor("#3322C55E");
                holder.tvStatus.setTextColor(Color.parseColor("#22C55E"));
                break;
            case "error":
                dotColor = Color.parseColor("#EF4444");
                bgColor  = Color.parseColor("#33EF4444");
                holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
                break;
            default:
                dotColor = Color.parseColor("#64748B");
                bgColor  = Color.parseColor("#1F64748B");
                holder.tvStatus.setTextColor(Color.parseColor("#64748B"));
                break;
        }

        if (holder.statusDot != null) {
            holder.statusDot.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(dotColor));
        }
        if (holder.statusBadge != null) {
            holder.statusBadge.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(bgColor));
        }

        if (agent.getColor() != null && holder.iconBg != null) {
            try {
                int agentColor = Color.parseColor(agent.getColor());
                int alpha = (agentColor & 0x00FFFFFF) | 0x33000000;
                holder.iconBg.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(alpha));
            } catch (Exception ignored) {}
        }
    }

    @Override
    public int getItemCount() {
        return agents != null ? agents.size() : 0;
    }

    public void updateStatuses(Map<String, String> statuses) {
        for (Agent a : agents) {
            if (statuses.containsKey(a.getId())) a.setStatus(statuses.get(a.getId()));
        }
        notifyDataSetChanged();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvStatus, tvEmoji;
        View statusDot, statusBadge, iconBg;

        ViewHolder(View v) {
            super(v);
            tvName     = v.findViewById(R.id.tv_agent_name);
            tvDesc     = v.findViewById(R.id.tv_agent_desc);
            tvStatus   = v.findViewById(R.id.tv_agent_status);
            tvEmoji    = v.findViewById(R.id.tv_agent_emoji);
            statusDot  = v.findViewById(R.id.view_status_dot);
            statusBadge= v.findViewById(R.id.layout_status_badge);
            iconBg     = v.findViewById(R.id.view_agent_bg);
        }
    }
}
