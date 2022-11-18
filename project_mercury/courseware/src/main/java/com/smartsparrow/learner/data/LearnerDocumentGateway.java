package com.smartsparrow.learner.data;

import java.beans.Transient;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import com.smartsparrow.util.monitoring.ReactiveTransaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerDocumentGateway {

    private final Session session;
    private final LearnerDocumentMaterializer learnerDocumentMaterializer;
    private final LearnerDocumentMutator learnerDocumentMutator;

    @Inject
    public LearnerDocumentGateway(Session session,
                                  LearnerDocumentMaterializer learnerDocumentMaterializer,
                                  LearnerDocumentMutator learnerDocumentMutator) {
        this.session = session;
        this.learnerDocumentMaterializer = learnerDocumentMaterializer;
        this.learnerDocumentMutator = learnerDocumentMutator;
    }

    /**
     * Persist a published documet
     *
     * @param learnerDocument the document to persist
     * @return a flux of void
     */
    public Flux<Void> persist(LearnerDocument learnerDocument) {
        return Mutators.execute(session, Flux.just(
                learnerDocumentMutator.upsert(learnerDocument)
        ));
    }

    /**
     * Find a published competency document by id
     *
     * @param documentId the id of the document to fetch
     * @return a mono of learner document
     */
    @Trace(async = true)
    public Mono<LearnerDocument> find(UUID documentId) {
        return ResultSets.query(session, learnerDocumentMaterializer.find(documentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(learnerDocumentMaterializer::fromRow)
                .singleOrEmpty();
    }
}
