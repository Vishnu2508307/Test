package com.smartsparrow.rtm.subscription.courseware.message;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.WalkableChild;

public class PathwayReOrderedBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = -905936343532372796L;

    private final List<WalkableChild> walkables;

    public PathwayReOrderedBroadcastMessage(UUID activityId, UUID elementId, List<WalkableChild> walkables) {
        super(activityId, elementId, PATHWAY);
        this.walkables = walkables;
    }

    public List<WalkableChild> getWalkables() {
        return walkables;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PathwayReOrderedBroadcastMessage that = (PathwayReOrderedBroadcastMessage) o;
        return Objects.equals(walkables, that.walkables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), walkables);
    }

    @Override
    public String toString() {
        return "PathwayReOrderedBroadcastMessage{" +
                "walkables=" + walkables +
                "} " + super.toString();
    }
}
