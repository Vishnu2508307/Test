package com.smartsparrow.iam.data.permission.workspace;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ThemePermissionGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ThemePermissionGateway.class);

    private final Session session;

    private final ThemePermissionByAccountMutator themePermissionByAccountMutator;
    private final ThemePermissionByAccountMaterializer themePermissionByAccountMaterializer;
    private final ThemePermissionByTeamMutator themePermissionByTeamMutator;
    private final ThemePermissionByTeamMaterializer themePermissionByTeamMaterializer;

    @Inject
    public ThemePermissionGateway(Session session,
                                  ThemePermissionByAccountMutator themePermissionByAccountMutator,
                                  ThemePermissionByAccountMaterializer themePermissionByAccountMaterializer,
                                  ThemePermissionByTeamMutator themePermissionByTeamMutator,
                                  ThemePermissionByTeamMaterializer themePermissionByTeamMaterializer) {
        this.session = session;
        this.themePermissionByAccountMutator = themePermissionByAccountMutator;
        this.themePermissionByAccountMaterializer = themePermissionByAccountMaterializer;
        this.themePermissionByTeamMutator = themePermissionByTeamMutator;
        this.themePermissionByTeamMaterializer = themePermissionByTeamMaterializer;
    }

    /**
     * Persist theme permission for an account
     *
     * @param themePermissionByAccount, the theme permission by an account object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final ThemePermissionByAccount themePermissionByAccount) {
        Flux<? extends Statement> iter = Mutators.upsert(themePermissionByAccountMutator,
                                                         themePermissionByAccount);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", themePermissionByAccount.getThemeId());
                                                             put("accountId", themePermissionByAccount.getAccountId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist theme permission by team
     *
     * @param teamPermissionByTheme, the team permission by theme object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final ThemePermissionByTeam teamPermissionByTheme) {
        Flux<? extends Statement> iter = Mutators.upsert(themePermissionByTeamMutator,
                                                         teamPermissionByTheme);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", teamPermissionByTheme.getThemeId());
                                                             put("teamId", teamPermissionByTheme.getTeamId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch theme permission by team
     *
     * @param teamId, the team id
     * @param themeId, the theme id
     * @return mono of permission level object
     */
    @Trace(async = true)
    public Mono<PermissionLevel> fetchThemePermissionByTeam(UUID teamId, UUID themeId) {
        return ResultSets.query(session, themePermissionByTeamMaterializer.fetchThemePermissionByTeam(teamId, themeId))
                .flatMapIterable(row -> row)
                .map(themePermissionByTeamMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch theme permission by an account and theme
     *
     * @param accountId, the account id
     * @param themeId, the theme id
     * @return momo of Theme permission by account
     */
    @Trace(async = true)
    public Mono<ThemePermissionByAccount> fetchThemePermissionByAccount(UUID accountId, UUID themeId) {
        return ResultSets.query(session,
                                themePermissionByAccountMaterializer.fetchThemePermissionByAccountTheme(accountId,
                                                                                                        themeId))
                .flatMapIterable(row -> row)
                .map(themePermissionByAccountMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete a theme permission by team mapping
     *
     * @param themePermissionByTeam theme permission by team object
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(ThemePermissionByTeam themePermissionByTeam) {
        return Mutators.execute(session, Flux.just(
                themePermissionByTeamMutator.delete(themePermissionByTeam)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete a theme permission by account mapping
     *
     * @param themePermissionByAccount the theme permission by account object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(ThemePermissionByAccount themePermissionByAccount) {
        return Mutators.execute(session, Flux.just(
                themePermissionByAccountMutator.delete(themePermissionByAccount)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
