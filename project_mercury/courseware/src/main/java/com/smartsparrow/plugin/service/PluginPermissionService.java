package com.smartsparrow.plugin.service;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.data.permission.plugin.AccountPluginPermission;
import com.smartsparrow.iam.data.permission.plugin.PluginPermissionGateway;
import com.smartsparrow.iam.data.permission.plugin.TeamPluginPermission;
import com.smartsparrow.iam.service.HighestPermissionLevel;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.plugin.data.PluginAccessGateway;
import com.smartsparrow.plugin.data.PluginAccount;
import com.smartsparrow.plugin.data.PluginAccountCollaborator;
import com.smartsparrow.plugin.data.PluginByTeam;
import com.smartsparrow.plugin.data.PluginTeamCollaborator;
import com.smartsparrow.plugin.lang.PluginPermissionPersistenceException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class PluginPermissionService {

    private static final Logger log = LoggerFactory.getLogger(PluginPermissionService.class);

    private final PluginAccessGateway pluginAccessGateway;
    private final PluginPermissionGateway pluginPermissionGateway;
    private final TeamService teamService;

    @Inject
    public PluginPermissionService(final PluginPermissionGateway pluginPermissionGateway,
                                   final PluginAccessGateway pluginAccessGateway,
                                   final TeamService teamService) {
        this.pluginPermissionGateway = pluginPermissionGateway;
        this.pluginAccessGateway = pluginAccessGateway;
        this.teamService = teamService;
    }

    /**
     * Saves permission rule on a plugin {@param pluginId} for an account {@param accountId}
     *
     * @param accountId an account
     * @param pluginId  a plugin
     * @param level     permission level
     * @throws PluginPermissionPersistenceException when an error occur while saving the permissions
     */
    public Flux<Void> saveAccountPermission(final UUID accountId, final UUID pluginId, final PermissionLevel level) {

        //save plugin into a list of plugins visible for the creator
        PluginAccount pluginAccount = new PluginAccount()
                .setAccountId(accountId)
                .setPluginId(pluginId);

        //save creator's account into list of plugin collaborators
        PluginAccountCollaborator pluginCollaborator = new PluginAccountCollaborator()
                .setAccountId(accountId)
                .setPluginId(pluginId)
                .setPermissionLevel(level);

        //save plugin permission for the account
        AccountPluginPermission pluginPermission = new AccountPluginPermission()
                .setAccountId(accountId)
                .setPermissionLevel(level)
                .setPluginId(pluginId);

        return Flux.merge(
                pluginAccessGateway.persist(pluginAccount),
                pluginAccessGateway.persist(pluginCollaborator),
                pluginPermissionGateway.persist(pluginPermission)
        ).doOnError(t -> {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Error saving permission: %s", t.getMessage()), t);
            }
            throw new PluginPermissionPersistenceException("Error granting permission");
        });
    }

    /**
     * Revokes permission to an account over a plugin.
     *
     * @param accountId the account id
     * @param pluginId  the plugin id
     * @throws PluginPermissionPersistenceException when an error occur while deleting the permissions
     */
    public Flux<Void> deleteAccountPermission(final UUID accountId, final UUID pluginId)
            throws PluginPermissionPersistenceException {

        // create the plugin account association to delete
        PluginAccount pluginAccount = new PluginAccount()
                .setAccountId(accountId)
                .setPluginId(pluginId);

        // create the plugin account collaborator to delete
        PluginAccountCollaborator pluginCollaborator = new PluginAccountCollaborator()
                .setAccountId(accountId)
                .setPluginId(pluginId);

        // create the permission object to delete
        AccountPluginPermission pluginPermission = new AccountPluginPermission()
                .setAccountId(accountId)
                .setPluginId(pluginId);

        return Flux.merge(
                pluginAccessGateway.delete(pluginAccount),
                pluginAccessGateway.delete(pluginCollaborator),
                pluginPermissionGateway.delete(pluginPermission)
        ).doOnError(t -> {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Error revoking permission: %s", t.getMessage()), t);
            }
            throw new PluginPermissionPersistenceException("Error revoking permissions");
        });
    }

    /**
     * Fetch the permission level of an account over a plugin entity
     *
     * @param accountId the account permission
     * @param pluginId  the target plugin
     * @return a {@link Flux} of {@link AccountPluginPermission}
     */
    public Flux<AccountPluginPermission> fetchAccountPermission(final UUID accountId, final UUID pluginId) {
        return pluginPermissionGateway.fetchAccountPermission(accountId, pluginId);
    }

    /**
     * Saves permission rule on a plugin for a team
     *
     * @param teamId   a team
     * @param pluginId a plugin
     * @param level    permission level
     * @throws PluginPermissionPersistenceException when an error occur while saving the permissions
     */
    public Flux<Void> saveTeamPermission(final UUID teamId, final UUID pluginId, final PermissionLevel level) {

        //save plugin into a list of plugins visible for a team
        PluginByTeam pluginByTeam = new PluginByTeam()
                .setTeamId(teamId)
                .setPluginId(pluginId);

        //save team into list of plugin team collaborators
        PluginTeamCollaborator pluginCollaborator = new PluginTeamCollaborator()
                .setTeamId(teamId)
                .setPluginId(pluginId)
                .setPermissionLevel(level);

        //save plugin permission for the team
        TeamPluginPermission pluginPermission = new TeamPluginPermission()
                .setTeamId(teamId)
                .setPermissionLevel(level)
                .setPluginId(pluginId);

        return Flux.merge(
                pluginAccessGateway.persist(pluginByTeam),
                pluginAccessGateway.persist(pluginCollaborator),
                pluginPermissionGateway.persist(pluginPermission)
        ).doOnError(t -> {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Error saving team permission for plugin '%s' for team '%s'", pluginId, teamId), t);
            }
            throw new PluginPermissionPersistenceException("Error granting permission");
        });
    }

    /**
     * Revokes permission to a team over a plugin.
     *
     * @param teamId   the team id
     * @param pluginId the plugin id
     * @throws PluginPermissionPersistenceException when an error occur while deleting the permissions
     */
    public Flux<Void> deleteTeamPermission(final UUID teamId, final UUID pluginId)
            throws PluginPermissionPersistenceException {

        // create the plugin team association to delete
        PluginByTeam pluginByTeam = new PluginByTeam()
                .setTeamId(teamId)
                .setPluginId(pluginId);

        // create the plugin team collaborator to delete
        PluginTeamCollaborator pluginCollaborator = new PluginTeamCollaborator()
                .setTeamId(teamId)
                .setPluginId(pluginId);

        // create the permission object to delete
        TeamPluginPermission pluginPermission = new TeamPluginPermission()
                .setTeamId(teamId)
                .setPluginId(pluginId);

        return Flux.merge(
                pluginAccessGateway.delete(pluginByTeam),
                pluginAccessGateway.delete(pluginCollaborator),
                pluginPermissionGateway.delete(pluginPermission)
        ).doOnError(t -> {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Error revoking permission on plugin '%s' for team '%s'", pluginId, teamId), t);
            }
            throw new PluginPermissionPersistenceException("Error revoking permissions");
        });
    }

    /**
     * Fetch the permission level of a team over a plugin entity
     *
     * @param teamId   the account permission
     * @param pluginId the target plugin
     * @return a {@link Flux} of {@link AccountPluginPermission}
     */
    public Mono<PermissionLevel> fetchTeamPermission(final UUID teamId, final UUID pluginId) {
        return pluginPermissionGateway.fetchTeamPermission(teamId, pluginId);
    }

    /**
     * Finds all the permission the account has over the plugin. The methods finds both account and team specific
     * permissions over the plugin, then the highest permission level is returned.
     *
     * @param accountId the account id to search the permissions for
     * @param pluginId  the plugin id the account should have permission over
     * @return a mono of permission level
     */
    public Mono<PermissionLevel> findHighestPermissionLevel(final UUID accountId, final UUID pluginId) {
        return teamService.findTeamsForAccount(accountId)
                .onErrorResume(ex -> {
                    if (log.isDebugEnabled()) {
                        log.debug("could not fetch teams for account {} {}", accountId, ex.getMessage());
                    }
                    return Mono.empty();
                })
                .map(teamAccount -> fetchTeamPermission(teamAccount.getTeamId(), pluginId))
                .flatMap(one -> one)
                .mergeWith(fetchAccountPermission(accountId, pluginId)
                        .map(AccountPluginPermission::getPermissionLevel))
                .reduce(new HighestPermissionLevel());
    }

    /**
     * Remove all accounts permissions for a plugin.
     * @param pluginId the plugin id
     */
    public Flux<Void> deleteAccountPermissions(final UUID pluginId) {
        return pluginAccessGateway.fetchAccounts(pluginId)
                .flatMap(a -> deleteAccountPermission(a.getAccountId(), pluginId));
    }

    /**
     * Remove all teams permissions for a plugin.
     * @param pluginId the plugin id
     */
    public Flux<Void> deleteTeamPermissions(final UUID pluginId) {
        return pluginAccessGateway.fetchTeams(pluginId)
                .flatMap(a -> deleteTeamPermission(a.getTeamId(), pluginId));
    }
}
