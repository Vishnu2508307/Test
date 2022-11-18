package com.smartsparrow.rtm.message.recv.workspace;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ElementChangeLogListMessage extends ReceivedMessage {

    private UUID elementId;
    private Integer limit;

    public UUID getElementId() {
        return elementId;
    }

    @Nullable
    public Integer getLimit() {
        return limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementChangeLogListMessage that = (ElementChangeLogListMessage) o;
        return Objects.equal(elementId, that.elementId) &&
                Objects.equal(limit, that.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(elementId, limit);
    }

    @Override
    public String toString() {
        return "ElementChangeLogListMessage{" +
                "elementId=" + elementId +
                ", limit=" + limit +
                "} " + super.toString();
    }
}
