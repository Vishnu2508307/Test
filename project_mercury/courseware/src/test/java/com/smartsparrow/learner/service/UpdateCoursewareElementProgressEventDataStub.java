package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static com.smartsparrow.learner.service.EvaluationDataStub.buildEvaluationResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.PreloadPathway;
import com.smartsparrow.eval.action.progress.ProgressActionContext;
import com.smartsparrow.eval.action.progress.ProgressionType;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationActionState;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.progress.WalkableProgress;

import reactor.core.publisher.Flux;

public class UpdateCoursewareElementProgressEventDataStub {

    public static final UUID elementId = UUID.randomUUID();
    public static final UUID attemptId = UUID.randomUUID();
    public static final UUID changeId = UUID.randomUUID();
    public static final UUID deploymentId = UUID.randomUUID();
    public static final UUID evaluationId = UUID.randomUUID();
    public static final UUID studentId = UUID.randomUUID();
    public static final UUID learnerPathwayId = UUID.randomUUID();
    public static final String producingClientId = UUID.randomUUID().toString();

    public static UpdateCoursewareElementProgressEvent progressEvent(@Nullable CoursewareElement parentElement,
                                                                     @Nonnull CoursewareElementType elementType,
                                                                     @Nonnull ProgressionType progressionType,
                                                                     boolean completed) {
        CoursewareElement element = new CoursewareElement()
                .setElementId(elementId)
                .setElementType(elementType);

        Attempt attempt = new Attempt()
                .setId(attemptId)
                .setStudentId(studentId)
                .setCoursewareElementId(elementId)
                .setCoursewareElementType(elementType)
                .setDeploymentId(deploymentId)
                .setValue(1)
                .setParentId((parentElement != null ? parentElement.getElementId() : null)); // more

        EvaluationResult evaluationResult = evaluationResult(completed, attempt, elementId);

        List<CoursewareElement> ancestry = (parentElement != null ? Lists.newArrayList(element, parentElement) : Lists.newArrayList(element));

        EvaluationEventMessage evaluationEvent = new EvaluationEventMessage()
                .setStudentId(studentId)
                .setProducingClientId(producingClientId)
                .setEvaluationResult(evaluationResult)
                .setAncestryList(ancestry);

        Completion completion = new Completion()
                .setValue(1f)
                .setConfidence(1f);

        Progress progress = new WalkableProgress()
                .setAttemptId(attemptId)
                .setChangeId(changeId)
                .setDeploymentId(deploymentId)
                .setCoursewareElementId(elementId)
                .setCoursewareElementType(elementType)
                .setStudentId(studentId)
                .setEvaluationId(evaluationId)
                .setCompletion(completion);

        List<Progress> eventProgress = Lists.newArrayList(progress);

        return new UpdateCoursewareElementProgressEvent()
                .setElement(element)
                .setUpdateProgressEvent(evaluationEvent)
                .setEventProgress(eventProgress);
    }

    private static EvaluationResult evaluationResult(boolean completed, Attempt attempt, UUID elementId) {
        Deployment deployment = new Deployment()
                .setChangeId(changeId)
                .setId(deploymentId)
                .setCohortId(UUID.randomUUID());

        return new EvaluationResult()
                .setAttemptId(attemptId)
                .setCoursewareElementId(elementId)
                .setDeployment(deployment)
                .setId(evaluationId)
                .setInteractiveComplete(completed)
                .setAttempt(attempt);
    }

    public static UpdateCoursewareElementProgressEvent progressEventCompleted(@Nullable CoursewareElement parentElement,
                                                                              @Nonnull CoursewareElementType elementType) {
        return progressEvent(parentElement, elementType, ProgressionType.INTERACTIVE_COMPLETE, true);
    }

    public static Exchange mockExchangeFrom(UpdateCoursewareElementProgressEvent event, ProgressActionContext progressActionContext) {
        EvaluationEventMessage evaluationEventMessage = new EvaluationEventMessage()
                .setEvaluationResult(buildEvaluationResult(true))
                .setEvaluationActionState(new EvaluationActionState()
                                                  .setProgressActionContext(progressActionContext)
                                                  .setCoursewareElement(CoursewareElement.from(elementId, CoursewareElementType.INTERACTIVE)));;

        Exchange mock = mock(Exchange.class);
        Message inMock = mock(Message.class);
        Message outMock = mock(Message.class);
        when(mock.getIn()).thenReturn(inMock);
        when(mock.getOut()).thenReturn(outMock);
        when(inMock.getBody(any())).thenReturn(event);
        when(mock.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class)).thenReturn(evaluationEventMessage);
        return mock;
    }

    public static Exchange mockExchangeFrom(@Nonnull UpdateCoursewareElementProgressEvent event,
                                            @Nonnull EvaluationResult evaluationResult,
                                            @Nonnull UUID elementId,
                                            @Nonnull ProgressActionContext progressActionContext) {
        EvaluationEventMessage evaluationEventMessage = new EvaluationEventMessage()
                .setEvaluationResult(evaluationResult)
                .setEvaluationActionState(new EvaluationActionState()
                        .setProgressActionContext(progressActionContext)
                        .setCoursewareElement(CoursewareElement.from(elementId, CoursewareElementType.INTERACTIVE)));

        Exchange mock = mock(Exchange.class);
        Message inMock = mock(Message.class);
        Message outMock = mock(Message.class);
        when(mock.getIn()).thenReturn(inMock);
        when(mock.getOut()).thenReturn(outMock);
        when(inMock.getBody(any())).thenReturn(event);
        when(mock.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class)).thenReturn(evaluationEventMessage);
        return mock;
    }

    public static LearnerPathway learnerPathway() {
        return new LearnerPathway() {
            @Override
            public UUID getDeploymentId() {
                return deploymentId;
            }

            @Override
            public UUID getChangeId() {
                return changeId;
            }

            @Override
            public Flux<WalkableChild> supplyRelevantWalkables(UUID studentId) {
                return Flux.empty();
            }

            @Override
            public UUID getId() {
                return learnerPathwayId;
            }

            @Override
            public PathwayType getType() {
                return PathwayType.LINEAR;
            }
            @Override
            public PreloadPathway getPreloadPathway() {
                return PreloadPathway.NONE;
            }

            @Nullable
            @Override
            public String getConfig() {
                return null;
            }
        };
    }
}
