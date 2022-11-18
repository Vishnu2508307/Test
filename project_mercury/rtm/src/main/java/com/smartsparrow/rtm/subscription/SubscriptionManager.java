package com.smartsparrow.rtm.subscription;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.rtm.wiring.RTMScoped;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import com.smartsparrow.util.monitoring.ReactiveTransaction;
import reactor.core.publisher.Mono;

/**
 * Manage subscriptions. Each subscription is responsible for any threads and lifecycles it requires.
 *
 * FIXME: Determine if this class should manage threads or the subscriptions should. Depending on how
 *        the pub/sub will work will probably drive the use-case here.
 *
 * Restrictions that are applied to client subscriptions:
 *  - Limit number of subscriptions to 10.
 *  - (TODO) Error when a duplicate subscription exists.
 * !!! This class has been deprecated in favour of the {@link com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager}
 */
@Deprecated
@RTMScoped
public class SubscriptionManager {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SubscriptionManager.class);
    private final int MAX_SUBSCRIPTIONS = 50;

    private ConcurrentHashMap<String, Subscription> subscriptions = new ConcurrentHashMap<>(MAX_SUBSCRIPTIONS);
    private final RTMClient rtmClient;

    @Inject
    public SubscriptionManager() {
        rtmClient = null;
    }

    public SubscriptionManager(RTMClient rtmClient) {
        this.rtmClient = rtmClient;
    }

    /**
     * Add and start a subscription.
     *
     * @param subscription the subscription to start
     * @return the unique subscription id what is generated
     * @throws SubscriptionLimitExceeded when the limit would exceed the MAX_SUBSCRIPTIONS
     * @throws SubscriptionAlreadyExists when the client was already subscribed to this event
     */
    @Trace(async = true)
    public Mono add(Subscription subscription) {
        checkNotNull(subscription, "Subscription can not be null");
        checkNotNull(subscription.getName(), "channel name can not be null");

        if (subscriptions.containsKey(subscription.getName())) {
            log.debug("Will not subscribe as already subscribed to {}", subscription.getName());
            return Mono.error(new SubscriptionAlreadyExists());
        }

        if (subscriptions.size() + 1 > MAX_SUBSCRIPTIONS) {
            log.debug("Can not subscribe to {} as it would exceed the max number of subscriptions {}", //
                    subscription.getName(), MAX_SUBSCRIPTIONS);
            return Mono.error(new SubscriptionLimitExceeded());
        }

        // track it.
        log.debug("add subscription {}", subscription.getName());
        subscriptions.put(subscription.getName(), subscription);

        // start it's subscription
        log.debug("starting subscribe on {}", subscription.getName());

        return subscription.subscribe(rtmClient)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Unsubscribe and stop a subscription
     *
     * @param subscription the subscription to unsubscribe
     */
    public void unsubscribe(Subscription subscription) {
        checkNotNull(subscription, "Subscription can not be null");
        // remove it from being tracked.
        log.debug("removing subscription {}", subscription.getName());
        subscription.unsubscribe(rtmClient);
        subscriptions.remove(subscription.getName());
        log.debug("unsubscribed from {}", subscription.getName());
    }

    /**
     * Unsubscribe and stop a subscription by name
     *
     * @param name the name of the subscription to unsubscribe from
     * @throws SubscriptionNotFound if subscription with name {@param name} is not found
     */
    public void unsubscribe(String name) throws SubscriptionNotFound {
        Subscription subscription = subscriptions.get(name);
        if (subscription == null) {
            throw new SubscriptionNotFound(name);
        }
        unsubscribe(subscription);
    }

    /**
     * Unsubscribe and stop all subscriptions.
     */
    public void unsubscribeAll() {
        for (Subscription subscription : subscriptions.values()) {
            unsubscribe(subscription);
        }
    }

    /**
     * Returns a subscription instance by name or null if it does not exist
     * @param name name of the susbciption to retrive
     * @return
     */
    public Subscription getSubscription(String name) {
        return subscriptions.get(name);
    }
}
