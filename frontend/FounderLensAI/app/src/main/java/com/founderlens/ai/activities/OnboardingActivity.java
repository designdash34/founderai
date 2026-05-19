package com.founderlens.ai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.founderlens.ai.R;
import com.founderlens.ai.adapters.OnboardingAdapter;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FounderLensPrefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";

    private ViewPager2 viewPager;
    private Button btnNext;
    private TextView tvSkip;
    private LinearLayout dotContainer;

    private int currentPage = 0;
    private final int totalPages = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewpager);
        btnNext = findViewById(R.id.btn_next);
        tvSkip = findViewById(R.id.tv_skip);
        dotContainer = findViewById(R.id.dot_container);

        setupViewPager();
        setupDots();
        setupListeners();
    }

    private void setupViewPager() {
        List<OnboardingAdapter.OnboardingPage> pages = new ArrayList<>();
        pages.add(new OnboardingAdapter.OnboardingPage(
                "01",
                getString(R.string.onboarding_1_title),
                getString(R.string.onboarding_1_desc),
                "6 AI Agents", "Autonomous"
        ));
        pages.add(new OnboardingAdapter.OnboardingPage(
                "02",
                getString(R.string.onboarding_2_title),
                getString(R.string.onboarding_2_desc),
                "Full Trace", "Transparent"
        ));
        pages.add(new OnboardingAdapter.OnboardingPage(
                "03",
                getString(R.string.onboarding_3_title),
                getString(R.string.onboarding_3_desc),
                "Simulate", "Predict"
        ));

        OnboardingAdapter adapter = new OnboardingAdapter(pages);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updateDots(position);
                updateButton(position);
            }
        });
    }

    private void setupDots() {
        for (int i = 0; i < totalPages; i++) {
            View dot = new View(this);
            int size = i == 0 ? dpToPx(24) : dpToPx(8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, dpToPx(8));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0 ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
            dotContainer.addView(dot);
        }
    }

    private void updateDots(int active) {
        for (int i = 0; i < dotContainer.getChildCount(); i++) {
            View dot = dotContainer.getChildAt(i);
            int size = i == active ? dpToPx(24) : dpToPx(8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, dpToPx(8));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == active ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
        }
    }

    private void updateButton(int page) {
        if (page == totalPages - 1) {
            btnNext.setText(getString(R.string.get_started));
            tvSkip.setVisibility(View.INVISIBLE);
        } else {
            btnNext.setText(getString(R.string.next));
            tvSkip.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                viewPager.setCurrentItem(currentPage + 1, true);
            } else {
                finishOnboarding();
            }
        });

        tvSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void finishOnboarding() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply();
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
