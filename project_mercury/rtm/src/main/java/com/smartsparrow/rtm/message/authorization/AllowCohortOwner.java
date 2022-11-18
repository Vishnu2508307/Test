package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.cohort.CohortMessage;

public class AllowCohortOwner implements AuthorizationPredicate<CohortMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowCohortOwner.class);

    private final CohortPermissionService cohortPermissionService;

    @Inject
    public AllowCohortOwner(CohortPermissionService cohortPermissionService) {
        this.cohortPermissionService = cohortPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, CohortMessage cohortMessage) {
        Account account = authenticationContext.getAccount();

        if (cohortMessage.getCohortId() != null) {
            if (account != null) {
                PermissionLevel permission = cohortPermissionService
                        .findHighestPermissionLevel(account.getId(), cohortMessage.getCohortId()).block();

                return permission != null && permission.equals(PermissionLevel.OWNER);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Could not verify permission level, `cohortId` was not supplied with the message {}", cohortMessage);
        }

        return false;
    }
}
