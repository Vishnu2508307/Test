package com.smartsparrow.cache.diffsync;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import javax.inject.Inject;

import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.listener.MessageListener;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import data.DiffSync;
import data.DiffSyncEntity;
import data.DiffSyncIdentifier;
import data.DiffSyncIdentifierType;
import data.DiffSyncProvider;
import data.DiffSyncService;
import data.Exchangeable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public abstract class AbstractDiffSyncSubscription implements Serializable {

    private static final long serialVersionUID = 5997179687824224010L;

    private Integer listenerId = null;
    private RTopicReactive redisTopic = null;

    private MessageListener<DiffSyncMessage> messageListener;
    private final UUID subscriptionId;

    @Inject
    private RedissonReactiveClient redis;
    @Inject
    private DiffSyncProducer diffSyncProducer;
    @Inject
    private DiffSyncService diffSyncService;
    @Inject
    private DiffSyncProvider diffSyncProvider;

    public AbstractDiffSyncSubscription() {
        this.subscriptionId = UUIDs.timeBased();
    }

    public MessageListener<DiffSyncMessage> getMessageListener() {
        return messageListener;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public RedissonReactiveClient getRedis() {
        return redis;
    }

    // subscription topic name
    public abstract String getName();

    /**
     * Implements the message listener that is added as a listener to the topic. This method is invoked everytime
     * an event is sent to the diff sync subscription triggering a wide broadcast to all listening servers
     *
     * @param diffSyncIdentifier the diff sync identifier
     * @return the diff sync message listener that will be invoked on a subscription wide broadcast
     */
    protected MessageListener<DiffSyncMessage> messageListener(final DiffSyncIdentifier diffSyncIdentifier) {

        return ((channel, diffSyncMessage) -> {
            // Do nothing if event originates from this same diff sync server
            // Is this message coming from another diff sync stack?
            if (diffSyncMessage.getDiffSyncIdentifier().equals(diffSyncIdentifier)) {
                // drop it
                return;
            }

            if (diffSyncMessage.getMessage().getBody().getType().equals(Exchangeable.Type.PATCH)) {
                Collection<DiffSync> diffSyncs = diffSyncProvider.getDiffSyncByEntity(diffSyncMessage.getDiffSyncEntity().getEntity());
                // and for each one of them filter out those where the
                diffSyncs.stream()
                        .filter(diffSync -> !diffSyncMessage.getDiffSyncIdentifier().equals(diffSync.getDiffSyncIdentifier()))
                        .forEach(diffSync -> diffSync.getChannel().send(diffSyncMessage.getMessage()));
                return;
            }
            return;

        });
    }

    /**
     * Allows to subscribe to the subscription
     *
     * @param diffSyncIdentifier the diff sync identifier
     * @return
     */
    @Trace(async = true)
    public Mono<Integer> subscribe(final DiffSyncIdentifier diffSyncIdentifier) {

        if (getName() == null) throw new RuntimeException("name is empty, use setName method");
        redisTopic = getRedis().getTopic(getName());

        this.messageListener = messageListener(diffSyncIdentifier);
        Mono<Integer> listenerIdMono = redisTopic.addListener(DiffSyncMessage.class, this.messageListener);

        return listenerIdMono.publishOn(Schedulers.elastic())
                .doOnNext(id -> listenerId = id)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Allows to unsubscribe from the subscription
     */
    public void unsubscribe(DiffSyncEntity diffSyncEntity) {
        redis.getTopic(diffSyncEntity.getEntity()).removeListener(messageListener).publishOn(Schedulers.elastic()).subscribe();
    }

}
