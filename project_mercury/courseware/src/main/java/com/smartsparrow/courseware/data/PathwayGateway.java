package com.smartsparrow.courseware.data;

import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.pathway.PathwayBuilder;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.PreloadPathway;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class PathwayGateway {

    private final Session session;

    private final PathwayBuilder pathwayBuilder;

    //
    private final PathwayMutator pathwayMutator;
    private final ChildPathwayByActivityMutator childPathwayByActivityMutator;
    private final ParentActivityByPathwayMutator parentActivityByPathwayMutator;
    private final ChildWalkableByPathwayMutator childWalkableByPathwayMutator;
    private final PathwayConfigMutator pathwayConfigMutator;
    private final ElementMutator elementMutator;

    //
    private final PathwayMaterializer pathwayMaterializer;
    private final ParentActivityByPathwayMaterializer parentActivityByPathwayMaterializer;
    private final ChildWalkableByPathwayMaterializer childWalkableByPathwayMaterializer;
    private final PathwayConfigMaterializer pathwayConfigMaterializer;

    @Inject
    public PathwayGateway(final Session session,
                          final PathwayBuilder pathwayBuilder,
                          final PathwayMutator pathwayMutator,
                          final ChildPathwayByActivityMutator childPathwayByActivityMutator,
                          final ParentActivityByPathwayMutator parentActivityByPathwayMutator,
                          final ChildWalkableByPathwayMutator childWalkableByPathwayMutator,
                          final PathwayConfigMutator pathwayConfigMutator,
                          final ElementMutator elementMutator,
                          final PathwayMaterializer pathwayMaterializer,
                          final ParentActivityByPathwayMaterializer parentActivityByPathwayMaterializer,
                          final ChildWalkableByPathwayMaterializer childWalkableByPathwayMaterializer,
                          final PathwayConfigMaterializer pathwayConfigMaterializer) {
        this.session = session;
        this.pathwayBuilder = pathwayBuilder;
        this.pathwayMutator = pathwayMutator;
        this.childPathwayByActivityMutator = childPathwayByActivityMutator;
        this.parentActivityByPathwayMutator = parentActivityByPathwayMutator;
        this.childWalkableByPathwayMutator = childWalkableByPathwayMutator;
        this.pathwayConfigMutator = pathwayConfigMutator;
        this.elementMutator = elementMutator;
        this.pathwayMaterializer = pathwayMaterializer;
        this.parentActivityByPathwayMaterializer = parentActivityByPathwayMaterializer;
        this.childWalkableByPathwayMaterializer = childWalkableByPathwayMaterializer;
        this.pathwayConfigMaterializer = pathwayConfigMaterializer;
    }

    /**
     * Creates a pathway persisting the child/parent relationship with the parent activity
     *
     * @param pathway the new pathway to persist
     * @param activityId the pathway parent activity
     */
    @Trace(async = true)
    public Flux<Void> persist(Pathway pathway, UUID activityId) {
        CoursewareElement pathwayElement = new CoursewareElement()
                .setElementId(pathway.getId())
                .setElementType(CoursewareElementType.PATHWAY);

        return Mutators.execute(session, Flux.just(
                pathwayMutator.upsert(pathway),
                childPathwayByActivityMutator.addPathway(pathway.getId(), activityId),
                parentActivityByPathwayMutator.insert(pathway.getId(), activityId),
                elementMutator.upsert(pathwayElement)
        )).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete the child/parent relationship between two existing pathway/activity entities. <b>The pathway is not
     * deleted</b> from its table and will continue to exists in a limbo. Only the relationship is deleted.
     *
     * @param pathwayId to delete the relationship for
     * @param activityId the activity that should detach from its child pathway
     */
    @Trace(async = true)
    public Flux<Void> deleteRelationship(UUID pathwayId, UUID activityId) {
        return Mutators.execute(session, Flux.just(
                childPathwayByActivityMutator.removePathway(pathwayId, activityId),
                parentActivityByPathwayMutator.delete(pathwayId)
        ))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the parent activity id for a given pathway.
     *
     * @param pathwayId the pathway to search the parent activity for
     * @return an activity id
     * @throws NoSuchElementException if no parent is found
     */
    @Trace(async = true)
    public Mono<UUID> findParentActivityId(UUID pathwayId) {
        return ResultSets.query(session, parentActivityByPathwayMaterializer.fetch(pathwayId))
                .flatMapIterable(row->row)
                .map(parentActivityByPathwayMaterializer::fromRow)
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the walkable children for a given pathway.
     *
     * @param pathwayId the pathway to find the children for
     * @return a {@link WalkablePathwayChildren} obj that holds an ordered list of children and a map of types.
     */
    @Trace(async = true)
    public Mono<WalkablePathwayChildren> findWalkableChildren(UUID pathwayId) {
        return ResultSets.query(session, childWalkableByPathwayMaterializer.fetchBy(pathwayId))
                .flatMapIterable(row->row)
                .map(childWalkableByPathwayMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a pathway by id
     *
     * @param pathwayId
     * @return
     */
    @Trace(async = true)
    public Mono<Pathway> findById(final UUID pathwayId) {
        return ResultSets.query(session, pathwayMaterializer.findById(pathwayId)) //
                .flatMapIterable(row -> row) //
                .map(row -> pathwayBuilder.build(Enums.of(PathwayType.class, row.getString("type")),
                                                 row.getUUID("id"),
                                                 row.getString("preload_pathway") != null ?
                                                         Enums.of(PreloadPathway.class,
                                                                  row.getString("preload_pathway")) :
                                                         PreloadPathway.NONE))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save child(activity or interactive) in a pathway
     *
     * @param childId   the child activity
     * @param childType the child type
     * @param pathwayId the parent pathway
     */
    @Trace(async = true)
    public Flux<Void> persistChild(UUID childId, CoursewareElementType childType, UUID pathwayId) {
        WalkablePathwayChildren child = new WalkablePathwayChildren()
                .setPathwayId(pathwayId)
                .addWalkable(childId, childType.name());

        return Mutators.execute(session, Flux.just(childWalkableByPathwayMutator.addWalkable(child)
        ).doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Save children for a pathway
     *
     * @param pathwayChildren pathway children
     */
    @Trace(async = true)
    public Flux<Void> persist(WalkablePathwayChildren pathwayChildren) {
        return Mutators.execute(session, Flux.just(
                childWalkableByPathwayMutator.upsert(pathwayChildren)
        ).doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Remove child from pathway
     *
     * @param childId   the child to remove
     * @param childType the child type
     * @param pathwayId the pathway
     */
    @Trace(async = true)
    public Flux<Void> removeChild(UUID childId, CoursewareElementType childType, UUID pathwayId) {
        WalkablePathwayChildren child = new WalkablePathwayChildren()
                .setPathwayId(pathwayId)
                .addWalkable(childId, childType.name());

        return Mutators.execute(session, Flux.just(
                childWalkableByPathwayMutator.removeWalkable(child)
        ).doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Persist the config for a pathway
     *
     * @param pathwayConfig the pathway config to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final PathwayConfig pathwayConfig) {
        return Mutators.execute(session, Flux.just(pathwayConfigMutator.upsert(pathwayConfig)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the latest config for a pathway. Config are optional on pathways therefore not all pathway have a config
     *
     * @param pathwayId the pathway id to find the latest config for
     * @return a mono of pathway config or empty if not found
     */
    @Trace(async = true)
    public Mono<PathwayConfig> findLatestConfig(final UUID pathwayId) {
        return ResultSets.query(session, pathwayConfigMaterializer.fetchLatestConfig(pathwayId))
                .flatMapIterable(row -> row)
                .map(pathwayConfigMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

}
