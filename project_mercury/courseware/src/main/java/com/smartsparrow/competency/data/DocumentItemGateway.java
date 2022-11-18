package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class DocumentItemGateway {

    private static final Logger log = LoggerFactory.getLogger(DocumentItemGateway.class);
    private final Session session;

    private final DocumentItemMaterializer documentItemMaterializer;
    private final DocumentItemMutator documentItemMutator;
    private final ItemByDocumentMaterializer itemByDocumentMaterializer;
    private final ItemByDocumentMutator itemByDocumentMutator;

    @Inject
    public DocumentItemGateway(Session session,
                               DocumentItemMaterializer documentItemMaterializer,
                               DocumentItemMutator documentItemMutator,
                               ItemByDocumentMaterializer itemByDocumentMaterializer,
                               ItemByDocumentMutator itemByDocumentMutator) {
        this.session = session;
        this.documentItemMaterializer = documentItemMaterializer;
        this.documentItemMutator = documentItemMutator;
        this.itemByDocumentMaterializer = itemByDocumentMaterializer;
        this.itemByDocumentMutator = itemByDocumentMutator;
    }

    /**
     * Persist a document item to the database
     *
     * @param item the document item to persist
     * @return a flux of void
     */
    public Flux<Void> persist(DocumentItem item) {
        return Mutators.execute(session, Flux.just(
                documentItemMutator.upsert(item),
                itemByDocumentMutator.upsert(item.getDocumentId(), item.getId())
        ));
    }

    /**
     * Update a document item
     *
     * @param item the document item to update
     * @return a flux of void
     */
    public Flux<Void> update(DocumentItem item) {
        return Mutators.execute(session, Flux.just(documentItemMutator.updateFields(item)));
    }

    /**
     * Delete a document item
     *
     * @param item the item to delete
     * @return a flux of void
     */
    public Flux<Void> delete(DocumentItem item) {
        return Mutators.execute(session, Flux.just(
                documentItemMutator.delete(item),
                itemByDocumentMutator.delete(item.getDocumentId(), item.getId())
        ));
    }

    /**
     * Find a document item by its id
     *
     * @param documentItemId the document item id
     * @return a mono of document item or an empty mono when the document item is not found
     */
    @Trace(async = true)
    public Mono<DocumentItem> find(UUID documentItemId) {
        return ResultSets.query(session, documentItemMaterializer.fetchById(documentItemId))
                .flatMapIterable(row -> row)
                .map(documentItemMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find document item by id
     *
     * @param documentItemId - the document item to find
     * @return {@link Mono<DocumentItem>}
     */
    public Mono<DocumentItem> findById(UUID documentItemId) {
        return ResultSets.query(session,
                documentItemMaterializer.fetchById(documentItemId))
                .flatMapIterable(row -> row)
                .map(documentItemMaterializer::fromRow)
                .singleOrEmpty()
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching document item %s",
                            documentItemId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all document items by document id
     *
     * @param documentId - the document id to find the document items for
     * @return {@link Flux<DocumentItem>}
     */
    @Trace(async = true)
    public Flux<DocumentItem> findByDocumentId(UUID documentId) {
        return ResultSets
                .query(session,
                        itemByDocumentMaterializer.fetchItems(documentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(itemByDocumentMaterializer::fromRow)
                .flatMap(this::findById);
    }


}
