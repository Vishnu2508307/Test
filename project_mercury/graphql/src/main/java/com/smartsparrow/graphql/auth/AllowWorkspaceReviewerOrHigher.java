package com.smartsparrow.graphql.auth;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.workspace.service.WorkspaceService;

@Singleton
public class AllowWorkspaceReviewerOrHigher {

    private final WorkspaceService workspaceService;

    @Inject
    public AllowWorkspaceReviewerOrHigher(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    public boolean test(AuthenticationContext authenticationContext, UUID workspaceId) {
        Account account = authenticationContext.getAccount();
        affirmArgument(workspaceId != null, "workspaceId is required");
        affirmArgument(account != null, "account is required");

        return PermissionLevel.REVIEWER.isEqualOrLowerThan(workspaceService
                .findHighestPermissionLevel(account.getId(), workspaceId).block());
    }
}
