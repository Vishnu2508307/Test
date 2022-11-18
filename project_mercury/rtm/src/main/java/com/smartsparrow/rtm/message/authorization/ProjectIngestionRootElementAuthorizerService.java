package com.smartsparrow.rtm.message.authorization;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.workspace.service.ProjectPermissionService;

public class ProjectIngestionRootElementAuthorizerService {

    private static final Logger log = LoggerFactory.getLogger(ProjectIngestionRootElementAuthorizerService.class);
    private final ProjectPermissionService projectPermissionService;

    @Inject
    public ProjectIngestionRootElementAuthorizerService(
            final ProjectPermissionService projectPermissionService) {
        this.projectPermissionService = projectPermissionService;
    }

    /**
     * Method to authorize permission level by projecr Id
     *
     * @param authenticationContext holds the authenticated user
     * @param rootElementId the root element id
     * @param projectId the project id
     * @param permissionLevel the permission level
     * @return <code>true</code> if the request is permitted or <code>false</code> when not
     */
    public boolean authorize(AuthenticationContext authenticationContext,
                             UUID rootElementId,
                             UUID projectId,
                             PermissionLevel permissionLevel) {
        Account account = authenticationContext.getAccount();
        PermissionLevel permission;
        try {

            //Finding the permission level via project
            if (rootElementId != null && projectId != null && account != null) {
                permission = projectPermissionService.findHighestPermissionLevel(account.getId(), projectId).block();

                return permission != null
                        && permission.isEqualOrHigherThan(permissionLevel);
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        "Could not verify permission level, `projectId` or `accountId` can not be defined for rootElementId: " + rootElementId);
            }
            return false;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception while checking permissions for project ingestion", e);
            }
            return false;
        }
    }
}
