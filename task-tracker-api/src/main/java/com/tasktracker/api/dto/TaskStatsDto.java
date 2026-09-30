package com.tasktracker.api.dto;

/**
 * DTO providing summary statistics for tracking daily tasks.
 */
public class TaskStatsDto {

    private long totalTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long completedTasks;
    private long cancelledTasks;
    private long overdueTasks;
    private double completionRate;

    public TaskStatsDto() {
    }

    public TaskStatsDto(long totalTasks, long pendingTasks, long inProgressTasks,
                        long completedTasks, long cancelledTasks, long overdueTasks) {
        this.totalTasks = totalTasks;
        this.pendingTasks = pendingTasks;
        this.inProgressTasks = inProgressTasks;
        this.completedTasks = completedTasks;
        this.cancelledTasks = cancelledTasks;
        this.overdueTasks = overdueTasks;
        this.completionRate = totalTasks > 0
                ? Math.round(((double) completedTasks / totalTasks) * 1000.0) / 10.0
                : 0.0;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public long getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(long pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public long getInProgressTasks() {
        return inProgressTasks;
    }

    public void setInProgressTasks(long inProgressTasks) {
        this.inProgressTasks = inProgressTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(long completedTasks) {
        this.completedTasks = completedTasks;
    }

    public long getCancelledTasks() {
        return cancelledTasks;
    }

    public void setCancelledTasks(long cancelledTasks) {
        this.cancelledTasks = cancelledTasks;
    }

    public long getOverdueTasks() {
        return overdueTasks;
    }

    public void setOverdueTasks(long overdueTasks) {
        this.overdueTasks = overdueTasks;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }
}
