package com.smartsparrow.rtm.subscription.courseware.elementthemecreate;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.message.ThemeBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

/**
 * This RTM consumable describes an element theme create event
 */
public class ElementThemeCreateRTMConsumable extends AbstractRTMConsumable<ThemeBroadcastMessage> {

    private static final long serialVersionUID = 2227951064687640526L;

    public ElementThemeCreateRTMConsumable(RTMClientContext rtmClientContext, ThemeBroadcastMessage content) {
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
                             new ElementThemeCreateRTMEventDecoratorImpl(new ElementThemeCreateRTMEvent()).getName(
                                     content.getElementType()));
    }

    @Override
    public String getSubscriptionName() {
        return ActivityRTMSubscription.NAME(content.getActivityId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new ElementThemeCreateRTMEventDecoratorImpl(new ElementThemeCreateRTMEvent());
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
