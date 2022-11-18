package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.competency.data.Document;
import com.smartsparrow.courseware.data.CoursewareElementAncestry;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowWorkspaceReviewerOrHigher;
import com.smartsparrow.graphql.auth.AllowWorkspaceRoles;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.service.WorkspaceService;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;

@Singleton
public class WorkspaceSchema {

    private final WorkspaceService workspaceService;
    private final AllowWorkspaceRoles allowWorkspaceRoles;
    private final AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;
    private final CoursewareService coursewareService;

    @Inject
    public WorkspaceSchema(WorkspaceService workspaceService, AllowWorkspaceRoles allowWorkspaceRoles,
                           AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher,
                           CoursewareService coursewareService) {
        this.workspaceService = workspaceService;
        this.allowWorkspaceRoles = allowWorkspaceRoles;
        this.allowWorkspaceReviewerOrHigher = allowWorkspaceReviewerOrHigher;
        this.coursewareService = coursewareService;
    }

    /**
     * Get all documents for a workspace
     *
     * @param workspaceId - the workspace id to fetch the documents for
     * @return {@link Page<Document>} - A page of document
     */
    @GraphQLQuery(name = "workspace", description = "The workspace data")
    public CompletableFuture<Workspace> getWorkspace(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                     @GraphQLArgument(name = "workspaceId", description = "Fetch a workspace by id") UUID workspaceId) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        Account account = context.getAuthenticationContext().getAccount();

        affirmArgument(account != null, "account cannot be null");
        affirmArgument(workspaceId != null, "workspaceId is required");

        affirmPermission(allowWorkspaceRoles.test(context.getAuthenticationContext()),
                "User does not have access to the workspace");
        affirmPermission(allowWorkspaceReviewerOrHigher.test(context.getAuthenticationContext(), workspaceId),
                "User does not have permissions to workspace");

        return workspaceService
                .fetchById(workspaceId)
                .toFuture();
    }

    @GraphQLQuery(name = "getCoursewareAncestry", description = "fetch the ancestry and type of an element given an id")
    public CompletableFuture<CoursewareElementAncestry> getCoursewareAncestry(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                              @GraphQLContext Workspace workspace,
                                                                              @GraphQLArgument(name = "elementId", description = "the element id to find the ancestry and type for")
                                                                   UUID elementId) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        affirmPermission(allowWorkspaceReviewerOrHigher.test(context.getAuthenticationContext(), workspace.getId()), "Not allowed");

        return coursewareService.findCoursewareElementAncestry(elementId)
                .toFuture();
    }
}
