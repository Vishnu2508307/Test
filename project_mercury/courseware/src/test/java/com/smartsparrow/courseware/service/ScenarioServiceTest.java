package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

public class ScenarioServiceTest {

    private ScenarioService scenarioService;

    @Mock
    ScenarioGateway scenarioGateway;

    private ConditionDeserializer conditionDeserializer;

    private ActionDeserializer actionDeserializer;

    private static final UUID scenarioId = UUID.randomUUID();
    private static final UUID parentId = UUID.randomUUID();
    private static final CoursewareElementType parentType = CoursewareElementType.ACTIVITY;
    private static final String condition = "{\"type\" : \"CHAINED_CONDITION\", \"operator\": \"OR\", \"conditions\": []}";
    private static final String action = "[{\"action\":\"FUBAR\",\"resolver\":{\"type\":\"LITERAL\"},\"context\":{}}]";
    private static final String name = "name";
    private static final String description = "description";
    private static final ScenarioCorrectness correctness = ScenarioCorrectness.correct;
    private ArgumentCaptor<Scenario> scenarioArgumentCaptor;

    private static final ScenarioLifecycle lifecycle = ScenarioLifecycle.INTERACTIVE_DURING;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        scenarioArgumentCaptor = ArgumentCaptor.forClass(Scenario.class);

        conditionDeserializer = new ConditionDeserializer();

        actionDeserializer = new ActionDeserializer();

        scenarioService = new ScenarioService(scenarioGateway, conditionDeserializer, actionDeserializer);
    }

    @Test
    void createScenario_valid_withAllProperties() {
        TestPublisher<Void> persistPublisher = TestPublisher.create();
        persistPublisher.complete();

        when(scenarioGateway.persist(any(Scenario.class), eq(parentId), eq(parentType))).thenReturn(persistPublisher.mono());

        Scenario result = scenarioService.create(condition, action, name, description, correctness, lifecycle, parentId, parentType).block();

        assertNotNull(result);

        verify(scenarioGateway, atLeastOnce()).persist(scenarioArgumentCaptor.capture(), eq(parentId), eq(parentType));

        Scenario scenario = scenarioArgumentCaptor.getValue();

        assertNotNull(scenario);

        assertAll(() -> {
            assertNotNull(result.getId());
            assertEquals(condition, scenario.getCondition());
            assertEquals(action, scenario.getActions());
            assertEquals(name, scenario.getName());
            assertEquals(description, scenario.getDescription());
            assertEquals(correctness, scenario.getCorrectness());
            assertEquals(lifecycle, scenario.getLifecycle());
        });

        persistPublisher.assertWasRequested();
    }

    @Test
    void createScenario_withNullConditionProperty() {
        TestPublisher<Void> persistPublisher = TestPublisher.create();
        persistPublisher.complete();

        when(scenarioGateway.persist(any(Scenario.class), eq(parentId), eq(parentType))).thenReturn(persistPublisher.mono());

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> scenarioService.create(null, null, name, null, null,
                lifecycle, parentId, parentType).block());

        assertNotNull(e);
        assertEquals("argument \"content\" is null", e.getMessage());

        persistPublisher.assertWasNotRequested();
    }

    @Test
    void createScenario_withInvalidActionsProperty() {
        TestPublisher<Void> persistPublisher = TestPublisher.create();
        persistPublisher.complete();

        when(scenarioGateway.persist(any(Scenario.class), eq(parentId), eq(parentType))).thenReturn(persistPublisher.mono());

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> scenarioService.create(condition, "[{\"invalid\":\"actions\"}]", name, null, null,
                lifecycle, parentId, parentType).block());

        assertNotNull(e);
        assertNotNull(e.getMessage());

        persistPublisher.assertWasNotRequested();
    }

    @Test
    void createScenario_invalid_lifecycleNotSupplied() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> scenarioService
                .create(null, null, null, null, null, null, parentId, parentType).block());

        assertEquals("scenarioLifecycle is required", throwable.getMessage());
    }

    @Test
    void createScenario_invalid_nameNotSupplied() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> scenarioService
                .create(null, null, null, null, null,
                        lifecycle, parentId, parentType).block());

        assertEquals("name is required", throwable.getMessage());
    }

    @Test
    void createScenario_invalid_parentIdNotSupplied() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> scenarioService
                .create(null, null, name, null, null,
                        lifecycle, null, parentType).block());

        assertEquals("parentId is required", throwable.getMessage());
    }

    @Test
    void updateScenario_nullScenarioId() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> scenarioService.updateScenario(null, null, null, null, null, null));
        assertEquals(e.getMessage(), "missing scenario id");

    }

    @Test
    void updateScenario_valid() {

        when(scenarioGateway.updateScenario(any(Scenario.class))).thenReturn(Mono.empty());
        when(scenarioGateway.findById(any(UUID.class))).thenReturn(Mono.just(new Scenario()));

        scenarioService.updateScenario(scenarioId, condition, action,
                "name", "description", ScenarioCorrectness.correct).block();

        verify(scenarioGateway, atLeastOnce()).updateScenario(scenarioArgumentCaptor.capture());

        Scenario scenario = scenarioArgumentCaptor.getValue();

        assertNotNull(scenario);

        assertEquals(scenarioId, scenario.getId());
        assertEquals(condition, scenario.getCondition());
        assertEquals(action, scenario.getActions());
    }

    @Test
    void updateScenario_shouldUpdateAllFields() {
        String name = "updated name";
        String description = "updated description";

        when(scenarioGateway.updateScenario(any(Scenario.class))).thenReturn(Mono.empty());
        when(scenarioGateway.findById(any(UUID.class))).thenReturn(Mono.just(new Scenario()));

        ArgumentCaptor<Scenario> scenarioArgumentCaptor = ArgumentCaptor.forClass(Scenario.class);

        scenarioService.updateScenario(scenarioId, condition, action, name, description, ScenarioCorrectness.correct).block();

        verify(scenarioGateway, atLeastOnce()).updateScenario(scenarioArgumentCaptor.capture());

        Scenario updated = scenarioArgumentCaptor.getValue();

        assertNotNull(updated);

        assertAll(() -> {
            assertEquals(scenarioId, updated.getId());
            assertEquals(action, updated.getActions());
            assertEquals(condition, updated.getCondition());
            assertEquals(name, updated.getName());
            assertEquals(description, updated.getDescription());
            assertEquals(ScenarioCorrectness.correct, updated.getCorrectness());
            assertNull(updated.getLifecycle());
        });
    }

    @Test
    void updateScenario_invalidCondition() {
        TestPublisher<Void> updatePublisher = TestPublisher.create();
        updatePublisher.complete();

        when(scenarioGateway.updateScenario(any(Scenario.class))).thenReturn(updatePublisher.mono());
        when(scenarioGateway.findById(any(UUID.class))).thenReturn(Mono.just(new Scenario()));

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> scenarioService.updateScenario(scenarioId, null, action,
                "name", "description", ScenarioCorrectness.correct).block());

        assertNotNull(e);

        updatePublisher.assertWasNotRequested();
    }

    @Test
    void updateScenario_invalidAction() {
        TestPublisher<Void> updatePublisher = TestPublisher.create();
        updatePublisher.complete();

        when(scenarioGateway.updateScenario(any(Scenario.class))).thenReturn(updatePublisher.mono());
        when(scenarioGateway.findById(any(UUID.class))).thenReturn(Mono.just(new Scenario()));

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> scenarioService.updateScenario(scenarioId, condition, "{\"invalid\": true}",
                "name", "description", ScenarioCorrectness.correct).block());

        assertNotNull(e);

        updatePublisher.assertWasNotRequested();
    }
    @Test
    void updateScenario_inullAction() {
        TestPublisher<Void> updatePublisher = TestPublisher.create();
        updatePublisher.complete();

        when(scenarioGateway.updateScenario(any(Scenario.class))).thenReturn(updatePublisher.mono());
        when(scenarioGateway.findById(any(UUID.class))).thenReturn(Mono.just(new Scenario()));

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> scenarioService.updateScenario(scenarioId, condition, null,
                "name", "description", ScenarioCorrectness.correct).block());

        assertNotNull(e);

        updatePublisher.assertWasNotRequested();
    }

    @Test
    void findAll_byParentLifecycle() {
        UUID parentId = UUID.randomUUID();
        UUID scenarioId1 = UUID.randomUUID();
        UUID scenarioId2 = UUID.randomUUID();
        Scenario scenario1 = new Scenario().setId(UUID.randomUUID()).setName("scenario 1").setLifecycle(lifecycle);
        Scenario scenario2 = new Scenario().setId(UUID.randomUUID()).setName("scenario 2").setLifecycle(lifecycle);
        when(scenarioGateway.findByParent(eq(parentId), eq(lifecycle))).thenReturn(Flux.just(scenarioId1, scenarioId2));
        //even if first of the query evaluates longer it still should be first in a list
        when(scenarioGateway.findById(eq(scenarioId1))).thenReturn(Mono.delay(Duration.ofSeconds(1)).map(l -> scenario1));
        when(scenarioGateway.findById(eq(scenarioId2))).thenReturn(Mono.just(scenario2));

        StepVerifier.create(scenarioService.findAll(parentId, lifecycle))
                .expectNext(scenario1, scenario2)
                .verifyComplete();
    }

    @Test
    void findAll_byParentLifecycle_noScenarios() {
        UUID parentId = UUID.randomUUID();
        when(scenarioGateway.findByParent(eq(parentId), eq(lifecycle))).thenReturn(Flux.empty());

        StepVerifier.create(scenarioService.findAll(parentId, lifecycle))
                .verifyComplete();
        verify(scenarioGateway, never()).findById(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void findAll_byParentLifecycle_exception() {
        UUID parentId = UUID.randomUUID();
        Flux error = TestPublisher.create().error(new RuntimeException("some exception")).flux();
        when(scenarioGateway.findByParent(eq(parentId), eq(lifecycle))).thenReturn(error);

        StepVerifier.create(scenarioService.findAll(parentId, lifecycle))
                .expectError(RuntimeException.class)
                .verify();
        verify(scenarioGateway, never()).findById(any());
    }

    @Test
    void attach_parentIdNotSupplied() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> scenarioService.reorder(null, lifecycle, new ArrayList<>(), parentType));

        assertEquals("parentId is required", e.getMessage());
    }

    @Test
    void attach_lifecycleNotSupplied() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> scenarioService.reorder(UUID.randomUUID(), null, new ArrayList<>(), parentType));

        assertEquals("lifecycle is required", e.getMessage());
    }

    @Test
    void attach_scenarioIdsNotSupplied() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> scenarioService.reorder(UUID.randomUUID(), lifecycle, null, parentType));

        assertEquals("scenarioIds are required", e.getMessage());
    }

    @Test
    void attach() {
        when(scenarioGateway.persist(any(ScenarioByParent.class))).thenReturn(Flux.just(new Void[]{}));

        ScenarioByParent scenarioByParent = scenarioService
                .reorder(UUID.randomUUID(), lifecycle, Lists.newArrayList(UUID.randomUUID()), parentType).block();

        verify(scenarioGateway, atLeastOnce()).persist(scenarioByParent);

        assertAll(() -> {
            assertNotNull(scenarioByParent);
            assertEquals(lifecycle, scenarioByParent.getLifecycle());
            assertNotNull(scenarioByParent.getParentId());
            assertNotNull(scenarioByParent.getScenarioIds());
            assertTrue(!scenarioByParent.getScenarioIds().isEmpty());
        });
    }

    @Test
    void findScenarios_byParentId() {
        final UUID parentId = UUID.randomUUID();
        final UUID scenarioIdOne = UUID.randomUUID();
        final UUID scenarioIdTwo = UUID.randomUUID();
        final UUID scenarioIdThree = UUID.randomUUID();

        ScenarioByParent scenarioByParentOne = new ScenarioByParent()
                .setParentId(parentId)
                .setLifecycle(ScenarioLifecycle.ACTIVITY_ENTRY)
                .setScenarioIds(Lists.newArrayList(scenarioIdOne, scenarioIdTwo));

        ScenarioByParent scenarioByParentTwo = new ScenarioByParent()
                .setParentId(parentId)
                .setLifecycle(ScenarioLifecycle.ACTIVITY_EVALUATE)
                .setScenarioIds(Lists.newArrayList(scenarioIdThree));

        Scenario scenarioOne = new Scenario()
                .setId(scenarioIdOne);
        Scenario scenarioTwo = new Scenario()
                .setId(scenarioIdTwo);
        Scenario scenarioThree = new Scenario()
                .setId(scenarioIdThree);

        when(scenarioGateway.findByParent(parentId)).thenReturn(Flux.just(scenarioByParentOne, scenarioByParentTwo));
        when(scenarioGateway.findById(scenarioIdOne)).thenReturn(Mono.just(scenarioOne));
        when(scenarioGateway.findById(scenarioIdTwo)).thenReturn(Mono.just(scenarioTwo));
        when(scenarioGateway.findById(scenarioIdThree)).thenReturn(Mono.just(scenarioThree));

        final List<Scenario> results = scenarioService.findScenarios(parentId).collectList().block();

        assertAll(() -> {
            assertNotNull(results);
            assertEquals(3, results.size());
            assertEquals(scenarioIdOne, results.get(0).getId());
            assertEquals(scenarioIdTwo, results.get(1).getId());
            assertEquals(scenarioIdThree, results.get(2).getId());
        });
    }

    @Test
    void finsScenarios_byParentId_scenarioNotFound() {
        final UUID parentId = UUID.randomUUID();
        final UUID scenarioId = UUID.randomUUID();

        ScenarioByParent scenarioByParent = new ScenarioByParent()
                .setParentId(parentId)
                .setLifecycle(ScenarioLifecycle.ACTIVITY_ENTRY)
                .setScenarioIds(Lists.newArrayList(scenarioId));

        when(scenarioGateway.findByParent(parentId)).thenReturn(Flux.just(scenarioByParent));
        when(scenarioGateway.findById(scenarioId)).thenReturn(Mono.empty());

        ScenarioNotFoundException e = assertThrows(ScenarioNotFoundException.class, () -> scenarioService
                .findScenarios(parentId).collectList().block());
        assertEquals(String.format("no scenario with id %s", scenarioId), e.getMessage());
    }

    @Test
    void findScenarioIdsFor_emptyStream() {
        when(scenarioGateway.findByParent(parentId)).thenReturn(Flux.empty());

        List<UUID> scenarioIds = scenarioService.findScenarioIdsFor(parentId).collectList().block();
        assertNotNull(scenarioIds);
        assertTrue(scenarioIds.isEmpty());
    }

    @Test
    void findScenarioIdsFor_found() {
        UUID scenarioIdOne = UUID.randomUUID();
        UUID scenarioIdTwo = UUID.randomUUID();

        ScenarioByParent scenarioByParent = new ScenarioByParent()
                .setScenarioIds(Lists.newArrayList(scenarioIdOne, scenarioIdTwo))
                .setParentId(parentId)
                .setLifecycle(ScenarioLifecycle.ACTIVITY_EVALUATE);

        when(scenarioGateway.findByParent(parentId)).thenReturn(Flux.just(scenarioByParent));

        List<UUID> scenarioIds = scenarioService.findScenarioIdsFor(parentId).collectList().block();
        assertNotNull(scenarioIds);
        assertFalse(scenarioIds.isEmpty());
        assertEquals(2, scenarioIds.size());
    }

    @Test
    void findScenarioIdsFor_emptyList() {
        ScenarioByParent scenarioByParent = new ScenarioByParent()
                .setScenarioIds(new ArrayList<>())
                .setParentId(parentId)
                .setLifecycle(ScenarioLifecycle.ACTIVITY_EVALUATE);

        when(scenarioGateway.findByParent(parentId)).thenReturn(Flux.just(scenarioByParent));

        List<UUID> scenarioIds = scenarioService.findScenarioIdsFor(parentId).collectList().block();
        assertNotNull(scenarioIds);
        assertTrue(scenarioIds.isEmpty());
    }

    @Test
    void duplicateScenario_scenarioNotFound() {
        UUID scenarioId = UUID.randomUUID();

        when(scenarioGateway.findById(scenarioId)).thenReturn(Mono.empty());

        ScenarioNotFoundException ex = assertThrows(ScenarioNotFoundException.class, () -> scenarioService
                .duplicate(scenarioId, parentId, parentType, new DuplicationContext()).block());

        assertEquals(String.format("no scenario with id %s", scenarioId), ex.getMessage());
    }

    @Test
    void duplicateScenario_scenarioFound() {
        UUID scenarioId = UUID.randomUUID();
        UUID oldId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();
        DuplicationContext context = new DuplicationContext();
        context.putIds(oldId, newId);

        Scenario scenario = new Scenario()
                .setId(scenarioId)
                .setCondition("condition id:" + oldId)
                .setCorrectness(ScenarioCorrectness.correct)
                .setActions("actions id:" + oldId)
                .setLifecycle(ScenarioLifecycle.ACTIVITY_COMPLETE)
                .setName("name")
                .setDescription("description");

        when(scenarioGateway.findById(scenarioId)).thenReturn(Mono.just(scenario));
        when(scenarioGateway.persist(any(), eq(parentId), eq(parentType))).thenReturn(Mono.empty());

        Scenario duplicated = scenarioService.duplicate(scenarioId, parentId, parentType, context).block();

        assertAll(() -> {
            assertNotNull(duplicated);
            assertNotSame(scenario.getId(), duplicated.getId());
            assertEquals("condition id:" + newId, duplicated.getCondition());
            assertEquals(ScenarioCorrectness.correct, duplicated.getCorrectness());
            assertEquals("actions id:" + newId, duplicated.getActions());
            assertEquals("name", duplicated.getName());
            assertEquals("description", duplicated.getDescription());
            assertEquals(ScenarioLifecycle.ACTIVITY_COMPLETE, duplicated.getLifecycle());
        });
    }

    @Test
    void getScenarioParentTypeByLifecycle_activity() {
        String parentType = scenarioService.getScenarioParentTypeByLifecycle(ScenarioLifecycle.ACTIVITY_COMPLETE);
        assertEquals("ACTIVITY", parentType);
    }

    @Test
    void getScenarioParentTypeByLifecycle_interactive() {
        String parentType = scenarioService.getScenarioParentTypeByLifecycle(ScenarioLifecycle.INTERACTIVE_EVALUATE);
        assertEquals("INTERACTIVE", parentType);
    }

    @Test
    void findParent_notFound() {
        UUID scenarioId = UUID.randomUUID();

        TestPublisher<ParentByScenario> publisher = TestPublisher.create();
        publisher.error(new NoSuchElementException("not found"));

        when(scenarioGateway.findParent(scenarioId)).thenReturn(publisher.mono());

        assertThrows(ScenarioParentNotFoundException.class, () -> scenarioService.findParent(scenarioId).block());
    }
}
