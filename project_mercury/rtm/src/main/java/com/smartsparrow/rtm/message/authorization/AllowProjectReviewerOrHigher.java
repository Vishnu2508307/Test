package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.workspace.ProjectMessage;
import com.smartsparrow.workspace.service.ProjectPermissionService;

public class AllowProjectReviewerOrHigher implements AuthorizationPredicate<ProjectMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowProjectReviewerOrHigher.class);

    private final ProjectPermissionService projectPermissionService;

    @Inject
    public AllowProjectReviewerOrHigher(final ProjectPermissionService projectPermissionService) {
        this.projectPermissionService = projectPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    @Override
    public boolean test(final AuthenticationContext authenticationContext, final ProjectMessage projectMessage) {
        Account account = authenticationContext.getAccount();

        if (projectMessage.getProjectId() != null) {
            if (account != null) {
                PermissionLevel permissionLevel = projectPermissionService
                        .findHighestPermissionLevel(account.getId(), projectMessage.getProjectId())
                        .block();

                return permissionLevel != null && permissionLevel.isEqualOrHigherThan(PermissionLevel.REVIEWER);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Could not verify permission level, `projectId` was not supplied with the message {}", projectMessage.toString());
        }

        return false;
    }
}
