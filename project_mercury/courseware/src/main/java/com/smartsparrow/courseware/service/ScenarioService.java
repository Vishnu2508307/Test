package com.smartsparrow.courseware.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ParentByScenario;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.data.ScenarioByParent;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.ScenarioGateway;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.lang.ScenarioNotFoundException;
import com.smartsparrow.courseware.lang.ScenarioParentNotFoundException;
import com.smartsparrow.eval.deserializer.ActionDeserializer;
import com.smartsparrow.eval.deserializer.ConditionDeserializer;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ScenarioService {

    private final static Logger log = LoggerFactory.getLogger(ScenarioService.class);

    private final ScenarioGateway scenarioGateway;
    private final ConditionDeserializer conditionDeserializer;
    private final ActionDeserializer actionDeserializer;

    @Inject
    public ScenarioService(final ScenarioGateway scenarioGateway,
                           final ConditionDeserializer conditionDeserializer,
                           final ActionDeserializer actionDeserializer) {
        this.scenarioGateway = scenarioGateway;
        this.conditionDeserializer = conditionDeserializer;
        this.actionDeserializer = actionDeserializer;
    }

    /**
     * Create a scenario and its corresponding interactive mapping. Condition and action can be defined later
     *
     * @param condition - condition json string
     * @param action - action json string
     * @param scenarioLifecycle - defines at which stage the scenario should be evaluated
     * @param name - the scenario name
     * @param description - the scenario description
     * @param correctness - the correctness of the scenario
     * @throws IllegalArgumentException when <code>scenarioLifecycle</code>, <code>name</code> or <code>parentId</code>
     * are <code>null</code>
     * @return a mono of with the created scenario
     */
    @Trace(async = true)
    public Mono<Scenario> create(final String condition, final String action,
                                 final String name, final String description, final ScenarioCorrectness correctness,
                                 final ScenarioLifecycle scenarioLifecycle,
                                 final UUID parentId, final CoursewareElementType parentType) {

        if (log.isDebugEnabled()) {
            log.debug("Create scenario with condition {} action {}", condition, action);
        }

        checkArgument(scenarioLifecycle != null, "scenarioLifecycle is required");
        checkArgument(name != null, "name is required");
        checkArgument(parentId != null, "parentId is required");

        //Setup scenario object
        UUID scenarioId = UUIDs.timeBased();
        final Scenario scenario = new Scenario()
                .setId(scenarioId)
                .setCondition(condition)
                .setActions(action)
                .setLifecycle(scenarioLifecycle)
                .setName(name)
                .setDescription(description)
                .setCorrectness(correctness);

        return validateScenario(scenario)
                .then(scenarioGateway.persist(scenario, parentId, parentType)
                        .doOnSuccess(s -> log.info("Created scenario {}", s))
                        .thenReturn(scenario))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Check that the scenario condition can be parsed successfully.
     *
     * @param condition the condition to check
     * @return a mono of boolean with value <code>true</code> when the condition is valid
     * @throws IllegalArgumentFault when the condition cannot be parsed
     */
    private Mono<Boolean> validateCondition(final String condition) {
        return conditionDeserializer.deserialize(condition)
                .flatMap(deserialized -> Mono.just(true))
                .doOnError(throwable -> {
                    String errorMessage = throwable.getMessage() != null ? throwable.getMessage() : "invalid condition";
                    throw new IllegalArgumentFault(errorMessage);
                });
    }

    /**
     * Check that the scenario action can be parsed successfully.
     *
     * @param action the action to check
     * @return a mono of boolean with value <code>true</code> when the action is valid
     * @throws IllegalArgumentFault when the action cannot be parsed
     */
    private Mono<Boolean> validateActions(final String action) {
        return actionDeserializer.reactiveDeserialize(action)
                .flatMap(deserialized -> Mono.just(true))
                .doOnError(throwable -> {
                    String errorMessage = throwable.getMessage() != null ? throwable.getMessage() : "invalid action";
                    throw new IllegalArgumentFault(errorMessage);
                });
    }

    /**
     * Check that the scenario actions and conditions are valid values.
     *
     * @param scenario the scenario to validate conditions and actions for
     * @return a mono of boolean with value <code>true</code> when scenario is valid
     * @throws IllegalArgumentFault when either actions or conditions have an invalid value
     */
    @Trace(async = true)
    private Mono<Boolean> validateScenario(final Scenario scenario) {
        return Mono.zip(validateCondition(scenario.getCondition()), validateActions(scenario.getActions()))
                .flatMap(tuple -> Mono.just(true))
                .doOnError(throwable -> {
                    throw new IllegalArgumentFault(throwable.getMessage());
                }).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Update a scenario.
     *
     * @param scenarioId - scenario id
     * @param condition - condition json string
     * @param action - action json string
     * @param name - name json string
     * @param description - description json string
     * @param correctness - correctness json string
     * @throws IllegalArgumentException when the supplied scenario id is <code>null</code>
     */
    @Trace(async = true)
    public Mono<Void> updateScenario(final UUID scenarioId, final String condition, final String action,
                                         final String name, final String description, final ScenarioCorrectness correctness) {

        if (log.isDebugEnabled()) {
            log.debug("Update Scenario with id {} condition {} action {} name {} description {} correctness {}",
                    scenarioId, condition, action, name, description, correctness);
        }

        checkArgument(scenarioId != null, "missing scenario id");

        final Scenario scenario = new Scenario()
                .setId(scenarioId)
                .setCondition(condition)
                .setActions(action)
                .setName(name)
                .setDescription(description)
                .setCorrectness(correctness);

        return findById(scenarioId)
                .then(validateScenario(scenario))
                .then(scenarioGateway.updateScenario(scenario))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnSuccess(s -> log.info("Updated scenario {}", s));
    }

    /**
     * Finds scenario by id
     * @param scenarioId the scenario id
     * @return mono with scenario
     * @throws ScenarioNotFoundException if scenario is not found
     */
    @Trace(async = true)
    public Mono<Scenario> findById(final UUID scenarioId) {
        checkArgument(scenarioId != null, "missing scenarioId");
        return scenarioGateway.findById(scenarioId).single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ScenarioNotFoundException(scenarioId);
                });
    }

    /**
     * Delete a scenario and remove its tracking from the parent
     *
     * @param scenarioId - scenario id
     * @param parentId - the parent id to remove the scenario from
     * @param lifecycle - the lifecycle defining the parent relationship with the scenario
     */
    public Mono<Scenario> deleteScenario(final UUID scenarioId, final UUID parentId, final ScenarioLifecycle lifecycle,
                                         final CoursewareElementType parentType) {

        // TODO: hook up to ActivitySubscription when remove scenario message handler is created

        if (log.isDebugEnabled()) {
            log.debug("updateScenarioAction with scenario_id {}",
                    scenarioId);
        }

        checkArgument(scenarioId != null, "missing scenario id");

        final Scenario scenario = new Scenario()
                .setId(scenarioId);

        Mono<Scenario> source = scenarioGateway.delete(scenario, parentId, lifecycle, parentType).thenReturn(scenario);

        if (log.isDebugEnabled()) {
            log.info("Deleted scenario {}", scenario);
        }

        return source;
    }

    /**
     * Returns an ordered list of scenarios for parent and lifecycle.
     * @param parentId parent id (ex. activityId or interactiveId) - for which scenario was created
     * @param lifecycle lifecycle stage
     * @return an ordered list of scenarios
     */
    @Trace(async = true)
    public Flux<Scenario> findAll(final UUID parentId, final ScenarioLifecycle lifecycle) {
        checkArgument(parentId != null, "missing parent id");
        checkArgument(lifecycle != null, "missing lifecycle");

        return scenarioGateway.findByParent(parentId, lifecycle)
                .concatMap(this::findById)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Reorder a list of scenario ids to the supplied parent entity id
     * @param parentId the parent id that owns the scenarios
     * @param lifecycle the lifecycle stage those scenarios should be fired
     * @param scenarioIds a list containing the scenario ids to track
     * @throws IllegalArgumentException when any of the method argument are <code>null</code>
     * @return an object representing the relationship
     */
    @Trace(async = true)
    public Mono<ScenarioByParent> reorder(final UUID parentId, final ScenarioLifecycle lifecycle,
                                          final List<UUID> scenarioIds, final CoursewareElementType parentType) {

        checkArgument(parentId != null, "parentId is required");
        checkArgument(lifecycle != null, "lifecycle is required");
        checkArgument(scenarioIds != null, "scenarioIds are required");
        checkArgument(parentType != null, "parentType is required");

        ScenarioByParent scenarioByParent = new ScenarioByParent()
                .setParentId(parentId)
                .setLifecycle(lifecycle)
                .setScenarioIds(scenarioIds)
                .setParentType(parentType);

        return scenarioGateway.persist(scenarioByParent)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .then(Mono.just(scenarioByParent));
    }

    /**
     * Find a list of scenarios by parent id. The method propagates the exception when
     * {@link ScenarioService#findById(UUID)} fails to find a scenario
     * @param parentId the parent id to search the scenarios for
     * @return a mono list of scenarios
     */
    public Flux<Scenario> findScenarios(final UUID parentId) {
        return findScenarioIdsFor(parentId)
                .flatMap(this::findById);
    }

    /**
     * Find all the scenario ids belonging to the supplied parent id
     *
     * @param parentId the parent to search the scenarios for
     * @return a flux of scenario ids
     */
    @Trace(async = true)
    public Flux<UUID> findScenarioIdsFor(final UUID parentId) {
        return scenarioGateway.findByParent(parentId)
                .concatMapIterable(ScenarioByParent::getScenarioIds)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the scenarios belonging to the supplied parent id
     *
     * @param parentId the parent to search the scenarios for
     * @return a flux of scenario by parent
     */
    public Flux<ScenarioByParent> findScenariosFor(final UUID parentId) {
        return scenarioGateway.findByParent(parentId);
    }

    /**
     * Find all the scenario ids belonging to the supplied parent id and lifecycle in an ordered list
     *
     * @param parentId the parent to search the scenarios for
     * @return a flux of scenario ids
     */
    public Flux<UUID> findScenarioIdsFor(final UUID parentId, final ScenarioLifecycle lifecycle) {
        return scenarioGateway.findByParent(parentId, lifecycle);
    }

    /**
     * Find a scenario by parent
     *
     * @param parentId the parent id
     * @param scenarioLifecycle the scenario lifecycle to search the scenario ids for
     * @return a mono of scenario by parent
     */
    public Mono<ScenarioByParent> findScenarioByParent(final UUID parentId, final ScenarioLifecycle scenarioLifecycle) {
        return scenarioGateway.findScenarioByParent(parentId, scenarioLifecycle);
    }

    /**
     * Duplicate an existing scenario. If the scenario is not found the duplication is interrupted and the exception
     * is propagated.
     * Scenario actions and conditions will be updated with new ids.
     *
     * @param scenarioId  the scenario to duplicate
     * @param newParentId the new parent for duplicated scenario
     * @param parentType  the type of parent element
     * @param context     keeps mapping new id and old ids
     * @return a mono of the duplicated scenario
     */
    @Trace(async = true)
    public Mono<Scenario> duplicate(final UUID scenarioId, final UUID newParentId, final CoursewareElementType parentType,
                                    final DuplicationContext context) {
        return findById(scenarioId)
                .doOnError(ScenarioNotFoundException.class, ex -> {
                    if (log.isDebugEnabled()) {
                        log.debug("could not duplicate scenario {}. Scenario not found", scenarioId);
                    }
                    // propagate the exception
                    throw Exceptions.propagate(ex);
                }).map(scenario -> new Scenario()
                        .setId(UUIDs.timeBased())
                        .setCondition(context.replaceIds(scenario.getCondition()))
                        .setActions(context.replaceIds(scenario.getActions()))
                        .setLifecycle(scenario.getLifecycle())
                        .setName(scenario.getName())
                        .setDescription(scenario.getDescription())
                        .setCorrectness(scenario.getCorrectness())
                ).flatMap(scenario -> persist(scenario, newParentId, parentType))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a scenario to the database and save the parentId association.
     *
     * @param scenario the scenario to save
     * @param parentId the parent to associate
     * @return a mono of the saved scenario
     */
    @Trace(async = true)
    private Mono<Scenario> persist(final Scenario scenario, final UUID parentId, final CoursewareElementType parentType) {
        return scenarioGateway.persist(scenario, parentId, parentType)
                .doOnSuccess(s -> log.info("Created scenario {}", s))
                .thenReturn(scenario)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Helper method to distinguish which type of courseware element is relevant to supplied lifecycle by looking at
     * the prefix in its name
     *
     * @param lifecycle the lifecycle enum
     * @return enum with the proper parent type
     */
//    TODO: ScenarioLifecycle#getElementType can be used instead. or fetch parent type from ScenarioByParent ???
    public String getScenarioParentTypeByLifecycle(final ScenarioLifecycle lifecycle) {
        String[] tokens = lifecycle.name().split("_");
        if (tokens.length < 2) {
            log.warn("Lifecycle {} does not follow scenario lifecycle naming convention", lifecycle.name());
            throw new IllegalArgumentException(
                    String.format("Lifecycle %s does not follow scenario lifecycle naming convention", lifecycle));
        }
        return tokens[0];
    }

    /**
     * Find the parent element for a scenario
     *
     * @param scenarioId the scenario to find the parent for
     * @return a mono of parent by scenario object
     * @throws ScenarioParentNotFoundException if the parent element is not found for the given scenario
     */
    @Trace(async = true)
    public Mono<ParentByScenario> findParent(final UUID scenarioId) {
        return scenarioGateway.findParent(scenarioId)
                .single()
                .doOnError(NoSuchElementException.class, (ex) -> {
                    throw new ScenarioParentNotFoundException(scenarioId);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

}
