package com.founderlens.ai.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.founderlens.ai.R;

public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FounderLensPrefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final long SPLASH_DELAY = 2800L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Avoid starting splash again if the app is already running and brought to front from the launcher.
        if (!isTaskRoot() 
                && getIntent() != null 
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) 
                && Intent.ACTION_MAIN.equals(getIntent().getAction())) {
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        
        // Initialize ApiClient with SharedPreferences saved base URL
        com.founderlens.ai.api.ApiClient.init(getApplicationContext());
        
        setContentView(R.layout.activity_splash);

        View logoContainer = findViewById(R.id.logo_container);
        View glowCenter = findViewById(R.id.glow_center);
        ProgressBar progressLoading = findViewById(R.id.progress_loading);
        TextView tvVersion = findViewById(R.id.tv_version);

        // Animate glow
        if (glowCenter != null) {
            ObjectAnimator glowScale = ObjectAnimator.ofFloat(glowCenter, "scaleX", 0.5f, 1.2f);
            ObjectAnimator glowScaleY = ObjectAnimator.ofFloat(glowCenter, "scaleY", 0.5f, 1.2f);
            glowScale.setDuration(1200);
            glowScaleY.setDuration(1200);
            AnimatorSet glowSet = new AnimatorSet();
            glowSet.playTogether(glowScale, glowScaleY);
            glowSet.setInterpolator(new AccelerateDecelerateInterpolator());
            glowSet.start();
        }

        // Animate logo in
        if (logoContainer != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                logoContainer.animate().alpha(1f).translationY(0).setDuration(700)
                        .setStartDelay(0).start();
                logoContainer.setTranslationY(40f);
            }, 200);
        }

        // Animate progress bar
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (progressLoading != null) progressLoading.animate().alpha(1f).setDuration(400).start();
            if (tvVersion != null) tvVersion.animate().alpha(1f).setDuration(400).start();
        }, 900);

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNext, SPLASH_DELAY);
    }

    private void navigateToNext() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean onboardingDone = prefs.getBoolean(KEY_ONBOARDING_DONE, false);

        Intent intent;
        if (onboardingDone) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, OnboardingActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
