package com.smartsparrow.workspace.subscription;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.pubsub.data.AbstractProducer;

public class ProjectEventProducer extends AbstractProducer<ProjectEventConsumable> {

    private ProjectEventConsumable projectEventConsumable;

    @Inject
    public ProjectEventProducer() {
    }

    public ProjectEventProducer buildProjectEventConsumable(UUID projectId,
                                                            UUID ingestionId,
                                                            IngestionStatus ingestionStatus) {
        this.projectEventConsumable = new ProjectEventConsumable(
                new ProjectBroadcastMessage()
                        .setProjectId(projectId)
                        .setIngestionId(ingestionId)
                        .setIngestionStatus(ingestionStatus));
        return this;
    }

    @Override
    public ProjectEventConsumable getEventConsumable() {
        return projectEventConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectEventProducer that = (ProjectEventProducer) o;
        return Objects.equals(projectEventConsumable, that.projectEventConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectEventConsumable);
    }

    @Override
    public String toString() {
        return "ProjectEventProducer{" +
                "projectEventConsumable=" + projectEventConsumable +
                '}';
    }
}
