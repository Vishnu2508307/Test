package com.smartsparrow.competency.data;

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
public class DocumentItemLinkGateway {

    private final Session session;

    private final CoursewareElementByDocumentItemMutator coursewareElementByDocumentItemMutator;
    private final CoursewareElementByDocumentMutator coursewareElementByDocumentMutator;
    private final DocumentItemByCoursewareElementMutator documentItemByCoursewareElementMutator;
    private final DocumentItemByCoursewareElementMaterializer documentItemByCoursewareElementMaterializer;
    private final CoursewareElementByDocumentItemMaterializer coursewareElementByDocumentItemMaterializer;

    @Inject
    public DocumentItemLinkGateway(Session session,
                                   CoursewareElementByDocumentItemMutator coursewareElementByDocumentItemMutator,
                                   CoursewareElementByDocumentMutator coursewareElementByDocumentMutator,
                                   DocumentItemByCoursewareElementMutator documentItemByCoursewareElementMutator,
                                   DocumentItemByCoursewareElementMaterializer documentItemByCoursewareElementMaterializer,
                                   CoursewareElementByDocumentItemMaterializer coursewareElementByDocumentItemMaterializer) {
        this.session = session;
        this.coursewareElementByDocumentItemMutator = coursewareElementByDocumentItemMutator;
        this.coursewareElementByDocumentMutator = coursewareElementByDocumentMutator;
        this.documentItemByCoursewareElementMutator = documentItemByCoursewareElementMutator;
        this.documentItemByCoursewareElementMaterializer = documentItemByCoursewareElementMaterializer;
        this.coursewareElementByDocumentItemMaterializer = coursewareElementByDocumentItemMaterializer;
    }

    /**
     * Persist a document item link
     *
     * @param documentItemTag the document item link to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final DocumentItemTag documentItemTag) {
        return Mutators.execute(session, Flux.just(
                coursewareElementByDocumentItemMutator.upsert(documentItemTag),
                coursewareElementByDocumentMutator.upsert(documentItemTag),
                documentItemByCoursewareElementMutator.upsert(documentItemTag)
        )).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete a document item link
     *
     * @param documentItemTag the document item link to delete
     * @return a flux of void
     */
    public Flux<Void> delete(final DocumentItemTag documentItemTag) {
        return Mutators.execute(session, Flux.just(
                coursewareElementByDocumentItemMutator.delete(documentItemTag),
                coursewareElementByDocumentMutator.delete(documentItemTag),
                documentItemByCoursewareElementMutator.delete(documentItemTag)
        ));
    }

    /**
     * Find all document items linked to a courseware element
     *
     * @param elementId the element id to find the document items for
     * @return a flux of document item tag
     */
    @Trace(async = true)
    public Flux<DocumentItemTag> fetchByElement(final UUID elementId) {
        return ResultSets.query(session, documentItemByCoursewareElementMaterializer.fetchAllItems(elementId))
                .flatMapIterable(row->row)
                .map(documentItemByCoursewareElementMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all courseware elements linked to a document item
     *
     * @param documentItemId the document item id to find the linked elements for
     * @return a flux of document item tag
     */
    public Flux<DocumentItemTag> fetchByDocumentItem(final UUID documentItemId) {
        return ResultSets.query(session, coursewareElementByDocumentItemMaterializer.fetchElements(documentItemId))
                .flatMapIterable(row->row)
                .map(coursewareElementByDocumentItemMaterializer::fromRow);
    }
}
