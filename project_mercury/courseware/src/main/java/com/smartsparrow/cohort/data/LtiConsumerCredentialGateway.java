package com.smartsparrow.cohort.data;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class has been deprecated and it will be deleted in the future.
 * Replaced by {@link com.smartsparrow.sso.data.ltiv11.LTIv11Gateway} which is the only
 * central LTI gateway
 */
@Deprecated
public class LtiConsumerCredentialGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LtiConsumerCredentialGateway.class);

    private final Session session;

    private final LtiConsumerCredentialDetailByCohortMutator ltiConsumerCredentialDetailByCohortMutator;
    private final LtiConsumerCredentialDetailByCohortMaterializer ltiConsumerCredentialDetailByCohortMaterializer;

    @Inject
    public LtiConsumerCredentialGateway(final Session session,
                                        final LtiConsumerCredentialDetailByCohortMutator ltiConsumerCredentialDetailByCohortMutator,
                                        final LtiConsumerCredentialDetailByCohortMaterializer ltiConsumerCredentialDetailByCohortMaterializer) {
        this.session = session;
        this.ltiConsumerCredentialDetailByCohortMutator = ltiConsumerCredentialDetailByCohortMutator;
        this.ltiConsumerCredentialDetailByCohortMaterializer = ltiConsumerCredentialDetailByCohortMaterializer;
    }

    /**
     * Save LTI consumer credential detail
     *
     * @param ltiConsumerCredentialDetail the Lti consumer credential detail object to save
     * @return a flux of void
     */
    public Flux<Void> persist(LtiConsumerCredentialDetail ltiConsumerCredentialDetail) {
        return Mutators.execute(session, Mutators.upsert(ltiConsumerCredentialDetailByCohortMutator, ltiConsumerCredentialDetail))
                .doOnEach(log.reactiveErrorThrowable("error while saving Lti consumer credential detail",
                        throwable -> new HashMap<String, Object>() {
                            {
                                put("cohortId", ltiConsumerCredentialDetail.getCohortId());
                            }
                        }));
    }

    /**
     * Find all Lti consumer credential details on a cohort
     *
     * @param cohortId the cohort id
     * @return a flux of LtiConsumerCredentialDetail
     */
    public Flux<LtiConsumerCredentialDetail> findLtiConsumerCredentialByCohort(UUID cohortId) {
        return ResultSets.query(session, ltiConsumerCredentialDetailByCohortMaterializer.fetchByCohortId(cohortId))
                .flatMapIterable(row -> row)
                .map(ltiConsumerCredentialDetailByCohortMaterializer::fromRow)
                .doOnEach(log.reactiveErrorThrowable("error while fetching Lti consumer credential details",
                        throwable -> new HashMap<String, Object>() {
                            {
                                put("cohortId", cohortId);
                            }
                        }));
    }

    /**
     * Find a Lti consumer credential detail by cohort it and consumer key
     *
     * @param cohortId the cohort id
     * @param key      ths Lti consumer key
     * @return a mono of LtiConsumerCredentialDetail
     */
    public Mono<LtiConsumerCredentialDetail> findLtiConsumerCredentialByCohortAndKey(UUID cohortId, String key) {
        return ResultSets.query(session, ltiConsumerCredentialDetailByCohortMaterializer.fetchByCohortIdAndKey(cohortId, key))
                .flatMapIterable(row -> row)
                .map(ltiConsumerCredentialDetailByCohortMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(log.reactiveErrorThrowable("error while fetching Lti consumer credential detail",
                        throwable -> new HashMap<String, Object>() {
                            {
                                put("cohortId", cohortId);
                            }

                            {
                                put("consumerKey", key);
                            }
                        }));
    }
}
