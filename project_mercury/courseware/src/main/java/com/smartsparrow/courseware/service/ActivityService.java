package com.smartsparrow.courseware.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityChange;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityGateway;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.ActivityThemeGateway;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.DeletedActivity;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.PathwayGateway;
import com.smartsparrow.courseware.data.WalkablePathwayChildren;
import com.smartsparrow.courseware.lang.ActivityAlreadyExistsFault;
import com.smartsparrow.courseware.lang.ActivityChangeNotFoundException;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.ActivityByWorkspace;
import com.smartsparrow.workspace.data.IconLibrary;
import com.smartsparrow.workspace.data.ProjectActivity;
import com.smartsparrow.workspace.data.ProjectGateway;
import com.smartsparrow.workspace.data.ThemePayload;
import com.smartsparrow.workspace.data.WorkspaceGateway;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Operations related to an Activity.
 */
@Singleton
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    private final ActivityGateway activityGateway;
    private final PluginService pluginService;
    private final WorkspaceGateway workspaceGateway;
    private final AccountService accountService;
    private final ActivityThemeGateway activityThemeGateway;
    private final ComponentGateway componentGateway;
    private final PathwayGateway pathwayGateway;
    private final CoursewareAssetService coursewareAssetService;
    private final DocumentItemService documentItemService;
    private final ProjectGateway projectGateway;
    private final CoursewareElementDescriptionService coursewareDescriptionService;
    private final ThemeService themeService;
    private final AnnotationService annotationService;

    @Inject
    public ActivityService(final ActivityGateway activityGateway,
                           final PluginService pluginService,
                           final WorkspaceGateway workspaceGateway,
                           final AccountService accountService,
                           final ActivityThemeGateway activityThemeGateway,
                           final ComponentGateway componentGateway,
                           final PathwayGateway pathwayGateway,
                           final CoursewareAssetService coursewareAssetService,
                           final DocumentItemService documentItemService,
                           final ProjectGateway projectGateway,
                           final CoursewareElementDescriptionService coursewareDescriptionService,
                           final ThemeService themeService,
                           final AnnotationService annotationService) {
        this.activityGateway = activityGateway;
        this.pluginService = pluginService;
        this.workspaceGateway = workspaceGateway;
        this.accountService = accountService;
        this.activityThemeGateway = activityThemeGateway;
        this.componentGateway = componentGateway;
        this.pathwayGateway = pathwayGateway;
        this.coursewareAssetService = coursewareAssetService;
        this.documentItemService = documentItemService;
        this.projectGateway = projectGateway;
        this.coursewareDescriptionService = coursewareDescriptionService;
        this.themeService = themeService;
        this.annotationService = annotationService;
    }

    /**
     * Create a top level activity
     *
     * @param creatorId         the account id of the user performing the creation
     * @param pluginId          the plugin id
     * @param pluginVersionExpr the version expression used to resolve the plugin
     * @param activityId        optional activity id, if not supplied a new id will be created
     * @return the created Activity
     * @throws VersionParserFault  if {@param pluginVersionExpr} can't be parsed to version string
     * @throws PluginNotFoundFault if plugin doesn't exist
     * @throws ActivityAlreadyExistsFault if provided activityId already exists
     */
    @Trace(async = true)
    public Mono<Activity> create(final UUID creatorId, final UUID pluginId, final String pluginVersionExpr, @Nullable final UUID activityId)
            throws VersionParserFault, PluginNotFoundFault {

        checkArgument(creatorId != null, "missing account creator");
        checkArgument(pluginId != null, "missing plugin id");
        checkArgument(!Strings.isNullOrEmpty(pluginVersionExpr), "missing plugin version");

        // If a activity id has been supplied
        Mono<UUID> idMono = activityId != null ?
                // check it does not already exist
                activityGateway.findById(activityId)
                        .hasElement()
                        .handle((hasElement, sink) -> {
                            if (hasElement) {
                                sink.error(new ActivityAlreadyExistsFault(activityId));
                            } else {
                                sink.next(activityId);
                            }
                        })
                // create a new id otherwise
                : Mono.just(UUIDs.timeBased());

        // prep object
        Mono<Activity> activityMono = idMono.map(id -> new Activity()
                .setId(id) //
                .setEvaluationMode(EvaluationMode.DEFAULT)
                .setPluginId(pluginId) //
                .setPluginVersionExpr(pluginVersionExpr)
                .setCreatorId(creatorId)
                .setStudentScopeURN(UUIDs.timeBased()));

        // Verify plugin exists, will throw PluginNotFoundFault if cant find id and version
        return activityMono.flatMap(activity ->
                pluginService.findLatestVersion(pluginId, pluginVersionExpr)
                // compose the persist call
                .thenEmpty(activityGateway.persist(activity))
                // return the activity
                .thenReturn(activity))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Create an activity that should be linked to an existing parent pathway id.
     *
     * @param creatorId         the account id of the user performing the creation
     * @param pluginId          the plugin id
     * @param parentPathwayId   the parent pathway id
     * @param pluginVersionExpr the version expression used to resolve the plugin
     * @param activityId        optional activity id, if not supplied a new id will be created
     * @return the created Activity
     * @throws VersionParserFault  if {@param pluginVersionExpr} can't be parsed to version string
     * @throws PluginNotFoundFault if plugin doesn't exist
     * @throws ActivityAlreadyExistsFault if provided activityId already exists
     */
    @Trace(async = true)
    public Mono<Activity> create(final UUID creatorId, final UUID pluginId, @Nonnull final UUID parentPathwayId,
                                 final String pluginVersionExpr, @Nullable final UUID activityId) {

        return create(creatorId, pluginId, pluginVersionExpr, activityId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMap(activity -> saveRelationship(activity.getId(), parentPathwayId)
                        .singleOrEmpty()
                        .thenReturn(activity));
    }

    /**
     * Save the child-parent relationship for an activity. Insert activity at the end of pathway.
     *
     * @param activityId      the child activity
     * @param parentPathwayId the parent pathway
     */
    @Trace(async = true)
    public Flux<Void> saveRelationship(final UUID activityId, final UUID parentPathwayId) {
        checkArgument(activityId != null, "missing activityId");
        checkArgument(parentPathwayId != null, "missing parentPathwayId");

        return Flux.merge(
                activityGateway.persistParent(activityId, parentPathwayId),
                pathwayGateway.persistChild(activityId, CoursewareElementType.ACTIVITY, parentPathwayId)
        ).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the child-parent relationship for an activity. Insert activity at the index in the pathway.
     *
     * @param activityId      the activity to save as a child
     * @param parentPathwayId the pathway to save activity for
     * @param index           the index at which the specified activity insert to
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Trace(async = true)
    public Flux<Void> saveRelationship(final UUID activityId, final UUID parentPathwayId, final int index) {
        checkArgument(activityId != null, "missing activityId");
        checkArgument(parentPathwayId != null, "missing parentPathwayId");
        checkArgument(index >= 0, "index should be positive or zero");

        return Flux.merge(activityGateway.persistParent(activityId, parentPathwayId),
                pathwayGateway.findWalkableChildren(parentPathwayId)
                        .defaultIfEmpty(new WalkablePathwayChildren())
                        .map(children -> children.addWalkable(activityId, CoursewareElementType.ACTIVITY.name(), index))
                        .flatMapMany(pathwayGateway::persist))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate an activity.
     *
     * @param activity  the activity to duplicate
     * @param accountId the creator
     * @return a mono containing the duplicated activity
     * @throws VersionParserFault  if {@link Activity#getPluginVersionExpr()} can't be parsed to version string
     * @throws PluginNotFoundFault if plugin doesn't exist
     */
    @Trace(async = true)
    Mono<Activity> duplicateActivity(final Activity activity, final UUID accountId) {
        Activity duplicate = new Activity()
                .setEvaluationMode(activity.getEvaluationMode())
                .setId(UUIDs.timeBased())
                .setCreatorId(accountId)
                .setPluginId(activity.getPluginId())
                .setPluginVersionExpr(activity.getPluginVersionExpr())
                .setStudentScopeURN(UUIDs.timeBased());

        // Verify plugin exists, will throw PluginNotFoundFault if cant find id and version
        return pluginService.findLatestVersion(activity.getPluginId(), activity.getPluginVersionExpr())
                // compose the persist call
                .thenEmpty(activityGateway.persist(duplicate))
                // return the activity
                .thenReturn(duplicate)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find an Activity by id.
     *
     * @param activityId the activity id
     * @return an Activity
     * @throws ActivityNotFoundException if no activity found for the given id
     */
    @Trace(async = true)
    public Mono<Activity> findById(final UUID activityId) {
        checkArgument(activityId != null, "missing activity id");

        return activityGateway.findById(activityId).single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ActivityNotFoundException(activityId);
                });
    }

    /**
     * Replace the configuration of an Activity with the supplied version. (Note that the <code>creatorId</code>
     * argument is currently not used).
     *
     * @param creatorId  the account id of the user performing the action
     * @param activityId the activity id
     * @param config     the configuration data
     */
    @Trace(async = true)
    public Mono<Void> replaceConfig(final UUID creatorId, final UUID activityId, final String config) {

        checkArgument(creatorId != null, "missing account creator");
        checkArgument(activityId != null, "missing activity id");

        UUID changeId = UUIDs.timeBased();

        ActivityConfig replacement = new ActivityConfig() //
                .setId(changeId) //
                .setActivityId(activityId) //
                .setConfig(config);
        return activityGateway.persist(replacement)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .singleOrEmpty();

        // TODO: record activity change in a LOG table using the changeId.
    }

    /**
     * Duplicate the config for an activity. This method is the same as {@link ActivityService#replaceConfig} except
     * it does not require a creatorId.
     *
     * @param config     the config to duplicate
     * @param activityId the activity to attach the config to
     * @return a mono of activity config
     */
    @Trace(async = true)
    Mono<ActivityConfig> duplicateConfig(final String config, final UUID activityId, final DuplicationContext context) {
        ActivityConfig duplicate = new ActivityConfig() //
                .setId(UUIDs.timeBased()) //
                .setActivityId(activityId) //
                .setConfig(context.replaceIds(config));

        return activityGateway.persist(duplicate)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .then(Mono.just(duplicate));

    }

    /**
     * Find the latest configuration for an activity
     *
     * @param activityId the activity id
     * @return the latest config for an activity
     */
    @Trace(async = true)
    public Mono<ActivityConfig> findLatestConfig(final UUID activityId) {
        checkArgument(activityId != null, "missing activity id");

        return activityGateway.findLatestConfig(activityId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Add activity to workspace. This method has been deprecated and it will be removed.
     * An activity should only be concerned to know about a parent project and not care about workspaces anymore
     *
     * @param activityId  the activity id
     * @param workspaceId the workspace id
     */
    @Deprecated
    public Mono<Void> addToWorkspace(final UUID activityId, final UUID workspaceId) {
        checkArgument(activityId != null, "missing activity id");
        checkArgument(workspaceId != null, "missing workspace id");

        return workspaceGateway.persist(new ActivityByWorkspace()
                .setActivityId(activityId)
                .setWorkspaceId(workspaceId))
                .singleOrEmpty();
    }

    /**
     * Add an activity to a project
     *
     * @param activityId the activity id to add
     * @param projectId  the project id the activity will belong to
     * @return a mono of void
     */
    public Mono<Void> addToProject(final UUID activityId, final UUID projectId) {
        affirmArgument(activityId != null, "missing activityId");
        affirmArgument(projectId != null, "missing projectId");

        return projectGateway.persist(new ProjectActivity()
                .setActivityId(activityId)
                .setProjectId(projectId))
                .singleOrEmpty();
    }

    /**
     * Delete a root level activity from a project
     *
     * @param activityId the id of the activity to delete
     * @param projectId  the project the activity should be removed from
     * @return a flux of void
     */
    public Flux<Void> deleteFromProject(final UUID activityId, final UUID projectId, final UUID accountId) {
        affirmArgument(activityId != null, "activityId is missing");
        affirmArgument(projectId != null, "projectId is missing");
        affirmArgument(accountId != null, "accountId is missing");

        return Flux.merge(projectGateway.delete(new ProjectActivity()
                                             .setProjectId(projectId)
                                             .setActivityId(activityId)),
                annotationService.deleteAnnotationByRootElementId(activityId),
                activityGateway.persist(new DeletedActivity()
                        .setActivityId(activityId)
                        .setAccountId(accountId)
                        .setDeletedAt(DateFormat.asRFC1123(UUIDs.timeBased()))));
    }

    /**
     * Fetches deleted activity info by activity id
     *
     * @param activityId the workspace id
     */
    @Trace(async = true)
    public Mono<DeletedActivity> fetchDeletedActivityById(final UUID activityId) {
        checkArgument(activityId != null, "activityId is required");
        return activityGateway.findDeletedActivityById(activityId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Builds a payload object for an activity. The parentPathwayId nullable field is set only if found
     *
     * @param activity the activity
     */
    @SuppressWarnings("unchecked")
    @Trace(async = true)
    public Mono<ActivityPayload> getActivityPayload(final Activity activity) {
        checkArgument(activity != null, "missing activity");

        Mono<ActivityConfig> config = findLatestConfig(activity.getId())
                .defaultIfEmpty(new ActivityConfig())
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<PluginSummary> plugin = pluginService.fetchById(activity.getPluginId())
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new PluginNotFoundFault(activity.getPluginId());
                });
        Mono<AccountPayload> creator = accountService.getAccountPayload(activity.getCreatorId())
                .defaultIfEmpty(new AccountPayload())
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<ActivityTheme> defaultTheme = getLatestActivityThemeByActivityId(activity.getId())
                .defaultIfEmpty(new ActivityTheme())
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<List<UUID>> childrenPathwayIds = activityGateway.findChildPathwayIds(activity.getId())
                .defaultIfEmpty(new ArrayList<>())
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<List<UUID>> componentIds = componentGateway.findComponentIdsByActivity(activity.getId())
                .collectList()
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<CoursewareElementDescription> elementDescriptionMono = getElementDescriptionByActivityId(activity.getId())
                .defaultIfEmpty(new CoursewareElementDescription())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<List<PluginFilter>> pluginFilters = pluginService.fetchPluginFiltersByIdVersionExpr(activity.getPluginId(),
                                                                                                 activity.getPluginVersionExpr());

        Mono<ThemePayload> themePayloadMono = themeService.fetchThemeByElementId(activity.getId())
                .defaultIfEmpty(new ThemePayload());

        Mono<List<IconLibrary>> activityThemeIconLibraryMono = themeService.fetchActivityThemeIconLibraries(
                activity.getId())
                .collectList()
                .defaultIfEmpty(Collections.emptyList())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<ActivityPayload> activityPayload = Mono.zip(objects -> {
                                                             ActivityPayload payload = ActivityPayload.from(
                                                                     (Activity) objects[0],
                                                                     (ActivityConfig) objects[1],
                                                                     (PluginSummary) objects[2],
                                                                     (AccountPayload) objects[3],
                                                                     (ActivityTheme) objects[4],
                                                                     (List<UUID>) objects[5],
                                                                     (List<UUID>) objects[6],
                                                                     (CoursewareElementDescription) objects[7],
                                                                     (List<PluginFilter>) objects[8],
                                                                     (ThemePayload) objects[9],
                                                                     (List<IconLibrary>) objects[10]
                                                             );
                                                             return activityGateway.findParentPathwayId(activity.getId())
                                                                     .doOnSuccess(payload::setParentPathwayId)
                                                                     .thenReturn(payload);
                                                         },
                                                         Mono.just(activity),
                                                         config,
                                                         plugin,
                                                         creator,
                                                         defaultTheme,
                                                         childrenPathwayIds,
                                                         componentIds,
                                                         elementDescriptionMono,
                                                         pluginFilters,
                                                         themePayloadMono,
                                                         activityThemeIconLibraryMono)
                .flatMap(one -> one)
                .doOnEach(ReactiveTransaction.linkOnNext());

        activityPayload = activityPayload.flatMap(payload -> coursewareAssetService.getAssetPayloads(activity.getId())
                .doOnSuccess(payload::setAssets)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .thenReturn(payload));

        activityPayload = activityPayload.flatMap(payload -> coursewareAssetService.fetchMathAssetsForElement(activity.getId())
                .doOnSuccess(payload::setMathAssets)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .thenReturn(payload));

        activityPayload = activityPayload.flatMap(payload -> documentItemService.findAllLinked(activity.getId())
                .map(DocumentItemPayload::from)
                .collectList()
                .doOnSuccess(payload::setLinkedDocumentItems)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .thenReturn(payload));

        return activityPayload;
    }

    /**
     * Get the descriptive json for an activity
     *
     * @param activityId the activity id
     * @return a mono of courseware element description or empty if none are found
     */
    @Trace(async = true)
    public Mono<CoursewareElementDescription> getElementDescriptionByActivityId(final UUID activityId) {
        affirmArgument(activityId != null, "activityId is required");
        return coursewareDescriptionService.fetchCoursewareDescriptionByElement(activityId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find parent pathway id for a given activity id
     *
     * @param activityId the id of the activity to search the parent pathway for
     */
    @Trace(async = true)
    public Mono<UUID> findParentPathwayId(final UUID activityId) {
        return activityGateway.findParentPathwayId(activityId)
                              .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Detach the activity from its from its parent pathway id. The relationships are delete, however the activity
     * still exists in the activity table and is not marked as deleted.
     *
     * @param activityId      the id of the activity to delete
     * @param parentPathwayId the id of the parent pathway to detach the activity from
     * @throws IllegalArgumentException when wither method argument is <code>null</code>
     */
    @Trace(async = true)
    public Flux<Void> detach(final UUID activityId, final UUID parentPathwayId) {
        checkArgument(activityId != null, "activityId is required");
        checkArgument(parentPathwayId != null, "parentPathwayId is required");

        return Flux.merge(
                activityGateway.removeParent(activityId),
                pathwayGateway.removeChild(activityId, CoursewareElementType.ACTIVITY, parentPathwayId)
        ).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete an activity by detaching it from its parent pathway id. The relationships are deleted and the activity is
     * marked as deleted, however the activity still exists in the activity table.
     *
     * @param activityId      the id of the activity to delete
     * @param parentPathwayId the id of the parent pathway to detach the activity from
     * @param accountId       the id of the account which deleted this activity
     * @throws IllegalArgumentException when wither method argument is <code>null</code>
     */
    @Trace(async = true)
    public Flux<Void> delete(final UUID activityId, final UUID parentPathwayId, final UUID accountId) {
        checkArgument(activityId != null, "activityId is required");
        checkArgument(parentPathwayId != null, "parentPathwayId is required");
        checkArgument(accountId != null, "accountId is required");

        return Flux.merge(
                activityGateway.removeParent(activityId),
                pathwayGateway.removeChild(activityId, CoursewareElementType.ACTIVITY, parentPathwayId),
                activityGateway.persist(new DeletedActivity()
                        .setActivityId(activityId)
                        .setAccountId(accountId)
                        .setDeletedAt(DateFormat.asRFC1123(UUIDs.timeBased())))
                )
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Builds a payload object for an activity
     *
     * @param activityId the activity id
     * @throws ActivityNotFoundException if no activity found for the given id
     */
    @Trace(async = true)
    public Mono<ActivityPayload> getActivityPayload(final UUID activityId) {
        checkArgument(activityId != null, "missing activity id");

        return findById(activityId)
                .flatMap(this::getActivityPayload)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Method to replace and activity theme config
     *
     * @param activityId
     * @param config
     */
    @Trace(async = true)
    public Mono<ActivityTheme> replaceActivityThemeConfig(final UUID activityId, final String config) {
        if (log.isDebugEnabled()) {
            log.debug("Replace ActivityThemeConfig with activity_id {} and config {}", activityId, config);
        }
        checkArgument(activityId != null, "activity Id is required");

        UUID activityThemeId = UUIDs.timeBased();
        ActivityTheme activityTheme = new ActivityTheme()
                .setId(activityThemeId)
                .setActivityId(activityId)
                .setConfig(config);

        return findById(activityId)
                .thenEmpty(activityThemeGateway.persist(activityTheme))
                .thenReturn(activityTheme)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate an activity theme.
     *
     * @param config     the theme config to duplicate
     * @param activityId the activity id to attach the theme config to
     * @return a mono of activity theme
     */
    @Trace(async = true)
    public Mono<ActivityTheme> duplicateTheme(final String config, final UUID activityId) {
        ActivityTheme duplicate = new ActivityTheme()
                .setId(UUIDs.timeBased())
                .setActivityId(activityId)
                .setConfig(config);

        return activityThemeGateway.persist(duplicate)
                .thenReturn(duplicate)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Get the latest activity theme for an activity
     *
     * @param activityId
     * @return
     */
    @Trace(async = true)
    public Mono<ActivityTheme> getLatestActivityThemeByActivityId(final UUID activityId) {

        checkArgument(activityId != null, "activity Id is required");
        return activityThemeGateway.findLatestByActivityId(activityId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the children pathway ids for an activity.
     *
     * @param activityId the activity to find the children pathways for
     * @return either a Mono List of UUIDs or an empty stream if none are found
     */
    @Trace(async = true)
    public Mono<List<UUID>> findChildPathwayIds(final UUID activityId) {
        checkArgument(activityId != null, "activity Id is required");
        return activityGateway.findChildPathwayIds(activityId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Verifies whether provided activity id has child pathways or not
     *
     * @param activityId the activity to find the children pathways for
     * @return either a Mono of Boolean.TRUE if UUIDs exist, Boolean.FALSE if none are found
     */
    @Trace(async = true)
    public Mono<Boolean> hasChildPathwayIds(final UUID activityId) {
        checkArgument(activityId != null, "activity Id is required");
        return activityGateway.findChildPathwayIds(activityId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .hasElement();
    }

    /**
     * Find all the children component ids for an activity.
     *
     * @param activityId the activity to find the children components for
     * @return either a Mono List of UUIDs or an empty stream if none are found
     */
    @Trace(async = true)
    public Mono<List<UUID>> findChildComponentIds(final UUID activityId) {
        checkArgument(activityId != null, "activity Id is required");
        return componentGateway.findComponentIdsByActivity(activityId).collectList()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a workspace id for the the activity
     *
     * @param activityId the activity id
     * @return a mono with workspace id or empty mono if activity is not connected to a workspace
     */
    public Mono<UUID> findWorkspaceIdByActivity(final UUID activityId) {
        checkArgument(activityId != null, "activityId is required");
        return workspaceGateway.findByActivityId(activityId);
    }

    /**
     * Find a project the activity belongs to
     *
     * @param activityId the activity id to find the project for
     * @return a mono of project activity
     */
    @Trace(async = true)
    public Mono<ProjectActivity> findProjectIdByActivity(final UUID activityId) {
        affirmArgument(activityId != null, "activityId is missing");
        return projectGateway.findProjectId(activityId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Create a change id and save it to the database
     *
     * @param activityId the activity id to save the change entry for
     * @return a mono of activity change
     */
    public Mono<ActivityChange> saveChange(final UUID activityId) {

        checkArgument(activityId != null, "activityId is required");

        ActivityChange activityChange = new ActivityChange()
                .setActivityId(activityId)
                .setChangeId(UUIDs.timeBased());

        return activityGateway.persistChange(activityChange)
                .singleOrEmpty()
                .thenReturn(activityChange);
    }

    /**
     * Find the latest change id for a given activity
     *
     * @param activityId the activity id to find the change for
     * @return a mono of activity change object
     * @throws ActivityChangeNotFoundException when no changes are found for the activity
     */
    public Mono<ActivityChange> fetchLatestChange(final UUID activityId) {

        checkArgument(activityId != null, "activityId is required");

        return activityGateway.findLatestActivityChange(activityId)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    if (log.isDebugEnabled()) {
                        log.debug(ex.getMessage(), ex);
                    }
                    throw new ActivityChangeNotFoundException(activityId);
                });
    }

    /**
     * Find the latest configuration id for an activity
     *
     * @param activityId the activity id
     * @return the latest config for an activity
     */
    @Trace(async = true)
    public Mono<UUID> findLatestConfigId(final UUID activityId) {
        checkArgument(activityId != null, "missing activity id");
        return activityGateway.findLatestConfigId(activityId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Move an activity to another pathway. Insert activity at the end of destination pathway.
     *
     * @param activityId the activity id
     * @param destinationPathwayId the destination pathway id
     * @param parentPathwayId the old parent pathway id for an activity
     * @return the mono of activity payload
     * @throws ActivityNotFoundException if no activity found for the given id
     */
    @Trace(async = true)
    public Mono<ActivityPayload> move(final UUID activityId, final UUID destinationPathwayId,
                                      final UUID parentPathwayId) {

        checkArgument(activityId != null, "activityId is required");
        checkArgument(destinationPathwayId != null, "destinationPathwayId is required");
        affirmArgument(parentPathwayId != null, "parentPathwayId is required");

        return detach(activityId, parentPathwayId)
                .thenMany(saveRelationship(activityId, destinationPathwayId))
                .then(getActivityPayload(activityId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Move an activity to another pathway at the specified index provided.
     *
     * @param activityId the activity id to move
     * @param destinationPathwayId the pathway to move activity to
     * @param index the index at which the specified activity moved to
     * @param parentPathwayId the old parent pathway id for an activity
     * @return the mono of activity payload
     * @throws IndexOutOfBoundsException if the index is out of range
     * @throws ActivityNotFoundException if no activity found for the given id
     */
    @Trace(async = true)
    public Mono<ActivityPayload> move(final UUID activityId, final UUID destinationPathwayId, final Integer index,
                                      final UUID parentPathwayId) {

        affirmArgument(activityId != null, "activityId is required");
        affirmArgument(destinationPathwayId != null, "destinationPathwayId is required");
        affirmArgument(index >= 0, "index should be positive or zero");
        affirmArgument(parentPathwayId != null, "parentPathwayId is required");

        return detach(activityId, parentPathwayId)
                .thenMany(saveRelationship(activityId, destinationPathwayId, index))
                .then(getActivityPayload(activityId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the activity ids for plugin id.
     *
     * @param pluginId the plugin id to find the activity ids for
     * @return either a Mono List of UUIDs or an empty stream if none are found
     */
    public Mono<List<UUID>> findActivityIds(final UUID pluginId) {
        checkArgument(pluginId != null, "plugin Id is required");
        return activityGateway.findActivityIdsByPluginId(pluginId).collectList();
    }

    /**
     * Update the evaluation mode for an activity
     *
     * @param activityId the activity to update the evaluation mode for
     * @param evaluationMode the evaluation mode value to update
     * @return a mono of void
     * @throws com.smartsparrow.exception.IllegalArgumentFault when any of the method arguments is null
     */
    public Mono<Void> updateEvaluationMode(final UUID activityId, final EvaluationMode evaluationMode) {
        affirmArgument(activityId != null, "activityId is required");
        affirmArgument(evaluationMode != null, "evaluationMode is required");
        return activityGateway.updateEvaluationMode(activityId, evaluationMode);
    }

    /**
     * Check if the project of the duplicated course (or lesson) equals to the destination project or not.
     *
     * @param activityId            the duplicated activity id (course or lesson)
     * @param destinationProjectId  the destination project id
     * @param newDuplicateFlow      feature flag control by launchDarkly
     * @return a mono of boolean True if the project of the duplicated activity equals to the destination project. Otherwise, false
     * @throws NotFoundFault if a project id is not found for the activity id
     */
    public Mono<Boolean> isDuplicatedCourseInTheSameProject(final UUID activityId, final UUID destinationProjectId, final Boolean newDuplicateFlow) {
        affirmArgument(activityId != null, "activityId is required");
        affirmArgument(destinationProjectId != null, "destinationProjectId is required");

        // current duplicated flow is not checking if the project of the duplicated activity equals to the destination
        // project, and it does not generate new asset ids for the duplicated course.
        // Return true, so no new asset ids will be generated for duplicated assets
        if(!newDuplicateFlow){
            return Mono.just(true);
        }

        // new flow: checking if the project of the duplicated activity equals to the destination project
        return findProjectIdByActivity(activityId)
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find a project id by activity id: %s", activityId))))
                .map(projectActivity -> {
                    if (projectActivity.getProjectId().equals(destinationProjectId)) {
                        return true;
                    }
                    return false;
                });
    }

    /**
     * Check if the original activity and new activity are in the same project or not
     *
     * @param activityId            the original activity id
     * @param newActivityId         the new activity id
     * @param newDuplicateFlow      feature flag control by launchDarkly
     * @return a mono of boolean True if original and new activities are in the same project. Otherwise, false
     * @throws NotFoundFault if a project id is not found for the activity id
     */
    public Mono<Boolean> isDuplicatedActivityInTheSameProject(final UUID activityId, final UUID newActivityId, final Boolean newDuplicateFlow) {
        affirmArgument(activityId != null, "activityId is required");
        affirmArgument(newActivityId != null, "newActivityId is required");

        // current duplicated flow is not checking if the original activity and the new activity are in the same project
        // or not, and it does not generate new asset ids for the duplicated activity.
        // Return true, so no new asset ids will be generated for duplicated assets
        if(!newDuplicateFlow){
            return Mono.just(true);
        }

        Mono<ProjectActivity> projectIdByActivity = findProjectIdByActivity(activityId)
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find a project id by activity id: %s", activityId))));

        Mono<ProjectActivity> newProjectIdByActivity = findProjectIdByActivity(newActivityId)
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find a project id by activity id: %s", newActivityId))));

        return Mono.zip(projectIdByActivity, newProjectIdByActivity)
                .map(tuple2 ->{
                    if(tuple2.getT1().getProjectId().equals(tuple2.getT2().getProjectId())){
                        return true;
                    }
                    return false;
                });
    }

}

