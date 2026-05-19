package com.founderlens.ai.api;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static ApiClient instance;
    private Retrofit retrofit;
    private ApiService apiService;
    private String baseUrl;

    private static String getDefaultUrl() {
        if (android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || android.os.Build.PRODUCT.contains("sdk_google")
                || android.os.Build.PRODUCT.contains("google_sdk")
                || android.os.Build.PRODUCT.contains("sdk")
                || android.os.Build.PRODUCT.contains("emulator")) {
            return "http://10.0.2.2:8000/";
        }
        return "http://192.168.100.8:8000/";
    }

    private static final String DEFAULT_URL = getDefaultUrl();

    private ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        buildRetrofit();
    }

    private void buildRetrofit() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = new ApiClient(DEFAULT_URL);
                }
            }
        }
        return instance;
    }

    public static void init(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("FounderLensPrefs", Context.MODE_PRIVATE);
        String savedUrl = prefs.getString("base_url", null);
        
        // Auto-migrate legacy cached IP address if running on Emulator
        if (savedUrl == null || (savedUrl.equals("http://192.168.100.8:8000/") && getDefaultUrl().equals("http://10.0.2.2:8000/"))) {
            savedUrl = DEFAULT_URL;
            prefs.edit().putString("base_url", savedUrl).apply();
        }
        
        instance = new ApiClient(savedUrl);
    }

    public static void updateBaseUrl(String newUrl) {
        if (instance == null) {
            instance = new ApiClient(newUrl);
        } else {
            instance.baseUrl = newUrl.endsWith("/") ? newUrl : newUrl + "/";
            instance.buildRetrofit();
        }
    }

    public ApiService getApiService() {
        return apiService;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
