package com.smartsparrow.learner.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.ComponentConfig;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.lang.ComponentNotFoundException;
import com.smartsparrow.courseware.lang.ComponentParentNotFound;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerActivityGateway;
import com.smartsparrow.learner.data.LearnerComponent;
import com.smartsparrow.learner.data.LearnerComponentGateway;
import com.smartsparrow.learner.data.LearnerInteractiveGateway;
import com.smartsparrow.learner.data.ParentByLearnerComponent;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.PublishComponentException;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerComponentService {

    private final LearnerComponentGateway learnerComponentGateway;
    private final LearnerActivityGateway learnerActivityGateway;
    private final LearnerInteractiveGateway learnerInteractiveGateway;
    private final ComponentService componentService;
    private final LearnerAssetService learnerAssetService;
    private final DeploymentLogService deploymentLogService;
    private final LearnerService learnerService;
    private final ManualGradeService manualGradeService;
    private final PluginService pluginService;
    private final LearnerSearchableDocumentService learnerSearchableDocumentService;
    private final AnnotationService annotationService;
    private final CoursewareService coursewareService;

    @Inject
    public LearnerComponentService(LearnerComponentGateway learnerComponentGateway,
                                   LearnerActivityGateway learnerActivityGateway,
                                   LearnerInteractiveGateway learnerInteractiveGateway,
                                   ComponentService componentService,
                                   LearnerAssetService learnerAssetService,
                                   DeploymentLogService deploymentLogService,
                                   LearnerService learnerService,
                                   ManualGradeService manualGradeService,
                                   PluginService pluginService,
                                   LearnerSearchableDocumentService learnerSearchableDocumentService,
                                   AnnotationService annotationService,
                                   CoursewareService coursewareService) {
        this.learnerComponentGateway = learnerComponentGateway;
        this.learnerActivityGateway = learnerActivityGateway;
        this.learnerInteractiveGateway = learnerInteractiveGateway;
        this.componentService = componentService;
        this.learnerAssetService = learnerAssetService;
        this.deploymentLogService = deploymentLogService;
        this.learnerService = learnerService;
        this.manualGradeService = manualGradeService;
        this.pluginService = pluginService;
        this.learnerSearchableDocumentService = learnerSearchableDocumentService;
        this.annotationService = annotationService;
        this.coursewareService = coursewareService;
    }

    /**
     * Publish all components for a parent element. Find all the component for the supplied parent, converts them to a
     * learner component object, persist to the database and save the relationship with the parent.
     *
     * @param parentId the parent to deploy the components for
     * @param deployment the deployment to deploy the components to
     * @param parentType the parent type
     * @return a flux of learner components
     * @throws PublishComponentException when either:
     * <br> parentId is <code>null</code>
     * <br> deployment is <code>null</code>
     * <br> parentType is <code>null</code>
     * <br> component is not found
     */
    public Flux<LearnerComponent> publish(final UUID parentId, final DeployedActivity deployment, final CoursewareElementType parentType,
                                          final boolean lockPluginVersionEnabled) {

        try {
            checkArgument(deployment != null, "deployment is required");
            checkArgument(parentType != null, "parentType is required");
            checkArgument(parentId != null, "parentId is required");
        } catch (IllegalArgumentException e) {
            throw new PublishComponentException(parentId, e.getMessage());
        }

        return componentService.findIdsByParentType(parentId, parentType)
                .collectList()
                .flatMapMany(components -> publish(parentId, parentType, deployment, components, lockPluginVersionEnabled));
    }

    /**
     * For each component id build a learner component and persist to the database.
     *
     * @param parentId the parent of the components
     * @param parentType the type of parent
     * @param deployment the deployment to deploy the learner components to
     * @param componentIds the list of componentIds to convert to learner component
     * @return a flux of learner component
     */
    public Flux<LearnerComponent> publish(final UUID parentId, final CoursewareElementType parentType, final DeployedActivity deployment,
                                          final List<UUID> componentIds, final boolean lockPluginVersionEnabled) {

        if (componentIds.isEmpty()) {
            return Flux.empty();
        }

        return componentIds.stream()
                .map(componentId -> build(componentId, deployment, parentId, lockPluginVersionEnabled)
                        .flux()
                        .doOnError(throwable -> {
                            deploymentLogService.logFailedStep(deployment, componentId, CoursewareElementType.COMPONENT,
                                    "[learnerComponentService] " + Arrays.toString(throwable.getStackTrace()))
                                    .subscribe();
                            throw Exceptions.propagate(throwable);
                        }))
                .reduce(Flux::concat)
                .orElse(Flux.empty())
                .concatMap(component -> persist(component, parentId, parentType, deployment));
    }

    /**
     * Build a learner component for a deployment.
     *
     * @param componentId the component id to find and convert to a learner component
     * @param deployment the deployment id to associate with the learner component
     * @return a mono of newly created learner component
     * @throws PublishComponentException when the component is not found
     */
    private Mono<LearnerComponent> build(final UUID componentId, final DeployedActivity deployment, final UUID parentId, final boolean lockPluginVersionEnabled) {
        Mono<Component> componentMono = componentService.findById(componentId);
        Mono<ComponentConfig> componentConfigMono = componentService.findLatestByConfigId(componentId)
                .defaultIfEmpty(new ComponentConfig());

        return Mono.zip(componentMono, componentConfigMono, Mono.just(deployment))
                .flatMap(tuple3 -> deploymentLogService.logProgressStep(deployment, componentId, CoursewareElementType.COMPONENT,
                        "[learnerComponentService] started publishing component")
                        .thenReturn(tuple3))
                .doOnError(ComponentNotFoundException.class, ex -> {
                    throw new PublishComponentException(parentId, ex.getMessage());
                })
                .map(tuple -> new LearnerComponent()
                        .setId(tuple.getT1().getId())
                        .setPluginId(tuple.getT1().getPluginId())
                        .setPluginVersionExpr(pluginService.resolvePluginVersion(tuple.getT1().getPluginId(),
                                tuple.getT1().getPluginVersionExpr(), lockPluginVersionEnabled))
                        .setConfig(tuple.getT2().getConfig())
                        .setDeploymentId(tuple.getT3().getId())
                        .setChangeId(tuple.getT3().getChangeId()));
    }

    /**
     * Persist the learner component along with its parent child relationship with the given parent element.
     *
     * @param component the component to persist
     * @param parentId the parent id to save as the component parent
     * @param type the parent type
     * @return a mono of learner component
     */
    private Mono<LearnerComponent> persist(final LearnerComponent component, final UUID parentId, final CoursewareElementType type, final Deployment deployment) {

        // find manual grading configuration and publish it if exists
        final Mono<Void> publishManualGradingConfiguration = componentService.findManualGradingConfiguration(component.getId())
                .flatMap(manualGradingConfiguration -> manualGradeService
                        .publishManualGradingConfiguration(manualGradingConfiguration, deployment, parentId, type)
                        .singleOrEmpty());

        //find root element from the path
        final UUID rootElementId = coursewareService.getRootElementId(component.getId(), component.getElementType()).block();

        return learnerComponentGateway.persist(component)
                .thenMany(learnerAssetService.publishAssetsFor(deployment, component.getId(), CoursewareElementType.COMPONENT))
                .then(deploymentLogService.logProgressStep(deployment, component.getId(), CoursewareElementType.COMPONENT,
                        "[learnerComponentService] finished publishing assets"))
                .thenMany(learnerAssetService.publishMathAssetsFor(deployment, component.getId(), CoursewareElementType.COMPONENT))
                .then(deploymentLogService.logProgressStep(deployment, component.getId(), CoursewareElementType.COMPONENT,
                        "[learnerComponentService] finished publishing math assets"))
                .then(learnerComponentGateway.persistParent(new ParentByLearnerComponent()
                        .setParentId(parentId)
                        .setParentType(type)
                        .setComponentId(component.getId())
                        .setDeploymentId(component.getDeploymentId())
                        .setChangeId(component.getChangeId()))
                        .singleOrEmpty()
                        .thenReturn(component))
                .flatMap(learnerComponent -> persistChildRelationship(learnerComponent, parentId, type))
                .then(deploymentLogService.logProgressStep(deployment, component.getId(), CoursewareElementType.COMPONENT,
                        "[learnerComponentService] finished persisting parent/child relationship"))
                .thenMany(learnerService.publishConfigurationFields(deployment, component.getId()))
                .then(deploymentLogService.logProgressStep(deployment, component.getId(), CoursewareElementType.COMPONENT,
                        "[learnerComponentService] finished publishing configuration fields"))
                .then(publishManualGradingConfiguration)
                .then(deploymentLogService.logProgressStep(deployment, component.getId(), CoursewareElementType.COMPONENT,
                        "[learnerComponentService] finished publishing manual grading configuration"))
                .thenMany(learnerSearchableDocumentService.publishSearchableDocuments(component, deployment.getCohortId()))
                .then(deploymentLogService.logProgressStep(deployment, component.getId(), CoursewareElementType.COMPONENT,
                        "[learnerComponentService] finished mapping component searchable fields"))
                .thenMany(annotationService.publishAnnotationMotivations(rootElementId, component.getId(), deployment.getId(), deployment.getChangeId()))
                .then(deploymentLogService.logProgressStep(deployment, component.getId(), CoursewareElementType.COMPONENT,
                        "[learnerComponentService] finished publishing annotation motivations"))
                .then(Mono.just(component));
    }

    /**
     * Persist the learner component child relationship with its parent element.
     *
     * @param learnerComponent to persist the child relationship for
     * @param parentId the parent id of the component
     * @param type the type of parent
     * @return a mono of void
     */
    private Mono<Void> persistChildRelationship(final LearnerComponent learnerComponent, final UUID parentId, final CoursewareElementType type) {
        switch (type) {
            case INTERACTIVE:
                return learnerInteractiveGateway.persistChildComponent(
                        parentId,
                        learnerComponent.getDeploymentId(),
                        learnerComponent.getChangeId(),
                        learnerComponent.getId()
                ).singleOrEmpty();
            case ACTIVITY:
                return learnerActivityGateway.persistChildComponent(
                        parentId,
                        learnerComponent.getDeploymentId(),
                        learnerComponent.getChangeId(),
                        learnerComponent.getId()
                ).singleOrEmpty();
            default:
                throw new UnsupportedOperationException("learner component parent type `" + type + "` not supported");
        }
    }

    /**
     * Find components for the parent (interactive or activity)
     * @param parentId the parent id
     * @param parentType the parent type
     * @param deploymentId the deployment id
     * @throws IllegalArgumentFault if paren type is different from INTERACTIVE or ACTIVITY
     * @return flux of LearnerComponent, empty flux if no components are found
     */
    @Trace(async = true)
    public Flux<LearnerComponent> findComponents(final UUID parentId, final CoursewareElementType parentType, final UUID deploymentId) {
        Mono<List<UUID>> components;
        switch(parentType) {
            case ACTIVITY: {
                components = learnerActivityGateway.findChildComponents(parentId, deploymentId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
                break;
            }
            case INTERACTIVE: {
                components = learnerInteractiveGateway.findChildrenComponent(parentId, deploymentId)
                        .doOnEach(ReactiveTransaction.linkOnNext());
                break;
            }
            default: {
                throw new IllegalArgumentFault("Unsupported type " + parentType);
            }
        }
        return components
                .flatMapIterable(list -> list)
                .flatMap(id -> learnerComponentGateway.findLatestDeployed(id, deploymentId))
                .doOnEach(ReactiveTransaction.linkOnNext());


    }

    @Trace(async = true)
    public Mono<ParentByLearnerComponent> findParentFor(final UUID componentId, final UUID deploymentId) {
        return learnerComponentGateway.findParent(componentId, deploymentId)
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ComponentParentNotFound(componentId);
                });
    }

    /**
     * Find the learner component by id
     *
     * @param componentId the component id
     * @param deploymentId the deployment id to find the component at
     * @return a mono with the learner component or empty mono when not found
     */
    public Mono<LearnerComponent> findComponent(final UUID componentId, final UUID deploymentId) {
        return learnerComponentGateway.findLatestDeployed(componentId, deploymentId);
    }

}
