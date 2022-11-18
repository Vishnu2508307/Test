package com.smartsparrow.export.data;


import com.smartsparrow.courseware.data.CoursewareElementType;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ExportAmbrosiaSnippet implements Serializable {


    private static final long serialVersionUID = -6343239228122826498L;

    private UUID exportId;
    private UUID notificationId;
    private UUID elementId;
    private UUID accountId;
    private CoursewareElementType elementType;
    private String ambrosiaSnippet;

    public UUID getExportId() {
        return exportId;
    }

    public ExportAmbrosiaSnippet setExportId(UUID exportId) {
        this.exportId = exportId;
        return this;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public ExportAmbrosiaSnippet setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public ExportAmbrosiaSnippet setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public ExportAmbrosiaSnippet setAccountId(UUID accountId) {
        this.accountId = accountId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public ExportAmbrosiaSnippet setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public String getAmbrosiaSnippet() {
        return ambrosiaSnippet;
    }

    public ExportAmbrosiaSnippet setAmbrosiaSnippet(String ambrosiaSnippet) {
        this.ambrosiaSnippet = ambrosiaSnippet;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportAmbrosiaSnippet that = (ExportAmbrosiaSnippet) o;
        return Objects.equals(exportId, that.exportId) &&
                Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(accountId, that.accountId) &&
                elementType == that.elementType &&
                Objects.equals(ambrosiaSnippet, that.ambrosiaSnippet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exportId, notificationId, elementId, accountId, elementType, ambrosiaSnippet);
    }

    @Override
    public String toString() {
        return "ExportAmbrosiaSnippet{" +
                "exportId=" + exportId +
                ", notificationId=" + notificationId +
                ", elementId=" + elementId +
                ", accountId=" + accountId +
                ", elementType=" + elementType +
                ", ambrosiaSnippet='" + ambrosiaSnippet + '\'' +
                '}';
    }
}
