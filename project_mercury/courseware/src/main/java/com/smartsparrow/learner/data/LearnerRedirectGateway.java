package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.learner.redirect.LearnerRedirect;
import com.smartsparrow.learner.redirect.LearnerRedirectType;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerRedirectGateway {

    private final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerRedirectGateway.class);

    private final Session session;

    //
    private final LearnerRedirectByIdMutator learnerRedirectByIdMutator;
    private final LearnerRedirectByKeyMutator learnerRedirectByKeyMutator;
    private final LearnerRedirectByIdHistoryMutator learnerRedirectByIdHistoryMutator;

    //
    private final LearnerRedirectByKeyMaterializer learnerRedirectByKeyMaterializer;
    private final LearnerRedirectByIdMaterializer learnerRedirectByIdMaterializer;

    @Inject

    public LearnerRedirectGateway(final Session session,
                                  final LearnerRedirectByIdMutator learnerRedirectByIdMutator,
                                  final LearnerRedirectByKeyMutator learnerRedirectByKeyMutator,
                                  final LearnerRedirectByIdHistoryMutator learnerRedirectByIdHistoryMutator,
                                  final LearnerRedirectByKeyMaterializer learnerRedirectByKeyMaterializer,
                                  final LearnerRedirectByIdMaterializer learnerRedirectByIdMaterializer) {
        this.session = session;
        this.learnerRedirectByIdMutator = learnerRedirectByIdMutator;
        this.learnerRedirectByKeyMutator = learnerRedirectByKeyMutator;
        this.learnerRedirectByIdHistoryMutator = learnerRedirectByIdHistoryMutator;
        this.learnerRedirectByKeyMaterializer = learnerRedirectByKeyMaterializer;
        this.learnerRedirectByIdMaterializer = learnerRedirectByIdMaterializer;
    }

    /**
     * Persist a Learner Redirect.
     *
     * @param learnerRedirect redirect object to be persisted
     * @return Flux of {@link Void}
     */
    public Flux<Void> persist(final LearnerRedirect learnerRedirect) {
        return Mutators.execute(session, Flux.just(learnerRedirectByIdMutator.upsert(learnerRedirect),
                                                   learnerRedirectByKeyMutator.upsert(learnerRedirect),
                                                   learnerRedirectByIdHistoryMutator.upsert(learnerRedirect)))
                .doOnEach(log.reactiveErrorThrowable("error persisting the learner redirect"));
    }

    public Flux<Void> delete(final LearnerRedirect learnerRedirect) {
        return Mutators.execute(session, Flux.just(learnerRedirectByKeyMutator.delete(learnerRedirect)))
                .doOnEach(log.reactiveErrorThrowable("error deleting the learner redirect by key"));
    }

    /**
     * Fetches a learner redirect by type and key.
     *
     * @param type the type portion
     * @param key the key portion
     * @return a {@link Mono} that emits the LearnerRedirect or an empty Mono if not found
     */
    public Mono<LearnerRedirect> fetch(final LearnerRedirectType type, final String key) {
        return ResultSets.query(session, learnerRedirectByKeyMaterializer.fetch(type, key))
                .flatMapIterable(row -> row)
                .map(this::fromRow)
                .doOnEach(log.reactiveErrorThrowable("error fetching the learner redirect"))
                .singleOrEmpty();
    }

    /**
     * Fetches a learner redirect by id.
     *
     * @param redirectId the id of the learner redirect to fetch
     * @return a {@link Mono} that emits the LearnerRedirect or an empty Mono if not found
     */
    public Mono<LearnerRedirect> fetchById(final UUID redirectId) {
        return ResultSets.query(session, learnerRedirectByIdMaterializer.fetch(redirectId))
                .flatMapIterable(row -> row)
                .map(this::fromRow)
                .doOnEach(log.reactiveErrorThrowable("error fetching the learner redirect"))
                .singleOrEmpty();
    }

    /*
     * Map a Row to LearnerRedirect
     */
    LearnerRedirect fromRow(final Row row) {
        LearnerRedirect obj = new LearnerRedirect();
        obj.setId(row.getUUID("id"));
        obj.setVersion(row.getUUID("version"));
        obj.setType(Enums.of(LearnerRedirectType.class, row.getString("redirect_type")));
        obj.setKey(row.getString("redirect_key"));
        obj.setDestinationPath(row.getString("destination_path"));
        return obj;
    }
}
