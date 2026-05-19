package com.founderlens.ai.api;

import com.founderlens.ai.models.AnalysisRequest;
import com.founderlens.ai.models.AnalysisResult;
import com.founderlens.ai.models.DashboardStats;
import com.founderlens.ai.models.HealthResponse;
import com.founderlens.ai.models.InsightsResponse;
import com.founderlens.ai.models.SimulationRequest;
import com.founderlens.ai.models.SimulationResult;
import com.founderlens.ai.models.TraceResponse;
import com.founderlens.ai.models.UploadResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface ApiService {

    // Health
    @GET("health")
    Call<HealthResponse> healthCheck();

    // Dashboard
    @GET("api/v1/dashboard/stats")
    Call<DashboardStats> getDashboardStats();

    @POST("api/v1/dashboard/reset")
    Call<Void> resetDashboardData();

    // File Upload
    @Multipart
    @POST("api/v1/upload")
    Call<UploadResponse> uploadFile(@Part MultipartBody.Part file);

    // Analysis
    @POST("api/v1/analysis/start")
    Call<AnalysisResult> startAnalysis(@Body AnalysisRequest request);

    @POST("api/v1/analysis/baseline")
    Call<AnalysisResult> startBaselineAnalysis(@Body AnalysisRequest request);

    @GET("api/v1/analysis/{id}")
    Call<AnalysisResult> getAnalysisDetail(@Path("id") String id);

    // Insights
    @GET("api/v1/insights")
    Call<InsightsResponse> getInsights();

    // Simulation
    @POST("api/v1/simulation/run")
    Call<SimulationResult> runSimulation(@Body SimulationRequest request);

    // Trace
    @GET("api/v1/trace")
    Call<TraceResponse> getTrace();

    @GET("api/v1/trace/{analysis_id}")
    Call<TraceResponse> getTraceForAnalysis(@Path("analysis_id") String analysisId);
}
