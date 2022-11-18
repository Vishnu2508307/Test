package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.courseware.service.ThemePermissionService;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionByAccount;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.courseware.theme.RevokeThemePermissionMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class AllowRevokeEqualOrHigherThemePermissionLevel implements AuthorizationPredicate<RevokeThemePermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowRevokeEqualOrHigherThemePermissionLevel.class);

    private final ThemePermissionService themePermissionService;

    @Inject
    public AllowRevokeEqualOrHigherThemePermissionLevel(ThemePermissionService themePermissionService) {
        this.themePermissionService = themePermissionService;
    }


    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    /**
     * Validates that the requesting account highest permission level is higher than the permission level the account is
     * trying to revoke. The authorizer the message to either supply a {@link RevokeThemePermissionMessage#getAccountId()}
     * or {@link RevokeThemePermissionMessage#getTeamId()} at least one of those fields has to be supplied and not both
     * at the same time for the authorizer to work its logic. Breaking this rule will result in an unauthorized return.
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, RevokeThemePermissionMessage message) {

        Account account = authenticationContext.getAccount();
        PermissionLevel requestingPermission = themePermissionService
                .findHighestPermissionLevel(account.getId(), message.getThemeId()).block();

        if (requestingPermission != null) {
            if (message.getAccountId() != null && message.getTeamId() == null) {

                PermissionLevel targetAccountPermission = themePermissionService.fetchThemePermissionByAccount(message.getAccountId(),
                                                                                                               message.getThemeId())
                        .map(ThemePermissionByAccount::getPermissionLevel)
                        .block();

                return targetAccountPermission != null && requestingPermission.isEqualOrHigherThan(
                        targetAccountPermission);

            } else if (message.getAccountId() == null && message.getTeamId() != null) {
                // revoke the team id
                PermissionLevel targetTeamPermission = themePermissionService
                        .fetchThemePermissionByTeam(message.getTeamId(), message.getThemeId())
                        .block();

                return targetTeamPermission != null && requestingPermission.isEqualOrHigherThan(targetTeamPermission);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("could not authorize teamId {} and accountId {}", message.getTeamId(), message.getAccountId());
        }

        return false;
    }
}
