package com.smartsparrow.learner.service;

import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.learner.data.ProgressGateway;
import com.smartsparrow.learner.lang.ProgressNotFoundFault;
import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.learner.progress.BKTPathwayProgress;
import com.smartsparrow.learner.progress.FreePathwayProgress;
import com.smartsparrow.learner.progress.GraphPathwayProgress;
import com.smartsparrow.learner.progress.LinearPathwayProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.progress.RandomPathwayProgress;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service to perform Progress operations.
 */
@Singleton
public class ProgressService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ProgressService.class);
    private final ProgressGateway progressGateway;
    private final AcquireAttemptService acquireAttemptService;

    @Inject
    public ProgressService(ProgressGateway progressGateway, AcquireAttemptService acquireAttemptService) {
        this.progressGateway = progressGateway;
        this.acquireAttemptService = acquireAttemptService;
    }

    /**
     * Save a Progress, in the general format.
     *
     * @param progress the progress to save
     * @return a Flux void.
     */
    @Trace(async = true)
    public Flux<Void> persist(final Progress progress) {
        //
        return progressGateway.persist(progress);
    }

    /**
     * Save an Activity Progress, also persists in the General tables.
     *
     * @param progress the progress to save
     * @return a Flux void.
     */
    @Trace(async = true)
    public Flux<Void> persist(final ActivityProgress progress) {
        //
        return progressGateway.persist(progress);
    }

    /**
     * Save a Linear Pathway Progress, also persists in the General tables.
     *
     * @param progress the progress to save
     * @return a Flux void.
     */
    @Trace(async = true)
    public Flux<Void> persist(final LinearPathwayProgress progress) {
        //
        return progressGateway.persist(progress);
    }

    /**
     * Save a Free Pathway Progress, also persists in the General tables.
     *
     * @param progress the progress to save
     * @return a Flux void.
     */
    @Trace(async = true)
    public Flux<Void> persist(final FreePathwayProgress progress) {
        //
        return progressGateway.persist(progress);
    }

    /**
     * Save a graph pathway progress, also persist in the General tables.
     *
     * @param progress the graph pathway progress to save
     * @return a Flux of void.
     */
    @Trace(async = true)
    public Flux<Void> persist(final GraphPathwayProgress progress) {
        return progressGateway.persist(progress);
    }

    /**
     * Save a random pathway progress, also persist in General tables
     *
     * @param progress the random pathway progress to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final RandomPathwayProgress progress) {
        return progressGateway.persist(progress);
    }

    /**
     * Save a BKT pathway progress, also persist in General tables
     *
     * @param progress the bkt pathway progress to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final BKTPathwayProgress progress) {
        return progressGateway.persist(progress);
    }

    /**
     * Find a Progress
     *
     * @param id the id
     * @return a Mono containing a the Progress.
     */
    public Mono<Progress> find(final UUID id) {
        //
        return progressGateway.find(id);
    }

    /**
     * Find a Progress
     *
     * @param deploymentId        the deployment id
     * @param coursewareElementId the courseware element (pathway) id
     * @param studentId           the student id
     * @return a Mono containing Progress object
     * @throws ProgressNotFoundFault when the latest progress is not found
     */
    public Mono<Progress> findLatest(final UUID deploymentId, final UUID coursewareElementId, final UUID studentId) {
        //
        return progressGateway.findLatest(deploymentId, coursewareElementId, studentId)
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ProgressNotFoundFault(String.format(
                            "progress not found for deployment %s, element %s, student %s",
                            deploymentId, coursewareElementId, studentId
                    ));
                });
    }

    /**
     * Find a Linear pathway progress,
     * NB: This should not be impacted by a change id; if the underlying courseware changes, it should not "reset" progress.
     *
     * @param deploymentId the deployment id
     * @param pathwayId    the courseware element (pathway) id
     * @param studentId    the student id
     * @return a Flux containing linear pathway progress, ordered newest to oldest.
     */
    public Flux<LinearPathwayProgress> findLinearPathway(final UUID deploymentId,
                                                         final UUID pathwayId,
                                                         final UUID studentId) {
        //
        return progressGateway.findLinearPathway(deploymentId, pathwayId, studentId);
    }

    /**
     * Find the latest Linear pathway progress,
     * NB: This should not be impacted by a change id; if the underlying courseware changes, it should not "reset" progress.
     *
     * @param deploymentId the deployment id
     * @param pathwayId    the courseware element (pathway) id
     * @param studentId    the student id
     * @return a Mono containing the latest linear pathway progress
     */
    @Trace(async = true)
    public Mono<LinearPathwayProgress> findLatestLinearPathway(final UUID deploymentId,
                                                               final UUID pathwayId,
                                                               final UUID studentId) {

        return acquireAttemptService.acquireLatestPathwayAttempt(deploymentId, pathwayId, studentId)
                .flatMap(attempt ->
                        progressGateway.findLatestLinearPathway(deploymentId, pathwayId, studentId)
                                .filter(progress -> progress.getAttemptId().equals(attempt.getId()))
                );
    }

    /**
     * Find the latest graph pathway progress
     *
     * @param deploymentId the deployment id
     * @param pathwayId    the pathway id
     * @param studentId    the student id
     * @return a mono containing the latest graph pathway progress or an empty stream when the progress is not found
     */
    @Trace(async = true)
    public Mono<GraphPathwayProgress> findLatestGraphPathway(final UUID deploymentId,
                                                             final UUID pathwayId,
                                                             final UUID studentId) {
        return acquireAttemptService.acquireLatestPathwayAttempt(deploymentId, pathwayId, studentId)
                .flatMap(attempt -> progressGateway.findLatestGraphPathway(deploymentId, pathwayId, studentId)
                        .filter(progress -> progress.getAttemptId().equals(attempt.getId())));
    }


    /**
     * Find an Activity Progress
     *
     * @param deploymentId the deployment id
     * @param activityId   the courseware element (pathway) id
     * @param studentId    the student id
     * @return a Flux containing activity progress, ordered newest to oldest.
     */
    public Flux<ActivityProgress> findActivity(final UUID deploymentId, final UUID activityId, final UUID studentId) {
        //
        return progressGateway.findActivity(deploymentId, activityId, studentId);
    }

    /**
     * Find an Activity Progress
     *
     * @param deploymentId the deployment id
     * @param activityId   the courseware element (pathway) id
     * @param studentId    the student id
     * @return a Mono containing the latest linear pathway progress
     */
    @Trace(async = true)
    public Mono<ActivityProgress> findLatestActivity(final UUID deploymentId,
                                                     final UUID activityId,
                                                     final UUID studentId) {
        log.info("findLatestActivity() :: " + deploymentId + ", " + activityId + ", " + studentId);
        return progressGateway.findLatestActivity(deploymentId, activityId, studentId);
    }

    /**
     * Find the latest Free Pathway Progress
     *
     * @param deploymentId the deployment id
     * @param pathwayId    the courseware element (pathway) id
     * @param studentId    the student id
     * @return a Mono containing the latest free pathway progress
     */
    public Flux<FreePathwayProgress> findFreePathway(final UUID deploymentId,
                                                     final UUID pathwayId,
                                                     final UUID studentId) {
        //
        return progressGateway.findFreePathway(deploymentId, pathwayId, studentId);
    }

    /**
     * Find the latest Free Pathway Progress
     *
     * @param deploymentId the deployment id
     * @param pathwayId    the courseware element (pathway) id
     * @param studentId    the student id
     * @return a Mono containing the latest free pathway progress
     */
    @Trace(async = true)
    public Mono<FreePathwayProgress> findLatestFreePathway(final UUID deploymentId,
                                                           final UUID pathwayId,
                                                           final UUID studentId) {
        //
        return acquireAttemptService.acquireLatestPathwayAttempt(deploymentId, pathwayId, studentId)
                .flatMap(attempt ->
                        progressGateway.findLatestFreePathway(deploymentId, pathwayId, studentId)
                                .filter(progress -> progress.getAttemptId().equals(attempt.getId()))
                );
    }

    /**
     * Find the latest random pathway progress
     *
     * @param deploymentId the deployment id
     * @param pathwayId    the pathway id
     * @param studentId    the student id
     * @return a mono containing the latest random pathway progress or an empty stream when the progress is not found
     */
    @Trace(async = true)
    public Mono<RandomPathwayProgress> findLatestRandomPathway(final UUID deploymentId,
                                                               final UUID pathwayId,
                                                               final UUID studentId) {
        return acquireAttemptService.acquireLatestPathwayAttempt(deploymentId, pathwayId, studentId)
                .flatMap(attempt -> progressGateway.findLatestRandomPathway(deploymentId, pathwayId, studentId)
                        .filter(progress -> progress.getAttemptId().equals(attempt.getId())));
    }

    /**
     * Find the latest BKT pathway progress
     *
     * @param deploymentId the deployment id
     * @param pathwayId    the pathway id
     * @param studentId    the student id
     * @param n            the number of latest progresses to fetch
     * @return a flux containing the latest BKT pathway progress or an empty stream when the progress is not found
     */
    @Trace(async = true)
    public Flux<BKTPathwayProgress> findLatestNBKTPathway(final UUID deploymentId,
                                                          final UUID pathwayId,
                                                          final UUID studentId,
                                                          final int n) {
        return acquireAttemptService.acquireLatestPathwayAttempt(deploymentId, pathwayId, studentId)
                .flux()
                // maintain ordering
                .concatMap(attempt -> progressGateway.findLatestNBKTPathway(deploymentId, pathwayId, studentId, n)
                        .filter(progress -> progress.getAttemptId().equals(attempt.getId())));
    }
}
