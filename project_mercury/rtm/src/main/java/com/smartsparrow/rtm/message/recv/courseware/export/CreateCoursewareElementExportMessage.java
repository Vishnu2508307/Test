package com.smartsparrow.rtm.message.recv.courseware.export;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.export.data.ExportType;
import com.smartsparrow.rtm.message.ReceivedMessage;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateCoursewareElementExportMessage extends ReceivedMessage {

    private UUID elementId;
    private CoursewareElementType elementType;
    private ExportType exportType;
    private String metadata;

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareElementType getElementType() {
        return elementType;
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
        CreateCoursewareElementExportMessage that = (CreateCoursewareElementExportMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                exportType == that.exportType &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType, exportType, metadata);
    }

    @Override
    public String toString() {
        return "CreateCoursewareElementExportMessage{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", exportType=" + exportType +
                ", metadata=" + metadata +
                '}';
    }
}
