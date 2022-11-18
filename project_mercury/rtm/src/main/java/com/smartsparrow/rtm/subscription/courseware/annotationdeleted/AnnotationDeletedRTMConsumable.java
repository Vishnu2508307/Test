package com.smartsparrow.rtm.subscription.courseware.annotationdeleted;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.AnnotationBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes an annotation deleted event
 */
public class AnnotationDeletedRTMConsumable extends AbstractRTMConsumable<AnnotationBroadcastMessage> {

    private static final long serialVersionUID = 4219449738304140337L;

    public AnnotationDeletedRTMConsumable(RTMClientContext rtmClientContext, AnnotationBroadcastMessage content) {
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
                             new AnnotationDeletedRTMEventDecoratorImpl(new AnnotationDeletedRTMEvent()).getName(content.getElementType()));
    }

    @Override
    public String getSubscriptionName() {
        return ActivityRTMSubscription.NAME(content.getActivityId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new AnnotationDeletedRTMEventDecoratorImpl(new AnnotationDeletedRTMEvent());
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
