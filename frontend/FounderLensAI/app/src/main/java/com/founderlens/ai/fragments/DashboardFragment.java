package com.founderlens.ai.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.founderlens.ai.R;
import com.founderlens.ai.activities.MainActivity;
import com.founderlens.ai.adapters.AgentAdapter;
import com.founderlens.ai.adapters.ActivityAdapter;
import com.founderlens.ai.api.ApiClient;
import com.founderlens.ai.models.Agent;
import com.founderlens.ai.models.ActivityItem;
import com.founderlens.ai.models.DashboardStats;
import com.founderlens.ai.utils.AppState;
import com.founderlens.ai.utils.DemoDataProvider;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private RecyclerView rvAgents, rvActivity;
    private TextView tvKpiAgents, tvKpiAnalyses, tvKpiInsights, tvKpiSuccess;
    private View emptyState;
    private AgentAdapter agentAdapter;
    private ActivityAdapter activityAdapter;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Views
        rvAgents = view.findViewById(R.id.rv_agents);
        rvActivity = view.findViewById(R.id.rv_activity);
        tvKpiAgents = view.findViewById(R.id.tv_kpi_agents);
        tvKpiAnalyses = view.findViewById(R.id.tv_kpi_analyses);
        tvKpiInsights = view.findViewById(R.id.tv_kpi_insights);
        tvKpiSuccess = view.findViewById(R.id.tv_kpi_success);
        emptyState = view.findViewById(R.id.empty_state);
        swipeRefresh = view.findViewById(R.id.swipe_refresh_dashboard);

        // Swipe Refresh Setup
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.color_secondary, R.color.color_primary);
            swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.color_card);
            swipeRefresh.setOnRefreshListener(this::loadDashboardData);
        }

        // Setup RecyclerViews
        rvAgents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAgents.setNestedScrollingEnabled(false);
        rvActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvActivity.setNestedScrollingEnabled(false);

        // Load agents
        List<Agent> agents = DemoDataProvider.getAgents();
        agentAdapter = new AgentAdapter(agents);
        rvAgents.setAdapter(agentAdapter);

        // Buttons
        view.findViewById(R.id.btn_start_analysis).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToUpload();
            }
        });

        TextView tvViewAll = view.findViewById(R.id.tv_view_all);
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTrace();
                }
            });
        }

        loadDashboardData();
    }

    private void loadDashboardData() {
        ApiClient.getInstance().getApiService().getDashboardStats()
                .enqueue(new Callback<DashboardStats>() {
                    @Override
                    public void onResponse(Call<DashboardStats> call, Response<DashboardStats> response) {
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            updateKPIs(response.body());
                            updateActivity(response.body().getRecentActivity());
                        } else {
                            handleConnectionError();
                        }
                    }

                    @Override
                    public void onFailure(Call<DashboardStats> call, Throwable t) {
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        handleConnectionError();
                    }
                });
    }

    private void handleConnectionError() {
        if (!isAdded()) return;
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
        String currentUrl = com.founderlens.ai.api.ApiClient.getInstance().getBaseUrl();
        android.widget.Toast.makeText(requireContext(), "Backend connection failed to " + currentUrl, android.widget.Toast.LENGTH_LONG).show();
        
        tvKpiAgents.setText("6/6"); // 6 agents nominal in offline/demo mode
        
        if (com.founderlens.ai.utils.AppState.hasPerformedActivity()) {
            List<ActivityItem> localActs = com.founderlens.ai.utils.AppState.getRealActivities();
            tvKpiAnalyses.setText(String.valueOf(localActs.size()));
            tvKpiInsights.setText(String.valueOf(localActs.size() * 5)); // safe estimation of insights found
            tvKpiSuccess.setText("98%");
            updateActivity(localActs);
        } else {
            tvKpiAnalyses.setText("0");
            tvKpiInsights.setText("0");
            tvKpiSuccess.setText("0%");
            updateActivity(new java.util.ArrayList<>());
        }
    }

    private void updateKPIs(DashboardStats stats) {
        if (!isAdded()) return;
        tvKpiAgents.setText(stats.getActiveAgents() + "/6");
        tvKpiAnalyses.setText(String.valueOf(stats.getAnalysesRun()));
        tvKpiInsights.setText(String.valueOf(stats.getInsightsFound()));
        tvKpiSuccess.setText(stats.getSuccessRate() + "%");

        // Update agent statuses
        if (agentAdapter != null && stats.getAgentStatuses() != null) {
            agentAdapter.updateStatuses(stats.getAgentStatuses());
        }
    }

    private void updateActivity(List<ActivityItem> items) {
        if (!isAdded()) return;
        if (items == null || items.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvActivity.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvActivity.setVisibility(View.VISIBLE);
            activityAdapter = new ActivityAdapter(items);
            rvActivity.setAdapter(activityAdapter);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadDashboardData();
        }
    }
}
