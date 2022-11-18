package com.smartsparrow.rtm.subscription.iam;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

/**
 * Defines a iam account provision RTM subscription
 */
public class IamAccountProvisionRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = 8204736412418508775L;

    public interface IamAccountProvisionRTMSubscriptionFactory {
        /**
         * Create a new instance of IamAccountProvisionRTMSubscription with a given accountSubscriptionId
         *
         * @param accountSubscriptionId the accountSubscriptionId
         * @return the IamAccountProvisionRTMSubscription created instance
         */
        IamAccountProvisionRTMSubscription create(final UUID accountSubscriptionId);
    }

    /**
     * Provides the name of the IamAccountProvisionRTMSubscription

     * @return the subscription name
     */
    public static String NAME(UUID accountSubscriptionId) {
        return String.format("iam.account.provision/subscription/%s", accountSubscriptionId);
    }

    private UUID accountSubscriptionId;

    @Inject
    public IamAccountProvisionRTMSubscription(@Assisted final UUID accountSubscriptionId) {
        this.accountSubscriptionId = accountSubscriptionId;
    }


    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return IamAccountProvisionRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(accountSubscriptionId);
    }

    @Override
    public String getBroadcastType() {
        return "iam.account.provision.broadcast";
    }

    public UUID getAccountSubscriptionId() {
        return accountSubscriptionId;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IamAccountProvisionRTMSubscription that = (IamAccountProvisionRTMSubscription) o;
        return Objects.equals(accountSubscriptionId, that.accountSubscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountSubscriptionId);
    }

    @Override
    public String toString() {
        return "IamAccountProvisionRTMSubscription{" +
                "accountSubscriptionId=" + accountSubscriptionId +
                '}';
    }
}
