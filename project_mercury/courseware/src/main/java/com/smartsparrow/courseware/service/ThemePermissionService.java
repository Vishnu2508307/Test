package com.smartsparrow.courseware.service;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.iam.data.permission.workspace.ThemePermissionByAccount;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionGateway;
import com.smartsparrow.iam.service.HighestPermissionLevel;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

@Singleton
public class ThemePermissionService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ThemePermissionService.class);

    private final ThemePermissionGateway themePermissionGateway;
    private final TeamService teamService;

    @Inject
    public ThemePermissionService(final ThemePermissionGateway themePermissionGateway,
                                  final TeamService teamService) {
        this.themePermissionGateway = themePermissionGateway;
        this.teamService = teamService;
    }

    /**
     * Finds all the permission the account has over the theme. The methods finds both account and team specific
     * permissions over the theme, then the highest permission level is returned.
     *
     * @param accountId the account id to search the permissions for
     * @param themeId  the theme id the account should have permission over
     * @return a mono of permission level
     */
    public Mono<PermissionLevel> findHighestPermissionLevel(final UUID accountId, final UUID themeId) {
        return teamService.findTeamsForAccount(accountId)
                .map(teamAccount -> fetchThemePermissionByTeam(teamAccount.getTeamId(), themeId))
                .flatMap(one -> one)
                .mergeWith(fetchThemePermissionByAccount(accountId, themeId)
                                   .map(ThemePermissionByAccount::getPermissionLevel))
                .reduce(new HighestPermissionLevel());
    }

    /**
     * Fetch theme permission level by team
     * @param teamId the team id
     * @param themeId the theme id
     * @return mono of permission level
     */
    public Mono<PermissionLevel> fetchThemePermissionByTeam(final UUID teamId, final UUID themeId) {
        return themePermissionGateway.fetchThemePermissionByTeam(teamId, themeId);
    }

    /**
     *Fetch theme permission level by account
     * @param accountId the account id
     * @param themeId the theme id
     * @return mono of theme permission account
     */
    public Mono<ThemePermissionByAccount> fetchThemePermissionByAccount(final UUID accountId, final UUID themeId) {
        return themePermissionGateway.fetchThemePermissionByAccount(accountId, themeId);
    }

}
