package com.smartsparrow.export.subscription;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.ExportProgress;
import com.smartsparrow.dataevent.BroadcastMessage;

public class ExportBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = -8058474287795464879L;

    private UUID exportId;
    private ExportProgress progress;

    public UUID getExportId() {
        return exportId;
    }

    public ExportBroadcastMessage setExportId(UUID exportId) {
        this.exportId = exportId;
        return this;
    }

    public ExportProgress getExportProgress() {
        return progress;
    }

    public ExportBroadcastMessage setExportProgress(ExportProgress exportProgress) {
        this.progress = exportProgress;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportBroadcastMessage that = (ExportBroadcastMessage) o;
        return Objects.equals(exportId, that.exportId) &&
                Objects.equals(progress, that.progress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exportId, progress);
    }

    @Override
    public String toString() {
        return "ExportBroadcastMessage{" +
                "exportId=" + exportId +
                ", progress= " + progress +
                '}';
    }
}
