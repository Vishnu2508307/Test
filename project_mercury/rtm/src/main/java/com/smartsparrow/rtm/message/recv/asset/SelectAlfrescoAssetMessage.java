package com.smartsparrow.rtm.message.recv.asset;

import com.smartsparrow.rtm.message.ReceivedMessage;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class SelectAlfrescoAssetMessage  extends ReceivedMessage {
    private String alfrescoNodeId;

    public String getAlfrescoNodeId() {
        return alfrescoNodeId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectAlfrescoAssetMessage that = (SelectAlfrescoAssetMessage) o;
        return Objects.equals(alfrescoNodeId, that.alfrescoNodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alfrescoNodeId);
    }

    @Override
    public String toString() {
        return "SelectAlfrescoAssetMessage{" +
                "alfrescoNodeId='" + alfrescoNodeId + '\'' +
                "} " + super.toString();
    }
}
