package com.smartsparrow.rtm.subscription.courseware.message;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class AssetAddedBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = -7693647391332457514L;

    public AssetAddedBroadcastMessage(UUID activityId, UUID elementId, CoursewareElementType coursewareElementType) {
        super(activityId, elementId, coursewareElementType);
    }

    @Override
    public String toString() {
        return "AssetAddedBroadcastMessage{} " + super.toString();
    }
}
