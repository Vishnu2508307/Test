package com.smartsparrow.graphql.auth;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;

public class AllowSupportRole {

    @Inject
    public AllowSupportRole() {
    }

    /**
     * Test if the user has a {@link AccountRole#SUPPORT}
     * @param authenticationContext - the authentication context of the user
     * @return <strong>true</strong> when the user has the support role
     *         <br>
     *         <strong>false</strong> when the user does not have the support role
     */
    public boolean test(AuthenticationContext authenticationContext) {
        Account account = authenticationContext.getAccount();

        affirmArgument(account != null, "account is required");

        // do not allow the user to proceed when no roles are found in the account
        if (!CollectionUtils.isNotEmpty(account.getRoles())){
            return false;
        }

        return account
                .getRoles()
                .stream()
                .anyMatch(AccountRole.SUPPORT::equals);
    }
}
