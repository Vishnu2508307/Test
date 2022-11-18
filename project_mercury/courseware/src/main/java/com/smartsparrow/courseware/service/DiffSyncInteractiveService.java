package com.smartsparrow.courseware.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.data.InteractiveGateway;
import com.smartsparrow.util.UUIDs;

import data.DiffSyncEntity;
import data.SynchronizableService;
import reactor.core.publisher.Mono;

@Singleton
public class DiffSyncInteractiveService implements SynchronizableService {

    private final InteractiveGateway interactiveGateway;

    @Inject
    public DiffSyncInteractiveService(final InteractiveGateway interactiveGateway) {
        this.interactiveGateway = interactiveGateway;
    }

    @Override
    public Mono<String> getEntity(final DiffSyncEntity entity) {
        return interactiveGateway.findLatestConfig(entity.getEntityId())
                .map(InteractiveConfig::getConfig);
    }

    @Override
    public Mono<Void> persist(final DiffSyncEntity entity, final String content) {
        return interactiveGateway.persist(new InteractiveConfig()
                                                  .setId(UUIDs.timeBased())
                                                  .setInteractiveId(entity.getEntityId())
                                                  .setConfig(content));
    }
}
