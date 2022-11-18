package com.smartsparrow.courseware.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.PathwayConfig;
import com.smartsparrow.courseware.data.PathwayGateway;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.lang.ParentActivityNotFoundException;
import com.smartsparrow.courseware.lang.PathwayAlreadyExistsFault;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.pathway.PathwayBuilder;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.PreloadPathway;
import com.smartsparrow.courseware.payload.PathwayPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.util.Walkables;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class PathwayService {

    private final PathwayGateway pathwayGateway;
    private final PathwayBuilder pathwayBuilder;
    private final ActivityService activityService;
    private final CoursewareAssetService coursewareAssetService;
    private final CoursewareElementDescriptionService coursewareDescriptionService;

    @Inject
    public PathwayService(final PathwayGateway pathwayGateway,
                          final PathwayBuilder pathwayBuilder,
                          final ActivityService activityService,
                          final CoursewareAssetService coursewareAssetService,
                          final CoursewareElementDescriptionService coursewareDescriptionService) {
        this.pathwayGateway = pathwayGateway;
        this.pathwayBuilder = pathwayBuilder;
        this.activityService = activityService;
        this.coursewareAssetService = coursewareAssetService;
        this.coursewareDescriptionService = coursewareDescriptionService;
    }

    /**
     * Create a Pathway associated to an Activity
     *
     * @param creatorId the user creating the pathway
     * @param activityId the activity to link the new pathway to
     * @param pathwayType the type of pathway to create
     * @param pathwayId the pathway id to create
     * @param preloadPathway the preload pathway info
     * @return a new Pathway
     * @throws PathwayAlreadyExistsFault if provided pathway id already exists
     */
    @Trace(async = true)
    public Mono<Pathway> create(final UUID creatorId,
                                final UUID activityId,
                                final PathwayType pathwayType,
                                final UUID pathwayId,
                                final PreloadPathway preloadPathway) {

        checkArgument(creatorId != null, "missing creatorId");
        checkArgument(activityId != null, "missing activityId");
        checkArgument(pathwayType != null, "missing pathwayType");

        // If a pathway id has been supplied
        // check it does not already exist
        return  pathwayGateway.findById(pathwayId)
                .hasElement()
                .filter(hasElement -> !hasElement)
                .switchIfEmpty(Mono.error(new PathwayAlreadyExistsFault(pathwayId)))
                .flatMap(pathwayIdNotFound -> {
                    return  createPathway(creatorId, activityId, pathwayType, pathwayId, preloadPathway);
                });
    }

    /**
     * setting the pathwayId when pathway created
     *
     * @param creatorId the user creating the pathway
     * @param activityId the activity to link the new pathway to
     * @param pathwayType the type of pathway to create
     * @param preloadPathway the preload pathway info
     * @return a new Pathway
     */
    @Trace(async = true)
    public Mono<Pathway> create(final UUID creatorId,
                                final UUID activityId,
                                final PathwayType pathwayType,
                                final PreloadPathway preloadPathway) {

        checkArgument(creatorId != null, "missing creatorId");
        checkArgument(activityId != null, "missing activityId");
        checkArgument(pathwayType != null, "missing pathwayType");

        return  createPathway(creatorId, activityId, pathwayType, UUIDs.timeBased(), preloadPathway);
    }

    /**
     * Create a Pathway associated to an Activity
     *
     * @param creatorId the user creating the pathway
     * @param activityId the activity to link the new pathway to
     * @param pathwayType the type of pathway to create
     * @param pathwayId the pathway id to create
     * @param preloadPathway the preload pathway info
     * @return a new Pathway
     */
    @Trace(async = true)
    public Mono<Pathway> createPathway(final UUID creatorId,
                                       final UUID activityId,
                                       final PathwayType pathwayType,
                                       final UUID pathwayId,
                                       final PreloadPathway preloadPathway) {

        Mono<Pathway> pathwayMono = Mono.just(pathwayBuilder.build(pathwayType, pathwayId, preloadPathway));
        return pathwayMono.flatMap(pathway ->
                                           activityService.findById(activityId)
                                                   .thenEmpty(pathwayGateway.persist(pathway, activityId))
                                                   .then(Mono.just(pathway))
                                                   .single()
                                                   .doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Find a pathway by the given id
     *
     * @param pathwayId the pathway id
     * @throws PathwayNotFoundException if pathway for the given id can not be found
     */
    @Trace(async = true)
    public Mono<Pathway> findById(final UUID pathwayId) {
        return pathwayGateway.findById(pathwayId).single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new PathwayNotFoundException(pathwayId);
                });
    }

    /**
     * Find the parent activity id for a given pathway
     *
     * @param pathwayId the pathway to find the parent activity for
     * @throws ParentActivityNotFoundException if the parent activity is not found
     */
    @Trace(async = true)
    public Mono<UUID> findParentActivityId(final UUID pathwayId) {
        return pathwayGateway.findParentActivityId(pathwayId)
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ParentActivityNotFoundException(pathwayId);
                });
    }

    /**
     * Delete an existing pathway by deleting the relationship with its parent activity item. The pathway will still
     * exists in its table.
     *
     * @param pathwayId  the pathway id to delete
     * @param activityId the activity to detach the pathway from
     * @return void mono or empty
     */
    @Trace(async = true)
    public Mono<Void> delete(final UUID pathwayId, final UUID activityId) {
        return pathwayGateway.deleteRelationship(pathwayId, activityId)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Build a pathway payload object given a pathwayId
     *
     * @param pathwayId the pathway id to build the payload for
     * @throws RuntimeException when failing to zip the necessary streams to build the payload
     */
    @Trace(async = true)
    public Mono<PathwayPayload> getPathwayPayload(final UUID pathwayId) {
        return findById(pathwayId)
                .flatMap(this::getPathwayPayload)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Build a pathway payload given an existing pathway
     *
     * @param pathway the pathway to build the payload for
     * @throws RuntimeException when failing to zip the necessary streams to build the payload
     */
    @Trace(async = true)
    public Mono<PathwayPayload> getPathwayPayload(final Pathway pathway) {
        Mono<UUID> parentActivityId = findParentActivityId(pathway.getId())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<List<WalkableChild>> children = getOrderedWalkableChildren(pathway.getId())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<PathwayConfig> configMono = findLatestConfig(pathway.getId())
                .defaultIfEmpty(new PathwayConfig())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<CoursewareElementDescription> descriptionMono = getElementDescriptionByPathwayId(pathway.getId())
                .defaultIfEmpty(new CoursewareElementDescription())
                .doOnEach(ReactiveTransaction.linkOnNext());

                Mono < PathwayPayload > payloadMono = Mono.zip(Mono.just(pathway), parentActivityId, children, configMono, descriptionMono)
                        .map(tuple5 -> PathwayPayload.from(tuple5.getT1(), tuple5.getT2(), tuple5.getT3(), tuple5.getT4().getConfig(), tuple5.getT5()));

        payloadMono = payloadMono.flatMap(payload -> coursewareAssetService.getAssetPayloads(pathway.getId())
                .doOnSuccess(payload::setAssets)
                .thenReturn(payload))
                .doOnEach(ReactiveTransaction.linkOnNext());
        return payloadMono;
    }

    /**
     * Get the descriptive json for a pathway
     *
     * @param pathwayId the pathway id
     * @return a mono of courseware element description or empty if none are found
     */
    @Trace(async = true)
    public Mono<CoursewareElementDescription> getElementDescriptionByPathwayId(final UUID pathwayId) {
        affirmArgument(pathwayId != null, "pathwayId is required");
        return coursewareDescriptionService.fetchCoursewareDescriptionByElement(pathwayId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Finds all the interactive and activity children for an existing pathway and map those to an ordered list of
     * walkable children
     *
     * @param pathwayId the pathway to find the children for
     * @return a mono list of walkable children or a mono of empty list when no children are found
     */
    @Trace(async = true)
    public Mono<List<WalkableChild>> getOrderedWalkableChildren(final UUID pathwayId) {

        return pathwayGateway.findWalkableChildren(pathwayId)
                .map(Walkables::toList)
                .defaultIfEmpty(Lists.newArrayList())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * verifies whether walkable children exist for provided pathway id
     *
     *
     * @param pathwayId the pathway to find the children for
     * @return either a Mono of Boolean.TRUE if UUIDs exist, Boolean.FALSE if none are found
     */
    @Trace(async = true)
    public Mono<Boolean> hasOrderedWalkableChildren(final UUID pathwayId) {
        checkArgument(pathwayId != null, "missing pathway id");
        return pathwayGateway.findWalkableChildren(pathwayId)
                .map(Walkables::toList)
                .doOnEach(ReactiveTransaction.linkOnNext()).hasElement();
    }

    /**
     * Duplicate pathway and add to the given activity
     *
     * @param pathway       the pathway to duplicate
     * @param newActivityId the new parent activity id
     * @return mono with new created pathway
     */
    @Trace(async = true)
    Mono<Pathway> duplicatePathway(final Pathway pathway, final UUID newActivityId) {
        UUID newPathwayId = UUIDs.timeBased();
        Pathway copy = pathwayBuilder //
                .build(pathway.getType(), newPathwayId, pathway.getPreloadPathway());

        return pathwayGateway.persist(copy, newActivityId)
                .then(Mono.just(copy))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Reorder walkables for the pathway. Validate that the provided list includes only walkables
     * that were previously included in the pathway
     *
     * @param pathwayId   the pathway id
     * @param walkableIds the new order for walkables
     */
    @Trace(async = true)
    public Flux<Void> reorder(final UUID pathwayId, final List<UUID> walkableIds) {
        affirmNotNull(pathwayId, "pathwayId is required");
        affirmArgumentNotNullOrEmpty(walkableIds, "walkableIds is required");

        return pathwayGateway.findWalkableChildren(pathwayId)
                .single()
                .onErrorResume(NoSuchElementException.class,
                        ex -> {
                            throw new IllegalArgumentFault("Pathway does not have walkables to reorder");
                        })
                .map(children -> {
                    affirmArgument(children.getWalkableIds().size() == walkableIds.size()
                            && Sets.newHashSet(children.getWalkableIds()).equals(Sets.newHashSet(walkableIds)), "Invalid walkables list");
                    return children;
                })
                .map(children -> children.setWalkableIds(walkableIds))
                .flatMapMany(pathwayGateway::persist)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Replace the configuration of a pathway with the supplied version
     *
     * @param pathwayId the pathway id to replace the config for
     * @param config    the configuration data
     * @return a mono of void
     */
    @Trace(async = true)
    public Mono<PathwayConfig> replaceConfig(final UUID pathwayId, final String config) {
        affirmArgument(pathwayId != null, "pathwayId is required");
        affirmArgument(config != null, "config is required");

        UUID changeId = UUIDs.timeBased();

        PathwayConfig replacement = new PathwayConfig()
                .setId(changeId)
                .setPathwayId(pathwayId)
                .setConfig(config);

        return pathwayGateway.persist(replacement)
                .singleOrEmpty()
                .thenReturn(replacement)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the latest configuration for a pathway. Not all pathways have config
     *
     * @param pathwayId the pathway id to find the latest configuration for
     * @return a mono of pathway config or empty stream when the config are not found, which is an acceptable use case
     */
    @Trace(async = true)
    public Mono<PathwayConfig> findLatestConfig(final UUID pathwayId) {
        affirmArgument(pathwayId != null, "pathwayId is required");

        return pathwayGateway.findLatestConfig(pathwayId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate the config for a pathway
     *
     * @param config    the config to duplicate
     * @param pathwayId the pathway to duplicate the config for
     * @param context   the duplication context that handles the ids replacement
     * @return a mono of pathway config
     */
    @Trace(async = true)
    public Mono<PathwayConfig> duplicateConfig(final String config, final UUID pathwayId, final DuplicationContext context) {
        PathwayConfig duplicate = new PathwayConfig()
                .setId(UUIDs.timeBased())
                .setPathwayId(pathwayId)
                .setConfig(context.replaceIds(config));

        return pathwayGateway.persist(duplicate)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .then(Mono.just(duplicate));
    }
}
