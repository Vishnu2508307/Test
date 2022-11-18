package com.smartsparrow.rtm.subscription.courseware.publication;

import com.smartsparrow.courseware.eventmessage.PublicationJobBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This RTM consumable describes a publication job event
 */
public class PublicationJobRTMConsumable extends AbstractRTMConsumable<PublicationJobBroadcastMessage> {

    private static final long serialVersionUID = -1084967799691724826L;

    public PublicationJobRTMConsumable(RTMClientContext rtmClientContext, PublicationJobBroadcastMessage content) {
        super(rtmClientContext, content);
    }

    @Override
    public RTMClientContext getRTMClientContext() {
        return rtmClientContext;
    }

    @Override
    public String getName() {
        return String.format("publication/%s/%s", content.getPublicationId(), getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return PublicationJobRTMSubscription.NAME(content.getPublicationId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new PublicationRTMEvent();
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
