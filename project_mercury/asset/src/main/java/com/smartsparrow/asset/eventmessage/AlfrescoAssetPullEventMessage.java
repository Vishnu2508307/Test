package com.smartsparrow.asset.eventmessage;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.asset.data.AlfrescoNode;

public class AlfrescoAssetPullEventMessage {

    private UUID assetId;
    private String myCloudToken;
    private String alfrescoUrl;
    private InputStream inputStream;
    private AlfrescoNode alfrescoNode;
    private UUID referenceId;
    private Map<String, String> metadata;
    private boolean forceSync;
    private boolean requireUpdate;

    public UUID getAssetId() {
        return assetId;
    }

    public AlfrescoAssetPullEventMessage setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public boolean requireUpdate() {
        return requireUpdate;
    }

    public AlfrescoAssetPullEventMessage setRequireUpdate(boolean requireUpdate) {
        this.requireUpdate = requireUpdate;
        return this;
    }

    public boolean isForceSync() {
        return forceSync;
    }

    public AlfrescoAssetPullEventMessage setForceSync(boolean forceSync) {
        this.forceSync = forceSync;
        return this;
    }

    public String getMyCloudToken() {
        return myCloudToken;
    }

    public AlfrescoAssetPullEventMessage setMyCloudToken(String myCloudToken) {
        this.myCloudToken = myCloudToken;
        return this;
    }

    public String getAlfrescoUrl() {
        return alfrescoUrl;
    }

    public AlfrescoAssetPullEventMessage setAlfrescoUrl(String alfrescoUrl) {
        this.alfrescoUrl = alfrescoUrl;
        return this;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public AlfrescoAssetPullEventMessage setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public AlfrescoNode getAlfrescoNode() {
        return alfrescoNode;
    }

    public AlfrescoAssetPullEventMessage setAlfrescoNode(AlfrescoNode alfrescoNode) {
        this.alfrescoNode = alfrescoNode;
        return this;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public AlfrescoAssetPullEventMessage setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public AlfrescoAssetPullEventMessage setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoAssetPullEventMessage that = (AlfrescoAssetPullEventMessage) o;
        return forceSync == that.forceSync &&
                requireUpdate == that.requireUpdate &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(myCloudToken, that.myCloudToken) &&
                Objects.equals(alfrescoUrl, that.alfrescoUrl) &&
                Objects.equals(inputStream, that.inputStream) &&
                Objects.equals(alfrescoNode, that.alfrescoNode) &&
                Objects.equals(referenceId, that.referenceId) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, myCloudToken, alfrescoUrl, inputStream, alfrescoNode, referenceId, metadata,
                forceSync, requireUpdate);
    }

    @Override
    public String toString() {
        return "AlfrescoAssetPullEventMessage{" +
                "assetId=" + assetId +
                ", myCloudToken='" + myCloudToken + '\'' +
                ", alfrescoUrl='" + alfrescoUrl + '\'' +
                ", inputStream=" + inputStream +
                ", alfrescoNode=" + alfrescoNode +
                ", referenceId=" + referenceId +
                ", metadata=" + metadata +
                ", forceSync=" + forceSync +
                ", requireUpdate=" + requireUpdate +
                '}';
    }
}
