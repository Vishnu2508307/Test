package com.smartsparrow.rtm.subscription.project;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

public class ProjectEventRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = -7770741788520726604L;

    public interface ProjectEventRTMSubscriptionFactory {
        /**
         * Create a new instance of ProjectEventRTMSubscription with a given projectId
         *
         * @param projectId the projectId
         * @return the ProjectEventRTMSubscription created instance
         */
        ProjectEventRTMSubscription create(final UUID projectId);
    }

    private UUID projectId;

    /**
     * Provides the name of the ProjectEventRTMSubscription
     *
     * @param projectId the project id of this subscription is for
     * @return the subscription name
     */
    public static String NAME(final UUID projectId) {
        return String.format("workspace.project/%s", projectId);
    }

    @Inject
    public ProjectEventRTMSubscription(@Assisted final  UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return ProjectEventRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(projectId);
    }

    @Override
    public String getBroadcastType() {
        return "workspace.project.broadcast";
    }

    public UUID getProjectId() {
        return projectId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectEventRTMSubscription that = (ProjectEventRTMSubscription) o;
        return Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }

    @Override
    public String toString() {
        return "ProjectEventRTMSubscription{" +
                "projectId=" + projectId +
                '}';
    }
}
