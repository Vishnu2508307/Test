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
import com.smartsparrow.rtm.message.recv.AnnotationMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import javax.inject.Inject;

public class AllowUpdateDeploymentAnnotationAuthorizer implements AuthorizationPredicate<AnnotationMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowUpdateDeploymentAnnotationAuthorizer.class);

    private final DeploymentService deploymentService;
    private final AnnotationService annotationService;
    private final CohortPermissionService cohortPermissionService;


    @Inject
    public AllowUpdateDeploymentAnnotationAuthorizer(final DeploymentService deploymentService,
                                                     final AnnotationService annotationService,
                                                     final CohortPermissionService cohortPermissionService) {
        this.deploymentService = deploymentService;
        this.annotationService = annotationService;
        this.cohortPermissionService = cohortPermissionService;
    }

    /**
     * @param authenticationContext the context containing the authenticated user
     * @param message the incoming webSocket message
     * @return <code>true</code> if the request is permitted or <code>false</code> when not
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, AnnotationMessage message) {
        Account account = authenticationContext.getAccount();
        if (message.getAnnotationId() != null) {
            final LearnerAnnotation annotation = annotationService.findLearnerAnnotation(message.getAnnotationId()).block();
            if (annotation != null && annotation.getCreatorAccountId() != null) {
                if (account.getId().equals(annotation.getCreatorAccountId())) {
                    return true;
                }
            }
            if (annotation != null && annotation.getDeploymentId() != null) {
                final DeployedActivity deployment = deploymentService.findDeployment(annotation.getDeploymentId()).block();
                if (deployment != null && deployment.getCohortId() != null) {
                    if (account != null && account.getRoles() != null) {
                        PermissionLevel cohortPermission = cohortPermissionService.findHighestPermissionLevel(account.getId(), deployment.getCohortId()).block();
                        return account.getRoles().stream().anyMatch(AccountRole.WORKSPACE_ROLES::contains)
                                && PermissionLevel.REVIEWER.isEqualOrLowerThan(cohortPermission);
                    }

                }
            }
        }
        log.warn("Could not verify permission level, `annotationId` was not supplied with the message", message);

        return false;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }
}
