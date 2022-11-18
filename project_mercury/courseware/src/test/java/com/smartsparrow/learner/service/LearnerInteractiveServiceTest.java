package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.DeploymentLogServiceStub.mockLogMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerInteractiveGateway;
import com.smartsparrow.learner.data.LearnerParentElement;
import com.smartsparrow.learner.data.LearnerPathwayGateway;
import com.smartsparrow.learner.data.LearnerSearchableDocument;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.data.LearnerWalkablePathwayChildren;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.PublishInteractiveException;
import com.smartsparrow.plugin.service.PluginService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class LearnerInteractiveServiceTest {

    @InjectMocks
    private LearnerInteractiveService learnerInteractiveService;

    @Mock
    private LearnerInteractiveGateway learnerInteractiveGateway;

    @Mock
    private LearnerPathwayGateway learnerPathwayGateway;

    @Mock
    private InteractiveService interactiveService;

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
    private PluginService pluginService;

    @Mock
    private LearnerSearchableDocumentService learnerSearchableDocumentService;

    @Mock
    private AnnotationService annotationService;

    @Mock
    private CoursewareService coursewareService;

    private static final UUID parentPathwayId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID studentScopeURN = UUID.randomUUID();
    private static final String pluginVersionExpr = "*.1";
    private static final String resolvedPluginVersion = "0.1";
    private static final boolean lockPluginVersionEnabled = true;
    private DeployedActivity deployment;
    private static final UUID cohortId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        deployment = mock(DeployedActivity.class);

        when(deployment.getId()).thenReturn(deploymentId);
        when(deployment.getChangeId()).thenReturn(changeId);

        when(pluginService.resolvePluginVersion(pluginId, pluginVersionExpr, lockPluginVersionEnabled)).thenReturn(resolvedPluginVersion);

        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(new Interactive()
                .setEvaluationMode(EvaluationMode.COMBINED)
                .setId(interactiveId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr)
                .setStudentScopeURN(studentScopeURN)));

        when(interactiveService.findLatestConfig(interactiveId))
                .thenReturn(Mono.just(new InteractiveConfig().setConfig("dope")));

        when(learnerAssetService.publishAssetsFor(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(learnerAssetService.publishMathAssetsFor(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(learnerService.replicateRegisteredStudentScopeElements(any(LearnerWalkable.class), any(Deployment.class), any(Boolean.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(learnerService.publishDocumentItemLinks(any(UUID.class), any(CoursewareElementType.class), any(Deployment.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(publishCompetencyDocumentService.publishDocumentsFor(any(LearnerWalkable.class)))
                .thenReturn(Flux.empty());

        when(learnerService.publishConfigurationFields(any(Deployment.class), eq(interactiveId)))
                .thenReturn(Flux.just(new Void[]{}));

        when(manualGradeService.publishManualComponentByWalkable(interactiveId, deployment)).thenReturn(Flux.empty());

        when(coursewareElementMetaInformationService.publish(interactiveId, deployment)).thenReturn(Flux.empty());

        when(learnerSearchableDocumentService.publishSearchableDocuments(any(LearnerInteractive.class), any(UUID.class)))
                .thenReturn(Flux.just(new LearnerSearchableDocument()));

        when(annotationService.publishAnnotationMotivations(any(UUID.class), any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Flux.just(new Void[]{}));


        when(deployment.getCohortId()).thenReturn(cohortId);
        when(coursewareService.getRootElementId(interactiveId, CoursewareElementType.INTERACTIVE)).thenReturn(Mono.just(parentPathwayId));

        mockLogMethods(deploymentLogService);
    }

    @Test
    void publish_parentPathwayNull() {
        PublishInteractiveException e = assertThrows(PublishInteractiveException.class,
                () -> learnerInteractiveService.publish(null, interactiveId, deployment, lockPluginVersionEnabled));

        assertTrue(e.getMessage().contains("parentPathwayId is required"));
    }

    @Test
    void publish_interactiveIdNull() {
        PublishInteractiveException e = assertThrows(PublishInteractiveException.class,
                () -> learnerInteractiveService.publish(parentPathwayId, null, deployment, lockPluginVersionEnabled));

        assertTrue(e.getMessage().contains("interactiveId is required"));
    }

    @Test
    void publish_deploymentNull() {
        PublishInteractiveException e = assertThrows(PublishInteractiveException.class,
                () -> learnerInteractiveService.publish(parentPathwayId, interactiveId, null, lockPluginVersionEnabled));

        assertTrue(e.getMessage().contains("deployment is required"));
    }

    @Test
    void publish_interactiveNotFound() {
        TestPublisher<Interactive> publisher = TestPublisher.create();
        publisher.error(new InteractiveNotFoundException(interactiveId));

        when(interactiveService.findById(interactiveId)).thenReturn(publisher.mono());

        assertThrows(PublishInteractiveException.class,
                () -> learnerInteractiveService.publish(parentPathwayId, interactiveId, deployment, lockPluginVersionEnabled).block());
    }

    @Test
    void publish_interactiveConfigNotFound() {
        when(learnerInteractiveGateway.persist(any(LearnerInteractive.class), any(Deployment.class))).thenReturn(Flux.just(new Void[]{}));
        when(learnerInteractiveGateway.persistParentPathway(any(LearnerParentElement.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerPathwayGateway.persistChildWalkable(any(LearnerWalkablePathwayChildren.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(interactiveService.findLatestConfig(interactiveId)).thenReturn(Mono.empty());

        LearnerInteractive learnerInteractive = learnerInteractiveService.publish(parentPathwayId, interactiveId, deployment,
                                                                                  lockPluginVersionEnabled).block();

        assertNotNull(learnerInteractive);
        verify(manualGradeService).publishManualComponentByWalkable(interactiveId, deployment);
    }

    @Test
    void publish() {
        when(learnerInteractiveGateway.persist(any(LearnerInteractive.class), any(Deployment.class))).thenReturn(Flux.just(new Void[]{}));
        when(learnerInteractiveGateway.persistParentPathway(any(LearnerParentElement.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerPathwayGateway.persistChildWalkable(any(LearnerWalkablePathwayChildren.class)))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<LearnerInteractive> interactiveCaptor = ArgumentCaptor.forClass(LearnerInteractive.class);
        ArgumentCaptor<LearnerParentElement> parentCaptor = ArgumentCaptor.forClass(LearnerParentElement.class);
        ArgumentCaptor<LearnerWalkablePathwayChildren> childCaptor = ArgumentCaptor.forClass(LearnerWalkablePathwayChildren.class);

        LearnerInteractive published = learnerInteractiveService.publish(parentPathwayId, interactiveId, deployment, lockPluginVersionEnabled)
                .block();

        assertNotNull(published);

        verify(learnerInteractiveGateway).persist(interactiveCaptor.capture(), any(Deployment.class));
        verify(learnerInteractiveGateway).persistParentPathway(parentCaptor.capture());
        verify(learnerPathwayGateway).persistChildWalkable(childCaptor.capture());
        verify(learnerService).publishConfigurationFields(any(Deployment.class), eq(interactiveId));

        LearnerInteractive learnerInteractive = interactiveCaptor.getValue();

        assertEquals(interactiveId, learnerInteractive.getId());
        assertEquals(deploymentId, learnerInteractive.getDeploymentId());
        assertEquals(changeId, learnerInteractive.getChangeId());
        assertEquals(pluginId, learnerInteractive.getPluginId());
        assertEquals("dope", learnerInteractive.getConfig());
        assertEquals(resolvedPluginVersion, learnerInteractive.getPluginVersionExpr());
        assertEquals(studentScopeURN, learnerInteractive.getStudentScopeURN());
        assertEquals(EvaluationMode.COMBINED, learnerInteractive.getEvaluationMode());

        LearnerParentElement parentElement = parentCaptor.getValue();

        assertEquals(parentPathwayId, parentElement.getParentId());
        assertEquals(interactiveId, parentElement.getElementId());
        assertEquals(deploymentId, parentElement.getDeploymentId());
        assertEquals(changeId, parentElement.getChangeId());

        LearnerWalkablePathwayChildren child = childCaptor.getValue();

        assertEquals(interactiveId, child.getWalkableIds().get(0));
        assertEquals(CoursewareElementType.INTERACTIVE.name(), child.getWalkableTypes().get(interactiveId));
        assertEquals(deploymentId, child.getDeploymentId());
        assertEquals(changeId, child.getChangeId());
        assertEquals(parentPathwayId, child.getPathwayId());

        verify(deploymentLogService, times(12))
                .logProgressStep(any(Deployment.class), eq(interactiveId), eq(CoursewareElementType.INTERACTIVE), anyString());
        verify(deploymentLogService, times(0))
                .logStartedStep(any(Deployment.class), eq(interactiveId), eq(CoursewareElementType.INTERACTIVE), anyString());
        verify(manualGradeService).publishManualComponentByWalkable(interactiveId, deployment);
    }
}
