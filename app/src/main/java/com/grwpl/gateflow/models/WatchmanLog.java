package com.grwpl.gateflow.models;

import java.io.Serializable;

/**
 * Watchman Log model class for exit/entry tracking
 */
public class WatchmanLog implements Serializable {
    private String logId;
    private String applicationId;
    private String studentId;
    private String studentName;
    private String action; // exit or entry
    private long timestamp;
    private String watchmanId;
    private String remarks;

    public WatchmanLog() {}

    public WatchmanLog(String applicationId, String studentId, String action) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.action = action;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getWatchmanId() {
        return watchmanId;
    }

    public void setWatchmanId(String watchmanId) {
        this.watchmanId = watchmanId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
