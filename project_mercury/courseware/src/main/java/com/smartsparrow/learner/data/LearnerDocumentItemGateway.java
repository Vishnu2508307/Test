package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.annotation.Nonnull;
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
public class LearnerDocumentItemGateway {

    private final Session session;
    private final LearnerDocumentItemMaterializer learnerDocumentItemMaterializer;
    private final LearnerDocumentItemMutator learnerDocumentItemMutator;
    private final LearnerDocumentItemByDocumentMaterializer learnerDocumentItemByDocumentMaterializer;
    private final LearnerDocumentItemByDocumentMutator learnerDocumentItemByDocumentMutator;

    @Inject
    public LearnerDocumentItemGateway(Session session,
                                      LearnerDocumentItemMaterializer learnerDocumentItemMaterializer,
                                      LearnerDocumentItemMutator learnerDocumentItemMutator,
                                      LearnerDocumentItemByDocumentMaterializer learnerDocumentItemByDocumentMaterializer,
                                      LearnerDocumentItemByDocumentMutator learnerDocumentItemByDocumentMutator) {
        this.session = session;
        this.learnerDocumentItemMaterializer = learnerDocumentItemMaterializer;
        this.learnerDocumentItemMutator = learnerDocumentItemMutator;
        this.learnerDocumentItemByDocumentMaterializer = learnerDocumentItemByDocumentMaterializer;
        this.learnerDocumentItemByDocumentMutator = learnerDocumentItemByDocumentMutator;
    }

    /**
     * Persist a document item for a published document
     *
     * @param item the learner document item to persist
     * @return a flux of void
     */
    public Flux<Void> persist(LearnerDocumentItem item) {
        return Mutators.execute(session, Flux.just(
                learnerDocumentItemMutator.upsert(item),
                learnerDocumentItemByDocumentMutator.upsert(item.getDocumentId(), item.getId())
        ));
    }

    /**
     * Find a published document item by id
     *
     * @param documentItemId the id of the published document item to find
     * @return mono of published document item if item exists, otherwise return empty mono
     */
    public Mono<LearnerDocumentItem> findById(final UUID documentItemId) {
        return ResultSets.query(session, learnerDocumentItemMaterializer.find(documentItemId))
                .flatMapIterable(row -> row)
                .map(learnerDocumentItemMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find all the published document items for a published document
     *
     * @param documentId the id of the published document to find the published document items for
     * @return a flux of learner document item
     */
    @Trace(async = true)
    public Flux<LearnerDocumentItem> findItems(@Nonnull final UUID documentId) {
        return ResultSets.query(session, learnerDocumentItemByDocumentMaterializer.fetchItems(documentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row-> row)
                .map(learnerDocumentItemByDocumentMaterializer::fromRow)
                .flatMap(this::findById);
    }
}
