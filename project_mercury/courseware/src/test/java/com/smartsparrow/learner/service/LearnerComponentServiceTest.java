package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.DeploymentLogServiceStub.mockLogMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.ComponentConfig;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ManualGradingConfiguration;
import com.smartsparrow.courseware.lang.ComponentNotFoundException;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerActivityGateway;
import com.smartsparrow.learner.data.LearnerComponent;
import com.smartsparrow.learner.data.LearnerComponentGateway;
import com.smartsparrow.learner.data.LearnerElement;
import com.smartsparrow.learner.data.LearnerInteractiveGateway;
import com.smartsparrow.learner.data.ParentByLearnerComponent;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.PublishComponentException;
import com.smartsparrow.plugin.service.PluginService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class LearnerComponentServiceTest {

    @InjectMocks
    private LearnerComponentService learnerComponentService;

    @Mock
    private LearnerComponentGateway learnerComponentGateway;

    @Mock
    private LearnerActivityGateway learnerActivityGateway;
    @Mock
    private LearnerInteractiveGateway learnerInteractiveGateway;

    @Mock
    private ComponentService componentService;

    @Mock
    private LearnerAssetService learnerAssetService;

    @Mock
    private DeploymentLogService deploymentLogService;

    @Mock
    private LearnerService learnerService;

    @Mock
    private ManualGradeService manualGradeService;

    @Mock
    private PluginService pluginService;

    @Mock
    private LearnerSearchableDocumentService learnerSearchableDocumentService;

    @Mock
    private AnnotationService annotationService;

    @Mock
    private CoursewareService coursewareService;

    private static final UUID parentId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String pluginVerionExpr = "*.1";
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

        when(pluginService.resolvePluginVersion(pluginId, pluginVerionExpr, lockPluginVersionEnabled)).thenReturn(resolvedPluginVersion);

        when(componentService.findIdsByParentType(eq(parentId), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(componentId));

        when(learnerAssetService.publishAssetsFor(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(learnerAssetService.publishMathAssetsFor(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(learnerService.publishConfigurationFields(deployment, componentId)).thenReturn(Flux.just(new Void[]{}));
        when(componentService.findManualGradingConfiguration(componentId)).thenReturn(Mono.empty());

        when(learnerSearchableDocumentService
                .publishSearchableDocuments(any(LearnerElement.class), any(UUID.class)))
                .thenReturn(Flux.empty());

        when(annotationService.publishAnnotationMotivations(any(UUID.class), any(UUID.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(deployment.getCohortId()).thenReturn(cohortId);
        when(coursewareService.getRootElementId(componentId, CoursewareElementType.COMPONENT)).thenReturn(Mono.just(parentId));
        mockLogMethods(deploymentLogService);
    }

    @Test
    void publish_nullParent() {
        PublishComponentException e = assertThrows(PublishComponentException.class,
                () -> learnerComponentService.publish(null, deployment, CoursewareElementType.ACTIVITY, false));

        assertTrue(e.getMessage().contains("parentId is required"));
    }

    @Test
    void publish_nullDeployment() {
        PublishComponentException e = assertThrows(PublishComponentException.class,
                () -> learnerComponentService.publish(parentId, null, CoursewareElementType.ACTIVITY, false));

        assertTrue(e.getMessage().contains("deployment is required"));
    }

    @Test
    void publish_nullParentType() {
        PublishComponentException e = assertThrows(PublishComponentException.class,
                () -> learnerComponentService.publish(parentId, deployment, null, false));

        assertTrue(e.getMessage().contains("parentType is required"));
    }

    @Test
    void publish_noComponentsFound() {
        when(componentService.findIdsByParentType(eq(parentId), any(CoursewareElementType.class)))
                .thenReturn(Flux.empty());

        List<LearnerComponent> components = learnerComponentService
                .publish(parentId, deployment, CoursewareElementType.ACTIVITY, false)
                .collectList()
                .block();

        assertNotNull(components);
        assertEquals(0, components.size());
    }

    @Test
    void publish_componentNotFound() {
        TestPublisher<Component> publisher = TestPublisher.create();
        publisher.error(new ComponentNotFoundException(componentId));

        when(componentService.findById(componentId)).thenReturn(publisher.mono());

        when(componentService.findLatestByConfigId(componentId))
                .thenReturn(Mono.just(new ComponentConfig().setConfig("config")));

        assertThrows(PublishComponentException.class, () ->  learnerComponentService
                .publish(parentId, deployment, CoursewareElementType.ACTIVITY, false).blockLast());
    }

    @Test
    void publish_componentConfigNotFound() {
        when(learnerComponentGateway.persist(any(LearnerComponent.class))).thenReturn(Flux.just(new Void[]{}));
        when(learnerComponentGateway.persistParent(any(ParentByLearnerComponent.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerActivityGateway.persistChildComponent(parentId, deploymentId, changeId, componentId))
                .thenReturn(Flux.just(new Void[]{}));

        when(componentService.findById(componentId)).thenReturn(Mono.just(new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVerionExpr)));
        when(componentService.findLatestByConfigId(componentId))
                .thenReturn(Mono.empty());

        List<LearnerComponent> components = learnerComponentService.publish(parentId, deployment, CoursewareElementType.ACTIVITY, false)
                .collectList()
                .block();

        assertNotNull(components);
        assertEquals(1, components.size());
        verify(manualGradeService, never())
                .publishManualGradingConfiguration(any(ManualGradingConfiguration.class), eq(deployment), eq(parentId), any(CoursewareElementType.class));
    }

    @Test
    void publish() {
        ManualGradingConfiguration manualGradingConfiguration = new ManualGradingConfiguration()
                .setComponentId(componentId)
                .setMaxScore(10d);
        when(manualGradeService.publishManualGradingConfiguration(eq(manualGradingConfiguration), eq(deployment), eq(parentId), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(componentService.findManualGradingConfiguration(componentId)).thenReturn(Mono.just(manualGradingConfiguration));
        when(componentService.findById(componentId)).thenReturn(Mono.just(new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVerionExpr)));

        when(componentService.findLatestByConfigId(componentId))
                .thenReturn(Mono.just(new ComponentConfig().setConfig("config")));

        when(learnerComponentGateway.persist(any(LearnerComponent.class))).thenReturn(Flux.just(new Void[]{}));
        when(learnerComponentGateway.persistParent(any(ParentByLearnerComponent.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerActivityGateway.persistChildComponent(parentId, deploymentId, changeId, componentId))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<LearnerComponent> learnerComponentCaptor = ArgumentCaptor.forClass(LearnerComponent.class);
        ArgumentCaptor<ParentByLearnerComponent> parentCaptor = ArgumentCaptor.forClass(ParentByLearnerComponent.class);

        List<LearnerComponent> components = learnerComponentService
                .publish(parentId, deployment, CoursewareElementType.ACTIVITY, lockPluginVersionEnabled)
                .collectList()
                .block();

        assertNotNull(components);
        assertEquals(1, components.size());

        verify(learnerComponentGateway).persist(learnerComponentCaptor.capture());
        verify(learnerComponentGateway).persistParent(parentCaptor.capture());
        verify(manualGradeService)
                .publishManualGradingConfiguration(eq(manualGradingConfiguration), eq(deployment), eq(parentId), any(CoursewareElementType.class));

        LearnerComponent learnerComponent = learnerComponentCaptor.getValue();

        assertEquals(componentId, learnerComponent.getId());
        assertEquals(deploymentId, learnerComponent.getDeploymentId());
        assertEquals(changeId, learnerComponent.getChangeId());
        assertEquals("config", learnerComponent.getConfig());
        assertEquals(pluginId, learnerComponent.getPluginId());
        assertEquals(resolvedPluginVersion, learnerComponent.getPluginVersionExpr());

        ParentByLearnerComponent parentByLearnerComponent = parentCaptor.getValue();

        assertEquals(componentId, parentByLearnerComponent.getComponentId());
        assertEquals(deploymentId, parentByLearnerComponent.getDeploymentId());
        assertEquals(changeId, parentByLearnerComponent.getChangeId());
        assertEquals(parentId, parentByLearnerComponent.getParentId());
        assertEquals(CoursewareElementType.ACTIVITY, parentByLearnerComponent.getParentType());
    }

    @Test
    @DisplayName("Should return activity components")
    void findComponents_activity() {
        UUID componentId1 = UUID.randomUUID();
        UUID componentId2 = UUID.randomUUID();
        LearnerComponent component1 = new LearnerComponent().setId(componentId1);
        LearnerComponent component2 = new LearnerComponent().setId(componentId2);
        when(learnerActivityGateway.findChildComponents(parentId, deploymentId)).thenReturn(
                Mono.just(Lists.newArrayList(componentId1, componentId2)));
        when(learnerComponentGateway.findLatestDeployed(componentId1, deploymentId)).thenReturn(Mono.just(component1));
        when(learnerComponentGateway.findLatestDeployed(componentId2, deploymentId)).thenReturn(Mono.just(component2));

        List<LearnerComponent> result =
                learnerComponentService.findComponents(parentId, CoursewareElementType.ACTIVITY, deploymentId).collectList().block();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(component1));
        assertTrue(result.contains(component2));
    }

    @Test
    @DisplayName("Should return interactive components")
    void findComponents_interactive() {
        UUID componentId1 = UUID.randomUUID();
        LearnerComponent component1 = new LearnerComponent().setId(componentId1);
        when(learnerInteractiveGateway.findChildrenComponent(parentId, deploymentId)).thenReturn(
                Mono.just(Lists.newArrayList(componentId1)));
        when(learnerComponentGateway.findLatestDeployed(componentId1, deploymentId)).thenReturn(Mono.just(component1));

        List<LearnerComponent> result =
                learnerComponentService.findComponents(parentId, CoursewareElementType.INTERACTIVE, deploymentId).collectList().block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(component1));
    }
}
