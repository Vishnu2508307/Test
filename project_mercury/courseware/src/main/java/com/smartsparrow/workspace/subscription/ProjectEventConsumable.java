package com.smartsparrow.workspace.subscription;

import com.smartsparrow.pubsub.data.AbstractConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This consumable describes a project event
 */
public class ProjectEventConsumable extends AbstractConsumable<ProjectBroadcastMessage> {

    private static final long serialVersionUID = 295288494941153316L;

    public ProjectEventConsumable(final ProjectBroadcastMessage content) {
        super(content);
    }

    @Override
    public String getName() {
        return String.format("workspace.project/%s/%s", content.getProjectId(), getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return String.format("workspace.project/%s", content.getProjectId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new ProjectRTMEvent();
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
