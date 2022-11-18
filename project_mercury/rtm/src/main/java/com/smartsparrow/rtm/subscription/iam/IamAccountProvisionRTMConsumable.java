package com.smartsparrow.rtm.subscription.iam;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This RTM consumable describes a iam account provision event
 */
public class IamAccountProvisionRTMConsumable extends AbstractRTMConsumable<IamAccountBroadcastMessage> {

    private static final long serialVersionUID = -1094967799691724827L;

    public IamAccountProvisionRTMConsumable(RTMClientContext rtmClientContext, IamAccountBroadcastMessage content) {
        super(rtmClientContext, content);
    }

    @Override
    public RTMClientContext getRTMClientContext() {
        return rtmClientContext;
    }

    @Override
    public String getName() {
        return String.format("iam.account.provision/subscription/%s/%s", content.accountSubscriptionId, getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return IamAccountProvisionRTMSubscription.NAME(content.getAccountSubscriptionId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new IamAccountProvisionRTMEvent();
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
