package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.workspace.WorkspaceMessage;
import com.smartsparrow.workspace.service.WorkspaceService;

public class AllowWorkspaceReviewerOrHigher implements AuthorizationPredicate<WorkspaceMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowWorkspaceReviewerOrHigher.class);

    private final WorkspaceService workspaceService;

    @Inject
    public AllowWorkspaceReviewerOrHigher(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, WorkspaceMessage message) {
        Account account = authenticationContext.getAccount();

        if (message.getWorkspaceId() != null) {
            if (account != null) {
                PermissionLevel permission = workspaceService
                        .findHighestPermissionLevel(account.getId(), message.getWorkspaceId()).block();
                return permission != null &&
                        permission.isEqualOrHigherThan(PermissionLevel.REVIEWER);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("could not authorize account, `workspaceId` field is missing {}", message);
        }

        return false;
    }
}
