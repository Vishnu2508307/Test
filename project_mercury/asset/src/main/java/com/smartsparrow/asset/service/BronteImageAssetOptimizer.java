package com.smartsparrow.asset.service;

import static com.smartsparrow.asset.data.ImageSourceName.THUMB;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.Arrays;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.asset.data.AssetErrorNotification;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetRequestNotification;
import com.smartsparrow.asset.data.AssetResultNotification;
import com.smartsparrow.asset.data.AssetRetryNotification;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.asset.data.ImageSourceName;
import com.smartsparrow.asset.route.AssetRoute;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class BronteImageAssetOptimizer {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(BronteImageAssetOptimizer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final ProducerTemplate producerTemplate;
    private final AssetGateway assetGateway;
    private final AssetConfig assetConfig;

    @Inject
    public BronteImageAssetOptimizer(final ProducerTemplate producerTemplate,
                                     final AssetGateway assetGateway,
                                     final AssetConfig assetConfig) {
        this.producerTemplate = producerTemplate;
        this.assetGateway = assetGateway;
        this.assetConfig = assetConfig;
    }

    /**
     * Sends an sns notification for each required output size. The sns message will trigger a lambda function
     * that will take care of resizing the image. The process is async
     *
     * @param imageSource the source of the original image to be resized
     * @return a mono with the assetId being optimized
     */
    public Mono<UUID> optimize(final ImageSource imageSource) {
        Double maxSize = imageSource.getHeight() > imageSource.getWidth() ? imageSource.getHeight() : imageSource.getWidth();
        return Arrays.stream(ImageSourceName.values())
                .filter(name -> name.getLabel().equals(THUMB.getLabel()) || maxSize > name.getThreshold())
                .map(name -> {
                    UUID notificationId = com.smartsparrow.util.UUIDs.timeBased();
                    AssetRequestNotification request = new AssetRequestNotification()
                            .setAssetId(imageSource.getAssetId())
                            .setOriginalHeight(imageSource.getHeight())
                            .setOriginalWidth(imageSource.getWidth())
                            .setUrl(imageSource.getUrl())
                            .setNotificationId(notificationId)
                            .setSize(name.getLabel())
                            .setThreshold(name.getThreshold());
                    return sendNotificationToSNS(request);
                }).reduce(Flux::merge)
                .orElse(Flux.empty())
                .then(Mono.just(imageSource.getAssetId()))
                .doOnEach(log.reactiveErrorThrowable("error triggering the image asset optimizer"));
    }

    /**
     * Persist and process the result
     *
     * @param resultNotification the result notification
     * @param messagePayload     sns payload message
     * @return mono of AssetNotification
     */
    public Mono<AssetResultNotification> processResultNotification(final AssetResultNotification resultNotification,
                                                                   final String messagePayload) {
        affirmNotNull(resultNotification, "resultNotification is required");
        affirmNotNull(messagePayload, "messagePayload is required");


        ImageSource image = new ImageSource()
                .setAssetId(resultNotification.getAssetId())
                .setHeight(resultNotification.getHeight())
                .setName(ImageSourceName.fromLabel(resultNotification.getSize()))
                .setWidth(resultNotification.getWidth())
                .setUrl(resultNotification.getUrl());

        return Flux.merge(assetGateway.persist(resultNotification),
                assetGateway.persist(image))
                .then(Mono.just(resultNotification));
    }

    /**
     * Process an error notification. These are errors which are caught within the external processing.
     *
     * @param errorNotification the export error notification
     * @param messagePayload    sns payload message
     * @return a mono of the supplied AssetErrorNotification argument
     */
    public Mono<AssetErrorNotification> processErrorNotification(final AssetErrorNotification errorNotification,
                                                                 final String messagePayload) {
        affirmNotNull(errorNotification, "errorNotification is required");
        affirmNotNull(messagePayload, "messagePayload is required");

        return assetGateway.persist(errorNotification)
                .then(Mono.just(errorNotification));
    }

    /**
     * Process a retry notification, brokers to the proper handler based on the purpose.
     *
     * @param retryNotification the notification
     * @param messagePayload    the sns payload message
     * @return a Mono of the supplied RetryNotification argument
     */
    public Mono<AssetRetryNotification> processRetryNotification(final AssetRetryNotification retryNotification,
                                                                 final String messagePayload) {
        affirmNotNull(retryNotification, "retryNotification is required");
        affirmNotNull(messagePayload, "messagePayload is required");
        return assetGateway.persist(retryNotification)
                .then(Mono.just(retryNotification));
    }

    /**
     * Send sns notification for each asset size
     *
     * @param request asset resize request notification object
     * @return flux of void
     */
    private Flux<Void> sendNotificationToSNS(final AssetRequestNotification request) {
        Exchange response = producerTemplate
                .request(AssetRoute.SUBMIT_ASSET_RESIZE_REQUEST, exchange -> {
                    Message m = exchange.getIn();
                    m.setBody(mapper.writeValueAsString(request));
                    m.setHeader("bucketName", assetConfig.getBucketName());
                });
        if (response.isFailed()) {
            Exception exception = (Exception) response.getProperty(Exchange.EXCEPTION_CAUGHT);
            AssetErrorNotification errorNotification = new AssetErrorNotification()
                    .setAssetId(request.getAssetId())
                    .setNotificationId(request.getNotificationId());
            if (exception != null) {
                errorNotification
                        .setErrorMessage(exception.getMessage() != null ? exception.getMessage() : "unknown exception")
                        .setCause(exception.getCause() != null ? exception.getCause().getMessage() : "unknown cause");
            } else {
                errorNotification.setErrorMessage("unknown exception").setCause("unknown cause");
            }

            return Flux.merge(assetGateway.persist(request),
                    assetGateway.persist(errorNotification));
        }
        return assetGateway.persist(request);
    }
}
