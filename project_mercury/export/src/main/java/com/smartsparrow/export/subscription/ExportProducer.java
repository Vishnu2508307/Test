package com.smartsparrow.export.subscription;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.ExportProgress;
import com.smartsparrow.pubsub.data.AbstractProducer;

public class ExportProducer extends AbstractProducer<ExportConsumable> {

    private ExportConsumable exportConsumable;

    @Inject
    public ExportProducer() {
    }

    public ExportProducer buildExportConsumable(UUID exportId,
                                                ExportProgress progress) {
        this.exportConsumable = new ExportConsumable(
                new ExportBroadcastMessage()
                        .setExportId(exportId)
                        .setExportProgress(progress));
        return this;
    }

    @Override
    public ExportConsumable getEventConsumable() {
        return exportConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportProducer that = (ExportProducer) o;
        return Objects.equals(exportConsumable, that.exportConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exportConsumable);
    }

    @Override
    public String toString() {
        return "ExportProducer{" +
                "exportConsumable=" + exportConsumable +
                '}';
    }
}
