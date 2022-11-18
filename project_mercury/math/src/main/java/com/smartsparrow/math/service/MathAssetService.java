package com.smartsparrow.math.service;

import static com.smartsparrow.asset.data.AssetProvider.MATH;
import static com.smartsparrow.dataevent.RouteUri.MATH_ASSET_GET;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetBuilder;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.asset.data.MathAssetData;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.AssetUtils;
import com.smartsparrow.math.config.MathConfig;
import com.smartsparrow.math.data.AssetIdByUrn;
import com.smartsparrow.math.data.AssetSummary;
import com.smartsparrow.math.data.MathAssetErrorNotification;
import com.smartsparrow.math.data.MathAssetGateway;
import com.smartsparrow.math.data.MathAssetRequestNotification;
import com.smartsparrow.math.data.MathAssetResultNotification;
import com.smartsparrow.math.data.MathAssetRetryNotification;
import com.smartsparrow.math.event.MathAssetEventMessage;
import com.smartsparrow.math.route.MathRoute;
import com.smartsparrow.util.Hashing;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Singleton
public class MathAssetService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MathAssetService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final MathAssetGateway mathAssetGateway;
    private final CamelReactiveStreamsService camelReactiveStreamsService;
    private final ProducerTemplate producerTemplate;
    private final MathConfig mathConfig;
    private final AssetBuilder assetBuilder;

    @Inject
    public MathAssetService(MathAssetGateway mathAssetGateway,
                            CamelReactiveStreamsService camelReactiveStreamsService,
                            final ProducerTemplate producerTemplate,
                            final MathConfig mathConfig,
                            AssetBuilder assetBuilder) {
        this.mathAssetGateway = mathAssetGateway;
        this.camelReactiveStreamsService = camelReactiveStreamsService;
        this.producerTemplate = producerTemplate;
        this.mathConfig = mathConfig;
        this.assetBuilder = assetBuilder;
    }

    @Trace(async = true)
    public Mono<AssetUrn> createMathAsset(final String mathML,
                                          final String altText,
                                          final UUID elementId,
                                          final UUID accountId) {
        String hash = Hashing.string(mathML);

        return mathAssetGateway.findByHash(hash)
                .flatMap(assetByHash -> {
                    AssetUrn assetUrn = new AssetUrn(assetByHash.getAssetId(), MATH);
                    return mathAssetGateway.update(elementId, assetUrn);
                })
                .switchIfEmpty(Mono.just(new MathAssetEventMessage(mathML)) //
                                       .doOnEach(log.reactiveInfo("handling MathML get image content"))
                                       .map(event -> camelReactiveStreamsService.toStream(MATH_ASSET_GET,
                                                                                          event,
                                                                                          MathAssetEventMessage.class)) //
                                       .doOnError(ex -> {
                                           ex = Exceptions.unwrap(ex);
                                           log.reactiveError("exception MathML get image content: " + ex.getMessage());
                                       })
                                       .flatMap(Mono::from)
                                       .map(eventMessage -> {
                                           if (altText != null && !altText.isEmpty()) {
                                               log.info(String.format("Setting eventMessage with altText: %s",
                                                                      altText));
                                               eventMessage.setAlt(altText);
                                           }
                                           return eventMessage;
                                       })
                                       .flatMap(eventMessage -> {
                                           log.reactiveInfo("handling MathML wiris result");
                                           UUID assetId = UUIDs.timeBased();
                                           AssetUrn assetUrn = new AssetUrn(assetId, MATH);
                                           return mathAssetGateway.findByUrn(assetUrn.toString())
                                                   .flatMap(usageByAssetUrn -> {
                                                       List<String> elementIds = usageByAssetUrn.getElementId();
                                                       if (elementIds == null) {
                                                           elementIds = new ArrayList<>();
                                                       }
                                                       elementIds.add(elementId.toString());
                                                       return Mono.just(elementIds);
                                                   })
                                                   .switchIfEmpty(Mono.just(Collections.singletonList(elementId.toString())))
                                                   .flatMap(elementIds -> mathAssetGateway.persist(eventMessage,
                                                                                                   hash,
                                                                                                   assetUrn,
                                                                                                   elementId,
                                                                                                   accountId,
                                                                                                   elementIds));
                                       })
                                       .flatMap(urn -> {
                                           log.reactiveInfo("handling MathML MathJax request");
                                           UUID notificationId = com.smartsparrow.util.UUIDs.timeBased();
                                           MathAssetRequestNotification request = new MathAssetRequestNotification()
                                                   .setAssetId(urn.getAssetId())
                                                   .setNotificationId(notificationId)
                                                   .setmathML(mathML);
                                           return sendNotificationToSNS(request)
                                                   .then(Mono.just(urn));
                                       })
                )
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find all the assetUrn associated to an elementId with the corresponding assetId that
     * was last associated to the assetUrn
     *
     * @param elementId the element to find the urn and asset id for
     * @return a flux of AssetIdByUrn object
     */
    @Trace(async = true)
    public Flux<AssetIdByUrn> getAssetsFor(final UUID elementId) {
        affirmArgument(elementId != null, "elementId is required");

        return mathAssetGateway.findAssetUrn(elementId)
                .flatMap(this::getAssetIdByUrn)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the last associated asset id to this asset urn
     *
     * @param assetUrn the urn to find the asset id for
     * @return a mono containing the AssetIdByUrn object
     */
    @Trace(async = true)
    public Mono<AssetIdByUrn> getAssetIdByUrn(final String assetUrn) {
        affirmArgumentNotNullOrEmpty(assetUrn, "assetUrn is required");

        return mathAssetGateway.fetchAssetIdByUrn(assetUrn)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Get the math asset payload by urn. Find the asset id associated to the urn and then fetches the asset object.
     *
     * @param urn the urn to resolve the math asset for
     * @return a mono of AssetSummary
     */
    @Trace(async = true)
    public Mono<AssetSummary> getMathAssetSummary(final String urn) {
        // try fetching the asset id by urn
        return mathAssetGateway.fetchAssetIdByUrn(urn)
                .map(AssetIdByUrn::getAssetId)
                // get the asset payload
                .flatMap(this::getMathAssetSummaryById)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveErrorThrowable("failed to get asset summary",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("assetUrn", urn);
                                                         }
                                                     }));
    }

    /**
     * Get the math asset payload by id.
     *
     * @param id the id to resolve the math asset for
     * @return a mono of AssetSummary
     */
    @Trace(async = true)
    public Mono<AssetSummary> getMathAssetSummaryById(final UUID id) {
        // try fetching the asset by id
        return mathAssetGateway.fetchAssetById(id)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveErrorThrowable("failed to get asset summary",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("assetId", id);
                                                         }
                                                     }));
    }

    /**
     * Remove a math asset from a courseware element
     *
     * @param elementId the courseware element id
     * @param assetUrn the asset URN
     * @throws AssetURNParseException if asset URN is invalid
     */
    @Trace(async = true)
    public Flux<Void> removeMathAsset(final UUID elementId, final String assetUrn) {
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(!Strings.isNullOrEmpty(assetUrn), "assetUrn is required");

        return mathAssetGateway.remove(elementId, AssetUtils.parseURN(assetUrn))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Send sns notification for math asset
     *
     * @param request asset resolver request notification object
     * @return flux of void
     */
    @Trace(async = true)
    private Flux<Void> sendNotificationToSNS(final MathAssetRequestNotification request) {
        Exchange response = producerTemplate
                .request(MathRoute.SUBMIT_MATH_RESOLVER_REQUEST, exchange -> {
                    Message m = exchange.getIn();
                    m.setBody(mapper.writeValueAsString(request));
                    m.setHeader("bucketName", mathConfig.getBucketName());
                });
        if (response.isFailed()) {
            Exception exception = (Exception) response.getProperty(Exchange.EXCEPTION_CAUGHT);
            MathAssetErrorNotification errorNotification = new MathAssetErrorNotification()
                    .setAssetId(request.getAssetId())
                    .setNotificationId(request.getNotificationId());
            if (exception != null) {
                errorNotification
                        .setErrorMessage(exception.getMessage() != null ? exception.getMessage() : "unknown exception")
                        .setCause(exception.getCause() != null ? exception.getCause().getMessage() : "unknown cause");
            } else {
                errorNotification.setErrorMessage("unknown exception").setCause("unknown cause");
            }

            return Flux.merge(mathAssetGateway.persist(request),
                              mathAssetGateway.persist(errorNotification));
        }
        return mathAssetGateway.persist(request);
    }

    /**
     * Persist and process the result
     *
     * @param resultNotification the result notification
     * @return mono of MathAssetResultNotification
     */
    public Mono<MathAssetResultNotification> processResultNotification(final MathAssetResultNotification resultNotification) {
        affirmNotNull(resultNotification, "resultNotification is required");

        return Flux.merge(mathAssetGateway.persist(resultNotification),
                          mathAssetGateway.persist(resultNotification.getAssetId(), resultNotification.getSvgShape()))
                .then(Mono.just(resultNotification));
    }

    /**
     * Process an error notification. These are errors which are caught within the external processing.
     *
     * @param errorNotification the export error notification
     * @return a mono of the supplied MathAssetErrorNotification argument
     */
    public Mono<MathAssetErrorNotification> processErrorNotification(final MathAssetErrorNotification errorNotification) {
        affirmNotNull(errorNotification, "errorNotification is required");

        return mathAssetGateway.persist(errorNotification)
                .then(Mono.just(errorNotification));
    }

    /**
     * Process a retry notification, brokers to the proper handler based on the purpose.
     *
     * @param retryNotification the notification
     * @return a Mono of the supplied RetryNotification argument
     */
    public Mono<MathAssetRetryNotification> processRetryNotification(final MathAssetRetryNotification retryNotification) {
        affirmNotNull(retryNotification, "retryNotification is required");

        return mathAssetGateway.persist(retryNotification)
                .then(Mono.just(retryNotification));
    }

    /**
     * Fetch math asset payload by asset id
     *
     * @param assetId the asset id
     * @return mono with asset info, or empty mono if no asset for the urn
     */
    @Trace(async = true)
    @SuppressWarnings("Duplicates")
    public Mono<AssetPayload> getMathAssetPayload(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");
        return getMathAssetSummaryById(assetId)
                .map(assetSummary -> {
                         MathAssetData mathAssetData = new MathAssetData()
                                 .setId(assetSummary.getId())
                                 .setMathML(assetSummary.getMathML())
                                 .setAltText(assetSummary.getAltText())
                                 .setHash(assetSummary.getHash())
                                 .setSvgShape(assetSummary.getSvgShape())
                                 .setSvgText(assetSummary.getSvgText());

                         AssetPayload assetPayload = new AssetPayload();
                         assetPayload.setAsset(assetBuilder
                                                       .setAssetSummary(new com.smartsparrow.asset.data.AssetSummary()
                                                                                .setId(assetId)
                                                                                .setProvider(AssetProvider.MATH)
                                                                                .setHash(assetSummary.getHash()))
                                                       .setMathAssetData(mathAssetData)
                                                       .build(AssetProvider.MATH))
                                 .setUrn(String.format("urn:%s:%s", AssetProvider.MATH.getLabel(), assetSummary.getId()));
                         return assetPayload;
                     }
                )
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch math feature is enabled or not from mathConfig
     *
     * @return boolean value for feature enabled
     */
    public boolean isFeatureEnabled() {
        return mathConfig.isEnabled();
    }
}
