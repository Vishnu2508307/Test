package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.workspace.service.ProjectPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.UUID;

public class CoursewareElementAuthorizerService {
    private static final Logger log = LoggerFactory.getLogger(CoursewareElementAuthorizerService.class);
    private final CoursewareService coursewareService;
    private final ProjectPermissionService projectPermissionService;

    @Inject
    public CoursewareElementAuthorizerService(final CoursewareService coursewareService,
                                              final ProjectPermissionService projectPermissionService) {
        this.coursewareService = coursewareService;
        this.projectPermissionService = projectPermissionService;
    }

    /**
     * Method to authorize permission level
     *
     * @param authenticationContext holds the authenticated user
     * @param elementId             the element id
     * @param elementType           the element type
     * @param permissionLevel       the permission level
     * @return <code>true</code> if the request is permitted or <code>false</code> when not
     */
    public boolean authorize(AuthenticationContext authenticationContext,
                             UUID elementId,
                             CoursewareElementType elementType,
                             PermissionLevel permissionLevel) {
        Account account = authenticationContext.getAccount();
        UUID projectId;
        try {
            projectId = coursewareService.getProjectId(elementId, elementType).block();
            //Finding the permission level via project
            if (projectId != null && account != null) {
                PermissionLevel permission = projectPermissionService.findHighestPermissionLevel(account.getId(), projectId).block();

                return permission != null
                        && permission.isEqualOrHigherThan(permissionLevel);
            }

            if (log.isDebugEnabled()) {
                log.debug("Could not verify permission level, `projectId` or `accountId` can not be defined: " + elementId);
            }

            return false;
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Exception while checking permissions for courseware elements", ex);
            }
            return false;
        }
    }

}
