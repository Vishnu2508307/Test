package com.smartsparrow.graphql.auth;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;

public class AllowWorkspaceRoles {

    @Inject
    public AllowWorkspaceRoles() {
    }

    /**
     * Test if the user has workspace roles
     * @param authenticationContext - the authentication context of the userß
     * @return whether the user has access(true) or not(false)
     */
    public boolean test(AuthenticationContext authenticationContext) {
        Account account = authenticationContext.getAccount();
        affirmArgument(account != null, "account is required");
        affirmArgument(CollectionUtils.isNotEmpty(account.getRoles()), "roles can not be empty or null");

        return account
                .getRoles()
                .stream()
                .anyMatch(AccountRole.WORKSPACE_ROLES::contains);
    }
}
