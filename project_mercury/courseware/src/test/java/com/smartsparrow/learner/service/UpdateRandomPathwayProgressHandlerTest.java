package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.pathway.WalkableChildStub.buildWalkableChild;
import static com.smartsparrow.learner.service.UpdateProgressHandlerDataStub.mockProgressHandler;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.LearnerRandomPathway;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.event.UpdateProgressMessage;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.progress.RandomPathwayProgress;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Disabled
class UpdateRandomPathwayProgressHandlerTest {

    @InjectMocks
    private UpdateRandomPathwayProgressHandler updateRandomPathwayProgressHandler;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private ProgressService progressService;

    @Mock
    private AttemptService attemptService;

    @Mock
    private Exchange exchange;
    @Mock
    private UpdateCoursewareElementProgressEvent event;
    @Mock
    private UpdateProgressMessage updateProgressMessage;
    @Mock
    private Progress childProgress;
    @Mock
    private LearnerRandomPathway learnerRandomPathway;
    @Mock
    private Attempt childAttempt;

    private UpdateRandomPathwayProgressHandler spy;
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID evaluationId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID childAttemptId = UUID.randomUUID();
    private static final UUID pathwayAttemptId = UUID.randomUUID();
    private static final List<Progress> progresses = new ArrayList<>();
    private static WalkableChild walkableOne = buildWalkableChild();
    private static WalkableChild walkableTwo = buildWalkableChild();
    private static WalkableChild walkableThree = buildWalkableChild();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        learnerRandomPathway = mock(LearnerRandomPathway.class);

        spy = mockProgressHandler(updateRandomPathwayProgressHandler);

        when(learnerRandomPathway.getId()).thenReturn(pathwayId);
        when(learnerRandomPathway.getDeploymentId()).thenReturn(deploymentId);
        when(learnerRandomPathway.getChangeId()).thenReturn(changeId);
        when(learnerRandomPathway.getConfig()).thenReturn("{\"exitAfter\": 2}");
        when(learnerRandomPathway.getExitAfter()).thenReturn(2);

        Message message = mock(Message.class);

        when(exchange.getIn()).thenReturn(message);
        when(message.getBody(UpdateCoursewareElementProgressEvent.class)).thenReturn(event);
        when(childProgress.getAttemptId()).thenReturn(childAttemptId);

        progresses.add(childProgress);

        when(event.getElement()).thenReturn(CoursewareElement.from(pathwayId, CoursewareElementType.PATHWAY));
        when(event.getUpdateProgressEvent()).thenReturn(updateProgressMessage);
        when(updateProgressMessage.getChangeId()).thenReturn(changeId);
        when(updateProgressMessage.getDeploymentId()).thenReturn(deploymentId);
        when(updateProgressMessage.getEvaluationId()).thenReturn(evaluationId);
        when(updateProgressMessage.getStudentId()).thenReturn(studentId);
        when(event.getEventProgress()).thenReturn(progresses);

        when(childAttempt.getParentId()).thenReturn(pathwayAttemptId);

        when(learnerPathwayService.find(pathwayId, deploymentId, LearnerRandomPathway.class))
                .thenReturn(Mono.just(learnerRandomPathway));
        when(attemptService.findById(childAttemptId)).thenReturn(Mono.just(childAttempt));

        when(learnerPathwayService.findWalkables(pathwayId, deploymentId)).thenReturn(Flux.just(
                walkableOne,
                walkableTwo,
                walkableThree
        ));

        when(progressService.persist(any(RandomPathwayProgress.class))).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void updateProgress_firstTime_walkableIncomplete() {

        when(childProgress.getCoursewareElementId()).thenReturn(walkableOne.getElementId());
        when(childProgress.getCoursewareElementType()).thenReturn(walkableOne.getElementType());

        ArgumentCaptor<RandomPathwayProgress> captor = ArgumentCaptor.forClass(RandomPathwayProgress.class);

        when(progressService.findLatestRandomPathway(deploymentId, pathwayId, studentId))
                .thenReturn(Mono.empty());
        when(childProgress.getCompletion()).thenReturn(new Completion().setValue(0.2f).setConfidence(0.4f));

        spy.updateProgress(exchange);

        verify(progressService).persist(captor.capture());

        RandomPathwayProgress persistedProgress = captor.getValue();

        verifyRandomProgressValues(persistedProgress);
        verifyCompletion(persistedProgress.getCompletion(), 0.1f, 0.2f);

        assertEquals(walkableOne.getElementId(), persistedProgress.getInProgressElementId());
        assertEquals(walkableOne.getElementType(), persistedProgress.getInProgressElementType());

        assertTrue(persistedProgress.getCompletedWalkables().isEmpty());

        assertEquals(1, persistedProgress.getChildWalkableCompletionValues().size());
        assertEquals(1, persistedProgress.getChildWalkableCompletionConfidences().size());

    }

    @Test
    void updateProgress_firstTime_walkableComplete() {
        when(childProgress.getCoursewareElementId()).thenReturn(walkableOne.getElementId());
        when(childProgress.getCoursewareElementType()).thenReturn(walkableOne.getElementType());

        ArgumentCaptor<RandomPathwayProgress> captor = ArgumentCaptor.forClass(RandomPathwayProgress.class);

        when(progressService.findLatestRandomPathway(deploymentId, pathwayId, studentId))
                .thenReturn(Mono.empty());
        when(childProgress.getCompletion()).thenReturn(new Completion().setValue(1f).setConfidence(1f));

        spy.updateProgress(exchange);

        verify(progressService).persist(captor.capture());

        RandomPathwayProgress persistedProgress = captor.getValue();

        verifyRandomProgressValues(persistedProgress);
        verifyCompletion(persistedProgress.getCompletion(), 0.5f, 0.5f);

        assertNull(persistedProgress.getInProgressElementId());
        assertNull(persistedProgress.getInProgressElementType());

        assertEquals(1, persistedProgress.getCompletedWalkables().size());

        assertEquals(1, persistedProgress.getChildWalkableCompletionValues().size());
        assertEquals(1, persistedProgress.getChildWalkableCompletionConfidences().size());
    }

    @Test
    void updateProgress_hasCompleted_walkableIncomplete() {
        when(childProgress.getCoursewareElementId()).thenReturn(walkableOne.getElementId());
        when(childProgress.getCoursewareElementType()).thenReturn(walkableOne.getElementType());

        Map<UUID, Float> childCompletionValues = new HashMap<UUID, Float>() {
            {put(walkableThree.getElementId(), 1f);}
        };
        Map<UUID, Float> childCompletionConfidences = new HashMap<UUID, Float>() {
            {put(walkableThree.getElementId(), 1f);}
        };

        RandomPathwayProgress previousProgress = new RandomPathwayProgress()
                .setCompletedWalkables(Lists.newArrayList(walkableThree.getElementId()))
                .setChildWalkableCompletionValues(childCompletionValues)
                .setChildWalkableCompletionConfidences(childCompletionConfidences);

        ArgumentCaptor<RandomPathwayProgress> captor = ArgumentCaptor.forClass(RandomPathwayProgress.class);

        when(progressService.findLatestRandomPathway(deploymentId, pathwayId, studentId))
                .thenReturn(Mono.just(previousProgress));
        when(childProgress.getCompletion()).thenReturn(new Completion().setValue(0.1f).setConfidence(0.2f));

        spy.updateProgress(exchange);

        verify(progressService).persist(captor.capture());

        RandomPathwayProgress persistedProgress = captor.getValue();

        verifyRandomProgressValues(persistedProgress);
        verifyCompletion(persistedProgress.getCompletion(), 0.55f, 0.6f);

        assertEquals(walkableOne.getElementId(), persistedProgress.getInProgressElementId());
        assertEquals(walkableOne.getElementType(), persistedProgress.getInProgressElementType());

        assertEquals(1, persistedProgress.getCompletedWalkables().size());

        assertEquals(2, persistedProgress.getChildWalkableCompletionValues().size());
        assertEquals(2, persistedProgress.getChildWalkableCompletionConfidences().size());
    }

    @Test
    void updateProgress_hasCompleted_walkableComplete_meetExitCondition() {
        when(childProgress.getCoursewareElementId()).thenReturn(walkableOne.getElementId());
        when(childProgress.getCoursewareElementType()).thenReturn(walkableOne.getElementType());

        Map<UUID, Float> childCompletionValues = new HashMap<UUID, Float>() {
            {put(walkableThree.getElementId(), 1f);}
        };
        Map<UUID, Float> childCompletionConfidences = new HashMap<UUID, Float>() {
            {put(walkableThree.getElementId(), 1f);}
        };

        RandomPathwayProgress previousProgress = new RandomPathwayProgress()
                .setCompletedWalkables(Lists.newArrayList(walkableThree.getElementId()))
                .setChildWalkableCompletionValues(childCompletionValues)
                .setChildWalkableCompletionConfidences(childCompletionConfidences);

        ArgumentCaptor<RandomPathwayProgress> captor = ArgumentCaptor.forClass(RandomPathwayProgress.class);

        when(progressService.findLatestRandomPathway(deploymentId, pathwayId, studentId))
                .thenReturn(Mono.just(previousProgress));
        when(childProgress.getCompletion()).thenReturn(new Completion().setValue(1f).setConfidence(1f));

        spy.updateProgress(exchange);

        verify(progressService).persist(captor.capture());

        RandomPathwayProgress persistedProgress = captor.getValue();

        verifyRandomProgressValues(persistedProgress);
        verifyCompletion(persistedProgress.getCompletion(), 1f, 1f);

        assertNull(persistedProgress.getInProgressElementId());
        assertNull(persistedProgress.getInProgressElementType());

        assertEquals(2, persistedProgress.getCompletedWalkables().size());

        assertEquals(2, persistedProgress.getChildWalkableCompletionValues().size());
        assertEquals(2, persistedProgress.getChildWalkableCompletionConfidences().size());
    }

    private void verifyRandomProgressValues(final RandomPathwayProgress persistedProgress) {
        assertNotNull(persistedProgress);
        assertNotNull(persistedProgress.getId());
        assertEquals(deploymentId, persistedProgress.getDeploymentId());
        assertEquals(changeId, persistedProgress.getChangeId());
        assertEquals(pathwayId, persistedProgress.getCoursewareElementId());
        assertEquals(CoursewareElementType.PATHWAY, persistedProgress.getCoursewareElementType());
        assertEquals(studentId, persistedProgress.getStudentId());
        assertEquals(pathwayAttemptId, persistedProgress.getAttemptId());
        assertEquals(evaluationId, persistedProgress.getEvaluationId());
    }

    private void verifyCompletion(final Completion completion, final float value, final float confidence) {
        assertNotNull(completion);
        assertEquals(Float.valueOf(value), completion.getValue());
        assertEquals(Float.valueOf(confidence), completion.getConfidence());
    }
}