package com.smartsparrow.graphql.auth;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.service.ProjectPermissionService;
import com.smartsparrow.workspace.service.WorkspaceService;

public abstract class CoursewareElementAuthorizer {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareElementAuthorizer.class);

    private final WorkspaceService workspaceService;
    private final CoursewareService coursewareService;
    private final ProjectPermissionService projectPermissionService;

    @Inject
    public CoursewareElementAuthorizer(WorkspaceService workspaceService,
                                       CoursewareService coursewareService,
                                       ProjectPermissionService projectPermissionService) {
        this.workspaceService = workspaceService;
        this.coursewareService = coursewareService;
        this.projectPermissionService = projectPermissionService;
    }

    public abstract PermissionLevel getAllowedPermissionLevel();

    /**
     * Return boolean value based on the permission level
     * @param elementId the element id
     * @param elementType the element type
     * @return a boolean representing the result of the evaluated condition
     */
    public boolean test(AuthenticationContext authenticationContext, UUID elementId, CoursewareElementType elementType) {

        Account account = authenticationContext.getAccount();

        try {
            UUID projectId = coursewareService.getProjectId(elementId, elementType).block();

            if (projectId != null) {
                PermissionLevel permission = projectPermissionService.findHighestPermissionLevel(account.getId(),
                                                                                                 projectId).block();
                return permission != null
                        && permission.isEqualOrHigherThan(getAllowedPermissionLevel());
            }

            log.debug("Could not verify permission level, `projectId` or `accountId` can not be defined: ", new HashMap<String, Object>() {
                {
                    put("elementId", elementId);
                    put("elementType", elementType);
                }
            });

            return false;
        } catch (Exception ex) {
            log.debug("Exception while checking permissions for courseware elements", new HashMap<String, Object>() {
                {
                    put("error", ex.getStackTrace());
                }
            });
            return false;
        }
    }
}
