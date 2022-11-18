package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerSearchableDocumentGateway {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerSearchableDocumentGateway.class);

    private final Session session;
    private final LearnerSearchableDocumentMaterializer learnerSearchableDocumentMaterializer;
    private final LearnerSearchableDocumentMutator learnerSearchableDocumentMutator;

    @Inject
    public LearnerSearchableDocumentGateway(Session session,
                                            LearnerSearchableDocumentMaterializer learnerSearchableDocumentMaterializer,
                                            LearnerSearchableDocumentMutator learnerSearchableDocumentMutator) {
        this.session = session;
        this.learnerSearchableDocumentMaterializer = learnerSearchableDocumentMaterializer;
        this.learnerSearchableDocumentMutator = learnerSearchableDocumentMutator;
    }

    /**
     * Persist the learner searchable documents
     *
     * @param learnerSearchableDocument the learner searchable document to persist
     */
    public Flux<Void> persistLearnerSearchable(LearnerSearchableDocument learnerSearchableDocument) {
        Flux<? extends Statement> iter = Mutators.upsert(learnerSearchableDocumentMutator, learnerSearchableDocument);
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving learner searchable field %s",
                            learnerSearchableDocument), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch the latest learner searchable document entry
     *
     * @param deploymentId the plugin id
     * @param elementId the element id
     * @param searchableField the plugin searchable field
     */
    public Mono<LearnerSearchableDocument> fetchLatestLearnerSearchable(UUID deploymentId, UUID elementId, UUID searchableField) {
        return ResultSets.query(session, learnerSearchableDocumentMaterializer
                .findLatest(deploymentId, elementId, searchableField))
                .flatMapIterable(row -> row)
                .map(learnerSearchableDocumentMaterializer::fromRow)
                .singleOrEmpty()
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching learner searchable %s, %s, %s",
                            deploymentId, elementId, searchableField), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }


    public Flux<LearnerSearchableDocumentIdentity> fetchElementIds(UUID deploymentId) {
        return ResultSets.query(session, learnerSearchableDocumentMaterializer
                .findElementIdsByDeployment(deploymentId))
                .flatMapIterable(row -> row)
                .map(learnerSearchableDocumentMaterializer::identityFromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching learner searchable with deploymentIds %s",
                                            deploymentId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }
}
