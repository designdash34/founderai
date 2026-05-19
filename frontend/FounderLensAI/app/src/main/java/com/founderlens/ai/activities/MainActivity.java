package com.founderlens.ai.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.founderlens.ai.R;
import com.founderlens.ai.fragments.DashboardFragment;
import com.founderlens.ai.fragments.InsightsFragment;
import com.founderlens.ai.fragments.SimulationFragment;
import com.founderlens.ai.fragments.TraceFragment;
import com.founderlens.ai.fragments.UploadFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import com.founderlens.ai.api.ApiClient;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Fragment currentFragment;
    
    private Fragment dashboardFragment;
    private Fragment uploadFragment;
    private Fragment insightsFragment;
    private Fragment simulationFragment;
    private Fragment traceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);
        setupSettingsAction();

        if (savedInstanceState != null) {
            // Restore fragments by their tags to prevent duplicates and keep state
            dashboardFragment = getSupportFragmentManager().findFragmentByTag("DASHBOARD");
            uploadFragment = getSupportFragmentManager().findFragmentByTag("UPLOAD");
            insightsFragment = getSupportFragmentManager().findFragmentByTag("INSIGHTS");
            simulationFragment = getSupportFragmentManager().findFragmentByTag("SIMULATION");
            traceFragment = getSupportFragmentManager().findFragmentByTag("TRACE");
            
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f != null && f.isVisible()) {
                    currentFragment = f;
                    break;
                }
            }
        }

        setupBottomNav();

        // Load dashboard by default
        if (savedInstanceState == null) {
            dashboardFragment = new DashboardFragment();
            switchFragment(dashboardFragment, "DASHBOARD");
        }
    }

    private void setupSettingsAction() {
        View profileIcon = findViewById(R.id.iv_profile);
        if (profileIcon != null) {
            profileIcon.setOnClickListener(v -> showSettingsDialog());
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server Configuration");
        builder.setMessage("Enter your backend IP address (e.g., http://192.168.100.8:8000/ for phone or http://10.0.2.2:8000/ for emulator):");

        final EditText input = new EditText(this);
        String currentUrl = ApiClient.getInstance().getBaseUrl();
        input.setText(currentUrl);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newUrl = input.getText().toString().trim();
            if (!newUrl.isEmpty()) {
                SharedPreferences prefs = getSharedPreferences("FounderLensPrefs", Context.MODE_PRIVATE);
                prefs.edit().putString("base_url", newUrl).apply();
                ApiClient.updateBaseUrl(newUrl);
                Toast.makeText(this, "Server IP updated! Restarting app connection.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment targetFragment = null;
            String tag = "";

            if (id == R.id.nav_dashboard) {
                if (dashboardFragment == null) dashboardFragment = new DashboardFragment();
                targetFragment = dashboardFragment;
                tag = "DASHBOARD";
            } else if (id == R.id.nav_upload) {
                if (uploadFragment == null) uploadFragment = new UploadFragment();
                targetFragment = uploadFragment;
                tag = "UPLOAD";
            } else if (id == R.id.nav_insights) {
                if (insightsFragment == null) insightsFragment = new InsightsFragment();
                targetFragment = insightsFragment;
                tag = "INSIGHTS";
            } else if (id == R.id.nav_simulation) {
                if (simulationFragment == null) simulationFragment = new SimulationFragment();
                targetFragment = simulationFragment;
                tag = "SIMULATION";
            } else if (id == R.id.nav_trace) {
                if (traceFragment == null) traceFragment = new TraceFragment();
                targetFragment = traceFragment;
                tag = "TRACE";
            }

            if (targetFragment != null) {
                switchFragment(targetFragment, tag);
            }
            return true;
        });
    }

    public void switchFragment(Fragment fragment, String tag) {
        if (currentFragment == fragment) return;

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        if (currentFragment != null) {
            tx.hide(currentFragment);
        }

        if (!fragment.isAdded()) {
            tx.add(R.id.fragment_container, fragment, tag);
        } else {
            tx.show(fragment);
        }

        currentFragment = fragment;
        tx.commit();
    }

    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        // Fallback method for legacy or custom dialogs
        currentFragment = fragment;
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
        tx.replace(R.id.fragment_container, fragment);
        if (addToBackStack) tx.addToBackStack(null);
        tx.commit();
    }

    public void navigateToUpload() {
        bottomNav.setSelectedItemId(R.id.nav_upload);
    }

    public void navigateToTrace() {
        bottomNav.setSelectedItemId(R.id.nav_trace);
    }

    public void navigateToInsights() {
        bottomNav.setSelectedItemId(R.id.nav_insights);
    }
}
