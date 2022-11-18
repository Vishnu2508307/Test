package com.smartsparrow.rtm.subscription;

import com.smartsparrow.rtm.ws.RTMClient;

import reactor.core.publisher.Mono;

/**
 * !!! This class has been deprecated and replaced by the {@link com.smartsparrow.rtm.subscription.data.RTMSubscription}
 */
@Deprecated
public interface Subscription<T> {

    /**
     * Unique identifier for this subscription. It is used to refer to subscription in more general and short way
     * then subscription name. Can be the same as name. Depends on implementation.
     */
    public String getId();

    /**
     * Get the name of this subscription. Should be as unique as required to allow for multiple subscriptions of
     * the same type. For example, lesson/1234 and lesson/4567
     *
     */
    public String getName();

    /**
     * Perform the subscription action.
     * @return Mono with the redis assigned listener id of the subscription
     */
    public Mono<Integer> subscribe(RTMClient rtmClient);

    /**
     * Perform an unsubscribe action. This is a terminal event.
     */
    public void unsubscribe(RTMClient rtmClient);

    /**
     * Implementations must return Class&lt;T&gt; .class reference
     * @return
     */
    public Class<T> getMessageType();

}
