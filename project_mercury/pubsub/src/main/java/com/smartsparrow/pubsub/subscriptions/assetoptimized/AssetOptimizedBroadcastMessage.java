package com.smartsparrow.pubsub.subscriptions.assetoptimized;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

public class AssetOptimizedBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = -5932948747183708513L;
    private final UUID activityId;
    private final UUID elementId;
    private final Object elementType;
    private final UUID assetId;
    private final String assetUrl;

    public AssetOptimizedBroadcastMessage(UUID activityId,
                                          UUID elementId,
                                          Object elementType,
                                          UUID assetId,
                                          String assetUrl) {
        this.activityId = activityId;
        this.elementId = elementId;
        this.elementType = elementType;
        this.assetId = assetId;
        this.assetUrl = assetUrl;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public UUID getElementId() {
        return elementId;
    }

    public Object getElementType() {
        return elementType;
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
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(elementType, that.elementType) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(assetUrl, that.assetUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, elementId, elementType, assetId, assetUrl);
    }

    @Override
    public String toString() {
        return "AssetOptimizedBroadcastMessage{" +
                "activityId=" + activityId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", assetId=" + assetId +
                ", assetUrl='" + assetUrl + '\'' +
                '}';
    }
}
