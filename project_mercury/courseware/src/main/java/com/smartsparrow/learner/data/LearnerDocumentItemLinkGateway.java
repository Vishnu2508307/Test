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
public class LearnerDocumentItemLinkGateway {

    private final Session session;
    private final LearnerCoursewareElementByDocumentItemMaterializer learnerCoursewareElementByDocumentItemMaterializer;
    private final LearnerCoursewareElementByDocumentItemMutator learnerCoursewareElementByDocumentItemMutator;
    private final LearnerCoursewareElementByDocumentMaterializer learnerCoursewareElementByDocumentMaterializer;
    private final LearnerCoursewareElementByDocumentMutator learnerCoursewareElementByDocumentMutator;
    private final LearnerDocumentItemByCoursewareElementMaterializer learnerDocumentItemByCoursewareElementMaterializer;
    private final LearnerDocumentItemByCoursewareElementMutator learnerDocumentItemByCoursewareElementMutator;

    @Inject
    public LearnerDocumentItemLinkGateway(Session session,
                                          LearnerCoursewareElementByDocumentItemMaterializer learnerCoursewareElementByDocumentItemMaterializer,
                                          LearnerCoursewareElementByDocumentItemMutator learnerCoursewareElementByDocumentItemMutator,
                                          LearnerCoursewareElementByDocumentMaterializer learnerCoursewareElementByDocumentMaterializer,
                                          LearnerCoursewareElementByDocumentMutator learnerCoursewareElementByDocumentMutator,
                                          LearnerDocumentItemByCoursewareElementMaterializer learnerDocumentItemByCoursewareElementMaterializer,
                                          LearnerDocumentItemByCoursewareElementMutator learnerDocumentItemByCoursewareElementMutator) {
        this.session = session;
        this.learnerCoursewareElementByDocumentItemMaterializer = learnerCoursewareElementByDocumentItemMaterializer;
        this.learnerCoursewareElementByDocumentItemMutator = learnerCoursewareElementByDocumentItemMutator;
        this.learnerCoursewareElementByDocumentMaterializer = learnerCoursewareElementByDocumentMaterializer;
        this.learnerCoursewareElementByDocumentMutator = learnerCoursewareElementByDocumentMutator;
        this.learnerDocumentItemByCoursewareElementMaterializer = learnerDocumentItemByCoursewareElementMaterializer;
        this.learnerDocumentItemByCoursewareElementMutator = learnerDocumentItemByCoursewareElementMutator;
    }

    /**
     * Persist a link between a document item and a deployed courseware element
     *
     * @param learnerDocumentItemTag the object representing the link
     * @return a flux of void
     */
    public Flux<Void> persist(LearnerDocumentItemTag learnerDocumentItemTag) {
        return Mutators.execute(session, Flux.just(
                learnerCoursewareElementByDocumentItemMutator.upsert(learnerDocumentItemTag),
                learnerCoursewareElementByDocumentMutator.upsert(learnerDocumentItemTag),
                learnerDocumentItemByCoursewareElementMutator.upsert(learnerDocumentItemTag)
        ));
    }

    /**
     * Find all the competency document item ids that are linked to a given learner walkable
     *
     * @param learnerWalkable the learner walkable to find the links for
     * @return a flux of uuids
     */
    @Trace(async = true)
    public Flux<UUID> find(final LearnerWalkable learnerWalkable) {
        return ResultSets.query(session, learnerDocumentItemByCoursewareElementMaterializer
                .fetch(learnerWalkable.getId(), learnerWalkable.getDeploymentId(), learnerWalkable.getChangeId()))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(learnerCoursewareElementByDocumentItemMaterializer::fromRow)
                .map(LearnerDocumentItemTag::getDocumentItemId);
    }


}
