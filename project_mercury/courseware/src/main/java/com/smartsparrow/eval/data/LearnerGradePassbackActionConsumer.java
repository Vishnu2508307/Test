package com.smartsparrow.eval.data;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.eval.action.outcome.GradePassbackAction;
import com.smartsparrow.eval.action.outcome.GradePassbackActionContext;
import com.smartsparrow.eval.action.outcome.LTIData;
import com.smartsparrow.eval.action.progress.EmptyActionResult;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.learner.service.GradePassbackService;
import com.smartsparrow.learner.wiring.GradePassbackConfig;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;

/**
 * Grade passback action consumer
 */
public class LearnerGradePassbackActionConsumer implements ActionConsumer<GradePassbackAction, EmptyActionResult> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerGradePassbackActionConsumer.class);

    private final ActionConsumerOptions options;
    private final GradePassbackConfig gradePassbackConfig;
    private final GradePassbackService gradePassbackService;
    private final RedissonReactiveClient redissonReactiveClient;

    @Inject
    public LearnerGradePassbackActionConsumer(final GradePassbackConfig gradePassbackConfig,
                                              final GradePassbackService gradePassbackService,
                                              final RedissonReactiveClient redissonReactiveClient) {
        this.gradePassbackConfig = gradePassbackConfig;
        this.gradePassbackService = gradePassbackService;
        this.redissonReactiveClient = redissonReactiveClient;
        this.options = new ActionConsumerOptions()
                .setAsync(true);
    }

    @Trace(async = true)
    @Override
    public Mono<EmptyActionResult> consume(GradePassbackAction gradePassbackAction, LearnerEvaluationResponseContext responseContext) {

        log.info("In LearnerGradePassbackActionConsumer.consume() method");
        // preparing required variables

        final List<CoursewareElement> ancestry = responseContext.getAncestry();

        if (ancestry.isEmpty()) {
            // this is not possible! at least 1 element is always present in the ancestry
            throw new IllegalStateFault("error processing GRADE_PASSBACK action. Ancestry cannot be empty");
        }

        //
        final LearnerEvaluationResponse response = responseContext.getResponse();
        final LearnerEvaluationRequest request = response.getEvaluationRequest();
        UUID deploymentId = request.getDeployment().getId();
        UUID changeId = request.getDeployment().getChangeId();
        UUID studentId = request.getStudentId();
        log.info("consume() :: studentId: "+ studentId);
        GradePassbackActionContext context = gradePassbackAction.getContext();
        CoursewareElement coursewareElement = CoursewareElement.from(context.getElementId(), context.getElementType());
        String outcomeServiceUrl = gradePassbackConfig.getMasteringGradeSyncUrl();
        LTIData ltiData = populateLTIData(studentId, deploymentId);

        return gradePassbackService.getGradePassbackAssignment(deploymentId, studentId, coursewareElement.getElementId(), ltiData)
                .flatMap(gradePassbackAssignment -> {
                    if(ltiData != null && ltiData.getAttemptLimit() != null && gradePassbackAssignment.getAttemptNo() != null
                            && gradePassbackAssignment.getAttemptNo() <= ltiData.getAttemptLimit()
                            && ltiData.getCustomGradingMethod() != null
                            && !ltiData.getCustomGradingMethod().equalsIgnoreCase("ungraded")
                            && ltiData.getRole() != null && !ltiData.getRole().equalsIgnoreCase("Instructor")) {
                        log.info("consume() :: invoking saveOutcome() method");
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
                        log.info("consume() :: Not invoking saveOutcome() method. attemptNo: {}, gradingMethod: {}, role: {}",
                                 gradePassbackAssignment.getAttemptNo(), ltiData.getCustomGradingMethod(), ltiData.getRole());
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
                .then(Mono.just(new EmptyActionResult(gradePassbackAction)));
    }

    @Override
    public Mono<ActionConsumerOptions> getActionConsumerOptions() {
        return Mono.just(options);
    }

    private LTIData populateLTIData(UUID accountId, UUID deploymentId) {
        //retrieving ltiParams from cache
        String cacheName = String.format("ltiParams:account:/%s:deploymentId:/%s", accountId, deploymentId);
        RBucketReactive<Map<String, String>> bucket = redissonReactiveClient.getBucket(cacheName);
        Map<String, String> ltiParams = bucket.get().block();

        try {
            log.info("LearnerGradePassbackActionConsumer.populateLTIData() :: LTIData :: " + ltiParams);
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
