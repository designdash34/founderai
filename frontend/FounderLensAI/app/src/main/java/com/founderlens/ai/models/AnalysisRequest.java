package com.founderlens.ai.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class AnalysisRequest {
    @SerializedName("business_problem") private String businessProblem;
    @SerializedName("website_url") private String websiteUrl;
    @SerializedName("use_demo_data") private boolean useDemoData;
    @SerializedName("input_types") private List<String> inputTypes;
    @SerializedName("pdf_path") private String pdfPath;
    @SerializedName("csv_path") private String csvPath;
    @SerializedName("baseline") private boolean baseline;

    public String getBusinessProblem() { return businessProblem; } public void setBusinessProblem(String b) { this.businessProblem = b; }
    public String getWebsiteUrl() { return websiteUrl; } public void setWebsiteUrl(String w) { this.websiteUrl = w; }
    public boolean isUseDemoData() { return useDemoData; } public void setUseDemoData(boolean u) { this.useDemoData = u; }
    public List<String> getInputTypes() { return inputTypes; } public void setInputTypes(List<String> i) { this.inputTypes = i; }
    public String getPdfPath() { return pdfPath; } public void setPdfPath(String p) { this.pdfPath = p; }
    public String getCsvPath() { return csvPath; } public void setCsvPath(String c) { this.csvPath = c; }
    public boolean isBaseline() { return baseline; } public void setBaseline(boolean b) { this.baseline = b; }
}
