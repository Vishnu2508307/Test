package com.smartsparrow.learner.service;


import static com.smartsparrow.courseware.CoursewareDataStubs.ELEMENT_ID;
import static com.smartsparrow.courseware.CoursewareDataStubs.mockCoursewareElement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerScopeReference;
import com.smartsparrow.learner.data.StudentScope;
import com.smartsparrow.learner.data.StudentScopeData;
import com.smartsparrow.learner.data.StudentScopeEntry;
import com.smartsparrow.learner.data.StudentScopeGateway;
import com.smartsparrow.learner.data.StudentScopeTrace;
import com.smartsparrow.learner.lang.DataValidationException;
import com.smartsparrow.learner.lang.InvalidFieldsException;
import com.smartsparrow.learner.payload.StudentScopePayload;
import com.smartsparrow.plugin.data.PluginManifest;
import com.smartsparrow.plugin.service.PluginSchemaParser;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class StudentScopeServiceTest {

    @InjectMocks
    private StudentScopeService studentScopeService;

    @Mock
    private PluginService pluginService;
    @Mock
    private StudentScopeGateway studentScopeGateway;
    @Mock
    private LearnerService learnerService;
    @Mock
    private PluginSchemaParser pluginSchemaParser;
    @Mock
    private LearnerCoursewareService learnerCoursewareService;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID scopeURN = UUID.randomUUID();
    private static final UUID scopeId = UUID.randomUUID();
    private static final UUID sourceId = UUID.randomUUID();

    private static StudentScopeEntry scopeEntry = new StudentScopeEntry().setId(UUID.randomUUID())
            .setScopeId(scopeId).setSourceId(sourceId).setData("data");

    private static LearnerScopeReference registry;

    static {
        registry = new LearnerScopeReference()
                .setPluginId(UUID.randomUUID())
                .setPluginVersion("1.2.0")
                .setElementId(sourceId)
                .setDeploymentId(deploymentId)
                .setElementType(CoursewareElementType.COMPONENT);
    }

    private StudentScopeService serviceSpy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        serviceSpy = spy(studentScopeService);

        CoursewareElement element = mockCoursewareElement(ELEMENT_ID, CoursewareElementType.ACTIVITY);

        when(learnerService.findWalkable(scopeURN, deploymentId))
                .thenReturn(Mono.just(element));

        when(learnerCoursewareService.getAncestry(deploymentId, ELEMENT_ID, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(Lists.newArrayList(element)));
    }

    @Test
    void createScopeEntry() {
        when(studentScopeGateway.persist(any(StudentScopeEntry.class))).thenReturn(Flux.empty());

        StudentScopeEntry result = studentScopeService.createScopeEntry(scopeId, sourceId, "data").block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(scopeId, result.getScopeId());
        assertEquals(sourceId, result.getSourceId());
        assertEquals("data", result.getData());
        verify(studentScopeGateway).persist(result);
    }

    @Test
    void createScopeEntry_idProvided() {
        UUID id = UUIDs.random();
        when(studentScopeGateway.persist(any(StudentScopeEntry.class))).thenReturn(Flux.empty());

        StudentScopeEntry result = studentScopeService.createScopeEntry(scopeId, sourceId, "data", id).block();

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(scopeId, result.getScopeId());
        assertEquals(sourceId, result.getSourceId());
        assertEquals("data", result.getData());
        verify(studentScopeGateway).persist(result);
    }

    @Test
    void createFromOutputSchema() {
    }

    @Test
    void findScopeId() {
        StudentScope scope = new StudentScope().setId(scopeId);
        when(studentScopeGateway.fetchLatestScope(deploymentId, accountId, scopeURN)).thenReturn(Mono.just(scope));

        UUID result = studentScopeService.findScopeId(deploymentId, accountId, scopeURN).block();

        assertEquals(scopeId, result);
    }

    @Test
    void findScopeId_notFound() {
        when(studentScopeGateway.fetchLatestScope(deploymentId, accountId, scopeURN)).thenReturn(Mono.empty());

        UUID result = studentScopeService.findScopeId(deploymentId, accountId, scopeURN).block();

        assertNull(result);
    }

    @Test
    void fetchScopeEntry() {
        StudentScopeEntry entry = new StudentScopeEntry().setScopeId(scopeId).setSourceId(sourceId).setData("data");
        when(studentScopeGateway.fetchLatestEntry(scopeId, sourceId)).thenReturn(Mono.just(entry));

        StudentScopeEntry result = studentScopeService.fetchScopeEntry(scopeId, sourceId).block();

        assertEquals(entry, result);
    }

    @Test
    void fetchScopeEntry_notFound() {
        when(studentScopeGateway.fetchLatestEntry(scopeId, sourceId)).thenReturn(Mono.empty());

        StudentScopeEntry result = studentScopeService.fetchScopeEntry(scopeId, sourceId).block();

        assertNull(result);
    }

    @Test
    void fetchScope_noScope() {
        //scope is not initialized yes
        doReturn(Mono.empty()).when(serviceSpy).findScopeId(deploymentId, accountId, scopeURN);
        //create new scope
        StudentScope newScope = new StudentScope().setId(UUID.randomUUID());
        doReturn(Mono.just(newScope)).when(serviceSpy).createScope(deploymentId, accountId, scopeURN);
        when(learnerService.findAllRegistered(scopeURN, deploymentId, changeId)).thenReturn(Flux.just(registry));
        //scope entry is not initialized yet
        doReturn(Mono.empty()).when(serviceSpy).fetchScopeEntry(newScope.getId(), sourceId);
        //initialize scope entry
        doReturn(Mono.just("output config")).when(serviceSpy).initFromOutputSchema(newScope.getId(), registry);
        StudentScopeEntry entry = new StudentScopeEntry().setScopeId(newScope.getId()).setSourceId(sourceId).setData("output config");
        doReturn(Mono.just(entry)).when(serviceSpy).createScopeEntry(newScope.getId(), sourceId, "output config");

        List<StudentScopePayload> result = serviceSpy.fetchScope(deploymentId, accountId, scopeURN, changeId).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sourceId, result.get(0).getSourceId());
        assertEquals(scopeURN, result.get(0).getScopeURN());
        assertEquals("output config", result.get(0).getData());
    }

    @Test
    void fetchScope_scopeExists_newComponentAdded() {
        //scope already exists
        doReturn(Mono.just(scopeId)).when(serviceSpy).findScopeId(deploymentId, accountId, scopeURN);
        doReturn(Mono.empty()).when(serviceSpy).createScope(deploymentId, accountId, scopeURN);
        when(learnerService.findAllRegistered(scopeURN, deploymentId, changeId)).thenReturn(Flux.just(registry));
        //scope entry does not exist
        doReturn(Mono.empty()).when(serviceSpy).fetchScopeEntry(scopeId, sourceId);
        doReturn(Mono.just(scopeEntry.getData())).when(serviceSpy).initFromOutputSchema(scopeId, registry);
        doReturn(Mono.just(scopeEntry)).when(serviceSpy).createScopeEntry(scopeId, sourceId, scopeEntry.getData());

        List<StudentScopePayload> result = serviceSpy.fetchScope(deploymentId, accountId, scopeURN, changeId).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sourceId, result.get(0).getSourceId());
        assertEquals(scopeURN, result.get(0).getScopeURN());
        assertEquals("data", result.get(0).getData());
    }

    @Test
    void fetchScope_scopeExists_entryExists() {
        //scope already exists
        doReturn(Mono.just(scopeId)).when(serviceSpy).findScopeId(deploymentId, accountId, scopeURN);
        doReturn(Mono.empty()).when(serviceSpy).createScope(deploymentId, accountId, scopeURN);
        when(learnerService.findAllRegistered(scopeURN, deploymentId, changeId)).thenReturn(Flux.just(registry));
        //scope entry already exists
        doReturn(Mono.just(scopeEntry)).when(serviceSpy).fetchScopeEntry(scopeId, sourceId);
        doReturn(Mono.empty()).when(serviceSpy).initFromOutputSchema(scopeId, registry);

        List<StudentScopePayload> result = serviceSpy.fetchScope(deploymentId, accountId, scopeURN, changeId).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sourceId, result.get(0).getSourceId());
        assertEquals(scopeURN, result.get(0).getScopeURN());
        assertEquals("data", result.get(0).getData());
    }

    @Test
    void fetchScope_registryEmpty() {
        //scope already exists
        doReturn(Mono.just(scopeId)).when(serviceSpy).findScopeId(deploymentId, accountId, scopeURN);
        doReturn(Mono.empty()).when(serviceSpy).createScope(deploymentId, accountId, scopeURN);
        //registry empty
        when(learnerService.findAllRegistered(scopeURN, deploymentId, changeId)).thenReturn(Flux.empty());

        List<StudentScopePayload> result = serviceSpy.fetchScope(deploymentId, accountId, scopeURN, changeId).collectList().block();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void initFromOutputSchema() {
        when(pluginService.findOutputSchema(any(UUID.class), anyString())).thenReturn(Mono.just("{outputSchema}"));
        when(pluginService.findPluginManifest(registry.getPluginId(), "1.2.0"))
                .thenReturn(Mono.just(new PluginManifest().setOutputSchema("{outputSchema}")));
        when(learnerCoursewareService.fetchConfig(sourceId, deploymentId, CoursewareElementType.COMPONENT)).thenReturn(Mono.just("{config}"));
        when(pluginSchemaParser.extractOutputConfig("{outputSchema}", "{config}")).thenReturn("{output config}");

        String result = studentScopeService.initFromOutputSchema(scopeId, registry).block();

        assertEquals("{output config}", result);
    }

    @Test
    void initFromOutputSchema_noOutputSchema() {
        when(pluginService.findOutputSchema(any(UUID.class), anyString())).thenReturn(Mono.just(""));
        when(pluginService.findPluginManifest(registry.getPluginId(), "1.2.0"))
                .thenReturn(Mono.just(new PluginManifest().setOutputSchema(null)));
        when(learnerCoursewareService.fetchConfig(sourceId, deploymentId, CoursewareElementType.COMPONENT)).thenReturn(Mono.just("{config}"));
        when(pluginSchemaParser.extractOutputConfig("", "{config}")).thenReturn("");

        String result = studentScopeService.initFromOutputSchema(scopeId, registry).block();

        assertEquals("", result);
    }

    @Test
    void initFromOutputSchema_emptyOutputSchema() {
        when(pluginService.findOutputSchema(any(UUID.class), anyString())).thenReturn(Mono.just(""));
        when(pluginService.findPluginManifest(registry.getPluginId(), "1.2.0"))
                .thenReturn(Mono.just(new PluginManifest().setOutputSchema("")));
        when(learnerCoursewareService.fetchConfig(sourceId, deploymentId, CoursewareElementType.COMPONENT)).thenReturn(Mono.just("{config}"));
        when(pluginSchemaParser.extractOutputConfig("", "{config}")).thenReturn("");

        String result = studentScopeService.initFromOutputSchema(scopeId, registry).block();

        assertEquals("", result);
    }

    @Test
    void initFromOutputSchema_noConfig() {
        when(pluginService.findOutputSchema(any(UUID.class), anyString())).thenReturn(Mono.just("{output schema}"));
        when(pluginService.findPluginManifest(registry.getPluginId(), "1.2.0"))
                .thenReturn(Mono.just(new PluginManifest().setOutputSchema("{output schema}")));
        when(learnerCoursewareService.fetchConfig(sourceId, deploymentId, CoursewareElementType.COMPONENT)).thenReturn(Mono.empty());
        when(pluginSchemaParser.extractOutputConfig("{output schema}", "")).thenReturn("");

        String result = studentScopeService.initFromOutputSchema(scopeId, registry).block();

        assertEquals("", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void setStudentScope_invalidData() {
        UUID sourceId = UUID.randomUUID();
        UUID pluginId = UUID.randomUUID();
        String pluginVersion = "1.2.1";
        String data = "{" +
                "\"selection\": {\n" +
                "\"type\": \"list\",\n" +
                "\"listType\": \"text\",\n" +
                "\"learnerEditable\": true,\n" +
                "\"label\": \"selection\"\n" +
                "}," +
                "\"invalid\":\"foo\"" +
                "}";
        String schema = "{" +
                "\"selection\": {\n" +
                "\"type\": \"list\",\n" +
                "\"listType\": \"text\",\n" +
                "\"learnerEditable\": true,\n" +
                "\"label\": \"selection\"\n" +
                "}" +
                "}";

        Deployment deployment = new Deployment()
                .setId(deploymentId)
                .setChangeId(changeId);

        LearnerScopeReference learnerScopeReference = new LearnerScopeReference()
                .setPluginVersion(pluginVersion)
                .setPluginId(pluginId);

        StudentScope studentScope = new StudentScope()
                .setId(scopeId);

        when(learnerService.findRegisteredElement(scopeURN, deploymentId, changeId, sourceId))
                .thenReturn(Mono.just(learnerScopeReference));

        when(pluginService.findOutputSchema(pluginId, pluginVersion)).thenReturn(Mono.just(schema));
        when(studentScopeGateway.fetchLatestScope(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(studentScope));
        doThrow(InvalidFieldsException.class).when(pluginSchemaParser).validateDataAgainstSchema(data, schema);
        TestPublisher<Void> persistPublisher = TestPublisher.create();
        persistPublisher.complete();
        when(studentScopeGateway.persist(any(StudentScope.class), any(List.class))).thenReturn(persistPublisher.flux());
        when(studentScopeGateway.persist(any(StudentScopeEntry.class))).thenReturn(Flux.just(new Void[]{}));

        DataValidationException e = assertThrows(DataValidationException.class,
                ()-> studentScopeService.setStudentScope(deployment, accountId, scopeURN, sourceId, data).block());

        persistPublisher.assertWasNotRequested();
        assertEquals("data has invalid fields", e.getMessage());
        assertTrue(e.getCause() instanceof InvalidFieldsException);

        verify(studentScopeGateway, never()).persist(any(StudentScopeEntry.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void setStudentScope_validData_scopeFound() {
        UUID sourceId = UUID.randomUUID();
        UUID pluginId = UUID.randomUUID();
        String pluginVersion = "1.2.1";
        String data = "{" +
                "\"selection\": {\n" +
                "\"type\": \"list\",\n" +
                "\"listType\": \"text\",\n" +
                "\"learnerEditable\": true,\n" +
                "\"label\": \"selection\"\n" +
                "}" +
                "}";
        String schema = "{" +
                "\"selection\": {\n" +
                "\"type\": \"list\",\n" +
                "\"listType\": \"text\",\n" +
                "\"learnerEditable\": true,\n" +
                "\"label\": \"selection\"\n" +
                "}" +
                "}";

        Deployment deployment = new Deployment()
                .setId(deploymentId)
                .setChangeId(changeId);

        LearnerScopeReference learnerScopeReference = new LearnerScopeReference()
                .setPluginVersion(pluginVersion)
                .setPluginId(pluginId);

        StudentScope studentScope = new StudentScope()
                .setId(scopeId);

        when(learnerService.findRegisteredElement(scopeURN, deploymentId, changeId, sourceId))
                .thenReturn(Mono.just(learnerScopeReference));

        when(pluginService.findOutputSchema(pluginId, pluginVersion)).thenReturn(Mono.just(schema));
        when(studentScopeGateway.fetchLatestScope(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(studentScope));
        TestPublisher<Void> persistPublisher = TestPublisher.create();
        persistPublisher.complete();
        when(studentScopeGateway.persist(any(StudentScope.class), any(List.class))).thenReturn(persistPublisher.flux());
        when(studentScopeGateway.persist(any(StudentScopeEntry.class))).thenReturn(Flux.just(new Void[]{}));
        StudentScopeEntry studentScopeEntry = studentScopeService.setStudentScope(deployment, accountId, scopeURN, sourceId, data).block();

        persistPublisher.assertWasNotRequested();
        assertNotNull(studentScopeEntry);
        assertEquals(scopeId, studentScopeEntry.getScopeId());
        assertNotNull(studentScopeEntry.getId());
        assertEquals(sourceId, studentScopeEntry.getSourceId());
        assertEquals(data, studentScopeEntry.getData());

        verify(studentScopeGateway).persist(any(StudentScopeEntry.class));
    }


    @Test
    @SuppressWarnings("unchecked")
    void setStudentScope_validData_scopeNotFound() {
        UUID sourceId = UUID.randomUUID();
        UUID pluginId = UUID.randomUUID();
        String pluginVersion = "1.2.1";
        String data = "{" +
                "\"selection\": {\n" +
                "\"type\": \"list\",\n" +
                "\"listType\": \"text\",\n" +
                "\"learnerEditable\": true,\n" +
                "\"label\": \"selection\"\n" +
                "}" +
                "}";
        String schema = "{" +
                "\"selection\": {\n" +
                "\"type\": \"list\",\n" +
                "\"listType\": \"text\",\n" +
                "\"learnerEditable\": true,\n" +
                "\"label\": \"selection\"\n" +
                "}" +
                "}";

        Deployment deployment = new Deployment()
                .setId(deploymentId)
                .setChangeId(changeId);

        LearnerScopeReference learnerScopeReference = new LearnerScopeReference()
                .setPluginVersion(pluginVersion)
                .setPluginId(pluginId);

        when(learnerService.findRegisteredElement(scopeURN, deploymentId, changeId, sourceId))
                .thenReturn(Mono.just(learnerScopeReference));

        when(pluginService.findOutputSchema(pluginId, pluginVersion)).thenReturn(Mono.just(schema));
        when(studentScopeGateway.fetchLatestScope(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.empty());
        TestPublisher<Void> persistPublisher = TestPublisher.create();
        persistPublisher.complete();
        when(studentScopeGateway.persist(any(StudentScope.class), any(List.class))).thenReturn(persistPublisher.flux());
        when(studentScopeGateway.persist(any(StudentScopeEntry.class))).thenReturn(Flux.just(new Void[]{}));
        StudentScopeEntry studentScopeEntry = studentScopeService.setStudentScope(deployment, accountId, scopeURN, sourceId, data).block();

        persistPublisher.assertWasRequested();
        assertNotNull(studentScopeEntry);
        assertNotNull(studentScopeEntry.getScopeId());
        assertNotNull(studentScopeEntry.getId());
        assertEquals(sourceId, studentScopeEntry.getSourceId());
        assertEquals(data, studentScopeEntry.getData());

        verify(studentScopeGateway).persist(any(StudentScopeEntry.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void setStudentScope_validData_scopeFound_IdProvided() {
        UUID id = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID pluginId = UUID.randomUUID();
        String pluginVersion = "1.2.1";
        String data = "{" +
                "\"selection\": {\n" +
                "\"type\": \"list\",\n" +
                "\"listType\": \"text\",\n" +
                "\"learnerEditable\": true,\n" +
                "\"label\": \"selection\"\n" +
                "}" +
                "}";
        String schema = "{" +
                "\"selection\": {\n" +
                "\"type\": \"list\",\n" +
                "\"listType\": \"text\",\n" +
                "\"learnerEditable\": true,\n" +
                "\"label\": \"selection\"\n" +
                "}" +
                "}";

        Deployment deployment = new Deployment()
                .setId(deploymentId)
                .setChangeId(changeId);

        LearnerScopeReference learnerScopeReference = new LearnerScopeReference()
                .setPluginVersion(pluginVersion)
                .setPluginId(pluginId);

        StudentScope studentScope = new StudentScope()
                .setId(scopeId);

        when(learnerService.findRegisteredElement(scopeURN, deploymentId, changeId, sourceId))
                .thenReturn(Mono.just(learnerScopeReference));

        when(pluginService.findOutputSchema(pluginId, pluginVersion)).thenReturn(Mono.just(schema));
        when(studentScopeGateway.fetchLatestScope(any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(studentScope));
        TestPublisher<Void> persistPublisher = TestPublisher.create();
        persistPublisher.complete();
        when(studentScopeGateway.persist(any(StudentScope.class), any(List.class))).thenReturn(persistPublisher.flux());
        when(studentScopeGateway.persist(any(StudentScopeEntry.class))).thenReturn(Flux.just(new Void[]{}));
        StudentScopeEntry studentScopeEntry = studentScopeService.setStudentScope(deployment, accountId, scopeURN, sourceId, data, id).block();

        persistPublisher.assertWasNotRequested();
        assertNotNull(studentScopeEntry);
        assertEquals(scopeId, studentScopeEntry.getScopeId());
        assertNotNull(studentScopeEntry.getId());
        assertEquals(sourceId, studentScopeEntry.getSourceId());
        assertEquals(data, studentScopeEntry.getData());

        verify(studentScopeGateway).persist(any(StudentScopeEntry.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("It should throw an exception when the ancestry list is empty")
    void createStudentScope_rootElement() {
        CoursewareElement element = mockCoursewareElement(ELEMENT_ID, CoursewareElementType.ACTIVITY);
        when(learnerService.findWalkable(scopeURN, deploymentId))
                .thenReturn(Mono.just(element));

        when(learnerCoursewareService.getAncestry(deploymentId, element.getElementId(), element.getElementType()))
                .thenReturn(Mono.just(new ArrayList<>()));

        when(studentScopeGateway.persist(any(StudentScope.class), any(List.class))).thenReturn(Flux.just());

        IllegalStateFault e = assertThrows(IllegalStateFault.class,
                () -> studentScopeService.createScope(deploymentId, accountId, scopeURN).block());

        assertNotNull(e);
        assertEquals("ancestry list must contain at least 1 element", e.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("It should create the student scope and track the ancestry for the root element")
    void createStudentScope_withAncestry_isRoot() {
        CoursewareElement element = mockCoursewareElement(ELEMENT_ID, CoursewareElementType.ACTIVITY);
        List<CoursewareElement> ancestry = Lists.newArrayList(
                element
        );

        when(learnerService.findWalkable(scopeURN, deploymentId))
                .thenReturn(Mono.just(element));
        when(learnerCoursewareService.getAncestry(deploymentId, element.getElementId(), element.getElementType()))
                .thenReturn(Mono.just(ancestry));
        when(studentScopeGateway.persist(any(StudentScope.class), any(List.class))).thenReturn(Flux.just());

        ArgumentCaptor<List<StudentScopeTrace>> captor = ArgumentCaptor.forClass(List.class);

        StudentScope created = studentScopeService.createScope(deploymentId, accountId, scopeURN).block();

        assertNotNull(created);
        assertEquals(deploymentId, created.getDeploymentId());
        assertEquals(accountId, created.getAccountId());
        assertEquals(scopeURN, created.getScopeUrn());
        assertNotNull(created.getId());

        verify(studentScopeGateway).persist(eq(created), captor.capture());
        verify(learnerCoursewareService).getAncestry(deploymentId, ELEMENT_ID, CoursewareElementType.ACTIVITY);

        List<StudentScopeTrace> tracked = captor.getValue();

        assertNotNull(tracked);
        assertEquals(1, tracked.size());

        tracked.forEach(one -> {
            assertNotNull(one);
            assertEquals(deploymentId, one.getDeploymentId());
            assertEquals(accountId, one.getStudentId());
            assertEquals(scopeURN, one.getStudentScopeUrn());
            assertEquals(created.getId(), one.getScopeId());
            assertNotNull(one.getElementId());
            assertNotNull(one.getElementType());
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("It should create the student scope and track the ancestry")
    void createStudentScope_withAncestry_nonRootElement() {
        UUID parentPathwayId = UUID.randomUUID();
        UUID rootActivityId = UUID.randomUUID();
        CoursewareElement element = mockCoursewareElement(ELEMENT_ID, CoursewareElementType.INTERACTIVE);
        List<CoursewareElement> ancestry = Lists.newArrayList(
                element,
                mockCoursewareElement(parentPathwayId, CoursewareElementType.PATHWAY),
                mockCoursewareElement(rootActivityId, CoursewareElementType.ACTIVITY)
        );

        when(learnerService.findWalkable(scopeURN, deploymentId))
                .thenReturn(Mono.just(element));
        when(learnerCoursewareService.getAncestry(deploymentId, element.getElementId(), element.getElementType()))
                .thenReturn(Mono.just(ancestry));
        when(studentScopeGateway.persist(any(StudentScope.class), any(List.class))).thenReturn(Flux.just());

        ArgumentCaptor<List<StudentScopeTrace>> captor = ArgumentCaptor.forClass(List.class);

        StudentScope created = studentScopeService.createScope(deploymentId, accountId, scopeURN).block();

        assertNotNull(created);
        assertEquals(deploymentId, created.getDeploymentId());
        assertEquals(accountId, created.getAccountId());
        assertEquals(scopeURN, created.getScopeUrn());
        assertNotNull(created.getId());

        verify(studentScopeGateway).persist(eq(created), captor.capture());
        verify(learnerCoursewareService).getAncestry(deploymentId, ELEMENT_ID, CoursewareElementType.INTERACTIVE);

        List<StudentScopeTrace> tracked = captor.getValue();

        assertNotNull(tracked);
        assertEquals(3, tracked.size());

        tracked.forEach(one -> {
            assertNotNull(one);
            assertEquals(deploymentId, one.getDeploymentId());
            assertEquals(accountId, one.getStudentId());
            assertEquals(scopeURN, one.getStudentScopeUrn());
            assertEquals(created.getId(), one.getScopeId());
            assertNotNull(one.getElementId());
            assertNotNull(one.getElementType());
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void resetScopesFor_noInitialisedScopesToReset() {
        when(studentScopeGateway.findInitialisedStudentScopeSubTree(deploymentId, accountId, ELEMENT_ID))
                .thenReturn(Flux.empty());

        List<StudentScope> scopes = studentScopeService.resetScopesFor(deploymentId, ELEMENT_ID, accountId)
                .collectList()
                .block();

        assertNotNull(scopes);
        assertEquals(0, scopes.size());

        verify(studentScopeGateway, never()).persist(any(StudentScope.class), any(List.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void resetScopesFor() {
        UUID oldScopeId = UUID.randomUUID();
        StudentScopeTrace trace = new StudentScopeTrace()
                .setStudentScopeUrn(scopeURN);

        when(studentScopeGateway.persist(any(StudentScope.class), any(List.class))).thenReturn(Flux.just());

        when(studentScopeGateway.fetchLatestScope(deploymentId, accountId, scopeURN))
                .thenReturn(Mono.just(new StudentScope().setId(oldScopeId)));

        when(studentScopeGateway.findInitialisedStudentScopeSubTree(deploymentId, accountId, ELEMENT_ID))
                .thenReturn(Flux.just(trace));

        List<StudentScope> scopes = studentScopeService.resetScopesFor(deploymentId, ELEMENT_ID, accountId)
                .collectList()
                .block();

        assertNotNull(scopes);
        assertEquals(1, scopes.size());

        verify(studentScopeGateway, times(1)).persist(any(StudentScope.class), any(List.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void resetScopesFor_multipleScopesToReset() {
        UUID oldScopeId = UUID.randomUUID();
        UUID scopeUrnTwo = UUID.randomUUID();
        StudentScopeTrace traceOne = new StudentScopeTrace()
                .setStudentScopeUrn(scopeURN);
        StudentScopeTrace traceTwo = new StudentScopeTrace()
                .setStudentScopeUrn(scopeUrnTwo);

        when(studentScopeGateway.persist(any(StudentScope.class), any(List.class))).thenReturn(Flux.just());

        when(studentScopeGateway.fetchLatestScope(deploymentId, accountId, scopeURN))
                .thenReturn(Mono.just(new StudentScope().setId(oldScopeId)));

        when(studentScopeGateway.fetchLatestScope(deploymentId, accountId, scopeUrnTwo))
                .thenReturn(Mono.just(new StudentScope().setId(oldScopeId)));

        when(studentScopeGateway.findInitialisedStudentScopeSubTree(deploymentId, accountId, ELEMENT_ID))
                .thenReturn(Flux.just(traceOne, traceTwo));

        CoursewareElement element = mockCoursewareElement(ELEMENT_ID, CoursewareElementType.ACTIVITY);
        CoursewareElement elementTwo = mockCoursewareElement(UUID.randomUUID(), CoursewareElementType.PATHWAY);

        when(learnerCoursewareService.getAncestry(deploymentId, elementTwo.getElementId(), elementTwo.getElementType()))
                .thenReturn(Mono.just(Lists.newArrayList(elementTwo)));

        when(learnerCoursewareService.getAncestry(deploymentId, ELEMENT_ID, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(Lists.newArrayList(element, elementTwo)));

        when(learnerService.findWalkable(scopeUrnTwo, deploymentId))
                .thenReturn(Mono.just(elementTwo));

        List<StudentScope> scopeIds = studentScopeService.resetScopesFor(deploymentId, ELEMENT_ID, accountId)
                .collectList()
                .block();

        assertNotNull(scopeIds);
        assertEquals(2, scopeIds.size());
        assertTrue(scopeIds.stream().noneMatch(one -> one.getId().equals(oldScopeId)));
        verify(studentScopeGateway, times(2)).persist(any(StudentScope.class), any(List.class));
    }

    @Test
    void findLatestEntries_scopeIdNotFound() {
        when(studentScopeGateway.fetchLatestScope(deploymentId, accountId, scopeURN)).thenReturn(Mono.empty());

        Map<UUID, String> entries = studentScopeService.findLatestEntries(deploymentId, accountId, scopeURN).block();

        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
    void findLatestEntries_noEntriesFound() {
        StudentScope scope = new StudentScope()
                .setId(scopeId);
        when(studentScopeGateway.fetchLatestScope(deploymentId, accountId, scopeURN)).thenReturn(Mono.just(scope));
        when(studentScopeGateway.fetchLatestEntries(scopeId)).thenReturn(Flux.empty());

        Map<UUID, String> entries = studentScopeService.findLatestEntries(deploymentId, accountId, scopeURN).block();

        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
    void findLatestEntries_oneFound() {
        StudentScope scope = new StudentScope()
                .setId(scopeId);
        StudentScopeData scopeData = new StudentScopeData()
                .setData("data")
                .setSourceId(UUID.randomUUID());
        when(studentScopeGateway.fetchLatestScope(deploymentId, accountId, scopeURN)).thenReturn(Mono.just(scope));
        when(studentScopeGateway.fetchLatestEntries(scopeId)).thenReturn(Flux.just(scopeData));

        Map<UUID, String> entries = studentScopeService.findLatestEntries(deploymentId, accountId, scopeURN).block();

        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        assertEquals(1, entries.size());
        List<Map.Entry> entriesList = new ArrayList<>(entries.entrySet());
        assertEquals(scopeData.getSourceId(), entriesList.get(0).getKey());
        assertEquals(scopeData.getData(), entriesList.get(0).getValue());
    }

    @Test
    void findLatestEntries_multipleFound() {
        StudentScope scope = new StudentScope()
                .setId(scopeId);

        StudentScopeData scopeDataOne = new StudentScopeData()
                .setData("data 1")
                .setSourceId(UUID.randomUUID());

        StudentScopeData scopeDataTwo = new StudentScopeData()
                .setData("data 2")
                .setSourceId(UUID.randomUUID());

        StudentScopeData scopeDataThree = new StudentScopeData()
                .setData("data 3")
                .setSourceId(UUID.randomUUID());
        when(studentScopeGateway.fetchLatestScope(deploymentId, accountId, scopeURN)).thenReturn(Mono.just(scope));

        when(studentScopeGateway.fetchLatestEntries(scopeId)).thenReturn(Flux.just(
                scopeDataOne,
                scopeDataTwo,
                scopeDataThree
        ));

        Map<UUID, String> entries = studentScopeService.findLatestEntries(deploymentId, accountId, scopeURN).block();

        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        assertEquals(3, entries.size());
        List<Map.Entry> entriesList = new ArrayList<>(entries.entrySet());

        assertTrue(entriesList.stream().anyMatch(one -> scopeDataOne.getSourceId().equals(one.getKey())));
        assertTrue(entriesList.stream().anyMatch(one -> scopeDataOne.getData().equals(one.getValue())));

        assertTrue(entriesList.stream().anyMatch(two -> scopeDataTwo.getSourceId().equals(two.getKey())));
        assertTrue(entriesList.stream().anyMatch(two -> scopeDataTwo.getData().equals(two.getValue())));

        assertTrue(entriesList.stream().anyMatch(three -> scopeDataThree.getSourceId().equals(three.getKey())));
        assertTrue(entriesList.stream().anyMatch(three -> scopeDataThree.getData().equals(three.getValue())));
    }
}
