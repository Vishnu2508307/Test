package com.smartsparrow.cache.diffsync;

import java.util.HashMap;

import javax.inject.Inject;

import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.scheduler.Schedulers;

public abstract class AbstractDiffSyncProducer implements DiffSyncEventProducer {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AbstractDiffSyncProducer.class);

    @Inject
    protected RedissonReactiveClient redis;

    public abstract DiffSyncMessage getDiffSyncMessage();


    @Override
    public void produce() {
        DiffSyncMessage t = getDiffSyncMessage();
        RTopicReactive topic = redis.getTopic(t.getSubscriptionName());

        // publish the whole event message obj to redis and log how many subscribers got it
        topic.publish(t) //
                .publishOn(Schedulers.elastic())
                .doOnError(throwable -> {
                    log.jsonError("error producing event", new HashMap<String, Object>() {
                        {
                            put("event", t.getDiffSyncEntity());
                        }
                    }, throwable);
                })
                .subscribe(i -> {

                               log.debug("Producer published to channel: {}, servers: {}",
                                         t.getDiffSyncEntity(), i);
                           },
                           ex -> {
                               log.jsonError("Unable to publish diff syncmessage{}",
                                             new HashMap<String, Object>() {
                                             },
                                             ex);
                           });
    }
}

