package com.smartsparrow.graphql.schema;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.service.GraphQLPageFactory;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.learner.service.StudentScoreService;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Mono;

@Singleton
public class PathwaySchema {

    private static final Logger log = LoggerFactory.getLogger(PathwaySchema.class);

    private final LearnerActivityService learnerActivityService;
    private final StudentScoreService studentScoreService;
    private final ActivityService activityService;
    private final PathwayService pathwayService;

    @Inject
    public PathwaySchema(final LearnerActivityService learnerActivityService,
                         final StudentScoreService studentScoreService,
                         final ActivityService activityService,
                         final PathwayService pathwayService) {
        this.learnerActivityService = learnerActivityService;
        this.studentScoreService = studentScoreService;
        this.activityService = activityService;
        this.pathwayService = pathwayService;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Pathway.pathways")
    @GraphQLQuery(name = "pathways", description = "List of child pathways")
    public CompletableFuture<List<LearnerPathway>> getWalkableLearnerPathways(@GraphQLContext LearnerWalkable walkable,
                                                                              @GraphQLArgument(name = "pathwayId", description = "Fetch pathway with specific id") UUID pathwayId) {

        switch (walkable.getElementType()) {
            case ACTIVITY:
                if (pathwayId != null) {
                    return learnerActivityService.findChildPathway(walkable.getId(), walkable.getDeploymentId(), pathwayId)
                            .doOnEach(ReactiveTransaction.linkOnNext())
                            .doOnEach(ReactiveTransaction.expireOnComplete())
                            .subscriberContext(ReactiveMonitoring.createContext())
                            .flux()
                            .collectList()
                            .toFuture();
                } else {
                    return learnerActivityService.findChildPathways(walkable.getId(), walkable.getDeploymentId())
                            .doOnEach(ReactiveTransaction.linkOnNext())
                            .doOnEach(ReactiveTransaction.expireOnComplete())
                            .subscriberContext(ReactiveMonitoring.createContext())
                            .collectList()
                            .toFuture();
                }
            case INTERACTIVE:
                // This query is not intended to do anything, it will always return null as interactives cannot have child pathways.
                // This has been added in order for learnspace to be able to query for walkables, without knowing the element type in advance.
                return CompletableFuture.completedFuture(null);
            default:
                return CompletableFuture.failedFuture(new UnsupportedOperationException("Unsupported courseware element type " + walkable.getElementType()));
        }

    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Pathway.score")
    @GraphQLQuery(name = "score", description = "get the learner pathway latest attempt score for the authenticated student")
    public CompletableFuture<Score> getStudentPathwayScore(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                           @GraphQLContext final LearnerPathway learnerPathway) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        final UUID studentId = context.getAuthenticationContext().getAccount().getId();

        return studentScoreService.computeScore(learnerPathway.getDeploymentId(), studentId, learnerPathway.getId(),
                                                null)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Find the child pathways for an activity
     *
     * @param activity the activity to find the child pathwyas for
     * @param pathwayId the pathway id to filter
     * @return a page of pathways
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Pathway.coursewarePathways")
    @GraphQLQuery(name = "coursewarePathways", description = "List of child pathways")
    public CompletableFuture<Page<Pathway>> getActivityPathways(@GraphQLContext Activity activity,
                                                                @Nullable @GraphQLArgument(name = "pathwayId", description = "Fetch pathway with specific id") UUID pathwayId,
                                                                @GraphQLArgument(name = "before", description = "fetching only nodes before this node (exclusive)") String before,
                                                                @GraphQLArgument(name = "last", description = "fetching only the last certain number of nodes") Integer last) {


        Mono<List<UUID>> childPathwayIds = activityService
                .findChildPathwayIds(activity.getId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .defaultIfEmpty(Lists.newArrayList());

        Mono<List<Pathway>> childPathways = childPathwayIds
                .flatMapIterable(list -> list)
                .filter(uuid -> pathwayId == null || uuid.equals(pathwayId))
                .flatMap(childPathwayId -> pathwayService.findById(childPathwayId)
                        // if a pathway is not found log the error and keep going
                        .onErrorResume(PathwayNotFoundException.class, ex -> {
                            log.warn("pathway not found {}", ex.getMessage());
                            return Mono.empty();
                        }))
                .filter(Objects::nonNull)
                .collectList();

        return GraphQLPageFactory.createPage(childPathways, before, last).toFuture();
    }
}
