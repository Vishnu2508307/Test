package com.smartsparrow.export.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.util.DateFormat;

public class ExportMetadata {

    private UUID exportId;
    private String startedAt;
    private final String completedAt;
    private Integer elementsExportedCount;
    private ExportType exportType;
    @JsonIgnore
    private final UUID completedId;
    private List<CoursewareElement> ancestry;
    private String metadata;

    /**
     * Creates an export metadata with a required completedId as a timeuuid
     * then sets the completedAt string
     *
     * @param completedId represents when the export was completed with a timeuuid
     */
    public ExportMetadata(final UUID completedId) {
        this.completedId = completedId;
        this.completedAt = DateFormat.asRFC1123(this.completedId);
    }

    public UUID getExportId() {
        return exportId;
    }

    public ExportMetadata setExportId(UUID exportId) {
        this.exportId = exportId;
        return this;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public ExportMetadata setStartedAt(String startedAt) {
        this.startedAt = startedAt;
        return this;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public Integer getElementsExportedCount() {
        return elementsExportedCount;
    }

    public ExportMetadata setElementsExportedCount(Integer elementsExportedCount) {
        this.elementsExportedCount = elementsExportedCount;
        return this;
    }

    public UUID getCompletedId() {
        return completedId;
    }

    public ExportType getExportType() {
        return exportType;
    }

    public ExportMetadata setExportType(ExportType exportType) {
        this.exportType = exportType;
        return this;
    }

    public List<CoursewareElement> getAncestry() {
        return ancestry;
    }

    public ExportMetadata setAncestry(List<CoursewareElement> ancestry) {
        this.ancestry = ancestry;
        return this;
    }

    public String getMetadata() {
        return metadata;
    }

    public ExportMetadata setMetadata(final String metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportMetadata that = (ExportMetadata) o;
        return Objects.equals(exportId, that.exportId) &&
                Objects.equals(startedAt, that.startedAt) &&
                Objects.equals(completedAt, that.completedAt) &&
                Objects.equals(elementsExportedCount, that.elementsExportedCount) &&
                exportType == that.exportType &&
                Objects.equals(completedId, that.completedId) &&
                Objects.equals(ancestry, that.ancestry) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exportId, startedAt, completedAt, elementsExportedCount, exportType, completedId, ancestry,
                            metadata);
    }

    @Override
    public String toString() {
        return "ExportMetadata{" +
                "exportId=" + exportId +
                ", startedAt='" + startedAt + '\'' +
                ", completedAt='" + completedAt + '\'' +
                ", elementsExportedCount=" + elementsExportedCount +
                ", exportType=" + exportType +
                ", completedId=" + completedId +
                ", ancestry=" + ancestry +
                ", metadata=" + metadata +
                '}';
    }
}
