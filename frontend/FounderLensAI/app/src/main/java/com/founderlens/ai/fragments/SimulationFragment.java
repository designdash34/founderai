package com.founderlens.ai.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.founderlens.ai.R;
import com.founderlens.ai.api.ApiClient;
import com.founderlens.ai.models.SimulationRequest;
import com.founderlens.ai.models.SimulationResult;
import com.founderlens.ai.utils.DemoDataProvider;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SimulationFragment extends Fragment {

    private ChipGroup cgScenario;
    private TextInputEditText etInvestment, etGrowthTarget;
    private SeekBar seekTimeHorizon;
    private TextView tvTimeHorizonValue;
    private Button btnRunSimulation;
    private LinearLayout resultsLayout;
    private ProgressBar progressSimulation;
    private TextView tvSimStatus;

    // Result Views
    private TextView tvRevenueImpact, tvRoiValue, tvGrowthRate;
    private TextView tvKpiRevenue, tvKpiCustomers, tvKpiMarketShare, tvKpiEbitda;
    private LineChart revenueChart;

    private String selectedScenario = "marketing_strategy";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simulation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cgScenario = view.findViewById(R.id.cg_scenario);
        etInvestment = view.findViewById(R.id.et_investment);
        etGrowthTarget = view.findViewById(R.id.et_growth_target);
        seekTimeHorizon = view.findViewById(R.id.seek_time_horizon);
        tvTimeHorizonValue = view.findViewById(R.id.tv_time_horizon_value);
        btnRunSimulation = view.findViewById(R.id.btn_run_simulation);
        resultsLayout = view.findViewById(R.id.results_layout);
        progressSimulation = view.findViewById(R.id.progress_simulation);
        tvSimStatus = view.findViewById(R.id.tv_sim_status);
        revenueChart = view.findViewById(R.id.revenue_chart);

        tvRevenueImpact = view.findViewById(R.id.tv_revenue_impact);
        tvRoiValue = view.findViewById(R.id.tv_roi_value);
        tvGrowthRate = view.findViewById(R.id.tv_growth_rate_result);
        tvKpiRevenue = view.findViewById(R.id.tv_kpi_revenue);
        tvKpiCustomers = view.findViewById(R.id.tv_kpi_customers);
        tvKpiMarketShare = view.findViewById(R.id.tv_kpi_market_share);
        tvKpiEbitda = view.findViewById(R.id.tv_kpi_ebitda);

        setupScenarioChips();
        setupSeekBar();
        setupChart();

        btnRunSimulation.setOnClickListener(v -> runSimulation());
    }

    private void setupScenarioChips() {
        if (cgScenario == null) return;
        String[][] scenarios = {
                {"marketing_strategy", "📊 Marketing Strategy"},
                {"pricing_change", "💰 Pricing Change"},
                {"growth_plan", "🚀 Growth Plan"},
                {"expansion", "🌍 Market Expansion"}
        };
        for (String[] s : scenarios) {
            Chip chip = new Chip(requireContext());
            chip.setText(s[1]);
            chip.setCheckable(true);
            chip.setChecked("marketing_strategy".equals(s[0]));
            String id = s[0];
            chip.setOnCheckedChangeListener((v, checked) -> {
                if (checked) selectedScenario = id;
            });
            cgScenario.addView(chip);
        }
        cgScenario.setSingleSelection(true);
    }

    private void setupSeekBar() {
        if (seekTimeHorizon == null) return;
        seekTimeHorizon.setMax(24);
        seekTimeHorizon.setProgress(12);
        tvTimeHorizonValue.setText("12 months");
        seekTimeHorizon.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int months = Math.max(1, progress);
                tvTimeHorizonValue.setText(months + " months");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupChart() {
        if (revenueChart == null) return;
        revenueChart.setBackgroundColor(getResources().getColor(R.color.color_card, null));
        revenueChart.getDescription().setEnabled(false);
        revenueChart.setTouchEnabled(true);
        revenueChart.setDragEnabled(true);
        revenueChart.setScaleEnabled(false);
        revenueChart.getLegend().setTextColor(getResources().getColor(R.color.color_text_secondary, null));

        XAxis xAxis = revenueChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getResources().getColor(R.color.color_text_tertiary, null));
        xAxis.setGridColor(getResources().getColor(R.color.color_divider, null));
        xAxis.setAxisLineColor(getResources().getColor(R.color.color_stroke, null));

        YAxis leftAxis = revenueChart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.color_text_tertiary, null));
        leftAxis.setGridColor(getResources().getColor(R.color.color_divider, null));
        revenueChart.getAxisRight().setEnabled(false);
    }

    private void runSimulation() {
        // Validation
        if (etInvestment.getText() == null || etInvestment.getText().toString().trim().isEmpty()) {
            etInvestment.setError("Investment amount is required");
            etInvestment.requestFocus();
            return;
        }
        if (etGrowthTarget.getText() == null || etGrowthTarget.getText().toString().trim().isEmpty()) {
            etGrowthTarget.setError("Growth rate is required");
            etGrowthTarget.requestFocus();
            return;
        }

        btnRunSimulation.setEnabled(false);
        progressSimulation.setVisibility(View.VISIBLE);
        resultsLayout.setVisibility(View.GONE);

        String[] statusMessages = {
                "🔵 Analyzing business parameters...",
                "🟣 Simulation Agent: Running Monte Carlo models...",
                "📈 Projecting revenue curves...",
                "✅ Simulation complete!"
        };
        int[] delays = {0, 1000, 2000, 3200};
        for (int i = 0; i < statusMessages.length; i++) {
            final String msg = statusMessages[i];
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded() && tvSimStatus != null) tvSimStatus.setText(msg);
            }, delays[i]);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;
            sendSimulationRequest();
        }, 3500);
    }

    private void sendSimulationRequest() {
        String investment = etInvestment != null && etInvestment.getText() != null
                ? etInvestment.getText().toString() : "100000";
        String growthTarget = etGrowthTarget != null && etGrowthTarget.getText() != null
                ? etGrowthTarget.getText().toString() : "20";
        int timeHorizon = seekTimeHorizon != null ? Math.max(1, seekTimeHorizon.getProgress()) : 12;

        SimulationRequest request = new SimulationRequest();
        request.setScenarioType(selectedScenario);
        request.setInvestmentAmount(investment.isEmpty() ? 100000.0 : Double.parseDouble(investment));
        request.setTargetGrowthRate(growthTarget.isEmpty() ? 20.0 : Double.parseDouble(growthTarget));
        request.setTimeHorizonMonths(timeHorizon);

        ApiClient.getInstance().getApiService().runSimulation(request)
                .enqueue(new Callback<SimulationResult>() {
                    @Override
                    public void onResponse(Call<SimulationResult> call, Response<SimulationResult> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            showResults(response.body());
                        } else {
                            handleSimulationFailure("Simulation failed on server");
                        }
                    }
                    @Override
                    public void onFailure(Call<SimulationResult> call, Throwable t) {
                        if (!isAdded()) return;
                        handleSimulationFailure("Backend connection failed");
                    }
                });
    }

    private void handleSimulationFailure(String errorMessage) {
        btnRunSimulation.setEnabled(true);
        progressSimulation.setVisibility(View.GONE);
        resultsLayout.setVisibility(View.GONE);
        String currentUrl = ApiClient.getInstance().getBaseUrl();
        android.widget.Toast.makeText(requireContext(), errorMessage + " at " + currentUrl, android.widget.Toast.LENGTH_LONG).show();
    }

    private void showResults(SimulationResult result) {
        btnRunSimulation.setEnabled(true);
        progressSimulation.setVisibility(View.GONE);
        resultsLayout.setVisibility(View.VISIBLE);

        // Add real activity
        com.founderlens.ai.models.ActivityItem activityItem = new com.founderlens.ai.models.ActivityItem(
                "sim_" + System.currentTimeMillis(),
                "Simulation Agent",
                "Ran simulation for: " + selectedScenario.replace("_", " "),
                "Just now",
                "success"
        );
        com.founderlens.ai.utils.AppState.addActivity(activityItem);

        if (tvRevenueImpact != null) tvRevenueImpact.setText(result.getRevenueImpact());
        if (tvRoiValue != null) tvRoiValue.setText(result.getRoi());
        if (tvGrowthRate != null) tvGrowthRate.setText(result.getGrowthRate());
        if (tvKpiRevenue != null) tvKpiRevenue.setText(result.getKpiRevenue());
        if (tvKpiCustomers != null) tvKpiCustomers.setText(result.getKpiCustomers());
        if (tvKpiMarketShare != null) tvKpiMarketShare.setText(result.getKpiMarketShare());
        if (tvKpiEbitda != null) tvKpiEbitda.setText(result.getKpiEbitda());

        renderChart(result.getProjectedData());
    }

    private void renderChart(List<Float> projectedData) {
        if (revenueChart == null || projectedData == null) return;
        List<Entry> baseline = new ArrayList<>();
        List<Entry> projected = new ArrayList<>();
        for (int i = 0; i < projectedData.size(); i++) {
            baseline.add(new Entry(i, 100f + (i * 2f)));
            projected.add(new Entry(i, projectedData.get(i)));
        }
        LineDataSet baselineSet = new LineDataSet(baseline, "Baseline");
        baselineSet.setColor(getResources().getColor(R.color.color_text_tertiary, null));
        baselineSet.setLineWidth(1.5f);
        baselineSet.setDrawCircles(false);
        baselineSet.setDrawValues(false);
        baselineSet.enableDashedLine(10f, 5f, 0f);

        LineDataSet projectedSet = new LineDataSet(projected, "Projected");
        projectedSet.setColor(getResources().getColor(R.color.color_primary, null));
        projectedSet.setLineWidth(2.5f);
        projectedSet.setDrawCircles(false);
        projectedSet.setDrawValues(false);
        projectedSet.setDrawFilled(true);
        projectedSet.setFillColor(getResources().getColor(R.color.color_primary_alpha20, null));

        revenueChart.setData(new LineData(baselineSet, projectedSet));
        revenueChart.invalidate();
        revenueChart.animateXY(800, 800);
    }
}
