package com.smartsparrow.rtm.subscription.courseware.message;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import java.util.UUID;

public class ComponentCreatedBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = 3342346783419414107L;

    public ComponentCreatedBroadcastMessage(UUID activityId, UUID componentId) {
        super(activityId, componentId, COMPONENT);
    }

    @Override
    public String toString() {
        return "ComponentCreatedBroadcastMessage{} " + super.toString();
    }
}
