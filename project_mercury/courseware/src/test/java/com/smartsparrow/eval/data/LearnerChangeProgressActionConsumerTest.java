package com.smartsparrow.eval.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Provider;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.inject.util.Providers;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.action.progress.ProgressActionResult;
import com.smartsparrow.eval.service.ProgressUpdateService;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class LearnerChangeProgressActionConsumerTest {

    @InjectMocks
    private LearnerChangeProgressActionConsumer consumer;

    @Mock
    private Map<CoursewareElementType, Provider<ProgressUpdateService>> progressUpdateServiceProviders;

    @Mock
    private ProgressAction action;

    @Mock
    private Progress interactiveProgress;
    @Mock
    private Progress parentPathwayProgress;
    @Mock
    private Progress parentActivityProgress;
    @Mock
    private ProgressUpdateService interactiveImplementation;
    @Mock
    private ProgressUpdateService activityImplementation;
    @Mock
    private ProgressUpdateService pathwayImplementation;
    @Mock
    private StudentProgressRTMProducer studentProgressRTMProducer;

    private LearnerEvaluationResponseContext responseContext;
    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID parentPathwayId = UUIDs.timeBased();
    private static final UUID parentActivityId = UUIDs.timeBased();
    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final String clientId = "clientId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(progressUpdateServiceProviders.get(CoursewareElementType.INTERACTIVE))
                .thenReturn(Providers.of(interactiveImplementation));
        when(progressUpdateServiceProviders.get(CoursewareElementType.PATHWAY))
                .thenReturn(Providers.of(pathwayImplementation));
        when(progressUpdateServiceProviders.get(CoursewareElementType.ACTIVITY))
                .thenReturn(Providers.of(activityImplementation));

        when(interactiveImplementation.updateProgress(any(CoursewareElement.class), eq(action), any(LearnerEvaluationResponseContext.class)))
                .thenReturn(Mono.just(interactiveProgress));
        when(pathwayImplementation.updateProgress(any(CoursewareElement.class), eq(action), any(LearnerEvaluationResponseContext.class)))
                .thenReturn(Mono.just(parentPathwayProgress));
        when(activityImplementation.updateProgress(any(CoursewareElement.class), eq(action), any(LearnerEvaluationResponseContext.class)))
                .thenReturn(Mono.just(parentActivityProgress));
        TestPublisher<Exchange> publisher = TestPublisher.create();
        publisher.complete();

        when(interactiveProgress.getCoursewareElementId()).thenReturn(interactiveId);
        when(parentActivityProgress.getCoursewareElementId()).thenReturn(parentActivityId);
        when(parentPathwayProgress.getCoursewareElementId()).thenReturn(parentPathwayId);
        when(studentProgressRTMProducer.buildStudentProgressRTMConsumable(any(UUID.class),
                                                                          any(UUID.class),
                                                                          any(UUID.class),
                                                                          any(Progress.class)))
                .thenReturn(studentProgressRTMProducer);

        responseContext = new LearnerEvaluationResponseContext()
                .setResponse(new LearnerEvaluationResponse()
                        .setEvaluationRequest(new LearnerEvaluationRequest()
                                .setStudentId(studentId)
                                .setDeployment(new Deployment()
                                        .setId(deploymentId))
                                .setProducingClientId(clientId)))
                .setAncestry(Lists.newArrayList(
                        CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE),
                        CoursewareElement.from(parentPathwayId, CoursewareElementType.PATHWAY),
                        CoursewareElement.from(parentActivityId, CoursewareElementType.ACTIVITY)
                ));
    }

    @Test
    void getActionConsumerOptions() {
        ActionConsumerOptions options = consumer.getActionConsumerOptions()
                .block();
        assertNotNull(options);
        assertFalse(options.isAsync());
    }

    @Test
    void consume() {
        final ProgressActionResult result = consumer.consume(action, responseContext)
                .block();

        assertNotNull(result);
        assertEquals(3, result.getValue().size());
        assertEquals(Action.Type.CHANGE_PROGRESS, result.getType());

        verify(interactiveImplementation).updateProgress(
                eq(CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE)),
                eq(action),
                eq(responseContext)
        );
        verify(pathwayImplementation).updateProgress(
                eq(CoursewareElement.from(parentPathwayId, CoursewareElementType.PATHWAY)),
                eq(action),
                eq(responseContext)
        );
        verify(activityImplementation).updateProgress(
                eq(CoursewareElement.from(parentActivityId, CoursewareElementType.ACTIVITY)),
                eq(action),
                eq(responseContext)
        );
        verify(studentProgressRTMProducer).buildStudentProgressRTMConsumable(studentId, interactiveId, deploymentId,interactiveProgress);
        verify(studentProgressRTMProducer).buildStudentProgressRTMConsumable(studentId, parentPathwayId, deploymentId,parentPathwayProgress);
        verify(studentProgressRTMProducer).buildStudentProgressRTMConsumable(studentId, parentActivityId, deploymentId,parentActivityProgress);
    }

    @Test
    void consume_emptyAncestry() {
        responseContext = new LearnerEvaluationResponseContext()
                .setResponse(new LearnerEvaluationResponse()
                                     .setEvaluationRequest(new LearnerEvaluationRequest()
                                                                   .setStudentId(studentId)
                                                                   .setDeployment(new Deployment()
                                                                                          .setId(deploymentId))
                                                                   .setProducingClientId(clientId)))
                .setAncestry(Collections.emptyList());
        List<CoursewareElement> ancestry = responseContext.getAncestry();
        final ProgressActionResult result = consumer.consume(action, responseContext)
                .block();
        assertTrue(ancestry.isEmpty());
        assertNull(result);

    }

}
