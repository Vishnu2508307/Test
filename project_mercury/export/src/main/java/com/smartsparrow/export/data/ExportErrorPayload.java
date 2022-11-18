package com.smartsparrow.export.data;

import java.lang.annotation.ElementType;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class ExportErrorPayload {

    private UUID notificationId;
    private String errorMessage;
    private String cause;
    private UUID exportId;
    private UUID elementId;
    private CoursewareElementType elementType;

    public UUID getNotificationId() {
        return notificationId;
    }

    public ExportErrorPayload setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public ExportErrorPayload setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public ExportErrorPayload setElementID(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ExportErrorPayload setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getCause() {
        return cause;
    }

    public ExportErrorPayload setCause(String cause) {
        this.cause = cause;
        return this;
    }



    public UUID getExportId() {
        return exportId;
    }

    public ExportErrorPayload setExportId(UUID exportId) {
        this.exportId = exportId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportErrorPayload that = (ExportErrorPayload) o;
        return Objects.equals(notificationId, that.notificationId) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(elementType, that.elementType) &&
                Objects.equals(cause, that.cause) &&
                Objects.equals(exportId, that.exportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, errorMessage, cause, exportId);
    }

    @Override
    public String toString() {
        return "ExportErrorPayload  {" +
                "notificationId=" + notificationId +
                ", errorMessage='" + errorMessage + '\'' +
                ", elementId='" + elementId + '\'' +
                ", elementType='" + elementType + '\'' +
                ", cause='" + cause + '\'' +
                ", exportId=" + exportId +
                '}';
    }
}
