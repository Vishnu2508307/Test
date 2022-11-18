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

public class AllowWorkspaceContributorOrHigher implements AuthorizationPredicate<WorkspaceMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowWorkspaceContributorOrHigher.class);

    private final WorkspaceService workspaceService;

    @Inject
    public AllowWorkspaceContributorOrHigher(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, WorkspaceMessage workspaceMessage) {
        Account account = authenticationContext.getAccount();

        if (workspaceMessage.getWorkspaceId() != null) {
            if (account != null) {
                PermissionLevel permissionLevel = workspaceService
                        .findHighestPermissionLevel(account.getId(), workspaceMessage.getWorkspaceId()).block();

                return permissionLevel != null
                        && permissionLevel.isEqualOrHigherThan(PermissionLevel.CONTRIBUTOR);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Could not verify permission level, `workspaceId` was not supplied in the message {}", workspaceMessage);
        }

        return false;
    }
}
