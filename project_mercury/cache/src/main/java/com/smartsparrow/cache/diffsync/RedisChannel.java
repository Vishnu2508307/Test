package com.smartsparrow.cache.diffsync;

import java.util.Arrays;

import javax.inject.Inject;

import com.smartsparrow.exception.UnsupportedOperationFault;

import data.Ack;
import data.Channel;
import data.DiffSyncEntity;
import data.DiffSyncIdentifier;
import data.DiffSyncService;
import data.Message;
import data.Patch;

/**
 * Channel type used for maintaining a diff sync with another Server
 */
public class RedisChannel implements Channel {

    private final DiffSyncProducer diffSyncProducer;
    private final DiffSyncService diffSyncService;
    // unique identifier name
    private final DiffSyncIdentifier diffSyncIdentifier;
   private final DiffSyncEntity diffSyncEntity;

    @Inject
    public RedisChannel(final DiffSyncProducer diffSyncProducer,
                        final DiffSyncIdentifier diffSyncIdentifier,
                        final DiffSyncEntity diffSyncEntity,
                        final DiffSyncService diffSyncService) {
        this.diffSyncProducer = diffSyncProducer;
        this.diffSyncIdentifier = diffSyncIdentifier;
        this.diffSyncEntity = diffSyncEntity;
        this.diffSyncService = diffSyncService;
    }

    @Override
    public void send(Message message) {
        // sending message to the diff sync topic
        diffSyncProducer.buildConsumableMessage(message, diffSyncIdentifier,diffSyncEntity).produce();

    }

    @Override
    public void receive(Message message) {
        // listen to redis for incoming messages
        switch (message.getBody().getType()) {
            case PATCH:
                // a server just told me of a patch, let me find the DiffSync stack I have with that
                // server and call the sync patch method
                Patch patch = (Patch)message.getBody();
                diffSyncService.syncPatch(diffSyncEntity, Arrays.asList(patch), diffSyncIdentifier);
                break;
            case ACK:
                // a server just told me of a ack, let me find the DiffSync stack I have with that
                // server and call the sync ack method
                Ack ack = (Ack)message.getBody();
                diffSyncService.syncAck(diffSyncEntity, diffSyncIdentifier, ack);
                break;
            default:
                throw new UnsupportedOperationFault("Message type doesn't exist");
        }
    }

    public DiffSyncIdentifier getDiffSyncIdentifier() {
        return diffSyncIdentifier;
    }

    public DiffSyncEntity getDiffSyncEntity() {
        return diffSyncEntity;
    }
}
