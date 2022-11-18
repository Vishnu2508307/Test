package com.smartsparrow.learner.service;

import static com.smartsparrow.dataevent.RouteUri.LEARNER_PROGRESS_UPDATE;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Maps;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.StudentScope;
import com.smartsparrow.learner.event.RestartActivityEventMessage;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.lang.LearnerPathwayNotFoundFault;
import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.learner.progress.Completion;

import reactor.core.publisher.Mono;

@Singleton
public class RestartActivityService {

    private final ProgressService progressService;
    private final DeploymentService deploymentService;
    private final CamelReactiveStreamsService camel;
    private final LearnerCoursewareService learnerCoursewareService;
    private final AcquireAttemptService acquireAttemptService;
    private final AttemptService attemptService;
    private final StudentScopeService studentScopeService;

    @Inject
    public RestartActivityService(ProgressService progressService,
                                  DeploymentService deploymentService,
                                  CamelReactiveStreamsService camel,
                                  LearnerCoursewareService learnerCoursewareService,
                                  AcquireAttemptService acquireAttemptService,
                                  AttemptService attemptService,
                                  StudentScopeService studentScopeService) {
        this.progressService = progressService;
        this.deploymentService = deploymentService;
        this.camel = camel;
        this.learnerCoursewareService = learnerCoursewareService;
        this.acquireAttemptService = acquireAttemptService;
        this.attemptService = attemptService;
        this.studentScopeService = studentScopeService;
    }

    /**
     * Restart activity.
     * Bump up the latest attempt and create zero progress for the activity.
     * Trigger camel events to update progress of elements upwards
     *
     * @param deploymentId the deployment the activity belongs to
     * @param activityId   the activity to restart
     * @param studentId    the student which progress on activity should be restarted
     * @return Mono of new progress
     */
    @Trace(async = true)
    public Mono<ActivityProgress> restartActivity(UUID deploymentId, UUID activityId, UUID studentId) {
        affirmNotNull(deploymentId, "deploymentId is required");
        affirmNotNull(activityId, "activityId is required");
        affirmNotNull(studentId, "studentId is required");

        //bump up the activity
        Mono<Attempt> newAttempt = acquireAttemptService.acquireLatestActivityAttempt(deploymentId, activityId, studentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMap(attempt -> attemptService.newAttempt(deploymentId, studentId, CoursewareElementType.ACTIVITY, activityId,
                        attempt.getParentId(), attempt.getValue() + 1));

        Mono<DeployedActivity> deployment = deploymentService.findDeployment(deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext());

        //create new zero progress for the activity and trigger progress recalculation
        return Mono.zip(newAttempt, deployment).map(tuple2 ->
                //update progresss on activity
                new ActivityProgress() //
                        .setId(UUIDs.timeBased()) //
                        .setAttemptId(tuple2.getT1().getId()) //
                        .setChangeId(tuple2.getT2().getChangeId()) //
                        .setChildWalkableCompletionValues(Maps.newHashMap()) //
                        .setChildWalkableCompletionConfidences(Maps.newHashMap()) //
                        .setCompletion(new Completion().setValue(0f).setConfidence(0f)) //
                        .setCoursewareElementId(activityId) //
                        .setCoursewareElementType(CoursewareElementType.ACTIVITY) //
                        .setDeploymentId(deploymentId) //
                        .setEvaluationId(null) // //todo what is evaluationId here?
                        .setStudentId(studentId))
                .flatMap(progress -> progressService.persist(progress).then(Mono.just(progress)))
                .flatMap(progress -> updateProgressUpward(activityId, deploymentId, progress).thenReturn(progress))
                .flatMap(progress -> updateScopeDownward(activityId, deploymentId, progress.getStudentId()).thenReturn(progress))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    private Mono<Exchange> updateProgressUpward(UUID activityId, UUID deploymentId, ActivityProgress activityProgress) {
        return learnerCoursewareService.getAncestry(deploymentId, activityId, CoursewareElementType.ACTIVITY)
                //check that activity has parents, in ancestryList first element is always current activity
                .filter(ancestryList -> ancestryList.size() > 1)
                //propagate events to update parent's progress
                .map(ancestryList -> {
                    RestartActivityEventMessage restartEventMessage = new RestartActivityEventMessage()
                            .setStudentId(activityProgress.getStudentId())
                            .setAttemptId(activityProgress.getAttemptId())
                            .setChangeId(activityProgress.getChangeId())
                            .setDeploymentId(deploymentId)
                            .setAncestryList(ancestryList)
                            .setProducingClientId(null);

                    UpdateCoursewareElementProgressEvent event = new UpdateCoursewareElementProgressEvent()
                            .setUpdateProgressEvent(restartEventMessage)
                            .setElement(ancestryList.get(1));
                    event.getEventProgress().add(activityProgress);
                    return event;
                }).flatMap(event -> {
                        return Mono.from(camel.to("direct:" + LEARNER_PROGRESS_UPDATE, event));

                })
                .onErrorResume(LearnerPathwayNotFoundFault.class,
                        //if parent pathway no found, it is ok, it is the top activity
                        ex -> Mono.empty());
    }

    /**
     * Reset the initialised scoped in the subtree for the supplied activity. Create a new {@link StudentScope} for all
     * the initialised student scope present in the subtree.
     *
     * @param activityId the activity to reset the scope from
     * @param deploymentId the deployment id
     * @param studentId the student to reset the scope for
     * @return a mono list of newly created student scopes or an empty list if no scopes required reset
     */
    private Mono<List<StudentScope>> updateScopeDownward(final UUID activityId, final UUID deploymentId, final UUID studentId) {
        return studentScopeService.resetScopesFor(deploymentId, activityId, studentId)
                .collectList();
    }
}
