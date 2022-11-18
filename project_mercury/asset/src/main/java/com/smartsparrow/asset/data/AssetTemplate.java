package com.smartsparrow.asset.data;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AssetTemplate {

    private InputStream inputStream;
    private final String originalFileName;
    private AssetVisibility visibility;
    private AssetProvider provider;
    private final String fileExtension;
    private UUID ownerId;
    private UUID subscriptionId;
    private Map<String, String> metadata;
    private String urn;

    public AssetTemplate(String originalFileName) {
        if (!originalFileName.contains(".")) {
            throw new UnsupportedOperationException("file name does not have extension");
        }
        this.originalFileName = originalFileName;
        this.fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public AssetTemplate setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public AssetVisibility getVisibility() {
        return visibility;
    }

    public AssetTemplate setVisibility(AssetVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public AssetProvider getProvider() {
        return provider;
    }

    public AssetTemplate setProvider(AssetProvider provider) {
        this.provider = provider;
        return this;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public AssetTemplate setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public AssetTemplate setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public AssetTemplate setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getUrn() {
        return urn;
    }

    public AssetTemplate setUrn(String urn) {
        this.urn = urn;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetTemplate that = (AssetTemplate) o;
        return Objects.equals(inputStream, that.inputStream) &&
                Objects.equals(originalFileName, that.originalFileName) &&
                visibility == that.visibility &&
                provider == that.provider &&
                Objects.equals(fileExtension, that.fileExtension) &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(urn, that.urn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputStream, originalFileName, visibility, provider, fileExtension, ownerId, subscriptionId,
                metadata, urn);
    }

    @Override
    public String toString() {
        return "AssetTemplate{" +
                "inputStream=" + inputStream +
                ", originalFileName='" + originalFileName + '\'' +
                ", visibility=" + visibility +
                ", provider=" + provider +
                ", fileExtension='" + fileExtension + '\'' +
                ", ownerId=" + ownerId +
                ", subscriptionId=" + subscriptionId +
                ", metadata=" + metadata +
                ", urn='" + urn + '\'' +
                '}';
    }
}
