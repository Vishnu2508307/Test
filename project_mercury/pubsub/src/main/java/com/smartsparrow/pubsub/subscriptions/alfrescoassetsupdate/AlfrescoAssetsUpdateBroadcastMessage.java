package com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

public class AlfrescoAssetsUpdateBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = -3548297891094048368L;
    private final UUID activityId;
    private final UUID elementId;
    private final Object elementType;
    private final UUID assetId;
    private final Object alfrescoSyncType;
    private final boolean isAlfrescoAssetUpdated;
    private final boolean isAlfrescoSyncComplete;


    public AlfrescoAssetsUpdateBroadcastMessage(final UUID activityId,
                                                final UUID elementId,
                                                final Object elementType,
                                                final UUID assetId,
                                                final Object alfrescoSyncType,
                                                final boolean isAlfrescoAssetUpdated,
                                                final boolean isAlfrescoSyncComplete) {
        this.activityId = activityId;
        this.elementId = elementId;
        this.elementType = elementType;
        this.assetId = assetId;
        this.alfrescoSyncType = alfrescoSyncType;
        this.isAlfrescoAssetUpdated = isAlfrescoAssetUpdated;
        this.isAlfrescoSyncComplete = isAlfrescoSyncComplete;
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

    public Object getAlfrescoSyncType() {
        return alfrescoSyncType;
    }

    public boolean isAlfrescoAssetUpdated() {
        return isAlfrescoAssetUpdated;
    }

    public boolean isAlfrescoSyncComplete() {
        return isAlfrescoSyncComplete;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoAssetsUpdateBroadcastMessage that = (AlfrescoAssetsUpdateBroadcastMessage) o;
        return isAlfrescoAssetUpdated == that.isAlfrescoAssetUpdated &&
                isAlfrescoSyncComplete == that.isAlfrescoSyncComplete &&
                Objects.equals(activityId, that.activityId) &&
                Objects.equals(elementId, that.elementId) &&
                Objects.equals(elementType, that.elementType) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(alfrescoSyncType, that.alfrescoSyncType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId,
                            elementId,
                            elementType,
                            assetId,
                            alfrescoSyncType,
                            isAlfrescoAssetUpdated,
                            isAlfrescoSyncComplete);
    }

    @Override
    public String toString() {
        return "AlfrescoAssetsUpdateBroadcastMessage{" +
                "activityId=" + activityId +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", assetId=" + assetId +
                ", alfrescoSyncType=" + alfrescoSyncType +
                ", isAlfrescoAssetUpdated=" + isAlfrescoAssetUpdated +
                ", isAlfrescoSyncComplete=" + isAlfrescoSyncComplete +
                '}';
    }
}
