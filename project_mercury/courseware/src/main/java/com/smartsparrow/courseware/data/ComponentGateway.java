package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ComponentGateway {

    private final static Logger log = LoggerFactory.getLogger(ComponentGateway.class);

    private final Session session;

    private final ComponentMutator componentMutator;
    private final ChildComponentByInteractiveMutator childComponentByInteractiveMutator;
    private final ChildComponentByActivityMutator childComponentByActivityMutator;
    private final ComponentConfigurationMutator componentConfigurationMutator;
    private final ParentByComponentMutator parentByComponentMutator;
    private final ComponentMaterializer componentMaterializer;
    private final ChildComponentByInteractiveMaterializer childComponentByInteractiveMaterializer;
    private final ChildComponentByActivityMaterializer childComponentByActivityMaterializer;
    private final ComponentConfigMaterializer componentConfigMaterializer;
    private final ParentByComponentMaterializer parentByComponentMaterializer;
    private final ComponentByPluginMutator componentByPluginMutator;
    private final ComponentByPluginMaterializer componentByPluginMaterializer;
    private final ElementMutator elementMutator;

    @Inject
    public ComponentGateway(Session session,
                            ComponentMutator componentMutator,
                            ChildComponentByInteractiveMutator childComponentByInteractiveMutator,
                            ChildComponentByActivityMutator childComponentByActivityMutator,
                            ComponentConfigurationMutator componentConfigurationMutator,
                            ParentByComponentMutator parentByComponentMutator,
                            ComponentMaterializer componentMaterializer,
                            ChildComponentByInteractiveMaterializer childComponentByInteractiveMaterializer,
                            ChildComponentByActivityMaterializer childComponentByActivityMaterializer,
                            ComponentConfigMaterializer componentConfigMaterializer,
                            ParentByComponentMaterializer parentByComponentMaterializer,
                            ComponentByPluginMutator componentByPluginMutator,
                            ComponentByPluginMaterializer componentByPluginMaterializer,
                            ElementMutator elementMutator) {
        this.session = session;
        this.componentMutator = componentMutator;
        this.childComponentByInteractiveMutator = childComponentByInteractiveMutator;
        this.childComponentByActivityMutator = childComponentByActivityMutator;
        this.componentConfigurationMutator = componentConfigurationMutator;
        this.parentByComponentMutator = parentByComponentMutator;
        this.componentMaterializer = componentMaterializer;
        this.childComponentByInteractiveMaterializer = childComponentByInteractiveMaterializer;
        this.childComponentByActivityMaterializer = childComponentByActivityMaterializer;
        this.componentConfigMaterializer = componentConfigMaterializer;
        this.parentByComponentMaterializer = parentByComponentMaterializer;
        this.componentByPluginMutator = componentByPluginMutator;
        this.componentByPluginMaterializer = componentByPluginMaterializer;
        this.elementMutator = elementMutator;
    }

    private ComponentConfig toComponentConfig(Row row) {
        return new ComponentConfig()
                .setId(row.getUUID("id"))
                .setComponentId(row.getUUID("component_id"))
                .setConfig(row.getString("config"));
    }

    private Component toComponent(Row row) {
        return new Component()
                .setId(row.getUUID("id"))
                .setPluginId(row.getUUID("plugin_id"))
                .setPluginVersionExpr(row.getString("plugin_version_expr"));
    }

    @Trace(async = true)
    public Mono<Void> persist(final Component component, final UUID parentId, final CoursewareElementType parentType) {
        ParentByComponent parentByComponent = new ParentByComponent()
                .setComponentId(component.getId())
                .setParentId(parentId)
                .setParentType(parentType);

        CoursewareElement componentElement = new CoursewareElement()
                .setElementId(component.getId())
                .setElementType(CoursewareElementType.COMPONENT);

        Statement saveAsChild;
        switch (parentType) {
            case ACTIVITY:
                saveAsChild = childComponentByActivityMutator.insert(component.getId(), parentId);
                break;
            case INTERACTIVE:
                saveAsChild = childComponentByInteractiveMutator.insert(component.getId(), parentId);
                break;
            default:
                throw new UnsupportedOperationException(String .format("Wrong parent type '%s'. " +
                        "Component can be added to activity or interactive only", parentType));
        }

        return Mutators.execute(session, Flux.just(componentMutator.upsert(component),
                saveAsChild,
                componentByPluginMutator.upsert(component),
                parentByComponentMutator.upsert(parentByComponent),
                elementMutator.upsert(componentElement)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error(String.format("Error create component %s", component), e);
                    throw Exceptions.propagate(e);
                })
                .singleOrEmpty();
    }

    @Trace(async = true)
    public Mono<Void> persist(final ComponentConfig componentConfig) {
        return Mutators.execute(session, Flux.just(componentConfigurationMutator.upsert(componentConfig)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error(String.format("Error replace component %s", componentConfig), e);
                    throw Exceptions.propagate(e);
                })
                .singleOrEmpty();
    }

    @Trace(async = true)
    public Flux<UUID> findComponentIdsByInteractive(final UUID interactiveId) {
        return ResultSets.query(session, childComponentByInteractiveMaterializer.fetchAllBy(interactiveId))
                .flatMapIterable(row -> row)
                .map(childComponentByInteractiveMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    public Flux<UUID> findComponentIdsByActivity(final UUID activityId) {
        return ResultSets.query(session, childComponentByActivityMaterializer.fetchAllBy(activityId))
                .flatMapIterable(row -> row)
                .map(childComponentByActivityMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    public Mono<Component> findById(final UUID componentId) {
        return ResultSets.query(session, componentMaterializer.findById(componentId))
                .flatMapIterable(row -> row)
                .map(this::toComponent)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error(String.format("Error: findById, with componentId %s", componentId), e);
                    throw Exceptions.propagate(e);
                });
    }

    /**
     * Fetches all component Ids for plugin id
     *
     * @param pluginId the plugin id to find component ids for
     */
    public Flux<UUID> findComponentIdsByPluginId(final UUID pluginId) {
        return ResultSets.query(session, componentByPluginMaterializer.fetchAllBy(pluginId))
                .flatMapIterable(row -> row)
                .map(componentByPluginMaterializer::fromRow);
    }

    /**
     * Fetches all component Ids for plugin id and version
     *
     * @param pluginId the plugin id to find component ids for
     * @param pluginVersion the plugin version to find component ids for
     */
    @Trace(async = true)
    public Flux<UUID> findComponentIdsByPluginIdAndVersion(final UUID pluginId, final String pluginVersion) {
        return ResultSets.query(session, componentByPluginMaterializer.fetchAllBy(pluginId))
                .flatMapIterable(row -> row)
                .map(componentByPluginMaterializer::fromRowToComponent)
                .filter(component -> pluginVersion.equalsIgnoreCase(component.getPluginVersionExpr()))
                .map(Component::getId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    @Trace(async = true)
    public Mono<ComponentConfig> findLatestConfig(final UUID componentId) {
        return ResultSets.query(session, componentConfigMaterializer.fetchLatestConfig(componentId))
                .flatMapIterable(row -> row)
                .map(this::toComponentConfig)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error(String.format("Error: findLatestConfig, with componentId %s", componentId), e);
                    throw Exceptions.propagate(e);
                });
    }

    /**
     * Find the parent element for a given component
     *
     * @param componentId the component it to find the parent for
     * @return a parentByComponent mono or empty stream if not found
     */
    @Trace(async = true)
    public Mono<ParentByComponent> findParentBy(final UUID componentId) {
        return ResultSets.query(session, parentByComponentMaterializer.fetchById(componentId))
                .flatMapIterable(one->one)
                .map(parentByComponentMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete an interactive component by removing the relationship with its parent element
     *
     * @param componentId the component id to delete
     * @param interactiveId the parent interactive id to delete the relationship with
     */
    @Trace(async = true)
    public Mono<Void> deleteInteractiveComponent(final UUID componentId, final UUID interactiveId) {
        ParentByComponent parent = new ParentByComponent()
                .setParentId(interactiveId)
                .setComponentId(componentId)
                .setParentType(CoursewareElementType.INTERACTIVE);

        return Mutators.execute(session, Flux.just(
                childComponentByInteractiveMutator.deleteComponent(componentId, interactiveId),
                parentByComponentMutator.delete(parent)))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete an activity component by removing the relationship with its parent element
     *
     * @param componentId the component id to delete
     * @param activityId the parent activity id to delete the relationship with
     */
    @Trace(async = true)
    public Mono<Void> deleteActivityComponent(final UUID componentId, final UUID activityId) {
        ParentByComponent parent = new ParentByComponent()
                .setParentId(activityId)
                .setComponentId(componentId)
                .setParentType(CoursewareElementType.ACTIVITY);

        return Mutators.execute(session, Flux.just(
                childComponentByActivityMutator.deleteComponent(componentId, activityId),
                parentByComponentMutator.delete(parent))
                .doOnEach(ReactiveTransaction.linkOnNext()))
                .singleOrEmpty();
    }

    /**
     * Restore deleted component
     * @param componentId the componentId
     * @param parentId the parentId
     * @param parentType the parentType
     * @return mono of void
     */
    @Trace(async = true)
    public Mono<Void> restoreComponent(final UUID componentId,
                                       final UUID parentId,
                                       final CoursewareElementType parentType) {
        ParentByComponent parentByComponent = new ParentByComponent()
                .setComponentId(componentId)
                .setParentId(parentId)
                .setParentType(parentType);

        Statement saveAsChild;
        switch (parentType) {
            case ACTIVITY:
                saveAsChild = childComponentByActivityMutator.insert(componentId, parentId);
                break;
            case INTERACTIVE:
                saveAsChild = childComponentByInteractiveMutator.insert(componentId, parentId);
                break;
            default:
                throw new UnsupportedOperationException(String.format("Wrong parent type '%s'. " +
                                                                              "Component can be added to activity or interactive only",
                                                                      parentType));
        }

        return Mutators.execute(session, Flux.just(saveAsChild,
                                                   parentByComponentMutator.upsert(parentByComponent)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error(String.format("Error creating component %s", componentId), e);
                    throw Exceptions.propagate(e);
                })
                .singleOrEmpty();
    }
}
