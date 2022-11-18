package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.learner.annotation.CreatorMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import javax.inject.Inject;

public class AllowListDeploymentAnnotationAuthorizer implements AuthorizationPredicate<CreatorMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowListDeploymentAnnotationAuthorizer.class);

    private final DeploymentService deploymentService;
    private final CohortPermissionService cohortPermissionService;

    @Inject
    public AllowListDeploymentAnnotationAuthorizer(final DeploymentService deploymentService,
                                                   final CohortPermissionService cohortPermissionService) {
        this.deploymentService = deploymentService;
        this.cohortPermissionService = cohortPermissionService;
    }

    /**
     * @param authenticationContext the context containing the authenticated user
     * @param message the incoming webSocket message
     * @return <code>true</code> if the request is permitted or <code>false</code> when not
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, CreatorMessage message) {
        Account account = authenticationContext.getAccount();
        if (message.getCreatorAccountId() != null) {
            if (account.getId().equals(message.getCreatorAccountId())) {
                return true;
            }
        }
        if (message.getDeploymentId() != null) {
            final DeployedActivity deployment = deploymentService.findDeployment(message.getDeploymentId()).block();
            if (deployment != null && deployment.getCohortId() != null) {
                if (account != null && account.getRoles() != null) {
                    PermissionLevel cohortPermission = cohortPermissionService.findHighestPermissionLevel(account.getId(), deployment.getCohortId()).block();
                    return account.getRoles().stream().anyMatch(AccountRole.WORKSPACE_ROLES::contains)
                            && PermissionLevel.REVIEWER.isEqualOrLowerThan(cohortPermission);
                }
            }
        }

        log.warn("Could not verify permission level, deployment could not be found", message);
        return false;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }
}
