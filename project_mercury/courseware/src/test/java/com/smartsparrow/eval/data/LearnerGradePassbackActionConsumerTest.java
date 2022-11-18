package com.smartsparrow.eval.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.smartsparrow.eval.action.outcome.LTIData;
import com.smartsparrow.learner.data.GradePassbackAssignment;
import com.smartsparrow.learner.data.GradePassbackItem;
import com.smartsparrow.learner.data.GradePassbackProgressType;
import com.smartsparrow.learner.wiring.GradePassbackConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.outcome.GradePassbackAction;
import com.smartsparrow.eval.action.outcome.GradePassbackActionContext;
import com.smartsparrow.eval.action.progress.EmptyActionResult;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.service.GradePassbackService;

import reactor.core.publisher.Mono;

public class LearnerGradePassbackActionConsumerTest {

    @InjectMocks
    private LearnerGradePassbackActionConsumer consumer;

    @Mock
    private GradePassbackConfig gradePassbackConfig;

    @Mock
    private GradePassbackService gradePassbackService;

    @Mock
    private GradePassbackAction action;

    private LearnerEvaluationResponseContext responseContext;

    @Mock
    private LearnerEvaluationResponseContext mockResponseContext;

    @Mock
    private RedissonReactiveClient redissonReactiveClient;

    @Mock
    private RBucketReactive<Map<String, String>> redissonReactiveBucket;

    private LTIData ltiData;

    private List<CoursewareElement> ancestry = Lists.newArrayList(
            CoursewareElement.from(UUIDs.timeBased(), CoursewareElementType.INTERACTIVE),
            CoursewareElement.from(UUIDs.timeBased(), CoursewareElementType.ACTIVITY)
    );

    private Double value = 0.5;

    private UUID deploymentId = UUID.randomUUID();
    private UUID changeId = UUID.randomUUID();
    private UUID studentId = UUID.randomUUID();
    private UUID elementId = UUID.randomUUID();
    private CoursewareElementType elementType = CoursewareElementType.INTERACTIVE;
    private CoursewareElement coursewareElement = CoursewareElement.from(elementId, elementType);
    private UUID cohortId = UUID.randomUUID();
    private String outcomeServiceUrl = "https://basic.outcome.lis";
    private String outcomeServiceUrl2 = "https://basic.outcome.lis/MasteringBiology/api/grade";
    private Deployment deployment = new Deployment().setId(deploymentId).setChangeId(changeId).setCohortId(cohortId);
    private GradePassbackActionContext actionContext = new GradePassbackActionContext().setValue(value).setElementId(elementId).setElementType(elementType);
    private LearnerEvaluationRequest request = new LearnerEvaluationRequest().setDeployment(deployment).setStudentId(studentId);
    private LearnerEvaluationResponse response = new LearnerEvaluationResponse().setEvaluationRequest(request);
    private final static Integer scoreValue = 90;
    private static final UUID rootElementId = UUID.randomUUID();


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(action.getType()).thenReturn(Action.Type.GRADE_PASSBACK);
        when(action.getResolvedValue()).thenReturn(value);
        when(action.getContext()).thenReturn(actionContext);

        responseContext = new LearnerEvaluationResponseContext();
        Map<String, String> ltiParams = new HashMap<>();
        ltiParams.put("user_id", "1234");
        ltiParams.put("custom_platform_assignment_id", "5657575");
        ltiParams.put("custom_sms_course_id", "87868676767");
        ltiParams.put("custom_attempt_limit", "1");
        ltiParams.put("custom_grading_method", "accuracy");
        ltiParams.put("custom_product_discipline", "MasteringBiology");
        ltiParams.put("roles", "Learner");

        String cacheName = String.format("ltiParams:account:/%s:deploymentId:/%s", studentId, deploymentId);
        when(redissonReactiveBucket.get())
                .thenReturn(Mono.just(ltiParams));
        doReturn(redissonReactiveBucket).when(redissonReactiveClient).getBucket(eq(cacheName));

        responseContext.setAncestry(ancestry).setResponse(response);

        when(gradePassbackConfig.getMasteringGradeSyncUrl()).thenReturn(outcomeServiceUrl);

        GradePassbackAssignment gradePassbackAssignment = new GradePassbackAssignment();
        List<GradePassbackItem> itemScores = new ArrayList<>();
        GradePassbackItem gradePassbackItem = new GradePassbackItem();
        gradePassbackItem.setItemURN("/items/"+ UUID.randomUUID());
        gradePassbackItem.setScore(scoreValue.floatValue());
        gradePassbackItem.setItemId(UUID.randomUUID().toString().hashCode());
        gradePassbackItem.setCallerCode("BRONTE_INTERACTIVE_LAB");
        itemScores.add(gradePassbackItem);
        gradePassbackAssignment.setAttemptNo(1).setCallerCode("BRONTE_INTERACTIVE_LAB").setAssignmentId(2323423).setCourseId("5657577").setCorrelationId(2147483649L)
                .setItemScore(itemScores).setUserId("24545").setRootElementId(rootElementId).setAssignmentProgress(GradePassbackProgressType.inprogress);

        ltiData = new LTIData().setUserId("1234").setAttemptLimit(1).setAssignmentId(5657575).setCourseId("87868676767")
                .setCustomGradingMethod("accuracy").setDiscipline("MasteringBiology").setRole("Learner");
        when(gradePassbackService.getGradePassbackAssignment(any(UUID.class), any(UUID.class), any(UUID.class), any(
                LTIData.class)))
                .thenReturn(Mono.just(gradePassbackAssignment));

        when(gradePassbackService.saveOutcome(eq(deploymentId), eq(changeId), eq(studentId), eq(coursewareElement),
                eq(outcomeServiceUrl2), any(GradePassbackAssignment.class))).thenReturn(Mono.just(new RequestNotification()));

    }


    @Test
    void getActionConsumerOptions_notAsync() {
        ActionConsumerOptions options = consumer.getActionConsumerOptions()
                .block();
        assertNotNull(options);
        assertTrue(options.isAsync());
    }

    @Test
    void consume() {
        final EmptyActionResult result = consumer.consume(action, responseContext)
                .block();

        assertNotNull(result);
        assertEquals(action, result.getValue());
        assertEquals(Action.Type.GRADE_PASSBACK, result.getType());

        verify(gradePassbackService).getGradePassbackAssignment(deploymentId, studentId, coursewareElement.getElementId(), ltiData);

        verify(gradePassbackService).saveOutcome(eq(deploymentId), eq(changeId), eq(studentId),
                eq(coursewareElement), eq(outcomeServiceUrl2), any(GradePassbackAssignment.class));
    }

    @Test
    void consume_emptyAncestry() {
        when(mockResponseContext.getAncestry()).thenReturn(Collections.emptyList());
        IllegalStateFault fault = assertThrows(IllegalStateFault.class,
                                               () -> consumer.consume(action, mockResponseContext));
        assertEquals("error processing GRADE_PASSBACK action. Ancestry cannot be empty", fault.getMessage());
    }

    @Test
    void consume_emptyNotification() {
        when(gradePassbackService.saveOutcome(eq(deploymentId), eq(changeId), eq(studentId), eq(coursewareElement),
                                              eq(outcomeServiceUrl2), any(GradePassbackAssignment.class))).thenReturn(Mono.empty());
        final EmptyActionResult result = consumer.consume(action, responseContext)
                .block();

        assertNotNull(result);
        assertEquals(action, result.getValue());
        assertEquals(Action.Type.GRADE_PASSBACK, result.getType());

        verify(gradePassbackService).saveOutcome(eq(deploymentId), eq(changeId), eq(studentId),
                eq(coursewareElement), eq(outcomeServiceUrl2), any(GradePassbackAssignment.class));
    }

}
