package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.courseware.annotation.ResolveCoursewareAnnotationMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class AllowResolveCoursewareAnnotationAuthorizer implements AuthorizationPredicate<ResolveCoursewareAnnotationMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowResolveCoursewareAnnotationAuthorizer.class);

    private final CoursewareElementAuthorizerService coursewareElementAuthorizerService;

    @Inject
    public AllowResolveCoursewareAnnotationAuthorizer(final CoursewareElementAuthorizerService coursewareElementAuthorizerService) {
        this.coursewareElementAuthorizerService = coursewareElementAuthorizerService;
    }


    /**
     * Verify that the user granting the permission level has an equal or higher permission
     * @param authenticationContext holds the authenticated user
     * @param message the message to authorize
     * @return <code>false</code> when the permission level of the requesting user is lower than the permission
     * being granted/revoked
     * <code>true</code> when the permission level of the requesting user is equal or higher than the permission
     * being granted/revoked
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, ResolveCoursewareAnnotationMessage message) {
        return coursewareElementAuthorizerService.authorize(authenticationContext, message.getRootElementId(),
                CoursewareElementType.ACTIVITY, PermissionLevel.REVIEWER);

    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }
}
