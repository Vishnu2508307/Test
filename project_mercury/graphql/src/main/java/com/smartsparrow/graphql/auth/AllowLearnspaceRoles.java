package com.smartsparrow.graphql.auth;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;

public class AllowLearnspaceRoles {


    @Inject
    public AllowLearnspaceRoles() {
    }

    public boolean test(AuthenticationContext authenticationContext) {
        Account account = authenticationContext.getAccount();
        affirmArgument(account != null, "account can not be null");

        //right now we allow all roles to enter learnspace, maybe later it will be only STUDENT and INSTRUCTOR
        return CollectionUtils.isNotEmpty(account.getRoles());
    }
}
