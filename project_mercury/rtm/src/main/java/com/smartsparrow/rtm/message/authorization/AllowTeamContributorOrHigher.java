package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.data.permission.team.TeamPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.team.TeamMessage;

public class AllowTeamContributorOrHigher implements AuthorizationPredicate<TeamMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowTeamContributorOrHigher.class);

    private final TeamService teamService;

    @Inject
    public AllowTeamContributorOrHigher(TeamService teamService) {
        this.teamService = teamService;
    }

    @Override
    public String getErrorMessage() {
        return "Higher permission level required";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, TeamMessage teamMessage) {
        Account account = authenticationContext.getAccount();

        if (account != null) {
            TeamPermission teamPermission = teamService.fetchPermission(account.getId(), teamMessage.getTeamId()).block();

            return teamPermission != null &&
                    teamPermission.getPermissionLevel().isEqualOrHigherThan(PermissionLevel.CONTRIBUTOR);
        }

        if (log.isDebugEnabled()) {
            log.debug("Could not authorize permission level for null account");
        }

        return false;
    }
}
