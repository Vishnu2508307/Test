package com.smartsparrow.publication.data;

import java.util.Objects;

public class EtextNotification {

    private String type;
    private String exportId;
    private String activityId;
    private String status;
    private String message;
    private String bookId;
    private String etextVersion;

    public String getType() {
        return type;
    }

    public EtextNotification setType(String type) {
        this.type = type;
        return this;
    }

    public String getExportId() {
        return exportId;
    }

    public EtextNotification setExportId(String exportId) {
        this.exportId = exportId;
        return this;
    }

    public String getActivityId() {
        return activityId;
    }

    public EtextNotification setActivityId(String activityId) {
        this.activityId = activityId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public EtextNotification setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public EtextNotification setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getBookId() {
        return bookId;
    }

    public EtextNotification setBookId(String bookId) {
        this.bookId = bookId;
        return this;
    }

    public String getEtextVersion() {
        return etextVersion;
    }

    public EtextNotification setEtextVersion(String etextVersion) {
        this.etextVersion = etextVersion;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EtextNotification that = (EtextNotification) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(exportId, that.exportId) &&
                Objects.equals(activityId, that.activityId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(message, that.message) &&
                Objects.equals(bookId, that.bookId) &&
                Objects.equals(etextVersion, that.etextVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, exportId, activityId, status, message, bookId, etextVersion);
    }

    @Override
    public String toString() {
        return "EtextNotification{" +
                "type=" + type +
                ", exportId=" + exportId +
                ", activityId=" + activityId +
                ", status=" + status +
                ", message=" + message +
                ", bookId=" + bookId +
                ", etextVersion=" + etextVersion +
                '}';
    }
}
