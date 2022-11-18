package com.smartsparrow.learner.data;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerScenarioGateway {

    private static final Logger log = LoggerFactory.getLogger(LearnerScenarioGateway.class);

    private final Session session;

    private final LearnerScenarioMutator learnerScenarioMutator;
    private final LearnerScenarioMaterializer learnerScenarioMaterializer;
    private final LearnerScenarioByParentMutator learnerScenarioByParentMutator;
    private final LearnerScenarioByParentMaterializer learnerScenarioByParentMaterializer;
    private final ParentByLearnerScenarioMutator parentByLearnerScenarioMutator;
    private final ParentByLearnerScenarioMaterializer parentByLearnerScenarioMaterializer;
    private final LearnerElementMutator learnerElementMutator;
    private final DeploymentMetadataMutator deploymentMetadataMutator;

    @Inject
    public LearnerScenarioGateway(Session session,
                                  LearnerScenarioMutator learnerScenarioMutator,
                                  LearnerScenarioMaterializer learnerScenarioMaterializer,
                                  LearnerScenarioByParentMutator learnerScenarioByParentMutator,
                                  LearnerScenarioByParentMaterializer learnerScenarioByParentMaterializer,
                                  ParentByLearnerScenarioMutator parentByLearnerScenarioMutator,
                                  ParentByLearnerScenarioMaterializer parentByLearnerScenarioMaterializer,
                                  LearnerElementMutator learnerElementMutator,
                                  final DeploymentMetadataMutator deploymentMetadataMutator) {
        this.session = session;
        this.learnerScenarioMutator = learnerScenarioMutator;
        this.learnerScenarioMaterializer = learnerScenarioMaterializer;
        this.learnerScenarioByParentMutator = learnerScenarioByParentMutator;
        this.learnerScenarioByParentMaterializer = learnerScenarioByParentMaterializer;
        this.parentByLearnerScenarioMutator = parentByLearnerScenarioMutator;
        this.parentByLearnerScenarioMaterializer = parentByLearnerScenarioMaterializer;
        this.learnerElementMutator = learnerElementMutator;
        this.deploymentMetadataMutator = deploymentMetadataMutator;
    }

    /**
     * Persist a learner scenario
     *
     * @param learnerScenario the object to persist
     * @return a flux of void
     */
    public Flux<Void> persist(LearnerScenario learnerScenario) {
        LearnerCoursewareElement element = new LearnerCoursewareElement()
                .setId(learnerScenario.getId())
                .setDeploymentId(learnerScenario.getDeploymentId())
                .setChangeId(learnerScenario.getChangeId())
                .setElementType(CoursewareElementType.SCENARIO);

        return Mutators.execute(session, Flux.just(
                learnerScenarioMutator.upsert(learnerScenario),
                learnerElementMutator.upsert(element)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find a learner scenario based on the latest change id
     *
     * @param scenarioId   the scenario to find
     * @param deploymentId the deployment associated with the scenario
     * @return a mono of learner scenario
     */
    public Mono<LearnerScenario> findLatestDeployed(UUID scenarioId, UUID deploymentId) {
        return ResultSets.query(session, learnerScenarioMaterializer.findLatestDeployed(scenarioId, deploymentId))
                .flatMapIterable(row -> row)
                .map(learnerScenarioMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist the relationships between a learner scenario and its parent element
     *
     * @param scenarioId   the learner scenario id to track the relationship for
     * @param parentId     the parent id of the scenario
     * @param deploymentId the deployment id the scenario has been published to
     * @param changeId     the change id
     * @param lifecycle    the scenario lifecycle
     * @param type         the parent element type
     * @return a flux of void
     */
    public Flux<Void> persistParent(UUID scenarioId, UUID parentId, UUID deploymentId, UUID changeId,
                                    ScenarioLifecycle lifecycle, CoursewareElementType type) {
        return Mutators.execute(session, Flux.just(
                parentByLearnerScenarioMutator.upsert(new ParentByLearnerScenario()
                        .setScenarioId(scenarioId)
                        .setParentId(parentId)
                        .setParentType(type)
                        .setChangeId(changeId)
                        .setDeploymentId(deploymentId))
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Persist the relationship between a parent and all its child scenario ids
     *
     * @param scenarioIds  the ordered list of scenario ids children
     * @param deploymentId the deployment id
     * @param changeId     the change id
     * @param parentId     the parent of the scenario ids
     * @param lifecycle    the scenario lifecycle
     * @param type         the parent type
     * @return a flux of void
     */
    public Flux<Void> persistScenarioIdsList(final List<UUID> scenarioIds, final UUID deploymentId, final UUID changeId,
                                             final UUID parentId, final ScenarioLifecycle lifecycle, final CoursewareElementType type) {
        return Mutators.execute(session, Flux.just(
                learnerScenarioByParentMutator.insertScenarioByParent(scenarioIds, deploymentId, changeId, parentId, lifecycle, type)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find a specific learner scenario by scenario id, deployment id and change id.
     *
     * @param id           the scenario id
     * @param deploymentId the deployment id this scenario instance belongs to
     * @param changeId     the specific change id of the deployment
     * @return Mono emitting single scenario instance for specified ids
     */
    public Mono<LearnerScenario> findById(UUID id, UUID deploymentId, UUID changeId) {
        return ResultSets.query(session, learnerScenarioMaterializer.findById(id, deploymentId, changeId))
                .flatMapIterable(row -> row)
                .map(learnerScenarioMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find all the learner scenarios by parent based on the latest change id. The method returns all the different
     * scenario lifecycle.
     *
     * @param parentId     the parent to search the learner scenarios for
     * @param deploymentId the deployment id associated with the scenarios
     * @return a flux of learner scenarios by parent
     */
    public Flux<LearnerScenarioByParent> findByParent(UUID parentId, UUID deploymentId) {
        return ResultSets.query(session, learnerScenarioByParentMaterializer.findLatestDeployed(parentId, deploymentId))
                .flatMapIterable(row -> row)
                .map(learnerScenarioByParentMaterializer::fromRow);
    }

    /**
     * Find a specific learner scenario by parent.
     *
     * @param parentId     the parent id to search the scenario for
     * @param deploymentId the deployment associated with the scenario
     * @param changeId     the specific change id of the scenario
     * @param lifecycle    the lifecycle
     * @return a mono of learner scenario
     */
    public Mono<LearnerScenarioByParent> findByLifecycle(UUID parentId, UUID deploymentId, UUID changeId, ScenarioLifecycle lifecycle) {

        return ResultSets.query(session,
                        learnerScenarioByParentMaterializer.findByLifecycle(parentId, deploymentId, changeId, lifecycle))
                .flatMapIterable(row -> row)
                .map(learnerScenarioByParentMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find all the learner scenarios by deployment and lifecycle
     *
     * @param parentId          the parent id to search the learner scenarios for
     * @param deploymentId      the deployment id
     * @param changeId          the change id
     * @param scenarioLifecycle the scenario lifecycle
     * @return a flux of learner scenarios
     */
    public Flux<LearnerScenario> findAll(UUID parentId, UUID deploymentId, UUID changeId, ScenarioLifecycle scenarioLifecycle) {
        return findByLifecycle(parentId, deploymentId, changeId, scenarioLifecycle)
                .flux()
                .flatMap(learnerScenarioByParent -> Flux.just(learnerScenarioByParent.getScenarioIds().toArray(new UUID[0])))
                .concatMap(scenarioId -> findLatestDeployed(scenarioId, deploymentId));
    }

    @Trace(async = true)
    public Flux<LearnerScenario> fetchAllById(UUID deploymentId, UUID changeId) {
        return ResultSets.query(session, learnerScenarioMaterializer.findAllById(deploymentId, changeId))
                .flatMapIterable(row -> row)
                .map(learnerScenarioMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a learner scenario
     *
     * @param learnerScenario the object to persist
     * @return a flux of void
     */
    public Flux<Void> persistDeploymentMetadata(LearnerScenario learnerScenario, UUID elementId) {
        LearnerCoursewareElement coursewareElement = new LearnerCoursewareElement()
                .setId(elementId)
                .setDeploymentId(learnerScenario.getDeploymentId())
                .setChangeId(learnerScenario.getChangeId());

        return Mutators.execute(session, Flux.just(
                deploymentMetadataMutator.upsert(coursewareElement)
        )).doOnError(throwable -> {
            log.warn(throwable.getMessage());
            throw Exceptions.propagate(throwable);
        });
    }
}