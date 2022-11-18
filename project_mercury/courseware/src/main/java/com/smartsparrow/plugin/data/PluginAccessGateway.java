package com.smartsparrow.plugin.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class PluginAccessGateway {

    private static final Logger log = LoggerFactory.getLogger(PluginAccessGateway.class);

    private final Session session;
    //account accessibility
    private final PluginByAccountMaterializer pluginByAccountMaterializer;
    private final PluginByAccountMutator pluginByAccountMutator;
    private final AccountByPluginMaterializer accountByPluginMaterializer;
    private final AccountByPluginMutator accountByPluginMutator;
    //team accessibility
    private final PluginByTeamMaterializer pluginByTeamMaterializer;
    private final PluginByTeamMutator pluginByTeamMutator;
    private final PluginTeamCollaboratorMaterializer pluginTeamCollaboratorMaterializer;
    private final PluginTeamCollaboratorMutator pluginTeamCollaboratorMutator;


    @Inject
    public PluginAccessGateway(Session session,
                               PluginByAccountMaterializer pluginByAccountMaterializer,
                               PluginByAccountMutator pluginByAccountMutator,
                               AccountByPluginMaterializer accountByPluginMaterializer,
                               AccountByPluginMutator accountByPluginMutator,
                               PluginByTeamMaterializer pluginByTeamMaterializer,
                               PluginByTeamMutator pluginByTeamMutator,
                               PluginTeamCollaboratorMaterializer pluginTeamCollaboratorMaterializer,
                               PluginTeamCollaboratorMutator pluginTeamCollaboratorMutator) {
        this.session = session;
        this.pluginByAccountMaterializer = pluginByAccountMaterializer;
        this.pluginByAccountMutator = pluginByAccountMutator;
        this.accountByPluginMaterializer = accountByPluginMaterializer;
        this.accountByPluginMutator = accountByPluginMutator;
        this.pluginByTeamMaterializer = pluginByTeamMaterializer;
        this.pluginByTeamMutator = pluginByTeamMutator;
        this.pluginTeamCollaboratorMutator = pluginTeamCollaboratorMutator;
        this.pluginTeamCollaboratorMaterializer = pluginTeamCollaboratorMaterializer;
    }

    /**
     * Saves a plugin into a list of visible plugins for an account
     * @param accountPlugin plugin-account information
     * @return Flux of Void
     */
    public Flux<Void> persist(final PluginAccount accountPlugin) {
        Flux<? extends Statement> stmt = Mutators.upsert(pluginByAccountMutator, accountPlugin);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving plugin account %s", accountPlugin),
                            throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Saves an account into a list of accounts who have direct access to a plugin
     * @param pluginCollaborator plugin-account information
     * @return Flux of Void
     */
    public Flux<Void> persist(final PluginAccountCollaborator pluginCollaborator) {
        Flux<? extends Statement> stmt = Mutators.upsert(accountByPluginMutator, pluginCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving plugin collaborator %s", pluginCollaborator),
                            throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Saves plugin-team relationships: adds a plugin into a list of plugins for a team
     * @param pluginByTeam plugin-team information
     * @return Flux of Void
     */
    public Flux<Void> persist(final PluginByTeam pluginByTeam) {
        Flux<? extends Statement> stmt = Mutators.upsert(pluginByTeamMutator, pluginByTeam);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving pluginByTeam %s", pluginByTeam), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persists the relationship between a plugin and team: adds a team collaborator for a plugin
     * @param pluginTeamCollaborator plugin-team information
     * @return Flux of Void
     */
    public Flux<Void> persist(final PluginTeamCollaborator pluginTeamCollaborator) {
        Flux<? extends Statement> stmt = Mutators.upsert(pluginTeamCollaboratorMutator, pluginTeamCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving plugin team collaborator %s", pluginTeamCollaborator),
                            throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Deletes a {@link PluginAccount} from the table.
     * @param pluginAccount the account plugin row to remove
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(final PluginAccount pluginAccount) {
        Flux<? extends Statement> stmt = Mutators.delete(pluginByAccountMutator, pluginAccount);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting plugin account %s", pluginAccount), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Deletes a {@link PluginAccountCollaborator} from the table.
     * @param pluginCollaborator the collaborator to remove from the table
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(final PluginAccountCollaborator pluginCollaborator) {
        Flux<? extends Statement> stmt = Mutators.delete(accountByPluginMutator, pluginCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting plugin collaborator %s", pluginCollaborator.toString()),
                            throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Deletes a {@link PluginByTeam} from the table.
     * @param pluginByTeam the team plugin row to remove
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(final PluginByTeam pluginByTeam) {
        Flux<? extends Statement> stmt = Mutators.delete(pluginByTeamMutator, pluginByTeam);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting pluginByTeam %s", pluginByTeam), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Deletes a {@link PluginTeamCollaborator} from the table.
     * @param pluginCollaborator the team collaborator to remove from the table
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(final PluginTeamCollaborator pluginCollaborator) {
        Flux<? extends Statement> stmt = Mutators.delete(pluginTeamCollaboratorMutator, pluginCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting plugin team collaborator %s", pluginCollaborator),
                            throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetches list of accounts who have direct access to a plugin
     * @param pluginId plugin id
     * @return list of accounts
     */
    public Flux<PluginAccountCollaborator> fetchAccounts(final UUID pluginId) {
        return ResultSets.query(session, accountByPluginMaterializer.fetchByPlugin(pluginId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToAccount)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching accounts for pluginId %s", pluginId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetches a single account with direct access to a plugin
     * @param pluginId the plugin id to fetch
     * @param accountId the account if to fetch
     * @return a {@link Mono} of {@link PluginAccountCollaborator}
     */
    public Mono<PluginAccountCollaborator> fetchAccount(final UUID pluginId, final UUID accountId) {
        return ResultSets.query(session, accountByPluginMaterializer.fetchByPluginAccount(pluginId, accountId))
                .flatMapIterable(row-> row)
                .map(this::mapRowToAccount)
                .singleOrEmpty();
    }

    private PluginAccountCollaborator mapRowToAccount(Row row) {
        return new PluginAccountCollaborator()
                .setAccountId(row.getUUID("account_id"))
                .setPluginId(row.getUUID("plugin_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }

     /**
     * Fetch plugin permissions by account
     *
     * @param accountId the account id to search the plugin for
     * @return a {@link Flux} of {@link PluginAccount}
     */
     @Trace(async = true)
    public Flux<PluginAccount> fetchPluginsByAccount(final UUID accountId) {
        return ResultSets.query(session, pluginByAccountMaterializer.fetchByAccount(accountId))
                .flatMapIterable(row->row)
                .map(this::mapRowToPluginAccount)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching permissions for accountId %s", accountId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    private PluginAccount mapRowToPluginAccount(Row row) {
        return new PluginAccount()
                .setPluginId(row.getUUID("plugin_id"))
                .setAccountId(row.getUUID("account_id"));
    }

    /**
     * Fetches list of teams who have direct access to a plugin
     * @param pluginId plugin id
     * @return list of teams
     */
    public Flux<PluginTeamCollaborator> fetchTeams(final UUID pluginId) {
        return ResultSets.query(session, pluginTeamCollaboratorMaterializer.fetchByPlugin(pluginId))
                .flatMapIterable(row -> row)
                .map(pluginTeamCollaboratorMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching teams for pluginId %s", pluginId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetches a team collaborator details
     * @param pluginId plugin id
     * @param teamId team id
     */
    public Mono<PluginTeamCollaborator> fetchTeamCollaborator(final UUID pluginId, final UUID teamId) {
        return ResultSets.query(session, pluginTeamCollaboratorMaterializer.fetchByPluginTeam(pluginId, teamId))
                .flatMapIterable(row -> row)
                .map(pluginTeamCollaboratorMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching teams for pluginId %s", pluginId), throwable);
                    throw Exceptions.propagate(throwable);
                }).singleOrEmpty();
    }

    /**
     * Fetch plugin ids which are visible for a team
     *
     * @param teamId the team id to search plugins for
     * @return a {@link Flux} of {@link UUID}
     */
    public Flux<UUID> fetchPluginsByTeam(final UUID teamId) {
        return ResultSets.query(session, pluginByTeamMaterializer.fetchByTeam(teamId))
                .flatMapIterable(row -> row)
                .map(pluginByTeamMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching plugins for teamId %s", teamId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

}
