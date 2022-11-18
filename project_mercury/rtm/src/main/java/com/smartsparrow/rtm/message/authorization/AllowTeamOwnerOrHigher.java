package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.iam.data.permission.team.TeamPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.team.TeamMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class AllowTeamOwnerOrHigher implements AuthorizationPredicate<TeamMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AllowTeamOwnerOrHigher.class);

    private final TeamService teamService;

    @Inject
    public AllowTeamOwnerOrHigher(TeamService teamService) {
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
                    teamPermission.getPermissionLevel().isEqualOrHigherThan(PermissionLevel.OWNER);
        }

        if (log.isDebugEnabled()) {
            log.debug("Could not authorize permission level for null account");
        }

        return false;
    }
}
