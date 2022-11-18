package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.tree.CoursewareElementNode;
import com.smartsparrow.courseware.service.CoursewareElementStructureNavigateService;
import com.smartsparrow.courseware.service.CoursewareElementStructureService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowWorkspaceReviewerOrHigher;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.Workspace;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Singleton
public class CoursewareElementStructureSchema {

    private final AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;
    private final CoursewareElementStructureService coursewareElementStructureService;
    private final CoursewareElementStructureNavigateService coursewareElementStructureNavigateService;

    @Inject
    public CoursewareElementStructureSchema(final AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher,
                                            final CoursewareElementStructureService coursewareElementStructureService,
                                            final CoursewareElementStructureNavigateService coursewareElementStructureNavigateService) {
        this.allowWorkspaceReviewerOrHigher = allowWorkspaceReviewerOrHigher;
        this.coursewareElementStructureService = coursewareElementStructureService;
        this.coursewareElementStructureNavigateService = coursewareElementStructureNavigateService;
    }

    /**
     * Fetch flatten courseware structure from an element
     *
     * @param workspace the workspace to fetch the courseware element from
     * @param elementId the id of the element to fetch
     * @param elementType the courseware element type to fetch
     * @return the flattened CoursewareElementNode List
     */
    @Trace(dispatcher = true, metricName = "CoursewareElementStructure.getCoursewareElementIndex")
    @GraphQLQuery(name = "coursewareElementIndex",
            description = "fetch the courseware element structure given an elementId and elementType")
    public CompletableFuture<List<CoursewareElementNode>> getCoursewareElementIndex(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                    @GraphQLContext Workspace workspace,
                                                                                    @GraphQLArgument(name = "elementId",
                                                                         description = "the id to find the courseware element structure for")
                                                                         UUID elementId,
                                                                                    @GraphQLArgument(name = "elementType",
                                                                         description = "the courseware element type")
                                                                         CoursewareElementType elementType) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        affirmPermission(allowWorkspaceReviewerOrHigher.test(context.getAuthenticationContext(), workspace.getId()), "Not allowed");

        return coursewareElementStructureService.getCoursewareElementStructure(
                        elementId,
                        elementType,
                        Collections.singletonList("title"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .expandDeep(coursewareElementNode -> Flux.fromIterable(coursewareElementNode.getChildren())
                        .subscribeOn(Schedulers.elastic())
                        .publishOn(Schedulers.elastic()))
                .collectList()
                .toFuture();
    }

    /**
     * Fetch a courseware structure from an element
     *
     * @param workspace the workspace to fetch the courseware element from
     * @param elementId the id of the element to fetch
     * @param elementType the courseware element type to fetch
     * @return the found CoursewareElementNode
     */
    @Trace(dispatcher = true, metricName = "CoursewareElementStructure.getCoursewareElementStructure")
    @GraphQLQuery(name = "coursewareElementStructure",
            description = "fetch the courseware element structure given an elementId and elementType")
    public CompletableFuture<CoursewareElementNode> getCoursewareElementStructure(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                  @GraphQLContext Workspace workspace,
                                                                                  @GraphQLArgument(name = "elementId",
                                                                       description = "the id to find the courseware element structure for")
                                                                       UUID elementId,
                                                                                  @GraphQLArgument(name = "elementType",
                                                                       description = "the courseware element type")
                                                                       CoursewareElementType elementType) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        affirmPermission(allowWorkspaceReviewerOrHigher.test(context.getAuthenticationContext(), workspace.getId()), "Not allowed");

        return coursewareElementStructureService.getCoursewareElementStructure(elementId,
                                                                               elementType,
                                                                               Collections.singletonList("title"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Fetch a courseware structure from an element to one level
     *
     * @param workspace the workspace to fetch the courseware element from
     * @param elementId the id of the element to fetch
     * @param elementType the courseware element type to fetch
     * @return the found CoursewareElementNode
     */
    @Trace(dispatcher = true, metricName = "CoursewareElementStructure.navigateCoursewareElementStructure")
    @GraphQLQuery(name = "navigateCoursewareElementStructure",
            description = "fetch the courseware element structure with one level of children given an elementId and elementType")
    public CompletableFuture<CoursewareElementNode> navigateCoursewareElementStructure(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                       @GraphQLContext Workspace workspace,
                                                                                       @GraphQLArgument(name = "elementId",
                                                                            description = "the id to find the courseware element structure with one level of children for")
                                                                            UUID elementId,
                                                                                       @GraphQLArgument(name = "elementType",
                                                                            description = "the courseware element type")
                                                                            CoursewareElementType elementType) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        affirmPermission(allowWorkspaceReviewerOrHigher.test(context.getAuthenticationContext(), workspace.getId()), "Not allowed");

        return coursewareElementStructureNavigateService.getCoursewareElementStructure(elementId,
                                                                                       elementType,
                                                                                       Collections.singletonList("title"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

}
