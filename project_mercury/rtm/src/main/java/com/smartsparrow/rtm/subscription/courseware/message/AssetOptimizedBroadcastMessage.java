package com.smartsparrow.rtm.subscription.courseware.message;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class AssetOptimizedBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = -1460363991628560839L;

    private final UUID assetId;
    private final String assetUrl;

    public AssetOptimizedBroadcastMessage(UUID activityId,
                                          UUID elementId,
                                          CoursewareElementType elementType,
                                          UUID assetId,
                                          String assetUrl) {
        super(activityId, elementId, elementType);
        this.assetId = assetId;
        this.assetUrl = assetUrl;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public String getAssetUrl() {
        return assetUrl;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetOptimizedBroadcastMessage that = (AssetOptimizedBroadcastMessage) o;
        return Objects.equals(assetId, that.assetId) &&
                Objects.equals(assetUrl, that.assetUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetId, assetUrl);
    }

    @Override
    public String toString() {
        return "AssetOptimizedBroadcastMessage{" +
                "assetId=" + assetId +
                ", assetUrl='" + assetUrl + "'" +
                '}';
    }
}
