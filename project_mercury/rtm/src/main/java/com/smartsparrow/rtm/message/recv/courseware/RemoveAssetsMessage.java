package com.smartsparrow.rtm.message.recv.courseware;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class RemoveAssetsMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID elementId;
    private CoursewareElementType elementType;
    private List<String> assetURN;

    @Override
    public UUID getElementId() {
        return elementId;
    }

    @Override
    public CoursewareElementType getElementType() {
        return elementType;
    }

    public List<String> getAssetURN() {
        return assetURN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoveAssetsMessage that = (RemoveAssetsMessage) o;
        return Objects.equals(elementId, that.elementId) &&
                Objects.equals(elementType, that.elementType) &&
                Objects.equals(assetURN, that.assetURN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, elementType);
    }

    @Override
    public String toString() {
        return "RemoveAssetMessage{" +
                "elementId=" + elementId +
                ", elementType=" + elementType +
                ", assetURN=" + assetURN +
                "} " + super.toString();
    }
}
