package com.smartsparrow.graphql.auth;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.CollectionUtils;

import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;

@Singleton
public class AllowCohortInstructor {

    private final CohortPermissionService cohortPermissionService;

    private final CohortService cohortService;

    @Inject
    public AllowCohortInstructor(CohortPermissionService cohortPermissionService,
                                 final CohortService cohortService) {
        this.cohortPermissionService = cohortPermissionService;
        this.cohortService = cohortService;
    }

    public boolean test(AuthenticationContext authenticationContext,UUID cohortId) {
        Account account = authenticationContext.getAccount();
        affirmArgument(cohortId != null, "missing cohortId");
        affirmArgument(account != null, "account can not be null");
        affirmArgument(CollectionUtils.isNotEmpty(account.getRoles()), "roles can not be empty or null");

        boolean allowCohortInstructorCheck = false;

        if (account.getRoles().stream().anyMatch(AccountRole.WORKSPACE_ROLES::contains)) {
            // check the given cohortId
            allowCohortInstructorCheck = PermissionLevel.REVIEWER.isEqualOrLowerThan(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId).block());

            // only if given cohortId is not authorized should we check (LTI) instance cohortIds, if there are any
            // todo once every FE GQL query passes the cohortId, and every BE schema uses it, the code block below can be removed
            if (!allowCohortInstructorCheck) {
                List<PermissionLevel> permissionLevels = cohortService.findCohortInstanceIds(cohortId) // for LTI instructors
                        .flatMap(cohortIds -> cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortIds))
                        .collectList().block();

                if (permissionLevels != null) {
                    for (PermissionLevel permissionLevel : permissionLevels) {
                        if (allowCohortInstructorCheck) {
                            break;
                        }
                        allowCohortInstructorCheck = allowCohortInstructorCheck || PermissionLevel.REVIEWER.isEqualOrLowerThan(
                                permissionLevel);
                    }
                }
            }
        }

        return allowCohortInstructorCheck;

//        TODO: keep it. will change back to this when cohort id is available in the graphql argument
//        return account.getRoles().stream().anyMatch(AccountRole.WORKSPACE_ROLES::contains)
//                && PermissionLevel.REVIEWER.isEqualOrLowerThan(cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId).block());

    }
}
