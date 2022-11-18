package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.learner.LearnerAnnotationMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.UUID;


public class AllowDeleteLearnerAnnotationAuthorizer implements AuthorizationPredicate<LearnerAnnotationMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowDeleteLearnerAnnotationAuthorizer.class);

    private final AnnotationService annotationService;
    private final DeploymentService deploymentService;
    private final CohortPermissionService cohortPermissionService;

    @Inject
    public AllowDeleteLearnerAnnotationAuthorizer(final AnnotationService annotationService,
                                                  final DeploymentService deploymentService,
                                                  final CohortPermissionService cohortPermissionService) {
        this.annotationService = annotationService;
        this.deploymentService = deploymentService;
        this.cohortPermissionService = cohortPermissionService;
    }

    /**
     * Allow creator or instructor to delete a learner annotation
     *
     * @param authenticationContext the context containing the authenticated user
     * @param message               the incoming webSocket message
     * @return <code>true</code> if the request is permitted or <code>false</code> when not
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, LearnerAnnotationMessage message) {
        Account account = authenticationContext.getAccount();
        final LearnerAnnotation annotation = annotationService.findLearnerAnnotation(message.getAnnotationId()).block();

        if (annotation != null) {
            if (!account.getId().equals(annotation.getCreatorAccountId())) {
                //instructor on the cohort
                return testCohortInstructor(account, annotation.getDeploymentId());
            } else {
                return true;
            }
        }
        if (log.isDebugEnabled()) {
            log.jsonDebug("Could not verify permission level", new HashMap<String, Object>() {
                {
                    put("annotationId", message.getAnnotationId());
                }
            });
        }
        return false;
    }

    private boolean testCohortInstructor(Account account, UUID deploymentId) {
        if (deploymentId != null) {
            final DeployedActivity deployment = deploymentService.findDeployment(deploymentId).block();
            if (deployment != null && deployment.getCohortId() != null) {
                UUID cohortId = deployment.getCohortId();

                if (cohortId != null) {
                    if (account != null && account.getRoles() != null) {
                        PermissionLevel cohortPermission = cohortPermissionService.findHighestPermissionLevel(account.getId(), cohortId).block();
                        return account.getRoles().stream().anyMatch(AccountRole.WORKSPACE_ROLES::contains)
                                && PermissionLevel.REVIEWER.isEqualOrLowerThan(cohortPermission);
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.jsonDebug("Could not verify permission level", new HashMap<String, Object>() {
                {
                    put("deploymentId", deploymentId);
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
