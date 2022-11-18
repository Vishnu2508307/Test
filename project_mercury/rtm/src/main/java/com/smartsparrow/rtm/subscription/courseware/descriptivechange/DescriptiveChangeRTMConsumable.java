package com.smartsparrow.rtm.subscription.courseware.descriptivechange;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.DescriptiveChangeBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes a descriptive change event
 */
public class DescriptiveChangeRTMConsumable extends AbstractRTMConsumable<DescriptiveChangeBroadcastMessage> {

    private static final long serialVersionUID = -4366282929072601399L;

    public DescriptiveChangeRTMConsumable(RTMClientContext rtmClientContext, DescriptiveChangeBroadcastMessage content) {
        super(rtmClientContext, content);
    }

    @Override
    public RTMClientContext getRTMClientContext() {
        return rtmClientContext;
    }

    @Override
    public String getName() {
        return String.format("author.activity/%s/%s",
                             content.getActivityId(),
                             new DescriptiveChangeRTMEventDecoratorImpl(new DescriptiveChangeRTMEvent()).getName(content.getElementType()));
    }

    @Override
    public String getSubscriptionName() {
        return ActivityRTMSubscription.NAME(content.getActivityId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new DescriptiveChangeRTMEventDecoratorImpl(new DescriptiveChangeRTMEvent());
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
