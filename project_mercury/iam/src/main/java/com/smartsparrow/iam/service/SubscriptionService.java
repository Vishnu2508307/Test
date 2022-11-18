package com.smartsparrow.iam.service;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Preconditions;
import com.smartsparrow.iam.data.SubscriptionGateway;

import reactor.core.publisher.Flux;

/**
 * Service to manage subscriptions.
 */
@Singleton
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionGateway subscriptionGateway;

    @Inject
    public SubscriptionService(SubscriptionGateway subscriptionGateway) {
        this.subscriptionGateway = subscriptionGateway;
    }

    /**
     * Create a subscription.
     *
     * @param name the name of the subscription
     * @param region the data region
     * @return a created @{code Subscription}
     */
    public Subscription create(final String name, final Region region) {
        Preconditions.checkNotNull(region);
        //
        UUID id = UUIDs.timeBased();

        // build it.
        Subscription s = new Subscription().setId(id).setName(name).setIamRegion(region);
        // create it.
        if (log.isDebugEnabled()) {
            log.debug("creating subscription {}", s);
        }
        subscriptionGateway.persistBlocking(s);
        return s;
    }

    /**
     * Find a subscription by id.
     *
     * @param subscriptionIds the subscription ids to find.
     * @return an {@code Flux} {@code Subscription}, return order may not be the same as the supplied
     */
    public Flux<Subscription> find(final UUID... subscriptionIds) {
        return Flux.just(subscriptionIds)
                //
                .flatMap(subscriptionGateway::fetchSubscription);
    }

    /**
     * Change the region on the subscription. Note: This is not intended to change the accounts on the subscription.
     *
     * @param subscription the subscription to modify
     * @param toRegion the region to change to.
     */
    public void setRegion(final Subscription subscription, final Region toRegion) {
        Preconditions.checkArgument(subscription != null, "missing subscription");
        Preconditions.checkArgument(toRegion != null, "missing to region");
        Preconditions.checkArgument(!subscription.getIamRegion().equals(toRegion), "region already " + toRegion);

        Region prevRegion = subscription.getIamRegion();
        subscription.setIamRegion(toRegion);
        subscriptionGateway.persistBlocking(subscription);
        log.info("changed subscription {} from {} to {}", subscription.getId(), prevRegion, toRegion);
    }
}
