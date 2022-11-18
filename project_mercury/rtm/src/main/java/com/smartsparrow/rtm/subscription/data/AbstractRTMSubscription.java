package com.smartsparrow.rtm.subscription.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.listener.MessageListener;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractConsumable;
import com.smartsparrow.pubsub.data.EventConsumable;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Common behaviour for all RTM Subscription classes
 */
@SuppressWarnings("unchecked")
public abstract class AbstractRTMSubscription implements RTMSubscription, Serializable {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AbstractRTMSubscription.class);
    private static final long serialVersionUID = 573174064692945487L;

    private Integer listenerId = null;
    private RTopicReactive redisTopic = null;

    @SuppressWarnings("rawtypes")
    private MessageListener<EventConsumable> messageListener;
    private final UUID subscriptionId;

    @Inject
    private Map<Class<? extends RTMSubscription>, Collection<Provider<RTMConsumer<? extends EventConsumable<? extends BroadcastMessage>>>>> consumers;

    @Inject
    private RedissonReactiveClient redis;

    public AbstractRTMSubscription() {
        this.subscriptionId = UUIDs.timeBased();
    }

    public abstract Class<? extends RTMSubscription> getSubscriptionType();

    /**
     * Accessor for the injected Redisson client
     */
    protected RedissonReactiveClient getRedis() {
        return redis;
    }

    /**
     * Accessor for the injected RTM consumers that are bound to the subscription
     *
     * @return all the RTM consumers bound to the RTM subscription
     */
    protected Collection<Provider<RTMConsumer<? extends EventConsumable<? extends BroadcastMessage>>>> getConsumers() {
        return consumers.get(getSubscriptionType());
    }

    @Override
    public UUID getId() {
        return subscriptionId;
    }

    /**
     * Implements the message listener that is added as a listener to the topic. This method is invoked everytime
     * an event is sent to the subscription triggering a wide broadcast to all listening clients
     *
     * @param rtmClient the rtm client that will be listening to the subscription
     * @return the messge listener that will be invoked on a subscription wide broadcast
     */
    @SuppressWarnings("rawtypes")
    protected MessageListener<EventConsumable> messageListener(RTMClient rtmClient) {
        return (channel, msg) -> {
            try {
                final AbstractConsumable consumable = (AbstractConsumable) msg;
                RTMConsumer<EventConsumable<? extends BroadcastMessage>> triggeredConsumer = (RTMConsumer<EventConsumable<? extends BroadcastMessage>>) getConsumers().stream()
                        .map(Provider::get)
                        .filter(rtmConsumer -> rtmConsumer.getRTMEvent().equalsTo(msg.getRTMEvent()))
                        .findFirst()
                        .orElse(null);

                if (triggeredConsumer != null) {
                    // enrich the consumable to include the subscriptionId and the subscription broadcast type
                    consumable.setSubscriptionId(this.getId());
                    consumable.setBroadcastType(this.getBroadcastType());

                    triggeredConsumer.accept(rtmClient, consumable);
                } else {
                    // trying to fire a consumable for a consumer that is not wired, log it as a warning
                    log.jsonWarn("Consumer not found for RTM subscription", new HashMap<String, Object>(){
                        {put("subscriptionName", getName());}
                        {put("rtmEvent", msg.getRTMEvent());}
                    });
                }
            } catch (Throwable throwable) {
                // catch anything that fails on this message listener and log it
                log.jsonError("error invoking subscription message listener", new HashMap<String, Object>(){
                    {put("subscriptionName", msg.getSubscriptionName());}
                    {put("consumerName", msg.getName());}
                    {put("event", msg.getRTMEvent());}
                }, throwable);
            }
        };
    }

    /**
     * Allows an RTM client to subscribe to the subscription
     *
     * @param rtmClient the rtm client that is subscribing
     * @return a mono holding the subscription listener id
     */
    @Trace(async = true)
    @Override
    public Mono<Integer> subscribe(RTMClient rtmClient) {

        if (getName() == null) throw new RuntimeException("name is empty, use setName method");
        redisTopic = getRedis().getTopic(getName());

        this.messageListener = messageListener(rtmClient);
        Mono<Integer> listenerIdMono = redisTopic.addListener(EventConsumable.class, this.messageListener);

        return listenerIdMono.publishOn(Schedulers.elastic())
                .doOnNext(id -> listenerId = id)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Allows an RTM client to unsubscribe from the subscription
     *
     * @param rtmClient the rtm client to unsubscribe
     */
    @Override
    public void unsubscribe(RTMClient rtmClient) {
        redisTopic.removeListener(messageListener).publishOn(Schedulers.elastic()).subscribe();
    }
}
