package com.smartsparrow.rtm.subscription;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import org.apache.http.HttpStatus;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.listener.MessageListener;

import com.smartsparrow.dataevent.eventmessage.EventMessage;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * The class provides the basic fields and implementation for a {@link Subscription}
 * @param <T> the type of reactive topic for the subscription
 * !!! This class has been deprecated in favour of {@link com.smartsparrow.rtm.subscription.data.RTMSubscription}
 */
@Deprecated
public abstract class EventSubscription<T extends EventMessage> implements Subscription<T> {

    private String channel = null;
    private Integer listenerId = null;
    private RTopicReactive redisTopic = null;
    private MessageListener<T> messageListener;

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(EventSubscription.class);

    @Inject
    private RedissonReactiveClient redis;

    /**
     * Accessor for the injected redis client service
     */
    public RedissonReactiveClient getRedis() {
        return redis;
    }

    /**
     * Set the channel name for the subscription. The channel name is required. Should be as unique as required
     * to allow for multiple subscriptions of the same type. For example, lesson/1234 and lesson/4567
     *
     * @param data parameterized type the channel name is built from
     */
    protected void setName(String data) {
        channel = data;
    }

    @Override
    public String getName() {
        return channel;
    }

    public abstract MessageListener<T> initMessageListener(RTMClient rtmClient);

    @Trace(async = true)
    @Override
    public Mono<Integer> subscribe(RTMClient rtmClient) {
        if (getName() == null) throw new RuntimeException("name is empty, use setName method");
        redisTopic = getRedis().getTopic(getName());

        this.messageListener = initMessageListener(rtmClient);
        Mono<Integer> listenerIdMono = redisTopic.addListener(getMessageType(), messageListener);

        return listenerIdMono.publishOn(Schedulers.elastic())
                .doOnNext(id -> listenerId = id)
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    @Override
    public void unsubscribe(RTMClient rtmClient) {
        redisTopic.removeListener(messageListener).publishOn(Schedulers.elastic()).subscribe();
    }

    protected void emitError(RTMClient rtmClient, String type, EventMessage msg) {
        try {
            Responses.error(rtmClient.getSession(), getId(), type,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error broadcasting event %s", msg.getName());
        } catch (WriteResponseException e) {
            log.warn("Error emitting error broadcast to socket, reason: ", e);
            log.info("Discarded broadcast message: {}", msg);
        }
    }

    protected void emitSuccess(RTMClient rtmClient, BasicResponseMessage reply) {
        try {
            Responses.write(rtmClient.getSession(), reply);
        } catch (WriteResponseException e) {
            // not reproducing this pattern to other subscribers now, it should be pulled up to a single point in
            // code to be done in Steady September
            log.info("Discarding received publish message to {}, client is gone", getName());
        }
    }

}
