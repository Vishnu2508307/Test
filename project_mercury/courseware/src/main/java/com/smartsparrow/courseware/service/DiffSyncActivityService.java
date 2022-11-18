package com.smartsparrow.courseware.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityGateway;
import com.smartsparrow.util.UUIDs;

import data.DiffSyncEntity;
import data.SynchronizableService;
import reactor.core.publisher.Mono;

@Singleton
public class DiffSyncActivityService implements SynchronizableService {

    private final ActivityGateway activityGateway;

    @Inject
    public DiffSyncActivityService(final ActivityGateway activityGateway) {
        this.activityGateway = activityGateway;
    }

    @Override
    public Mono<String> getEntity(final DiffSyncEntity entity) {
        return activityGateway.findLatestConfig(entity.getEntityId())
                .map(ActivityConfig::getConfig);
    }

    @Override
    public Mono<Void> persist(final DiffSyncEntity entity, final String content) {
        return activityGateway.persist(new ActivityConfig()
                                               .setId(UUIDs.timeBased())
                                               .setActivityId(entity.getEntityId())
                                               .setConfig(content)).singleOrEmpty();
    }
}
