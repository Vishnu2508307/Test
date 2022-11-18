package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.learner.progress.BKTPathwayProgress;
import com.smartsparrow.learner.progress.FreePathwayProgress;
import com.smartsparrow.learner.progress.GraphPathwayProgress;
import com.smartsparrow.learner.progress.LinearPathwayProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.progress.RandomPathwayProgress;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ProgressGateway {

    private static final Logger log = LoggerFactory.getLogger(ProgressGateway.class);

    //
    private final Session session;

    //
    private final ProgressMutator progressMutator;
    private final ProgressByCoursewareMutator progressByCoursewareMutator;
    private final ActivityProgressMutator activityProgressMutator;
    private final ActivityProgressByCoursewareMutator activityProgressByCoursewareMutator;
    private final LinearPathwayProgressMutator linearPathwayProgressMutator;
    private final LinearPathwayProgressByCoursewareMutator linearPathwayProgressByCoursewareMutator;
    private final FreePathwayProgressMutator freePathwayProgressMutator;
    private final FreePathwayProgressByCoursewareMutator freePathwayProgressByCoursewareMutator;
    private final GraphPathwayProgressMutator graphPathwayProgressMutator;
    private final GraphPathwayProgressByCoursewareMutator graphPathwayProgressByCoursewareMutator;
    private final RandomPathwayProgressMutator randomPathwayProgressMutator;
    private final RandomPathwayProgressByCoursewareMutator randomPathwayProgressByCoursewareMutator;
    private final BKTPathwayProgressMutator bktPathwayProgressMutator;
    private final BKTPathwayProgressByCoursewareMutator bktPathwayProgressByCoursewareMutator;

    //
    private final ProgressMaterializer progressMaterializer;
    private final ProgressByCoursewareMaterializer progressByCoursewareMaterializer;
    private final ActivityProgressMateralizer activityProgressMateralizer;
    private final ActivityProgressByCoursewareMaterializer activityProgressByCoursewareMaterializer;
    private final LinearPathwayProgressMaterializer linearPathwayProgressMaterializer;
    private final LinearPathwayProgressByCoursewareMaterializer linearPathwayProgressByCoursewareMaterializer;
    private final FreePathwayProgressMaterializer freePathwayProgressMaterializer;
    private final FreePathwayProgressByCoursewareMaterializer freePathwayProgressByCoursewareMaterializer;
    private final GraphPathwayProgressMaterializer graphPathwayProgressMaterializer;
    private final GraphPathwayProgressByCoursewareMaterializer graphPathwayProgressByCoursewareMaterializer;
    private final RandomPathwayProgressMaterializer randomPathwayProgressMaterializer;
    private final RandomPathwayProgressByCoursewareMaterializer randomPathwayProgressByCoursewareMaterializer;
    private final BKTPathwayProgressMaterializer bktPathwayProgressMaterializer;
    private final BKTPathwayProgressByCoursewareMaterializer bktPathwayProgressByCoursewareMaterializer;

    @Inject
    public ProgressGateway(Session session,
                           ProgressMutator progressMutator,
                           ProgressByCoursewareMutator progressByCoursewareMutator,
                           ActivityProgressMutator activityProgressMutator,
                           ActivityProgressByCoursewareMutator activityProgressByCoursewareMutator,
                           LinearPathwayProgressMutator linearPathwayProgressMutator,
                           LinearPathwayProgressByCoursewareMutator linearPathwayProgressByCoursewareMutator,
                           FreePathwayProgressMutator freePathwayProgressMutator,
                           FreePathwayProgressByCoursewareMutator freePathwayProgressByCoursewareMutator,
                           GraphPathwayProgressMutator graphPathwayProgressMutator,
                           GraphPathwayProgressByCoursewareMutator graphPathwayProgressByCoursewareMutator,
                           RandomPathwayProgressMutator randomPathwayProgressMutator,
                           RandomPathwayProgressByCoursewareMutator randomPathwayProgressByCoursewareMutator,
                           BKTPathwayProgressMutator bktPathwayProgressMutator,
                           BKTPathwayProgressByCoursewareMutator bktPathwayProgressByCoursewareMutator,
                           ProgressMaterializer progressMaterializer,
                           ProgressByCoursewareMaterializer progressByCoursewareMaterializer,
                           ActivityProgressMateralizer activityProgressMateralizer,
                           ActivityProgressByCoursewareMaterializer activityProgressByCoursewareMaterializer,
                           LinearPathwayProgressMaterializer linearPathwayProgressMaterializer,
                           LinearPathwayProgressByCoursewareMaterializer linearPathwayProgressByCoursewareMaterializer,
                           FreePathwayProgressMaterializer freePathwayProgressMaterializer,
                           FreePathwayProgressByCoursewareMaterializer freePathwayProgressByCoursewareMaterializer,
                           GraphPathwayProgressMaterializer graphPathwayProgressMaterializer,
                           GraphPathwayProgressByCoursewareMaterializer graphPathwayProgressByCoursewareMaterializer,
                           RandomPathwayProgressMaterializer randomPathwayProgressMaterializer,
                           RandomPathwayProgressByCoursewareMaterializer randomPathwayProgressByCoursewareMaterializer,
                           BKTPathwayProgressMaterializer bktPathwayProgressMaterializer,
                           BKTPathwayProgressByCoursewareMaterializer bktPathwayProgressByCoursewareMaterializer) {
        this.session = session;
        this.progressMutator = progressMutator;
        this.progressByCoursewareMutator = progressByCoursewareMutator;
        this.activityProgressMutator = activityProgressMutator;
        this.activityProgressByCoursewareMutator = activityProgressByCoursewareMutator;
        this.linearPathwayProgressMutator = linearPathwayProgressMutator;
        this.linearPathwayProgressByCoursewareMutator = linearPathwayProgressByCoursewareMutator;
        this.freePathwayProgressMutator = freePathwayProgressMutator;
        this.freePathwayProgressByCoursewareMutator = freePathwayProgressByCoursewareMutator;
        this.graphPathwayProgressMutator = graphPathwayProgressMutator;
        this.graphPathwayProgressByCoursewareMutator = graphPathwayProgressByCoursewareMutator;
        this.randomPathwayProgressMutator = randomPathwayProgressMutator;
        this.randomPathwayProgressByCoursewareMutator = randomPathwayProgressByCoursewareMutator;
        this.bktPathwayProgressMutator = bktPathwayProgressMutator;
        this.bktPathwayProgressByCoursewareMutator = bktPathwayProgressByCoursewareMutator;
        this.progressMaterializer = progressMaterializer;
        this.progressByCoursewareMaterializer = progressByCoursewareMaterializer;
        this.activityProgressMateralizer = activityProgressMateralizer;
        this.activityProgressByCoursewareMaterializer = activityProgressByCoursewareMaterializer;
        this.linearPathwayProgressMaterializer = linearPathwayProgressMaterializer;
        this.linearPathwayProgressByCoursewareMaterializer = linearPathwayProgressByCoursewareMaterializer;
        this.freePathwayProgressMaterializer = freePathwayProgressMaterializer;
        this.freePathwayProgressByCoursewareMaterializer = freePathwayProgressByCoursewareMaterializer;
        this.graphPathwayProgressMaterializer = graphPathwayProgressMaterializer;
        this.graphPathwayProgressByCoursewareMaterializer = graphPathwayProgressByCoursewareMaterializer;
        this.randomPathwayProgressMaterializer = randomPathwayProgressMaterializer;
        this.randomPathwayProgressByCoursewareMaterializer = randomPathwayProgressByCoursewareMaterializer;
        this.bktPathwayProgressMaterializer = bktPathwayProgressMaterializer;
        this.bktPathwayProgressByCoursewareMaterializer = bktPathwayProgressByCoursewareMaterializer;
    }

    /**
     * High level, non-type specific persistence of Progress.
     *
     * @param progress the progress
     * @return a void flux.
     */
    public Flux<Void> persist(final Progress progress) {
        return Mutators.execute(session, Flux.just(progressMutator.upsert(progress),
                                                   progressByCoursewareMutator.upsert(progress)))
                .doOnError(throwable -> {
                    log.error(String.format("error while persisting progress %s", progress), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist an activity progress.
     *
     * @param progress the progress
     * @return a void flux.
     */
    public Flux<Void> persist(final ActivityProgress progress) {
        return Mutators.execute(session, Flux.just(progressMutator.upsert(progress),
                                                   progressByCoursewareMutator.upsert(progress),
                                                   activityProgressMutator.upsert(progress),
                                                   activityProgressByCoursewareMutator.upsert(progress)))
                .doOnError(throwable -> {
                    log.error(String.format("error while persisting progress %s", progress), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist a linear pathway progress.
     *
     * @param progress the progress
     * @return a void flux.
     */
    public Flux<Void> persist(final LinearPathwayProgress progress) {
        return Mutators.execute(session, Flux.just(progressMutator.upsert(progress),
                                                   progressByCoursewareMutator.upsert(progress),
                                                   linearPathwayProgressMutator.upsert(progress),
                                                   linearPathwayProgressByCoursewareMutator.upsert(progress)))
                .doOnError(throwable -> {
                    log.error(String.format("error while persisting progress %s", progress), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist a free pathway progress.
     *
     * @param progress the progress
     * @return a void flux.
     */
    public Flux<Void> persist(final FreePathwayProgress progress) {
        return Mutators.execute(session, Flux.just(progressMutator.upsert(progress),
                                                   progressByCoursewareMutator.upsert(progress),
                                                   freePathwayProgressMutator.upsert(progress),
                                                   freePathwayProgressByCoursewareMutator.upsert(progress)))
                .doOnError(throwable -> {
                    log.error(String.format("error while persisting progress %s", progress), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist a graph pathway progress
     *
     * @param progress the progress to persist
     * @return a void flux
     */
    public Flux<Void> persist(final GraphPathwayProgress progress) {
        return Mutators.execute(session, Flux.just(
                progressMutator.upsert(progress),
                progressByCoursewareMutator.upsert(progress),
                graphPathwayProgressMutator.upsert(progress),
                graphPathwayProgressByCoursewareMutator.upsert(progress)
        )).doOnError(throwable -> {
            log.error(String.format("error while persisting progress %s", progress), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Persist a random pathway progress
     *
     * @param progress the progress to persist
     * @return a void flux
     */
    public Flux<Void> persist(final RandomPathwayProgress progress) {
        return Mutators.execute(session, Flux.just(
                progressMutator.upsert(progress),
                progressByCoursewareMutator.upsert(progress),
                randomPathwayProgressMutator.upsert(progress),
                randomPathwayProgressByCoursewareMutator.upsert(progress)
        )).doOnError(throwable -> {
            log.error(String.format("error while persisting progress %s", progress), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Persist a BKT pathway progress
     *
     * @param progress the progress to persist
     * @return a void flux
     */
    @Trace(async = true)
    public Flux<Void> persist(final BKTPathwayProgress progress) {
        return Mutators.execute(session, Flux.just(
                progressMutator.upsert(progress),
                progressByCoursewareMutator.upsert(progress),
                bktPathwayProgressMutator.upsert(progress),
                bktPathwayProgressByCoursewareMutator.upsert(progress)
        )).doOnError(throwable -> {
            log.error(String.format("error while persisting progress %s", progress), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Find a non-type specific progress by id.
     *
     * @param id the progress id
     * @return a mono of a general non-type specific progress
     */
    public Mono<Progress> find(final UUID id) {
        return ResultSets.query(session, progressMaterializer.find(id)) //
                .flatMapIterable(row -> row) //
                .map(progressMaterializer::fromRow) //
                .singleOrEmpty();
    }

    /**
     * Find a non-type specific progress by courseware
     *
     * @param deploymentId the deployment id
     * @param coursewareElementId the courseware element id
     * @param studentId the student id
     *
     * @return a flux of general non-type specific progress
     */
    public Flux<Progress> find(final UUID deploymentId, final UUID coursewareElementId, final UUID studentId) {
        //
        return ResultSets.query(session,
                                progressByCoursewareMaterializer.find(deploymentId, coursewareElementId, studentId)) //
                .flatMapIterable(row -> row) //
                .map(progressByCoursewareMaterializer::fromRow);
    }

    /**
     * Find the latest non-type specific progress by courseware
     *
     * @param deploymentId the deployment id
     * @param coursewareElementId the courseware element id
     * @param studentId the student id
     *
     * @return a Mono of the latest general non-type specific progress
     */
    public Mono<Progress> findLatest(final UUID deploymentId, final UUID coursewareElementId, final UUID studentId) {
        //
        return ResultSets.query(session, progressByCoursewareMaterializer.findLatest(deploymentId, coursewareElementId,
                                                                                     studentId))
                .flatMapIterable(row -> row) //
                .map(progressByCoursewareMaterializer::fromRow) //
                .singleOrEmpty();
    }

    /**
     * Find a Linear Pathway Progress by id
     *
     * @param id the id
     *
     * @return a Mono of the Linear Pathway Progress
     */
    public Mono<LinearPathwayProgress> findLinearPathway(final UUID id) {
        //
        return ResultSets.query(session, linearPathwayProgressMaterializer.find(id)) //
                .flatMapIterable(row -> row) //
                .map(linearPathwayProgressMaterializer::fromRow) //
                .singleOrEmpty();
    }

    /**
     * Find a Linear Pathway Progress by courseware
     *
     * @param deploymentId the deployment id
     * @param coursewareElementId the courseware element id
     * @param studentId the student id
     *
     * @return a Flux of Linear Pathway Progress, ordered from newest to oldest
     */
    public Flux<LinearPathwayProgress> findLinearPathway(final UUID deploymentId,
            final UUID coursewareElementId,
            final UUID studentId) {
        //
        return ResultSets.query(session, //
                                linearPathwayProgressByCoursewareMaterializer.find(deploymentId, coursewareElementId,
                                                                                   studentId))
                .flatMapIterable(row -> row) //
                .map(linearPathwayProgressByCoursewareMaterializer::fromRow);
    }

    /**
     * Find a Linear Pathway Progress by courseware
     *
     * @param deploymentId the deployment id
     * @param coursewareElementId the courseware element id
     * @param studentId the student id
     *
     * @return a Mono of the latest Linear Pathway Progress
     */
    @Trace(async = true)
    public Mono<LinearPathwayProgress> findLatestLinearPathway(final UUID deploymentId,
            final UUID coursewareElementId,
            final UUID studentId) {
        //
        return ResultSets.query(session, //
                                linearPathwayProgressByCoursewareMaterializer.findLatest(deploymentId,
                                                                                         coursewareElementId,
                                                                                         studentId))
                .flatMapIterable(row -> row) //
                .map(linearPathwayProgressByCoursewareMaterializer::fromRow) //
                .singleOrEmpty();
    }

    //

    /**
     * Find a Activity Pathway Progress by id
     *
     * @param id the id
     *
     * @return a Mono of the Linear Pathway Progress
     */
    public Mono<ActivityProgress> findActivity(final UUID id) {
        //
        return ResultSets.query(session, activityProgressMateralizer.find(id)) //
                .flatMapIterable(row -> row) //
                .map(activityProgressMateralizer::fromRow) //
                .singleOrEmpty();
    }

    /**
     * Find a Linear Pathway Progress by courseware
     *
     * @param deploymentId the deployment id
     * @param coursewareElementId the courseware element id
     * @param studentId the student id
     *
     * @return a Flux of Linear Pathway Progress, ordered from newest to oldest
     */
    public Flux<ActivityProgress> findActivity(final UUID deploymentId,
            final UUID coursewareElementId,
            final UUID studentId) {
        //
        return ResultSets.query(session, //
                                activityProgressByCoursewareMaterializer.find(deploymentId, coursewareElementId,
                                                                              studentId)) //
                .flatMapIterable(row -> row) //
                .map(activityProgressByCoursewareMaterializer::fromRow);
    }

    /**
     * Find a Linear Pathway Progress by courseware
     *
     * @param deploymentId the deployment id
     * @param coursewareElementId the courseware element id
     * @param studentId the student id
     *
     * @return a Mono of the latest Linear Pathway Progress
     */
    @Trace(async = true)
    public Mono<ActivityProgress> findLatestActivity(final UUID deploymentId,
            final UUID coursewareElementId,
            final UUID studentId) {
        //
        return ResultSets.query(session, //
                                activityProgressByCoursewareMaterializer.findLatest(deploymentId, coursewareElementId,
                                                                                    studentId))
                .flatMapIterable(row -> row) //
                .map(activityProgressByCoursewareMaterializer::fromRow) //
                .singleOrEmpty();
    }

    /**
     * Find a Free Pathway Progress by courseware*
     *
     * @param deploymentId the deployment id
     * @param pathwayId the pathway id
     * @param studentId the student id
     *
     * @return a Flux of Free Pathway Progress, ordered from newest to oldest
     */
    public Flux<FreePathwayProgress> findFreePathway(final UUID deploymentId,
            final UUID pathwayId,
            final UUID studentId) {
        //
        return ResultSets.query(session, //
                                freePathwayProgressByCoursewareMaterializer.find(deploymentId, pathwayId, studentId))
                .flatMapIterable(row -> row) //
                .map(freePathwayProgressByCoursewareMaterializer::fromRow);
    }

    /**
     * Find a Free Pathway Progress by courseware
     *
     * @param deploymentId the deployment id
     * @param pathwayId the pathway id
     * @param studentId the student id
     *
     * @return a Mono of the latest Free Pathway Progress
     */
    @Trace(async = true)
    public Mono<FreePathwayProgress> findLatestFreePathway(final UUID deploymentId,
                                                           final UUID pathwayId,
                                                           final UUID studentId) {
        return ResultSets.query(session, freePathwayProgressByCoursewareMaterializer
                .findLatest(deploymentId, pathwayId, studentId))
                .flatMapIterable(row -> row)
                .map(freePathwayProgressByCoursewareMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find a Graph Pathway Progress by id
     *
     * @param id the id
     *
     * @return a Mono of the Graph Pathway Progress
     */
    public Mono<GraphPathwayProgress> findGraphPathway(final UUID id) {
        return ResultSets.query(session, graphPathwayProgressMaterializer.find(id))
                .flatMapIterable(row -> row)
                .map(graphPathwayProgressMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find a Graph Pathway Progress by courseware
     *
     * @param deploymentId the deployment id
     * @param coursewareElementId the courseware element id
     * @param studentId the student id
     *
     * @return a Flux of Graph Pathway Progress, ordered from newest to oldest
     */
    public Flux<GraphPathwayProgress> findGraphPathway(final UUID deploymentId,
                                                       final UUID coursewareElementId,
                                                       final UUID studentId) {
        return ResultSets.query(session, graphPathwayProgressByCoursewareMaterializer
                .find(deploymentId, coursewareElementId, studentId))
                .flatMapIterable(row -> row)
                .map(graphPathwayProgressByCoursewareMaterializer::fromRow);
    }

    /**
     * Find a Graph Pathway Progress by courseware
     *
     * @param deploymentId the deployment id
     * @param coursewareElementId the courseware element id
     * @param studentId the student id
     *
     * @return a Mono of the latest Graph Pathway Progress
     */
    @Trace(async = true)
    public Mono<GraphPathwayProgress> findLatestGraphPathway(final UUID deploymentId,
                                                             final UUID coursewareElementId,
                                                             final UUID studentId) {
        return ResultSets.query(session, graphPathwayProgressByCoursewareMaterializer
                .findLatest(deploymentId, coursewareElementId, studentId))
                .flatMapIterable(row -> row)
                .map(graphPathwayProgressByCoursewareMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find progress for a pathway of type RANDOM by courseware
     *
     * @param deploymentId the deployment id
     * @param coursewareElementId the courseware element id
     * @param studentId the student id
     *
     * @return a Mono of the latest Random Pathway Progress
     */
    @Trace(async = true)
    public Mono<RandomPathwayProgress> findLatestRandomPathway(final UUID deploymentId,
                                                               final UUID coursewareElementId,
                                                               final UUID studentId) {
        return ResultSets.query(session, randomPathwayProgressByCoursewareMaterializer
                .findLatest(deploymentId, coursewareElementId, studentId))
                .flatMapIterable(row -> row)
                .map(randomPathwayProgressByCoursewareMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find the latest <i>n</i> progresses for a pathway of type BKT by courseware
     *
     * @param deploymentId the deployment id
     * @param coursewareElementId the courseware element id
     * @param studentId the student id
     * @param n the number of latest progresses to fetch
     * @return a flux of the latest BKT Pathway Progress
     */
    @Trace(async = true)
    public Flux<BKTPathwayProgress> findLatestNBKTPathway(final UUID deploymentId,
                                                          final UUID coursewareElementId,
                                                          final UUID studentId,
                                                          final int n) {
        return ResultSets.query(session, bktPathwayProgressByCoursewareMaterializer
                .findLatestN(deploymentId, coursewareElementId, studentId, n))
                // maintain ordering
                .concatMapIterable(row -> row)
                .map(bktPathwayProgressByCoursewareMaterializer::fromRow);
    }
}
