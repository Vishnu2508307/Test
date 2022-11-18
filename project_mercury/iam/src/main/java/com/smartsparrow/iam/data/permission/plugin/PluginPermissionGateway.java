package com.smartsparrow.iam.data.permission.plugin;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class PluginPermissionGateway {

    private static final Logger log = LoggerFactory.getLogger(PluginPermissionGateway.class);

    private final Session session;
    private final PluginPermissionByAccountMaterializer pluginPermissionByAccountMaterializer;
    private final PluginPermissionByAccountMutator pluginPermissionByAccountMutator;
    private final TeamPluginPermissionMaterializer teamPluginPermissionMaterializer;
    private final TeamPluginPermissionMutator teamPluginPermissionMutator;

    @Inject
    public PluginPermissionGateway(Session session,
                                   PluginPermissionByAccountMaterializer pluginPermissionByAccountMaterializer,
                                   PluginPermissionByAccountMutator pluginPermissionByAccountMutator,
                                   TeamPluginPermissionMaterializer teamPluginPermissionMaterializer,
                                   TeamPluginPermissionMutator teamPluginPermissionMutator) {
        this.session = session;
        this.pluginPermissionByAccountMaterializer = pluginPermissionByAccountMaterializer;
        this.pluginPermissionByAccountMutator = pluginPermissionByAccountMutator;
        this.teamPluginPermissionMaterializer = teamPluginPermissionMaterializer;
        this.teamPluginPermissionMutator = teamPluginPermissionMutator;
    }

    /**
     * Saves permission rule for plugin
     * @param pluginPermission plugin permission rule
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(final AccountPluginPermission pluginPermission) {
        Flux<? extends Statement> stmt = Mutators.upsert(
                Lists.newArrayList(pluginPermissionByAccountMutator), pluginPermission);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving plugin permission %s", pluginPermission),
                            throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Saves team permission rule for plugin
     * @param pluginPermission team plugin permission rule
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(final TeamPluginPermission pluginPermission) {
        Flux<? extends Statement> stmt = Mutators.upsert(
                Lists.newArrayList(teamPluginPermissionMutator), pluginPermission);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving team plugin permission %s", pluginPermission),
                            throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Deletes permission rule for a plugin
     * @param pluginPermission plugin permission rule
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(final AccountPluginPermission pluginPermission) {
        Flux<? extends Statement> stmt = Mutators.delete(
                Lists.newArrayList(pluginPermissionByAccountMutator), pluginPermission);
        return Mutators.execute(session, stmt).
                doOnError(throwable -> {
                    log.error(String.format("error while deleting account plugin permission %s", pluginPermission),
                            throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Deletes team permission rule for a plugin
     * @param pluginPermission team plugin permission rule
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(final TeamPluginPermission pluginPermission) {
        Flux<? extends Statement> stmt = Mutators.delete(
                Lists.newArrayList(teamPluginPermissionMutator), pluginPermission);
        return Mutators.execute(session, stmt).
                doOnError(throwable -> {
                    log.error(String.format("error while deleting team plugin permission %s", pluginPermission),
                            throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch permission for an account over a plugin
     *
     * @param accountId the account to search the permission for
     * @param pluginId the plugin entity the permission refers to
     * @return a {@link Flux} of {@link AccountPluginPermission}
     */
    public Flux<AccountPluginPermission> fetchAccountPermission(final UUID accountId, final UUID pluginId) {
        return ResultSets.query(session, pluginPermissionByAccountMaterializer.fetchByAccountPlugin(accountId, pluginId))
                .flatMapIterable(row->row)
                .map(this::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching account plugin permission for account %s and " +
                            "plugin %s", accountId, pluginId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch permission level for a team over a plugin
     *
     * @param teamId the team to search the permission for
     * @param pluginId the plugin entity the permission refers to
     * @return a {@link Flux} of {@link PermissionLevel}
     */
    public Mono<PermissionLevel> fetchTeamPermission(final UUID teamId, final UUID pluginId) {
        return ResultSets.query(session, teamPluginPermissionMaterializer.fetchPermissionLevel(teamId, pluginId))
                .flatMapIterable(row -> row)
                .map(teamPluginPermissionMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching team plugin permission for team %s and " +
                            "plugin %s", teamId, pluginId), throwable);
                    throw Exceptions.propagate(throwable);
                }).singleOrEmpty();
    }

    /**
     * Fetch permissions for an account over multiple plugins
     *
     * @param accountId the account to search the permissions for
     * @return a {@link Flux} of {@link AccountPluginPermission}
     */
    public Flux<AccountPluginPermission> fetchPermissions(final UUID accountId) {
        return ResultSets.query(session, pluginPermissionByAccountMaterializer.fetchByAccount(accountId))
                .flatMapIterable(row->row)
                .map(this::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching account plugin permissions for account %s",
                            accountId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    private AccountPluginPermission fromRow(Row row) {
        return new AccountPluginPermission()
                .setAccountId(row.getUUID("account_id"))
                .setPluginId(row.getUUID("plugin_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
