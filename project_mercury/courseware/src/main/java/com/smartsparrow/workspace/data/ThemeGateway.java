package com.smartsparrow.workspace.data;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ThemeGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ThemeGateway.class);

    private final Session session;

    private final ThemeMutator themeMutator;
    private final ThemeMaterializer themeMaterializer;
    private final ThemeVariantMutator themeVariantMutator;
    private final ThemeVariantMaterializer themeVariantMaterializer;
    private final ThemeVariantByStateMutator themeVariantByStateMutator;
    private final ThemeVariantByStateMaterializer themeVariantByStateMaterializer;
    private final IconLibraryByThemeMutator iconLibraryByThemeMutator;
    private final IconLibraryByThemeMaterializer iconLibraryByThemeMaterializer;
    private final ActivityThemeIconLibraryMutator activityThemeIconLibraryMutator;
    private final ActivityThemeIconLibraryMaterializer activityThemeIconLibraryMaterializer;

    @Inject
    public ThemeGateway(Session session,
                        final ThemeMutator themeMutator,
                        final ThemeMaterializer themeMaterializer,
                        final ThemeVariantMutator themeVariantMutator,
                        final ThemeVariantMaterializer themeVariantMaterializer,
                        final ThemeVariantByStateMutator themeVariantByStateMutator,
                        final ThemeVariantByStateMaterializer themeVariantByStateMaterializer,
                        final IconLibraryByThemeMutator iconLibraryByThemeMutator,
                        final IconLibraryByThemeMaterializer iconLibraryByThemeMaterializer,
                        final ActivityThemeIconLibraryMutator activityThemeIconLibraryMutator,
                        final ActivityThemeIconLibraryMaterializer activityThemeIconLibraryMaterializer) {
        this.session = session;
        this.themeMutator = themeMutator;
        this.themeMaterializer = themeMaterializer;
        this.themeVariantMutator = themeVariantMutator;
        this.themeVariantMaterializer = themeVariantMaterializer;
        this.themeVariantByStateMutator = themeVariantByStateMutator;
        this.themeVariantByStateMaterializer = themeVariantByStateMaterializer;
        this.iconLibraryByThemeMutator = iconLibraryByThemeMutator;
        this.iconLibraryByThemeMaterializer = iconLibraryByThemeMaterializer;
        this.activityThemeIconLibraryMutator = activityThemeIconLibraryMutator;
        this.activityThemeIconLibraryMaterializer = activityThemeIconLibraryMaterializer;

    }

    /**
     * Persist theme information
     *
     * @param theme, the theme object
     */
    @Trace(async = true)
    public Mono<Void> persistTheme(Theme theme) {
        Flux<? extends Statement> iter = Mutators.upsert(themeMutator,
                                                         theme);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", theme.getId());
                                                             put("name", theme.getName());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch theme information by theme id
     *
     * @param themeId, the theme id
     * @return mono of theme object
     */
    @Trace(async = true)
    public Mono<Theme> fetchThemeById(UUID themeId) {
        return ResultSets.query(session, themeMaterializer.findThemeById(themeId))
                .flatMapIterable(row -> row)
                .map(themeMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching for theme id %s", themeId));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete the theme from workspace.theme
     *
     * @param theme the theme object
     */
    @Trace(async = true)
    public Flux<Void> delete(Theme theme) {
        Flux<? extends Statement> stmt = Flux.just(
                themeMutator.delete(theme));
        return Mutators.execute(session, stmt)
                .doOnEach(log.reactiveErrorThrowable("error deleting theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", theme.getId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist theme variant information
     *
     * @param themeVariant, the theme variant object
     */
    @Trace(async = true)
    public Mono<Void> persistThemeVariant(ThemeVariant themeVariant) {
        Flux<? extends Statement> iter = Mutators.upsert(themeVariantMutator,
                                                         themeVariant);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving theme variant",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", themeVariant.getThemeId());
                                                             put("variantName", themeVariant.getVariantName());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch theme variant information by theme id
     *
     * @param themeId, the theme id
     * @return mono of theme variant object
     */
    @Trace(async = true)
    public Flux<ThemeVariant> fetchVariantsByThemeId(UUID themeId) {
        return ResultSets.query(session, themeVariantMaterializer.findVariantByThemeId(themeId))
                .flatMapIterable(row -> row)
                .map(themeVariantMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching theme variant %s", themeId));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch theme variant information by theme id and variant name
     *
     * @param themeId, the theme id
     * @param themeVariantId, the theme variant name
     * @return mono of theme variant object
     */
    @Trace(async = true)
    public Mono<ThemeVariant> fetchVariantByThemeIdAndVariantId(UUID themeId, UUID themeVariantId) {
        return ResultSets.query(session, themeVariantMaterializer.findVariantByThemeIdAndVariantId(themeId, themeVariantId))
                .flatMapIterable(row -> row)
                .map(themeVariantMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching theme variant %s", themeId));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist theme variant by state
     *
     * @param themeVariant, the theme variant object
     */
    @Trace(async = true)
    public Mono<Void> persistThemeVariantByState(ThemeVariant themeVariant) {
        return Mutators.execute(session, Flux.just(themeVariantByStateMutator.upsert(themeVariant)))
                .doOnEach(log.reactiveErrorThrowable("error while saving theme variant by state",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", themeVariant.getThemeId());
                                                             put("variantName", themeVariant.getVariantName());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch default theme variant information by theme id, state and variant id
     *
     * @param themeId, the theme id
     *  @param state, the theme state
     * @param variantId the theme variant id
     * @return mono of theme variant object
     */
    @Trace(async = true)
    public Mono<ThemeVariant> fetchVariantByStateAndVariantId(UUID themeId, ThemeState state, UUID variantId) {
        return ResultSets.query(session, themeVariantByStateMaterializer.findVariantByStateVariantId(themeId, state, variantId))
                .flatMapIterable(row -> row)
                .map(themeVariantByStateMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching theme variant by state %s", themeId));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch default theme variant information by theme id and state
     *
     * @param themeId, the theme id
     *  @param state, the theme state
     * @return mono of theme variant object
     */
    @Trace(async = true)
    public Mono<ThemeVariant> findThemeVariantByState(UUID themeId, ThemeState state) {
        return ResultSets.query(session, themeVariantByStateMaterializer.findThemeVariantByState(themeId, state))
                .flatMapIterable(row -> row)
                .map(themeVariantByStateMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching theme variant by state %s", themeId));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete theme variant info by theme id and variant name
     *
     * @param themeVariant the theme variant object
     */
    @Trace(async = true)
    public Flux<Void> deleteThemeVariant(ThemeVariant themeVariant) {
        Flux<? extends Statement> stmt = Flux.just(
                themeVariantMutator.deleteByThemeIdAndVariant(themeVariant.getThemeId(),
                                                              themeVariant.getVariantId()),
                themeVariantByStateMutator.deleteByThemeIdAndVariant(themeVariant.getThemeId(),
                                                                     ThemeState.DEFAULT,
                                                                     themeVariant.getVariantId()));

        return Mutators.execute(session, stmt)
                .doOnEach(log.reactiveErrorThrowable("error deleting theme variant",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", themeVariant.getThemeId());
                                                             put("variantName", themeVariant.getVariantName());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete theme variants by theme id from both tables, theme variant and theme variant by state
     *
     *
     * @param themeVariant the theme variant object
     *
     */
    @Trace(async = true)
    public Flux<Void> deleteThemeVariantAndByState(ThemeVariant themeVariant) {

        return Mutators.execute(session, Flux.just(
                themeVariantMutator.deleteByThemeId(themeVariant),
                themeVariantByStateMutator.deleteByThemeId(themeVariant)))
                .doOnEach(log.reactiveErrorThrowable("error deleting theme variant",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", themeVariant.getThemeId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist theme icon library information
     *
     * @param iconLibraryByTheme, the icon library by theme object
     */
    @Trace(async = true)
    public Mono<Void> persistIconLibraryByTheme(IconLibraryByTheme iconLibraryByTheme) {
        Flux<? extends Statement> iter = Mutators.upsert(iconLibraryByThemeMutator,
                                                         iconLibraryByTheme);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving icon library by theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", iconLibraryByTheme.getThemeId());
                                                             put("iconLibrary", iconLibraryByTheme.getIconLibrary());
                                                             put("status", iconLibraryByTheme.getStatus());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch icon libraries information by theme id
     *
     * @param themeId, the theme id
     * @return mono of icon library by theme object
     */
    @Trace(async = true)
    public Flux<IconLibrary> fetchIconLibrariesByThemeId(UUID themeId) {
        return ResultSets.query(session, iconLibraryByThemeMaterializer.fetchById(themeId))
                .flatMapIterable(row -> row)
                .map(iconLibraryByThemeMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching icon libraries by theme %s", themeId));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist activity theme icon library information
     *
     * @param activityThemeIconLibrary, activity theme icon library object
     */
    @Trace(async = true)
    public Mono<Void> persistActivityThemeIconLibrary(ActivityThemeIconLibrary activityThemeIconLibrary) {
        Flux<? extends Statement> iter = Mutators.upsert(activityThemeIconLibraryMutator,
                                                         activityThemeIconLibrary);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving icon library by activity",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("activityId", activityThemeIconLibrary.getActivityId());
                                                             put("iconLibrary", activityThemeIconLibrary.getIconLibrary());
                                                             put("status", activityThemeIconLibrary.getStatus());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch activity theme icon libraries by activity id
     *
     * @param activityId, the activity id
     * @return flux of activity theme icon library object
     */
    @Trace(async = true)
    public Flux<IconLibrary> fetchActivityThemeIconLibraries(UUID activityId) {
        return ResultSets.query(session, activityThemeIconLibraryMaterializer.fetchById(activityId))
                .flatMapIterable(row -> row)
                .map(activityThemeIconLibraryMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching icon libraries by activity %s",
                                                    activityId));
                    throw Exceptions.propagate(throwable);
                });
    }

}
