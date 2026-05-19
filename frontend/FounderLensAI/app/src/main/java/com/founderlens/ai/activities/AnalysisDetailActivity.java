package com.founderlens.ai.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.founderlens.ai.R;
import com.founderlens.ai.adapters.InsightAdapter;
import com.founderlens.ai.api.ApiClient;
import com.founderlens.ai.models.AnalysisResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalysisDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ANALYSIS_ID = "analysis_id";

    private RecyclerView rvInsights;
    private TextView tvStatus, tvTimestamp, tvAgentsSummary;
    private View loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        rvInsights = findViewById(R.id.rv_insights);
        tvStatus = findViewById(R.id.tv_status);
        tvTimestamp = findViewById(R.id.tv_timestamp);
        tvAgentsSummary = findViewById(R.id.tv_agents_summary);
        loadingView = findViewById(R.id.loading_view);

        rvInsights.setLayoutManager(new LinearLayoutManager(this));

        String analysisId = getIntent().getStringExtra(EXTRA_ANALYSIS_ID);
        if (analysisId != null) {
            loadAnalysisDetail(analysisId);
        }
    }

    private void loadAnalysisDetail(String id) {
        loadingView.setVisibility(View.VISIBLE);
        ApiClient.getInstance().getApiService().getAnalysisDetail(id)
                .enqueue(new Callback<AnalysisResult>() {
                    @Override
                    public void onResponse(Call<AnalysisResult> call, Response<AnalysisResult> response) {
                        loadingView.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            bindData(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<AnalysisResult> call, Throwable t) {
                        loadingView.setVisibility(View.GONE);
                        Toast.makeText(AnalysisDetailActivity.this,
                                "Failed to load analysis", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindData(AnalysisResult result) {
        tvStatus.setText(result.getStatus());
        tvTimestamp.setText(result.getTimestamp());
        tvAgentsSummary.setText("Agents: " + result.getAgentsInvolved());
        if (result.getInsights() != null) {
            InsightAdapter adapter = new InsightAdapter(result.getInsights());
            rvInsights.setAdapter(adapter);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
