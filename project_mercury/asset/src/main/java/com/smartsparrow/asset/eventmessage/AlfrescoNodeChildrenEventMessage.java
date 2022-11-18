package com.smartsparrow.asset.eventmessage;

import com.smartsparrow.asset.service.AlfrescoNodeChildren;

import java.util.Objects;

public class AlfrescoNodeChildrenEventMessage {
    private final String nodeId;
    private final String myCloudToken;
    private AlfrescoNodeChildren alfrescoNodeChildren;
    private String errorMessage;

    public AlfrescoNodeChildrenEventMessage(final String nodeId, final String myCloudToken) {
        this.nodeId = nodeId;
        this.myCloudToken = myCloudToken;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getMyCloudToken() {
        return myCloudToken;
    }

    public AlfrescoNodeChildren getAlfrescoNodeChildren() {
        return alfrescoNodeChildren;
    }

    public AlfrescoNodeChildrenEventMessage setAlfrescoNodeChildren(AlfrescoNodeChildren alfrescoNodeChildren) {
        this.alfrescoNodeChildren = alfrescoNodeChildren;
        return this;
    }

    public String getErrorMessage() { return errorMessage; }

    public AlfrescoNodeChildrenEventMessage setErrorMessage(String error) {
        this.errorMessage = error;
        return this;
    }

    public boolean hasError() { return errorMessage != null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoNodeChildrenEventMessage that = (AlfrescoNodeChildrenEventMessage) o;
        return Objects.equals(nodeId, that.nodeId) &&
                Objects.equals(myCloudToken, that.myCloudToken) &&
                Objects.equals(alfrescoNodeChildren, that.alfrescoNodeChildren) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, myCloudToken, alfrescoNodeChildren);
    }

    @Override
    public String toString() {
        return "AlfrescoNodeChildrenEventMessage{" +
                "nodeId='" + nodeId + '\'' +
                ", myCloudToken='" + myCloudToken + '\'' +
                ", alfrescoNodeChildren=" + alfrescoNodeChildren +
                ", errorMessage=" + errorMessage +
                '}';
    }
}
