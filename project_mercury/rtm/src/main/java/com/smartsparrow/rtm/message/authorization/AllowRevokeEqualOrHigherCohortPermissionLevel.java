package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.cohort.RevokeCohortPermissionMessage;

/**
 * This authorizer is used when a permission over a cohort entity is revoked from an account.
 */
public class AllowRevokeEqualOrHigherCohortPermissionLevel implements AuthorizationPredicate<RevokeCohortPermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowRevokeEqualOrHigherCohortPermissionLevel.class);

    private final CohortPermissionService cohortPermissionService;

    @Inject
    public AllowRevokeEqualOrHigherCohortPermissionLevel(CohortPermissionService cohortPermissionService) {
        this.cohortPermissionService = cohortPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Higher permission level required";
    }

    /**
     * Verify that the user that is revoking the permission level has an equal or higher permission
     * than the one he/she is trying to revoke.
     *
     * @param authenticationContext holds the authenticated user
     * @param message               the message to authorize
     * @return <code>false</code> when the permission level of the requesting user is lower than the permission
     * being revoked
     * <code>true</code> when the permission level of the requesting user is equal or higher than the permission
     * being revoked
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, RevokeCohortPermissionMessage message) {
        Account account = authenticationContext.getAccount();

        PermissionLevel requestingAccountPermission = cohortPermissionService.findHighestPermissionLevel(account.getId(),
                message.getCohortId()).block();

        if (log.isDebugEnabled()) {
            log.debug("checking authorization for revoking permission for cohort {}", message);
        }
        PermissionLevel targetPermission;
        if (message.getAccountId() != null) {
            targetPermission = cohortPermissionService.fetchAccountPermission(message.getAccountId(), message.getCohortId())
                    .block();
        } else {
            targetPermission = cohortPermissionService.fetchTeamPermission(message.getTeamId(), message.getCohortId())
                    .block();
        }

        if (targetPermission == null) {
            if (log.isDebugEnabled()) {
                log.debug("target permission level not found for cohort {}", message);
            }
            return false;
        }

        return requestingAccountPermission != null && requestingAccountPermission.isEqualOrHigherThan(targetPermission);
    }
}
