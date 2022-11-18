package com.smartsparrow.courseware.data;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CoursewareThemeGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareThemeGateway.class);

    private final Session session;
    private final ThemeByCoursewareElementMaterializer themeByCoursewareElementMaterializer;
    private final ThemeByCoursewareElementMutator themeByCoursewareElementMutator;
    private final ElementByThemeMaterializer elementByThemeMaterializer;
    private final ElementByThemeMutator elementByThemeMutator;

    @Inject
    public CoursewareThemeGateway(final Session session,
                                  final ThemeByCoursewareElementMaterializer themeByCoursewareElementMaterializer,
                                  final ThemeByCoursewareElementMutator themeByCoursewareElementMutator,
                                  final ElementByThemeMaterializer elementByThemeMaterializer,
                                  final ElementByThemeMutator elementByThemeMutator) {
        this.session = session;
        this.themeByCoursewareElementMaterializer = themeByCoursewareElementMaterializer;
        this.themeByCoursewareElementMutator = themeByCoursewareElementMutator;
        this.elementByThemeMaterializer = elementByThemeMaterializer;
        this.elementByThemeMutator = elementByThemeMutator;
    }

    /**
     * Persist theme by a courseware element
     *
     * @param themeCoursewareElement theme by courseware element object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final ThemeCoursewareElement themeCoursewareElement) {
        Flux<? extends Statement> iter = Flux.just(themeByCoursewareElementMutator.upsert(themeCoursewareElement),
                                                         elementByThemeMutator.upsert(themeCoursewareElement));

        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving theme by a courseware element",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", themeCoursewareElement.getThemeId());
                                                             put("elementId", themeCoursewareElement.getElementId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch themes by courseware element
     *
     * @param elementId the courseware element id
     * @return flux of theme ids
     */
    @Trace(async = true)
    public Mono<ThemeCoursewareElement> fetchThemeByElementId(final UUID elementId) {
        return ResultSets.query(session,
                                themeByCoursewareElementMaterializer.fetchByElementId(elementId))
                .flatMapIterable(row -> row)
                .map(themeByCoursewareElementMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete theme by a courseware element
     *
     * @param themeCoursewareElement theme courseware element object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(final ThemeCoursewareElement themeCoursewareElement) {
        Flux<? extends Statement> iter = Mutators.delete(themeByCoursewareElementMutator,
                                                         themeCoursewareElement);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while deleting theme by a courseware element",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("elementId", themeCoursewareElement.getElementId());
                                                             put("elementType", themeCoursewareElement.getElementType());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch elements by theme id
     *
     * @param themeId the theme id
     * @return flux of theme ids
     */
    @Trace(async = true)
    public Flux<ThemeCoursewareElement> fetchElementByThemeId(final UUID themeId) {
        return ResultSets.query(session, elementByThemeMaterializer.fetchByThemeId(themeId))
                .flatMapIterable(row -> row)
                .map(elementByThemeMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.reactiveError(String.format("error while fetching elements by theme %s", themeId));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete element by a theme
     *
     * @param themeCoursewareElement theme courseware element object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> deleteElementThemeAssociation(final ThemeCoursewareElement themeCoursewareElement) {
        Flux<? extends Statement> iter = Flux.just(themeByCoursewareElementMutator.delete(themeCoursewareElement),
                                                   elementByThemeMutator.delete(themeCoursewareElement));
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while deleting courseware element and theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", themeCoursewareElement.getThemeId());
                                                             put("elementId", themeCoursewareElement.getElementId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete elements by theme
     *
     * @param themeCoursewareElement theme courseware element object
     * @return flux of void
     */
    @Trace(async = true)
    public Mono<Void> deleteElementByTheme(final ThemeCoursewareElement themeCoursewareElement) {
        Flux<? extends Statement> stmt = Flux.just(
                elementByThemeMutator.deleteElementsByTheme(themeCoursewareElement.getThemeId(),
                                                            themeCoursewareElement.getElementId()));

        return Mutators.execute(session, stmt)
                .doOnEach(log.reactiveErrorThrowable("error deleting elements by theme",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("themeId", themeCoursewareElement.getThemeId());
                                                             put("elementId", themeCoursewareElement.getElementId());
                                                         }
                                                     }))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
