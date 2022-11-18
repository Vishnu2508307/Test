package com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate;

import com.smartsparrow.pubsub.data.AbstractConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This RTM consumable describes an activity alfresco assets updated event
 */
public class ActivityAlfrescoAssetsUpdateRTMConsumable extends AbstractConsumable<AlfrescoAssetsUpdateBroadcastMessage> {

    private static final long serialVersionUID = -6333034279922142799L;

    public ActivityAlfrescoAssetsUpdateRTMConsumable(AlfrescoAssetsUpdateBroadcastMessage content) {
        super(content);
    }

    @Override
    public String getName() {
        return String.format("author.activity/%s/%s", content.getActivityId(), getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return String.format("author.activity/%s", content.getActivityId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new ActivityAlfrescoAssetsUpdateRTMEvent();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
