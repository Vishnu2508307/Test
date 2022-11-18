package com.smartsparrow.courseware.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.FeedbackGateway;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.data.InteractiveGateway;
import com.smartsparrow.courseware.data.PathwayGateway;
import com.smartsparrow.courseware.data.WalkablePathwayChildren;
import com.smartsparrow.courseware.lang.InteractiveAlreadyExistsFault;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.lang.ParentPathwayNotFoundException;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class InteractiveService {

    private final InteractiveGateway interactiveGateway;
    private final PluginService pluginService;
    private final PathwayService pathwayService;
    private final ComponentGateway componentGateway;
    private final FeedbackGateway feedbackGateway;
    private final CoursewareAssetService coursewareAssetService;
    private final PathwayGateway pathwayGateway;
    private final DocumentItemService documentItemService;
    private final CoursewareElementDescriptionService coursewareDescriptionService;

    @Inject
    public InteractiveService(InteractiveGateway interactiveGateway,
                              PluginService pluginService,
                              PathwayService pathwayService,
                              ComponentGateway componentGateway,
                              FeedbackGateway feedbackGateway,
                              CoursewareAssetService coursewareAssetService,
                              PathwayGateway pathwayGateway,
                              DocumentItemService documentItemService,
                              CoursewareElementDescriptionService coursewareDescriptionService) {
        this.interactiveGateway = interactiveGateway;
        this.pluginService = pluginService;
        this.pathwayService = pathwayService;
        this.componentGateway = componentGateway;
        this.feedbackGateway = feedbackGateway;
        this.coursewareAssetService = coursewareAssetService;
        this.pathwayGateway = pathwayGateway;
        this.documentItemService = documentItemService;
        this.coursewareDescriptionService = coursewareDescriptionService;
    }

    /**
     * Create an Interactive with the supplied parameters if it's not exists
     *
     * @param creatorId the account id of the user performing the creation
     * @param pathwayId the pathway the interactive should be added to
     * @param pluginId the plugin id
     * @param pluginVersionExpr the version expression used to resolve the plugin
     * @param interactiveId optional interactive id, if not supplied a new id will be created
     * @return the created Interactive
     * @throws PluginNotFoundFault if plugin doesn't exist
     * @throws VersionParserFault if plugin version doesn't exist
     * @throws InteractiveAlreadyExistsFault if provided interactive id already exists
     */
    @Trace(async = true)
    public Mono<Interactive> create(final UUID creatorId,
                                    final UUID pathwayId,
                                    final UUID pluginId,
                                    final String pluginVersionExpr,
                                    final UUID interactiveId) {

        checkArgument(creatorId != null, "missing account creator");
        checkArgument(pathwayId != null, "missing pathway id");
        checkArgument(pluginId != null, "missing plugin id");
        checkArgument(!Strings.isNullOrEmpty(pluginVersionExpr), "missing plugin version");

        // check it does not already exist
        return interactiveGateway.findById(interactiveId)
                .hasElement()
                //filter out the value
                .filter(hasElement -> !hasElement)
                .switchIfEmpty(Mono.error(new InteractiveAlreadyExistsFault(interactiveId)))
                .flatMap(interactiveIdNotFound ->{
                    return createInteractive(creatorId, pathwayId, pluginId,pluginVersionExpr,interactiveId);
                });
    }

    /**
     * This method is setting up the interactiveId when the Interactive created
     *
     * @param creatorId the account id of the user performing the creation
     * @param pathwayId the pathway the interactive should be added to
     * @param pluginId the plugin id
     * @param pluginVersionExpr the version expression used to resolve the plugin
     * @return the created Interactive
     * @throws PluginNotFoundFault if plugin doesn't exist
     * @throws VersionParserFault if plugin version doesn't exist
     * @throws InteractiveAlreadyExistsFault if provided interactive id already exists
     */
    @Trace(async = true)
    public Mono<Interactive> create(final UUID creatorId,
                                    final UUID pathwayId,
                                    final UUID pluginId,
                                    final String pluginVersionExpr) {

        checkArgument(creatorId != null, "missing account creator");
        checkArgument(pathwayId != null, "missing pathway id");
        checkArgument(pluginId != null, "missing plugin id");
        checkArgument(!Strings.isNullOrEmpty(pluginVersionExpr), "missing plugin version");

        return createInteractive(creatorId, pathwayId, pluginId,pluginVersionExpr,UUIDs.timeBased());

    }

    /**
     * Create an Interactive with the supplied parameters
     *
     * @param creatorId the account id of the user performing the creation
     * @param pathwayId the pathway the interactive should be added to
     * @param pluginId the plugin id
     * @param pluginVersionExpr the version expression used to resolve the plugin
     * @param interactiveId optional interactive id, if not supplied a new id will be created
     * @return the created Interactive
     * @throws PluginNotFoundFault if plugin doesn't exist
     * @throws VersionParserFault if plugin version doesn't exist
     * @throws InteractiveAlreadyExistsFault if provided interactive id already exists
     */
    @Trace(async = true)
    public Mono<Interactive> createInteractive(final UUID creatorId,
                                    final UUID pathwayId,
                                    final UUID pluginId,
                                    final String pluginVersionExpr,
                                    final UUID interactiveId) {

        checkArgument(creatorId != null, "missing account creator");
        checkArgument(pathwayId != null, "missing pathway id");
        checkArgument(pluginId != null, "missing plugin id");
        checkArgument(!Strings.isNullOrEmpty(pluginVersionExpr), "missing plugin version");

        Mono<Interactive> interactiveMono = Mono.just(new Interactive() //
                .setEvaluationMode(EvaluationMode.DEFAULT)
                .setId(interactiveId) //
                .setPluginId(pluginId) //
                .setPluginVersionExpr(pluginVersionExpr)
                .setStudentScopeURN(UUIDs.timeBased()));

        Mono<String> ifPluginExistsMono = pluginService.findLatestVersion(pluginId, pluginVersionExpr);
        Mono<Pathway> ifPathwayExistsMono = pathwayService.findById(pathwayId);

        return ifPluginExistsMono
                .then(ifPathwayExistsMono)
                .then(interactiveMono)
                .flatMap(interactive ->
                                 interactiveGateway.persist(interactive)
                                         .thenMany(saveToPathway(interactive.getId(), pathwayId))
                                         .then(Mono.just(interactive)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Saves interactive configuration
     *
     * @param creatorId the id of an account performing a replacing
     * @param interactiveId the interactive id
     * @param config configuration
     * @throws InteractiveNotFoundException if an interactive is not found
     */
    @Trace(async = true)
    public Mono<InteractiveConfig> replaceConfig(final UUID creatorId, final UUID interactiveId, final String config) {

        // TODO: validate the config is valid.
        // TODO: validate the config matches the schema.

        checkArgument(creatorId != null, "missing account creator");
        checkArgument(interactiveId != null, "missing interactive id");

        UUID changeId = UUIDs.timeBased();

        InteractiveConfig replacement = new InteractiveConfig() //
                .setId(changeId) //
                .setInteractiveId(interactiveId) //
                .setConfig(config);

        Mono<Interactive> ifInteractiveExists = findById(interactiveId);

        return ifInteractiveExists.then(interactiveGateway.persist(replacement))
                .then(Mono.just(replacement))
                .doOnEach(ReactiveTransaction.linkOnNext());

        // TODO: record activity change in a LOG table using the changeId.
    }

    /**
     * Finds an Interactive by id
     *
     * @param interactiveId the interactive id to fetch
     * @return an Interactive object
     * @throws InteractiveNotFoundException if an interactive is not found
     */
    @Trace(async = true)
    public Mono<Interactive> findById(final UUID interactiveId) {
        checkArgument(interactiveId != null, "missing interactive id");
        return interactiveGateway.findById(interactiveId)
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new InteractiveNotFoundException(interactiveId);
                });
    }

    /**
     * Finds the latest configuration for the interactive
     *
     * @param interactiveId the interactive id
     * @return
     */
    @Trace(async = true)
    public Mono<InteractiveConfig> findLatestConfig(final UUID interactiveId) {
        checkArgument(interactiveId != null, "missing interactive id");
        return interactiveGateway.findLatestConfig(interactiveId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate an interactive itself
     *
     * @param interactive the interactive to duplicate
     * @return mono with new created interactive
     */
    @Trace(async = true)
    Mono<Interactive> duplicateInteractive(final Interactive interactive) {

        Interactive copy = new Interactive() //
                .setEvaluationMode(interactive.getEvaluationMode())
                .setId(UUIDs.timeBased()) //
                .setPluginId(interactive.getPluginId()) //
                .setPluginVersionExpr(interactive.getPluginVersionExpr())
                .setStudentScopeURN(UUIDs.timeBased());

        return interactiveGateway.persist(copy)
                .thenReturn(copy)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate the latest interactive config if exists. Replace old ids with new ids
     *
     * @param oldInteractiveId the interactive which config should be copied from
     * @param newInteractiveId the interactive which config should be copied to
     * @param context keeps the mapping between old and new ids
     * @return Mono with duplicated config or empty mono
     */
    @Trace(async = true)
    Mono<InteractiveConfig> duplicateInteractiveConfig(final UUID oldInteractiveId, final UUID newInteractiveId, final DuplicationContext context) {
        return interactiveGateway.findLatestConfig(oldInteractiveId)
                .flatMap(c -> {
                    InteractiveConfig copy = new InteractiveConfig() //
                            .setId(UUIDs.timeBased()) //
                            .setInteractiveId(newInteractiveId) //
                            .setConfig(context.replaceIds(c.getConfig()));

                    return interactiveGateway.persist(copy)
                            .doOnEach(ReactiveTransaction.linkOnNext())
                            .thenReturn(copy);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find parent pathway id for a given interactive id
     *
     * @param interactiveId the id of the activity to search the parent pathway for
     * @throws ParentPathwayNotFoundException if the parent pathway not found for given interactive id
     */
    @Trace(async = true)
    public Mono<UUID> findParentPathwayId(final UUID interactiveId) {
        return interactiveGateway.findParent(interactiveId)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ParentPathwayNotFoundException(interactiveId);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the children component ids for an interactive.
     *
     * @param interactiveId the interactive to find the children components for
     * @return either a Mono List of UUIDs or an empty stream if none are found
     */
    @Trace(async = true)
    public Mono<List<UUID>> findChildComponentIds(final UUID interactiveId) {
        return componentGateway.findComponentIdsByInteractive(interactiveId).collectList()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * verifies if provided interactive id has children or not
     *
     * @param interactiveId the interactive to find the children components for
     * @return either a Mono of Boolean.TRUE if UUIDs exist, Boolean.FALSE if none are found
     */
    @Trace(async = true)
    public Mono<Boolean> hasChildComponentIds(final UUID interactiveId) {
        checkArgument(interactiveId != null, "missing interactive id");
        return componentGateway.findComponentIdsByInteractive(interactiveId)
                .doOnEach(ReactiveTransaction.linkOnNext()).hasElements();
    }

    /**
     * Save child/parent relationships for interactive and pathway. Insert interactive at the end of pathway
     *
     * @param interactiveId the interactive
     * @param parentPathwayId the parent pathway
     */
    public Flux<Void> saveToPathway(final UUID interactiveId, final UUID parentPathwayId) {
        checkArgument(interactiveId != null, "interactiveId is required");
        checkArgument(parentPathwayId != null, "parentPathwayId is required");

        return Flux.merge(
                interactiveGateway.persistParent(interactiveId, parentPathwayId),
                pathwayGateway.persistChild(interactiveId, CoursewareElementType.INTERACTIVE, parentPathwayId));
    }

    /**
     * Save child/parent relationships for interactive and pathway. Insert interactive at the index position of pathway
     *
     * @param interactiveId the interactive
     * @param parentPathwayId the parent pathway
     * @param index the index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Trace(async = true)
    public Flux<Void> saveToPathway(final UUID interactiveId, final UUID parentPathwayId, final int index) {
        checkArgument(interactiveId != null, "interactiveId is required");
        checkArgument(parentPathwayId != null, "parentPathwayId is required");
        checkArgument(index >= 0, "index should be >= 0");

        return Flux.merge(interactiveGateway.persistParent(interactiveId, parentPathwayId),
                pathwayGateway.findWalkableChildren(parentPathwayId)
                        .defaultIfEmpty(new WalkablePathwayChildren())
                        .map(children -> children.addWalkable(interactiveId, CoursewareElementType.INTERACTIVE.name(), index))
                        .flatMapMany(pathwayGateway::persist))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete an interactive by detaching it from its parent pathway id. The relationship is deleted, however the interactive
     * still exists in the interactive table.
     *
     * @param interactiveId the id of the interactive to delete
     * @param parentPathwayId the id of the parent pathway to detach the interactive from
     * @throws IllegalArgumentException when wither method argument is <code>null</code>
     */
    @Trace(async = true)
    public Mono<Void> delete(final UUID interactiveId, final UUID parentPathwayId) {
        checkArgument(interactiveId != null, "interactiveId is required");
        checkArgument(parentPathwayId != null, "parentPathwayId is required");

        return Flux.merge(
                interactiveGateway.removeParent(interactiveId),
                pathwayGateway.removeChild(interactiveId, CoursewareElementType.INTERACTIVE, parentPathwayId))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Builds a payload object for an interactive id
     *
     * @param interactiveId the interactive
     * @throws IllegalArgumentException if interactive id is not supplied
     * @throws PluginNotFoundFault if plugin id in interactive is not found
     * @throws ParentPathwayNotFoundException if supplied interactive has no parent pathway
     */
    @Trace(async = true)
    public Mono<InteractivePayload> getInteractivePayload(final UUID interactiveId) {
        checkArgument(interactiveId != null, "missing activity id");
        return findById(interactiveId)
                .flatMap(this::getInteractivePayload)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Builds a payload object for an interactive
     *
     * @param interactive the interactive
     * @throws IllegalArgumentException if interactive is not supplied
     * @throws PluginNotFoundFault if plugin id in interactive is not found
     * @throws ParentPathwayNotFoundException if supplied interactive has no parent pathway
     */
    @Trace(async = true)
    public Mono<InteractivePayload> getInteractivePayload(final Interactive interactive) {

        checkArgument(interactive != null, "missing interactive");

        Mono<PluginSummary> pluginSummary = pluginService.fetchById(interactive.getPluginId())
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    String errorMsg = String.format("Plugin id %s not found", interactive.getPluginId());
                    throw Exceptions.propagate(new PluginNotFoundFault(errorMsg));
                }).doOnEach(ReactiveTransaction.linkOnNext());

        Mono<InteractiveConfig> config = findLatestConfig(interactive.getId()).defaultIfEmpty(new InteractiveConfig());

        Mono<UUID> parentPathwayId = findParentPathwayId(interactive.getId());
        Mono<List<UUID>> componentIds = componentGateway.findComponentIdsByInteractive(interactive.getId()).collectList()
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<List<UUID>> feedbackIds = feedbackGateway.findByInteractive(interactive.getId()).defaultIfEmpty(Lists.newArrayList())
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<CoursewareElementDescription> descriptionMono = getElementDescriptionByInteractiveId(interactive.getId())
                .defaultIfEmpty(new CoursewareElementDescription())
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<List<PluginFilter>> pluginFilters = pluginService.fetchPluginFiltersByIdVersionExpr(interactive.getPluginId(), interactive.getPluginVersionExpr())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<InteractivePayload> payloadMono = Mono.zip(Mono.just(interactive), pluginSummary, config, parentPathwayId,
                                                        componentIds, feedbackIds, descriptionMono, pluginFilters)
                .map(t -> InteractivePayload.from(t.getT1(),
                                                  t.getT2(),
                                                  t.getT3(),
                                                  t.getT4(),
                                                  t.getT5(),
                                                  t.getT6(),
                                                  t.getT7(),
                                                  t.getT8()));

        payloadMono = payloadMono.flatMap(payload -> coursewareAssetService.getAssetPayloads(interactive.getId())
                .doOnSuccess(payload::setAssets)
                .thenReturn(payload)
                .doOnEach(ReactiveTransaction.linkOnNext()));

        payloadMono = payloadMono.flatMap(payload -> coursewareAssetService.fetchMathAssetsForElement(interactive.getId())
                .doOnSuccess(payload::setMathAssets)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .thenReturn(payload));

        payloadMono = payloadMono.flatMap(payload -> documentItemService.findAllLinked(interactive.getId())
                .map(DocumentItemPayload::from)
                .collectList()
                .doOnSuccess(payload::setLinkedDocumentItems)
                .thenReturn(payload)
                .doOnEach(ReactiveTransaction.linkOnNext()));
        return payloadMono;
    }

    /**
     * Get the descriptive json for an interactive
     *
     * @param interactiveId the interactive id
     * @return a mono of courseware element description or empty if none are found
     */
    @Trace(async = true)
    public Mono<CoursewareElementDescription> getElementDescriptionByInteractiveId(final UUID interactiveId) {
        affirmArgument(interactiveId != null, "interactiveId is required");
        return coursewareDescriptionService.fetchCoursewareDescriptionByElement(interactiveId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Move the interactive to another pathway. Insert interactive at the end of pathway.
     *
     * @param interactiveId the interactive id
     * @param destinationPathwayId the pathway to move the interactive to
     * @param parentPathwayId the old parent pathway id for an activity
     * @throws ParentPathwayNotFoundException if the parent pathway not found for the interactive id
     * @return a mono of InteractivePayload or empty mono
     */
    @Trace(async = true)
    public Mono<InteractivePayload> move(final UUID interactiveId,
                                         final UUID destinationPathwayId,
                                         final UUID parentPathwayId) {

        checkArgument(interactiveId != null, "interactiveId is required");
        checkArgument(destinationPathwayId != null, "destinationPathwayId is required");

        return  delete(interactiveId, parentPathwayId)
                .thenMany(saveToPathway(interactiveId, destinationPathwayId))
                .then(getInteractivePayload(interactiveId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Move the interactive to another pathway at the specified index.
     *
     * @param interactiveId the interactive id
     * @param destinationPathwayId the pathway to move the interactive to
     * @param index the index of pathway to move the interactive to
     * @param parentPathwayId the old parent pathway id for an activity
     * @throws IndexOutOfBoundsException if the index is out of range
     * @throws ParentPathwayNotFoundException if the parent pathway not found for the interactive id
     * @return a mono of InteractivePayload or empty mono
     */
    @Trace(async = true)
    public Mono<InteractivePayload> move(final UUID interactiveId,
                                         final UUID destinationPathwayId,
                                         final Integer index,
                                         final UUID parentPathwayId) {

        affirmArgument(interactiveId != null, "interactiveId is required");
        affirmArgument(destinationPathwayId != null, "destinationPathwayId is required");
        affirmArgument(index >= 0, "index should be >= 0");

        return delete(interactiveId, parentPathwayId)
                .thenMany(saveToPathway(interactiveId, destinationPathwayId, index))
                .then(getInteractivePayload(interactiveId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the interactive ids for plugin id.
     *
     * @param pluginId the plugin id to find the interactive ids for
     * @return either a Mono List of UUIDs or an empty stream if none are found
     */
    public Mono<List<UUID>> findInteractiveIds(final UUID pluginId) {
        checkArgument(pluginId != null, "plugin Id is required");
        return interactiveGateway.findInteractiveIdsByPluginId(pluginId).collectList();
    }

    /**
     * Update the evaluation mode for an interactive
     *
     * @param interactiveId the interactive to update the evaluation mode for
     * @param evaluationMode the evaluation mode value to update
     * @return a mono of void
     * @throws com.smartsparrow.exception.IllegalArgumentFault when any of the method arguments is null
     */
    public Mono<Void> updateEvaluationMode(final UUID interactiveId, final EvaluationMode evaluationMode) {
        affirmArgument(interactiveId != null, "interactiveId is required");
        affirmArgument(evaluationMode != null, "evaluationMode is required");
        return interactiveGateway.updateEvaluationMode(interactiveId, evaluationMode);
    }
}
