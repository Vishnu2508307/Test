package com.smartsparrow.workspace.data;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.data.permission.workspace.ThemePermissionByTeamMaterializer;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ThemeAccessGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ThemeAccessGateway.class);

    private final Session session;

    private final ThemeByAccountMaterializer themeByAccountMaterializer;
    private final ThemeByAccountMutator themeByAccountMutator;
    private final AccountByThemeMaterializer accountByThemeMaterializer;
    private final AccountByThemeMutator accountByThemeMutator;
    private final TeamByThemeMaterializer teamByThemeMaterializer;
    private final TeamByThemeMutator teamByThemeMutator;
    private final ThemeByTeamMaterializer themeByTeamMaterializer;
    private final ThemeByTeamMutator themeByTeamMutator;
    private final ThemePermissionByTeamMaterializer themePermissionByTeamMaterializer;


    @Inject
    public ThemeAccessGateway(Session session,
                              ThemeByAccountMaterializer themeByAccountMaterializer,
                              ThemeByAccountMutator themeByAccountMutator,
                              AccountByThemeMaterializer accountByThemeMaterializer,
                              AccountByThemeMutator accountByThemeMutator,
                              TeamByThemeMaterializer teamByThemeMaterializer,
                              TeamByThemeMutator teamByThemeMutator,
                              ThemeByTeamMaterializer themeByTeamMaterializer,
                              ThemeByTeamMutator themeByTeamMutator,
                              ThemePermissionByTeamMaterializer themePermissionByTeamMaterializer) {
        this.session = session;
        this.themeByAccountMaterializer = themeByAccountMaterializer;
        this.themeByAccountMutator = themeByAccountMutator;
        this.accountByThemeMaterializer = accountByThemeMaterializer;
        this.accountByThemeMutator = accountByThemeMutator;
        this.teamByThemeMaterializer = teamByThemeMaterializer;
        this.teamByThemeMutator = teamByThemeMutator;
        this.themeByTeamMaterializer = themeByTeamMaterializer;
        this.themeByTeamMutator = themeByTeamMutator;
        this.themePermissionByTeamMaterializer = themePermissionByTeamMaterializer;
    }

    /**
     * Persist theme by an account
     *
     * @param themeByAccount, theme by account object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final ThemeByAccount themeByAccount) {
        Flux<? extends Statement> iter = Mutators.upsert(themeByAccountMutator,
                                                         themeByAccount);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving theme by an account",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", themeByAccount.getThemeId());
                                                             put("accountId", themeByAccount.getAccountId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist account by theme
     *
     * @param accountByTheme, the account by theme object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final AccountByTheme accountByTheme) {
        Flux<? extends Statement> iter = Mutators.upsert(accountByThemeMutator,
                                                         accountByTheme);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving account by theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", accountByTheme.getThemeId());
                                                             put("accountId", accountByTheme.getAccountId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist team by theme
     *
     * @param teamByTheme, the team by theme object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final TeamByTheme teamByTheme) {
        Flux<? extends Statement> iter = Mutators.upsert(teamByThemeMutator,
                                                         teamByTheme);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving team by theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", teamByTheme.getThemeId());
                                                             put("teamId", teamByTheme.getTeamId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist theme by team
     *
     * @param themeByTeam, the theme by team object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final ThemeByTeam themeByTeam) {
        Flux<? extends Statement> iter = Mutators.upsert(themeByTeamMutator,
                                                         themeByTeam);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving theme by team",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", themeByTeam.getThemeId());
                                                             put("teamId", themeByTeam.getTeamId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch teams by theme
     *
     * @param themeId, the theme id
     * @return flux of tea by theme object
     */
    @Trace(async = true)
    public Flux<TeamByTheme> findTeamsByTheme(final UUID themeId) {
        return ResultSets.query(session,
                                teamByThemeMaterializer.fetchByTheme(themeId))
                .flatMapIterable(row -> row)
                .map(teamByThemeMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch teams by theme and team
     *
     * @param themeId, the theme id
     * @param teamId, the team id
     * @return flux of team by theme object
     */
    @Trace(async = true)
    public Flux<TeamByTheme> findTeamsByThemeAndTeam(final UUID themeId, final UUID teamId) {
        return ResultSets.query(session,
                                teamByThemeMaterializer.fetchByThemeTeam(themeId, teamId))
                .flatMapIterable(row -> row)
                .map(teamByThemeMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch theme permission level by team
     *
     * @param teamId, the team id
     * @param themeId, the theme id
     * @return mono of permission level object
     */
    @Trace(async = true)
    public Mono<PermissionLevel> fetchPermissionLevel(UUID teamId, UUID themeId) {
        return ResultSets.query(session, themePermissionByTeamMaterializer.fetchThemePermissionByTeam(teamId, themeId))
                .flatMapIterable(row -> row)
                .map(themePermissionByTeamMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch theme for an account
     *
     * @param accountId, the account id
     * @return return flux of theme ids
     */
    @Trace(async = true)
    public Flux<UUID> fetchThemeForAccount(final UUID accountId) {
        return ResultSets.query(session,
                                themeByAccountMaterializer.fetchThemeForAccount(accountId))
                .flatMapIterable(row -> row)
                .map(themeByAccountMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch themes by team
     *
     * @param teamId, the team id
     * @return flux of theme ids
     */
    @Trace(async = true)
    public Flux<UUID> fetchThemeByTeam(final UUID teamId) {
        return ResultSets.query(session,
                                themeByTeamMaterializer.fetchByTeam(teamId))
                .flatMapIterable(row -> row)
                .map(themeByTeamMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch accounts by theme
     *
     * @param themeId, the theme id
     * @return flux of account by theme object
     */
    @Trace(async = true)
    public Flux<AccountByTheme> fetchAccountsForTheme(final UUID themeId) {
        return ResultSets.query(session,
                                accountByThemeMaterializer.fetchAccountsForTheme(themeId))
                .flatMapIterable(row -> row)
                .map(accountByThemeMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete the theme by team mapping
     *
     * @param themeByTeam theme by team object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(ThemeByTeam themeByTeam) {
        return Mutators.execute(session, Flux.just(
                themeByTeamMutator.delete(themeByTeam)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete the team by theme mapping
     *
     * @param teamByTheme team by theme object
     * @return return flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(TeamByTheme teamByTheme) {
        return Mutators.execute(session, Flux.just(
                teamByThemeMutator.delete(teamByTheme)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete theme by account mapping
     *
     * @param themeByAccount theme by account object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(ThemeByAccount themeByAccount) {
        return Mutators.execute(session, Flux.just(
                themeByAccountMutator.delete(themeByAccount)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete the account by theme mapping
     *
     * @param accountByTheme account by theme object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(AccountByTheme accountByTheme) {
        return Mutators.execute(session, Flux.just(
                accountByThemeMutator.delete(accountByTheme)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

}
