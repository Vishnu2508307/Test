package com.smartsparrow.math.data;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.math.event.MathAssetEventMessage;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class MathAssetGateway {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MathAssetGateway.class);

    private final Session session;
    private final AssetByHashMaterializer assetByHashMaterializer;
    private final AssetByHashMutator assetByHashMutator;
    private final AssetIdByUrnMaterializer assetIdByUrnMaterializer;
    private final AssetIdByUrnMutator assetIdByUrnMutator;
    private final AssetSummaryMaterializer assetSummaryMaterializer;
    private final AssetSummaryMutator assetSummaryMutator;
    private final UsageByAssetUrnMaterializer usageByAssetUrnMaterializer;
    private final UsageByAssetUrnMutator usageByAssetUrnMutator;
    private final AssetUrnByElementMaterializer assetUrnByElementMaterializer;
    private final AssetUrnByElementMutator assetUrnByElementMutator;
    private final ErrorNotificationByAssetMaterializer errorNotificationByAssetMaterializer;
    private final ErrorNotificationByAssetMutator errorNotificationByAssetMutator;
    private final RequestNotificationByAssetMaterializer requestNotificationByAssetMaterializer;
    private final RequestNotificationByAssetMutator requestNotificationByAssetMutator;
    private final ResultNotificationByAssetMaterializer resultNotificationByAssetMaterializer;
    private final ResultNotificationByAssetMutator resultNotificationByAssetMutator;
    private final AssetRetryNotificationMutator assetRetryNotificationMutator;

    @Inject
    public MathAssetGateway(final Session session,
                            final AssetByHashMaterializer assetByHashMaterializer,
                            final AssetByHashMutator assetByHashMutator,
                            final AssetIdByUrnMaterializer assetIdByUrnMaterializer,
                            final AssetIdByUrnMutator assetIdByUrnMutator,
                            final AssetSummaryMaterializer assetSummaryMaterializer,
                            final AssetSummaryMutator assetSummaryMutator,
                            final UsageByAssetUrnMaterializer usageByAssetUrnMaterializer,
                            final UsageByAssetUrnMutator usageByAssetUrnMutator,
                            final AssetUrnByElementMaterializer assetUrnByElementMaterializer,
                            final AssetUrnByElementMutator assetUrnByElementMutator,
                            final ErrorNotificationByAssetMaterializer errorNotificationByAssetMaterializer,
                            final ErrorNotificationByAssetMutator errorNotificationByAssetMutator,
                            final RequestNotificationByAssetMaterializer requestNotificationByAssetMaterializer,
                            final RequestNotificationByAssetMutator requestNotificationByAssetMutator,
                            final ResultNotificationByAssetMaterializer resultNotificationByAssetMaterializer,
                            final ResultNotificationByAssetMutator resultNotificationByAssetMutator,
                            final AssetRetryNotificationMutator assetRetryNotificationMutator) {
        this.session = session;
        this.assetByHashMaterializer = assetByHashMaterializer;
        this.assetByHashMutator = assetByHashMutator;
        this.assetIdByUrnMaterializer = assetIdByUrnMaterializer;
        this.assetIdByUrnMutator = assetIdByUrnMutator;
        this.assetSummaryMaterializer = assetSummaryMaterializer;
        this.assetSummaryMutator = assetSummaryMutator;
        this.usageByAssetUrnMaterializer = usageByAssetUrnMaterializer;
        this.usageByAssetUrnMutator = usageByAssetUrnMutator;
        this.assetUrnByElementMaterializer = assetUrnByElementMaterializer;
        this.assetUrnByElementMutator = assetUrnByElementMutator;
        this.errorNotificationByAssetMaterializer = errorNotificationByAssetMaterializer;
        this.errorNotificationByAssetMutator = errorNotificationByAssetMutator;
        this.requestNotificationByAssetMaterializer = requestNotificationByAssetMaterializer;
        this.requestNotificationByAssetMutator = requestNotificationByAssetMutator;
        this.resultNotificationByAssetMaterializer = resultNotificationByAssetMaterializer;
        this.resultNotificationByAssetMutator = resultNotificationByAssetMutator;
        this.assetRetryNotificationMutator = assetRetryNotificationMutator;
    }

    /**
     * Persist math asset into summary table and all related filtering tables
     *
     * @param eventMessage math asset event message.
     * @param hash hashing of MathML
     * @param assetUrn the asset urn
     * @param elementId courseware element id
     * @param accountId account id
     * @param elementIds element ids list
     */
    @Trace(async = true)
    public Mono<AssetUrn> persist(final MathAssetEventMessage eventMessage,
                                  final String hash,
                                  final AssetUrn assetUrn,
                                  final UUID elementId,
                                  final UUID accountId,
                                  final List<String> elementIds) {

        AssetSummary assetSummary = new AssetSummary()
                .setId(assetUrn.getAssetId())
                .setHash(hash)
                .setAltText(eventMessage.getAlt())
                .setMathML(eventMessage.getMathML())
                .setSvgShape(eventMessage.getFormat())
                .setSvgText(eventMessage.getContent());

        AssetByHash assetByHash = new AssetByHash()
                .setHash(hash)
                .setAssetId(assetUrn.getAssetId())
                .setOwnerId(accountId);

        AssetUrnByElement assetUrnByElement = new AssetUrnByElement()
                .setAssetUrn(assetUrn.toString())
                .setElementId(elementId);

        UsageByAssetUrn usageByAssetUrn = new UsageByAssetUrn()
                .setAssetUrn(assetUrn.toString())
                .setAssetId(assetUrn.getAssetId())
                .setElementId(elementIds);

        Flux<? extends Statement> iter = Flux.just(
                assetSummaryMutator.upsert(assetSummary),
                assetByHashMutator.upsert(assetByHash),
                assetIdByUrnMutator.persist(assetUrn.toString(), assetUrn.getAssetId()),
                assetUrnByElementMutator.upsert(assetUrnByElement),
                usageByAssetUrnMutator.upsert(usageByAssetUrn)
        );

        return Mutators.execute(session, iter)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while saving math asset %s", eventMessage),
                              throwable);
                    throw Exceptions.propagate(throwable);
                })
                .then(Mono.just(assetUrn));
    }

    /**
     * Find a math asset by hash
     *
     * @param hash hashing of MathML
     */
    @Trace(async = true)
    public Mono<AssetByHash> findByHash(String hash) {
        return ResultSets.query(session, assetByHashMaterializer.findByHash(hash))
                .flatMapIterable(row -> row)
                .map(assetByHashMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching math asset with hash %s", hash), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find a math asset by hash
     *
     * @param assetUrn hashing of MathML
     */
    @Trace(async = true)
    public Mono<UsageByAssetUrn> findByUrn(String assetUrn) {
        return ResultSets.query(session, usageByAssetUrnMaterializer.findByUrn(assetUrn))
                .flatMapIterable(row -> row)
                .map(usageByAssetUrnMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching math asset with urn %s", assetUrn), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch all the asset URNs associated with an element
     *
     * @param elementId the id of the element to find the asset URNs for
     * @return a flux of string representing the asset urn
     */
    @Trace(async = true)
    public Flux<String> findAssetUrn(final UUID elementId) {
        return ResultSets.query(session, assetUrnByElementMaterializer.findAssetUrnFor(elementId))
                .flatMapIterable(row -> row)
                .map(assetUrnByElementMaterializer::fromRow)
                .map(AssetUrnByElement::getAssetUrn)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch an asset id by urn
     *
     * @param assetUrn the asset urn
     * @return mono with AssetIdByUrn, empty mono if asset id is not found
     */
    @Trace(async = true)
    public Mono<AssetIdByUrn> fetchAssetIdByUrn(final String assetUrn) {
        return ResultSets.query(session, assetIdByUrnMaterializer.findAssetId(assetUrn))
                .flatMapIterable(row -> row)
                .map(assetIdByUrnMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch an asset by id
     *
     * @param id the asset id
     * @return mono with AssetSummary, empty mono if asset id is not found
     */
    @Trace(async = true)
    public Mono<AssetSummary> fetchAssetById(UUID id) {
        return ResultSets.query(session, assetSummaryMaterializer.findById(id))
                .flatMapIterable(row -> row)
                .map(assetSummaryMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Remove association between courseware element and asset
     *
     * @param elementId the element id
     * @param assetUrn contains asset id and asset provider details
     */
    @Trace(async = true)
    public Flux<Void> remove(final UUID elementId, final AssetUrn assetUrn) {
        AssetUrnByElement assetUrnByElement = new AssetUrnByElement()
                .setAssetUrn(assetUrn.toString())
                .setElementId(elementId);

        return Mutators.execute(session, Flux.just(
                        assetUrnByElementMutator.delete(assetUrnByElement),
                        usageByAssetUrnMutator.removeElementId(elementId, assetUrn.toString()))
                .doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Save the math resolver notification request log
     *
     * @param notification math resolver request
     */
    @Trace(async = true)
    public Flux<Void> persist(final MathAssetRequestNotification notification) {
        return Mutators.execute(session, Flux.just(
                requestNotificationByAssetMutator.upsert(notification)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving math resolver request log %s",
                                    notification), throwable);
            throw Exceptions.propagate(throwable);
        }).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the math resolver notification result log
     *
     * @param notification math resolver result
     */
    @Trace(async = true)
    public Flux<Void> persist(final MathAssetResultNotification notification) {
        return Mutators.execute(session, Flux.just(
                resultNotificationByAssetMutator.upsert(notification)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving math resolver result log %s",
                                    notification), throwable);
            throw Exceptions.propagate(throwable);
        }).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the math resolver notification retry log
     *
     * @param notification math resolver retry
     */
    @Trace(async = true)
    public Flux<Void> persist(final MathAssetRetryNotification notification) {
        return Mutators.execute(session, Flux.just(
                assetRetryNotificationMutator.upsert(notification)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving math resolver retry log %s",
                                    notification), throwable);
            throw Exceptions.propagate(throwable);
        }).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the math resolver notification error log
     *
     * @param notification math resolver error
     */
    @Trace(async = true)
    public Flux<Void> persist(final MathAssetErrorNotification notification) {
        return Mutators.execute(session, Flux.just(
                errorNotificationByAssetMutator.upsert(notification)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving math resolver error log %s",
                                    notification), throwable);
            throw Exceptions.propagate(throwable);
        }).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save the svg shape for the provided math asset id
     *
     * @param id       math asset id
     * @param svgShape the svg string to save
     */
    @Trace(async = true)
    public Flux<Void> persist(final UUID id, final String svgShape) {
        return Mutators.execute(session, Flux.just(
                assetSummaryMutator.setSvgShape(id, svgShape)
        )).doOnError(throwable -> {
            log.error(String.format("error while updating math asset svg shape %s", id), throwable);
            throw Exceptions.propagate(throwable);
        }).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Update an association between courseware element and math asset
     *
     * @param elementId the element id
     * @param assetUrn contains asset id and asset provider details
     */
    @Trace(async = true)
    public Mono<AssetUrn> update(final UUID elementId, final AssetUrn assetUrn) {
        AssetUrnByElement assetUrnByElement = new AssetUrnByElement()
                .setAssetUrn(assetUrn.toString())
                .setElementId(elementId);

        Flux<? extends Statement> iter = Flux.just(
                assetUrnByElementMutator.upsert(assetUrnByElement),
                usageByAssetUrnMutator.addElementId(elementId, assetUrn.toString())
        );

        return Mutators.execute(session, iter)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while updating math asset association for AssetUrnByElement %s",
                                            assetUrnByElement), throwable);
                    throw Exceptions.propagate(throwable);
                })
                .then(Mono.just(assetUrn));
    }
}
