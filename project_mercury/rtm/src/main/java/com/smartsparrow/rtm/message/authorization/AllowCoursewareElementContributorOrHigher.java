package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

public class AllowCoursewareElementContributorOrHigher extends CoursewareElementAuthorizer
        implements AuthorizationPredicate<CoursewareElementMessage> {

    @Inject
    public AllowCoursewareElementContributorOrHigher(final CoursewareElementAuthorizerService coursewareElementAuthorizerService) {
        super(coursewareElementAuthorizerService);
    }
    
    @Override
    public PermissionLevel getAllowedPermissionLevel() {
        return PermissionLevel.CONTRIBUTOR;
    }
}
