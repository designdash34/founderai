package com.founderlens.ai.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class UploadResponse {
    @SerializedName("file_id")
    private String fileId;

    @SerializedName("file_path")
    private String filePath;

    @SerializedName("filename")
    private String filename;

    @SerializedName("status")
    private String status;

    @SerializedName("parsed_preview")
    private Map<String, Object> parsedPreview;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getParsedPreview() {
        return parsedPreview;
    }

    public void setParsedPreview(Map<String, Object> parsedPreview) {
        this.parsedPreview = parsedPreview;
    }
}
