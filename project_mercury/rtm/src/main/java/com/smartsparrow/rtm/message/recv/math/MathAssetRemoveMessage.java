package com.smartsparrow.rtm.message.recv.math;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class MathAssetRemoveMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID elementId;
    private CoursewareElementType elementType;
    private String assetUrn;

    @Override
    public UUID getElementId() {
        return elementId;
    }

    public MathAssetRemoveMessage setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }

    public MathAssetRemoveMessage setElementType(final CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public String getAssetUrn() {
        return assetUrn;
    }

    public MathAssetRemoveMessage setAssetUrn(final String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathAssetRemoveMessage that = (MathAssetRemoveMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(assetUrn, that.assetUrn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType, assetUrn);
    }

    @Override
    public String toString() {
        return "MathAssetRemoveMessage{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", assetUrn='" + assetUrn + '\'' +
                '}';
    }
}
