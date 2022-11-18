package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.AnnotationMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;

public class AllowUpdateCoursewareAnnotationAuthorizer implements AuthorizationPredicate<AnnotationMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowUpdateCoursewareAnnotationAuthorizer.class);

    private final AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher;
    private final AllowCoursewareElementReviewerOrHigher allowCoursewareElementReviewerOrHigher;
    private final AnnotationService annotationService;

    @Inject
    public AllowUpdateCoursewareAnnotationAuthorizer(final AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher,
                                                     final AllowCoursewareElementReviewerOrHigher allowCoursewareElementReviewerOrHigher,
                                                     final AnnotationService annotationService) {
        this.allowCoursewareElementContributorOrHigher = allowCoursewareElementContributorOrHigher;
        this.allowCoursewareElementReviewerOrHigher = allowCoursewareElementReviewerOrHigher;
        this.annotationService = annotationService;
    }

    /**
     * Allow contributor or higher users if motivation is identifying, otherwise allow reviewer or higher users.
     *
     * @param authenticationContext the context containing the authenticated user
     * @param message               the incoming webSocket message
     * @return <code>true</code> if the request is permitted or <code>false</code> when not
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, AnnotationMessage message) {
        CoursewareAnnotation annotation = annotationService.findCoursewareAnnotation(message.getAnnotationId()).block();
        if (annotation != null) {
            if (annotation.getMotivation() != null && annotation.getMotivation().equals(Motivation.identifying)) {
                return allowCoursewareElementContributorOrHigher.test(authenticationContext, message);
            }
            return allowCoursewareElementReviewerOrHigher.test(authenticationContext, message);
        }
        if (log.isDebugEnabled()) {
            log.jsonDebug("Could not authorize courseware annotation", new HashMap<String, Object>() {
                {
                    put("annotationId", message.getAnnotationId());
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
