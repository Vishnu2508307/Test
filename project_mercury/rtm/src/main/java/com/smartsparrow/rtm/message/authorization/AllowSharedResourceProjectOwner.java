package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.user_content.SharedResourceMessage;

public class AllowSharedResourceProjectOwner implements AuthorizationPredicate<SharedResourceMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowSharedResourceProjectOwner.class);


    private final CoursewareElementAuthorizerService coursewareElementAuthorizerService;

    @Inject
    public AllowSharedResourceProjectOwner(final CoursewareElementAuthorizerService coursewareElementAuthorizerService) {
        this.coursewareElementAuthorizerService = coursewareElementAuthorizerService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    @Override
    public boolean test(final AuthenticationContext authenticationContext, final SharedResourceMessage sharedResourceMessage) {
        return coursewareElementAuthorizerService.authorize(authenticationContext, sharedResourceMessage.getResourceId(), CoursewareElementType.ACTIVITY, PermissionLevel.OWNER);
    }
}
