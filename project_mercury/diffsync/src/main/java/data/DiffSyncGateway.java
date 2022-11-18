package data;

import com.datastax.driver.core.Session;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

@Singleton
public class DiffSyncGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DiffSyncGateway.class);

    private final Session session;
    private final PatchSummaryMutator patchSummaryMutator;

    @Inject
    public DiffSyncGateway(final Session session, final PatchSummaryMutator patchSummaryMutator) {
        this.session = session;
        this.patchSummaryMutator = patchSummaryMutator;
    }

    /**
     * To save the patch summary
     *
     * @param patchSummary patch summary.
     */
    @Trace(async = true)
    public Flux<Void> savePatch(final PatchSummary patchSummary) {
        return Mutators.execute(session, Flux.just(patchSummaryMutator.upsert(patchSummary)))
                .doOnError(throwable -> {
                    log.error(String.format("error while saving the patch information %s",
                                            patchSummary), throwable);
                    throw Exceptions.propagate(throwable);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

}
