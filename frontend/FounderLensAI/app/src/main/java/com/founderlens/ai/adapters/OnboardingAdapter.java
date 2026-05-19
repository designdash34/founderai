package com.founderlens.ai.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.founderlens.ai.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    private List<OnboardingPage> pages;

    public OnboardingAdapter(List<OnboardingPage> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_onboarding_page, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnboardingPage page = pages.get(position);
        if (holder.tvBadge != null) holder.tvBadge.setText("STEP " + page.getStepNumber());
        if (holder.tvTitle != null) holder.tvTitle.setText(page.getTitle());
        if (holder.tvDescription != null) holder.tvDescription.setText(page.getDescription());
        if (holder.tvPill1 != null) holder.tvPill1.setText(page.getPill1());
        if (holder.tvPill2 != null) holder.tvPill2.setText(page.getPill2());
    }

    @Override
    public int getItemCount() {
        return pages != null ? pages.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBadge, tvTitle, tvDescription, tvPill1, tvPill2;
        ViewHolder(View v) {
            super(v);
            tvBadge = v.findViewById(R.id.tv_badge);
            tvTitle = v.findViewById(R.id.tv_title);
            tvDescription = v.findViewById(R.id.tv_description);
            tvPill1 = v.findViewById(R.id.pill_1);
            tvPill2 = v.findViewById(R.id.pill_2);
        }
    }

    public static class OnboardingPage {
        private String stepNumber, title, description, pill1, pill2;
        public OnboardingPage(String step, String title, String desc, String pill1, String pill2) {
            this.stepNumber = step; this.title = title; this.description = desc;
            this.pill1 = pill1; this.pill2 = pill2;
        }
        public String getStepNumber() { return stepNumber; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getPill1() { return pill1; }
        public String getPill2() { return pill2; }
    }
}
