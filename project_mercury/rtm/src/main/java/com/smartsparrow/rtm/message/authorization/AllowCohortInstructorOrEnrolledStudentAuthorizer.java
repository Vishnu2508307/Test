package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;

import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.learner.annotation.DeploymentMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.UUID;

public class AllowCohortInstructorOrEnrolledStudentAuthorizer implements AuthorizationPredicate<DeploymentMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowCohortInstructorOrEnrolledStudentAuthorizer.class);

    private final DeploymentService deploymentService;
    private final CohortEnrollmentService cohortEnrollmentService;
    private final CohortPermissionService cohortPermissionService;

    @Inject
    public AllowCohortInstructorOrEnrolledStudentAuthorizer(final DeploymentService deploymentService,
                                                            final CohortEnrollmentService cohortEnrollmentService,
                                                            final CohortPermissionService cohortPermissionService) {
        this.deploymentService = deploymentService;
        this.cohortEnrollmentService = cohortEnrollmentService;
        this.cohortPermissionService = cohortPermissionService;
    }

    /**
     * Method to authorize create deployment annotation for enrolled student or instructor
     *
     * @param authenticationContext the context containing the authenticated user
     * @param message               the incoming webSocket message
     * @return <code>true</code> if the request is permitted or <code>false</code> when not
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, DeploymentMessage message) {
        Account account = authenticationContext.getAccount();
        if (message.getDeploymentId() != null) {
            final DeployedActivity deployment = deploymentService.findDeployment(message.getDeploymentId()).block();
            if (deployment != null && deployment.getCohortId() != null) {
                UUID cohortId = deployment.getCohortId();
                return testInstructor(cohortId, account) || testEnrollStudent(cohortId, account);
            }
        }
        log.jsonWarn("Could not verify permission level", new HashMap<String, Object>() {
            {
                put("message", message.toString());
            }
        });

        return false;
    }

    private boolean testEnrollStudent(UUID cohortId, Account account) {
        if (cohortId != null) {
            if (account != null && account.getId() != null) {
                return cohortEnrollmentService.getAccountEnrollment(account.getId(), cohortId).block() != null;
            }
        }
        if (log.isDebugEnabled()) {
            log.jsonDebug("Could not verify permission level", new HashMap<String, Object>() {
                {
                    put("cohortId", cohortId);
                }
            });
        }
        return false;
    }

    private boolean testInstructor(UUID cohortId, Account account) {
        if (cohortId != null) {
            if (account != null && account.getRoles() != null) {
                PermissionLevel cohortPermission = cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId).block();
                return account.getRoles().stream().anyMatch(AccountRole.WORKSPACE_ROLES::contains)
                        && PermissionLevel.REVIEWER.isEqualOrLowerThan(cohortPermission);
            }
        }
        if (log.isDebugEnabled()) {
            log.jsonDebug("Could not verify permission level", new HashMap<String, Object>() {
                {
                    put("cohortId", cohortId);
                }
            });
        }
        return false;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }
}
