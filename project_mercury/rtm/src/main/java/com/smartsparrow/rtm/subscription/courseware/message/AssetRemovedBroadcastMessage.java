package com.smartsparrow.rtm.subscription.courseware.message;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class AssetRemovedBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = 3309653475310524965L;

    public AssetRemovedBroadcastMessage(UUID activityId, UUID componentId, CoursewareElementType coursewareElementType) {
        super(activityId, componentId, coursewareElementType);
    }

    @Override
    public String toString() {
        return "AssetRemovedBroadcastMessage{} " + super.toString();
    }
}
