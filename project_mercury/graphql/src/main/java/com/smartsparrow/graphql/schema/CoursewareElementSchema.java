package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.auth.AllowWorkspaceReviewerOrHigher;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.service.LearnerService;
import com.smartsparrow.workspace.data.Workspace;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

@Singleton
public class CoursewareElementSchema {

    private final AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;
    private final CoursewareService coursewareService;
    private final LearnerService learnerService;

    @Inject
    public CoursewareElementSchema(final AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher,
                                   final CoursewareService coursewareService,
                                   final LearnerService learnerService) {
        this.allowWorkspaceReviewerOrHigher = allowWorkspaceReviewerOrHigher;
        this.coursewareService = coursewareService;
        this.learnerService = learnerService;
    }

    /**
     * Fetch a courseware element given an id
     *
     * @param workspace the workspace to fetch the courseware element from
     * @param elementId the id of the element to fetch
     * @return the found courseware
     */
    @GraphQLQuery(name = "getCoursewareElement", description = "fetch the courseware element given an elementId")
    public CompletableFuture<CoursewareElement> getCoursewareElement(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                     @GraphQLContext Workspace workspace,
                                                                     @GraphQLArgument(name = "elementId", description = "the id to find the courseware element for")
                                                          UUID elementId) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        affirmPermission(allowWorkspaceReviewerOrHigher.test(context.getAuthenticationContext(), workspace.getId()), "Not allowed");

        return coursewareService.findCoursewareElement(elementId)
                .toFuture();
    }

    /**
     * Fetch a list of configuration fields given their names for a courseware element
     *
     * @param coursewareElement the context
     * @param fieldNames the name of the fields to find
     * @return a list of configuration fields
     */
    @GraphQLQuery(name = "configurationFields", description = "fetch configuration fields values for a courseware element")
    public CompletableFuture<List<ConfigurationField>> getCoursewareElementFields(@GraphQLContext CoursewareElement coursewareElement,
                                                                                  @GraphQLArgument(name = "fieldNames", description = "fetch those field values within the configuration")
                                                                       List<String> fieldNames) {

        affirmArgument(fieldNames != null, "fieldNames argument required");
        affirmArgument(!fieldNames.isEmpty(), "at least 1 field name must be supplied");

        return coursewareService.fetchConfigurationFields(coursewareElement.getElementId(), fieldNames)
                .collectList()
                .toFuture();
    }

    /**
     * Fetch a learner courseware element given an id
     *
     * @param deployment the deployment to fetch the courseware element from
     * @param elementId the id of the element to fetch
     * @return the found courseware
     */
    @GraphQLQuery(name = "getLearnerElement", description = "fetch the courseware element given an elementId")
    public CompletableFuture<CoursewareElement> getLearnerElementByDeployment(@GraphQLContext Deployment deployment,
                                                                              @GraphQLArgument(name = "elementId", description = "the id to find the courseware element for")
                                                          UUID elementId) {

        return learnerService.findElementByDeployment(elementId, deployment.getId())
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("type not found for element %s", elementId))))
                .map(learnerCoursewareElement -> new CoursewareElement()
                        .setElementId(learnerCoursewareElement.getId())
                        .setElementType(learnerCoursewareElement.getElementType()))
                .toFuture();
    }
}
