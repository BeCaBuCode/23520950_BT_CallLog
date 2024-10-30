package com.example.broadcastreceiver;

public class ClassHistory {
    public static enum Status
    {
        REJECTED,
        FORWARDED,
        RECEIVE,
        MISSED
    }
    private String displayName;
    private String displayDate;
    private Status status;

    public ClassHistory(String displayName, String displayDate, Status status) {
        this.displayName = displayName;
        this.displayDate = displayDate;
        this.status = status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayDate() {
        return displayDate;
    }

    public void setDisplayDate(String displayDate) {
        this.displayDate = displayDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
