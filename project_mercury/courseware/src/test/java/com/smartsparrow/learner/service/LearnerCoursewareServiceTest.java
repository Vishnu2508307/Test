package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementAncestry;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerComponent;
import com.smartsparrow.learner.data.LearnerCoursewareElement;
import com.smartsparrow.learner.data.LearnerFeedback;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.ParentByLearnerComponent;
import com.smartsparrow.learner.lang.LearnerPathwayNotFoundFault;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class LearnerCoursewareServiceTest {

    @InjectMocks
    private LearnerCoursewareService learnerCoursewareService;
    @Mock
    private LearnerActivityService learnerActivityService;
    @Mock
    private LearnerPathwayService learnerPathwayService;
    @Mock
    private LearnerInteractiveService learnerInteractiveService;
    @Mock
    private LearnerComponentService learnerComponentService;
    @Mock
    private LearnerFeedbackService learnerFeedbackService;
    @Mock
    private LatestDeploymentChangeIdCache changeIdCache;
    @Mock
    private LearnerService learnerService;
    @Mock
    private CacheService cacheService;


    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID pathwayIdTwo = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID pathwayIdOne = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(learnerComponentService.findParentFor(componentId, deploymentId))
                .thenReturn(Mono.just(
                        new ParentByLearnerComponent()
                        .setParentType(CoursewareElementType.INTERACTIVE)
                        .setParentId(interactiveId)
                ));

        when(learnerInteractiveService.findParentPathwayId(interactiveId, deploymentId))
                .thenReturn(Mono.just(pathwayIdTwo));

        when(learnerPathwayService.findParentActivityId(pathwayIdTwo, deploymentId))
                .thenReturn(Mono.just(activityId));

        when(learnerActivityService.findParentPathwayId(activityId, deploymentId))
                .thenReturn(Mono.just(pathwayIdOne));

        when(learnerPathwayService.findParentActivityId(pathwayIdOne, deploymentId))
                .thenReturn(Mono.just(rootElementId));

        TestPublisher<UUID> rootElementParentPathwayPublisher = TestPublisher.create();
        rootElementParentPathwayPublisher.error(new LearnerPathwayNotFoundFault("not found"));

        when(learnerActivityService.findParentPathwayId(rootElementId, deploymentId))
                .thenReturn(rootElementParentPathwayPublisher.mono());

        when(changeIdCache.get(eq(deploymentId))).thenReturn(changeId);
        when(cacheService.computeIfAbsent(any(), any(), any(), eq(365L), eq(TimeUnit.DAYS))).thenAnswer(invocation -> invocation.getArgument(2));
    }

    @Test
    @DisplayName("It should return a list of ordered courseware elements from the specified node up to the root element")
    void getAncestry() {
        List<CoursewareElement> ancestry = learnerCoursewareService.getAncestry(deploymentId, componentId, CoursewareElementType.COMPONENT)
                .block();

        assertNotNull(ancestry);
        assertEquals(6, ancestry.size());

        assertEquals(componentId, ancestry.get(0).getElementId());
        assertEquals(interactiveId, ancestry.get(1).getElementId());
        assertEquals(pathwayIdTwo, ancestry.get(2).getElementId());
        assertEquals(activityId, ancestry.get(3).getElementId());
        assertEquals(pathwayIdOne, ancestry.get(4).getElementId());
        assertEquals(rootElementId, ancestry.get(5).getElementId());
    }

    @Test
    void findCoursewareAncestry_notFound() {
        UUID elementId = UUID.randomUUID();

        when(learnerService.findElementByDeployment(elementId, deploymentId)).thenReturn(Mono.empty());

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> learnerCoursewareService.findCoursewareElementAncestry(elementId, deploymentId)
                        .block());

        assertEquals(String.format("type not found for element %s", elementId), f.getMessage());
    }

    @Test
    void findCoursewareAncestry() {
        UUID elementId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        when(learnerService.findElementByDeployment(elementId, deploymentId))
                .thenReturn(Mono.just(new LearnerCoursewareElement()
                        .setId(elementId)
                        .setElementType(CoursewareElementType.COMPONENT)));

        when(learnerComponentService.findParentFor(elementId, deploymentId))
                .thenReturn(Mono.just(new ParentByLearnerComponent()
                        .setComponentId(elementId)
                        .setParentId(parentId)
                        .setParentType(CoursewareElementType.ACTIVITY)));

        when(learnerActivityService.findParentPathwayId(parentId, deploymentId))
                .thenReturn(Mono.error(new LearnerPathwayNotFoundFault("not found")));

        CoursewareElementAncestry found = learnerCoursewareService.findCoursewareElementAncestry(elementId, deploymentId)
                .block();

        assertNotNull(found);
        assertEquals(elementId, found.getElementId());
        assertEquals(CoursewareElementType.COMPONENT, found.getType());
        assertNotNull(found.getAncestry());

        verify(learnerComponentService).findParentFor(elementId, deploymentId);
    }

    @Test
    void fetchConfig_activity() {
        when(learnerActivityService.findActivity(rootElementId, deploymentId))
                .thenReturn(Mono.just(new LearnerActivity()
                        .setConfig("config")));

        String config = learnerCoursewareService.fetchConfig(rootElementId, deploymentId, CoursewareElementType.ACTIVITY)
                .block();

        assertNotNull(config);
        assertEquals("config", config);
    }

    @Test
    void fetchConfig_interactive() {
        when(learnerInteractiveService.findInteractive(rootElementId, deploymentId))
                .thenReturn(Mono.just(new LearnerInteractive()
                        .setConfig("config")));

        String config = learnerCoursewareService.fetchConfig(rootElementId, deploymentId, CoursewareElementType.INTERACTIVE)
                .block();

        assertNotNull(config);
        assertEquals("config", config);
    }

    @Test
    void fetchConfig_component() {
        when(learnerComponentService.findComponent(rootElementId, deploymentId))
                .thenReturn(Mono.just(new LearnerComponent()
                        .setConfig("config")));

        String config = learnerCoursewareService.fetchConfig(rootElementId, deploymentId, CoursewareElementType.COMPONENT)
                .block();

        assertNotNull(config);
        assertEquals("config", config);
    }

    @Test
    void fetchConfig_feedback() {
        when(learnerFeedbackService.findFeedback(rootElementId, deploymentId))
                .thenReturn(Mono.just(new LearnerFeedback()
                        .setConfig("config")));

        String config = learnerCoursewareService.fetchConfig(rootElementId, deploymentId, CoursewareElementType.FEEDBACK)
                .block();

        assertNotNull(config);
        assertEquals("config", config);
    }

    @Test
    void fetchConfig_pathway() {
        LearnerPathway learnerPathway = mock(LearnerPathway.class);
        when(learnerPathway.getConfig()).thenReturn("config");

        when(learnerPathwayService.find(rootElementId, deploymentId))
                .thenReturn(Mono.just(learnerPathway));

        String config = learnerCoursewareService.fetchConfig(rootElementId, deploymentId, CoursewareElementType.PATHWAY)
                .block();

        assertNotNull(config);
        assertEquals("config", config);
    }

    @Test
    void fetchConfig_pathway_null() {
        LearnerPathway learnerPathway = mock(LearnerPathway.class);
        when(learnerPathway.getConfig()).thenReturn(null);

        when(learnerPathwayService.find(rootElementId, deploymentId))
                .thenReturn(Mono.just(learnerPathway));

        String config = learnerCoursewareService.fetchConfig(rootElementId, deploymentId, CoursewareElementType.PATHWAY)
                .block();

        assertNotNull(config);
        assertEquals("", config);
    }
}
