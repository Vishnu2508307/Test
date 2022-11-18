package com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;

/**
 * This RTM producer produces an RTM event for a newly updated activity alfresco assets
 */
public class ActivityAlfrescoAssetsUpdateRTMProducer extends AbstractProducer<ActivityAlfrescoAssetsUpdateRTMConsumable> {

    private ActivityAlfrescoAssetsUpdateRTMConsumable activityAlfrescoAssetsUpdateRTMConsumable;

    @Inject
    public ActivityAlfrescoAssetsUpdateRTMProducer() {
    }

    public ActivityAlfrescoAssetsUpdateRTMProducer buildActivityAlfrescoAssetsUpdateRTMConsumable(
            UUID activityId,
            UUID elementId,
            Object elementType,
            UUID assetId,
            Object alfrescoSyncType,
            boolean isAlfrescoAssetUpdated,
            boolean isAlfrescoSyncComplete) {
        this.activityAlfrescoAssetsUpdateRTMConsumable =
                new ActivityAlfrescoAssetsUpdateRTMConsumable(new AlfrescoAssetsUpdateBroadcastMessage(activityId,
                                                                                                       elementId,
                                                                                                       elementType,
                                                                                                       assetId,
                                                                                                       alfrescoSyncType,
                                                                                                       isAlfrescoAssetUpdated,
                                                                                                       isAlfrescoSyncComplete));
        return this;
    }

    @Override
    public ActivityAlfrescoAssetsUpdateRTMConsumable getEventConsumable() {
        return activityAlfrescoAssetsUpdateRTMConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityAlfrescoAssetsUpdateRTMProducer that = (ActivityAlfrescoAssetsUpdateRTMProducer) o;
        return Objects.equals(activityAlfrescoAssetsUpdateRTMConsumable,
                              that.activityAlfrescoAssetsUpdateRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityAlfrescoAssetsUpdateRTMConsumable);
    }

    @Override
    public String toString() {
        return "ActivityAlfrescoAssetsUpdateRTMProducer{" +
                "activityAlfrescoAssetsUpdateRTMConsumable=" + activityAlfrescoAssetsUpdateRTMConsumable +
                '}';
    }
}
