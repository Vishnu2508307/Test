package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.pathway.PathwayMock.mockPathway;
import static com.smartsparrow.learner.service.DeploymentLogServiceStub.mockLogMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import javax.inject.Provider;

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.LearnerPathwayBuilder;
import com.smartsparrow.courseware.pathway.LearnerPathwayBuilderStub;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerActivityGateway;
import com.smartsparrow.learner.data.LearnerParentElement;
import com.smartsparrow.learner.data.LearnerPathwayGateway;
import com.smartsparrow.learner.data.LearnerWalkablePathwayChildren;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.PublishPathwayException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class LearnerPathwayServiceTest {

    @InjectMocks
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private LearnerPathwayGateway learnerPathwayGateway;

    @Mock
    private LearnerActivityGateway learnerActivityGateway;

    @Mock
    private PathwayService pathwayService;

    @Mock
    private ActivityService activityService;

    @Mock
    private Provider<LearnerPathwayBuilder> learnerPathwayBuilderProvider;

    @Mock
    private LearnerAssetService learnerAssetService;

    @Mock
    private DeploymentLogService deploymentLogService;

    @Mock
    private LatestDeploymentChangeIdCache changeIdCache;

    @Mock
    private CacheService cacheService;

    private static final UUID parentActivityId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private DeployedActivity deployment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        deployment = mock(DeployedActivity.class);

        when(deployment.getId()).thenReturn(deploymentId);
        when(deployment.getChangeId()).thenReturn(changeId);

        when(activityService.findChildPathwayIds(parentActivityId)).thenReturn(Mono.just(Lists.newArrayList(pathwayId)));
        Pathway pathway = mockPathway(pathwayId);
        when(pathwayService.findById(pathwayId)) //
                .thenReturn(Mono.just(pathway));

        when(learnerAssetService.publishAssetsFor(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(pathwayService.findLatestConfig(pathwayId)).thenReturn(Mono.empty());

        LearnerPathwayBuilderStub.mock(learnerPathwayBuilderProvider);

        mockLogMethods(deploymentLogService);

        when(changeIdCache.get(eq(deploymentId))).thenReturn(changeId);
        when(cacheService.computeIfAbsent(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(2));
    }

    @Test
    void publish_nullParentActivityId() {
        PublishPathwayException e = assertThrows(PublishPathwayException.class,
                () -> learnerPathwayService.publish(null, deployment));

        assertTrue(e.getMessage().contains("parentActivityId is required"));
    }

    @Test
    void publish_nullDeployment() {
        PublishPathwayException e = assertThrows(PublishPathwayException.class,
                () -> learnerPathwayService.publish(parentActivityId, null));

        assertTrue(e.getMessage().contains("deployment is required"));
    }

    @Test
    void publish_noPathways() {
        when(activityService.findChildPathwayIds(parentActivityId)).thenReturn(Mono.just(Lists.newArrayList()));

        List<LearnerPathway> learnerPathways = learnerPathwayService.publish(parentActivityId, deployment)
                .collectList()
                .block();

        assertNotNull(learnerPathways);
        assertEquals(0, learnerPathways.size());

    }

    @Test
    void publish_pathwayNotFound() {
        TestPublisher<Pathway> publisher = TestPublisher.create();
        publisher.error(new PathwayNotFoundException(pathwayId));

        when(pathwayService.findById(pathwayId)).thenReturn(publisher.mono());

        assertThrows(PublishPathwayException.class, () -> learnerPathwayService.publish(parentActivityId, deployment)
                .blockLast());

        verify(deploymentLogService)
                .logFailedStep(any(Deployment.class), eq(pathwayId), eq(CoursewareElementType.PATHWAY), anyString());
    }

    @Test
    void publish() {
        when(learnerPathwayGateway.persist(any(LearnerPathway.class))).thenReturn(Flux.just(new Void[]{}));
        when(learnerActivityGateway.persistChildPathway(parentActivityId, deploymentId, changeId, pathwayId))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerPathwayGateway.persistParentActivity(any(LearnerParentElement.class)))
                .thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<LearnerPathway> pathwayCaptor = ArgumentCaptor.forClass(LearnerPathway.class);
        ArgumentCaptor<LearnerParentElement> parentCaptor = ArgumentCaptor.forClass(LearnerParentElement.class);

        List<LearnerPathway> learnerPathways = learnerPathwayService.publish(parentActivityId, deployment)
                .collectList()
                .block();

        assertNotNull(learnerPathways);
        assertEquals(1, learnerPathways.size());

        verify(learnerPathwayGateway).persist(pathwayCaptor.capture());
        verify(learnerPathwayGateway).persistParentActivity(parentCaptor.capture());

        LearnerPathway published = pathwayCaptor.getValue();

        assertEquals(pathwayId, published.getId());
        assertEquals(PathwayType.LINEAR, published.getType());
        assertEquals(deploymentId, published.getDeploymentId());
        assertEquals(changeId, published.getChangeId());

        LearnerParentElement parent = parentCaptor.getValue();

        assertEquals(parentActivityId, parent.getParentId());
        assertEquals(pathwayId, parent.getElementId());
        assertEquals(deploymentId, parent.getDeploymentId());
        assertEquals(changeId, parent.getChangeId());

        verify(deploymentLogService, never())
                .logFailedStep(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class), anyString());
        verify(deploymentLogService, never())
                .logStartedStep(any(Deployment.class), eq(pathwayId), eq(CoursewareElementType.PATHWAY), anyString());
        verify(deploymentLogService, times(2))
                .logProgressStep(any(Deployment.class), eq(pathwayId), eq(CoursewareElementType.PATHWAY), anyString());
    }

    @Test
    void getOrderedWalkableChildren() {
        UUID child1 = UUID.randomUUID();
        UUID child2 = UUID.randomUUID();
        LearnerWalkablePathwayChildren children = new LearnerWalkablePathwayChildren();
        children.addWalkable(child1, "ACTIVITY");
        children.addWalkable(child2, "INTERACTIVE");

        when(learnerPathwayGateway.findWalkableChildren(pathwayId, deploymentId)).thenReturn(Mono.just(children));

        List<WalkableChild> result = learnerPathwayService.findWalkables(pathwayId, deploymentId).collectList().block();

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(new WalkableChild().setElementId(child1).setElementType(CoursewareElementType.ACTIVITY), result.get(0));
        Assert.assertEquals(new WalkableChild().setElementId(child2).setElementType(CoursewareElementType.INTERACTIVE), result.get(1));
    }

    @Test
    void getOrderedWalkableChildren_notFound() {
        when(learnerPathwayGateway.findWalkableChildren(pathwayId, deploymentId)).thenReturn(Mono.empty());

        List<WalkableChild> result = learnerPathwayService.findWalkables(pathwayId, deploymentId).collectList().block();

        Assert.assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
