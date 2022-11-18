package com.smartsparrow.rtm.message.recv.courseware.export;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.export.data.ExportType;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateCoursewareRootElementExportMessage extends ReceivedMessage {

    private UUID elementId;
    private ExportType exportType;
    private String metadata;

    public UUID getElementId() {
        return elementId;
    }

    public ExportType getExportType() {
        return exportType;
    }

    public String getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateCoursewareRootElementExportMessage that = (CreateCoursewareRootElementExportMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                exportType == that.exportType &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, exportType, metadata);
    }

    @Override
    public String toString() {
        return "CreateCoursewareRootElementExportMessage{" +
                "elementId=" + elementId +
                ", exportType=" + exportType +
                ", metadata=" + metadata +
                '}';
    }
}
