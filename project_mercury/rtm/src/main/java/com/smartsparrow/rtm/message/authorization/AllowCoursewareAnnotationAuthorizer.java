package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.CoursewareElementMotivationMessage;

/**
 * Authorization predicate that allows authenticated users only.
 *
 */
public class AllowCoursewareAnnotationAuthorizer implements AuthorizationPredicate<CoursewareElementMotivationMessage> {

    private final AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher;
    private final AllowCoursewareElementReviewerOrHigher allowCoursewareElementReviewerOrHigher;

    @Inject
    public AllowCoursewareAnnotationAuthorizer(final AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher,
                                               final AllowCoursewareElementReviewerOrHigher allowCoursewareElementReviewerOrHigher) {
        this.allowCoursewareElementContributorOrHigher = allowCoursewareElementContributorOrHigher;
        this.allowCoursewareElementReviewerOrHigher = allowCoursewareElementReviewerOrHigher;
    }

    /**
     * Allow contributor or higher users if motivation is identifying, otherwise allow reviewer or higher users.
     *
     * @param authenticationContext the context containing the authenticated user
     * @param message the incoming webSocket message
     * @return <code>true</code> if the request is permitted or <code>false</code> when not
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, CoursewareElementMotivationMessage message) {
        if (message.getMotivation() != null && message.getMotivation().equals(Motivation.identifying)) {
            return allowCoursewareElementContributorOrHigher.test(authenticationContext, message);
        }
        return allowCoursewareElementReviewerOrHigher.test(authenticationContext, message);
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }
}
