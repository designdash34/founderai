package com.founderlens.ai.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class FileUtils {

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) result = cursor.getString(idx);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "Unknown file";
    }

    public static long getFileSize(Context context, Uri uri) {
        long size = -1;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (idx >= 0 && !cursor.isNull(idx)) size = cursor.getLong(idx);
                }
            }
        }
        return size;
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static String getMimeType(String fileName) {
        if (fileName == null) return "*/*";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".csv")) return "text/csv";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".txt")) return "text/plain";
        return "*/*";
    }

    public static java.io.File getFileFromUri(Context context, Uri uri) {
        try {
            String fileName = getFileName(context, uri);
            java.io.File tempFile = new java.io.File(context.getCacheDir(), fileName);
            try (java.io.InputStream input = context.getContentResolver().openInputStream(uri);
                 java.io.OutputStream output = new java.io.FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
                return tempFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
