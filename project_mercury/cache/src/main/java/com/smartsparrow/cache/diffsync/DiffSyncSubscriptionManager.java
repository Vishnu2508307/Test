package com.smartsparrow.cache.diffsync;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Singleton;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class DiffSyncSubscriptionManager {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DiffSyncSubscriptionManager.class);

    /**
     * Start a subscription.
     *
     * @param subscription the subscription to start
     * @return the unique subscription id what is generated
     */
    public Mono<Integer> add(final DiffSyncSubscription subscription) {
        checkNotNull(subscription, "Subscription can not be null");
        checkNotNull(subscription.getName(), "channel name can not be null");
        log.debug("adding subscription {}", subscription.getName());

        return subscription.subscribe(subscription.getDiffSyncIdentifier())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Stop a subscription
     *
     * @param subscription the subscription to unsubscribe
     */
    public void remove(DiffSyncSubscription subscription) {
        checkNotNull(subscription, "Subscription can not be null");
        // remove it from being tracked.
        log.debug("removing subscription {}", subscription.getName());
        subscription.unsubscribe(subscription.getDiffSyncEntity());
        log.debug("unsubscribed from {}", subscription.getName());
    }
}
