package com.founderlens.ai.utils;

import com.founderlens.ai.models.ActivityItem;
import java.util.ArrayList;
import java.util.List;

public class AppState {
    private static boolean activityPerformed = false;
    private static List<ActivityItem> realActivities = new ArrayList<>();

    public static boolean hasPerformedActivity() {
        return activityPerformed;
    }

    public static void setActivityPerformed(boolean performed) {
        activityPerformed = performed;
    }

    public static List<ActivityItem> getRealActivities() {
        return realActivities;
    }

    public static void addActivity(ActivityItem activity) {
        realActivities.add(0, activity); // Add to top
        activityPerformed = true;
    }

    public static void clearActivities() {
        realActivities.clear();
        activityPerformed = false;
    }
}
