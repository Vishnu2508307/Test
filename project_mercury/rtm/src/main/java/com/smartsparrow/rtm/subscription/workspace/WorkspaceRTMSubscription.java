package com.smartsparrow.rtm.subscription.workspace;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

/**
 * Defines a workspace RTM subscription
 */
public class WorkspaceRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = 8204736412418508775L;

    public interface WorkspaceRTMSubscriptionFactory {
        /**
         * Create a new instance of WorkspaceRTMSubscription with a given workspaceId
         *
         * @param workspaceId the workspaceId
         * @return the workspaceRTMSubscription created instance
         */
        WorkspaceRTMSubscription create(final UUID workspaceId);
    }

    /**
     * Provides the name of the WorkspaceRTMSubscription
     *
     * @param workspaceId the workspace id this subscription is for
     * @return the subscription name
     */
    public static String NAME(final UUID workspaceId) {
        return String.format("workspace/%s", workspaceId);
    }

    private UUID workspaceId;

    @Inject
    public WorkspaceRTMSubscription(@Assisted final UUID workspaceId) {
        this.workspaceId = workspaceId;
    }

    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return WorkspaceRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(workspaceId);
    }

    @Override
    public String getBroadcastType() {
        return "workspace.broadcast";
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceRTMSubscription that = (WorkspaceRTMSubscription) o;
        return Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId);
    }

    @Override
    public String toString() {
        return "WorkspaceRTMSubscription{" +
                "workspaceId=" + workspaceId +
                '}';
    }
}
