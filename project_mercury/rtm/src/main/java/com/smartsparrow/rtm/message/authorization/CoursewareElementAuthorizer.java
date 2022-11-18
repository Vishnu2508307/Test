package com.smartsparrow.rtm.message.authorization;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;
import com.smartsparrow.rtm.message.recv.courseware.activity.CreateActivityMessage;

import javax.inject.Inject;

public abstract class CoursewareElementAuthorizer implements AuthorizationPredicate<CoursewareElementMessage> {

    private static final Logger log = LoggerFactory.getLogger(CoursewareElementAuthorizer.class);
    
    private final CoursewareElementAuthorizerService coursewareElementAuthorizerService;

    @Inject
    public CoursewareElementAuthorizer(final CoursewareElementAuthorizerService coursewareElementAuthorizerService) {
        this.coursewareElementAuthorizerService = coursewareElementAuthorizerService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    public abstract PermissionLevel getAllowedPermissionLevel();

    @Override
    public boolean test(AuthenticationContext authenticationContext, CoursewareElementMessage message) {

        if (message instanceof CreateActivityMessage && message.getElementId() == null) {
            //TODO remove when PLT-4284 is done
            log.warn("Creating an author activity without a parent pathway. This is a temporary behaviour. " +
                    "Parent pathway will be required parameter soon. No permission checks for now. ");
            return true;
        }
        return coursewareElementAuthorizerService.authorize(authenticationContext, message.getElementId(), message.getElementType(), getAllowedPermissionLevel());

    }


}
