package com.founderlens.ai.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.founderlens.ai.R;
import com.founderlens.ai.adapters.InsightAdapter;
import com.founderlens.ai.api.ApiClient;
import com.founderlens.ai.models.Insight;
import com.founderlens.ai.models.InsightsResponse;
import com.founderlens.ai.utils.DemoDataProvider;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InsightsFragment extends Fragment {

    private RecyclerView rvInsights;
    private InsightAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private TabLayout tabLayout;
    private TextView tvEmptyState;
    private TextView tvRiskCount, tvOpportunityCount, tvTrendCount;

    private List<Insight> allInsights = new ArrayList<>();
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_insights, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvInsights = view.findViewById(R.id.rv_insights);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        tabLayout = view.findViewById(R.id.tabs);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        tvRiskCount = view.findViewById(R.id.tv_risk_count);
        tvOpportunityCount = view.findViewById(R.id.tv_opportunity_count);
        tvTrendCount = view.findViewById(R.id.tv_trend_count);

        rvInsights.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InsightAdapter(new ArrayList<>());
        rvInsights.setAdapter(adapter);

        setupTabs();

        swipeRefresh.setColorSchemeResources(R.color.color_primary, R.color.color_secondary);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.color_card);
        swipeRefresh.setOnRefreshListener(this::loadInsights);

        loadInsights();
    }

    private void setupTabs() {
        if (tabLayout == null) return;
        String[] tabs = {"All", "Risks", "Opportunities", "Trends", "Actions"};
        for (String t : tabs) tabLayout.addTab(tabLayout.newTab().setText(t));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentFilter = "all"; break;
                    case 1: currentFilter = "risk"; break;
                    case 2: currentFilter = "opportunity"; break;
                    case 3: currentFilter = "trend"; break;
                    case 4: currentFilter = "action"; break;
                }
                filterInsights();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadInsights() {
        ApiClient.getInstance().getApiService().getInsights()
                .enqueue(new Callback<InsightsResponse>() {
                    @Override
                    public void onResponse(Call<InsightsResponse> call, Response<InsightsResponse> response) {
                        if (!isAdded()) return;
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getInsights() != null) {
                            allInsights = response.body().getInsights();
                        } else {
                            allInsights = new ArrayList<>();
                            String currentUrl = ApiClient.getInstance().getBaseUrl();
                            android.widget.Toast.makeText(requireContext(), "Failed to load insights from " + currentUrl, android.widget.Toast.LENGTH_LONG).show();
                        }
                        updateCountBadges();
                        filterInsights();
                    }
                    @Override
                    public void onFailure(Call<InsightsResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        allInsights = new ArrayList<>();
                        String currentUrl = ApiClient.getInstance().getBaseUrl();
                        android.widget.Toast.makeText(requireContext(), "Backend connection failed to " + currentUrl, android.widget.Toast.LENGTH_LONG).show();
                        updateCountBadges();
                        filterInsights();
                    }
                });
    }

    private void updateCountBadges() {
        if (!isAdded()) return;
        long risks = allInsights.stream().filter(i -> "risk".equals(i.getType())).count();
        long opps = allInsights.stream().filter(i -> "opportunity".equals(i.getType())).count();
        long trends = allInsights.stream().filter(i -> "trend".equals(i.getType())).count();
        if (tvRiskCount != null) tvRiskCount.setText(String.valueOf(risks));
        if (tvOpportunityCount != null) tvOpportunityCount.setText(String.valueOf(opps));
        if (tvTrendCount != null) tvTrendCount.setText(String.valueOf(trends));
    }

    private void filterInsights() {
        if (!isAdded()) return;
        List<Insight> filtered;
        if ("all".equals(currentFilter)) {
            filtered = allInsights;
        } else {
            filtered = allInsights.stream()
                    .filter(i -> currentFilter.equals(i.getType()))
                    .collect(Collectors.toList());
        }
        if (filtered.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvInsights.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvInsights.setVisibility(View.VISIBLE);
            adapter.updateData(filtered);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadInsights();
        }
    }
}
