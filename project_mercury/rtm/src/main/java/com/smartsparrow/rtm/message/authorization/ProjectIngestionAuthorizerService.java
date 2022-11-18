package com.smartsparrow.rtm.message.authorization;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.workspace.service.ProjectPermissionService;

public class ProjectIngestionAuthorizerService {
    private static final Logger log = LoggerFactory.getLogger(ProjectIngestionAuthorizerService.class);
    private final IngestionService ingestionService;
    private final ProjectPermissionService projectPermissionService;

    @Inject
    public ProjectIngestionAuthorizerService(final IngestionService ingestionService,
                                             final ProjectPermissionService projectPermissionService) {
        this.ingestionService = ingestionService;
        this.projectPermissionService = projectPermissionService;
    }

    /**
     * Method to authorize permission level
     *
     * @param authenticationContext holds the authenticated user
     * @param ingestionId           the ingestion id
     * @param permissionLevel       the permission level
     * @return <code>true</code> if the request is permitted or <code>false</code> when not
     */
    public boolean authorize(AuthenticationContext authenticationContext,
                             UUID ingestionId,
                             PermissionLevel permissionLevel) {
        Account account = authenticationContext.getAccount();
        IngestionSummary ingestionSummary;
        try {
            ingestionSummary = ingestionService.findById(ingestionId).block();
            //Finding the permission level via project
            if (ingestionSummary != null && ingestionSummary.getProjectId() != null && account != null) {
                PermissionLevel permission = projectPermissionService.findHighestPermissionLevel(account.getId(), ingestionSummary.getProjectId()).block();

                return permission != null
                        && permission.isEqualOrHigherThan(permissionLevel);
            }

            if (log.isDebugEnabled()) {
                log.debug("Could not verify permission level, `projectId` or `accountId` can not be defined: " + ingestionId);
            }

            return false;
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Exception while checking permissions for project ingestion", ex);
            }
            return false;
        }
    }

}
