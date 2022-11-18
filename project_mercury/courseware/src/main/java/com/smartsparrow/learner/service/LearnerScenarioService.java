package com.smartsparrow.learner.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioByParent;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.lang.ScenarioNotFoundException;
import com.smartsparrow.courseware.service.ScenarioService;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.deserializer.ActionDeserializer;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerScenario;
import com.smartsparrow.learner.data.LearnerScenarioGateway;
import com.smartsparrow.learner.lang.PublishScenarioException;
import com.smartsparrow.learner.schema.ScenarioSchemaValidator;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerScenarioService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerScenarioService.class);

    private final LearnerScenarioGateway learnerScenarioGateway;
    private final ScenarioService scenarioService;
    private final ScenarioSchemaValidator scenarioSchemaValidator;
    private final DeploymentLogService deploymentLogService;
    private final ActionDeserializer actionDeserializer;

    @Inject
    public LearnerScenarioService(LearnerScenarioGateway learnerScenarioGateway,
                                  ScenarioService scenarioService,
                                  ScenarioSchemaValidator scenarioSchemaValidator,
                                  DeploymentLogService deploymentLogService,
                                  final ActionDeserializer actionDeserializer) {
        this.learnerScenarioGateway = learnerScenarioGateway;
        this.scenarioService = scenarioService;
        this.scenarioSchemaValidator = scenarioSchemaValidator;
        this.deploymentLogService = deploymentLogService;
        this.actionDeserializer = actionDeserializer;
    }

    /**
     * Publish all scenarios for a given parent element id
     *
     * @param parentId the parent id to find and deploy the scenarios for
     * @param deployment the deployment the scenarios should be published to
     * @return a flux of learner scenarios
     * @throws PublishScenarioException when either:
     * <br> parentId is <code>null</code>
     * <br> parentType is <code>null</code>
     * <br> deployment is <code>null</code>
     * <br> scenario is not found
     */
    public Flux<LearnerScenario> publish(final UUID parentId, final DeployedActivity deployment) {

        try {
            checkArgument(parentId != null, "parentId is required");
            checkArgument(deployment != null, "deployment is required");
        } catch (IllegalArgumentException e) {
            throw new PublishScenarioException(parentId, e.getMessage());
        }

        return scenarioService.findScenariosFor(parentId)
                .flatMap(scenarioByParent -> publishScenario(deployment, scenarioByParent));
    }

    /**
     * Publish a scenario.
     *
     * @param deployment the deployment to publish the scenario to
     * @param scenarioByParent represents the relationship to persist with the parent element
     * @return a flux of published scenarios
     */
    private Flux<LearnerScenario> publishScenario(final DeployedActivity deployment, final ScenarioByParent scenarioByParent) {
        return learnerScenarioGateway.persistScenarioIdsList(scenarioByParent.getScenarioIds(),
                deployment.getId(),
                deployment.getChangeId(),
                scenarioByParent.getParentId(),
                scenarioByParent.getLifecycle(),
                scenarioByParent.getParentType())
                .thenMany(publish(scenarioByParent.getParentId(),
                        scenarioByParent.getParentType(),
                        deployment,
                        scenarioByParent.getScenarioIds()));
    }

    /**
     * Find each scenario, build the corresponding learner scenario object and persist it to the database.
     *
     * @param parentId the parent id to save the scenario relationship for
     * @param parentType the parent type
     * @param deployment the deployment each scenario should be published to
     * @param scenarios a list of scenario ids
     * @return a flux of learner scenario
     */
    private Flux<LearnerScenario> publish(final UUID parentId, final CoursewareElementType parentType, final DeployedActivity deployment,
                                          List<UUID> scenarios) {
        if (scenarios.isEmpty()) {
            return Flux.empty();
        }

        return scenarios.stream()
                .map(scenarioId -> build(scenarioId, deployment, parentId)
                        .flux()
                        .doOnError(throwable -> {
                            deploymentLogService.logFailedStep(deployment, scenarioId, CoursewareElementType.SCENARIO,
                                    "[learnerScenarioService] " + Arrays.toString(throwable.getStackTrace()))
                                    .subscribe();
                            throw Exceptions.propagate(throwable);
                        }))
                .reduce(Flux::concat)
                .orElse(Flux.empty())
                .concatMap(scenario -> persist(scenario, parentId, parentType, deployment).flux());
    }

    /**
     * Persist a learner scenario object to the database together with its relationship with its parent element.
     *
     * @param scenario the learner scenario to persist
     * @param parentId the parent id to save as parent for the learner scenario
     * @param parentType the parent type
     * @return a mono of learner scenario
     */
    private Mono<LearnerScenario> persist(final LearnerScenario scenario, final UUID parentId, final CoursewareElementType parentType,
                                          DeployedActivity deployment) {

        return learnerScenarioGateway.persist(scenario)
                .singleOrEmpty()
                .thenMany(learnerScenarioGateway.persistParent(
                        scenario.getId(),
                        parentId,
                        scenario.getDeploymentId(),
                        scenario.getChangeId(),
                        scenario.getLifecycle(),
                        parentType
                ))
                .then(deploymentLogService.logProgressStep(deployment, scenario.getId(), CoursewareElementType.SCENARIO,
                        "[learnerScenarioService] finished publishing scenario " + scenario.getId()))
                .then(persistDeploymentMetadata(scenario, parentId));
               // .then(Mono.just(scenario));
    }

    /**
     * Build a learner scenario object from an existing scenario.
     *
     * @param scenarioId the scenario id to build the learner scenario from
     * @param deployment the deployment associated with the learner scenario
     * @param parentId the parent element
     * @return a mono of learner scenario
     * @throws PublishScenarioException when the scenario is not found
     */
    private Mono<LearnerScenario> build(final UUID scenarioId, final DeployedActivity deployment, final UUID parentId) {
        return scenarioService.findById(scenarioId)
                .flatMap(scenario -> deploymentLogService.logProgressStep(deployment, scenarioId, CoursewareElementType.SCENARIO,
                        "[learnerScenarioService] about to validate scenario to publish")
                        .thenReturn(scenario))
                .doOnError(ScenarioNotFoundException.class, ex -> {
                    throw new PublishScenarioException(parentId, ex.getMessage());
                })
                .map(scenario -> {
                    //TODO: Find out what needs to happen when validation fails
                    scenarioSchemaValidator.validate(scenario.getCondition());
                    return new LearnerScenario()
                            .setId(scenario.getId())
                            .setDescription(scenario.getDescription())
                            .setName(scenario.getName())
                            .setLifecycle(scenario.getLifecycle())
                            .setCorrectness(scenario.getCorrectness())
                            .setCondition(scenario.getCondition())
                            .setActions(scenario.getActions())
                            .setChangeId(deployment.getChangeId())
                            .setDeploymentId(deployment.getId());
                });
    }

    /**
     * Find a specific learner scenario by scenario id, deployment id and change id.
     *
     * @param scenarioId the scenario id
     * @param deploymentId the deployment id this scenario instance belongs to
     * @param changeId the specific change id of the deployment
     * @return Mono emitting single scenario instance for specified ids
     */
    public Mono<LearnerScenario> findById(final UUID scenarioId, final UUID deploymentId, final UUID changeId) {
        return learnerScenarioGateway.findById(scenarioId, deploymentId, changeId);
    }

    /**
     * Find all the learner scenarios for a specific lifecycle
     *
     * @param deployment the deployment
     * @param parentId the parent id
     * @param scenarioLifecycle the scenario lifecycle
     * @return a flux of learner scenarios
     * @throws IllegalArgumentFault when any of the required argument is <code>null</code>
     */
    public Flux<LearnerScenario> findAll(final Deployment deployment, final UUID parentId, final ScenarioLifecycle scenarioLifecycle) {
        affirmArgument(deployment != null, "deployment is required");
        affirmArgument(parentId != null, "parentId is required");
        affirmArgument(scenarioLifecycle != null, "scenarioLifecycle is required");

        return findAll(deployment.getId(), deployment.getChangeId(), parentId, scenarioLifecycle);
    }

    /**
     * Find all the learner scenarios for a specific lifecycle
     *
     * @param deploymentId the deployment id
     * @param changeId the change id
     * @param parentId the parent id
     * @param scenarioLifecycle the scenario lifecycle
     * @return a flux of learner scenarios
     * @throws IllegalArgumentFault when any of the required argument is <code>null</code>
     */
    @Trace(async = true)
    public Flux<LearnerScenario> findAll(final UUID deploymentId, final UUID changeId, final UUID parentId, final ScenarioLifecycle scenarioLifecycle) {
        affirmArgument(parentId != null, "parentId is required");
        affirmArgument(scenarioLifecycle != null, "scenarioLifecycle is required");

        return learnerScenarioGateway.findAll(parentId, deploymentId, changeId, scenarioLifecycle);
    }

    private Mono<LearnerScenario> persistDeploymentMetadata(final LearnerScenario scenario, UUID elementId) {
        log.jsonInfo("Storing GRADE PASSBACK data by scenario", new HashMap<String, Object>() {
            {
                put("deploymentId", scenario.getDeploymentId());
                put("changeId", scenario.getChangeId());
                put("elementId", elementId);
            }
        });
        Boolean isGradePassbackPresent = actionDeserializer.deserialize(scenario.getActions()).stream()
                // get all actions and filter by GRADE_PASSBACK
                .anyMatch(action -> action.getType().equals(Action.Type.GRADE_PASSBACK));

        if (isGradePassbackPresent) {
            return learnerScenarioGateway.persistDeploymentMetadata(scenario, elementId)
                    .singleOrEmpty()
                    .thenReturn(scenario);
        }
        return Mono.just(scenario);

    }
}
