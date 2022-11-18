package com.smartsparrow.iam.wiring;

import static reactor.core.scheduler.Schedulers.elastic;

import java.io.UnsupportedEncodingException;

import com.pearson.autobahn.common.domain.Environment;
import com.pearson.autobahn.common.exception.AutobahnIdentityProviderException;
import com.pearson.autobahn.common.sdk.auth.impl.PiAutobahnIdentityProvider;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import javax.inject.Singleton;

/**
 * Autobahn libs already include ies authentication libs and PiAutobahnProvider already wraps it this is an
 * adapter for reactive token retrieval and other customizations we deem necessary.
 *
 */
@Singleton
public class IesSystemToSystemIdentityProvider extends PiAutobahnIdentityProvider {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IesSystemToSystemIdentityProvider.class);

    /**
     * {@inheritDoc}
     */
    protected IesSystemToSystemIdentityProvider() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public IesSystemToSystemIdentityProvider(final String piUsername, final String piPassword, final Environment environment) {
        super(piUsername, piPassword, environment);
    }

    /**
     * Wraps {@link PiAutobahnIdentityProvider#getPiToken()} into a reactive cal so it can be used in reactive world
     * without blocking
     *
     * @return Mono that emits pi token
     */
    public Mono<String> getPiTokeReactive() {
        return Mono
                .fromCallable(this::getPiToken)
                .publishOn(elastic())
                .single()
                .doOnError(throwable -> {
                    log.error("error while fetching pi token", throwable);
                    throw Exceptions.propagate(throwable);
                });
    }
}
