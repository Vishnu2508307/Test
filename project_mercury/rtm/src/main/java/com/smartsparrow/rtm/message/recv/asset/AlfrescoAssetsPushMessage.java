package com.smartsparrow.rtm.message.recv.asset;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class AlfrescoAssetsPushMessage extends ActivityGenericMessage {

    private UUID alfrescoNodeId;

    public UUID getAlfrescoNodeId() {
        return alfrescoNodeId;
    }

    public AlfrescoAssetsPushMessage setAlfrescoNodeId(UUID alfrescoNodeId) {
        this.alfrescoNodeId = alfrescoNodeId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AlfrescoAssetsPushMessage that = (AlfrescoAssetsPushMessage) o;
        return Objects.equals(alfrescoNodeId, that.alfrescoNodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), alfrescoNodeId);
    }

    @Override
    public String toString() {
        return "AlfrescoAssetsPushMessage{" +
                "alfrescoNodeId=" + alfrescoNodeId +
                "} " + super.toString();
    }
}
