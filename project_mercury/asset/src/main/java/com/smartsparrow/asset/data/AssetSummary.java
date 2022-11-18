package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.asset.service.AssetUtils;

public class AssetSummary {

    private UUID id;
    private AssetProvider provider;
    private UUID ownerId;
    private UUID subscriptionId;
    private AssetMediaType mediaType;
    private String hash;
    private AssetVisibility visibility;
    private String urn;

    public UUID getId() {
        return id;
    }

    public AssetSummary setId(UUID id) {
        this.id = id;
        return this;
    }

    public AssetProvider getProvider() {
        return provider;
    }

    public AssetSummary setProvider(AssetProvider provider) {
        this.provider = provider;
        return this;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public AssetSummary setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public AssetSummary setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public AssetMediaType getMediaType() {
        return mediaType;
    }

    public AssetSummary setMediaType(AssetMediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    /**
     * Either returns the urn field or builds it dynamically. This is to ensure
     * backward compatibility. TODO update as part of BRNT-2977
     * @return the urn
     */
    public String getUrn() {
        if (urn == null) {
            return AssetUtils.buildURN(this);
        }
        return urn;
    }

    public AssetSummary setUrn(String urn) {
        this.urn = urn;
        return this;
    }

    /**
     * @return the uploaded asset file hash. For asset providers with value {@link AssetProvider#EXTERNAL} the hash
     * is always null
     */
    @Nullable
    public String getHash() {
        return hash;
    }

    public AssetSummary setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public AssetVisibility getVisibility() {
        return visibility;
    }

    public AssetSummary setVisibility(AssetVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetSummary summary = (AssetSummary) o;
        return Objects.equals(id, summary.id) &&
                provider == summary.provider &&
                Objects.equals(ownerId, summary.ownerId) &&
                Objects.equals(subscriptionId, summary.subscriptionId) &&
                mediaType == summary.mediaType &&
                Objects.equals(hash, summary.hash) &&
                visibility == summary.visibility &&
                Objects.equals(urn, summary.urn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, provider, ownerId, subscriptionId, mediaType, hash, visibility, urn);
    }

    @Override
    public String toString() {
        return "AssetSummary{" +
                "id=" + id +
                ", provider=" + provider +
                ", ownerId=" + ownerId +
                ", subscriptionId=" + subscriptionId +
                ", mediaType=" + mediaType +
                ", hash='" + hash + '\'' +
                ", visibility=" + visibility +
                ", urn='" + urn + '\'' +
                '}';
    }
}
