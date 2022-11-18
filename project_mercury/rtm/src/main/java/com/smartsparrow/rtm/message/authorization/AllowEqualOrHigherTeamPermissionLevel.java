package com.smartsparrow.rtm.message.authorization;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.data.permission.team.TeamPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.team.TeamPermissionMessage;

public class AllowEqualOrHigherTeamPermissionLevel implements AuthorizationPredicate<TeamPermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowEqualOrHigherTeamPermissionLevel.class);

    private final TeamService teamService;

    @Inject
    public AllowEqualOrHigherTeamPermissionLevel(TeamService teamService) {
        this.teamService = teamService;
    }

    @Override
    public String getErrorMessage() {
        return "Higher permission level required";
    }

    /**
     * Check that the permission level the user is trying to grant/revoke is a equal or lower permission level than the
     * his/her own
     *
     * @param authenticationContext the context containing the authenticated user
     * @param message the incoming webSocket message
     * @return <code>true</code> if the request is permitted or <code>false</code> when not
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, TeamPermissionMessage message) {
        Account account = authenticationContext.getAccount();

        TeamPermission requesterPermission = teamService.fetchPermission(account.getId(), message.getTeamId()).block();

        if (requesterPermission == null) {
            return false;
        }

        List<Boolean> teamPermissionTestForAccount = teamService
                .fetchPermissions(message.getAccountIds(), message.getTeamId())
                .map(teamPermission -> {
                    if (message.getPermissionLevel() != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("attempt to grant permission {}", message.toString());
                        }
                        return canGrant(message, requesterPermission, teamPermission);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("attempt to revoke permission {}", message.toString());
                        }
                        return canRevoke(requesterPermission, teamPermission);
                    }
                })
                .collectList()
                .block();

        if (teamPermissionTestForAccount != null && teamPermissionTestForAccount.size() > 0) {
            if (message.getPermissionLevel() != null) {
                return requesterPermission.getPermissionLevel().isEqualOrHigherThan(message.getPermissionLevel()) &&
                        teamPermissionTestForAccount
                                .stream()
                                .allMatch(aBoolean -> aBoolean.equals(true));
            } else {
                return teamPermissionTestForAccount
                        .stream()
                        .allMatch(aBoolean -> aBoolean.equals(true));
            }
        }
        if(message.getPermissionLevel() != null) {
            return requesterPermission.getPermissionLevel().isEqualOrHigherThan(message.getPermissionLevel());
        }
        //If the permission is not found for revoke just return true
        return true;
    }

    /**
     * Check if the requester is allowed to revoke the target permission
     *
     * @param requesterPermission the requesting account permission
     * @param targetAccountPermission the target account permission
     * @return <code>true</code> if the permission can be revoked, <code>false</code> when not.
     */
    private boolean canRevoke(TeamPermission requesterPermission, TeamPermission targetAccountPermission) {
        if (targetAccountPermission == null) {
            return false;
        }
        return requesterPermission.getPermissionLevel().isEqualOrHigherThan(targetAccountPermission.getPermissionLevel());
    }

    /**
     * Check if the requester can grant the permission level to the target user.
     *
     * @param message the incoming webSocket message
     * @param requesterPermission the requester permission level
     * @param targetAccountPermission the target account permission (when it exists)
     * @return <code>true</code> when either the target permission does not exists or its lower than the requester
     * permission, <code>false</code> when the target permission exists and is higher than the requester permission.
     */
    private boolean canGrant(TeamPermissionMessage message, TeamPermission requesterPermission, TeamPermission targetAccountPermission) {
        if (targetAccountPermission != null) {
            return requesterPermission.getPermissionLevel().isEqualOrHigherThan(targetAccountPermission.getPermissionLevel())
                    && requesterPermission.getPermissionLevel().isEqualOrHigherThan(message.getPermissionLevel());
        }
        return requesterPermission.getPermissionLevel().isEqualOrHigherThan(message.getPermissionLevel());
    }
}
