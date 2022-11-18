package com.smartsparrow.publication.job.data;

import java.util.Objects;
import java.util.UUID;

public class NotificationByJob {

    private UUID jobId;
    private UUID notificationId;

    public UUID getJobId() {
        return jobId;
    }

    public NotificationByJob setJobId(UUID jobId) {
        this.jobId = jobId;
        return this;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public NotificationByJob setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationByJob that = (NotificationByJob) o;
        return Objects.equals(jobId, that.jobId) && Objects.equals(notificationId, that.notificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, notificationId);
    }

    @Override
    public String toString() {
        return "NotificationByJob{" +
                "jobId=" + jobId +
                ", notificationId=" + notificationId +
                '}';
    }
}
