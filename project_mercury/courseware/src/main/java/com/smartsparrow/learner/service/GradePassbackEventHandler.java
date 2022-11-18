package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.eval.action.outcome.GradePassbackAction;
import com.smartsparrow.eval.action.outcome.GradePassbackActionContext;
import com.smartsparrow.eval.action.outcome.LTIData;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.wiring.GradePassbackConfig;

import reactor.core.publisher.Mono;

public class GradePassbackEventHandler {

    private static final Logger log = LoggerFactory.getLogger(GradePassbackEventHandler.class);

    private final GradePassbackConfig gradePassbackConfig;
    private final GradePassbackService gradePassbackService;
    private final RedissonReactiveClient redissonReactiveClient;

    @Inject
    public GradePassbackEventHandler(final GradePassbackConfig gradePassbackConfig,
                                     final GradePassbackService gradePassbackService,
                                     final RedissonReactiveClient redissonReactiveClient) {
        this.gradePassbackConfig = gradePassbackConfig;
        this.gradePassbackService = gradePassbackService;
        this.redissonReactiveClient = redissonReactiveClient;
    }

    @Handler
    public void handle(Exchange exchange) {
        // preparing required variables
        final EvaluationEventMessage eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);
        final GradePassbackAction action = exchange.getIn().getBody(GradePassbackAction.class);
        final List<CoursewareElement> ancestry = eventMessage.getAncestryList();

        if (ancestry.isEmpty()) {
            // this is not possible! at least 1 element is always present in the ancestry
            throw new IllegalStateFault("error processing GRADE_PASSBACK action. Ancestry cannot be empty");
        }

        if (log.isDebugEnabled()) {
            log.debug("processing change score action: {} for student {}", action, eventMessage.getStudentId());
        }

        //
        UUID deploymentId = eventMessage.getDeploymentId();
        UUID changeId = eventMessage.getChangeId();
        UUID studentId = eventMessage.getStudentId();
        log.info("handle() :: studentId: "+ studentId);
        GradePassbackActionContext context = action.getContext();
        CoursewareElement coursewareElement = CoursewareElement.from(context.getElementId(), context.getElementType());
        String outcomeServiceUrl = gradePassbackConfig.getMasteringGradeSyncUrl();
        LTIData ltiData = populateLTIData(studentId, deploymentId);

        gradePassbackService.getGradePassbackAssignment(deploymentId, studentId, coursewareElement.getElementId(), ltiData)
                .flatMap(gradePassbackAssignment -> {
                    if(ltiData != null &&  ltiData.getAttemptLimit() != null && gradePassbackAssignment.getAttemptNo() != null
                            && gradePassbackAssignment.getAttemptNo() <= ltiData.getAttemptLimit()
                            && ltiData.getCustomGradingMethod() != null
                            && !ltiData.getCustomGradingMethod().equalsIgnoreCase("ungraded")
                            && ltiData.getRole() != null && !ltiData.getRole().equalsIgnoreCase("Instructor")) {
                        log.info("handle() :: invoking saveOutcome() method");

                        String discipline = ltiData.getDiscipline();
                        String outcomeServiceUrlUpdated = outcomeServiceUrl.concat("/" + discipline + "/api/grade");
                        return gradePassbackService.saveOutcome(
                                deploymentId,
                                changeId,
                                studentId,
                                coursewareElement,
                                outcomeServiceUrlUpdated,
                                gradePassbackAssignment);
                    } else {
                        log.info("handle() :: Not invoking saveOutcome() method. deploymentId: {}, studentId: {}, changeId: {}",
                                 deploymentId, studentId, changeId);
                        return Mono.just(new RequestNotification());
                    }
                })
                .doOnError(throwable -> {
                    if (log.isErrorEnabled()) {
                        log.error("error occurred during grade passback", throwable);
                    }
                })
                // block the call, this is important especially if the next handled action is another
                // GRADE_PASSBACK action
                .block();
    }

    private LTIData populateLTIData(UUID accountId, UUID deploymentId) {
        //retrieving ltiParams from cache
        String cacheName = String.format("ltiParams:account:/%s:deploymentId:/%s", accountId, deploymentId);
        RBucketReactive<Map<String, String>> bucket = redissonReactiveClient.getBucket(cacheName);
        Map<String, String> ltiParams = bucket.get().block();

        try {
            log.info("GradePassbackEventHandler.populateLTIData() :: LTIData :: " + ltiParams);
            if (ltiParams != null && ltiParams.size() > 0) {
                LTIData ltiData = new LTIData();
                return ltiData.setUserId(ltiParams.get("user_id"))
                        .setAssignmentId(ltiParams.get("custom_platform_assignment_id") != null ? Integer.valueOf(
                                ltiParams.get(
                                        "custom_platform_assignment_id")) : null)
                        .setCourseId(ltiParams.get("custom_sms_course_id"))
                        .setAttemptLimit(ltiParams.get("custom_attempt_limit") != null ? Integer.valueOf(ltiParams.get(
                                "custom_attempt_limit")) : null)
                        .setCustomGradingMethod(ltiParams.get("custom_grading_method"))
                        .setDiscipline(ltiParams.get("custom_product_discipline"))
                        .setRole(ltiParams.get("roles"));
            } else {
                log.info("LTI params not available in cache");
            }
        } catch (Exception ex) {
            log.info("Error occurred while retrieving of ltiParams from cache");
        }

        return null;
    }
}
