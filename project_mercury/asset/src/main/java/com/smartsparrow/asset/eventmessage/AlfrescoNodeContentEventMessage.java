package com.smartsparrow.asset.eventmessage;

import java.io.InputStream;
import java.util.Objects;

public class AlfrescoNodeContentEventMessage {

    private final String nodeId;
    private final String myCloudToken;
    private InputStream contentStream;

    public AlfrescoNodeContentEventMessage(final String nodeId, final String myCloudToken) {
        this.nodeId = nodeId;
        this.myCloudToken = myCloudToken;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getMyCloudToken() {
        return myCloudToken;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public AlfrescoNodeContentEventMessage setContentStream(InputStream is) {
        this.contentStream = is;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoNodeContentEventMessage that = (AlfrescoNodeContentEventMessage) o;
        return Objects.equals(nodeId, that.nodeId) &&
                Objects.equals(myCloudToken, that.myCloudToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, myCloudToken);
    }

    @Override
    public String toString() {
        return "AlfrescoNodeContentEventMessage{" +
                "nodeId='" + nodeId + '\'' +
                ", myCloudToken='" + myCloudToken + '\'' +
                '}';
    }
}

