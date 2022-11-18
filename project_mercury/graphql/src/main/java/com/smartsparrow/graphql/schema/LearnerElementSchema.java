package com.smartsparrow.graphql.schema;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerWalkablePayload;
import com.smartsparrow.learner.service.LearnerCoursewareElementStructureService;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerElementSchema {

    private final LearnerCoursewareElementStructureService learnerCoursewareElementStructureService;

    @Inject
    public LearnerElementSchema(final LearnerCoursewareElementStructureService learnerCoursewareElementStructureService) {
        this.learnerCoursewareElementStructureService = learnerCoursewareElementStructureService;
    }


    @GraphQLQuery(name = "getDefaultFirst", description = "fetch the learner walkable payloads")
    public CompletableFuture<List<LearnerWalkablePayload>> getLearnerElementByDeployment(@GraphQLContext LearnerActivity learnerActivity,
                                                                                         @GraphQLArgument(name = "elementId", description = "the id to find the courseware element for")
                                                                                                 UUID elementId) {
        affirmArgument(learnerActivity != null, "learnerActivity is required");
        affirmArgument(learnerActivity.getDeploymentId() != null, "deploymnetId is required");


        return learnerCoursewareElementStructureService.getLearnerCoursewareWalkable(learnerActivity.getDeploymentId(),
                                                                                     elementId)
                .flatMapIterable(item -> item)
                .filter(learnerCoursewareWalkable -> !learnerCoursewareWalkable.getType().equals(CoursewareElementType.COMPONENT))
                .filter(learnerCoursewareWalkable -> !learnerCoursewareWalkable.getType().equals(CoursewareElementType.PATHWAY))
                .flatMap(learnerCoursewareWalkable -> {
                    if (learnerCoursewareWalkable.getType() == ACTIVITY || learnerCoursewareWalkable.getType() == INTERACTIVE) {
                        return Mono.just(learnerCoursewareWalkable.getLearnerWalkablePayload());
                    }
                    return Mono.error(new UnsupportedOperationException("Unsupported courseware element type " + learnerCoursewareWalkable.getType()));
                })
                .collectList()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

}
