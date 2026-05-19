package com.founderlens.ai.models;

import com.google.gson.annotations.SerializedName;

public class Agent {
    @SerializedName("id") private String id;
    @SerializedName("name") private String name;
    @SerializedName("description") private String description;
    @SerializedName("status") private String status; // idle, running, complete, error
    @SerializedName("icon_type") private String iconType;
    @SerializedName("color") private String color;
    @SerializedName("tasks_completed") private int tasksCompleted;
    @SerializedName("last_action") private String lastAction;

    public Agent() {}

    public Agent(String id, String name, String description, String status, String iconType, String color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.iconType = iconType;
        this.color = color;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getTasksCompleted() { return tasksCompleted; }
    public void setTasksCompleted(int tasksCompleted) { this.tasksCompleted = tasksCompleted; }
    public String getLastAction() { return lastAction; }
    public void setLastAction(String lastAction) { this.lastAction = lastAction; }
}
