package com.smartsparrow.courseware.service;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.ComponentConfig;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ManualGradingConfiguration;
import com.smartsparrow.courseware.data.ManualGradingConfigurationGateway;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.courseware.lang.ComponentAlreadyExistsFault;
import com.smartsparrow.courseware.lang.ComponentNotFoundException;
import com.smartsparrow.courseware.lang.ComponentParentNotFound;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static reactor.core.publisher.Mono.just;
import static reactor.core.publisher.Mono.zip;

@Singleton
public class ComponentService {

    private static final Logger log = MercuryLoggerFactory.getLogger(ComponentService.class);

    private final ComponentGateway componentGateway;
    private final PluginService pluginService;
    private final InteractiveService interactiveService;
    private final ActivityService activityService;
    private final CoursewareAssetService coursewareAssetService;
    private final CoursewareElementDescriptionService coursewareDescriptionService;
    private final ManualGradingConfigurationGateway manualGradingConfigurationGateway;

    @Inject
    ComponentService(ComponentGateway componentGateway,
                     PluginService pluginService,
                     InteractiveService interactiveService,
                     ActivityService activityService,
                     CoursewareAssetService coursewareAssetService,
                     ManualGradingConfigurationGateway manualGradingConfigurationGateway,
                     CoursewareElementDescriptionService coursewareDescriptionService) {
        this.componentGateway = componentGateway;
        this.pluginService = pluginService;
        this.interactiveService = interactiveService;
        this.activityService = activityService;
        this.coursewareAssetService = coursewareAssetService;
        this.manualGradingConfigurationGateway = manualGradingConfigurationGateway;
        this.coursewareDescriptionService = coursewareDescriptionService;
    }

    /**
     * Creates a Component with a Plugin inside an Interactive if it's not exist
     *
     * @param interactiveId     the interactive id
     * @param pluginId          the plugin id
     * @param pluginVersionExpr the plugin version expression
     * @param config            the component config
     * @param componentId optional component id, if not supplied a new id will be created
     * @return a Mono with created Component
     * @throws PluginNotFoundFault                                           if plugin not found
     * @throws VersionParserFault                                            if plugin version can not be parsed
     * @throws com.smartsparrow.courseware.lang.InteractiveNotFoundException when interactive is not found
     */
    @Trace(async = true)
    public Mono<Component> createForInteractive(final UUID interactiveId,
                                                final UUID pluginId,
                                                final String pluginVersionExpr,
                                                final String config,
                                                final UUID componentId) throws VersionParserFault {

        checkArgument(interactiveId != null, "missing interactive id");
        checkArgument(pluginId != null, "missing plugin id");
        checkArgument(!Strings.isNullOrEmpty(pluginVersionExpr), "missing plugin version");

        // check it does not already exist
        return componentGateway.findById(componentId)
                .hasElement()
                //filter out the value
                .filter(hasElement -> !hasElement)
                // a value was found, throw
                .switchIfEmpty(Mono.error(new ComponentAlreadyExistsFault(componentId)))
                // nothing was found, return the id
                .flatMap(componentNotFound -> {
                    return createComponentForInteractive(interactiveId, pluginId, pluginVersionExpr, config, componentId);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * this method allows to set the id of the component when this is created.
     *
     * @param interactiveId     the interactive id
     * @param pluginId          the plugin id
     * @param pluginVersionExpr the plugin version expression
     * @param config            the component config
     * @return a Mono with created Component
     * @throws PluginNotFoundFault                                           if plugin not found
     * @throws VersionParserFault                                            if plugin version can not be parsed
     * @throws com.smartsparrow.courseware.lang.InteractiveNotFoundException when interactive is not found
     */
    @Trace(async = true)
    public Mono<Component> createForInteractive(final UUID interactiveId,
                                                final UUID pluginId,
                                                final String pluginVersionExpr,
                                                final String config) throws VersionParserFault {
        return createComponentForInteractive(interactiveId, pluginId, pluginVersionExpr, config, UUIDs.timeBased())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Creates a Component with a Plugin inside an Interactive
     *
     * @param interactiveId     the interactive id
     * @param pluginId          the plugin id
     * @param pluginVersionExpr the plugin version expression
     * @param config            the component config
     * @param componentId            the component id
     * @return a Mono with created Component
     * @throws PluginNotFoundFault                                           if plugin not found
     * @throws VersionParserFault                                            if plugin version can not be parsed
     * @throws com.smartsparrow.courseware.lang.InteractiveNotFoundException when interactive is not found
     */
    @Trace(async = true)
    public Mono<Component> createComponentForInteractive(final UUID interactiveId,
                                                    final UUID pluginId,
                                                    final String pluginVersionExpr,
                                                    final String config,
                                                    final UUID componentId) throws VersionParserFault {
        Mono<Component> componentMono = Mono.just(new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr));

        Mono<Component> createdComponent = componentMono.flatMap(component -> interactiveService.findById(interactiveId)
                .then(pluginService.findLatestVersion(pluginId, pluginVersionExpr))
                .thenEmpty(componentGateway.persist(component, interactiveId, CoursewareElementType.INTERACTIVE))
                .thenReturn(component))
                .doOnEach(ReactiveTransaction.linkOnNext());

        if (StringUtils.isNotBlank(config)) {
            UUID componentConfigId = UUIDs.timeBased();
            Mono<ComponentConfig> componentConfigMono = Mono.just(new ComponentConfig()
                    .setId(componentConfigId)
                    .setComponentId(componentId)
                    .setConfig(config));

            return componentConfigMono
                    .flatMap(componentGateway::persist)
                    .then(createdComponent)
                    .doOnEach(ReactiveTransaction.linkOnNext());
        }

        return createdComponent;
    }

    /**
     * Creates a Component with a Plugin inside an Activity if it's not exist
     *
     * @param activityId        the activity id
     * @param pluginId          the plugin id
     * @param pluginVersionExpr the plugin version expression
     * @param config            the component config
     * @param componentId       the component id
     * @return a Mono with created Component
     * @throws PluginNotFoundFault                                        if plugin not found
     * @throws VersionParserFault                                         if plugin version can not be parsed
     * @throws com.smartsparrow.courseware.lang.ActivityNotFoundException when the activity is not found
     * @throws ComponentAlreadyExistsFault if provided component id already exists
     */
    @Trace(async = true)
    public Mono<Component> createForActivity(final UUID activityId,
                                             final UUID pluginId,
                                             final String pluginVersionExpr,
                                             final String config,
                                             final UUID componentId) throws VersionParserFault {

        checkArgument(activityId != null, "missing activity id");
        checkArgument(pluginId != null, "missing plugin id");
        checkArgument(!Strings.isNullOrEmpty(pluginVersionExpr), "missing plugin version");

        // If a component id has been supplied
        // check it does not already exist
        return componentGateway.findById(componentId)
                .hasElement()
                //filter out the value
                .filter(hasElement -> !hasElement)
                // a value was found, throw
                .switchIfEmpty(Mono.error(new ComponentAlreadyExistsFault(componentId)))
                // nothing was found, create component
                .flatMap(componentNotFound -> {
                    return createComponentForActivity(activityId,pluginId, pluginVersionExpr, config, componentId);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * this method allows to set the id of the component when this is created.
     *
     * @param activityId        the activity id
     * @param pluginId          the plugin id
     * @param pluginVersionExpr the plugin version expression
     * @param config            the component config
     * @return a Mono with created Component
     * @throws PluginNotFoundFault                                        if plugin not found
     * @throws VersionParserFault                                         if plugin version can not be parsed
     * @throws com.smartsparrow.courseware.lang.ActivityNotFoundException when the activity is not found
     * @throws ComponentAlreadyExistsFault if provided component id already exists
     */
    @Trace(async = true)
    public Mono<Component> createForActivity(final UUID activityId,
                                             final UUID pluginId,
                                             final String pluginVersionExpr,
                                             final String config) throws VersionParserFault {

        checkArgument(activityId != null, "missing activity id");
        checkArgument(pluginId != null, "missing plugin id");
        checkArgument(!Strings.isNullOrEmpty(pluginVersionExpr), "missing plugin version");

        return createComponentForActivity(activityId,pluginId, pluginVersionExpr, config, UUIDs.timeBased())
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Creates a Component with a Plugin inside an Activity
     *
     * @param activityId        the activity id
     * @param pluginId          the plugin id
     * @param pluginVersionExpr the plugin version expression
     * @param config            the component config
     * @param componentId            the component id
     * @return a Mono with created Component
     * @throws PluginNotFoundFault                                        if plugin not found
     * @throws VersionParserFault                                         if plugin version can not be parsed
     * @throws com.smartsparrow.courseware.lang.ActivityNotFoundException when the activity is not found
     * @throws ComponentAlreadyExistsFault if provided component id already exists
     */
    @Trace(async = true)
    public Mono<Component> createComponentForActivity(final UUID activityId,
                                             final UUID pluginId,
                                             final String pluginVersionExpr,
                                             final String config,
                                             final UUID componentId) throws VersionParserFault {

        Mono<Component> componentMono = Mono.just(new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr));

        Mono<Component> createdComponent = componentMono.flatMap(component -> activityService.findById(activityId)
                        .then(pluginService.findLatestVersion(pluginId, pluginVersionExpr))
                        .thenEmpty(componentGateway.persist(component, activityId, CoursewareElementType.ACTIVITY))
                        .thenReturn(component))
                .doOnEach(ReactiveTransaction.linkOnNext());

        if (StringUtils.isNotBlank(config)) {
            UUID componentConfigId = UUIDs.timeBased();
            Mono<ComponentConfig> componentConfigMono = Mono.just(new ComponentConfig()
                    .setId(componentConfigId)
                    .setComponentId(componentId)
                    .setConfig(config));

            return componentConfigMono
                    .flatMap(componentGateway::persist)
                    .then(createdComponent)
                    .doOnEach(ReactiveTransaction.linkOnNext());
        }

        return createdComponent;

    }


    /**
     * Saves a Configuration for a Component
     *
     * @param componentId the component id to save configuration for
     * @param config      the configuration to save
     * @throws ComponentNotFoundException if component not found
     */
    @Trace(async = true)
    public Mono<ComponentConfig> replaceConfig(final UUID componentId, final String config) {

        checkArgument(componentId != null, "missing component id");
        checkArgument(StringUtils.isNotBlank(config), "missing config");

        UUID componentConfigId = UUIDs.timeBased();
        ComponentConfig componentConfig = new ComponentConfig()
                .setId(componentConfigId)
                .setComponentId(componentId)
                .setConfig(config);

        return findById(componentId)
                .thenEmpty(componentGateway.persist(componentConfig))
                .thenReturn(componentConfig)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Finds a Component by Id
     *
     * @param componentId the component id
     * @return Mono with a Component object
     * @throws ComponentNotFoundException if component not found
     */
    @Trace(async = true)
    public Mono<Component> findById(final UUID componentId) {
        checkArgument(componentId != null, "missing component id");
        return componentGateway.findById(componentId).single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ComponentNotFoundException(componentId);
                });
    }

    /**
     * Finds a list of Components for Interactive
     *
     * @param interactiveId the interactive id
     * @return Flux of Components, can be empty
     */
    public Flux<Component> findByInteractive(final UUID interactiveId) {
        return findIdsByInteractive(interactiveId)
                .flatMap(this::findById);
    }

    /**
     * Finds a list of Component IDs for Interactive
     *
     * @param interactiveId the interactive id
     * @return Flux of Components, can be empty
     */
    @Trace(async = true)
    public Flux<UUID> findIdsByInteractive(final UUID interactiveId) {
        checkArgument(interactiveId != null, "missing interactive id");
        return componentGateway.findComponentIdsByInteractive(interactiveId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Finds a list of component ids for an activity
     *
     * @param activityId the activity to find the components for
     * @return a flux of component ids
     */
    public Flux<UUID> findIdsByActivity(final UUID activityId) {
        checkArgument(activityId != null, "activityId is required");
        return componentGateway.findComponentIdsByActivity(activityId);
    }

    /**
     * Find component ids by parent.
     *
     * @param parentId   the parent id to find the components for
     * @param parentType the parent type
     * @return a flux of component ids
     * @throws UnsupportedOperationException when the supplied parent type is not of either:
     *                                       <br> {@link CoursewareElementType#ACTIVITY}
     *                                       <br> {@link CoursewareElementType#INTERACTIVE}
     */
    public Flux<UUID> findIdsByParentType(final UUID parentId, final CoursewareElementType parentType) {
        switch (parentType) {
            case ACTIVITY:
                return findIdsByActivity(parentId);
            case INTERACTIVE:
                return findIdsByInteractive(parentId);
            default:
                throw new UnsupportedOperationException("invalid parentType supplied");
        }
    }

    /**
     * Finds the latest configuration for a Component
     *
     * @param componentId the component id
     * @return Mono of ComponentConfig, can be empty
     */
    @Trace(async = true)
    public Mono<ComponentConfig> findLatestByConfigId(final UUID componentId) {
        checkArgument(componentId != null, "missing component id");
        return componentGateway.findLatestConfig(componentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }


    /**
     * Fetches the parent element for a given component
     *
     * @param componentId the component to find the parent for
     * @throws ComponentNotFoundException when the component parent element is not found
     */
    @Trace(async = true)
    public Mono<ParentByComponent> findParentFor(final UUID componentId) {
        return componentGateway.findParentBy(componentId)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ComponentParentNotFound(componentId);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Build a component payload object for the given componentId. The config could be empty and is therefore defaulted
     * to an empty string if not found.
     *
     * @param componentId the component to build the payload for
     * @throws ComponentNotFoundException when the component is not found
     * @throws ComponentParentNotFound    when the parent element of the component is not found
     * @throws PluginNotFoundFault        when the plugin is not found
     */
    @Trace(async = true)
    public Mono<ComponentPayload> getComponentPayload(final UUID componentId) {
        Mono<Component> componentMono = findById(componentId)
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<ComponentPayload> payloadMono = componentMono.flatMap(component -> {

            Mono<ComponentConfig> config = findLatestByConfigId(componentId)
                    .defaultIfEmpty(new ComponentConfig().setConfig(""))
                    .doOnEach(ReactiveTransaction.linkOnNext());
            Mono<ParentByComponent> parent = findParentFor(componentId)
                    .doOnEach(ReactiveTransaction.linkOnNext());
            Mono<PluginSummary> plugin = pluginService.find(component.getPluginId())
                    .doOnEach(ReactiveTransaction.linkOnNext());
            Mono<CoursewareElementDescription> descriptionMono = getElementDescriptionByComponentId(componentId)
                    .defaultIfEmpty(new CoursewareElementDescription())
                    .doOnEach(ReactiveTransaction.linkOnNext());
            Mono<List<PluginFilter>> pluginFilters = pluginService.fetchPluginFiltersByIdVersionExpr(component.getPluginId(), component.getPluginVersionExpr())
                    .doOnEach(ReactiveTransaction.linkOnNext());

            return zip(just(component), config, parent, plugin, descriptionMono, pluginFilters)
                    .map(tuple6 -> ComponentPayload.from(
                            tuple6.getT1(),
                            tuple6.getT2().getConfig(),
                            tuple6.getT4(),
                            tuple6.getT3(),
                            tuple6.getT5(),
                            tuple6.getT6()
                    ));
        })
                .doOnEach(ReactiveTransaction.linkOnNext());

        payloadMono = payloadMono.flatMap(payload -> coursewareAssetService.getAssetPayloads(componentId)
                .doOnSuccess(payload::setAssets)
                .thenReturn(payload))
                .doOnEach(ReactiveTransaction.linkOnNext());
        return payloadMono;
    }

    /**
     * Get the descriptive json for a component
     *
     * @param componentId the component id
     * @return a mono of courseware element description or empty if none are found
     */
    @Trace(async = true)
    public Mono<CoursewareElementDescription> getElementDescriptionByComponentId(final UUID componentId) {
        affirmArgument(componentId != null, "componentId is required");
        return coursewareDescriptionService.fetchCoursewareDescriptionByElement(componentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete an interactive component. The method erase the component relationship with its parent interactive. The
     * component will continue to exist in the component table
     *
     * @param componentId   the component id to delete
     * @param interactiveId the interactive id to detach the component from
     */
    @Trace(async = true)
    public Mono<Void> deleteInteractiveComponent(final UUID componentId, final UUID interactiveId) {
        return componentGateway.deleteInteractiveComponent(componentId, interactiveId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete an activity component. The method erase the component relationship with its parent activity. The
     * component will continue to exist in the component table
     *
     * @param componentId the component id to delete
     * @param activityId  the activity id to detach the component from
     */
    @Trace(async = true)
    public Mono<Void> deleteActivityComponent(final UUID componentId, final UUID activityId) {
        return componentGateway.deleteActivityComponent(componentId, activityId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate a component and attach it to the new parentId
     *
     * @param component  the component to duplicate
     * @param parentId   the parent to attach the component to
     * @param parentType the parent type
     * @return a mono of the duplicated component
     */
    @Trace(async = true)
    public Mono<Component> duplicateComponent(final Component component, final UUID parentId, final CoursewareElementType parentType) {

        checkArgument(parentType != null, "parentType is required");

        switch (parentType) {
            case ACTIVITY:
                return createForActivity(parentId, component.getPluginId(), component.getPluginVersionExpr(), null)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            case INTERACTIVE:
                return createForInteractive(parentId, component.getPluginId(), component.getPluginVersionExpr(), null)
                        .doOnEach(ReactiveTransaction.linkOnNext());
            default:
                throw new UnsupportedOperationException(String.format("parentType %s not allowed for component", parentType));
        }
    }

    /**
     * Duplicate an existing component given the id
     *
     * @param componentId the id of the component to duplicate
     * @param parentId    the parent id to attach the duplicated component to
     * @param parentType  the parent type
     * @param context     keeps the mapping between old ids and new ids, used to replace ids in duplicated config
     * @return a mono of the duplicated component
     */
    @Trace(async = true)
    public Mono<Component> duplicate(final UUID componentId, final UUID parentId, final CoursewareElementType parentType, final DuplicationContext context) {

        return findById(componentId)
                //copy the component itself and attach to parent
                .flatMap(component -> duplicateComponent(component, parentId, parentType)
                        .doOnSuccess(copied -> context.putIds(component.getId(), copied.getId())))
                //copy config
                .flatMap(copied -> findLatestByConfigId(componentId)
                        .flatMap(componentConfig -> replaceConfig(copied.getId(), context.replaceIds(componentConfig.getConfig())))
                        .thenReturn(copied))
                //copy assets
                .flatMap(newComponent -> coursewareAssetService.duplicateAssets(componentId, newComponent.getId(), CoursewareElementType.COMPONENT, context)
                        .then(Mono.just(newComponent)))
                // copy manual grading configuration
                .flatMap(newComponent -> duplicateManualGradingConfiguration(componentId, newComponent.getId())
                        .then(Mono.just(newComponent)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate the manual grading configurations for a component
     *
     * @param oldComponentId the id of the component to duplicate the manual grading configurations for
     * @param newComponentId the new component id to save the manual grading configurations for
     * @return a mono of the duplicated manual grading configurations or an empty mono when no configurations are found
     */
    @Trace(async = true)
    public Mono<ManualGradingConfiguration> duplicateManualGradingConfiguration(final UUID oldComponentId, final UUID newComponentId) {
        return findManualGradingConfiguration(oldComponentId)
                .flatMap(manualGradingConfiguration -> createManualGradingConfiguration(newComponentId, manualGradingConfiguration.getMaxScore()))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Create a manual grading configuration object
     *
     * @param componentId the component id to create the configurations for
     * @param maxScore    the maxScore property of the configuration
     * @return a mono of the created manual grading configuration
     */
    @Trace(async = true)
    public Mono<ManualGradingConfiguration> createManualGradingConfiguration(final UUID componentId, final Double maxScore) {

        affirmArgument(componentId != null, "componentId is required");

        ManualGradingConfiguration configuration = new ManualGradingConfiguration()
                .setComponentId(componentId);

        if(maxScore !=null){
            configuration.setMaxScore(maxScore);
        }

        return manualGradingConfigurationGateway.persist(configuration)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .thenReturn(configuration);
    }

    /**
     * Delete a manual grading configuration object
     *
     * @param componentId the component id to delete the manual grading configuration for
     * @return a flux of void
     */
    public Flux<Void> deleteManualGradingConfiguration(final UUID componentId) {

        affirmArgument(componentId != null, "componentId is required");

        return manualGradingConfigurationGateway.delete(new ManualGradingConfiguration()
                .setComponentId(componentId));
    }

    /**
     * Find the manual grading configuration for a component
     *
     * @param componentId the component id to find the configurations for
     * @return a mono of manual grading configuration or an empty stream when not found
     */
    @Trace(async = true)
    public Mono<ManualGradingConfiguration> findManualGradingConfiguration(final UUID componentId) {

        affirmArgument(componentId != null, "componentId is required");

        return manualGradingConfigurationGateway.find(componentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the component ids for plugin id.
     *
     * @param pluginId the plugin id to find the component ids for
     * @return either a Mono List of UUIDs or an empty stream if none are found
     */
    @Trace(async = true)
    public Mono<List<UUID>> findComponentIds(final UUID pluginId) {
        checkArgument(pluginId != null, "plugin Id is required");
        return componentGateway.findComponentIdsByPluginId(pluginId).collectList()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Restore components for an interactive or activity
     * @param componentIds the component ids
     * @param parentId the parent id
     * @param parentElementType the parent type
     * @return flux of component
     */
    @Trace(async = true)
    public Flux<Component> restoreComponent(final List<UUID> componentIds,
                                            final UUID parentId,
                                            final CoursewareElementType parentElementType) {

        affirmArgumentNotNullOrEmpty(componentIds, "componentIds required");
        affirmArgument(parentId != null, "parentId is required");
        affirmArgument(parentElementType != null, "parentElementType is required");

        return componentIds.stream()
                .map(componentId -> {
                    return componentGateway.findById(componentId)
                            .switchIfEmpty(Mono.error(new NotFoundFault(String.format(
                                    "cannot find component for component id: %s",
                                    componentId))))
                            .flatMap(component -> componentGateway.restoreComponent(componentId,
                                                                                    parentId,
                                                                                    parentElementType).thenReturn(
                                    component)).flux();
                })
                .reduce(Flux::concatWith)
                .orElse(Flux.empty())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches the parent element for given component list
     *
     * @param componentIds the list of component ids
     * @throws ComponentNotFoundException when the component parent element is not found
     */
    @Trace(async = true)
    public Mono<List<ParentByComponent>> findParentForComponents(final List<UUID> componentIds) {
        return componentIds.stream()
                .map(componentId -> {
                    return findParentFor(componentId).flux();
                })
                .reduce(Flux::concatWith)
                .orElse(Flux.empty())
                .collectList()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete an interactive components. The method erase all components relationship with its parent interactive. The
     * component will continue to exist in the component table
     *
     * @param componentIds component ids to delete
     * @param interactiveId the interactive id to detach the component from
     */
    @Trace(async = true)
    public Flux<Void> deleteInteractiveComponents(final List<UUID> componentIds, final UUID interactiveId) {
        affirmArgumentNotNullOrEmpty(componentIds, "componentIds required");
        affirmArgument(interactiveId != null, "interactiveId is required");

        return componentIds.stream()
                .map(componentId -> {
                    return componentGateway.deleteInteractiveComponent(componentId, interactiveId).flux();
                })
                .reduce(Flux::concatWith)
                .orElse(Flux.empty())
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Move components from one element to another element
     *
     * @param componentIds component ids to move
     * @param elementId the element id to move the component into
     * @param elementType the element type to move the component into
     */
    public Flux<Void> move(List<UUID> componentIds, UUID elementId, CoursewareElementType elementType) {
        affirmArgumentNotNullOrEmpty(componentIds, "componentIds are required");
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(elementType != null, "elementType is required");

        return componentIds.stream()
                .map(componentId -> findParentFor(componentId).flux())
                .reduce(Flux::concatWith)
                .orElse(Flux.empty())
                .flatMap(parentByComponent ->
                        detach(parentByComponent.getComponentId(), parentByComponent.getParentId(), parentByComponent.getParentType())
                        .thenMany(saveRelationship(parentByComponent.getComponentId(), elementId, elementType)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Detach the component from its parent element id. The relationships are delete, however the component
     * still exists in the component table and is not marked as deleted.
     *
     * @param componentId      the id of the component to delete
     * @param elementId the id of the parent element to detach the component from
     * @throws IllegalArgumentException when wither method argument is <code>null</code>
     */
    @Trace(async = true)
    private Mono<Void> detach(final UUID componentId, final UUID elementId, final CoursewareElementType elementType) {
        affirmArgument(componentId != null, "componentId is required");
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(elementType != null, "elementType is required");

        Mono<Void> mono;
        switch (elementType) {
            case ACTIVITY:
                mono = componentGateway.deleteActivityComponent(componentId, elementId);
                break;
            case INTERACTIVE:
                mono = componentGateway.deleteInteractiveComponent(componentId, elementId);
                break;
            default:
                throw new UnsupportedOperationException(
                        String.format("parentType %s not allowed for component", elementType));
        }
        return mono.doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the child-parent relationship for component.
     *
     * @param componentId   the component id
     * @param elementId     the parent element id
     * @param elementType   the parent element type
     */
    @Trace(async = true)
    private Mono<Void> saveRelationship(final UUID componentId, final UUID elementId, final CoursewareElementType elementType) {
        affirmArgument(componentId != null, "missing componentId");
        affirmArgument(elementId != null, "missing elementId");
        affirmArgument(elementType != null, "missing elementType");

        return componentGateway.restoreComponent(componentId, elementId, elementType)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
