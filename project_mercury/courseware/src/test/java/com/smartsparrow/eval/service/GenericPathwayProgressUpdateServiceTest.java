package com.smartsparrow.eval.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.util.Providers;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.data.LearnerEvaluationRequest;
import com.smartsparrow.eval.data.LearnerEvaluationResponse;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.LearnerPathwayService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

class GenericPathwayProgressUpdateServiceTest {

    @InjectMocks
    private GenericPathwayProgressUpdateService genericPathwayProgressUpdateService;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private Map<PathwayType, Provider<PathwayProgressUpdateService<?>>> pathwayUpdateImpl;

    @Mock
    private LearnerPathway learnerPathway;
    @Mock
    private LearnerPathway parentPathway;
    @Mock
    private PathwayProgressUpdateService<LearnerPathway> linearPathwayUpdateService;
    @Mock
    private Progress progress;
    @Mock
    private ProgressAction action;

    private static final UUID pathwayId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private LearnerEvaluationResponseContext responseContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(learnerPathway.getId()).thenReturn(pathwayId);
        when(learnerPathway.getType()).thenReturn(PathwayType.LINEAR);

        when(parentPathway.getId()).thenReturn(pathwayId);
        when(parentPathway.getType()).thenReturn(PathwayType.LINEAR);

        responseContext = new LearnerEvaluationResponseContext()
                .setParentPathway(parentPathway)
                .setResponse(new LearnerEvaluationResponse()
                        .setEvaluationRequest(new LearnerEvaluationRequest()
                                .setDeployment(new Deployment()
                                        .setId(deploymentId))));

        when(pathwayUpdateImpl.get(PathwayType.LINEAR)).thenReturn(Providers.of(linearPathwayUpdateService));
        when(linearPathwayUpdateService.updateProgress(any(LearnerPathway.class), eq(action), eq(responseContext)))
                .thenReturn(Mono.just(progress));

        when(learnerPathwayService.find(pathwayId, deploymentId))
                .thenReturn(Mono.just(learnerPathway));
    }

    @Test
    void updateProgress_parentPathwayInContext() {
        final Progress result = genericPathwayProgressUpdateService.updateProgress(
                CoursewareElement.from(pathwayId, CoursewareElementType.PATHWAY),
                action,
                responseContext
        ).block();

        assertNotNull(result);
        assertEquals(progress, result);

        verify(learnerPathwayService, never()).find(any(UUID.class), any(UUID.class));
        verify(linearPathwayUpdateService).updateProgress(parentPathway, action, responseContext);
    }

    @Test
    void updateProgress_parentPathwayInContext_notMatching() {
        final UUID parentPathwayId = UUIDs.timeBased();
        when(parentPathway.getId()).thenReturn(parentPathwayId);
        final Progress result = genericPathwayProgressUpdateService.updateProgress(
                CoursewareElement.from(pathwayId, CoursewareElementType.PATHWAY),
                action,
                responseContext
        ).block();

        assertNotNull(result);
        assertEquals(progress, result);

        verify(learnerPathwayService).find(pathwayId, deploymentId);
        verify(linearPathwayUpdateService).updateProgress(learnerPathway, action, responseContext);
    }

    @Test
    void updateProgress_parentPathwayNotInContext() {
        responseContext.setParentPathway(null);

        final Progress result = genericPathwayProgressUpdateService.updateProgress(
                CoursewareElement.from(pathwayId, CoursewareElementType.PATHWAY),
                action,
                responseContext
        ).block();

        assertNotNull(result);
        assertEquals(progress, result);

        verify(learnerPathwayService).find(pathwayId, deploymentId);
        verify(linearPathwayUpdateService).updateProgress(learnerPathway, action, responseContext);
    }

}