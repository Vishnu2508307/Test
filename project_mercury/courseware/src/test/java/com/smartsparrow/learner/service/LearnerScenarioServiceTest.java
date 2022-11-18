package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.DeploymentLogServiceStub.mockLogMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.data.ScenarioByParent;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.courseware.lang.ScenarioNotFoundException;
import com.smartsparrow.courseware.service.ScenarioService;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.deserializer.ActionDeserializer;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerScenario;
import com.smartsparrow.learner.data.LearnerScenarioGateway;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.PublishScenarioException;
import com.smartsparrow.learner.schema.ScenarioSchemaValidator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class LearnerScenarioServiceTest {

    @InjectMocks
    private LearnerScenarioService learnerScenarioService;

    @Mock
    private LearnerScenarioGateway learnerScenarioGateway;

    @Mock
    private ScenarioService scenarioService;

    @Mock
    private LearnerAssetService learnerAssetService;

    @Mock
    private ScenarioSchemaValidator scenarioSchemaValidator;

    @Mock
    private DeploymentLogService deploymentLogService;

    @Mock
    private ActionDeserializer mockActionDeserializer;

    private ActionDeserializer actionDeserializer = new ActionDeserializer();

    private static final UUID parentId = UUID.randomUUID();
    private static final UUID scenarioId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final CoursewareElementType parentType = CoursewareElementType.ACTIVITY;
    private DeployedActivity deployment;
    private Scenario scenario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        deployment = mock(DeployedActivity.class);

        scenario = new Scenario()
                .setActions("[{\"action\":\"GRADE_PASSBACK\",\"resolver\":{\"type\":\"LITERAL\"},\"context\":{\"value\":\"1\", \"elementId\":\"6fa9a570-c1d0-11ec-8776-69d2ee6dcb42\"}}]")
                .setCondition("condition")
                .setCorrectness(ScenarioCorrectness.correct)
                .setDescription("Immaturity is the incapacity to use one's intelligence without the guidance of another")
                .setName("a name")
                .setId(scenarioId)
                .setLifecycle(ScenarioLifecycle.ACTIVITY_COMPLETE);

        when(deployment.getId()).thenReturn(deploymentId);
        when(deployment.getChangeId()).thenReturn(changeId);

        when(scenarioService.findScenariosFor(parentId)).thenReturn(Flux.just(
                new ScenarioByParent()
                .setScenarioIds(Lists.newArrayList(scenarioId))
                .setLifecycle(ScenarioLifecycle.ACTIVITY_COMPLETE)
                .setParentId(parentId)
                .setParentType(parentType)
        ));
        when(scenarioService.findById(scenarioId)).thenReturn(Mono.just(scenario));

        when(learnerAssetService.publishAssetsFor(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(learnerScenarioGateway.persistScenarioIdsList(Lists.newArrayList(scenarioId), deploymentId, changeId, parentId, ScenarioLifecycle.ACTIVITY_COMPLETE, parentType))
                .thenReturn(Flux.just(new Void[]{}));

        mockLogMethods(deploymentLogService);
        List<Action> actions = actionDeserializer.deserialize(scenario.getActions());
        when(mockActionDeserializer.deserialize(scenario.getActions())).thenReturn(actions);
        when(learnerScenarioGateway.persistDeploymentMetadata(any(LearnerScenario.class), any(UUID.class))).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void publish_nullParentId() {
        PublishScenarioException e = assertThrows(PublishScenarioException.class,
                () -> learnerScenarioService.publish(null, deployment));

        assertTrue(e.getMessage().contains("parentId is required"));
    }

    @Test
    void publish_nullDeployment() {
        PublishScenarioException e = assertThrows(PublishScenarioException.class,
                () -> learnerScenarioService.publish(parentId, null));

        assertTrue(e.getMessage().contains("deployment is required"));
    }

    @Test
    void publish_noScenarios() {
        when(scenarioService.findScenariosFor(parentId)).thenReturn(Flux.empty());

        List<LearnerScenario> learnerScenarios = learnerScenarioService.publish(parentId, deployment)
                .collectList()
                .block();

        assertNotNull(learnerScenarios);
        assertEquals(0, learnerScenarios.size());
        verify(deploymentLogService, never())
                .logCompletedStep(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class), anyString());
        verify(deploymentLogService, never())
                .logFailedStep(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class), anyString());
        verify(deploymentLogService, never())
                .logProgressStep(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class), anyString());
    }

    @Test
    void publish_scenarioNotFound() {
        TestPublisher<Scenario> publisher = TestPublisher.create();
        publisher.error(new ScenarioNotFoundException(scenarioId));

        when(scenarioService.findById(scenarioId)).thenReturn(publisher.mono());

        assertThrows(PublishScenarioException.class, () -> learnerScenarioService.publish(parentId, deployment)
                .blockLast());
    }

    @Test
    void publish_scenarioValidationFails() {

        when(scenarioService.findById(scenarioId)).thenReturn(Mono.just(scenario));
        when(learnerScenarioGateway.persist(any(LearnerScenario.class))).thenReturn(Flux.just(new Void[]{}));
        doThrow(new ValidationException(SchemaLoader.load(new JSONObject()), List.class,
                "test"))
                .when(scenarioSchemaValidator).validate(any(String.class));

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> learnerScenarioService.publish(parentId, deployment)
                        .blockLast());

        assertNotNull(validationException.getAllMessages());
        assertEquals("#: expected type: List, found: String", validationException.getMessage());
        verify(deploymentLogService)
                .logFailedStep(any(Deployment.class), eq(scenarioId), eq(CoursewareElementType.SCENARIO), anyString());
    }

    @Test
    void publish() {
        when(learnerScenarioGateway.persist(any(LearnerScenario.class))).thenReturn(Flux.just(new Void[]{}));
        when(learnerScenarioGateway.persistParent(scenarioId, parentId, deploymentId, changeId,
                ScenarioLifecycle.ACTIVITY_COMPLETE, parentType)).thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<LearnerScenario> scenarioCaptor = ArgumentCaptor.forClass(LearnerScenario.class);

        List<LearnerScenario> scenarios = learnerScenarioService.publish(parentId, deployment)
                .collectList()
                .block();

        assertNotNull(scenarios);
        assertEquals(1, scenarios.size());

        verify(learnerScenarioGateway).persist(scenarioCaptor.capture());
        verify(learnerScenarioGateway).persistParent(scenarioId, parentId, deploymentId, changeId,
                ScenarioLifecycle.ACTIVITY_COMPLETE, parentType);

        LearnerScenario learnerScenario = scenarioCaptor.getValue();

        assertEquals(scenario.getId(), learnerScenario.getId());
        assertEquals(scenario.getLifecycle(), learnerScenario.getLifecycle());
        assertEquals(scenario.getActions(), learnerScenario.getActions());
        assertEquals(scenario.getCondition(), learnerScenario.getCondition());
        assertEquals(scenario.getCorrectness(), learnerScenario.getCorrectness());
        assertEquals(scenario.getDescription(), learnerScenario.getDescription());
        assertEquals(scenario.getName(), learnerScenario.getName());
        assertEquals(deploymentId, learnerScenario.getDeploymentId());
        assertEquals(changeId, learnerScenario.getChangeId());

        verify(deploymentLogService, never())
                .logFailedStep(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class), anyString());
        verify(deploymentLogService, times(2))
                .logProgressStep(any(Deployment.class), eq(scenarioId), eq(CoursewareElementType.SCENARIO), anyString());
        verify(deploymentLogService, never())
                .logCompletedStep(any(Deployment.class), eq(scenarioId), eq(CoursewareElementType.SCENARIO), anyString());
    }

}
