package com.smartsparrow.graphql.auth;

import javax.inject.Inject;

import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.workspace.service.ProjectPermissionService;
import com.smartsparrow.workspace.service.WorkspaceService;

public class AllowCoursewareElementContributorOrHigher extends CoursewareElementAuthorizer {

    @Inject
    public AllowCoursewareElementContributorOrHigher(WorkspaceService workspaceService,
                                                     CoursewareService coursewareService,
                                                     ProjectPermissionService projectPermissionService) {

        super(workspaceService, coursewareService, projectPermissionService);
    }

    @Override
    public PermissionLevel getAllowedPermissionLevel() {
        return PermissionLevel.CONTRIBUTOR;
    }
}
