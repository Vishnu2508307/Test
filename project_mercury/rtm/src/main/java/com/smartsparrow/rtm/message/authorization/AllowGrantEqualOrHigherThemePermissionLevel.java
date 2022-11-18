package com.smartsparrow.rtm.message.authorization;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.service.ThemePermissionService;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionByAccount;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionByTeam;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.courseware.theme.GrantThemePermissionMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AllowGrantEqualOrHigherThemePermissionLevel implements AuthorizationPredicate<GrantThemePermissionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowGrantEqualOrHigherThemePermissionLevel.class);

    private final ThemePermissionService themePermissionService;

    @Inject
    public AllowGrantEqualOrHigherThemePermissionLevel(ThemePermissionService themePermissionService) {
        this.themePermissionService = themePermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    /**
     * Verify that the user granting the permission level has an equal or higher permission
     * than the one he/she is trying to grant.
     *
     * @param authenticationContext holds the authenticated user
     * @param message the message to authorize
     * @return <code>false</code> when the permission level of the requesting user is lower than the permission
     *         being granted/revoked
     *         <code>true</code> when the permission level of the requesting user is equal or higher than the permission
     *         being granted/revoked
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, GrantThemePermissionMessage message) {
        Account account = authenticationContext.getAccount();

        PermissionLevel requestingPermission = themePermissionService
                .findHighestPermissionLevel(account.getId(), message.getThemeId()).block();

        if (requestingPermission == null) {
            return false;
        }

        List<UUID> notAllowedTargetPermission = getNotPermitted(message, requestingPermission)
                .block();

        if (notAllowedTargetPermission != null && !notAllowedTargetPermission.isEmpty()) {
            String entity = message.getAccountIds() != null ? "accounts" : "teams";
            if (log.isDebugEnabled()) {
                log.warn(String.format(
                        "%s %s have an higher permission level over theme %s that cannot be overridden by %s",
                        entity,
                        notAllowedTargetPermission.toString(),
                        message.getThemeId(),
                        account.getId()));
            }
            // at least one account in the list has an higher permission level than the requesting account
            return false;
        }

        // all accounts in the list have either no permission at all or a lower permission than the requesting account
        return requestingPermission.isEqualOrHigherThan(message.getPermissionLevel());
    }

    /**
     * Get a list of ids that have an higher permission level than the requester. The id represents either an accountId
     * or a teamId
     *
     * @param message the incoming webSocket message
     * @param requestingPermission the requester permission level
     * @return a list of ids that are not allowed to be overridden
     */
    private Mono<List<UUID>> getNotPermitted(GrantThemePermissionMessage message,
                                             final PermissionLevel requestingPermission) {

        if (message.getAccountIds() != null) {
            return getAccountsPermissionLevelFor(message.getAccountIds(), message.getThemeId())
                    .filter(permission -> requestingPermission.isLowerThan(permission.getPermissionLevel()))
                    .map(ThemePermissionByAccount::getAccountId)
                    .collectList();
        } else {
            return getTeamsPermissionLevelFor(message.getTeamIds(), message.getThemeId())
                    .filter(permission -> requestingPermission.isLowerThan(permission.getPermissionLevel()))
                    .map(ThemePermissionByTeam::getTeamId)
                    .collectList();
        }
    }

    /**
     * Find all the team theme permission for each team id in the list
     *
     * @param teamIds the team ids to find the permission for
     * @param themeId the theme id the team permission refers to
     * @return a flux of theme permission by team
     */
    private Flux<ThemePermissionByTeam> getTeamsPermissionLevelFor(List<UUID> teamIds, final UUID themeId) {
        return Flux.just(teamIds.toArray(new UUID[0]))
                .flatMap(teamId -> themePermissionService.fetchThemePermissionByTeam(teamId, themeId)
                        .flatMap(permissionLevel -> Mono.just(new ThemePermissionByTeam()
                                                                      .setPermissionLevel(permissionLevel)
                                                                      .setTeamId(teamId)
                                                                      .setThemeId(themeId))));
    }

    /**
     * Find all the account theme permission for each account id in the list
     *
     * @param accountIds the account ids to find the permission for
     * @param themeId the theme id the permission refers to
     * @return a flux of theme permission by account
     */
    private Flux<ThemePermissionByAccount> getAccountsPermissionLevelFor(List<UUID> accountIds, final UUID themeId) {
        return Flux.just(accountIds.toArray(new UUID[0]))
                .flatMap(accountId -> themePermissionService.fetchThemePermissionByAccount(accountId, themeId));
    }

}
