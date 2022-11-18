package com.smartsparrow.graphql.auth;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.CollectionUtils;

import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;

@Singleton
public class AllowCohortContributorOrHigher {

    private final CohortPermissionService cohortPermissionService;

    @Inject
    public AllowCohortContributorOrHigher(CohortPermissionService cohortPermissionService) {
        this.cohortPermissionService = cohortPermissionService;
    }

    public boolean test(AuthenticationContext authenticationContext, UUID cohortId) {
        Account account = authenticationContext.getAccount();
        affirmArgument(cohortId != null, "missing cohortId");
        affirmArgument(account != null, "account can not be null");
        affirmArgument(CollectionUtils.isNotEmpty(account.getRoles()), "roles can not be empty or null");

        return account.getRoles().stream().anyMatch(AccountRole.WORKSPACE_ROLES::contains)
                && PermissionLevel.CONTRIBUTOR.isEqualOrLowerThan(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId).block());
    }
}
