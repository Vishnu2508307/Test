package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.courseware.service.ThemePermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.courseware.theme.ThemeMessage;

public class AllowThemeReviewerOrHigher implements AuthorizationPredicate<ThemeMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowThemeReviewerOrHigher.class);

    private final ThemePermissionService themePermissionService;

    @Inject
    public AllowThemeReviewerOrHigher(ThemePermissionService themePermissionService) {
        this.themePermissionService = themePermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, ThemeMessage themeMessage) {
        Account account = authenticationContext.getAccount();

        if (themeMessage.getThemeId() != null && account != null) {
            PermissionLevel permissionLevel = themePermissionService
                    .findHighestPermissionLevel(account.getId(), themeMessage.getThemeId()).block();

            return permissionLevel != null &&
                    permissionLevel.isEqualOrHigherThan(PermissionLevel.REVIEWER);
        }

        if (log.isDebugEnabled()) {
            log.debug("could not authorize account, `themeId` field is missing {}", themeMessage);
        }

        return false;
    }
}
