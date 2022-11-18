package com.smartsparrow.rtm.subscription.courseware.message;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class DescriptiveChangeBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = 5399594195000900167L;

    private final String value;

    public DescriptiveChangeBroadcastMessage(UUID activityId, UUID elementId, CoursewareElementType elementType, String value) {
        super(activityId, elementId, elementType);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DescriptiveChangeBroadcastMessage that = (DescriptiveChangeBroadcastMessage) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public String toString() {
        return "ActivityBroadcastMessage{" +
                ", value=\"" + value + "\"" +
                "} " + super.toString();
    }
}
