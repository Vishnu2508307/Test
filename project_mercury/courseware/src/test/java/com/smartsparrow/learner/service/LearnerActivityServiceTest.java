package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.pathway.LearnerPathwayMock.mockLearnerPathway;
import static com.smartsparrow.learner.service.DeploymentLogServiceStub.mockLogMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerActivityGateway;
import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.learner.data.LearnerParentElement;
import com.smartsparrow.learner.data.LearnerPathwayGateway;
import com.smartsparrow.learner.data.LearnerSearchableDocument;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.data.LearnerWalkablePathwayChildren;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.PublishActivityException;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.workspace.data.ThemePayload;
import com.smartsparrow.workspace.data.ThemeVariant;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class LearnerActivityServiceTest {

    @InjectMocks
    private LearnerActivityService learnerActivityService;

    @Mock
    private LearnerActivityGateway learnerActivityGateway;

    @Mock
    private LearnerPathwayGateway learnerPathwayGateway;

    @Mock
    private ActivityService activityService;

    @Mock
    private LearnerAssetService learnerAssetService;

    @Mock
    private LearnerService learnerService;

    @Mock
    private PublishCompetencyDocumentService publishCompetencyDocumentService;

    @Mock
    private DeploymentLogService deploymentLogService;

    @Mock
    private ManualGradeService manualGradeService;

    @Mock
    private CoursewareElementMetaInformationService coursewareElementMetaInformationService;

    @Mock
    private LatestDeploymentChangeIdCache changeIdCache;

    @Mock
    private CacheService cacheService;

    @Mock
    private PluginService pluginService;

    @Mock
    private LearnerSearchableDocumentService learnerSearchableDocumentService;

    @Mock
    private AnnotationService annotationService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private ThemeService themeService;

    @Mock
    private LearnerThemeService learnerThemeService;

    private static final UUID activityId = UUID.randomUUID();
    private static final UUID parentPathwayId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID creatorId = UUID.randomUUID();
    private static final UUID studentScopeURN = UUID.randomUUID();
    private static final String pluginVerionExpr = "*.1";
    private static final String resolvedPluginVersion = "0.1";
    private static final boolean lockPluginVersionEnabled = true;
    private DeployedActivity deployment;
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID themeId = UUID.randomUUID();
    private static final UUID variantId = UUID.randomUUID();
    private static final String variantName = "Day";
    private static final String config = " {\"colo\":\"orange\", \"margin\":\"20\"}";


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        deployment = mock(DeployedActivity.class);

        when(deployment.getId()).thenReturn(deploymentId);
        when(deployment.getChangeId()).thenReturn(changeId);
        when(deployment.getActivityId()).thenReturn(activityId);

        when(pluginService.resolvePluginVersion(pluginId, pluginVerionExpr, lockPluginVersionEnabled)).thenReturn(resolvedPluginVersion);

        Activity activity = new Activity()
                .setEvaluationMode(EvaluationMode.COMBINED)
                .setId(activityId)
                .setPluginId(pluginId)
                .setCreatorId(creatorId)
                .setPluginVersionExpr(pluginVerionExpr)
                .setStudentScopeURN(studentScopeURN);

        when(activityService.findById(activityId)).thenReturn(Mono.just(activity));
        when(activityService.findLatestConfig(activityId))
                .thenReturn(Mono.just(new ActivityConfig().setConfig("config")));
        when(activityService.getLatestActivityThemeByActivityId(activityId))
                .thenReturn(Mono.just(new ActivityTheme().setConfig("theme config")));

        when(learnerAssetService.publishAssetsFor(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(learnerAssetService.publishMathAssetsFor(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(learnerService.replicateRegisteredStudentScopeElements(any(LearnerWalkable.class), any(Deployment.class), any(Boolean.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(learnerService.publishDocumentItemLinks(any(UUID.class), any(CoursewareElementType.class), any(Deployment.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(publishCompetencyDocumentService.publishDocumentsFor(any(LearnerWalkable.class)))
                .thenReturn(Flux.just(new LearnerDocument()));

        when(learnerService.publishConfigurationFields(any(Deployment.class), any(UUID.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(manualGradeService.publishManualComponentByWalkable(activityId, deployment)).thenReturn(Flux.empty());

        when(coursewareElementMetaInformationService.publish(activityId, deployment))
                .thenReturn(Flux.empty());

        when(learnerSearchableDocumentService.publishSearchableDocuments(any(LearnerActivity.class), any(UUID.class)))
                .thenReturn(Flux.just(new LearnerSearchableDocument()));

        when(annotationService.publishAnnotationMotivations(any(UUID.class), any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Flux.just(new Void[]{}));


        mockLogMethods(deploymentLogService);

        when(changeIdCache.get(eq(deploymentId))).thenReturn(changeId);
        when(cacheService.computeIfAbsent(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(2));

        when(deployment.getCohortId()).thenReturn(cohortId);
        when(coursewareService.getRootElementId(activityId, CoursewareElementType.ACTIVITY)).thenReturn(Mono.just(parentPathwayId));
        when(themeService.fetchThemeByElementId(activityId)).thenReturn(Mono.just(new ThemePayload()
        .setId(themeId)
        .setThemeVariants(new ArrayList<>())));
        when(themeService.findThemeVariantByState(themeId, ThemeState.DEFAULT)).thenReturn(Mono.just(new ThemeVariant()
        .setVariantId(variantId)
        .setThemeId(themeId)
        .setVariantName(variantName)
        .setConfig(config)));

        when(learnerThemeService.saveSelectedThemeByElement(any())).thenReturn(Mono.empty());

    }

    @Test
    void publish_activityIdNull() {
        PublishActivityException e = assertThrows(PublishActivityException.class,
                () -> learnerActivityService.publish(null, deployment, null, lockPluginVersionEnabled));

        assertTrue(e.getMessage().contains("activityId is required"));
    }

    @Test
    void publish_deploymentNull() {
        PublishActivityException e = assertThrows(PublishActivityException.class,
                () -> learnerActivityService.publish(activityId, null, null, lockPluginVersionEnabled));

        assertTrue(e.getMessage().contains("deployment is required"));
    }

    @Test
    void publish_activityNotFound() {
        TestPublisher<Activity> publisher = TestPublisher.create();
        publisher.error(new ActivityNotFoundException(activityId));

        when(activityService.findById(activityId)).thenReturn(publisher.mono());

        assertThrows(PublishActivityException.class,
                () -> learnerActivityService.publish(activityId, deployment, null, lockPluginVersionEnabled).block());
    }

    @Test
    void publish_activityConfigNotFound() {
        when(learnerActivityGateway.persist(any(LearnerActivity.class), any(Deployment.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerActivityGateway.persistParentPathway(any(LearnerParentElement.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerPathwayGateway.persistChildWalkable(any(LearnerWalkablePathwayChildren.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(activityService.findLatestConfig(activityId)).thenReturn(Mono.empty());

        LearnerActivity learnerActivity = learnerActivityService.publish(activityId, deployment, null, false)
                .block();

        assertNotNull(learnerActivity);
    }

    @Test
    void publish_activityThemeNotFound() {
        when(learnerActivityGateway.persist(any(LearnerActivity.class), any(Deployment.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerActivityGateway.persistParentPathway(any(LearnerParentElement.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerPathwayGateway.persistChildWalkable(any(LearnerWalkablePathwayChildren.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());

        LearnerActivity learnerActivity = learnerActivityService.publish(activityId, deployment, null, false)
                .block();

        assertNotNull(learnerActivity);
    }

    @Test
    void publish_noParentPathway() {
        ArgumentCaptor<LearnerActivity> captor = ArgumentCaptor.forClass(LearnerActivity.class);
        when(learnerActivityGateway.persist(any(LearnerActivity.class), any(Deployment.class)))
                .thenReturn(Flux.just(new Void[]{}));

        learnerActivityService.publish(activityId, deployment, null, true).block();

        verify(learnerActivityGateway).persist(captor.capture(), any(Deployment.class));

        LearnerActivity learnerActivity = captor.getValue();

        assertEquals(activityId, learnerActivity.getId());
        assertEquals(deploymentId, learnerActivity.getDeploymentId());
        assertEquals(changeId, learnerActivity.getChangeId());
        assertEquals("config", learnerActivity.getConfig());
        assertEquals(config, learnerActivity.getTheme());
        assertEquals(pluginId, learnerActivity.getPluginId());
        assertEquals(resolvedPluginVersion, learnerActivity.getPluginVersionExpr());
        assertEquals(studentScopeURN, learnerActivity.getStudentScopeURN());
        assertEquals(EvaluationMode.COMBINED, learnerActivity.getEvaluationMode());

        verify(learnerService).publishConfigurationFields(any(Deployment.class), eq(activityId));
        verify(deploymentLogService, times(12))
                .logProgressStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
        verify(deploymentLogService, times(0))
                .logStartedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
        verify(manualGradeService).publishManualComponentByWalkable(activityId, deployment);
    }

    @Test
    void publish_withParent() {
        ArgumentCaptor<LearnerParentElement> parentElementCaptor = ArgumentCaptor.forClass(LearnerParentElement.class);
        ArgumentCaptor<LearnerWalkablePathwayChildren> childCaptor = ArgumentCaptor.forClass(LearnerWalkablePathwayChildren.class);

        when(learnerActivityGateway.persist(any(LearnerActivity.class), any(Deployment.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerActivityGateway.persistParentPathway(any(LearnerParentElement.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerPathwayGateway.persistChildWalkable(any(LearnerWalkablePathwayChildren.class)))
                .thenReturn(Flux.just(new Void[]{}));

        learnerActivityService.publish(activityId, deployment, parentPathwayId, lockPluginVersionEnabled).block();

        verify(learnerActivityGateway).persistParentPathway(parentElementCaptor.capture());
        verify(learnerPathwayGateway).persistChildWalkable(childCaptor.capture());

        LearnerParentElement parent = parentElementCaptor.getValue();

        assertEquals(activityId, parent.getElementId());
        assertEquals(parentPathwayId, parent.getParentId());
        assertEquals(deploymentId, parent.getDeploymentId());
        assertEquals(changeId, parent.getChangeId());

        LearnerWalkablePathwayChildren child = childCaptor.getValue();

        assertEquals(parentPathwayId, child.getPathwayId());
        assertEquals(activityId, child.getWalkableIds().get(0));
        assertEquals(CoursewareElementType.ACTIVITY.name(), child.getWalkableTypes().get(activityId));
        assertEquals(deploymentId, child.getDeploymentId());
        assertEquals(changeId, child.getChangeId());

        verify(learnerService).publishConfigurationFields(any(Deployment.class), eq(activityId));
        verify(deploymentLogService, times(13))
                .logProgressStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
        verify(deploymentLogService, times(0))
                .logStartedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
        verify(manualGradeService).publishManualComponentByWalkable(activityId, deployment);
    }

    @Test
    @DisplayName("Should return empty mono if child pathwayId does not belong to the deployed activity")
    void findChildPathway_noChildPathwayId() {
        UUID childPathwayId = UUID.randomUUID();
        when(learnerActivityGateway.findChildPathwayIds(activityId, deploymentId)).thenReturn(Mono.just(Lists.newArrayList(UUID.randomUUID())));

        Mono<LearnerPathway> result = learnerActivityService.findChildPathway(activityId, deploymentId, childPathwayId);

        assertNull(result.block());
    }

    @Test
    @DisplayName("Should return mono with pathway")
    void findChildPathway_childPathwayId() {
        UUID childPathwayId = UUID.randomUUID();
        LearnerPathway pathway = mockLearnerPathway(childPathwayId);
        when(learnerActivityGateway.findChildPathwayIds(activityId, deploymentId)).thenReturn(Mono.just(Lists.newArrayList(childPathwayId)));
        when(learnerPathwayGateway.findLatestDeployed(childPathwayId, deploymentId)).thenReturn(Mono.just(pathway));

        Mono<LearnerPathway> result = learnerActivityService.findChildPathway(activityId, deploymentId, childPathwayId);

        assertNotNull(result.block());
        assertEquals(pathway, result.block());
    }

    @Test
    @DisplayName("Should return pathways in correct order")
    void findChildPathways() {
        UUID childPathwayId1 = UUID.randomUUID();
        UUID childPathwayId2 = UUID.randomUUID();
        LearnerPathway pathway1 = mockLearnerPathway(childPathwayId1);
        LearnerPathway pathway2 = mockLearnerPathway(childPathwayId2);
        when(learnerActivityGateway.findChildPathwayIds(activityId, deploymentId)).thenReturn(Mono.just(
                Lists.newArrayList(childPathwayId1, childPathwayId2)));
        when(learnerPathwayGateway.findLatestDeployed(childPathwayId1, deploymentId)).thenReturn(Mono.delay(Duration.ofSeconds(1)).thenReturn(pathway1));
        when(learnerPathwayGateway.findLatestDeployed(childPathwayId2, deploymentId)).thenReturn(Mono.just(pathway2));

        List<LearnerPathway> result = learnerActivityService.findChildPathways(activityId, deploymentId).collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(pathway1, result.get(0));
        assertEquals(pathway2, result.get(1));
    }

    @Test
    void publish_selectedThemeFound() {
        when(learnerActivityGateway.persist(any(LearnerActivity.class), any(Deployment.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerActivityGateway.persistParentPathway(any(LearnerParentElement.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerPathwayGateway.persistChildWalkable(any(LearnerWalkablePathwayChildren.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(themeService.fetchThemeByElementId(activityId)).thenReturn(Mono.just(new ThemePayload()
                .setName("Theme_name")
                .setId(themeId)
        .setThemeVariants(Arrays.asList(new ThemeVariant()
        .setConfig("config1")))));

        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());

        LearnerActivity learnerActivity = learnerActivityService.publish(activityId, deployment, null, false)
                .block();

        assertNotNull(learnerActivity);
        verify(themeService).fetchThemeByElementId(eq(activityId));
    }

    @Test
    void publish_defaultThemeFound() {
        when(learnerActivityGateway.persist(any(LearnerActivity.class), any(Deployment.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerActivityGateway.persistParentPathway(any(LearnerParentElement.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerPathwayGateway.persistChildWalkable(any(LearnerWalkablePathwayChildren.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(themeService.fetchThemeByElementId(activityId)).thenReturn(Mono.empty());

        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.just(new ActivityTheme()
        .setConfig("theme config")));

        LearnerActivity learnerActivity = learnerActivityService.publish(activityId, deployment, null, false)
                .block();

        assertNotNull(learnerActivity);
        verify(themeService).fetchThemeByElementId(eq(activityId));
        verify(activityService).getLatestActivityThemeByActivityId(eq(activityId));
    }

    @Test
    void publish_notDefaultAndSelectedThemeFound() {
        when(learnerActivityGateway.persist(any(LearnerActivity.class), any(Deployment.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerActivityGateway.persistParentPathway(any(LearnerParentElement.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerPathwayGateway.persistChildWalkable(any(LearnerWalkablePathwayChildren.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(themeService.fetchThemeByElementId(activityId)).thenReturn(Mono.empty());

        when(activityService.getLatestActivityThemeByActivityId(activityId)).thenReturn(Mono.empty());

        LearnerActivity learnerActivity = learnerActivityService.publish(activityId, deployment, null, false)
                .block();

        assertNotNull(learnerActivity);
        verify(themeService).fetchThemeByElementId(eq(activityId));
        verify(activityService).getLatestActivityThemeByActivityId(eq(activityId));
    }
}
