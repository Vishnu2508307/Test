package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;

@Singleton
public class ManualGradeEntryGateway {

    private final Session session;

    private final ManualGradeEntryMaterializer manualGradeEntryMaterializer;
    private final ManualGradeEntryMutator manualGradeEntryMutator;

    @Inject
    public ManualGradeEntryGateway(Session session,
                                   ManualGradeEntryMaterializer manualGradeEntryMaterializer,
                                   ManualGradeEntryMutator manualGradeEntryMutator) {
        this.session = session;
        this.manualGradeEntryMaterializer = manualGradeEntryMaterializer;
        this.manualGradeEntryMutator = manualGradeEntryMutator;
    }

    /**
     * Persist a manual grade entry object
     *
     * @param manualGradeEntry the manual grade entry to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final ManualGradeEntry manualGradeEntry) {
        return Mutators.execute(session, Flux.just(
                manualGradeEntryMutator.upsert(manualGradeEntry)
        )).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the manual grade entries for a student on a particular component attempt
     *
     * @param deploymentId the deployment id the component belongs to
     * @param studentId the student id the manual grade has been awarded to
     * @param componentId the component id to find the manual grade for
     * @param attemptId the particular attempt over the component id
     * @return a flux of manual grade entries
     */
    @Trace(async = true)
    public Flux<ManualGradeEntry> findAll(final UUID deploymentId, final UUID studentId, final UUID componentId,
                                          final UUID attemptId) {
        return ResultSets.query(session, manualGradeEntryMaterializer.findAll(deploymentId, studentId, componentId, attemptId))
                .flatMapIterable(row -> row)
                .map(manualGradeEntryMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
