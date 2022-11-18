package com.smartsparrow.graphql.auth;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.workspace.service.WorkspaceService;

/**
 * TODO: this code is copy of AllowWorkspaceContributorOrHigher from rtm.
 * We need to move RTM authorizers to IAM to be able to use them in GraphQL
 */
public class AllowWorkspaceContributorOrHigher {

    private final WorkspaceService workspaceService;

    @Inject
    public AllowWorkspaceContributorOrHigher(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    public boolean test(AuthenticationContext authenticationContext, UUID workspaceId) {
        Account account = authenticationContext.getAccount();
        affirmArgument(workspaceId != null, "missing workspaceId");
        affirmArgument(account != null, "account can not be null");

        PermissionLevel accountPermission =
                workspaceService.findHighestPermissionLevel(account.getId(), workspaceId).block();

        return accountPermission != null && accountPermission.isEqualOrHigherThan(PermissionLevel.CONTRIBUTOR);
    }

}
