package com.smartsparrow.rtm.subscription.workspace;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * This RTM producer produces an RTM event for a newly create project
 */
public class ProjectCreatedRTMProducer extends AbstractProducer<ProjectCreatedRTMConsumable> {

    private ProjectCreatedRTMConsumable projectCreatedRTMConsumable;

    @Inject
    public ProjectCreatedRTMProducer() {
    }

    public ProjectCreatedRTMProducer buildProjectCreatedRTMConsumable(RTMClientContext rtmClientContext, UUID workspaceId, final UUID projectId) {
        this.projectCreatedRTMConsumable = new ProjectCreatedRTMConsumable(rtmClientContext, new ProjectCreatedBroadcastMessage(workspaceId, projectId));
        return this;
    }

    @Override
    public ProjectCreatedRTMConsumable getEventConsumable() {
        return projectCreatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectCreatedRTMProducer that = (ProjectCreatedRTMProducer) o;
        return Objects.equals(projectCreatedRTMConsumable, that.projectCreatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectCreatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ProjectCreatedRTMProducer{" +
                "projectCreatedRTMConsumable=" + projectCreatedRTMConsumable +
                '}';
    }
}
