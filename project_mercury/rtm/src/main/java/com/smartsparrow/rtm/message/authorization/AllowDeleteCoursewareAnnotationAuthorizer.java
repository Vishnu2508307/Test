package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.AnnotationMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

/**
 * Authorization predicate that allows authenticated users only.
 *
 */
public class AllowDeleteCoursewareAnnotationAuthorizer implements AuthorizationPredicate<AnnotationMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowDeleteCoursewareAnnotationAuthorizer.class);

    private final AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher;
    private final AnnotationService annotationService;

    @Inject
    public AllowDeleteCoursewareAnnotationAuthorizer(final AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher,
                                                     final AnnotationService annotationService) {
        this.allowCoursewareElementContributorOrHigher = allowCoursewareElementContributorOrHigher;
        this.annotationService = annotationService;
    }

    /**
     * Allow contributor or higher users if motivation is identifying, otherwise allow reviewer or higher users.
     *
     * @param authenticationContext the context containing the authenticated user
     * @param message the incoming webSocket message
     * @return <code>true</code> if the request is permitted or <code>false</code> when not
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, AnnotationMessage message) {
        final CoursewareAnnotation annotation = annotationService.findCoursewareAnnotation(message.getAnnotationId()).block();

        if (annotation != null) {
            if (annotation.getMotivation() != null && annotation.getMotivation().equals(Motivation.identifying)) {
                return allowCoursewareElementContributorOrHigher.test(authenticationContext, message);
            }

            // check if the creator
            return authenticationContext.getAccount().getId().equals(annotation.getCreatorAccountId());
        }
        if (log.isDebugEnabled()) {
            log.debug("Could not verify permission level, annotation was not found");
        }
        return false;

    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }
}
