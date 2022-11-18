package com.smartsparrow.export.data;

import java.util.Objects;
import java.util.UUID;

public class ExportSummaryNotification  {

    private String type;
    private UUID activityId;
    private UUID exportId;
    private ExportStatus status;
    private String message;
    private String bookId;
    private String etextVersion;

    public String getType() {
        return type;
    }

    public ExportSummaryNotification setType(final String type) {
        this.type = type;
        return this;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public ExportSummaryNotification setActivityId(final UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    public UUID getExportId() {
        return exportId;
    }

    public ExportSummaryNotification setExportId(final UUID exportId) {
        this.exportId = exportId;
        return this;
    }

    public ExportStatus getStatus() {
        return status;
    }

    public ExportSummaryNotification setStatus(final ExportStatus status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ExportSummaryNotification setMessage(final String message) {
        this.message = message;
        return this;
    }

    public String getBookId() {
        return bookId;
    }

    public ExportSummaryNotification setBookId(String bookId) {
        this.bookId = bookId;
        return this;
    }

    public String getEtextVersion() {
        return etextVersion;
    }

    public ExportSummaryNotification setEtextVersion(String etextVersion) {
        this.etextVersion = etextVersion;
        return this;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportSummaryNotification that = (ExportSummaryNotification) o;
        return Objects.equals(activityId, that.activityId) &&
         Objects.equals(message, that.message) &&
         Objects.equals(type, that.type) &&
         Objects.equals(bookId, that.bookId) &&
         status == that.status &&
         Objects.equals(etextVersion, that.etextVersion) &&
         Objects.equals(exportId, that.exportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash( activityId, exportId, type, status, message);
    }

    @Override
    public String toString() {
        return "ExportSummaryNotification{" +
                ", type='" + type + '\'' +
                ", activityId=" + activityId +
                ", exportId=" + exportId +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
