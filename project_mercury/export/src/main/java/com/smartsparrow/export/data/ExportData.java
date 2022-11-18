package com.smartsparrow.export.data;

import java.util.Objects;
import java.util.UUID;

public class ExportData {

    private UUID exportId;
    private Integer elementsExportedCount;

    public UUID getExportId() {
        return exportId;
    }

    public ExportData setExportId(UUID exportId) {
        this.exportId = exportId;
        return this;
    }

    public Integer getElementsExportedCount() {
        return elementsExportedCount;
    }

    public ExportData setElementsExportedCount(Integer elementsExportedCount) {
        this.elementsExportedCount = elementsExportedCount;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportData that = (ExportData) o;
        return Objects.equals(exportId, that.exportId) &&
               Objects.equals(elementsExportedCount, that.elementsExportedCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exportId, elementsExportedCount);
    }

    @Override
    public String toString() {
        return "ExportData{" +
                "exportId=" + exportId +
                ", elementsExportedCount=" + elementsExportedCount +
                '}';
    }
}
