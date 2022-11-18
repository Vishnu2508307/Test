package com.smartsparrow.asset.eventmessage;

import com.smartsparrow.asset.service.AlfrescoNodeInfo;

import java.util.Objects;

public class AlfrescoNodeInfoEventMessage {

    private final String nodeId;
    private final String myCloudToken;
    private AlfrescoNodeInfo alfrescoNodeInfo;

    public AlfrescoNodeInfoEventMessage(final String nodeId, final String myCloudToken) {
        this.nodeId = nodeId;
        this.myCloudToken = myCloudToken;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getMyCloudToken() {
        return myCloudToken;
    }

    public AlfrescoNodeInfo getAlfrescoNodeInfo() {
        return alfrescoNodeInfo;
    }

    public AlfrescoNodeInfoEventMessage setAlfrescoNodeInfo(AlfrescoNodeInfo alfrescoNodeInfo) {
        this.alfrescoNodeInfo = alfrescoNodeInfo;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoNodeInfoEventMessage that = (AlfrescoNodeInfoEventMessage) o;
        return Objects.equals(nodeId, that.nodeId) &&
                Objects.equals(myCloudToken, that.myCloudToken) &&
                Objects.equals(alfrescoNodeInfo, that.alfrescoNodeInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, myCloudToken, alfrescoNodeInfo);
    }

    @Override
    public String toString() {
        return "AlfrescoNodeInfoEventMessage{" +
                "nodeId='" + nodeId + '\'' +
                ", myCloudToken='" + myCloudToken + '\'' +
                ", alfrescoNodeInfo=" + alfrescoNodeInfo +
                '}';
    }
}

