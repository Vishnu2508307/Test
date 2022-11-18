package com.smartsparrow.rtm.subscription.iam;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * This RTM producer produces an RTM event for a account provision
 */
public class IamAccountProvisionRTMProducer extends AbstractProducer<IamAccountProvisionRTMConsumable> {

    private IamAccountProvisionRTMConsumable iamAccountProvisionRTMConsumable;

    @Inject
    public IamAccountProvisionRTMProducer() {
    }

    public IamAccountProvisionRTMProducer buildAccountProvisionedRTMConsumable(RTMClientContext rtmClientContext, UUID accountSubscriptionId, UUID accountId) {
        this.iamAccountProvisionRTMConsumable = new IamAccountProvisionRTMConsumable(rtmClientContext, new IamAccountBroadcastMessage(accountSubscriptionId, accountId));
        return this;
    }

    @Override
    public IamAccountProvisionRTMConsumable getEventConsumable() {
        return iamAccountProvisionRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IamAccountProvisionRTMProducer that = (IamAccountProvisionRTMProducer) o;
        return Objects.equals(iamAccountProvisionRTMConsumable, that.iamAccountProvisionRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iamAccountProvisionRTMConsumable);
    }

    @Override
    public String toString() {
        return "IamAccountProvisionRTMProducer{" +
                "iamAccountProvisionRTMConsumable=" + iamAccountProvisionRTMConsumable +
                '}';
    }
}
