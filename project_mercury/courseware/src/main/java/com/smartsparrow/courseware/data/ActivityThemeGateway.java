package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ActivityThemeGateway {

    private final Session session;
    private static final Logger log = LoggerFactory.getLogger(ActivityThemeGateway.class);

    private final ActivityThemeMutator activityThemeMutator;
    private final ActivityThemeMaterializer activityThemeMaterializer;

    @Inject
    ActivityThemeGateway(final Session session,
                         final ActivityThemeMutator activityThemeMutator,
                         final ActivityThemeMaterializer activityThemeMaterializer) {
        this.session = session;
        this.activityThemeMutator = activityThemeMutator;
        this.activityThemeMaterializer = activityThemeMaterializer;
    }

    /**
     * Persist the activityTheme to activityTheme table and activityThemeByActivity tables
     *
     * @param activityTheme
     */
    @Trace(async = true)
    public Mono<Void> persist(final ActivityTheme activityTheme) {
        return Mutators.execute(session, Flux.just(activityThemeMutator.upsert(activityTheme)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error(String.format("Error: persist, with activityTheme %s", activityTheme), e);
                    throw Exceptions.propagate(e);
                })
                .singleOrEmpty();
    }

    /**
     * Find the latest activity theme for an activity
     *
     * @param activityId the id of the activity to find the latest theme for
     * @return a mono of activity theme
     */
    @Trace(async = true)
    public Mono<ActivityTheme> findLatestByActivityId(final UUID activityId) {
        return ResultSets.query(session, activityThemeMaterializer.fetchLatestByActivity(activityId))
                .flatMapIterable(row -> row)
                .map(activityThemeMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("Error fetching latest activityTheme %s", activityId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

}
