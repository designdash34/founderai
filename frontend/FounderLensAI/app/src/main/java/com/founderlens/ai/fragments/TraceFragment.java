package com.founderlens.ai.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.founderlens.ai.R;
import com.founderlens.ai.adapters.TraceAdapter;
import com.founderlens.ai.api.ApiClient;
import com.founderlens.ai.models.TraceEntry;
import com.founderlens.ai.models.TraceResponse;
import com.founderlens.ai.utils.DemoDataProvider;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TraceFragment extends Fragment {

    private RecyclerView rvTrace;
    private TraceAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private View traceEmptyState;
    private TextView tvTotalSteps, tvDuration, tvAgentCount;
    private ChipGroup cgFilter;

    private List<TraceEntry> allEntries = new ArrayList<>();
    private String currentFilter = "all";
    private Handler pollingHandler;
    private boolean isPolling = false;
    private View btnShowMore;
    private int visibleCount = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trace, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTrace = view.findViewById(R.id.rv_trace);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        traceEmptyState = view.findViewById(R.id.trace_empty_state);
        tvTotalSteps = view.findViewById(R.id.trace_total_steps);
        tvDuration = view.findViewById(R.id.trace_duration);
        tvAgentCount = view.findViewById(R.id.trace_agent_count);
        cgFilter = view.findViewById(R.id.cg_filter);

        rvTrace.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TraceAdapter(new ArrayList<>());
        rvTrace.setAdapter(adapter);

        setupFilters();

        swipeRefresh.setColorSchemeResources(R.color.color_secondary, R.color.color_primary);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.color_card);
        swipeRefresh.setOnRefreshListener(() -> {
            visibleCount = 10;
            loadTrace(true);
        }); // Manual refresh shows Toasts on error

        view.findViewById(R.id.btn_export_trace).setOnClickListener(v -> exportTrace());
        view.findViewById(R.id.btn_clear_trace).setOnClickListener(v -> clearTrace());

        btnShowMore = view.findViewById(R.id.btn_show_more);
        if (btnShowMore != null) {
            btnShowMore.setOnClickListener(v -> {
                visibleCount += 10;
                filterEntries();
            });
        }

        pollingHandler = new Handler(Looper.getMainLooper());
    }

    private void setupFilters() {
        if (cgFilter == null) return;
        String[][] filters = {
                {"all", "All Steps"},
                {"decision", "Decisions"},
                {"agent_action", "Agent Actions"},
                {"conflict", "Conflicts"},
                {"error", "Errors"},
                {"recovery", "Recovery"}
        };

        // Create high-contrast state lists for premium UX readability (resolves text visibility issues)
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_checked},  // selected
                new int[] {-android.R.attr.state_checked} // unselected
        };

        int[] bgColors = new int[] {
                Color.parseColor("#4F8CFF"), // checked: primary blue
                Color.parseColor("#1B2435")  // unchecked: dark card background
        };

        int[] textColors = new int[] {
                Color.parseColor("#FFFFFF"), // checked: crisp white
                Color.parseColor("#94A3B8")  // unchecked: readable grey
        };

        int[] strokeColors = new int[] {
                Color.parseColor("#4F8CFF"), // checked: primary blue
                Color.parseColor("#253048")  // unchecked: card stroke
        };

        ColorStateList bgStateList = new ColorStateList(states, bgColors);
        ColorStateList textStateList = new ColorStateList(states, textColors);
        ColorStateList strokeStateList = new ColorStateList(states, strokeColors);

        for (String[] f : filters) {
            if (getContext() == null) break;
            Chip chip = new Chip(getContext());
            chip.setText(f[1]);
            chip.setCheckable(true);
            chip.setChecked("all".equals(f[0]));
            
            // Set dynamic colors
            chip.setChipBackgroundColor(bgStateList);
            chip.setTextColor(textStateList);
            chip.setChipStrokeColor(strokeStateList);
            chip.setChipStrokeWidth(3f);
            
            String id = f[0];
            chip.setOnCheckedChangeListener((v, checked) -> {
                if (checked) { currentFilter = id; filterEntries(); }
            });
            cgFilter.addView(chip);
        }
        cgFilter.setSingleSelection(true);
    }

    private void loadTrace() {
        loadTrace(false); // Default does not spam toasts on automatic background cycles
    }

    private void loadTrace(boolean showToastOnError) {
        ApiClient.getInstance().getApiService().getTrace()
                .enqueue(new Callback<TraceResponse>() {
                    @Override
                    public void onResponse(Call<TraceResponse> call, Response<TraceResponse> response) {
                        if (!isAdded()) return;
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getEntries() != null) {
                            allEntries = response.body().getEntries();
                            updateStats(response.body());
                        } else {
                            allEntries = new ArrayList<>();
                            updateStatsEmpty();
                            if (showToastOnError) {
                                String currentUrl = ApiClient.getInstance().getBaseUrl();
                                if (getContext() != null) {
                                    android.widget.Toast.makeText(getContext(), "Failed to load trace from " + currentUrl, android.widget.Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        filterEntries();
                    }
                    @Override
                    public void onFailure(Call<TraceResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        allEntries = new ArrayList<>();
                        updateStatsEmpty();
                        if (showToastOnError) {
                            String currentUrl = ApiClient.getInstance().getBaseUrl();
                            if (getContext() != null) {
                                android.widget.Toast.makeText(getContext(), "Backend connection failed to " + currentUrl, android.widget.Toast.LENGTH_LONG).show();
                            }
                        }
                        filterEntries();
                    }
                });
    }

    private void updateStatsEmpty() {
        if (!isAdded()) return;
        if (tvTotalSteps != null) tvTotalSteps.setText("0");
        if (tvDuration != null) tvDuration.setText("N/A");
        if (tvAgentCount != null) tvAgentCount.setText("0");
    }

    private void updateStats(TraceResponse response) {
        if (!isAdded()) return;
        if (tvTotalSteps != null) tvTotalSteps.setText(String.valueOf(response.getTotalSteps()));
        if (tvDuration != null) tvDuration.setText(response.getDuration());
        if (tvAgentCount != null) tvAgentCount.setText(String.valueOf(response.getAgentsInvolved()));
    }

    private void updateDemoStats() {
        if (!isAdded()) return;
        if (tvTotalSteps != null) tvTotalSteps.setText(String.valueOf(allEntries.size()));
        if (tvDuration != null) tvDuration.setText("7.4s");
        if (tvAgentCount != null) tvAgentCount.setText("6");
    }

    private void filterEntries() {
        if (!isAdded()) return;
        List<TraceEntry> filtered;
        if ("all".equals(currentFilter)) {
            filtered = allEntries.stream()
                    .filter(e -> e != null)
                    .collect(Collectors.toList());
        } else {
            filtered = allEntries.stream()
                    .filter(e -> e != null && currentFilter.equals(e.getType()))
                    .collect(Collectors.toList());
        }
        if (filtered.isEmpty()) {
            traceEmptyState.setVisibility(View.VISIBLE);
            rvTrace.setVisibility(View.GONE);
            if (btnShowMore != null) btnShowMore.setVisibility(View.GONE);
        } else {
            traceEmptyState.setVisibility(View.GONE);
            rvTrace.setVisibility(View.VISIBLE);
            
            int totalSize = filtered.size();
            List<TraceEntry> sliced;
            if (totalSize <= visibleCount) {
                sliced = filtered;
                if (btnShowMore != null) btnShowMore.setVisibility(View.GONE);
            } else {
                sliced = new ArrayList<>(filtered.subList(0, visibleCount));
                if (btnShowMore != null) {
                    btnShowMore.setVisibility(View.VISIBLE);
                    int remaining = totalSize - visibleCount;
                    if (btnShowMore instanceof TextView) {
                        ((TextView) btnShowMore).setText("▼  LOAD MORE (" + Math.min(10, remaining) + " OF " + remaining + " LEFT)");
                    }
                }
            }
            adapter.updateData(sliced);
        }
    }

    private void exportTrace() {
        StringBuilder sb = new StringBuilder();
        sb.append("# FounderLens AI — Execution Trace\n\n");
        sb.append("## Antigravity Orchestrator Log\n\n");
        for (TraceEntry e : allEntries) {
            if (e == null) continue;
            sb.append(String.format("[%s] [%s] %s: %s\n",
                    e.getTimestamp(), e.getType(), e.getAgent(), e.getMessage()));
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "FounderLens AI — Execution Trace");
        startActivity(Intent.createChooser(shareIntent, "Export Trace"));
    }

    private void clearTrace() {
        ApiClient.getInstance().getApiService().resetDashboardData()
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!isAdded()) return;
                        allEntries.clear();
                        visibleCount = 10;
                        if (btnShowMore != null) btnShowMore.setVisibility(View.GONE);
                        adapter.updateData(new ArrayList<>());
                        traceEmptyState.setVisibility(View.VISIBLE);
                        rvTrace.setVisibility(View.GONE);
                        updateStatsEmpty();
                        com.founderlens.ai.utils.AppState.clearActivities();
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Database and execution trace successfully reset!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (!isAdded()) return;
                        // Local fallback clear if offline
                        allEntries.clear();
                        visibleCount = 10;
                        if (btnShowMore != null) btnShowMore.setVisibility(View.GONE);
                        adapter.updateData(new ArrayList<>());
                        traceEmptyState.setVisibility(View.VISIBLE);
                        rvTrace.setVisibility(View.GONE);
                        updateStatsEmpty();
                        com.founderlens.ai.utils.AppState.clearActivities();
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Trace cleared locally (server unreachable)", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            loadTrace(false); // Don't show toast spam on auto-polling background errors!
            if (isPolling && pollingHandler != null) {
                pollingHandler.postDelayed(this, 3000L); // Poll every 3 seconds
            }
        }
    };

    private void startPolling() {
        if (pollingHandler == null) {
            pollingHandler = new Handler(Looper.getMainLooper());
        }
        if (!isPolling) {
            isPolling = true;
            pollingHandler.post(pollingRunnable);
        }
    }

    private void stopPolling() {
        isPolling = false;
        if (pollingHandler != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            stopPolling();
        } else {
            startPolling();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible() && !isHidden()) {
            startPolling();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPolling();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopPolling();
    }
}
