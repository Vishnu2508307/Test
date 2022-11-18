package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static com.smartsparrow.courseware.service.CoursewareElementDataStub.build;
import static com.smartsparrow.learner.event.EvaluationEventMessageDataStub.evaluationEventMessage;
import static com.smartsparrow.learner.service.EvaluationDataStub.buildEvaluationResult;
import static com.smartsparrow.learner.service.GradePassbackActionDataStub.buildGradePassbackAction;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.outcome.GradePassbackAction;
import com.smartsparrow.eval.action.outcome.LTIData;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.GradePassbackAssignment;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.wiring.GradePassbackConfig;

import reactor.core.publisher.Mono;

public class GradePassbackEventHandlerTest {

    @InjectMocks
    private GradePassbackEventHandler gradePassbackEventHandler;

    @Mock
    private GradePassbackConfig gradePassbackConfig;

    @Mock
    private GradePassbackService gradePassbackService;

    @Mock
    private RedissonReactiveClient redissonReactiveClient;

    @Mock
    private RBucketReactive<Map<String, String>> redissonReactiveBucket;

    private Exchange exchange;
    private EvaluationEventMessage eventMessage;
    private UUID clientId = UUID.randomUUID();
    private Double value = 1.0d;
    private UUID deploymentId = UUID.randomUUID();
    private UUID changeId = UUID.randomUUID();
    private UUID studentId = UUID.randomUUID();
    private UUID cohortId = UUID.randomUUID();
    private String outcomeServiceUrl = "https://basic.outcome.lis";
    private Deployment deployment = new Deployment().setId(deploymentId).setChangeId(changeId).setCohortId(cohortId);

    Map<String, String> ltiParams = new HashMap<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Message message = mock(Message.class);
        exchange = mock(Exchange.class);

        when(exchange.getIn()).thenReturn(message);

        EvaluationResult evaluationResult = buildEvaluationResult(true);
        GradePassbackAction action = buildGradePassbackAction(value, evaluationResult.getCoursewareElementId(), CoursewareElementType.INTERACTIVE);

        when(message.getBody(GradePassbackAction.class)).thenReturn(action);

        List<CoursewareElement> ancestry = Lists.newArrayList(
                new CoursewareElement()
                        .setElementId(evaluationResult.getCoursewareElementId())
                        .setElementType(CoursewareElementType.INTERACTIVE),
                build(CoursewareElementType.PATHWAY),
                build(CoursewareElementType.ACTIVITY)
        );

        // ensure sublist works as expected
        List<CoursewareElement> subList = ancestry.subList(1, ancestry.size());
        assertEquals(2, subList.size());
        assertFalse(subList.contains(ancestry.get(0)));

        eventMessage = evaluationEventMessage(evaluationResult, ancestry, clientId, studentId);
        eventMessage.setDeployment(deployment);

        when(exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class)).thenReturn(eventMessage);

        when(gradePassbackConfig.getMasteringGradeSyncUrl()).thenReturn(outcomeServiceUrl);

        when(gradePassbackService.saveOutcome(any(UUID.class), any(UUID.class), any(UUID.class), any(CoursewareElement.class),
                any(String.class), any(GradePassbackAssignment.class))).thenReturn(Mono.just(new RequestNotification()));

        GradePassbackAssignment gradePassbackAssignment = new GradePassbackAssignment();
        gradePassbackAssignment.setAttemptNo(1);
        when(gradePassbackService.getGradePassbackAssignment(any(UUID.class), any(UUID.class), any(UUID.class), any(
                LTIData.class)))
                .thenReturn(Mono.just(gradePassbackAssignment));

        LTIData ltiData = new LTIData();
        ltiData.setUserId("12345").setAssignmentId(5657576).setCourseId("797978979");
        ltiParams.put("user_id", "1234");
        ltiParams.put("custom_platform_assignment_id", "5657575");
        ltiParams.put("custom_sms_course_id", "87868676767");
        ltiParams.put("custom_attempt_limit", "1");
        ltiParams.put("custom_grading_method", "accuracy");
        ltiParams.put("custom_product_discipline", "MasteringBiology");
        ltiParams.put("roles", "Learner");

        eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);

        String cacheName = String.format("ltiParams:account:/%s:deploymentId:/%s", studentId, eventMessage.getDeploymentId());
        when(redissonReactiveBucket.get())
                .thenReturn(Mono.just(ltiParams));
        doReturn(redissonReactiveBucket).when(redissonReactiveClient).getBucket(eq(cacheName));
    }

    @Test
    void handle_emptyAncestry() {
        eventMessage.setAncestryList(new ArrayList<>());

        IllegalStateFault e = assertThrows(IllegalStateFault.class, () -> gradePassbackEventHandler.handle(exchange));

        assertNotNull(e);
        assertEquals("error processing GRADE_PASSBACK action. Ancestry cannot be empty", e.getMessage());
    }

    @Test
    void handle() {
        gradePassbackEventHandler.handle(exchange);

        verify(gradePassbackService).getGradePassbackAssignment(any(UUID.class), any(UUID.class), any(UUID.class), any(LTIData.class));

        verify(gradePassbackService).saveOutcome(any(UUID.class), any(UUID.class), any(UUID.class), any(CoursewareElement.class),
                any(String.class), any(GradePassbackAssignment.class));
    }

    @Test
    void handle_role_instructor() {
        ltiParams.put("roles", "Instructor");
        eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);
        String cacheName = String.format("ltiParams:account:/%s:deploymentId:/%s", studentId, eventMessage.getDeploymentId());
        doReturn(redissonReactiveBucket).when(redissonReactiveClient).getBucket(eq(cacheName));

        gradePassbackEventHandler.handle(exchange);

        verify(gradePassbackService).getGradePassbackAssignment(any(UUID.class), any(UUID.class), any(UUID.class), any(LTIData.class));
    }
}
