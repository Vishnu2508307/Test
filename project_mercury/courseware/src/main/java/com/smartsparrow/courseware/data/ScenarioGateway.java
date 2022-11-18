package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ScenarioGateway {

    private final static Logger log = LoggerFactory.getLogger(ScenarioGateway.class);

    private final Session session;
    private final ScenarioMutator scenarioMutator;
    private final ScenarioMaterializer scenarioMaterializer;
    private final ScenarioByParentMutator scenarioByParentMutator;
    private final ScenarioByParentMaterializer scenarioByParentMaterializer;
    private final ParentByScenarioMaterializer parentByScenarioMaterializer;
    private final ParentByScenarioMutator parentByScenarioMutator;
    private final ElementMutator elementMutator;

    @Inject
    public ScenarioGateway(Session session,
                           ScenarioMutator scenarioMutator,
                           ScenarioMaterializer scenarioMaterializer,
                           ScenarioByParentMutator scenarioByParentMutator,
                           ScenarioByParentMaterializer scenarioByParentMaterializer,
                           ParentByScenarioMaterializer parentByScenarioMaterializer,
                           ParentByScenarioMutator parentByScenarioMutator,
                           ElementMutator elementMutator) {

        this.session = session;
        this.scenarioMutator = scenarioMutator;
        this.scenarioMaterializer = scenarioMaterializer;
        this.scenarioByParentMutator = scenarioByParentMutator;
        this.scenarioByParentMaterializer = scenarioByParentMaterializer;
        this.parentByScenarioMaterializer = parentByScenarioMaterializer;
        this.parentByScenarioMutator = parentByScenarioMutator;
        this.elementMutator = elementMutator;
    }

    /**
     * Convert row to Scenario
     *
     * @param row
     * @return Scenario
     */
    private Scenario fromRow(Row row) {
        if (log.isDebugEnabled()) {
            log.debug("Mapping row to Scenario Object");
        }
        return new Scenario()
                .setId(row.getUUID("id"))
                .setCondition(row.getString("condition"))
                .setActions(row.getString("actions"))
                .setCorrectness(row.getString("correctness") != null ? Enums.of(ScenarioCorrectness.class, row.getString("correctness")) : null)
                .setDescription(row.getString("description"))
                .setName(row.getString("name"))
                .setLifecycle(Enums.of(ScenarioLifecycle.class, row.getString("lifecycle")));
    }

    /**
     * Find Scenario by scenarioId
     *
     * @param scenarioId
     * @return Mono<Scenario>
     */
    @Trace(async = true)
    public Mono<Scenario> findById(final UUID scenarioId) {
        return ResultSets.query(session, scenarioMaterializer.findById(scenarioId))
                .flatMapIterable(row -> row)
                .map(this::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(
                        e -> {
                            log.error(String.format("Error: findById, with scenario_id %s",
                                    scenarioId.toString()), e);
                            throw Exceptions.propagate(e);
                        });
    }

    /**
     * Persist scenario to both scenario and scenarioByParent
     *
     * @param scenario
     * @return
     */
    @Trace(async = true)
    public Mono<Void> persist(final Scenario scenario, final UUID parentId, final CoursewareElementType parentType) {
        CoursewareElement scenarioElement = new CoursewareElement()
                .setElementId(scenario.getId())
                .setElementType(CoursewareElementType.SCENARIO);

        return Mutators.execute(session, Flux.just(scenarioMutator.upsert(scenario),
                elementMutator.upsert(scenarioElement),
                scenarioByParentMutator.addScenario(parentId, scenario.getLifecycle(), scenario.getId(), parentType),
                parentByScenarioMutator.upsert(new ParentByScenario()
                        .setParentId(parentId)
                        .setScenarioId(scenario.getId())
                        .setParentType(parentType))))
                .doOnError(
                        e -> {
                            log.error(String.format("Error: persist, with scenario %s",
                                    scenario.toString()), e);
                            throw Exceptions.propagate(e);
                        })
                .doOnEach(ReactiveTransaction.linkOnNext())
                .singleOrEmpty();
    }

    /**
     * Update scenario
     *
     * @param scenario
     * @return
     */
    @Trace(async = true)
    public Mono<Void> updateScenario(final Scenario scenario) {
        return Mutators.execute(session, Flux.just(scenarioMutator.updateScenario(scenario))
                        .doOnEach(ReactiveTransaction.linkOnNext()))
                .doOnError(
                        e -> {
                            log.error(String.format("Error: updateScenario, with scenario %s",
                                    scenario.toString()), e);
                            throw Exceptions.propagate(e);
                        })
                .singleOrEmpty();
    }

    /**
     * Delete the scenario and the scenario by parent tracking
     *
     * @param scenario the scenario to delete
     * @param parentId the parent to remove the scenario tracking for
     * @param lifecycle the lifecycle the parent tracking should be removed for
     * @return
     */
    public Mono<Void> delete(Scenario scenario, UUID parentId, ScenarioLifecycle lifecycle, CoursewareElementType parentType) {
        return Mutators.execute(session, Flux.just(scenarioMutator.delete(scenario),
                scenarioByParentMutator.removeScenario(parentId, lifecycle, scenario.getId(), parentType)))
                .doOnError(
                        e -> {
                            log.error(String.format("Error: delete, with scenario %s",
                                    scenario.toString()), e);
                            throw Exceptions.propagate(e);
                        })
                .singleOrEmpty();
    }

    /**
     * Return list of scenario ids for parent and lifecycle
     * @param parentId parent id (ex. activity id, interactive id)
     * @param lifecycle lifecycle stage
     * @return a list of scenarios ids
     */
    @Trace(async = true)
    public Flux<UUID> findByParent(final UUID parentId, final ScenarioLifecycle lifecycle) {
        return ResultSets.query(session, scenarioByParentMaterializer.findByParentLifecycle(parentId, lifecycle))
                .flatMapIterable(row -> row)
                .map(row -> row.getList("scenario_ids", UUID.class))
                .flatMapIterable(id -> id)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a scenario by parent
     *
     * @param parentId the parent id
     * @param lifecycle the lifecycle to search the scenario for
     * @return a mono of scenario by parent
     */
    public Mono<ScenarioByParent> findScenarioByParent(final UUID parentId, final ScenarioLifecycle lifecycle) {
        return ResultSets.query(session, scenarioByParentMaterializer.findByParentLifecycle(parentId, lifecycle))
                .flatMapIterable(row -> row)
                .map(scenarioByParentMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find all scenarios by parent
     * @param parentId the parent Id to search the scenarios for
     * @return a flux of scenarios
     */
    @Trace(async = true)
    public Flux<ScenarioByParent> findByParent(final UUID parentId) {
        return ResultSets.query(session, scenarioByParentMaterializer.findByParent(parentId))
                .flatMapIterable(row->row)
                .map(scenarioByParentMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find parent element for a scenario.
     *
     * @param scenarioId the scenario to find the parent for
     * @return a mono of parent by scenario object
     */
    @Trace(async = true)
    public Mono<ParentByScenario> findParent(final UUID scenarioId) {
        return ResultSets.query(session, parentByScenarioMaterializer.fetchParent(scenarioId))
                .flatMapIterable(row->row)
                .map(parentByScenarioMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Attach an ordered list of scenarios to a parent entity. The relationship is grouped by the lifecycle value.
     * @param scenarioByParent an object representation of the above described relationship
     */
    @Trace(async = true)
    public Flux<Void> persist(final ScenarioByParent scenarioByParent) {
        return Mutators.execute(session, Flux.just(scenarioByParentMutator.upsert(scenarioByParent))
                        .doOnEach(ReactiveTransaction.linkOnNext()))
                .doOnError(throwable -> {
                    log.error(String.format("error attaching scenarios to parent entity: %s",
                            scenarioByParent.toString()));
                    throw Exceptions.propagate(throwable);
                });
    }

}
