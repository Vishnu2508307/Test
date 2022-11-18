package com.smartsparrow.rtm.subscription.courseware.message;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.UUID;

public class PathwayCreatedBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = -8194025131474099475L;

    public PathwayCreatedBroadcastMessage(UUID activityId, UUID pathwayId) {
        super(activityId, pathwayId, PATHWAY);
    }

    @Override
    public String toString() {
        return "PathwayCreatedBroadcastMessage{} " + super.toString();
    }
}
