package com.smartsparrow.courseware.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.json.JSONException;

import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.competency.service.DocumentItemLinkService;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityChange;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.ComponentConfig;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementAncestry;
import com.smartsparrow.courseware.data.CoursewareElementConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.CoursewareGateway;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.data.PluginReference;
import com.smartsparrow.courseware.data.RegisteredScopeReference;
import com.smartsparrow.courseware.data.ScopeReference;
import com.smartsparrow.courseware.lang.ActivityChangeNotFoundException;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.lang.ComponentNotFoundException;
import com.smartsparrow.courseware.lang.ComponentParentNotFound;
import com.smartsparrow.courseware.lang.CoursewareElementNotFoundFault;
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.lang.ParentActivityNotFoundException;
import com.smartsparrow.courseware.lang.ParentPathwayNotFoundException;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.lang.PluginReferenceNotFoundFault;
import com.smartsparrow.courseware.lang.ScenarioNotFoundException;
import com.smartsparrow.courseware.lang.ScenarioParentNotFoundException;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.learner.service.CoursewareElementMetaInformationService;
import com.smartsparrow.learner.service.ManualGradeDuplicationService;
import com.smartsparrow.plugin.data.PluginGateway;
import com.smartsparrow.plugin.data.PluginManifest;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.Project;
import com.smartsparrow.workspace.data.ProjectActivity;
import com.smartsparrow.workspace.service.ProjectService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CoursewareService {

    private final ActivityService activityService;
    private final PathwayService pathwayService;
    private final InteractiveService interactiveService;
    private final ScenarioService scenarioService;
    private final ComponentGateway componentGateway;
    private final ComponentService componentService;
    private final FeedbackService feedbackService;
    private final CoursewareAssetService coursewareAssetService;
    private final CoursewareGateway coursewareGateway;
    private final DocumentItemLinkService documentItemLinkService;
    private final ManualGradeDuplicationService manualGradeDuplicationService;
    private final CoursewareElementMetaInformationService coursewareElementMetaInformationService;
    private final AnnotationDuplicationService annotationDuplicationService;
    private final ProjectService projectService;
    private final PluginGateway pluginGateway;
    private final PluginService pluginService;
    private final ThemeService themeService;

    @Inject
    public CoursewareService(final ActivityService activityService,
                             final PathwayService pathwayService,
                             final InteractiveService interactiveService,
                             final ScenarioService scenarioService,
                             final ComponentGateway componentGateway,
                             final ComponentService componentService,
                             final FeedbackService feedbackService,
                             final CoursewareAssetService coursewareAssetService,
                             final CoursewareGateway coursewareGateway,
                             final DocumentItemLinkService documentItemLinkService,
                             final ManualGradeDuplicationService manualGradeDuplicationService,
                             final CoursewareElementMetaInformationService coursewareElementMetaInformationService,
                             final AnnotationDuplicationService annotationDuplicationService,
                             final ProjectService projectService,
                             final PluginGateway pluginGateway,
                             final PluginService pluginService,
                             final ThemeService themeService) {
        this.activityService = activityService;
        this.pathwayService = pathwayService;
        this.interactiveService = interactiveService;
        this.scenarioService = scenarioService;
        this.componentGateway = componentGateway;
        this.componentService = componentService;
        this.feedbackService = feedbackService;
        this.coursewareAssetService = coursewareAssetService;
        this.coursewareGateway = coursewareGateway;
        this.documentItemLinkService = documentItemLinkService;
        this.manualGradeDuplicationService = manualGradeDuplicationService;
        this.coursewareElementMetaInformationService = coursewareElementMetaInformationService;
        this.annotationDuplicationService = annotationDuplicationService;
        this.projectService = projectService;
        this.pluginGateway = pluginGateway;
        this.pluginService = pluginService;
        this.themeService = themeService;
    }

    /**
     * Returns the path from the most top activity to the given element.
     * The path is ordered: first element is the top activity (COURSE or LESSON) and the last element is the given element
     * Supported element types: ACTIVITY, PATHWAY, INTERACTIVE
     *
     * @param elementId   the element id
     * @param elementType the element type
     * @return mono with list of courseware elements.
     * @throws UnsupportedOperationException   if method was invoked for unsupported type
     * @throws ParentActivityNotFoundException if pathway in a path does not have parent activity for some reason.
     * @throws ParentPathwayNotFoundException  if interactive in a path does not have parent pathway for some reason.
     * @throws ParentActivityNotFoundException if feedback in a path does not have parent interactive for some reason.
     * @throws ComponentParentNotFound         if component in a path does not have parent element for some reason.
     * @throws ScenarioParentNotFoundException if scenario in a path does not have parent element for some reason.
     * @throws StackOverflowError              if there is a circular dependency
     */
    @Trace(async = true)
    public Mono<List<CoursewareElement>> getPath(final UUID elementId, final CoursewareElementType elementType) {
        switch (elementType) {
            case ACTIVITY:
                return getPathForActivity(elementId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            case PATHWAY:
                return getPathForPathway(elementId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            case INTERACTIVE:
                return getPathForInteractive(elementId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            case FEEDBACK:
                return getPathForFeedback(elementId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            case COMPONENT:
                return getPathForComponent(elementId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            case SCENARIO:
                return getPathForScenario(elementId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            default:
                throw new UnsupportedOperationException("Unsupported courseware element type " + elementType);
        }
    }

    /**
     * Returns all the {@link CoursewareElementType#ACTIVITY} ids present in the tree path
     *
     * @param elementId   the element id
     * @param elementType the element type
     * @return a mono list of activity ids
     * @throws UnsupportedOperationException   if method was invoked for unsupported type
     * @throws ParentActivityNotFoundException if pathway in a path does not have parent activity for some reason.
     * @throws ParentPathwayNotFoundException  if interactive in a path does not have parent pathway for some reason.
     * @throws StackOverflowError              if there is a circular dependency
     */
    public Mono<List<UUID>> getParentActivityIds(final UUID elementId, final CoursewareElementType elementType) {
        return getPath(elementId, elementType)
                .map(elements -> elements.stream()
                        .filter(element -> element.getElementType().equals(CoursewareElementType.ACTIVITY))
                        .map(CoursewareElement::getElementId)
                        .collect(Collectors.toList()));
    }

    /**
     * Returns the path from the most top activity to the given activity.
     *
     * @param activityId the activity to fetch the path for
     * @return mono with list of courseware elements.
     */
    @Trace(async = true)
    private Mono<List<CoursewareElement>> getPathForActivity(final UUID activityId) {
        return activityService.findParentPathwayId(activityId)
                .flatMap(this::getPathForPathway)
                .defaultIfEmpty(Lists.newArrayList())
                .map(list -> {
                    list.add(new CoursewareElement(activityId, CoursewareElementType.ACTIVITY));
                    return list;
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Returns the path from the most top activity to the given pathway.
     *
     * @param pathwayId the pathway to fetch the path for
     * @return mono with list of courseware elements.
     */
    @Trace(async = true)
    private Mono<List<CoursewareElement>> getPathForPathway(final UUID pathwayId) {
        return pathwayService.findParentActivityId(pathwayId)
                .flatMap(this::getPathForActivity)
                .map(list -> {
                    list.add(new CoursewareElement(pathwayId, CoursewareElementType.PATHWAY));
                    return list;
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Returns the path from the most top activity to the given interactive.
     *
     * @param interactiveId the interactive to fetch the path for
     * @return mono with list of courseware elements.
     */
    @Trace(async = true)
    private Mono<List<CoursewareElement>> getPathForInteractive(final UUID interactiveId) {
        return interactiveService.findParentPathwayId(interactiveId)
                .flatMap(this::getPathForPathway)
                .map(list -> {
                    list.add(new CoursewareElement(interactiveId, CoursewareElementType.INTERACTIVE));
                    return list;
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Returns the path from the most top activity to the given feedback.
     *
     * @param feedbackId the feedback to fetch the path for
     * @return a mono list of courseware elements.
     */
    @Trace(async = true)
    private Mono<List<CoursewareElement>> getPathForFeedback(final UUID feedbackId) {
        return feedbackService.findParentId(feedbackId)
                .flatMap(this::getPathForInteractive)
                .map(list -> {
                    list.add(new CoursewareElement(feedbackId, CoursewareElementType.FEEDBACK));
                    return list;
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Returns the path from the most top activity to the given component
     *
     * @param componentId the component to fetch the path for
     * @return a mono list of courseware elements.
     * @throws UnsupportedOperationException if the parent by component type is not allowed
     */
    @Trace(async = true)
    private Mono<List<CoursewareElement>> getPathForComponent(final UUID componentId) {

        return componentService.findParentFor(componentId)
                .flatMap(parentByComponent -> {
                    switch (parentByComponent.getParentType()) {
                        case INTERACTIVE:
                            return getPathForInteractive(parentByComponent.getParentId());
                        case ACTIVITY:
                            return getPathForActivity(parentByComponent.getParentId());
                        default:
                            throw new UnsupportedOperationException(
                                    String.format("parentType %s not allowed for component", parentByComponent.getParentType()));
                    }
                }).map(list -> {
                    list.add(new CoursewareElement(componentId, CoursewareElementType.COMPONENT));
                    return list;
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Returns the path from the most top activity to the given scenario
     *
     * @param scenarioId the scenario to fetch the path for
     * @return a mono list of courseware elements.
     * @throws UnsupportedOperationException if the parent by scenario type is not allowed
     */
    @Trace(async = true)
    private Mono<List<CoursewareElement>> getPathForScenario(final UUID scenarioId) {
        return scenarioService.findParent(scenarioId)
                .flatMap(parentByScenario -> {
                    switch (parentByScenario.getParentType()) {
                        case INTERACTIVE:
                            return getPathForInteractive(parentByScenario.getParentId());
                        case ACTIVITY:
                            return getPathForActivity(parentByScenario.getParentId());
                        default:
                            throw new UnsupportedOperationException(
                                    String.format("parentType %s not allowed for scenario", parentByScenario.getParentType()));
                    }
                })
                .map(list -> {
                    list.add(new CoursewareElement(scenarioId, CoursewareElementType.SCENARIO));
                    return list;
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate an activity and all the relative objects: config, theme, scenarios, pathways, components.
     * This method should be used for duplication of top activities (which do not have parent pathways).
     *
     * @param activityId      the activity id to duplicate
     * @param account         the creator account of the duplicated activity
     * @param isInSameProject the indicator to determine if old and new activities are in the same project or not
     * @return a mono of the duplicated activity object. The activity id will therefore be different
     */
    @Trace(async = true)
    public Mono<Activity> duplicateActivity(final UUID activityId, final Account account, final Boolean isInSameProject) {
        affirmArgument(activityId != null, "activityId is missing");
        affirmArgument(account != null, "account is missing");
        affirmArgument(isInSameProject != null, "isInSameProject is missing");

        DuplicationContext context = new DuplicationContext()
                                        .setDuplicatorAccount(account.getId())
                                        .setDuplicatorSubscriptionId(account.getSubscriptionId())
                                        .setRequireNewAssetId(!isInSameProject) // new asset id needs if the old and new activities are not in the same project
                                        .setOldRootElementId(activityId); //add old rootElementId to context

        //duplicate all courseware tree recursively
        return duplicateActivity(activityId, account.getId(), context)
                //replace ids in scenarios and save
                .flatMap(newActivity -> Flux.fromIterable(context.getScenarios())
                        .concatMap(scenario -> scenarioService.duplicate(scenario.getScenarioId(), scenario.getParentId(),
                                scenario.getParentType(), context))
                        .then(Mono.just(newActivity)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate an activity and all the relative objects (config, theme, scenarios, pathways, components) into new pathway.
     *
     * @param activityId         the activity to duplicate
     * @param newParentPathwayId the pathway to add new activity to
     * @param account            the creator account of the duplicated activity
     * @return a mono of the duplicated activity object
     */
    @Trace(async = true)
    public Mono<Activity> duplicateActivity(final UUID activityId, final UUID newParentPathwayId, final Account account, final Boolean newDuplicateFlow) {
        // find the root element for activity
        Mono<UUID> activityRootElementId = getRootElementId(activityId, CoursewareElementType.ACTIVITY);
        // find the root element for the pathway
        Mono<UUID> pathwayRootElementId = getRootElementId(newParentPathwayId, CoursewareElementType.PATHWAY);

        //duplicate all courseware tree recursively
        return Mono.zip(activityRootElementId, pathwayRootElementId)
                // check if both root elements are in the same project or not
                .flatMap(tuple2 -> activityService.isDuplicatedActivityInTheSameProject(tuple2.getT1(), tuple2.getT2(), newDuplicateFlow)
                        // add new rootElement id, account id, subscription id, and isInSameProject to context
                        .map(isInSameProject -> new DuplicationContext()
                                .setDuplicatorAccount(account.getId())
                                .setDuplicatorSubscriptionId(account.getSubscriptionId())
                                .setRequireNewAssetId(!isInSameProject)
                                .setOldRootElementId(tuple2.getT1())
                                .setNewRootElementId(tuple2.getT2())))
                .flatMap(context -> duplicateActivity(activityId, newParentPathwayId, account.getId(), context)
                        //replace ids in scenarios and save
                        .flatMap(newActivity -> Flux.fromIterable(context.getScenarios())
                                .concatMap(scenario -> scenarioService.duplicate(scenario.getScenarioId(),
                                        scenario.getParentId(),
                                        scenario.getParentType(),
                                        context))
                                .then(Mono.just(newActivity)))
                        .doOnEach(ReactiveTransaction.linkOnNext()))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate an activity and all the relative objects: config, theme, scenarios, pathways, components and insert
     * new activity at the given index into the parent pathway.
     * <br/>
     * Use {@link #duplicateActivity(UUID, UUID, Account, Boolean)} if you need just add new activity at the end of the pathway
     *
     * @param activityId         the activity id to duplicate
     * @param newParentPathwayId the parent pathway to insert new activity to
     * @param index              the pathway index (the position in a pathway the new activity should be inserted at)
     * @param account            the creator account of the duplicated activity
     * @return a mono of the duplicated activity object. The activity id will therefore be different
     */
    @Trace(async = true)
    public Mono<Activity> duplicateActivity(final UUID activityId, final UUID newParentPathwayId, final int index, final Account account, final Boolean newDuplicateFlow) {
        // find the root element for activity
        Mono<UUID> activityRootElementId = getRootElementId(activityId, CoursewareElementType.ACTIVITY);
        // find the root element for the pathway
        Mono<UUID> pathwayRootElementId = getRootElementId(newParentPathwayId, CoursewareElementType.PATHWAY);

        return Mono.zip(activityRootElementId, pathwayRootElementId)
                // check if both root elements are in the same project or not
                .flatMap(tuple2 -> activityService.isDuplicatedActivityInTheSameProject(tuple2.getT1(), tuple2.getT2(), newDuplicateFlow))
                //duplicate all courseware tree recursively
                .flatMap(isInSameProject -> duplicateActivity(activityId, account, isInSameProject)
                        //persist parent pathway
                        .flatMap(newActivity -> activityService.saveRelationship(newActivity.getId(), newParentPathwayId, index)
                                .then(Mono.just(newActivity)))
                                .doOnEach(ReactiveTransaction.linkOnNext())
                );
    }

    /**
     * Duplicate interactive and all related elements, insert created interactive at the end of the given pathway
     *
     * @param interactiveId the interactive to duplicate
     * @param newPathwayId  the pathway to insert new interactive
     * @param accountId     the user account requesting the interactive be duplicated
     * @return a mono with new created interactive
     */
    @Trace(async = true)
    public Mono<Interactive> duplicateInteractive(final UUID interactiveId, final UUID newPathwayId, final UUID accountId) {
        //duplicate all interactive tree recursively
        return getRootElementId(newPathwayId, CoursewareElementType.PATHWAY)
                .flatMap(rootElementId -> {
                    //add new rootElement id to context
                    DuplicationContext context = new DuplicationContext()
                            .setNewRootElementId(rootElementId)
                            .setDuplicatorAccount(accountId);
                    return duplicateInteractive(interactiveId, newPathwayId, context)
                            //replace ids in scenarios and save
                            .flatMap(newInteractive -> Flux.fromIterable(context.getScenarios())
                                    .concatMap(scenario -> scenarioService.duplicate(scenario.getScenarioId(),
                                                                                     scenario.getParentId(),
                                                                                     scenario.getParentType(),
                                                                                     context))
                                    .then(Mono.just(newInteractive)))
                            .doOnEach(ReactiveTransaction.linkOnNext());
                });
    }

    /**
     * Duplicate interactive and all related elements, insert created interactive at the index position of the given pathway
     *
     * @param interactiveId the interactive to duplicate
     * @param newPathwayId  the pathway to insert new interactive
     * @param index         the position in the pathway to insert the interactive at
     * @return a mono with new created interactive
     */
    @Trace(async = true)
    public Mono<Interactive> duplicateInteractive(final UUID interactiveId, final UUID newPathwayId, final int index) {
        //duplicate all interactive tree recursively
        return getRootElementId(newPathwayId, CoursewareElementType.PATHWAY)
                .flatMap(rootElementId -> {
                    //add new rootElement id to context
                    DuplicationContext context = new DuplicationContext()
                            .setNewRootElementId(rootElementId);
                    return duplicateInteractive(interactiveId, context)
                            //insert interactive into parent pathway
                            .flatMap(newInteractive -> interactiveService.saveToPathway(newInteractive.getId(),
                                                                                        newPathwayId,
                                                                                        index)
                                    .then(Mono.just(newInteractive)))
                            //replace ids in scenarios and save
                            .flatMap(newInteractive -> Flux.fromIterable(context.getScenarios())
                                    .concatMap(scenario -> scenarioService.duplicate(scenario.getScenarioId(),
                                                                                     scenario.getParentId(),
                                                                                     scenario.getParentType(),
                                                                                     context))
                                    .then(Mono.just(newInteractive)))
                            .doOnEach(ReactiveTransaction.linkOnNext());
                });
    }

    /**
     * Duplicate an activity and all the relative objects: config, theme, scenarios, pathways, components and
     * saves all ids into the context.
     * <br/>
     * <b>Note:</b> This method should not be used outside this service.
     * Service clients should use {@link #duplicateActivity(UUID, UUID, Account, Boolean)}
     *
     * @param activityId the activity id to duplicate
     * @param accountId  the creator of the duplicated activity
     * @return a mono of the duplicated activity object. The activity id will therefore be different
     */
    @Trace(async = true)
    Mono<Activity> duplicateActivity(final UUID activityId, final UUID accountId, final DuplicationContext context) {
        return activityService.findById(activityId)
                // store registered elements in the duplication context
                .flatMap(activity -> findRegisteredElements(activity.getStudentScopeURN())
                        .flatMap(ScopeReference -> {
                            context.putIds(ScopeReference.getElementId(), activity.getStudentScopeURN());
                            return Mono.just(ScopeReference);
                        })
                        .collectList()
                        .map(context::addAllScopeReferences)
                        .thenReturn(activity))
                // store manual grading components in the duplication context
                .flatMap(activity -> manualGradeDuplicationService.findManualGradingComponentByWalkable(activityId)
                        .collectList()
                        .map(context::addAllManualComponentByWalkable)
                        .thenReturn(activity))
                //copy activity itself
                .flatMap(activity -> activityService.duplicateActivity(activity, accountId)
                        .doOnSuccess(newActivity -> {
                            if(activityId.equals(context.getOldRootElementId())){
                                context.setNewRootElementId(newActivity.getId());
                            }
                            context.putIds(activityId, newActivity.getId());
                            context.putIds(activity.getStudentScopeURN(), newActivity.getStudentScopeURN());
                        }))
                //duplicate default theme
                .flatMap(newActivity -> activityService.getLatestActivityThemeByActivityId(activityId)
                        .flatMap(theme -> activityService.duplicateTheme(theme.getConfig(), newActivity.getId()))
                        .thenReturn(newActivity))
                //duplicate activity and selected theme association
                .flatMap(newActivity -> themeService.fetchThemeByElementId(activityId)
                        .flatMap(themePayload -> themeService.saveThemeByElement(themePayload.getId(),
                                                                                 newActivity.getId(),
                                                                                 CoursewareElementType.ACTIVITY))
                        .thenReturn(newActivity))
                //copy pathways
                .flatMap(newActivity -> activityService.findChildPathwayIds(activityId)
                        .flatMapIterable(p -> p)
                        .concatMap(pathway -> duplicatePathway(pathway, newActivity.getId(), accountId, context))
                        .then(Mono.just(newActivity)))
                //copy components
                .flatMap(newActivity -> componentGateway.findComponentIdsByActivity(activityId)
                        .flatMap(componentId -> duplicateComponent(context, newActivity.getId(), CoursewareElementType.ACTIVITY, componentId))
                        .then(Mono.just(newActivity)))
                //copy annotations - IMPORTANT: copy annotations should be before duplicating activity config and after updating new root element id in context
                .flatMap(newActivity -> getRootElementId(activityId, CoursewareElementType.ACTIVITY)
                        .flatMap(rootElement -> annotationDuplicationService.findIdsByElement(rootElement, activityId)
                                .flatMap(annotation -> annotationDuplicationService.duplicate(context.getNewRootElementId(), newActivity.getId(), context, annotation))
                                .then(Mono.just(newActivity))))
                //copy activity config - IMPORTANT: always should be the last, after all children are duplicated
                .flatMap(newActivity -> activityService.findLatestConfig(activityId)
                        .flatMap(config -> activityService.duplicateConfig(config.getConfig(), newActivity.getId(), context))
                        .thenMany(duplicateConfigurationFields(activityId, newActivity.getId()))
                        .then(Mono.just(newActivity)))
                // duplicate the meta information for this activity
                .flatMap(newActivity -> coursewareElementMetaInformationService.duplicate(activityId, newActivity.getId())
                        .then(Mono.just(newActivity)))
                //find scenarios and put to list to save it later
                .flatMap(newActivity -> scenarioService.findScenarioIdsFor(activityId)
                        .map(scenario -> context.addScenario(scenario, newActivity.getId(), CoursewareElementType.ACTIVITY))
                        .then(Mono.just(newActivity)))
                //copy assets
                .flatMap(newActivity -> coursewareAssetService.duplicateAssets(activityId, newActivity.getId(), CoursewareElementType.ACTIVITY, context)
                        .then(Mono.just(newActivity)))
                //copy links to document items
                .flatMap(newActivity -> documentItemLinkService.duplicateLinks(activityId, newActivity.getId())
                        .then(Mono.just(newActivity)))
                // duplicate the manual grading component by walkable
                .flatMap(newActivity -> manualGradeDuplicationService.persist(context.duplicateManualGradingComponentByWalkable())
                        .then(Mono.just(newActivity)))
                // duplicate the scope references
                .flatMap(newActivity -> register(context.duplicateScopeReferences())
                        .then(Mono.just(newActivity)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate a component
     *
     * @param context the duplication context
     * @param elementId the element id the component belongs to
     * @param elementType the element type the component belongs to
     * @param componentId the id of the component to duplicate
     * @return a mono of the duplicated component
     */
    @Trace(async = true)
    private Mono<Component> duplicateComponent(final DuplicationContext context, final UUID elementId,
                                                              final CoursewareElementType elementType,
                                                              final UUID componentId) {
        // duplicate the component
        return componentService.duplicate(componentId, elementId, elementType, context)
                .flatMap(duplicatedComponent -> {
                    // duplicate the configuration fields for the component
                    return duplicateConfigurationFields(componentId, duplicatedComponent.getId())
                            .then(Mono.just(duplicatedComponent))
                            .doOnEach(ReactiveTransaction.linkOnNext());
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate an activity and all the relative objects: config, theme, scenarios, pathways, components and
     * insert new activity at the end of the pathway. Saves all ids into the context.
     * <br/>
     * <b>Note:</b> This method should not be used outside this service.
     * Service clients should use {@link #duplicateActivity(UUID, UUID, Account, Boolean)}
     *
     */
    @Trace(async = true)
    Mono<Activity> duplicateActivity(final UUID activityId, final UUID newParentPathwayId, final UUID accountId, final DuplicationContext context) {
        return duplicateActivity(activityId, accountId, context)
                //persist parent pathway
                .flatMap(newActivity -> activityService.saveRelationship(newActivity.getId(), newParentPathwayId)
                        .then(Mono.just(newActivity)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate pathway and all its children and attach it to the given activity. Order of children should be preserved.
     *
     * @param pathwayId     the pathway to duplicate
     * @param newActivityId the new parent activity id
     * @param accountId     the account performing the duplication
     * @return mono with created pathway
     */
    @Trace(async = true)
    Mono<Pathway> duplicatePathway(final UUID pathwayId, final UUID newActivityId, final UUID accountId, final DuplicationContext context) {
        return pathwayService.findById(pathwayId)
                //copy a pathway
                .flatMap(p -> pathwayService.duplicatePathway(p, newActivityId)
                        .doOnSuccess(newPathway -> context.putIds(pathwayId, newPathway.getId())))
                //copy pathway's children (activities + components)
                .flatMap(newPathway -> pathwayService.getOrderedWalkableChildren(pathwayId)
                        .flatMapIterable(p -> p)
                        .concatMap(walkableChild -> {
                            switch (walkableChild.getElementType()) {
                                case ACTIVITY:
                                    return duplicateActivity(walkableChild.getElementId(), newPathway.getId(), accountId, context);
                                case INTERACTIVE:
                                    return duplicateInteractive(walkableChild.getElementId(), newPathway.getId(), context);
                                default:
                                    throw new UnsupportedOperationException(
                                            String.format("Broken pathway %s. Pathway can not have %s as a child", pathwayId, walkableChild));
                            }
                        })
                        .then(Mono.just(newPathway)))
                //copy pathway config - IMPORTANT: always should be the last, after all children are duplicated
                .flatMap(newPathway -> pathwayService.findLatestConfig(pathwayId)
                        .flatMap(config -> pathwayService.duplicateConfig(config.getConfig(), newPathway.getId(), context))
                        .thenReturn(newPathway))
                //copy assets
                .flatMap(newPathway -> coursewareAssetService.duplicateAssets(pathwayId, newPathway.getId(), CoursewareElementType.PATHWAY, context)
                        .then(Mono.just(newPathway)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate an interactive and all objects related to its: configuration, scenarios, feedback, components.
     * <br/>
     * <b>Note:</b> created interactive will <b>not</b> be attached to a parent pathway
     *
     * @param interactiveId the interactive to duplicate
     * @return mono with new duplicated interactive
     * @throws InteractiveNotFoundException if interactive doesn't exist
     * @throws FeedbackNotFoundException    if any of feedbacks is not found.
     * @throws ComponentNotFoundException   if any of components is not found
     */
    @Trace(async = true)
    Mono<Interactive> duplicateInteractive(final UUID interactiveId, final DuplicationContext context) {
        return interactiveService.findById(interactiveId)
                // store registered elements in the duplication context
                .flatMap(i -> findRegisteredElements(i.getStudentScopeURN())
                        .collectList()
                        .map(context::addAllScopeReferences)
                        .thenReturn(i))
                // store manual grading components in the duplication context
                .flatMap(i -> manualGradeDuplicationService.findManualGradingComponentByWalkable(i.getId())
                        .collectList()
                        .map(context::addAllManualComponentByWalkable)
                        .thenReturn(i))
                //copy an interactive itself
                .flatMap(i -> interactiveService.duplicateInteractive(i)
                        .doOnSuccess(newInteractive -> {
                            context.putIds(interactiveId, newInteractive.getId());
                            context.putIds(i.getStudentScopeURN(), newInteractive.getStudentScopeURN());
                        }))
                //copy components
                .flatMap(newInteractive -> componentService.findIdsByInteractive(interactiveId)
                        .flatMap(componentId -> duplicateComponent(context, newInteractive.getId(), CoursewareElementType.INTERACTIVE, componentId))
                        .then(Mono.just(newInteractive)))
                //copy feedback
                .flatMap(newInteractive -> feedbackService.findIdsByInteractive(interactiveId)
                        .flatMapIterable(f -> f)
                        .flatMap(feedback -> feedbackService.duplicate(feedback, newInteractive.getId(), context))
                        .then(Mono.just(newInteractive)))
                //copy annotations - IMPORTANT: copy annotations should be before duplicating interactive config and after updating new root element id in context
                .flatMap(newInteractive -> getRootElementId(interactiveId, CoursewareElementType.INTERACTIVE)
                        .flatMap(rootElement -> annotationDuplicationService.findIdsByElement(rootElement, interactiveId)
                                .flatMap(annotation -> annotationDuplicationService.duplicate(context.getNewRootElementId(), newInteractive.getId(), context, annotation))
                                .then(Mono.just(newInteractive))))
                //copy an interactive config - IMPORTANT: always should the last, after all children are duplicated
                .flatMap(newInteractive -> interactiveService.duplicateInteractiveConfig(interactiveId, newInteractive.getId(), context)
                        .thenMany(duplicateConfigurationFields(interactiveId, newInteractive.getId()))
                        .then(Mono.just(newInteractive)))
                // duplicate the meta information for this interactive
                .flatMap(newInteractive -> coursewareElementMetaInformationService.duplicate(interactiveId, newInteractive.getId())
                        .then(Mono.just(newInteractive)))
                //find scenarios and put to list to save it later
                .flatMap(newInteractive -> scenarioService.findScenarioIdsFor(interactiveId)
                        .map(scenario -> context.addScenario(scenario, newInteractive.getId(), CoursewareElementType.INTERACTIVE))
                        .then(Mono.just(newInteractive)))
                //copy assets
                .flatMap(newInteractive -> coursewareAssetService.duplicateAssets(interactiveId, newInteractive.getId(), CoursewareElementType.INTERACTIVE, context)
                        .then(Mono.just(newInteractive)))
                //copy links to document items
                .flatMap(newInteractive -> documentItemLinkService.duplicateLinks(interactiveId, newInteractive.getId())
                        .then(Mono.just(newInteractive)))
                // duplicate the manual grading component by walkable
                .flatMap(newInteractive -> manualGradeDuplicationService.persist(context.duplicateManualGradingComponentByWalkable())
                        .then(Mono.just(newInteractive)))
                // duplicate the scope references
                .flatMap(newInteractive -> register(context.duplicateScopeReferences())
                        .then(Mono.just(newInteractive)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate an interactive and all objects related to its: configuration, scenarios, feedback, components.
     * And attach new interactive at the end of parent pathway.
     *
     * @param interactiveId the interactive to duplicate
     * @param newPathwayId  the new pathway id to attach interactive
     * @return mono with new duplicated interactive
     * @throws InteractiveNotFoundException if interactive doesn't exist
     * @throws FeedbackNotFoundException    if any of feedbacks is not found.
     * @throws ComponentNotFoundException   if any of components is not found
     */
    @Trace(async = true)
    Mono<Interactive> duplicateInteractive(final UUID interactiveId, final UUID newPathwayId, final DuplicationContext context) {
        return duplicateInteractive(interactiveId, context)
                //save to parent pathway
                .flatMap(newInteractive -> interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)
                        .then(Mono.just(newInteractive)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a workspace id the given courseware element belongs to.
     * This method finds the top activity and fetches workspace for the top activity.
     *
     * @param elementId   the element id
     * @param elementType the element type
     * @return mono with workspace id, empty mono if the top activity of the element doesn't have workspace id
     * (ex. top activity was removed from workspace)
     * @throws UnsupportedOperationException   from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentActivityNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentPathwayNotFoundException  from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentActivityNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ComponentParentNotFound         from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ScenarioParentNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws StackOverflowError              from {@link #getPath(UUID, CoursewareElementType)}
     */
    @Deprecated
    public Mono<UUID> getWorkspaceId(final UUID elementId, final CoursewareElementType elementType) {
        checkArgument(elementId != null, "elementId is required");
        checkArgument(elementType != null, "elementType is required");
        return getPath(elementId, elementType)
                .flatMap(path -> activityService.findWorkspaceIdByActivity(path.get(0).getElementId()));
    }

    /**
     * Find a project id the given courseware element belongs to.
     * This method finds the top activity and fetches the project the top activity is related to.
     *
     * @param elementId  the element id to find the root activity for
     * @param elementType the element type
     * @return a mono with the project id
     * @throws UnsupportedOperationException   from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentActivityNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentPathwayNotFoundException  from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentActivityNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ComponentParentNotFound         from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ScenarioParentNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws StackOverflowError              from {@link #getPath(UUID, CoursewareElementType)}
     */
    @Trace(async = true)
    public Mono<UUID> getProjectId(final UUID elementId, final CoursewareElementType elementType) {
        affirmArgument(elementId != null, "elementId is missing");
        affirmArgument(elementType != null, "elementType is missing");
        return getPath(elementId, elementType)
                .flatMap(path -> activityService.findProjectIdByActivity(path.get(0).getElementId()))
                .map(ProjectActivity::getProjectId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a workspace id the given courseware element belongs to.
     * This method finds the project and fetches workspace for the project associated with.
     *
     * @param elementId   the element id
     * @param elementType the element type
     * @return mono with workspace id, empty mono if the top activity of the element doesn't have workspace id
     * (ex. top activity was removed from workspace)
     * @throws UnsupportedOperationException   from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentActivityNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentPathwayNotFoundException  from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentActivityNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ComponentParentNotFound         from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ScenarioParentNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws StackOverflowError              from {@link #getPath(UUID, CoursewareElementType)}
     */
    @Trace(async = true)
    public Mono<UUID> getWorkspaceIdByProject(final UUID elementId, final CoursewareElementType elementType) {
        checkArgument(elementId != null, "elementId is required");
        checkArgument(elementType != null, "elementType is required");
        return getProjectId(elementId, elementType)
                .flatMap(projectId -> projectService.findById(projectId))
                .map(Project::getWorkspaceId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a root element id the given courseware element belongs to.
     * This method finds the top activity and returns it's id.
     *
     * @param elementId  the element id to find the root activity for
     * @param elementType the element type
     * @return a mono with the root element id
     * @throws UnsupportedOperationException   from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentActivityNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentPathwayNotFoundException  from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ParentActivityNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ComponentParentNotFound         from {@link #getPath(UUID, CoursewareElementType)}
     * @throws ScenarioParentNotFoundException from {@link #getPath(UUID, CoursewareElementType)}
     * @throws StackOverflowError              from {@link #getPath(UUID, CoursewareElementType)}
     */
    @Trace(async = true)
    public Mono<UUID> getRootElementId(final UUID elementId, final CoursewareElementType elementType) {
        affirmArgument(elementId != null, "elementId is missing");
        affirmArgument(elementType != null, "elementType is missing");
        return getPath(elementId, elementType)
                .flatMap(path -> Mono.just(path.get(0).getElementId()))
                .onErrorReturn(elementId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save an activity change
     *
     * @param activityId the activity id to save the change for
     * @return a mono of activity change
     */
    public Mono<ActivityChange> saveChange(final UUID activityId) {
        return activityService.saveChange(activityId);
    }

    /**
     * Find the latest activity change for an activity
     *
     * @param activityId the activity to find the latest change for
     * @return a mono of activity change
     * @throws ActivityChangeNotFoundException when no changes are found for this activity
     */
    public Mono<ActivityChange> findLatestChange(final UUID activityId) {
        return activityService.fetchLatestChange(activityId);
    }

    /**
     * Register a courseware element with a plugin reference to a student scope
     *
     * @param studentScopeURN the scope to register to
     * @param ref the plugin reference to register to the scope
     * @param elementId the courseware element id
     * @param elementType the courseware element type
     * @return a mono of scope reference
     * @throws IllegalArgumentException when any of the supplied argument is <code>null</code>
     */
    @Trace(async = true)
    public Mono<ScopeReference> register(final UUID studentScopeURN, final PluginReference ref, final UUID elementId,
                                         final CoursewareElementType elementType) {

        checkArgument(studentScopeURN != null, "studentScopeURN is required");
        checkArgument(ref != null, "plugin reference is required");
        checkArgument(elementId != null, "elementId is required");
        checkArgument(elementType != null, "elementType is required");

        ScopeReference scopeReference = new ScopeReference()
                .setScopeURN(studentScopeURN)
                .setElementId(elementId)
                .setElementType(elementType)
                .setPluginId(ref.getPluginId())
                .setPluginVersion(ref.getPluginVersionExpr());

        return coursewareGateway.persist(scopeReference)
                .then(Mono.just(scopeReference))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Register a list of scope references
     *
     * @param scopeReferences the scope references to register
     * @return a flux of the registered scope references
     */
    @Trace(async = true)
    public Flux<ScopeReference> register(final List<ScopeReference> scopeReferences) {
        if (scopeReferences.isEmpty()) {
            return Flux.empty();
        }

        return scopeReferences.stream()
                .map(scopeReference -> coursewareGateway.persist(scopeReference)
                        .thenMany(Flux.just(scopeReference)))
                .reduce(Flux::mergeWith)
                .orElse(Flux.empty())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * De-register a courseware element from a student scope
     *
     * @param studentScopeURN the student scope to de-register the element from
     * @param elementId the element to de-register from the student scope
     * @return a flux of void
     * @throws IllegalArgumentException if any of the supplied argument is <code>null</code>
     */
    @Trace(async = true)
    public Flux<Void> deRegister(final UUID studentScopeURN, final UUID elementId) {

        checkArgument(studentScopeURN != null, "studentScopeURN is required");
        checkArgument(elementId != null, "elementId is required");

        ScopeReference scopeReference = new ScopeReference()
                .setScopeURN(studentScopeURN)
                .setElementId(elementId);

        return coursewareGateway.delete(scopeReference)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a courseware element by its student scope urn
     *
     * @param studentScopeURN the student scope urn to find the courseware element for
     * @return a mono of courseware element associated with the student scope
     * @throws CoursewareElementNotFoundFault when the courseware element is not found
     * @throws IllegalArgumentFault when the supplied argument is <code>null</code>
     */
    public Mono<CoursewareElement> findElementByStudentScope(final UUID studentScopeURN) {

        affirmArgument(studentScopeURN != null, "studentScopeURN is required");

        return coursewareGateway.findElementBy(studentScopeURN)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new CoursewareElementNotFoundFault(String.format(
                            "courseware element not found for studentScopeURN `%s`", studentScopeURN
                    ));
                });
    }

    /**
     * Find the plugin reference for a courseware element
     *
     * @param elementId the element id to find the plugin reference for
     * @param type the element type determining where to fetch the plugin reference information
     * @return a mono of plugin reference
     * @throws PluginReferenceNotFoundFault when failing to find the plugin reference
     * @throws UnsupportedOperationException when the type is not a plugin reference type
     */
    @Trace(async = true)
    public Mono<PluginReference> findPluginReference(final UUID elementId, final CoursewareElementType type) {
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(type != null, "elementType is required");

        Mono<?> pluginReference;

        switch (type) {
            case ACTIVITY:
                pluginReference = activityService.findById(elementId);
                break;
            case COMPONENT:
                pluginReference = componentService.findById(elementId);
                break;
            case FEEDBACK:
                pluginReference = feedbackService.findById(elementId);
                break;
            case INTERACTIVE:
                pluginReference = interactiveService.findById(elementId);
                break;
            default:
                throw new UnsupportedOperationException(String.format("%s not a plugin reference type", type));
        }

        return pluginReference
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
            throw new PluginReferenceNotFoundFault(throwable.getMessage());
        }).map(one -> (PluginReference) one);
    }

    /**
     * Fetch config for courseware element based on the type of element
     * @param elementId the element id
     * @param type the element type
     * @return Mono of String
     * @throws UnsupportedOperationException if given courseware type cannot have config
     */
    public Mono<String> fetchConfig(final UUID elementId, final CoursewareElementType type) {
        switch(type) {
            case ACTIVITY:
                return activityService.findLatestConfig(elementId).map(ActivityConfig::getConfig);
            case INTERACTIVE:
                return interactiveService.findLatestConfig(elementId).map(InteractiveConfig::getConfig);
            case COMPONENT:
                return componentService.findLatestByConfigId(elementId).map(ComponentConfig::getConfig);
            case FEEDBACK:
                return feedbackService.findLatestConfig(elementId);
            default : throw new UnsupportedOperationException("Courseware type can not have config: " + type);
        }
    }

    /**
     * Extract the configuration fields from the supplied json and save each extracted field
     *
     * @param elementId the courseware element id to save the fields for
     * @param config the config to extract the fields from
     * @return a flux of void
     * @throws IllegalArgumentFault when failing to parse the json string
     */
    @Trace(async = true)
    public Flux<Void> saveConfigurationFields(final UUID elementId, final String config) {
        return Flux.just(1)
                .map(ignored -> Json.toMap(config))
                .doOnError(JSONException.class, ex -> {
                    throw new IllegalStateFault(ex.getMessage());
                })
                .map(configMap -> configMap.entrySet()
                        .stream()
                        .map(entry -> new CoursewareElementConfigurationField()
                                .setElementId(elementId)
                                .setFieldName(entry.getKey())
                                .setFieldValue(entry.getValue()))
                        .collect(Collectors.toList()))
                .flatMap(configurationFields -> {
                    return Flux.just(configurationFields.toArray(new CoursewareElementConfigurationField[0]));
                })
                .flatMap(coursewareGateway::persist)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the extracted configuration fields for the element to duplicate and persist each configuration field
     * for the new element id
     *
     * @param oldElementId the element id to find the extracted configuration fields for
     * @param newElementId the new element id the configuration fields should be duplicated for
     * @return a flux of void
     */
    @Trace(async = true)
    private Flux<Void> duplicateConfigurationFields(final UUID oldElementId, final UUID newElementId) {
        return coursewareGateway.fetchConfigurationFields(oldElementId)
                .flatMap(configurationField -> coursewareGateway.persist(new CoursewareElementConfigurationField()
                        .setElementId(newElementId)
                        .setFieldName(configurationField.getFieldName())
                        .setFieldValue(configurationField.getFieldValue())))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /** Find all the registered elements to a student scope URN
     *
     * @param studentScopeURN the student scope URN to find all the registered elements to
     * @return a flux of scope references
     */
    @Trace(async = true)
    public Flux<ScopeReference> findRegisteredElements(final UUID studentScopeURN) {
        return coursewareGateway.findRegisteredElements(studentScopeURN)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a courseware element type and ancestry given an element id
     *
     * @param elementId the element id to find type and ancestry for
     * @return a mono with the courseware element ancestry
     * @throws IllegalArgumentFault when the element is not found
     */
    @SuppressWarnings("Duplicates")
    public Mono<CoursewareElementAncestry> findCoursewareElementAncestry(final UUID elementId) {

        Mono<CoursewareElementType> activityType = activityService.findById(elementId)
                .map(activity -> CoursewareElementType.ACTIVITY)
                .onErrorResume(ActivityNotFoundException.class, e -> Mono.empty());

        Mono<CoursewareElementType> pathwayType = pathwayService.findById(elementId)
                .map(pathway -> CoursewareElementType.PATHWAY)
                .onErrorResume(PathwayNotFoundException.class, e -> Mono.empty());

        Mono<CoursewareElementType> interactiveType = interactiveService.findById(elementId)
                .map(interactive -> CoursewareElementType.INTERACTIVE)
                .onErrorResume(InteractiveNotFoundException.class, e -> Mono.empty());

        Mono<CoursewareElementType> componentType = componentService.findById(elementId)
                .map(component -> CoursewareElementType.COMPONENT)
                .onErrorResume(ComponentNotFoundException.class, e -> Mono.empty());

        Mono<CoursewareElementType> scenarioType = scenarioService.findById(elementId)
                .map(scenario -> CoursewareElementType.SCENARIO)
                .onErrorResume(ScenarioNotFoundException.class, e -> Mono.empty());

        Mono<CoursewareElementType> feedbackType = feedbackService.findById(elementId)
                .map(feedback -> CoursewareElementType.FEEDBACK)
                .onErrorResume(FeedbackNotFoundException.class, e -> Mono.empty());

        return activityType
                .switchIfEmpty(pathwayType)
                .switchIfEmpty(interactiveType)
                .switchIfEmpty(componentType)
                .switchIfEmpty(scenarioType)
                .switchIfEmpty(feedbackType)
                .switchIfEmpty(Mono.error(new IllegalArgumentFault(String.format("type not found for element %s", elementId))))
                .flatMap(coursewareElementType -> getPath(elementId, coursewareElementType)
                        .flatMap(ancestry -> {
                            // sort from element to root
                            Collections.reverse(ancestry);
                            // remove first element which is the actual element requesting the ancestry
                            ancestry.remove(0);
                            // return the ancestry
                            return Mono.just(new CoursewareElementAncestry()
                                    .setElementId(elementId)
                                    .setType(coursewareElementType)
                                    .setAncestry(ancestry));
                        }));
    }

    /**
     * Find the courseware element ancestry, ordered from the requesting element to the top. The requesting element
     * is excluded from the list.
     *
     * @param coursewareElement the element to find the ancestry for
     * @return the courseware element ancestry
     */
    public Mono<CoursewareElementAncestry> findCoursewareElementAncestry(final CoursewareElement coursewareElement) {
        return getPath(coursewareElement.getElementId(), coursewareElement.getElementType())
                // when no path is found above, return a list with the courseware element
                .defaultIfEmpty(Lists.newArrayList(coursewareElement))
                .flatMap(ancestry -> {
                    // sort from element to root
                    Collections.reverse(ancestry);
                    // remove first element which is the actual element requesting the ancestry
                    ancestry.remove(0);
                    // return the ancestry
                    return Mono.just(new CoursewareElementAncestry()
                            .setElementId(coursewareElement.getElementId())
                            .setType(coursewareElement.getElementType())
                            .setAncestry(ancestry));
                });
    }

    /**
     * Find the courseware element type for a given id
     *
     * @param elementId the element id to find the type for
     * @return a mono with the courseware element
     * @throws NotFoundFault when the element type not found
     */
    @SuppressWarnings("Duplicates")
    @Deprecated
    public Mono<CoursewareElement> findCoursewareElementById(final UUID elementId) {
        Mono<CoursewareElementType> activityType = activityService.findById(elementId)
                .map(activity -> CoursewareElementType.ACTIVITY)
                .onErrorResume(ActivityNotFoundException.class, e -> Mono.empty());

        Mono<CoursewareElementType> pathwayType = pathwayService.findById(elementId)
                .map(pathway -> CoursewareElementType.PATHWAY)
                .onErrorResume(PathwayNotFoundException.class, e -> Mono.empty());

        Mono<CoursewareElementType> interactiveType = interactiveService.findById(elementId)
                .map(interactive -> CoursewareElementType.INTERACTIVE)
                .onErrorResume(InteractiveNotFoundException.class, e -> Mono.empty());

        Mono<CoursewareElementType> componentType = componentService.findById(elementId)
                .map(component -> CoursewareElementType.COMPONENT)
                .onErrorResume(ComponentNotFoundException.class, e -> Mono.empty());

        Mono<CoursewareElementType> scenarioType = scenarioService.findById(elementId)
                .map(scenario -> CoursewareElementType.SCENARIO)
                .onErrorResume(ScenarioNotFoundException.class, e -> Mono.empty());

        Mono<CoursewareElementType> feedbackType = feedbackService.findById(elementId)
                .map(feedback -> CoursewareElementType.FEEDBACK)
                .onErrorResume(FeedbackNotFoundException.class, e -> Mono.empty());

        return activityType
                .switchIfEmpty(pathwayType)
                .switchIfEmpty(interactiveType)
                .switchIfEmpty(componentType)
                .switchIfEmpty(scenarioType)
                .switchIfEmpty(feedbackType)
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("type not found for element %s", elementId))))
                .map(coursewareElementType -> new CoursewareElement()
                        .setElementId(elementId)
                        .setElementType(coursewareElementType));

    }

    /**
     * Fetch configuration fields for a courseware element
     *
     * @param elementId the element id to find the configuration fields for
     * @param fieldNames the name of the fields to find
     * @return a flux with the requested configuration fields
     */
    @Trace(async = true)
    public Flux<ConfigurationField> fetchConfigurationFields(final UUID elementId, final List<String> fieldNames) {
        return fieldNames.stream()
                .map(fieldName -> coursewareGateway.fetchConfigurationField(elementId, fieldName)
                        .defaultIfEmpty(new ConfigurationField()
                                .setFieldName(fieldName))
                        .flux()
                        .doOnEach(ReactiveTransaction.linkOnNext()))
                .reduce(Flux::mergeWith)
                .orElse(Flux.empty())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all sources for a registered scope urn.
     *
     * @param scopeURN the scope urn
     * @param fieldNames the name of the fields to find
     * @return flux of source by scope urn.
     */
    @Trace(async = true)
    public Flux<RegisteredScopeReference> fetchSourcesByScopeUrn(final UUID scopeURN, final List<String> fieldNames) {
        return coursewareGateway.findRegisteredElements(scopeURN)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .concatMap(scopeReference -> {
                    Mono<List<ConfigurationField>> configurationFields = fetchConfigurationFields(scopeReference.getElementId(),
                                                                                                  fieldNames)
                            .collectList()
                            .defaultIfEmpty(new ArrayList<>());

                    // resolve version expr, or if version expr is null return latest version of that plugin id,
                    // will throw PluginNotFoundFault if cant find id and version
                    Mono<PluginManifest> pluginManifestMono = pluginService.findLatestVersion(scopeReference.getPluginId(),
                                                                                              scopeReference.getPluginVersion())
                            .flatMap(version -> pluginGateway.fetchPluginManifestByIdVersion(scopeReference.getPluginId(),
                                                                                             version));

                    return Mono.zip(Mono.just(scopeReference), configurationFields, pluginManifestMono)
                            .map(tuple3 -> {
                                ScopeReference scopeRef = tuple3.getT1();
                                List<ConfigurationField> configFields = tuple3.getT2();
                                PluginManifest pluginManifest = tuple3.getT3();

                                return new RegisteredScopeReference()
                                        .setStudentScopeUrn(scopeRef.getScopeURN())
                                        .setElementId(scopeRef.getElementId())
                                        .setElementType(scopeRef.getElementType())
                                        .setPluginVersion(pluginManifest.getVersion())
                                        .setPluginId(pluginManifest.getPluginId())
                                        .setConfigSchema(pluginManifest.getConfigurationSchema())
                                        .setConfigurationFields(configFields);
                            });
                });
    }

    /**
     * Find a Project summary for the given courseware element and type it belongs to.
     *
     * @param elementId the element id to find the root activity for
     * @param elementType the element type
     * @return a mono with the project object
     */
    @Trace(async = true)
    public Mono<Project> findProjectSummary(final UUID elementId, final CoursewareElementType elementType) {
        checkArgument(elementId != null, "elementId is required");
        checkArgument(elementType != null, "elementType is required");
        return getProjectId(elementId, elementType)
                .flatMap(projectId -> projectService.findById(projectId))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the courseware element type for a given id
     *
     * @param elementId the element id to find the type for
     * @return a mono with the courseware element
     * @throws NotFoundFault when the element type not found
     */
    public Mono<CoursewareElement> findCoursewareElement(final UUID elementId) {
        return coursewareGateway.findElementById(elementId)
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("type not found for element %s", elementId))));

    }
}
