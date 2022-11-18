package com.smartsparrow.asset.data;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class AssetSignatureGateway {

    private final MercuryLogger log = MercuryLoggerFactory.getLogger(AssetSignatureGateway.class);

    private final Session session;

    private final SignatureStrategyByDomainPathMutator signatureStrategyByDomainPathMutator;
    private final SignatureStrategyByDomainPathMaterializer signatureStrategyByDomainPathMaterializer;

    @Inject
    public AssetSignatureGateway(final Session session,
                                 final SignatureStrategyByDomainPathMutator signatureStrategyByDomainPathMutator,
                                 final SignatureStrategyByDomainPathMaterializer signatureStrategyByDomainPathMaterializer) {
        this.session = session;
        this.signatureStrategyByDomainPathMutator = signatureStrategyByDomainPathMutator;
        this.signatureStrategyByDomainPathMaterializer = signatureStrategyByDomainPathMaterializer;
    }

    /**
     * Configure a particular signature strategy for a domain path
     *
     * @param assetSignature the asset signature strategy to configure
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final AssetSignature assetSignature) {
        return Mutators.execute(session, Flux.just(signatureStrategyByDomainPathMutator.upsert(assetSignature)))
                .doOnEach(log.reactiveErrorThrowable("error configuring signature strategy",
                        throwable -> new HashMap<String, Object>() {
                            {
                                put("strategyId", assetSignature.getId());
                            }
                        }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete an asset signature configuration
     *
     * @param assetSignature the signature configurations to delete
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(final AssetSignature assetSignature) {
        return Mutators.execute(session, Flux.just(signatureStrategyByDomainPathMutator.delete(assetSignature)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveErrorThrowable("error deleting signature strategy configurations",
                        throwable -> new HashMap<String, Object>() {
                            {
                                put("strategyId", assetSignature.getId());
                            }
                        }));
    }

    /**
     * Find the configured asset signature strategy
     *
     * @param host the host to find the signature strategy for
     * @param path the host path to find the signature strategy for
     * @return a mono containing the signature strategy configuration
     */
    @Trace(async = true)
    public Mono<AssetSignature> findAssetSignature(final String host, final String path) {
        return ResultSets.query(session, signatureStrategyByDomainPathMaterializer.findByHostPath(host, path))
                .flatMapIterable(row -> row)
                .map(signatureStrategyByDomainPathMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
