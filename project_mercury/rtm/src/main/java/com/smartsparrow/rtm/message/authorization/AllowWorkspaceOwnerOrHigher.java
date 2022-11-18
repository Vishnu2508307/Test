package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.workspace.WorkspaceMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.service.WorkspaceService;

public class AllowWorkspaceOwnerOrHigher implements AuthorizationPredicate<WorkspaceMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowWorkspaceOwnerOrHigher.class);

    private final WorkspaceService workspaceService;

    @Inject
    public AllowWorkspaceOwnerOrHigher(WorkspaceService workspaceService) {
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
                        permission.isEqualOrHigherThan(PermissionLevel.OWNER);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("could not authorize account, `workspaceId` field is missing {}", message);
        }

        return false;
    }
}
