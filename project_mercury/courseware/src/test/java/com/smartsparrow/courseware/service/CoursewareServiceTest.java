package com.smartsparrow.courseware.service;

import static com.smartsparrow.competency.DocumentDataStubs.DOCUMENT_ID;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A_ID;
import static com.smartsparrow.competency.DocumentDataStubs.buildTag;
import static com.smartsparrow.courseware.CoursewareDataStubs.buildConfigurationField;
import static com.smartsparrow.courseware.pathway.PathwayMock.mockPathway;
import static com.smartsparrow.courseware.service.ScopeReferenceStub.buildScopeReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.google.common.collect.Lists;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.competency.service.DocumentItemLinkService;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementAncestry;
import com.smartsparrow.courseware.data.CoursewareElementConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.CoursewareGateway;
import com.smartsparrow.courseware.data.Feedback;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.courseware.data.ParentByScenario;
import com.smartsparrow.courseware.data.PathwayConfig;
import com.smartsparrow.courseware.data.PluginReference;
import com.smartsparrow.courseware.data.RegisteredScopeReference;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.data.ScopeReference;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.lang.ComponentNotFoundException;
import com.smartsparrow.courseware.lang.CoursewareElementNotFoundFault;
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.lang.ParentActivityNotFoundException;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.lang.PluginReferenceNotFoundFault;
import com.smartsparrow.courseware.lang.ScenarioNotFoundException;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.service.Account;

import com.smartsparrow.learner.service.CoursewareElementMetaInformationService;
import com.smartsparrow.learner.service.ManualGradeDuplicationService;
import com.smartsparrow.plugin.data.PluginGateway;
import com.smartsparrow.plugin.data.PluginManifest;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.workspace.data.Project;
import com.smartsparrow.workspace.data.ProjectActivity;
import com.smartsparrow.workspace.data.ThemePayload;
import com.smartsparrow.workspace.service.ProjectService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

class CoursewareServiceTest {

    @InjectMocks
    private CoursewareService coursewareService;
    @Mock
    private ActivityService activityService;
    @Mock
    private InteractiveService interactiveService;
    @Mock
    private PathwayService pathwayService;
    @Mock
    private ScenarioService scenarioService;
    @Mock
    private PluginService pluginService;
    @Mock
    private ComponentGateway componentGateway;
    @Mock
    private ComponentService componentService;
    @Mock
    private FeedbackService feedbackService;
    @Mock
    private CoursewareAssetService coursewareAssetService;
    @Mock
    private CoursewareGateway coursewareGateway;
    @Mock
    private DocumentItemLinkService documentItemLinkService;
    @Mock
    private ManualGradeDuplicationService manualGradeDuplicationService;
    @Mock
    private CoursewareElementMetaInformationService coursewareElementMetaInformationService;
    @Mock
    private CacheService cacheService;
    @Mock
    private AnnotationDuplicationService annotationService;
    @Mock
    private ProjectService projectService;
    @Mock
    private PluginGateway pluginGateway;
    @Mock
    private ThemeService themeService;

    private static final UUID creatorId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID feedbackId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID scenarioId = UUID.randomUUID();
    private static final UUID annotationId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final String config = "My awesome config";
    private static final String themeConfig = "theme config";
    private static final UUID pluginId = UUID.randomUUID();
    private static final String pluginVersion = "1.*";
    private static final Account account = new Account().setId(creatorId).setSubscriptionId(UUID.randomUUID());

    Boolean isInSameProject = true;
    Boolean newDuplicateFlow = false;
    UUID rootElementId = UUID.randomUUID();
    UUID scopeId = UUID.randomUUID();
    UUID elementId = UUID.randomUUID();
    String version = "1.0.0";
    CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static String configSchema = "{\n" +
            "\t\"title\": {\n" +
            "      \"type\":\"text\"\n" +
            "    },\n" +
            "    \"description\":{\n" +
            "      \"type\": \"text\"\n" +  // changed prop
            "    },\n" +
            "    \"items\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"text\",\n" +
            "      \"label\": \"items\"\n" +
            "    },\n" +
            "    \"selection\": {\n" +
            "      \"type\": \"list\",\n" +
            "      \"listType\": \"list\",\n" +
            "      \"learnerEditable\": false,\n" +
            "      \"label\": \"selection\"\n" +
            "    }\n" +
            "}";



    private static final Interactive oldInteractive = new Interactive()
            .setId(interactiveId)
            .setPluginId(pluginId)
            .setPluginVersionExpr(pluginVersion)
            .setStudentScopeURN(UUID.randomUUID());
    private static final Interactive newInteractive = new Interactive()
            .setId(UUID.randomUUID())
            .setPluginId(pluginId)
            .setPluginVersionExpr(pluginVersion)
            .setStudentScopeURN(UUID.randomUUID());
    private DuplicationContext duplicationContext;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        duplicationContext = new DuplicationContext();

        when(documentItemLinkService.duplicateLinks(any(), any())).thenReturn(Flux.empty());
        when(coursewareGateway.findRegisteredElements(any(UUID.class))).thenReturn(Flux.empty());
        when(coursewareGateway.fetchConfigurationFields(any(UUID.class))).thenReturn(Flux.empty());
        when(coursewareGateway.persist(any(CoursewareElementConfigurationField.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(coursewareGateway.findRegisteredElements(oldInteractive.getStudentScopeURN())).thenReturn(Flux.empty());
        when(manualGradeDuplicationService.findManualGradingComponentByWalkable(any(UUID.class))).thenReturn(Flux.empty());
        when(manualGradeDuplicationService.persist(any(List.class))).thenReturn(Flux.empty());
        when(coursewareElementMetaInformationService.duplicate(any(UUID.class), any(UUID.class)))
                .thenReturn(Flux.empty());
        when(themeService.fetchThemeByElementId(any())).thenReturn(Mono.just(new ThemePayload()
                                                                                     .setThemeVariants(new ArrayList<>())));
        when(themeService.saveThemeByElement(any(), any(), any())).thenReturn(Mono.empty());

        when(activityService.isDuplicatedActivityInTheSameProject(any(UUID.class), any(UUID.class), any(Boolean.class)))
                .thenReturn(Mono.just(isInSameProject));

        when(coursewareAssetService.duplicateAssets(any(UUID.class), any(UUID.class), any(CoursewareElementType.class), any(DuplicationContext.class)))
                .thenReturn(Flux.empty());
    }

    @Test
    void getPath_topActivity() {
        when(activityService.findParentPathwayId(activityId)).thenReturn(Mono.empty());

        List<CoursewareElement> result = coursewareService.getPath(activityId, CoursewareElementType.ACTIVITY).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CoursewareElementType.ACTIVITY, result.get(0).getElementType());
        assertEquals(activityId, result.get(0).getElementId());
    }

    @Test
    void getPath_interactiveInLesson() {
        when(interactiveService.findParentPathwayId(interactiveId)).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(pathwayId)).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(activityId)).thenReturn(Mono.empty());

        List<CoursewareElement> result = coursewareService.getPath(interactiveId,
                                                                   CoursewareElementType.INTERACTIVE).block();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(CoursewareElementType.ACTIVITY, result.get(0).getElementType());
        assertEquals(activityId, result.get(0).getElementId());
        assertEquals(CoursewareElementType.PATHWAY, result.get(1).getElementType());
        assertEquals(pathwayId, result.get(1).getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, result.get(2).getElementType());
        assertEquals(interactiveId, result.get(2).getElementId());
    }

    @Test
    void getPath_forPathway() {
        when(pathwayService.findParentActivityId(pathwayId)).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(activityId)).thenReturn(Mono.empty());

        List<CoursewareElement> result = coursewareService.getPath(pathwayId, CoursewareElementType.PATHWAY).block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(CoursewareElementType.ACTIVITY, result.get(0).getElementType());
        assertEquals(activityId, result.get(0).getElementId());
        assertEquals(CoursewareElementType.PATHWAY, result.get(1).getElementType());
        assertEquals(pathwayId, result.get(1).getElementId());
    }

    @Test
    void getPath_interactiveInCourse() {
        UUID pathwayId2 = UUID.randomUUID();
        UUID pathwayId3 = UUID.randomUUID();
        UUID activityIdUnit = UUID.randomUUID();
        UUID activityIdCourse = UUID.randomUUID();

        when(interactiveService.findParentPathwayId(interactiveId)).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(pathwayId)).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(activityId)).thenReturn(Mono.just(pathwayId2));
        when(pathwayService.findParentActivityId(pathwayId2)).thenReturn(Mono.just(activityIdUnit));
        when(activityService.findParentPathwayId(activityIdUnit)).thenReturn(Mono.just(pathwayId3));
        when(pathwayService.findParentActivityId(pathwayId3)).thenReturn(Mono.just(activityIdCourse));
        when(activityService.findParentPathwayId(activityIdCourse)).thenReturn(Mono.empty());

        List<CoursewareElement> result = coursewareService.getPath(interactiveId,
                                                                   CoursewareElementType.INTERACTIVE).block();

        assertNotNull(result);
        assertEquals(7, result.size());
        assertEquals(CoursewareElementType.ACTIVITY, result.get(0).getElementType());
        assertEquals(activityIdCourse, result.get(0).getElementId());

        assertEquals(CoursewareElementType.PATHWAY, result.get(1).getElementType());
        assertEquals(pathwayId3, result.get(1).getElementId());

        assertEquals(CoursewareElementType.ACTIVITY, result.get(2).getElementType());
        assertEquals(activityIdUnit, result.get(2).getElementId());

        assertEquals(CoursewareElementType.PATHWAY, result.get(3).getElementType());
        assertEquals(pathwayId2, result.get(3).getElementId());

        assertEquals(CoursewareElementType.ACTIVITY, result.get(4).getElementType());
        assertEquals(activityId, result.get(4).getElementId());

        assertEquals(CoursewareElementType.PATHWAY, result.get(5).getElementType());
        assertEquals(pathwayId, result.get(5).getElementId());

        assertEquals(CoursewareElementType.INTERACTIVE, result.get(6).getElementType());
        assertEquals(interactiveId, result.get(6).getElementId());
    }

    @Test
    void getPath_feedbackInInteractive() {
        UUID interactiveId = UUID.randomUUID();
        UUID pathwayId2 = UUID.randomUUID();
        UUID activityId2 = UUID.randomUUID();
        UUID pathwayId1 = UUID.randomUUID();
        UUID activityId1 = UUID.randomUUID();

        when(feedbackService.findParentId(feedbackId)).thenReturn(Mono.just(interactiveId));
        when(interactiveService.findParentPathwayId(interactiveId)).thenReturn(Mono.just(pathwayId2));
        when(pathwayService.findParentActivityId(pathwayId2)).thenReturn(Mono.just(activityId2));
        when(activityService.findParentPathwayId(activityId2)).thenReturn(Mono.just(pathwayId1));
        when(pathwayService.findParentActivityId(pathwayId1)).thenReturn(Mono.just(activityId1));
        when(activityService.findParentPathwayId(activityId1)).thenReturn(Mono.empty());

        List<CoursewareElement> elements = coursewareService.getPath(feedbackId,
                                                                     CoursewareElementType.FEEDBACK).block();

        assertNotNull(elements);

        assertEquals(6, elements.size());
        assertEquals(activityId1, elements.get(0).getElementId());
        assertEquals(CoursewareElementType.ACTIVITY, elements.get(0).getElementType());

        assertEquals(feedbackId, elements.get(5).getElementId());
        assertEquals(CoursewareElementType.FEEDBACK, elements.get(5).getElementType());
    }

    @Test
    void getPath_componentInInteractive() {
        UUID interactiveId = UUID.randomUUID();
        UUID pathwayId2 = UUID.randomUUID();
        UUID activityId2 = UUID.randomUUID();
        UUID pathwayId1 = UUID.randomUUID();
        UUID activityId1 = UUID.randomUUID();

        ParentByComponent parent = new ParentByComponent()
                .setParentId(interactiveId)
                .setParentType(CoursewareElementType.INTERACTIVE)
                .setComponentId(componentId);

        when(componentService.findParentFor(componentId)).thenReturn(Mono.just(parent));
        when(interactiveService.findParentPathwayId(interactiveId)).thenReturn(Mono.just(pathwayId2));
        when(pathwayService.findParentActivityId(pathwayId2)).thenReturn(Mono.just(activityId2));
        when(activityService.findParentPathwayId(activityId2)).thenReturn(Mono.just(pathwayId1));
        when(pathwayService.findParentActivityId(pathwayId1)).thenReturn(Mono.just(activityId1));
        when(activityService.findParentPathwayId(activityId1)).thenReturn(Mono.empty());

        List<CoursewareElement> elements = coursewareService.getPath(componentId,
                                                                     CoursewareElementType.COMPONENT).block();

        assertNotNull(elements);

        assertEquals(6, elements.size());
        assertEquals(activityId1, elements.get(0).getElementId());
        assertEquals(CoursewareElementType.ACTIVITY, elements.get(0).getElementType());

        assertEquals(componentId, elements.get(5).getElementId());
        assertEquals(CoursewareElementType.COMPONENT, elements.get(5).getElementType());
    }

    @Test
    void getPath_componentInActivity() {
        UUID activityId = UUID.randomUUID();

        ParentByComponent parent = new ParentByComponent()
                .setParentId(activityId)
                .setParentType(CoursewareElementType.ACTIVITY)
                .setComponentId(componentId);

        when(componentService.findParentFor(componentId)).thenReturn(Mono.just(parent));
        when(activityService.findParentPathwayId(activityId)).thenReturn(Mono.empty());

        List<CoursewareElement> elements = coursewareService.getPath(componentId,
                                                                     CoursewareElementType.COMPONENT).block();

        assertNotNull(elements);

        assertEquals(2, elements.size());
        assertEquals(activityId, elements.get(0).getElementId());
        assertEquals(CoursewareElementType.ACTIVITY, elements.get(0).getElementType());
        assertEquals(componentId, elements.get(1).getElementId());
        assertEquals(CoursewareElementType.COMPONENT, elements.get(1).getElementType());

    }

    @Test
    void getPath_componentParentTypeNotAllowed() {
        UUID scenarioId = UUID.randomUUID();
        ParentByComponent parent = new ParentByComponent()
                .setParentId(scenarioId)
                .setParentType(CoursewareElementType.SCENARIO)
                .setComponentId(componentId);

        when(componentService.findParentFor(componentId)).thenReturn(Mono.just(parent));

        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
                                                       () -> coursewareService.getPath(componentId,
                                                                                       CoursewareElementType.COMPONENT).block());

        assertEquals("parentType SCENARIO not allowed for component", e.getMessage());
    }

    @Test
    void getPath_scenarioInInteractive() {
        UUID interactiveId = UUID.randomUUID();
        UUID pathwayId2 = UUID.randomUUID();
        UUID activityId2 = UUID.randomUUID();
        UUID pathwayId1 = UUID.randomUUID();
        UUID activityId1 = UUID.randomUUID();

        ParentByScenario parent = new ParentByScenario()
                .setParentId(interactiveId)
                .setParentType(CoursewareElementType.INTERACTIVE)
                .setScenarioId(scenarioId);

        when(scenarioService.findParent(scenarioId)).thenReturn(Mono.just(parent));
        when(interactiveService.findParentPathwayId(interactiveId)).thenReturn(Mono.just(pathwayId2));
        when(pathwayService.findParentActivityId(pathwayId2)).thenReturn(Mono.just(activityId2));
        when(activityService.findParentPathwayId(activityId2)).thenReturn(Mono.just(pathwayId1));
        when(pathwayService.findParentActivityId(pathwayId1)).thenReturn(Mono.just(activityId1));
        when(activityService.findParentPathwayId(activityId1)).thenReturn(Mono.empty());

        List<CoursewareElement> elements = coursewareService.getPath(scenarioId,
                                                                     CoursewareElementType.SCENARIO).block();

        assertNotNull(elements);

        assertEquals(6, elements.size());
        assertEquals(activityId1, elements.get(0).getElementId());
        assertEquals(CoursewareElementType.ACTIVITY, elements.get(0).getElementType());

        assertEquals(scenarioId, elements.get(5).getElementId());
        assertEquals(CoursewareElementType.SCENARIO, elements.get(5).getElementType());
    }

    @Test
    void getPath_scenarioInActivity() {
        UUID activityId = UUID.randomUUID();

        ParentByScenario parent = new ParentByScenario()
                .setParentId(activityId)
                .setParentType(CoursewareElementType.ACTIVITY)
                .setScenarioId(scenarioId);

        when(scenarioService.findParent(scenarioId)).thenReturn(Mono.just(parent));
        when(activityService.findParentPathwayId(activityId)).thenReturn(Mono.empty());

        List<CoursewareElement> elements = coursewareService.getPath(scenarioId,
                                                                     CoursewareElementType.SCENARIO).block();

        assertNotNull(elements);

        assertEquals(2, elements.size());
        assertEquals(activityId, elements.get(0).getElementId());
        assertEquals(CoursewareElementType.ACTIVITY, elements.get(0).getElementType());
        assertEquals(scenarioId, elements.get(1).getElementId());
        assertEquals(CoursewareElementType.SCENARIO, elements.get(1).getElementType());
    }

    @Test
    void getPath_scenarioParentTypeNotAllowed() {
        UUID componentId = UUID.randomUUID();
        ParentByScenario parent = new ParentByScenario()
                .setParentId(componentId)
                .setParentType(CoursewareElementType.COMPONENT)
                .setScenarioId(scenarioId);

        when(scenarioService.findParent(scenarioId)).thenReturn(Mono.just(parent));

        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
                                                       () -> coursewareService.getPath(scenarioId,
                                                                                       CoursewareElementType.SCENARIO).block());

        assertEquals("parentType COMPONENT not allowed for scenario", e.getMessage());
    }

    @Test
    void getPath_broken() {
        when(interactiveService.findParentPathwayId(interactiveId)).thenReturn(Mono.just(pathwayId));
        TestPublisher<UUID> errorPublisher = TestPublisher.create();
        errorPublisher.error(new ParentActivityNotFoundException(pathwayId));
        when(pathwayService.findParentActivityId(pathwayId)).thenReturn(errorPublisher.mono());

        assertThrows(ParentActivityNotFoundException.class,
                     () -> coursewareService.getPath(interactiveId, CoursewareElementType.INTERACTIVE).block());
    }

    @Test
    void getPath_circularDependency() {
        UUID pathwayId2 = UUID.randomUUID();
        UUID activityId2 = UUID.randomUUID();

        when(activityService.findParentPathwayId(activityId)).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(pathwayId)).thenReturn(Mono.just(activityId2));
        when(activityService.findParentPathwayId(activityId2)).thenReturn(Mono.just(pathwayId2));
        when(pathwayService.findParentActivityId(pathwayId2)).thenReturn(Mono.just(activityId));

        assertThrows(StackOverflowError.class,
                     () -> coursewareService.getPath(activityId, CoursewareElementType.ACTIVITY).block());
    }

    @Test
    void getParentActivityIds_fromComponent() {
        UUID interactiveId = UUID.randomUUID();
        UUID pathwayId2 = UUID.randomUUID();
        UUID activityId2 = UUID.randomUUID();
        UUID pathwayId1 = UUID.randomUUID();
        UUID activityId1 = UUID.randomUUID();

        ParentByComponent parent = new ParentByComponent()
                .setParentId(interactiveId)
                .setParentType(CoursewareElementType.INTERACTIVE)
                .setComponentId(componentId);

        when(componentService.findParentFor(componentId)).thenReturn(Mono.just(parent));
        when(interactiveService.findParentPathwayId(interactiveId)).thenReturn(Mono.just(pathwayId2));
        when(pathwayService.findParentActivityId(pathwayId2)).thenReturn(Mono.just(activityId2));
        when(activityService.findParentPathwayId(activityId2)).thenReturn(Mono.just(pathwayId1));
        when(pathwayService.findParentActivityId(pathwayId1)).thenReturn(Mono.just(activityId1));
        when(activityService.findParentPathwayId(activityId1)).thenReturn(Mono.empty());

        List<UUID> activityIds = coursewareService.getParentActivityIds(componentId,
                                                                        CoursewareElementType.COMPONENT).block();

        assertNotNull(activityIds);

        assertEquals(2, activityIds.size());
        assertEquals(activityId1, activityIds.get(0));
        assertEquals(activityId2, activityIds.get(1));
    }

    @Test
    void duplicateActivity_activityNotFound() {
        TestPublisher<Activity> activityPublisher = TestPublisher.create();
        activityPublisher.error(new ActivityNotFoundException(activityId));
        when(activityService.findById(activityId)).thenReturn(activityPublisher.mono());
        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(activityId)).thenReturn(Flux.empty());
        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());

        ActivityNotFoundException ex = assertThrows(ActivityNotFoundException.class,
                                                    () -> coursewareService.duplicateActivity(activityId,
                                                                                              account,
                                                                                              isInSameProject).block());

        assertEquals(String.format("no activity with id %s", activityId), ex.getMessage());

    }

    @Test
    void duplicateActivity_noAttachedObjects() {
        Activity activity = buildActivity(activityId);
        Activity newActivity = buildActivity(UUID.randomUUID());

        // mocking fetch
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(activityId)).thenReturn(Flux.empty());
        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());
        when(pluginService.findLatestVersion(pluginId, pluginVersion)).thenReturn(Mono.just("version"));
        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.empty());
        when(componentGateway.findComponentIdsByActivity(activityId)).thenReturn(Flux.empty());
        // mocking persistence
        when(activityService.duplicateActivity(eq(activity), eq(creatorId))).thenReturn(Mono.just(newActivity));
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        Activity duplicated = coursewareService.duplicateActivity(activityId, account, isInSameProject).block();

        verify(activityService).duplicateActivity(eq(activity), eq(creatorId));
        verify(scenarioService, never()).duplicate(any(UUID.class), any(UUID.class), any(), any());
        verify(activityService, never()).duplicateConfig(any(), any(), any());
        verify(activityService, never()).duplicateTheme(any(), any());
        verify(pathwayService, never()).duplicatePathway(any(), any());
        verify(componentService, never()).duplicate(any(), any(), any(), any());
        verify(coursewareAssetService).duplicateAssets(eq(activityId),
                                                       eq(newActivity.getId()),
                                                       eq(CoursewareElementType.ACTIVITY),
                                                       any(DuplicationContext.class));

        assertEquals(newActivity, duplicated);
    }

    @Test
    void duplicateActivity_everythingIsFoundAndDuplicated() {
        CoursewareService coursewareServiceSpy = Mockito.spy(coursewareService);
        ArgumentCaptor<CoursewareElementConfigurationField> configurationFieldCaptor = ArgumentCaptor
                .forClass(CoursewareElementConfigurationField.class);

        final UUID themeId = UUID.randomUUID();
        final UUID scenarioIdOne = UUID.randomUUID();
        UUID pathwayId = UUID.randomUUID();
        List<UUID> pathwayIds = Lists.newArrayList(pathwayId);
        UUID componentId = UUID.randomUUID();
        UUID newComponentId = UUID.randomUUID();

        Activity activity = buildActivity(activityId);
        ActivityConfig activityConfig = buildActivityConfig(activityId);
        ActivityTheme activityTheme = buildActivityTheme(themeId, activityId);
        Activity newActivity = buildActivity(UUID.randomUUID());
        ActivityConfig newActivityConfig = buildActivityConfig(newActivity.getId());
        ActivityTheme newActivityTheme = buildActivityTheme(UUID.randomUUID(), newActivity.getId());
        ArgumentCaptor<ScopeReference> scopeReferenceCaptor = ArgumentCaptor.forClass(ScopeReference.class);

        when(coursewareGateway.persist(any(ScopeReference.class)))
                .thenReturn(Flux.just(new Void[]{}));

        // mocking fetch
        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.just(activityConfig));

        when(coursewareGateway.findRegisteredElements(activity.getStudentScopeURN()))
                .thenReturn(Flux.just(
                        buildScopeReference(activity.getStudentScopeURN(), componentId)
                ));

        when(scenarioService.findScenarioIdsFor(activityId)).thenReturn(Flux.just(scenarioIdOne));
        when(scenarioService.duplicate(eq(scenarioIdOne),
                                       any(UUID.class),
                                       any(),
                                       any())).thenReturn(Mono.just(new Scenario()));

        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.just(activityTheme));
        when(pluginService.findLatestVersion(pluginId, pluginVersion)).thenReturn(Mono.just("version"));
        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.just(pathwayIds));
        when(componentGateway.findComponentIdsByActivity(activityId)).thenReturn(Flux.just(componentId));
        // mocking persistence
        when(activityService.duplicateActivity(eq(activity), eq(creatorId))).thenReturn(Mono.just(newActivity));
        when(activityService.duplicateConfig(any(), eq(newActivity.getId()), any())).thenReturn(Mono.just(
                newActivityConfig));
        when(activityService.duplicateTheme(any(), eq(newActivity.getId()))).thenReturn(Mono.just(newActivityTheme));
        Pathway p = mockPathway();
        Mockito.doReturn(Mono.just(p)).when(coursewareServiceSpy).duplicatePathway(any(), any(), any(), any());
        when(componentService.duplicate(eq(componentId),
                                        any(),
                                        eq(CoursewareElementType.ACTIVITY),
                                        any())).thenAnswer((Answer<Mono<Component>>) invocation -> {
            DuplicationContext context = invocation.getArgument(3);
            context.putIds(componentId, newComponentId);
            return Mono.just(new Component());
        });
        when(coursewareGateway.fetchConfigurationFields(activityId)).thenReturn(Flux.just(
                buildConfigurationField("foo", "bar")
        ));
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        Activity duplicated = coursewareServiceSpy.duplicateActivity(activityId, account, isInSameProject).block();

        verify(scenarioService).duplicate(eq(scenarioIdOne),
                                          eq(newActivity.getId()),
                                          eq(CoursewareElementType.ACTIVITY),
                                          any());
        verify(activityService).duplicateActivity(eq(activity), eq(creatorId));
        verify(activityService).duplicateConfig(eq(config), eq(newActivity.getId()), any());
        verify(activityService).duplicateTheme(eq(themeConfig), eq(newActivity.getId()));
        verify(coursewareServiceSpy).duplicatePathway(eq(pathwayId), eq(newActivity.getId()), eq(creatorId), any());
        verify(componentService).duplicate(eq(componentId),
                                           eq(newActivity.getId()),
                                           eq(CoursewareElementType.ACTIVITY),
                                           any());
        verify(coursewareAssetService).duplicateAssets(eq(activityId),
                                                       eq(newActivity.getId()),
                                                       eq(CoursewareElementType.ACTIVITY),
                                                       any(DuplicationContext.class));

        verify(coursewareGateway).persist(configurationFieldCaptor.capture());
        assertNotNull(duplicated);
        assertNotNull(duplicated.getStudentScopeURN());
        assertNotEquals(activity.getStudentScopeURN(), duplicated.getStudentScopeURN());
        assertEquals(newActivity, duplicated);

        verify(coursewareGateway).findRegisteredElements(activity.getStudentScopeURN());
        verify(coursewareGateway).persist(scopeReferenceCaptor.capture());

        ScopeReference capturedScopeReference = scopeReferenceCaptor.getValue();

        assertNotNull(capturedScopeReference);
        assertEquals(newComponentId, capturedScopeReference.getElementId());
        assertEquals(newActivity.getStudentScopeURN(), capturedScopeReference.getScopeURN());
        CoursewareElementConfigurationField duplicatedField = configurationFieldCaptor.getValue();

        assertNotNull(duplicatedField);
        assertEquals(duplicated.getId(), duplicatedField.getElementId());
        assertEquals("foo", duplicatedField.getFieldName());
        assertEquals("bar", duplicatedField.getFieldValue());
    }

    @Test
    void duplicateActivity_withParentPathway() {
        Activity activity = buildActivity(activityId);
        UUID newPathwayId = UUID.randomUUID();
        Activity newActivity = buildActivity(UUID.randomUUID());
        // mocking fetch
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(activityId)).thenReturn(Flux.empty());
        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());
        when(pluginService.findLatestVersion(pluginId, pluginVersion)).thenReturn(Mono.just("version"));
        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.empty());
        when(componentGateway.findComponentIdsByActivity(activityId)).thenReturn(Flux.empty());
        // mocking persistence
        when(activityService.duplicateActivity(eq(activity), eq(creatorId))).thenReturn(Mono.just(newActivity));
        when(activityService.saveRelationship(any(), any())).thenReturn(Flux.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        Activity duplicated = coursewareService.duplicateActivity(activityId,
                                                                  newPathwayId,
                                                                  creatorId,
                                                                  duplicationContext
                                                                          .setNewRootElementId(rootElementId)).block();

        verify(activityService).saveRelationship(eq(newActivity.getId()), eq(newPathwayId));
        assertEquals(newActivity, duplicated);
        verify(coursewareAssetService).duplicateAssets(eq(activityId),
                                                       eq(newActivity.getId()),
                                                       eq(CoursewareElementType.ACTIVITY),
                                                       eq(duplicationContext));
    }

    @Test
    void duplicateActivity_ContextIsPopulated() {
        CoursewareService coursewareServiceSpy = Mockito.spy(coursewareService);

        Activity activity = buildActivity(activityId);
        Activity newActivity = buildActivity(UUID.randomUUID());
        UUID pathwayId = UUID.randomUUID();
        List<UUID> pathwayIds = Lists.newArrayList(pathwayId);
        UUID componentId = UUID.randomUUID();
        ActivityConfig activityConfig = buildActivityConfig(activityId);
        UUID scenarioIdOne = UUID.randomUUID();
        UUID scenarioIdTwo = UUID.randomUUID();
        duplicationContext.setNewRootElementId(rootElementId);
        // mocking fetch
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.just(activityConfig));
        when(scenarioService.findScenarioIdsFor(activityId)).thenReturn(Flux.just(scenarioIdOne, scenarioIdTwo));
        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());
        when(pluginService.findLatestVersion(pluginId, pluginVersion)).thenReturn(Mono.just("version"));
        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.just(pathwayIds));
        when(componentGateway.findComponentIdsByActivity(activityId)).thenReturn(Flux.just(componentId));
        // mocking persistence
        when(activityService.duplicateActivity(eq(activity), eq(creatorId))).thenReturn(Mono.just(newActivity));
        Pathway p = mockPathway();
        Mockito.doReturn(Mono.just(p)).when(coursewareServiceSpy).duplicatePathway(any(), any(), any(), any());
        when(componentService.duplicate(eq(componentId), any(), eq(CoursewareElementType.ACTIVITY), any())).thenReturn(
                Mono.just(new Component()));
        when(activityService.duplicateConfig(any(),
                                             eq(newActivity.getId()),
                                             any())).thenReturn(Mono.just(new ActivityConfig()));
        when(documentItemLinkService.duplicateLinks(any(), any())).thenReturn(Flux.just(
                buildTag(newActivity.getId(), CoursewareElementType.ACTIVITY, DOCUMENT_ID, ITEM_A_ID)
        ));
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        coursewareServiceSpy.duplicateActivity(activityId, creatorId, duplicationContext).block();
        assertEquals(2, duplicationContext.getIdsMap().size());
        assertEquals(newActivity.getStudentScopeURN(),
                     duplicationContext.getIdsMap().get(activity.getStudentScopeURN()));
        assertEquals(newActivity.getId(), duplicationContext.getIdsMap().get(activityId));
        assertEquals(2, duplicationContext.getScenarios().size());
        assertEquals(scenarioIdOne, duplicationContext.getScenarios().get(0).getScenarioId());
        assertEquals(newActivity.getId(), duplicationContext.getScenarios().get(0).getParentId());
        assertEquals(CoursewareElementType.ACTIVITY, duplicationContext.getScenarios().get(0).getParentType());
        assertEquals(scenarioIdTwo, duplicationContext.getScenarios().get(1).getScenarioId());
        assertEquals(newActivity.getId(), duplicationContext.getScenarios().get(1).getParentId());
        assertEquals(CoursewareElementType.ACTIVITY, duplicationContext.getScenarios().get(1).getParentType());
        verify(coursewareServiceSpy).duplicatePathway(eq(pathwayId), eq(newActivity.getId()), eq(creatorId),
                                                      argThat(c -> c.getIdsMap().containsKey(activityId)));
        verify(componentService).duplicate(eq(componentId), eq(newActivity.getId()), eq(CoursewareElementType.ACTIVITY),
                                           argThat(c -> c.getIdsMap().containsKey(activityId)));
        verify(activityService).duplicateConfig(eq(config), eq(newActivity.getId()),
                                                argThat(c -> c.getIdsMap().containsKey(activityId)));
        verify(documentItemLinkService).duplicateLinks(activityId, newActivity.getId());
        verify(coursewareAssetService).duplicateAssets(eq(activityId),
                                                       eq(newActivity.getId()),
                                                       eq(CoursewareElementType.ACTIVITY),
                                                       eq(duplicationContext));
    }

    @Test
    void duplicateActivity_ScenarioNotFound() {
        Activity activity = buildActivity(activityId);
        Activity newActivity = buildActivity(UUID.randomUUID());
        UUID scenarioIdOne = UUID.randomUUID();
        UUID scenarioIdTwo = UUID.randomUUID();
        when(scenarioService.findScenarioIdsFor(activityId)).thenReturn(Flux.just(scenarioIdOne, scenarioIdTwo));

        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.empty());
        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());
        when(pluginService.findLatestVersion(pluginId, pluginVersion)).thenReturn(Mono.just("version"));
        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.empty());
        when(componentGateway.findComponentIdsByActivity(activityId)).thenReturn(Flux.empty());
        // mocking persistence
        when(activityService.duplicateActivity(eq(activity), eq(creatorId))).thenReturn(Mono.just(newActivity));
        when(activityService.saveRelationship(any(), any())).thenReturn(Flux.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        TestPublisher<Scenario> scenarioDuplicatePublisher = TestPublisher.create();
        scenarioDuplicatePublisher.error(new ScenarioNotFoundException(scenarioIdOne));
        when(scenarioService.duplicate(eq(scenarioIdOne), any(UUID.class), any(), any())).thenReturn(
                scenarioDuplicatePublisher.mono());

        assertThrows(ScenarioNotFoundException.class,
                     () -> coursewareService.duplicateActivity(activityId, account, isInSameProject).block());
    }

    @Test
    void duplicateActivity_withPathway_duplicateScenarios() {
        CoursewareService coursewareServiceSpy = Mockito.spy(coursewareService);

        final UUID scenarioIdOne = UUID.randomUUID();
        Activity activity = buildActivity(activityId);
        Activity newActivity = buildActivity(UUID.randomUUID());

        // mocking fetch
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(activityId)).thenReturn(Flux.just(scenarioIdOne));
        when(scenarioService.duplicate(eq(scenarioIdOne),
                                       any(UUID.class),
                                       any(),
                                       any())).thenReturn(Mono.just(new Scenario()));
        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());
        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.empty());
        when(componentGateway.findComponentIdsByActivity(activityId)).thenReturn(Flux.empty());
        // mocking persistence
        when(activityService.duplicateActivity(eq(activity), eq(creatorId))).thenReturn(Mono.just(newActivity));
        when(activityService.saveRelationship(newActivity.getId(), pathwayId)).thenReturn(Flux.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(rootElementId));

        Activity duplicated = coursewareServiceSpy.duplicateActivity(activityId, pathwayId, account, newDuplicateFlow).block();

        verify(scenarioService).duplicate(eq(scenarioIdOne),
                                          eq(newActivity.getId()),
                                          eq(CoursewareElementType.ACTIVITY),
                                          any());
        verify(activityService).duplicateActivity(eq(activity), eq(creatorId));
        assertEquals(newActivity, duplicated);
    }

    @Test
    void duplicateActivity_withAnnotations() {
        Activity activity = buildActivity(activityId);
        UUID newPathwayId = UUID.randomUUID();
        UUID parentActivityId = UUID.randomUUID();
        Activity newActivity = buildActivity(UUID.randomUUID());
        // mocking fetch
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(parentActivityId));
        when(activityService.findParentPathwayId(parentActivityId)).thenReturn(Mono.empty());
        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(activityId)).thenReturn(Flux.empty());
        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());
        when(pluginService.findLatestVersion(pluginId, pluginVersion)).thenReturn(Mono.just("version"));
        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.empty());
        when(componentGateway.findComponentIdsByActivity(activityId)).thenReturn(Flux.empty());
        // mocking persistence
        when(activityService.duplicateActivity(eq(activity), eq(creatorId))).thenReturn(Mono.just(newActivity));
        when(activityService.saveRelationship(any(), any())).thenReturn(Flux.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        UUID annotationId1 = UUID.randomUUID();
        UUID annotationId2 = UUID.randomUUID();
        when(annotationService.findIdsByElement(parentActivityId, activityId)).thenReturn(Flux.just(annotationId1,
                                                                                                    annotationId2));
        when(annotationService.duplicate(any(), any(), any(), any())).thenReturn(Mono.just(new CoursewareAnnotation()));

        Activity result = coursewareService.duplicateActivity(activityId, newPathwayId, creatorId, duplicationContext
                .setNewRootElementId(rootElementId)).block();

        assertNotNull(result);
        verify(annotationService).duplicate(eq(rootElementId),
                                            eq(result.getId()),
                                            eq(duplicationContext),
                                            eq(annotationId1));
        verify(annotationService).duplicate(eq(rootElementId),
                                            eq(result.getId()),
                                            eq(duplicationContext),
                                            eq(annotationId2));
        verify(coursewareAssetService).duplicateAssets(eq(activityId),
                                                       eq(newActivity.getId()),
                                                       eq(CoursewareElementType.ACTIVITY),
                                                       eq(duplicationContext));
    }

    @Test
    void duplicateActivity_withEvaluable() {
        Activity activity = buildActivity(activityId);
        UUID newPathwayId = UUID.randomUUID();
        UUID parentActivityId = UUID.randomUUID();
        Activity newActivity = buildActivity(UUID.randomUUID());
        // mocking fetch
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(parentActivityId));
        when(activityService.findParentPathwayId(parentActivityId)).thenReturn(Mono.empty());
        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(activityId)).thenReturn(Flux.empty());
        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());
        when(pluginService.findLatestVersion(pluginId, pluginVersion)).thenReturn(Mono.just("version"));
        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.empty());
        when(componentGateway.findComponentIdsByActivity(activityId)).thenReturn(Flux.empty());
        // mocking persistence
        when(activityService.duplicateActivity(eq(activity), eq(creatorId))).thenReturn(Mono.just(newActivity));
        when(activityService.saveRelationship(any(), any())).thenReturn(Flux.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());


        Activity result = coursewareService.duplicateActivity(activityId, newPathwayId, creatorId, duplicationContext
                .setNewRootElementId(rootElementId)).block();

        assertNotNull(result);
        verify(coursewareAssetService).duplicateAssets(eq(activityId),
                                                       eq(newActivity.getId()),
                                                       eq(CoursewareElementType.ACTIVITY),
                                                       eq(duplicationContext));
    }

    @Test
    void duplicateActivity_withElementThemeAssociation() {
        Activity activity = buildActivity(activityId);
        UUID newPathwayId = UUID.randomUUID();
        UUID parentActivityId = UUID.randomUUID();
        Activity newActivity = buildActivity(UUID.randomUUID());
        // mocking fetch
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(parentActivityId));
        when(activityService.findParentPathwayId(parentActivityId)).thenReturn(Mono.empty());
        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(activityId)).thenReturn(Flux.empty());
        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());
        when(pluginService.findLatestVersion(pluginId, pluginVersion)).thenReturn(Mono.just("version"));
        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.empty());
        when(componentGateway.findComponentIdsByActivity(activityId)).thenReturn(Flux.empty());
        // mocking persistence
        when(activityService.duplicateActivity(eq(activity), eq(creatorId))).thenReturn(Mono.just(newActivity));
        when(activityService.saveRelationship(any(), any())).thenReturn(Flux.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        UUID themeId = UUID.randomUUID();
        when(themeService.fetchThemeByElementId(any())).thenReturn(Mono.just(new ThemePayload()
                                                                                     .setId(themeId)
                                                                                     .setThemeVariants(new ArrayList<>())));

        Activity result = coursewareService.duplicateActivity(activityId, newPathwayId, creatorId, duplicationContext
                .setNewRootElementId(rootElementId)).block();

        assertNotNull(result);
        verify(themeService).fetchThemeByElementId(eq(activityId));
        verify(themeService).saveThemeByElement(eq(themeId),
                                                eq(newActivity.getId()),
                                                eq(CoursewareElementType.ACTIVITY));
    }

    @Test
    void duplicateInteractive_noAttachedObjects() {
        UUID newPathwayId = UUID.randomUUID();
        when(interactiveService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.empty());
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        Interactive result = coursewareService.duplicateInteractive(interactiveId, newPathwayId,
                                                                    duplicationContext.setNewRootElementId(rootElementId)).block();

        assertEquals(newInteractive, result);
        verify(interactiveService).duplicateInteractive(eq(oldInteractive));
        verify(coursewareAssetService).duplicateAssets(eq(interactiveId),
                                                       eq(newInteractive.getId()),
                                                       eq(CoursewareElementType.INTERACTIVE),
                                                       eq(duplicationContext));

        assertEquals(2, duplicationContext.getIdsMap().size());
        assertEquals(newInteractive.getStudentScopeURN(),
                     duplicationContext.getIdsMap().get(oldInteractive.getStudentScopeURN()));
        assertEquals(newInteractive.getId(), duplicationContext.getIdsMap().get(interactiveId));
        assertTrue(duplicationContext.getScenarios().isEmpty());
    }

    @Test
    void duplicateInteractive_withConfig() {
        ArgumentCaptor<CoursewareElementConfigurationField> configurationFieldCaptor = ArgumentCaptor
                .forClass(CoursewareElementConfigurationField.class);

        UUID newPathwayId = UUID.randomUUID();
        when(interactiveService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.empty());
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.just(new InteractiveConfig()));
        when(coursewareGateway.fetchConfigurationFields(interactiveId)).thenReturn(Flux.just(
                buildConfigurationField("foo", "bar")
        ));
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        Interactive result = coursewareService.duplicateInteractive(interactiveId, newPathwayId, duplicationContext
                .setNewRootElementId(rootElementId)).block();

        assertNotNull(result);
        assertEquals(newInteractive, result);
        verify(interactiveService).duplicateInteractiveConfig(eq(interactiveId), eq(newInteractive.getId()),
                                                              argThat(c -> c.getIdsMap().containsKey(interactiveId)));
        verify(coursewareGateway).persist(configurationFieldCaptor.capture());
        verify(coursewareAssetService).duplicateAssets(eq(interactiveId),
                                                       eq(newInteractive.getId()),
                                                       eq(CoursewareElementType.INTERACTIVE),
                                                       eq(duplicationContext));

        CoursewareElementConfigurationField duplicatedField = configurationFieldCaptor.getValue();

        assertNotNull(duplicatedField);
        assertEquals(result.getId(), duplicatedField.getElementId());
        assertEquals("foo", duplicatedField.getFieldName());
        assertEquals("bar", duplicatedField.getFieldValue());
    }

    @Test
    void duplicateInteractive_withScenarios() {
        UUID newPathwayId = UUID.randomUUID();
        when(interactiveService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.empty());
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        UUID scenarioId1 = UUID.randomUUID();
        UUID scenarioId2 = UUID.randomUUID();
        duplicationContext.setNewRootElementId(rootElementId);
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.just(scenarioId1, scenarioId2));

        Interactive result = coursewareService.duplicateInteractive(interactiveId,
                                                                    newPathwayId,
                                                                    duplicationContext).block();

        assertNotNull(result);
        assertEquals(2, duplicationContext.getScenarios().size());
        assertEquals(scenarioId1, duplicationContext.getScenarios().get(0).getScenarioId());
        assertEquals(result.getId(), duplicationContext.getScenarios().get(0).getParentId());
        assertEquals(CoursewareElementType.INTERACTIVE, duplicationContext.getScenarios().get(0).getParentType());
        assertEquals(scenarioId2, duplicationContext.getScenarios().get(1).getScenarioId());
        assertEquals(result.getId(), duplicationContext.getScenarios().get(1).getParentId());
        assertEquals(CoursewareElementType.INTERACTIVE, duplicationContext.getScenarios().get(1).getParentType());

        verify(coursewareAssetService).duplicateAssets(eq(interactiveId),
                                                       eq(newInteractive.getId()),
                                                       eq(CoursewareElementType.INTERACTIVE),
                                                       eq(duplicationContext));
    }

    @Test
    void duplicateInteractive_withComponents() {
        UUID newPathwayId = UUID.randomUUID();
        UUID newComponentIdOne = UUID.randomUUID();
        UUID newComponentIdTwo = UUID.randomUUID();

        ArgumentCaptor<ScopeReference> newScopeReferenceCaptor = ArgumentCaptor.forClass(ScopeReference.class);

        when(coursewareGateway.persist(any(ScopeReference.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(interactiveService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        UUID componentId1 = UUID.randomUUID();
        UUID componentId2 = UUID.randomUUID();
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.just(componentId1, componentId2));
        // mock context is set from componentService
        when(componentService.duplicate(any(),
                                        any(),
                                        any(),
                                        any())).thenAnswer((Answer<Mono<Component>>) invocation -> {
            DuplicationContext context = invocation.getArgument(3);
            context.putIds(componentId1, newComponentIdOne);
            context.putIds(componentId2, newComponentIdTwo);
            return Mono.just(new Component());
        });

        when(coursewareGateway.findRegisteredElements(oldInteractive.getStudentScopeURN()))
                .thenReturn(Flux.just(
                        buildScopeReference(oldInteractive.getStudentScopeURN(), componentId1),
                        buildScopeReference(oldInteractive.getStudentScopeURN(), componentId2)
                ));

        Interactive result = coursewareService.duplicateInteractive(interactiveId, newPathwayId, accountId).block();

        assertNotNull(result);
        verify(componentService).duplicate(eq(componentId1), eq(result.getId()), eq(CoursewareElementType.INTERACTIVE),
                                           argThat(c -> c.getIdsMap().containsKey(interactiveId)));
        verify(componentService).duplicate(eq(componentId2), eq(result.getId()), eq(CoursewareElementType.INTERACTIVE),
                                           argThat(c -> c.getIdsMap().containsKey(interactiveId)));

        verify(coursewareGateway).findRegisteredElements(oldInteractive.getStudentScopeURN());
        verify(coursewareGateway, times(2)).persist(newScopeReferenceCaptor.capture());
        verify(coursewareAssetService).duplicateAssets(eq(interactiveId),
                                                       eq(newInteractive.getId()),
                                                       eq(CoursewareElementType.INTERACTIVE),
                                                       any(DuplicationContext.class));

        ScopeReference capturedOne = newScopeReferenceCaptor.getAllValues().get(0);
        ScopeReference capturedTwo = newScopeReferenceCaptor.getAllValues().get(1);

        assertNotNull(capturedOne);
        assertEquals(newComponentIdOne, capturedOne.getElementId());
        assertEquals(newInteractive.getStudentScopeURN(), capturedOne.getScopeURN());
        assertNotNull(capturedTwo);
        assertEquals(newComponentIdTwo, capturedTwo.getElementId());
        assertEquals(newInteractive.getStudentScopeURN(), capturedTwo.getScopeURN());
    }

    @Test
    void duplicateInteractive_withFeedbacks() {
        UUID newPathwayId = UUID.randomUUID();
        when(interactiveService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.empty());
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        UUID feedbackId1 = UUID.randomUUID();
        UUID feedbackId2 = UUID.randomUUID();
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.just(Lists.newArrayList(feedbackId1,
                                                                                                          feedbackId2)));
        when(feedbackService.duplicate(any(), any(), any())).thenReturn(Mono.just(new Feedback()));

        Interactive result = coursewareService.duplicateInteractive(interactiveId,
                                                                    newPathwayId,
                                                                    new DuplicationContext()
                                                                            .setNewRootElementId(rootElementId)).block();

        assertNotNull(result);
        verify(feedbackService).duplicate(eq(feedbackId1),
                                          eq(result.getId()),
                                          argThat(c -> c.getIdsMap().containsKey(interactiveId)));
        verify(feedbackService).duplicate(eq(feedbackId2),
                                          eq(result.getId()),
                                          argThat(c -> c.getIdsMap().containsKey(interactiveId)));
    }

    @Test
    void duplicateInteractive_withLinkedDocumentItems() {
        UUID newPathwayId = UUID.randomUUID();
        when(interactiveService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.empty());
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());
        when(documentItemLinkService.duplicateLinks(any(), any())).thenReturn(Flux.just(
                buildTag(newInteractive.getId(), CoursewareElementType.INTERACTIVE, DOCUMENT_ID, ITEM_A_ID)
        ));
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        Interactive result = coursewareService.duplicateInteractive(interactiveId,
                                                                    newPathwayId,
                                                duplicationContext.setNewRootElementId(rootElementId)).block();

        assertNotNull(result);

        verify(documentItemLinkService).duplicateLinks(interactiveId, newInteractive.getId());
        verify(coursewareAssetService).duplicateAssets(eq(interactiveId),
                                                       eq(newInteractive.getId()),
                                                       eq(CoursewareElementType.INTERACTIVE),
                                                       eq(duplicationContext));
    }

    @Test
    void duplicateInteractive_withAnnotations() {
        UUID newPathwayId = UUID.randomUUID();
        when(interactiveService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.empty());
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        UUID annotationId1 = UUID.randomUUID();
        UUID annotationId2 = UUID.randomUUID();
        when(annotationService.findIdsByElement(activityId, interactiveId)).thenReturn(Flux.just(annotationId1,
                                                                                                 annotationId2));
        when(annotationService.duplicate(any(), any(), any(), any())).thenReturn(Mono.just(new CoursewareAnnotation()));

        Interactive result = coursewareService.duplicateInteractive(interactiveId,
                                                    newPathwayId,
                                                    duplicationContext.setNewRootElementId(rootElementId)).block();

        assertNotNull(result);
        verify(annotationService).duplicate(eq(rootElementId),
                                            eq(result.getId()),
                                            argThat(c -> c.getIdsMap().containsKey(interactiveId)),
                                            eq(annotationId1));
        verify(annotationService).duplicate(eq(rootElementId),
                                            eq(result.getId()),
                                            argThat(c -> c.getIdsMap().containsKey(interactiveId)),
                                            eq(annotationId2));
        verify(coursewareAssetService).duplicateAssets(eq(interactiveId),
                                                       eq(newInteractive.getId()),
                                                       eq(CoursewareElementType.INTERACTIVE),
                                                       eq(duplicationContext));
    }

    @Test
    void duplicateInteractive_withEvaluable() {
        UUID newPathwayId = UUID.randomUUID();
        when(interactiveService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.empty());
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        Interactive result = coursewareService.duplicateInteractive(interactiveId,
                                                        newPathwayId,
                                                        duplicationContext.setNewRootElementId(rootElementId)).block();

        assertNotNull(result);
        verify(coursewareAssetService).duplicateAssets(eq(interactiveId),
                                                       eq(newInteractive.getId()),
                                                       eq(CoursewareElementType.INTERACTIVE),
                                                       eq(duplicationContext));
    }

    @Test
    void duplicateInteractive_interactiveNotFound() {
        UUID newPathwayId = UUID.randomUUID();
        TestPublisher<Interactive> interactivePublisher = TestPublisher.create();
        interactivePublisher.error(new InteractiveNotFoundException(interactiveId));
        when(interactiveService.findById(interactiveId)).thenReturn(interactivePublisher.mono());

        assertThrows(InteractiveNotFoundException.class,
                     () -> coursewareService.duplicateInteractive(interactiveId,
                                                                  newPathwayId,
                                                                  new DuplicationContext()).block());
    }

    @Test
    void duplicateInteractive_feedbackNotFound() {
        UUID newPathwayId = UUID.randomUUID();
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.empty());
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());

        UUID feedbackId1 = UUID.randomUUID();
        UUID feedbackId2 = UUID.randomUUID();
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.just(Lists.newArrayList(feedbackId1,
                                                                                                          feedbackId2)));
        TestPublisher<Feedback> error = TestPublisher.create();
        error.error(new FeedbackNotFoundException(feedbackId1));
        when(feedbackService.duplicate(eq(feedbackId1), any(), any())).thenReturn(error.mono());
        when(feedbackService.duplicate(eq(feedbackId2), any(), any())).thenReturn(Mono.just(new Feedback()));

        assertThrows(FeedbackNotFoundException.class,
                     () -> coursewareService.duplicateInteractive(interactiveId,
                                                                  newPathwayId,
                                                                  new DuplicationContext()).block());
    }

    @Test
    void duplicateInteractive_componentNotFound() {
        UUID newPathwayId = UUID.randomUUID();
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.empty());
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());

        UUID componentId1 = UUID.randomUUID();
        UUID componentId2 = UUID.randomUUID();
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.just(componentId1, componentId2));
        TestPublisher<Component> error = TestPublisher.create();
        error.error(new ComponentNotFoundException(componentId1));
        when(componentService.duplicate(eq(componentId1),
                                        any(),
                                        eq(CoursewareElementType.INTERACTIVE),
                                        any())).thenReturn(error.mono());
        when(componentService.duplicate(eq(componentId2),
                                        any(),
                                        eq(CoursewareElementType.INTERACTIVE),
                                        any())).thenReturn(Mono.just(new Component()));

        assertThrows(ComponentNotFoundException.class,
                     () -> coursewareService.duplicateInteractive(interactiveId,
                                                                  newPathwayId,
                                                                  duplicationContext).block());
    }

    @Test
    void duplicateInteractive_scenariosDuplicated() {
        UUID newPathwayId = UUID.randomUUID();
        when(interactiveService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.empty());
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        UUID scenarioId1 = UUID.randomUUID();
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.just(scenarioId1));
        when(scenarioService.duplicate(any(), any(), any(), any())).thenReturn(Mono.just(new Scenario()));

        Interactive result = coursewareService.duplicateInteractive(interactiveId, newPathwayId, accountId).block();

        assertNotNull(result);
        assertEquals(newInteractive, result);
        verify(scenarioService).duplicate(eq(scenarioId1),
                                          eq(newInteractive.getId()),
                                          eq(CoursewareElementType.INTERACTIVE),
                                          any());
        verify(coursewareAssetService).duplicateAssets(eq(interactiveId),
                                                       eq(newInteractive.getId()),
                                                       eq(CoursewareElementType.INTERACTIVE),
                                                       any(DuplicationContext.class));
    }

    @Test
    void duplicateInteractive_withIndex_scenariosDuplicated() {
        UUID newPathwayId = UUID.randomUUID();
        when(interactiveService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId, 1)).thenReturn(Flux.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                                                           eq(newInteractive.getId()),
                                                           any())).thenReturn(Mono.empty());
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        UUID scenarioId1 = UUID.randomUUID();
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.just(scenarioId1));
        when(scenarioService.duplicate(any(), any(), any(), any())).thenReturn(Mono.just(new Scenario()));

        Interactive result = coursewareService.duplicateInteractive(interactiveId, newPathwayId, 1).block();

        assertNotNull(result);
        assertEquals(newInteractive, result);
        verify(scenarioService).duplicate(eq(scenarioId1),
                                          eq(newInteractive.getId()),
                                          eq(CoursewareElementType.INTERACTIVE),
                                          any());
        verify(coursewareAssetService).duplicateAssets(eq(interactiveId),
                                                       eq(newInteractive.getId()),
                                                       eq(CoursewareElementType.INTERACTIVE),
                                                       any(DuplicationContext.class));
    }

    @Test
    void duplicatePathway() {
        String config = "{\"foo\":\"bar\"}";
        when(pathwayService.duplicateConfig(eq(config), any(UUID.class), any(DuplicationContext.class)))
                .thenReturn(Mono.just(new PathwayConfig()));
        when(pathwayService.findLatestConfig(pathwayId)).thenReturn(Mono.just(
                new PathwayConfig()
                        .setId(UUID.randomUUID())
                        .setConfig(config)
                        .setPathwayId(pathwayId)
        ));

        ArgumentCaptor<UUID> configPathwayIdDuplicationCaptor = ArgumentCaptor.forClass(UUID.class);

        CoursewareService coursewareServiceSpy = Mockito.spy(coursewareService);

        UUID newActivityId = UUID.randomUUID();
        Pathway oldPathway = mockPathway(pathwayId);
        Pathway newPathway = mockPathway();
        UUID activityId = UUID.randomUUID();
        UUID interactiveId = UUID.randomUUID();
        List<WalkableChild> list = Lists.newArrayList();
        list.add(new WalkableChild().setElementId(activityId).setElementType(CoursewareElementType.ACTIVITY));
        list.add(new WalkableChild().setElementId(interactiveId).setElementType(CoursewareElementType.INTERACTIVE));

        when(pathwayService.findById(pathwayId)).thenReturn(Mono.just(oldPathway));
        when(pathwayService.duplicatePathway(any(), eq(newActivityId))).thenReturn(Mono.just(newPathway));
        when(pathwayService.getOrderedWalkableChildren(pathwayId)).thenReturn(Mono.just(list));
        Mockito.doReturn(Mono.just(new Activity())).when(coursewareServiceSpy).duplicateActivity(any(),
                                                                                                 any(),
                                                                                                 eq(creatorId),
                                                                                                 any());
        Mockito.doReturn(Mono.just(new Interactive())).when(coursewareServiceSpy).duplicateInteractive(any(),
                                                                                                       any(),
                                                                                                       any(DuplicationContext.class));

        Pathway result = coursewareServiceSpy.duplicatePathway(pathwayId, newActivityId, creatorId, duplicationContext
                .setNewRootElementId(rootElementId)).block();

        assertNotNull(result);
        assertEquals(newPathway, result);
        // This matcher is failing: org.mockito.exceptions.misusing.InvalidUseOfMatchersException
        //verify(coursewareServiceSpy).duplicateActivity(eq(activityId), eq(result.getId()), eq(creatorId), eq(duplicationContext));
        //verify(coursewareServiceSpy).duplicateInteractive(eq(interactiveId), eq(result.getId()), eq(duplicationContext));
        verify(coursewareServiceSpy).duplicateActivity(any(), any(), eq(creatorId), any(DuplicationContext.class));
        verify(coursewareServiceSpy).duplicateInteractive(any(), any(), any(DuplicationContext.class));
        // ^^^ replaced commented lines with these.
        assertEquals(1, duplicationContext.getIdsMap().size());
        assertEquals(newPathway.getId(), duplicationContext.getIdsMap().get(pathwayId));
        assertTrue(duplicationContext.getScenarios().isEmpty());
        verify(coursewareAssetService).duplicateAssets(eq(pathwayId),
                                                       any(UUID.class),
                                                       eq(CoursewareElementType.PATHWAY),
                                                       any(DuplicationContext.class));

        verify(pathwayService).duplicateConfig(eq(config),
                                               configPathwayIdDuplicationCaptor.capture(),
                                               any(DuplicationContext.class));

        UUID newPathwayId = configPathwayIdDuplicationCaptor.getValue();

        assertNotNull(newPathwayId);
        assertEquals(result.getId(), newPathwayId);
    }

    @Test
    void duplicatePathway_noChildren() {
        when(pathwayService.findLatestConfig(pathwayId)).thenReturn(Mono.empty());
        UUID newActivityId = UUID.randomUUID();
        Pathway oldPathway = mockPathway(pathwayId);
        Pathway newPathway = mockPathway();

        when(pathwayService.findById(pathwayId)).thenReturn(Mono.just(oldPathway));
        when(pathwayService.duplicatePathway(any(), eq(newActivityId))).thenReturn(Mono.just(newPathway));
        when(pathwayService.getOrderedWalkableChildren(pathwayId)).thenReturn(Mono.empty());

        Pathway result = coursewareService.duplicatePathway(pathwayId,
                                                            newActivityId,
                                                            creatorId,
                                                            duplicationContext
                                                            .setNewRootElementId(rootElementId)).block();

        assertEquals(newPathway, result);
        verify(pathwayService).duplicatePathway(oldPathway, newActivityId);
        assertTrue(duplicationContext.getIdsMap().containsKey(pathwayId));

        verify(coursewareAssetService).duplicateAssets(eq(pathwayId),
                                                        any(UUID.class),
                                                        eq(CoursewareElementType.PATHWAY),
                                                        eq(duplicationContext));
    }

    @Test
    void duplicatePathway_pathwayNotFound() {
        UUID newActivityId = UUID.randomUUID();
        TestPublisher<Pathway> pathwayPublisher = TestPublisher.create();
        pathwayPublisher.error(new PathwayNotFoundException(pathwayId));
        when(pathwayService.findById(pathwayId)).thenReturn(pathwayPublisher.mono());

        assertThrows(PathwayNotFoundException.class,
                     () -> coursewareService.duplicatePathway(pathwayId,
                                                              newActivityId,
                                                              creatorId,
                                                              duplicationContext).block());
        assertTrue(duplicationContext.getIdsMap().isEmpty());
    }

    @Test
    void getWorkspaceId() {
        UUID topActivityId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        List<CoursewareElement> path = Lists.newArrayList(
                new CoursewareElement(topActivityId, CoursewareElementType.ACTIVITY),
                new CoursewareElement(pathwayId, CoursewareElementType.PATHWAY),
                new CoursewareElement(activityId, CoursewareElementType.ACTIVITY));
        CoursewareService coursewareServiceSpy = Mockito.spy(coursewareService);
        Mockito.doReturn(Mono.just(path)).when(coursewareServiceSpy).getPath(activityId,
                                                                             CoursewareElementType.ACTIVITY);
        when(activityService.findWorkspaceIdByActivity(topActivityId)).thenReturn(Mono.just(workspaceId));

        assertEquals(workspaceId,
                     coursewareServiceSpy.getWorkspaceId(activityId, CoursewareElementType.ACTIVITY).block());
    }

    @Test
    void getWorkspaceId_noWorkspace() {
        UUID topActivityId = UUID.randomUUID();
        List<CoursewareElement> path = Lists.newArrayList(
                new CoursewareElement(topActivityId, CoursewareElementType.ACTIVITY));
        CoursewareService coursewareServiceSpy = Mockito.spy(coursewareService);
        Mockito.doReturn(Mono.just(path)).when(coursewareServiceSpy).getPath(activityId,
                                                                             CoursewareElementType.ACTIVITY);
        when(activityService.findWorkspaceIdByActivity(topActivityId)).thenReturn(Mono.empty());

        assertNull(coursewareServiceSpy.getWorkspaceId(activityId, CoursewareElementType.ACTIVITY).block());
    }

    @Test
    void getWorkspaceId_exception() {
        CoursewareService coursewareServiceSpy = Mockito.spy(coursewareService);
        TestPublisher<Pathway> pathPublisher = TestPublisher.create();
        pathPublisher.error(new ParentActivityNotFoundException(pathwayId));
        Mockito.doReturn(pathPublisher.mono()).when(coursewareServiceSpy).getPath(activityId,
                                                                                  CoursewareElementType.ACTIVITY);

        assertThrows(ParentActivityNotFoundException.class,
                     () -> coursewareServiceSpy.getWorkspaceId(activityId, CoursewareElementType.ACTIVITY).block());
    }

    @Test
    void register_nullStudentScope() {

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                coursewareService.register(null, new Activity(), UUID.randomUUID(), null)
                        .block());

        assertEquals("studentScopeURN is required", e.getMessage());
    }

    @Test
    void register_nullPluginReference() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                coursewareService.register(UUID.randomUUID(), null, UUID.randomUUID(), null)
                        .block());

        assertEquals("plugin reference is required", e.getMessage());
    }

    @Test
    void register_nullElementId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                coursewareService.register(UUID.randomUUID(), new Activity(), null, null)
                        .block());

        assertEquals("elementId is required", e.getMessage());
    }

    @Test
    void register_nullElementType() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                coursewareService.register(UUID.randomUUID(), new Activity(), UUID.randomUUID(), null)
                        .block());

        assertEquals("elementType is required", e.getMessage());
    }

    @Test
    @DisplayName("It should call the gateway and register the element to a student scope")
    void register() {
        when(coursewareGateway.persist(any(ScopeReference.class))).thenReturn(Flux.just(new Void[]{}));

        UUID studentScopeURN = UUID.randomUUID();
        UUID interactiveId = UUID.randomUUID();
        Interactive interactive = new Interactive()
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersion)
                .setId(interactiveId);

        ScopeReference reference = coursewareService.register(studentScopeURN,
                                                              interactive,
                                                              interactive.getId(),
                                                              CoursewareElementType.INTERACTIVE)
                .block();

        assertNotNull(reference);

        assertEquals(interactiveId, reference.getElementId());
        assertEquals(studentScopeURN, reference.getScopeURN());
        assertEquals(CoursewareElementType.INTERACTIVE, reference.getElementType());
        assertEquals(pluginVersion, reference.getPluginVersion());
        assertEquals(pluginId, reference.getPluginId());

        verify(coursewareGateway).persist(any(ScopeReference.class));
    }

    @Test
    void deRegister_nullStudentScope() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                coursewareService.deRegister(null, null)
                        .blockLast());

        assertEquals("studentScopeURN is required", e.getMessage());
    }

    @Test
    void deRegister_nullElementId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                coursewareService.deRegister(UUID.randomUUID(), null)
                        .blockLast());

        assertEquals("elementId is required", e.getMessage());
    }

    @Test
    @DisplayName("It should call the gateway to de-register the element from a student scope")
    void deRegister() {

        when(coursewareGateway.delete(any(ScopeReference.class))).thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<ScopeReference> scopeReferenceCaptor = ArgumentCaptor.forClass(ScopeReference.class);

        UUID studentScopeURN = UUID.randomUUID();
        UUID interactiveId = UUID.randomUUID();

        coursewareService.deRegister(studentScopeURN, interactiveId).blockFirst();

        verify(coursewareGateway).delete(scopeReferenceCaptor.capture());

        ScopeReference reference = scopeReferenceCaptor.getValue();

        assertNotNull(reference);
        assertEquals(studentScopeURN, reference.getScopeURN());
        assertEquals(interactiveId, reference.getElementId());
    }

    @Test
    @DisplayName("It should throw an fault exception if the supplied argument is null")
    void findElementByStudentScope_nullStudentScope() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                coursewareService.findElementByStudentScope(null).block());

        assertEquals("studentScopeURN is required", e.getMessage());
    }

    @Test
    @DisplayName("It should throw a not found exception when the element is not found")
    void findElementByStudentScope_notFound() {
        UUID studentScopeURN = UUID.randomUUID();

        when(coursewareGateway.findElementBy(studentScopeURN)).thenReturn(Mono.empty());

        CoursewareElementNotFoundFault e = assertThrows(CoursewareElementNotFoundFault.class, () ->
                coursewareService.findElementByStudentScope(studentScopeURN).block());

        assertTrue(e.getMessage().contains("courseware element not found for studentScopeURN"));
    }

    @Test
    void findPluginReference_nullElementId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                coursewareService.findPluginReference(null, null).block());

        assertEquals("elementId is required", e.getMessage());
    }

    @Test
    void findPluginReference_nullElementType() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                coursewareService.findPluginReference(UUID.randomUUID(), null).block());

        assertEquals("elementType is required", e.getMessage());
    }

    @Test
    void findPluginReference_invalidType() {
        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () ->
                coursewareService.findPluginReference(UUID.randomUUID(), CoursewareElementType.PATHWAY).block());

        assertEquals("PATHWAY not a plugin reference type", e.getMessage());
    }

    @Test
    void findPluginReference_anyError() {
        TestPublisher<Activity> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("ops"));

        UUID elementId = UUID.randomUUID();

        when(activityService.findById(elementId)).thenReturn(publisher.mono());

        PluginReferenceNotFoundFault e = assertThrows(PluginReferenceNotFoundFault.class, () ->
                coursewareService.findPluginReference(elementId, CoursewareElementType.ACTIVITY).block());

        assertEquals("ops", e.getMessage());
    }

    @Test
    void findPluginReference_activity() {
        UUID elementId = UUID.randomUUID();

        when(activityService.findById(elementId)).thenReturn(Mono.just(new Activity()));

        PluginReference ref = coursewareService.findPluginReference(elementId, CoursewareElementType.ACTIVITY).block();

        assertNotNull(ref);
    }

    @Test
    void findPluginReference_component() {
        UUID elementId = UUID.randomUUID();

        when(componentService.findById(elementId)).thenReturn(Mono.just(new Component()));

        PluginReference ref = coursewareService.findPluginReference(elementId, CoursewareElementType.COMPONENT).block();

        assertNotNull(ref);
    }

    @Test
    void findPluginReference_feedback() {
        UUID elementId = UUID.randomUUID();

        when(feedbackService.findById(elementId)).thenReturn(Mono.just(new Feedback()));

        PluginReference ref = coursewareService.findPluginReference(elementId, CoursewareElementType.FEEDBACK).block();

        assertNotNull(ref);
    }

    @Test
    void findPluginReference_interactive() {
        UUID elementId = UUID.randomUUID();

        when(interactiveService.findById(elementId)).thenReturn(Mono.just(new Interactive()));

        PluginReference ref = coursewareService.findPluginReference(elementId,
                                                                    CoursewareElementType.INTERACTIVE).block();

        assertNotNull(ref);
    }

    @Test
    void saveConfigurationFields_invalidJson() {
        assertThrows(IllegalStateFault.class,
                     () -> coursewareService.saveConfigurationFields(activityId, "{invalid json}")
                             .blockLast());
    }

    @Test
    void saveConfigurationFields() {
        when(coursewareGateway.persist(any(CoursewareElementConfigurationField.class)))
                .thenReturn(Flux.just(new Void[]{}));

        String json = "{\"foo\":\"bar\", \"foos\":[\"bar1\",\"bar2\"]}";

        ArgumentCaptor<CoursewareElementConfigurationField> captor = ArgumentCaptor.forClass(
                CoursewareElementConfigurationField.class);

        coursewareService.saveConfigurationFields(activityId, json).blockLast();

        verify(coursewareGateway, times(2)).persist(captor.capture());

        List<CoursewareElementConfigurationField> saved = captor.getAllValues();

        assertNotNull(saved);
        assertEquals(2, saved.size());

        CoursewareElementConfigurationField first = saved.get(0);

        assertNotNull(first);
        assertEquals(activityId, first.getElementId());
        assertEquals("foo", first.getFieldName());
        assertEquals("bar", first.getFieldValue());

        CoursewareElementConfigurationField second = saved.get(1);
        assertEquals(activityId, second.getElementId());
        assertEquals("foos", second.getFieldName());
        assertEquals("[\"bar1\",\"bar2\"]", second.getFieldValue());

    }

    @Test
    void register_emptyList() {
        List<ScopeReference> registered = coursewareService.register(new ArrayList<>())
                .collectList()
                .block();

        assertNotNull(registered);
        assertTrue(registered.isEmpty());
    }

    @Test
    void register_listHasElements() {

        List<ScopeReference> toRegister = Lists.newArrayList(
                buildScopeReference(UUID.randomUUID(), UUID.randomUUID()),
                buildScopeReference(UUID.randomUUID(), UUID.randomUUID())
        );

        when(coursewareGateway.persist(any(ScopeReference.class))).thenReturn(Flux.just(new Void[]{}));

        List<ScopeReference> registered = coursewareService.register(toRegister)
                .collectList()
                .block();

        assertNotNull(registered);
        assertEquals(2, registered.size());
        assertEquals(toRegister, registered);
    }

    @Test
    void findCoursewareElementAncestry_notFound() {
        UUID elementId = UUID.randomUUID();

        when(activityService.findById(elementId)).thenReturn(Mono.error(new ActivityNotFoundException(elementId)));
        when(pathwayService.findById(elementId)).thenReturn(Mono.error(new PathwayNotFoundException(elementId)));
        when(interactiveService.findById(elementId)).thenReturn(Mono.error(new InteractiveNotFoundException(elementId)));
        when(componentService.findById(elementId)).thenReturn(Mono.error(new ComponentNotFoundException(elementId)));
        when(scenarioService.findById(elementId)).thenReturn(Mono.error(new ScenarioNotFoundException(elementId)));
        when(feedbackService.findById(elementId)).thenReturn(Mono.error(new FeedbackNotFoundException(elementId)));

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                                              () -> coursewareService.findCoursewareElementAncestry(elementId)
                                                      .block());

        assertEquals(String.format("type not found for element %s", elementId), f.getMessage());
    }

    @Test
    void findCoursewareElementAncestry() {
        UUID elementId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        when(activityService.findById(elementId)).thenReturn(Mono.error(new ActivityNotFoundException(elementId)));
        when(pathwayService.findById(elementId)).thenReturn(Mono.error(new PathwayNotFoundException(elementId)));
        when(interactiveService.findById(elementId)).thenReturn(Mono.error(new InteractiveNotFoundException(elementId)));
        when(componentService.findById(elementId)).thenReturn(Mono.just(new Component()
                                                                                .setId(elementId)));
        when(scenarioService.findById(elementId)).thenReturn(Mono.error(new ScenarioNotFoundException(elementId)));
        when(feedbackService.findById(elementId)).thenReturn(Mono.error(new FeedbackNotFoundException(elementId)));

        when(componentService.findParentFor(elementId)).thenReturn(Mono.just(new ParentByComponent()
                                                                                     .setComponentId(elementId)
                                                                                     .setParentId(parentId)
                                                                                     .setParentType(
                                                                                             CoursewareElementType.ACTIVITY)));

        when(activityService.findParentPathwayId(parentId)).thenReturn(Mono.empty());

        CoursewareElementAncestry found = coursewareService.findCoursewareElementAncestry(elementId)
                .block();

        assertNotNull(found);
        assertEquals(elementId, found.getElementId());
        assertEquals(CoursewareElementType.COMPONENT, found.getType());
        assertNotNull(found.getAncestry());

        verify(componentService).findParentFor(elementId);
    }

    @Test
    void findCoursewareElementAncestry_withElementType_hasAncestry() {
        final CoursewareElement element = CoursewareElement.from(UUID.randomUUID(), CoursewareElementType.INTERACTIVE);
        final UUID parentPathwayId = UUID.randomUUID();
        final UUID rootActivityId = UUID.randomUUID();

        when(interactiveService.findParentPathwayId(element.getElementId())).thenReturn(Mono.just(parentPathwayId));
        when(pathwayService.findParentActivityId(parentPathwayId)).thenReturn(Mono.just(rootActivityId));
        when(activityService.findParentPathwayId(rootActivityId)).thenReturn(Mono.empty());

        CoursewareElementAncestry found = coursewareService.findCoursewareElementAncestry(element)
                .block();

        assertNotNull(found);
        assertEquals(element.getElementId(), found.getElementId());
        assertEquals(element.getElementType(), found.getType());
        assertNotNull(found.getAncestry());
        assertFalse(found.getAncestry().isEmpty());

        List<CoursewareElement> ancestry = found.getAncestry();
        assertEquals(CoursewareElement.from(parentPathwayId, CoursewareElementType.PATHWAY), ancestry.get(0));
        assertEquals(CoursewareElement.from(rootActivityId, CoursewareElementType.ACTIVITY), ancestry.get(1));

        verify(interactiveService).findParentPathwayId(element.getElementId());
        verify(pathwayService).findParentActivityId(parentPathwayId);
        verify(activityService).findParentPathwayId(rootActivityId);
    }

    @Test
    void findCoursewareElementAncestry_withElementType_noAncestry() {
        final CoursewareElement element = CoursewareElement.from(UUID.randomUUID(), CoursewareElementType.INTERACTIVE);


        when(interactiveService.findParentPathwayId(element.getElementId())).thenReturn(Mono.empty());

        CoursewareElementAncestry found = coursewareService.findCoursewareElementAncestry(element)
                .block();

        assertNotNull(found);
        assertEquals(element.getElementId(), found.getElementId());
        assertEquals(element.getElementType(), found.getType());
        assertNotNull(found.getAncestry());
        assertTrue(found.getAncestry().isEmpty());

        verify(interactiveService).findParentPathwayId(element.getElementId());
    }

    @Test
    void findCoursewareElementById() {
        UUID elementId = UUID.randomUUID();

        when(activityService.findById(elementId)).thenReturn(Mono.error(new ActivityNotFoundException(elementId)));
        when(pathwayService.findById(elementId)).thenReturn(Mono.error(new PathwayNotFoundException(elementId)));
        when(interactiveService.findById(elementId)).thenReturn(Mono.error(new InteractiveNotFoundException(elementId)));
        when(componentService.findById(elementId)).thenReturn(Mono.just(new Component()
                                                                                .setId(elementId)));
        when(scenarioService.findById(elementId)).thenReturn(Mono.error(new ScenarioNotFoundException(elementId)));
        when(feedbackService.findById(elementId)).thenReturn(Mono.error(new FeedbackNotFoundException(elementId)));

        CoursewareElement found = coursewareService.findCoursewareElementById(elementId)
                .block();

        assertNotNull(found);
        assertEquals(elementId, found.getElementId());
        assertEquals(CoursewareElementType.COMPONENT, found.getElementType());
    }

    @Test
    void findCoursewareElementById_notFound() {
        UUID elementId = UUID.randomUUID();

        when(activityService.findById(elementId)).thenReturn(Mono.error(new ActivityNotFoundException(elementId)));
        when(pathwayService.findById(elementId)).thenReturn(Mono.error(new PathwayNotFoundException(elementId)));
        when(interactiveService.findById(elementId)).thenReturn(Mono.error(new InteractiveNotFoundException(elementId)));
        when(componentService.findById(elementId)).thenReturn(Mono.error(new ComponentNotFoundException(elementId)));
        when(scenarioService.findById(elementId)).thenReturn(Mono.error(new ScenarioNotFoundException(elementId)));
        when(feedbackService.findById(elementId)).thenReturn(Mono.error(new FeedbackNotFoundException(elementId)));

        NotFoundFault f = assertThrows(NotFoundFault.class, () -> coursewareService.findCoursewareElementById(elementId)
                .block());

        assertEquals(String.format("type not found for element %s", elementId), f.getMessage());
    }

    private Activity buildActivity(UUID activityId) {
        return new Activity()
                .setId(activityId)
                .setPluginId(pluginId)
                .setCreatorId(creatorId)
                .setPluginVersionExpr(pluginVersion)
                .setStudentScopeURN(UUID.randomUUID());
    }

    private ActivityConfig buildActivityConfig(UUID activityId) {
        return new ActivityConfig()
                .setActivityId(activityId)
                .setConfig(config)
                .setId(UUID.randomUUID());
    }

    private ActivityTheme buildActivityTheme(UUID themeId, UUID activityId) {
        return new ActivityTheme()
                .setActivityId(activityId)
                .setConfig(themeConfig)
                .setId(themeId);
    }

    @Test
    public void getWorkspaceIdByProject() {
        UUID topActivityId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        List<CoursewareElement> path = Lists.newArrayList(
                new CoursewareElement(topActivityId, CoursewareElementType.ACTIVITY),
                new CoursewareElement(pathwayId, CoursewareElementType.PATHWAY),
                new CoursewareElement(activityId, CoursewareElementType.ACTIVITY));
        CoursewareService coursewareServiceSpy = Mockito.spy(coursewareService);
        Mockito.doReturn(Mono.just(path)).when(coursewareServiceSpy).getPath(activityId,
                                                                             CoursewareElementType.ACTIVITY);
        when(activityService.findProjectIdByActivity(topActivityId)).thenReturn(Mono.just(new ProjectActivity().setProjectId(
                projectId)));
        when(projectService.findById(projectId)).thenReturn(Mono.just(new Project().setWorkspaceId(workspaceId)));

        assertEquals(workspaceId,
                     coursewareServiceSpy.getWorkspaceIdByProject(activityId, CoursewareElementType.ACTIVITY).block());
    }

    @Test
    public void fetchSourcesByScopeUrn_success() {
        List<ConfigurationField> configFields = new ArrayList<>();
        ConfigurationField configurationField = new ConfigurationField()
                .setFieldName("title")
                .setFieldValue("titleValue");
        configFields.add(configurationField);
        when(coursewareGateway.findRegisteredElements(scopeId)).thenReturn(Flux.just(new ScopeReference()
                                                                                             .setElementId(elementId)
                                                                                             .setElementType(elementType)
                                                                                             .setPluginId(pluginId)
                                                                                             .setPluginVersion(
                                                                                                     pluginVersion)
                                                                                             .setScopeURN(scopeId)));
        when(coursewareGateway.fetchConfigurationField(any(), any())).thenReturn(Mono.just(configurationField));
        when(pluginGateway.fetchPluginManifestByIdVersion(any(), any())).thenReturn(Mono.just(new PluginManifest()
                                                                                                      .setPluginId(
                                                                                                              pluginId)
                                                                                                      .setConfigurationSchema(
                                                                                                              configSchema)
                                                                                                      .setVersion(
                                                                                                              "1.0.1")));
        when(pluginService.findLatestVersion(pluginId, pluginVersion)).thenReturn(Mono.just(version));

        RegisteredScopeReference sourceByScopeUrn = coursewareService.fetchSourcesByScopeUrn(scopeId, Lists.newArrayList(
                ConfigurationField.TITLE)).blockFirst();
        assertNotNull(sourceByScopeUrn);
        assertEquals(elementId, sourceByScopeUrn.getElementId());
        assertEquals(elementType, sourceByScopeUrn.getElementType());
        assertEquals(configSchema, sourceByScopeUrn.getConfigSchema());
        assertEquals(configFields, sourceByScopeUrn.getConfigurationFields());
        assertEquals(scopeId, sourceByScopeUrn.getStudentScopeUrn());

        verify(coursewareGateway, atMost(1)).findRegisteredElements(scopeId);
        verify(coursewareGateway, atMost(1)).fetchConfigurationField(eq(elementId), any());
        verify(pluginGateway, atMost(1)).fetchPluginManifestByIdVersion(eq(pluginId), any());
        verify(pluginService, atMost(1)).findLatestVersion(eq(pluginId), eq(pluginVersion));
    }

    @Test
    public void fetchSourcesByScopeUrn_failure() {
        ConfigurationField configurationField = new ConfigurationField()
                .setFieldName("title")
                .setFieldValue("titleValue");

        when(coursewareGateway.findRegisteredElements(scopeId)).thenReturn(Flux.just(new ScopeReference()
                                                                                             .setElementId(elementId)
                                                                                             .setElementType(elementType)
                                                                                             .setPluginId(pluginId)
                                                                                             .setPluginVersion(
                                                                                                     pluginVersion)
                                                                                             .setScopeURN(scopeId)));
        when(coursewareGateway.fetchConfigurationField(any(), any())).thenReturn(Mono.just(configurationField));
        when(pluginGateway.fetchPluginManifestByIdVersion(any(), any())).thenReturn(Mono.just(new PluginManifest()
                                                                                                      .setPluginId(
                                                                                                              pluginId)
                                                                                                      .setConfigurationSchema(
                                                                                                              configSchema)
                                                                                                      .setVersion(
                                                                                                              "1.0.1")));
        when(pluginService.findLatestVersion(pluginId, pluginVersion))
                .thenReturn(Mono.error(new PluginNotFoundFault(pluginId)));

        StepVerifier.create(coursewareService.fetchSourcesByScopeUrn(scopeId, Lists.newArrayList(
                ConfigurationField.TITLE)))
                .verifyError(PluginNotFoundFault.class);
    }

    @Test
    public void findProjectSummary_success() {
        UUID topActivityId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        List<CoursewareElement> path = Lists.newArrayList(
                new CoursewareElement(topActivityId, CoursewareElementType.ACTIVITY),
                new CoursewareElement(pathwayId, CoursewareElementType.PATHWAY),
                new CoursewareElement(activityId, CoursewareElementType.ACTIVITY));
        CoursewareService coursewareServiceSpy = Mockito.spy(coursewareService);
        Mockito.doReturn(Mono.just(path)).when(coursewareServiceSpy).getPath(activityId,
                                                                             CoursewareElementType.ACTIVITY);
        when(activityService.findProjectIdByActivity(topActivityId)).thenReturn(Mono.just(new ProjectActivity().setProjectId(
                projectId)));
        when(projectService.findById(projectId)).thenReturn(Mono.just(new Project().setWorkspaceId(workspaceId).setId(
                projectId)));
        Project project = coursewareServiceSpy.findProjectSummary(activityId, CoursewareElementType.ACTIVITY).block();
        assertNotNull(project);

        assertEquals(workspaceId, project.getWorkspaceId());
        assertEquals(projectId, project.getId());

        verify(activityService, atMost(1)).findProjectIdByActivity(topActivityId);
        verify(projectService, atMost(1)).findById(projectId);
    }

    @Test
    void findCoursewareElement() {
        UUID elementId = UUID.randomUUID();
        CoursewareElement element = new CoursewareElement()
                .setElementId(elementId)
                .setElementType(CoursewareElementType.ACTIVITY);

        when(coursewareGateway.findElementById(any(UUID.class))).thenReturn(Mono.just(element));

        CoursewareElement found = coursewareService.findCoursewareElement(elementId)
                .block();

        assertNotNull(found);
        assertEquals(elementId, found.getElementId());
        assertEquals(CoursewareElementType.ACTIVITY, found.getElementType());
    }

    @Test
    void findCoursewareElement_notFound() {
        UUID elementId = UUID.randomUUID();

        when(coursewareGateway.findElementById(elementId)).thenReturn(Mono.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class, () -> coursewareService.findCoursewareElement(elementId)
                .block());

        assertEquals(String.format("type not found for element %s", elementId), f.getMessage());
    }

    @Test
    void duplicatePathway_notInSameProject() {
        String config = "{\"foo\":\"bar\"}";
        when(pathwayService.duplicateConfig(eq(config), any(UUID.class), any(DuplicationContext.class)))
                .thenReturn(Mono.just(new PathwayConfig()));
        when(pathwayService.findLatestConfig(pathwayId)).thenReturn(Mono.just(
                new PathwayConfig()
                        .setId(UUID.randomUUID())
                        .setConfig(config)
                        .setPathwayId(pathwayId)
        ));

        ArgumentCaptor<UUID> configPathwayIdDuplicationCaptor = ArgumentCaptor.forClass(UUID.class);

        CoursewareService coursewareServiceSpy = Mockito.spy(coursewareService);

        UUID newActivityId = UUID.randomUUID();
        Pathway oldPathway = mockPathway(pathwayId);
        Pathway newPathway = mockPathway();
        UUID activityId = UUID.randomUUID();
        UUID interactiveId = UUID.randomUUID();
        List<WalkableChild> list = Lists.newArrayList();
        list.add(new WalkableChild().setElementId(activityId).setElementType(CoursewareElementType.ACTIVITY));
        list.add(new WalkableChild().setElementId(interactiveId).setElementType(CoursewareElementType.INTERACTIVE));

        isInSameProject = false;
        duplicationContext.setDuplicatorAccount(account.getId())
                            .setDuplicatorSubscriptionId(account.getSubscriptionId())
                            .setNewRootElementId(rootElementId)
                            .setRequireNewAssetId(!isInSameProject);

        when(pathwayService.findById(pathwayId)).thenReturn(Mono.just(oldPathway));
        when(pathwayService.duplicatePathway(any(), eq(newActivityId))).thenReturn(Mono.just(newPathway));
        when(pathwayService.getOrderedWalkableChildren(pathwayId)).thenReturn(Mono.just(list));
        Mockito.doReturn(Mono.just(new Activity())).when(coursewareServiceSpy).duplicateActivity(any(),
                any(),
                eq(creatorId),
                any());
        Mockito.doReturn(Mono.just(new Interactive())).when(coursewareServiceSpy).duplicateInteractive(any(),
                any(),
                any(DuplicationContext.class));

        Pathway result = coursewareServiceSpy.duplicatePathway(pathwayId, newActivityId, creatorId, duplicationContext)
                .block();

        assertNotNull(result);
        assertEquals(newPathway, result);

        verify(coursewareServiceSpy).duplicateActivity(any(), any(), eq(creatorId), any(DuplicationContext.class));
        verify(coursewareServiceSpy).duplicateInteractive(any(), any(), any(DuplicationContext.class));

        assertEquals(1, duplicationContext.getIdsMap().size());
        assertEquals(newPathway.getId(), duplicationContext.getIdsMap().get(pathwayId));
        assertTrue(duplicationContext.getScenarios().isEmpty());
        verify(coursewareAssetService, atLeastOnce()).duplicateAssets(eq(pathwayId),
                any(UUID.class),
                eq(CoursewareElementType.PATHWAY),
                eq(duplicationContext));

        verify(pathwayService).duplicateConfig(eq(config),
                configPathwayIdDuplicationCaptor.capture(),
                any(DuplicationContext.class));

        UUID newPathwayId = configPathwayIdDuplicationCaptor.getValue();

        assertNotNull(newPathwayId);
        assertEquals(result.getId(), newPathwayId);
    }

    @Test
    void duplicateInteractive_withConfig_notInSameProject() {
        ArgumentCaptor<CoursewareElementConfigurationField> configurationFieldCaptor = ArgumentCaptor
                .forClass(CoursewareElementConfigurationField.class);

        UUID newPathwayId = UUID.randomUUID();

        isInSameProject = false;
        duplicationContext.setDuplicatorAccount(account.getId())
                .setDuplicatorSubscriptionId(account.getSubscriptionId())
                .setNewRootElementId(rootElementId)
                .setRequireNewAssetId(!isInSameProject);

        when(interactiveService.findParentPathwayId(any())).thenReturn(Mono.just(pathwayId));
        when(pathwayService.findParentActivityId(any())).thenReturn(Mono.just(activityId));
        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(oldInteractive));
        when(interactiveService.duplicateInteractive(any())).thenReturn(Mono.just(newInteractive));
        when(interactiveService.saveToPathway(newInteractive.getId(), newPathwayId)).thenReturn(Flux.empty());
        when(scenarioService.findScenarioIdsFor(interactiveId)).thenReturn(Flux.empty());
        when(componentService.findIdsByInteractive(interactiveId)).thenReturn(Flux.empty());
        when(feedbackService.findIdsByInteractive(interactiveId)).thenReturn(Mono.empty());
        when(interactiveService.duplicateInteractiveConfig(eq(interactiveId),
                eq(newInteractive.getId()),
                any())).thenReturn(Mono.just(new InteractiveConfig()));

        when(coursewareGateway.fetchConfigurationFields(interactiveId)).thenReturn(Flux.just(
                buildConfigurationField("foo", "bar")
        ));
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        Interactive result = coursewareService.duplicateInteractive(interactiveId, newPathwayId, duplicationContext)
                .block();

        assertNotNull(result);
        assertEquals(newInteractive, result);
        verify(interactiveService).duplicateInteractiveConfig(eq(interactiveId), eq(newInteractive.getId()),
                argThat(c -> c.getIdsMap().containsKey(interactiveId)));
        verify(coursewareGateway).persist(configurationFieldCaptor.capture());

        verify(coursewareAssetService).duplicateAssets(eq(interactiveId),
                eq(newInteractive.getId()),
                eq(CoursewareElementType.INTERACTIVE),
                eq(duplicationContext));

        CoursewareElementConfigurationField duplicatedField = configurationFieldCaptor.getValue();

        assertNotNull(duplicatedField);
        assertEquals(result.getId(), duplicatedField.getElementId());
        assertEquals("foo", duplicatedField.getFieldName());
        assertEquals("bar", duplicatedField.getFieldValue());
    }

    @Test
    void duplicateActivity_notInSameProject() {
        CoursewareService coursewareServiceSpy = Mockito.spy(coursewareService);
        ArgumentCaptor<CoursewareElementConfigurationField> configurationFieldCaptor = ArgumentCaptor
                .forClass(CoursewareElementConfigurationField.class);

        final UUID themeId = UUID.randomUUID();
        final UUID scenarioIdOne = UUID.randomUUID();
        UUID pathwayId = UUID.randomUUID();
        List<UUID> pathwayIds = Lists.newArrayList(pathwayId);
        UUID componentId = UUID.randomUUID();
        UUID newComponentId = UUID.randomUUID();

        isInSameProject = false;

        Activity activity = buildActivity(activityId);
        ActivityConfig activityConfig = buildActivityConfig(activityId);
        ActivityTheme activityTheme = buildActivityTheme(themeId, activityId);
        Activity newActivity = buildActivity(UUID.randomUUID());
        ActivityConfig newActivityConfig = buildActivityConfig(newActivity.getId());
        ActivityTheme newActivityTheme = buildActivityTheme(UUID.randomUUID(), newActivity.getId());
        ArgumentCaptor<ScopeReference> scopeReferenceCaptor = ArgumentCaptor.forClass(ScopeReference.class);

        when(coursewareGateway.persist(any(ScopeReference.class)))
                .thenReturn(Flux.just(new Void[]{}));

        // mocking fetch
        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.just(activityConfig));

        when(coursewareGateway.findRegisteredElements(activity.getStudentScopeURN()))
                .thenReturn(Flux.just(
                        buildScopeReference(activity.getStudentScopeURN(), componentId)
                ));

        when(scenarioService.findScenarioIdsFor(activityId)).thenReturn(Flux.just(scenarioIdOne));
        when(scenarioService.duplicate(eq(scenarioIdOne),
                any(UUID.class),
                any(),
                any())).thenReturn(Mono.just(new Scenario()));

        when(activityService.findParentPathwayId(any())).thenReturn(Mono.empty());
        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.just(activityTheme));
        when(pluginService.findLatestVersion(pluginId, pluginVersion)).thenReturn(Mono.just("version"));
        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.just(pathwayIds));
        when(componentGateway.findComponentIdsByActivity(activityId)).thenReturn(Flux.just(componentId));
        // mocking persistence
        when(activityService.duplicateActivity(eq(activity), eq(creatorId))).thenReturn(Mono.just(newActivity));
        when(activityService.duplicateConfig(any(), eq(newActivity.getId()), any())).thenReturn(Mono.just(
                newActivityConfig));
        when(activityService.duplicateTheme(any(), eq(newActivity.getId()))).thenReturn(Mono.just(newActivityTheme));
        Pathway p = mockPathway();
        Mockito.doReturn(Mono.just(p)).when(coursewareServiceSpy).duplicatePathway(any(), any(), any(), any());
        when(componentService.duplicate(eq(componentId),
                any(),
                eq(CoursewareElementType.ACTIVITY),
                any())).thenAnswer((Answer<Mono<Component>>) invocation -> {
            DuplicationContext context = invocation.getArgument(3);
            context.putIds(componentId, newComponentId);
            return Mono.just(new Component());
        });

        when(coursewareGateway.fetchConfigurationFields(activityId)).thenReturn(Flux.just(
                buildConfigurationField("foo", "bar")
        ));
        when(annotationService.findIdsByElement(any(), any())).thenReturn(Flux.empty());

        Activity duplicated = coursewareServiceSpy.duplicateActivity(activityId, account, isInSameProject).block();

        verify(scenarioService).duplicate(eq(scenarioIdOne),
                eq(newActivity.getId()),
                eq(CoursewareElementType.ACTIVITY),
                any());
        verify(activityService).duplicateActivity(eq(activity), eq(creatorId));
        verify(activityService).duplicateConfig(eq(config), eq(newActivity.getId()), any());
        verify(activityService).duplicateTheme(eq(themeConfig), eq(newActivity.getId()));
        verify(coursewareServiceSpy).duplicatePathway(eq(pathwayId), eq(newActivity.getId()), eq(creatorId), any());
        verify(componentService).duplicate(eq(componentId),
                eq(newActivity.getId()),
                eq(CoursewareElementType.ACTIVITY),
                any());
        verify(coursewareAssetService).duplicateAssets(eq(activityId),
                eq(newActivity.getId()),
                eq(CoursewareElementType.ACTIVITY),
                any(DuplicationContext.class));

        verify(coursewareGateway).persist(configurationFieldCaptor.capture());
        assertNotNull(duplicated);
        assertNotNull(duplicated.getStudentScopeURN());
        assertNotEquals(activity.getStudentScopeURN(), duplicated.getStudentScopeURN());
        assertEquals(newActivity, duplicated);

        verify(coursewareGateway).findRegisteredElements(activity.getStudentScopeURN());
        verify(coursewareGateway).persist(scopeReferenceCaptor.capture());

    }
}
