package com.smartsparrow.learner.service;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.ManualGradingComponentByWalkable;
import com.smartsparrow.courseware.data.ManualGradingConfigurationGateway;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;

@Singleton
public class ManualGradeDuplicationService {

    private final ManualGradingConfigurationGateway manualGradingConfigurationGateway;

    @Inject
    public ManualGradeDuplicationService(ManualGradingConfigurationGateway manualGradingConfigurationGateway) {
        this.manualGradingConfigurationGateway = manualGradingConfigurationGateway;
    }


    /**
     * Find all the manual grading component descendants of a walkable
     *
     * @param walkableId the walkable to find the descendants manual components for
     * @return a flux of manual grading component by walkable
     */
    @Trace(async = true)
    public Flux<ManualGradingComponentByWalkable> findManualGradingComponentByWalkable(final UUID walkableId) {
        return manualGradingConfigurationGateway.findManualComponentsByWalkable(walkableId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a list of manual grading component by walkable to the db
     *
     * @param manualGradingComponentByWalkables the list of manual grading component by walkable to persist
     * @return a flux with the persisted manual grading component by walkable or an empty flux when none were persisted
     */
    @Trace(async = true)
    public Flux<ManualGradingComponentByWalkable> persist(final List<ManualGradingComponentByWalkable> manualGradingComponentByWalkables) {
        if (manualGradingComponentByWalkables.isEmpty()) {
            return Flux.empty();
        }
        return manualGradingComponentByWalkables.stream()
                .map(manualGradingComponentByWalkable -> manualGradingConfigurationGateway.persist(manualGradingComponentByWalkable)
                        .thenMany(Flux.just(manualGradingComponentByWalkable))).reduce(Flux::mergeWith)
                .orElse(Flux.empty())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
