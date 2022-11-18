package com.smartsparrow.rtm.subscription.courseware.evaluableset;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.EvaluableSetBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes an evaluable set event
 */
public class EvaluableSetRTMConsumable extends AbstractRTMConsumable<EvaluableSetBroadcastMessage> {

    private static final long serialVersionUID = 422773563927553361L;

    public EvaluableSetRTMConsumable(RTMClientContext rtmClientContext, EvaluableSetBroadcastMessage content) {
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
                             new EvaluableSetRTMEventDecoratorImpl(new EvaluableSetRTMEvent()).getName(content.getElementType()));
    }

    @Override
    public String getSubscriptionName() {
        return ActivityRTMSubscription.NAME(content.getActivityId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new EvaluableSetRTMEventDecoratorImpl(new EvaluableSetRTMEvent());
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
