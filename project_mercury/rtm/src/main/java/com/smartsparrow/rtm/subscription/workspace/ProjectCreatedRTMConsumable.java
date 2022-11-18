package com.smartsparrow.rtm.subscription.workspace;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This RTM consumable describes a project created event
 */
public class ProjectCreatedRTMConsumable extends AbstractRTMConsumable<ProjectCreatedBroadcastMessage> {

    private static final long serialVersionUID = -1084967799691724826L;

    public ProjectCreatedRTMConsumable(RTMClientContext rtmClientContext, ProjectCreatedBroadcastMessage content) {
        super(rtmClientContext, content);
    }

    @Override
    public RTMClientContext getRTMClientContext() {
        return rtmClientContext;
    }

    @Override
    public String getName() {
        return String.format("workspace/%s/%s", content.workspaceId, getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return WorkspaceRTMSubscription.NAME(content.workspaceId);
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new ProjectCreatedRTMEvent();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
