package com.smartsparrow.pubsub.data;

import java.util.HashMap;

import javax.inject.Inject;

import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;

import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.scheduler.Schedulers;

/**
 * This class holds the common behaviour for an RTM producer. It implements produce and delegates the RTM consumable
 * creation to child class
 *
 * @param <T> the type of consumable that will be emitted as an RTM event
 */
public abstract class AbstractProducer<T extends EventConsumable<? extends BroadcastMessage>> implements EventProducer {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AbstractProducer.class);

    @Inject
    protected RedissonReactiveClient redis;

    public abstract T getEventConsumable();

    /**
     * Finds the redis subscription for the {@link T} type of RTM consumable and publish an event there for all listening
     * RTM clients.
     */
    @Override
    public void produce() {
        T t = getEventConsumable();
        RTopicReactive topic = redis.getTopic(t.getSubscriptionName());

        // publish the whole event message obj to redis and log how many subscribers got it
        topic.publish(t) //
                .publishOn(Schedulers.elastic())
                .doOnError(throwable -> {
                    log.jsonError("error producing event", new HashMap<String, Object>() {
                        {put("event", getEventConsumable().getRTMEvent().getName());}
                    }, throwable);
                })
                .subscribe(i -> {
                    log.debug("Producer published to channel: {}, clients: {}",
                              t.getSubscriptionName(), i);
                });
    }
}
