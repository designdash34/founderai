package com.founderlens.ai.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.founderlens.ai.R;
import com.founderlens.ai.activities.MainActivity;
import com.founderlens.ai.api.ApiClient;
import com.founderlens.ai.models.AnalysisRequest;
import com.founderlens.ai.models.AnalysisResult;
import com.founderlens.ai.utils.AppState;
import com.founderlens.ai.utils.DemoDataProvider;
import com.founderlens.ai.utils.FileUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadFragment extends Fragment {

    private final List<Uri> selectedPdfs = new ArrayList<>();
    private final List<Uri> selectedCsvs = new ArrayList<>();
    private String websiteUrl = "";
    private boolean demoLoaded = false;
    private String uploadedPdfPath = null;
    private String uploadedCsvPath = null;

    // CrewAI Dynamic UX Animator State
    private final String[] loadingSteps = {
            "🔵 Intake Agent: Collecting and normalizing company data...",
            "🔵 Insight Agent: Scanning business files for critical trends...",
            "🔵 Insight Agent: Reviewing historical and local datasets...",
            "🟡 Conflict Agent: Auditing findings for conflicting metrics...",
            "🟡 Conflict Agent: Reconciling data inconsistencies...",
            "🟢 Action Planner: Framing strategic project milestones...",
            "🟢 Action Planner: Sequencing roadmap items for execution...",
            "🟣 Simulation Agent: Running business trajectory forecasts...",
            "🟣 Simulation Agent: Stress-testing growth & retention factors...",
            "🌀 Boardroom Orchestrator: Synthesizing courtroom reports..."
    };

    private int currentStepIndex = 0;
    private int currentProgressVal = 5;
    private Handler animationHandler;
    private boolean isApiCallRunning = false;

    private final Runnable statusAnimatorRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isApiCallRunning || !isAdded()) return;

            if (tvProcessingStatus != null && currentStepIndex < loadingSteps.length) {
                tvProcessingStatus.setText(loadingSteps[currentStepIndex]);
            }

            // Cycle step index
            currentStepIndex = (currentStepIndex + 1) % loadingSteps.length;

            // Increment progress bar slowly up to 95%
            if (progressBar != null) {
                if (currentProgressVal < 95) {
                    currentProgressVal += new java.util.Random().nextInt(5) + 3; // increment by 3-7%
                    if (currentProgressVal > 95) currentProgressVal = 95;
                    progressBar.setProgress(currentProgressVal);
                }
            }

            if (animationHandler != null && isApiCallRunning) {
                animationHandler.postDelayed(this, 3000L); // cycle message every 3 seconds
            }
        }
    };


    private TextView tvPdfCount, tvCsvCount, tvUrlStatus;
    private TextInputEditText etUrl, etBusinessProblem;
    private Button btnProcess;
    private LinearLayout processingLayout;
    private ProgressBar progressBar;
    private TextView tvProcessingStatus;
    private ChipGroup cgDataTypes;
    private View cardDemoData;
    private com.google.android.material.switchmaterial.SwitchMaterial switchBaseline;

    private final ActivityResultLauncher<Intent> pdfPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedPdfs.add(uri);
                        updatePdfCount();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> csvPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedCsvs.add(uri);
                        updateCsvCount();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvPdfCount = view.findViewById(R.id.tv_pdf_selected);
        tvCsvCount = view.findViewById(R.id.tv_csv_selected);
        tvUrlStatus = view.findViewById(R.id.tv_url_status);
        etUrl = view.findViewById(R.id.et_url);
        etBusinessProblem = view.findViewById(R.id.et_business_problem);
        btnProcess = view.findViewById(R.id.btn_process);
        processingLayout = view.findViewById(R.id.layout_processing);
        progressBar = view.findViewById(R.id.progress_bar);
        tvProcessingStatus = view.findViewById(R.id.tv_processing_status);
        cgDataTypes = view.findViewById(R.id.cg_data_types);
        cardDemoData = view.findViewById(R.id.btn_load_demo); // Using the button as a proxy or just leave it null
        switchBaseline = view.findViewById(R.id.switch_baseline);

        // File pickers
        view.findViewById(R.id.zone_pdf).setOnClickListener(v -> pickPdf());
        view.findViewById(R.id.zone_csv).setOnClickListener(v -> pickCsv());
        view.findViewById(R.id.btn_add_url).setOnClickListener(v -> addUrl());
        view.findViewById(R.id.btn_load_demo).setOnClickListener(v -> loadDemoData());

        btnProcess.setOnClickListener(v -> startProcessing());
    }

    private void pickPdf() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfPicker.launch(Intent.createChooser(intent, "Select PDF Report"));
    }

    private void pickCsv() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/json",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        csvPicker.launch(Intent.createChooser(intent, "Select CSV/JSON/Excel"));
    }

    private void addUrl() {
        if (etUrl != null && etUrl.getText() != null) {
            String url = etUrl.getText().toString().trim();
            if (!url.isEmpty()) {
                websiteUrl = url;
                tvUrlStatus.setVisibility(View.VISIBLE);
                tvUrlStatus.setText("✓ " + url);
                tvUrlStatus.setTextColor(getResources().getColor(R.color.color_success, null));
                addDataTypeChip("Website");
            }
        }
    }

    private void loadDemoData() {
        demoLoaded = true;
        selectedPdfs.clear();
        selectedCsvs.clear();

        // Simulate loading all 5 input types
        tvPdfCount.setText("✓ Demo Financial Report.pdf");
        tvPdfCount.setTextColor(getResources().getColor(R.color.color_success, null));
        tvCsvCount.setText("✓ Demo Sales Data.csv + metrics.json");
        tvCsvCount.setTextColor(getResources().getColor(R.color.color_success, null));
        tvUrlStatus.setText("✓ https://demo.founderlens.ai/data");
        tvUrlStatus.setTextColor(getResources().getColor(R.color.color_success, null));

        // Add all chip types
        if (cgDataTypes != null) {
            cgDataTypes.removeAllViews();
            String[] types = {"PDF Report", "CSV Data", "JSON Feed", "Website", "Real-time Feed"};
            for (String t : types) addDataTypeChip(t);
        }

        if (cardDemoData != null) cardDemoData.setVisibility(View.VISIBLE);
        Toast.makeText(requireContext(), "✅ Demo scenario loaded (5 input types)", Toast.LENGTH_SHORT).show();
    }

    private void addDataTypeChip(String label) {
        if (cgDataTypes == null) return;
        Chip chip = new Chip(requireContext());
        chip.setText(label);
        chip.setCheckable(false);
        chip.setTextColor(getResources().getColor(R.color.color_primary, null));
        chip.setChipBackgroundColorResource(R.color.color_primary_alpha20);
        chip.setChipStrokeColorResource(R.color.color_primary);
        chip.setChipStrokeWidth(1f);
        cgDataTypes.addView(chip);
    }

    private void updatePdfCount() {
        tvPdfCount.setText(selectedPdfs.size() + " PDF file(s) selected");
        tvPdfCount.setTextColor(getResources().getColor(R.color.color_success, null));
        addDataTypeChip("PDF Report");
    }

    private void updateCsvCount() {
        tvCsvCount.setText(selectedCsvs.size() + " data file(s) selected");
        tvCsvCount.setTextColor(getResources().getColor(R.color.color_success, null));
        addDataTypeChip("CSV/JSON");
    }

    private void startProcessing() {
        String problemText = etBusinessProblem != null && etBusinessProblem.getText() != null
                ? etBusinessProblem.getText().toString().trim() : "";

        boolean hasData = !problemText.isEmpty() || !selectedPdfs.isEmpty() || !selectedCsvs.isEmpty()
                || !websiteUrl.isEmpty() || demoLoaded;

        if (!hasData) {
            Toast.makeText(requireContext(),
                    "Please add at least one data source or load demo data", Toast.LENGTH_SHORT).show();
            return;
        }

        btnProcess.setEnabled(false);
        processingLayout.setVisibility(View.VISIBLE);
        if (switchBaseline != null && switchBaseline.isChecked()) {
            tvProcessingStatus.setText("⚖️ Running direct single-prompt LLM analyst baseline...");
        } else {
            tvProcessingStatus.setText("📤 Uploading files to backend...");
        }
        progressBar.setProgress(5);

        uploadedPdfPath = null;
        uploadedCsvPath = null;

        uploadPdfIfNeeded();
    }

    private void uploadPdfIfNeeded() {
        if (!selectedPdfs.isEmpty()) {
            Uri pdfUri = selectedPdfs.get(0);
            java.io.File file = FileUtils.getFileFromUri(requireContext(), pdfUri);
            if (file == null) {
                uploadCsvIfNeeded();
                return;
            }

            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                    file,
                    okhttp3.MediaType.parse(FileUtils.getMimeType(file.getName()))
            );
            okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            tvProcessingStatus.setText("📤 Uploading PDF: " + file.getName() + "...");
            ApiClient.getInstance().getApiService().uploadFile(body)
                    .enqueue(new Callback<com.founderlens.ai.models.UploadResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<com.founderlens.ai.models.UploadResponse> call,
                                               @NonNull Response<com.founderlens.ai.models.UploadResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                uploadedPdfPath = response.body().getFilePath();
                            }
                            uploadCsvIfNeeded();
                        }

                        @Override
                        public void onFailure(@NonNull Call<com.founderlens.ai.models.UploadResponse> call, @NonNull Throwable t) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "PDF Upload failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            uploadCsvIfNeeded();
                        }
                    });
        } else {
            uploadCsvIfNeeded();
        }
    }

    private void uploadCsvIfNeeded() {
        if (!selectedCsvs.isEmpty()) {
            Uri csvUri = selectedCsvs.get(0);
            java.io.File file = FileUtils.getFileFromUri(requireContext(), csvUri);
            if (file == null) {
                runAgentWorkflow();
                return;
            }

            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                    file,
                    okhttp3.MediaType.parse(FileUtils.getMimeType(file.getName()))
            );
            okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            tvProcessingStatus.setText("📤 Uploading Data: " + file.getName() + "...");
            ApiClient.getInstance().getApiService().uploadFile(body)
                    .enqueue(new Callback<com.founderlens.ai.models.UploadResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<com.founderlens.ai.models.UploadResponse> call,
                                               @NonNull Response<com.founderlens.ai.models.UploadResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                uploadedCsvPath = response.body().getFilePath();
                            }
                            runAgentWorkflow();
                        }

                        @Override
                        public void onFailure(@NonNull Call<com.founderlens.ai.models.UploadResponse> call, @NonNull Throwable t) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "CSV Upload failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            runAgentWorkflow();
                        }
                    });
        } else {
            runAgentWorkflow();
        }
    }

    private void runAgentWorkflow() {
        isApiCallRunning = true;
        currentStepIndex = 0;
        currentProgressVal = 8;

        if (progressBar != null) {
            progressBar.setProgress(currentProgressVal);
        }

        if (animationHandler == null) {
            animationHandler = new Handler(Looper.getMainLooper());
        }

        animationHandler.removeCallbacks(statusAnimatorRunnable);
        animationHandler.post(statusAnimatorRunnable);

        sendToApi();
    }

    private void sendToApi() {
        String problem = etBusinessProblem != null && etBusinessProblem.getText() != null
                ? etBusinessProblem.getText().toString() : "";

        AnalysisRequest request = new AnalysisRequest();
        request.setBusinessProblem(problem.isEmpty() ? "General business analysis" : problem);
        request.setWebsiteUrl(websiteUrl);
        request.setUseDemoData(demoLoaded || (uploadedPdfPath == null && uploadedCsvPath == null));
        request.setPdfPath(uploadedPdfPath);
        request.setCsvPath(uploadedCsvPath);
        request.setBaseline(switchBaseline != null && switchBaseline.isChecked());
        request.setInputTypes(new ArrayList<>());
        if (uploadedPdfPath != null) request.getInputTypes().add("pdf");
        if (uploadedCsvPath != null) request.getInputTypes().add("csv");
        if (!websiteUrl.isEmpty()) request.getInputTypes().add("website");
        if (demoLoaded) {
            request.getInputTypes().add("json");
            request.getInputTypes().add("realtime_feed");
        }

        boolean isBaseline = switchBaseline != null && switchBaseline.isChecked();
        Call<AnalysisResult> apiCall;
        if (isBaseline) {
            apiCall = ApiClient.getInstance().getApiService().startBaselineAnalysis(request);
        } else {
            apiCall = ApiClient.getInstance().getApiService().startAnalysis(request);
        }

        apiCall.enqueue(new Callback<AnalysisResult>() {
                    @Override
                    public void onResponse(@NonNull Call<AnalysisResult> call, @NonNull Response<AnalysisResult> response) {
                        if (!isAdded()) return;
                        finishProcessing(response.isSuccessful());
                    }
                    @Override
                    public void onFailure(@NonNull Call<AnalysisResult> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Analysis API error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        finishProcessing(false);
                    }
                });
    }

    private void finishProcessing(boolean success) {
        isApiCallRunning = false;
        if (animationHandler != null) {
            animationHandler.removeCallbacks(statusAnimatorRunnable);
        }

        btnProcess.setEnabled(true);
        if (success) {
            String problem = etBusinessProblem != null && etBusinessProblem.getText() != null
                    ? etBusinessProblem.getText().toString() : "General Analysis";
            
            com.founderlens.ai.models.ActivityItem item = new com.founderlens.ai.models.ActivityItem(
                    "act_" + System.currentTimeMillis(),
                    "System Agent",
                    "Performed analysis for: " + (problem.isEmpty() ? "General Analysis" : problem),
                    "Just now",
                    "success"
            );
            AppState.addActivity(item);

            if (tvProcessingStatus != null) {
                tvProcessingStatus.setText("✅ Analysis complete! View results in Insights tab.");
            }
            if (progressBar != null) {
                progressBar.setProgress(100);
            }
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToInsights();
                }
            }, 2000);
        } else {
            if (tvProcessingStatus != null) {
                tvProcessingStatus.setText("❌ Processing failed. Please retry.");
            }
            if (progressBar != null) {
                progressBar.setProgress(0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isApiCallRunning = false;
        if (animationHandler != null) {
            animationHandler.removeCallbacks(statusAnimatorRunnable);
        }
    }
}
