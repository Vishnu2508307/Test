package com.smartsparrow.export.data;

import java.util.Objects;
import java.util.UUID;

public class ExportErrorNotification implements Notification{

    private UUID notificationId;
    private String errorMessage;
    private String cause;
    private UUID exportId;

    @Override
    public UUID getNotificationId() {
        return notificationId;
    }

    public ExportErrorNotification setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ExportErrorNotification setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getCause() {
        return cause;
    }

    public ExportErrorNotification setCause(String cause) {
        this.cause = cause;
        return this;
    }

    @Override
    public ExportStatus getStatus() {
        return ExportStatus.FAILED;
    }

    public UUID getExportId() {
        return exportId;
    }

    public ExportErrorNotification setExportId(UUID exportId) {
        this.exportId = exportId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportErrorNotification that = (ExportErrorNotification) o;
        return Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(cause, that.cause) &&
                Objects.equals(exportId, that.exportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, errorMessage, cause, exportId);
    }

    @Override
    public String toString() {
        return "ExportErrorNotification{" +
                "notificationId=" + notificationId +
                ", errorMessage='" + errorMessage + '\'' +
                ", cause='" + cause + '\'' +
                ", exportId=" + exportId +
                '}';
    }
}
