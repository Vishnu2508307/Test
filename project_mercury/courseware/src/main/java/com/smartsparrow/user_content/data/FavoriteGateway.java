package com.smartsparrow.user_content.data;

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
public class FavoriteGateway {
    private final Session session;
    private static final Logger log = LoggerFactory.getLogger(FavoriteGateway.class);

    private final FavoriteMutator favoriteMutator;
    private final FavoriteMaterializer favoriteMaterializer;

    @Inject
    public FavoriteGateway(final Session session,
                           final FavoriteMutator favoriteMutator,
                           final FavoriteMaterializer favoriteMaterializer) {
        this.session = session;
        this.favoriteMutator = favoriteMutator;
        this.favoriteMaterializer = favoriteMaterializer;
    }

    /**
     * Persist favorite course for the account id
     * @param favorite course
     * @return mono of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final Favorite favorite) {

        return Mutators.execute(session, Flux.just(favoriteMutator.upsert(favorite)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error(String.format("Error: persist, with favoriteCourse %s", favorite), e);
                    throw Exceptions.propagate(e);
                });
    }

    /**
     * removes favorite from database
     * @param favorite user favorite object
     * @return mono of void
     */
    public Mono<Void> removeFavorite(final Favorite favorite) {

        return Mutators.execute(session, Flux.just(favoriteMutator.delete(favorite)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error(String.format("Error: removing favorite, with accountId %s id %s", favorite.getAccountId(), favorite.getId()), e);
                    throw Exceptions.propagate(e);
                }).singleOrEmpty()                ;
    }

    /**
     * fetch all favorite courses by account id
     * @param accountId account identifier
     * @return flux of FavoriteCourse
     */
    public Flux<Favorite> getByAccountId(final UUID accountId) {

        return ResultSets.query(session, favoriteMaterializer.fetchAllByAccount(accountId))
                .flatMapIterable(row -> row)
                .map(favoriteMaterializer::fromRow);
    }
}
