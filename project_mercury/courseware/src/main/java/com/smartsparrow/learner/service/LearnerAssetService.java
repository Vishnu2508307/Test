package com.smartsparrow.learner.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetBuilder;
import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.IconSource;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.asset.data.MathAssetData;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.AssetService;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.asset.service.ExternalAssetService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareAssetService;
import com.smartsparrow.courseware.service.WorkspaceAssetService;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerAssetGateway;
import com.smartsparrow.math.service.MathAssetService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerAssetService implements AssetService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerAssetService.class);

    private final CoursewareAssetService coursewareAssetService;
    private final LearnerAssetGateway learnerAssetGateway;
    private final BronteAssetService bronteAssetService;
    private final AssetBuilder assetBuilder;
    private final ExternalAssetService externalAssetService;
    private final WorkspaceAssetService workspaceAssetService;
    private final MathAssetService mathAssetService;

    @Inject
    public LearnerAssetService(CoursewareAssetService coursewareAssetService,
                               LearnerAssetGateway learnerAssetGateway,
                               BronteAssetService bronteAssetService,
                               AssetBuilder assetBuilder,
                               ExternalAssetService externalAssetService,
                               WorkspaceAssetService workspaceAssetService,
                               MathAssetService mathAssetService) {
        this.coursewareAssetService = coursewareAssetService;
        this.learnerAssetGateway = learnerAssetGateway;
        this.bronteAssetService = bronteAssetService;
        this.assetBuilder = assetBuilder;
        this.externalAssetService = externalAssetService;
        this.workspaceAssetService = workspaceAssetService;
        this.mathAssetService = mathAssetService;
    }

    /**
     * Publishes assets for a courseware element to a given deployment.
     *
     * @param deployment  the deployment to publish the assets to
     * @param elementId   the elementId to associate the asset/s to
     * @param elementType the type of element
     * @return a flux of void
     */
    public Flux<Void> publishAssetsFor(final Deployment deployment, final UUID elementId, final CoursewareElementType elementType) {

        // try publishing persisting the new association predicated on asset urn
        Flux<UUID> publishAssets = coursewareAssetService.getAssetsFor(elementId)
                // for each asset id/urn
                .flatMap(assetIdByUrn -> Flux.merge(
                        // persist the associations
                        learnerAssetGateway.persist(assetIdByUrn, CoursewareElement.from(elementId, elementType), deployment),
                        // find the asset summary
                        bronteAssetService.getAssetSummary(assetIdByUrn.getAssetId())
                                //change the asset provider to AERO if it's an ALFRESCO as mercury supports only AERO and EXTERNAL
                                .map(assetSummary ->  {
                                    if(assetSummary.getProvider().equals(AssetProvider.ALFRESCO)) {
                                        assetSummary.setProvider(AssetProvider.AERO);
                                    }
                                    return assetSummary;
                                })
                                .flux()
                                // persist the asset summary
                                .flatMap(summary -> learnerAssetGateway.persist(summary)
                                        // publish the asset source
                                        .thenMany(publishAssetSource(assetIdByUrn.getAssetId(), summary.getProvider(), summary.getMediaType()))
                                        // fetch the metadata
                                        .thenMany(bronteAssetService.getAssetMetadata(assetIdByUrn.getAssetId())
                                                // publish the metadata
                                                .flatMap(learnerAssetGateway::persist))))
                        .then(Mono.just(assetIdByUrn.getAssetId())));

        return publishAssets
                .then()
                .flux();
    }

    /**
     * Publishes math assets for a courseware element to a given deployment.
     *
     * @param deployment  the deployment to publish the assets to
     * @param elementId   the elementId to associate the asset/s to
     * @param elementType the type of element
     * @return a flux of void
     */
    public Flux<Void> publishMathAssetsFor(final Deployment deployment,
                                           final UUID elementId,
                                           final CoursewareElementType elementType) {

        // try publishing persisting the new association predicated on math asset urn
        Flux<UUID> publishAssets = mathAssetService.getAssetsFor(elementId)
                // for each math asset id/urn
                .flatMap(assetIdByUrn -> Flux.merge(
                                // persist the associations
                                learnerAssetGateway.persist(assetIdByUrn,
                                                            CoursewareElement.from(elementId, elementType),
                                                            deployment),
                                // find the math asset summary
                                mathAssetService.getMathAssetSummaryById(assetIdByUrn.getAssetId())
                                        .flux()
                                        // persist the math asset summary
                                        .flatMap(learnerAssetGateway::persist)
                        )
                        .then(Mono.just(assetIdByUrn.getAssetId())));
        return publishAssets
                .then()
                .flux();
    }

    /**
     * Fetch assets for an elementId and a changeId
     *
     * @param elementId the elementId to fetch the assets for
     * @param changeId  the changeId to fetch the assets for
     * @return a flux of AssetPayload{@link Flux<AssetPayload>}
     */
    public Flux<AssetPayload> fetchAssetsForElementAndChangeId(final UUID elementId, final UUID changeId) {
        // fetch the asset urn associated to this element
        return learnerAssetGateway.findAssetsUrn(elementId, changeId)
                // for each urn find the asset id
                .flatMap(assetUrn -> learnerAssetGateway.findAssetId(assetUrn)
                        .map(AssetIdByUrn::getAssetId))
                .flatMap(this::getAssetPayload);
    }

    /**
     * Fetch math assets for an elementId and a changeId
     *
     * @param elementId the elementId to fetch the assets for
     * @param changeId  the changeId to fetch the assets for
     * @return a flux of AssetPayload{@link Flux<AssetPayload>}
     */
    public Flux<AssetPayload> fetchMathAssetsForElementAndChangeId(final UUID elementId, final UUID changeId) {
        // fetch the asset urn associated to this element
        return learnerAssetGateway.findMathAssetsUrn(elementId, changeId)
                // for each urn find the asset id
                .flatMap(assetUrn -> learnerAssetGateway.findMathAssetId(assetUrn)
                        .map(com.smartsparrow.math.data.AssetIdByUrn::getAssetId))
                .flatMap(this::getMathAssetPayload);
    }

    /**
     * Publishes the asset source data to the learner space on deployment
     *
     * @param assetId   the asset id
     * @param provider  the asset provider
     * @param mediaType the ype of the asset
     * @return a flux of void
     */
    public Flux<Void> publishAssetSource(final UUID assetId, final AssetProvider provider, final AssetMediaType mediaType) {
        if (provider.equals(AssetProvider.EXTERNAL)) {
            return bronteAssetService.getExternalSourceRecord(assetId)
                    .flux()
                    .flatMap(learnerAssetGateway::persist);
        }
        switch (mediaType) {
            case AUDIO:
                return bronteAssetService.getAudioSource(assetId)
                        .flatMap(learnerAssetGateway::persist);
            case DOCUMENT:
                return bronteAssetService.getDocumentSource(assetId)
                        .flux()
                        .flatMap(learnerAssetGateway::persist);
            case IMAGE:
                return bronteAssetService.getImageSource(assetId)
                        .flatMap(learnerAssetGateway::persist);
            case VIDEO:
                return Flux.merge(bronteAssetService.getVideoSource(assetId)
                                .flatMap(learnerAssetGateway::persist),
                        bronteAssetService.getVideoSubtitle(assetId)
                                .flatMap(learnerAssetGateway::persist));
            case ICON:
                return bronteAssetService.getIconSource(assetId)
                        .flatMap(learnerAssetGateway::persist);

            default:
                throw new UnsupportedOperationException("Asset fetching is not supported for " + mediaType);
        }
    }

    /**
     * Fetch asset payload by URN
     *
     * @param urn the uniform resource name which is uniquely identifying the asset
     * @return mono with asset info, or empty mono if no asset for the urn
     * @throws AssetURNParseException if urn can not be parsed
     */
    @Trace(async = true)
    @Override
    public Mono<AssetPayload> getAssetPayload(final String urn) {
        affirmArgument(!Strings.isNullOrEmpty(urn), "urn is required");
        // try fetching the asset id by urn
        return learnerAssetGateway.findAssetId(urn)
                .map(AssetIdByUrn::getAssetId)
                // Try and get the asset from the learner space first, but if it's not found fetch it from asset keyspace.
                .flatMap(assetId -> getAssetPayload(assetId))
                // log a line if anything goes wrong, thank you
                .doOnEach(log.reactiveErrorThrowable("failed to get asset payload", throwable -> new HashMap<String, Object>(){
                    {put("assetUrn", urn);}
                }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch asset payload by asset id
     *
     * @param assetId the asset id
     * @return mono with asset info, or empty mono if no asset for the urn
     */
    @Trace(async = true)
    @SuppressWarnings("Duplicates")
    public Mono<AssetPayload> getAssetPayload(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");
        Mono<AssetSummary> summary = learnerAssetGateway.fetchAssetById(assetId)
                .doOnEach(ReactiveTransaction.linkOnNext());
        Mono<Map<String, String>> metadata = learnerAssetGateway.fetchMetadata(assetId)
                .collectMap(AssetMetadata::getKey, AssetMetadata::getValue)
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<Map<String, Object>> sources = summary.flatMap(asset -> {
            // when the provider is external always lookup the external source
            if (asset.getProvider().equals(AssetProvider.EXTERNAL)) {
                return getExternalSource(assetId).doOnEach(ReactiveTransaction.linkOnNext());
            }
            switch (asset.getMediaType()) {
                case IMAGE:
                    return getImageSourcePayload(assetId).doOnEach(ReactiveTransaction.linkOnNext());
                case VIDEO:
                    return getVideoSourcePayload(assetId).doOnEach(ReactiveTransaction.linkOnNext());
                case DOCUMENT:
                    return getDocumentSourcePayload(assetId).doOnEach(ReactiveTransaction.linkOnNext());
                case AUDIO:
                    return getAudioSourcePayload(assetId).doOnEach(ReactiveTransaction.linkOnNext());
                case ICON:
                    return getIconSourcePayload(assetId).doOnEach(ReactiveTransaction.linkOnNext());
                default:
                    return Mono.error(new UnsupportedOperationException("Asset fetching is not supported for " + asset.getMediaType()));
            }
        });

        return Mono.zip(summary, metadata, sources).map(tuple3 -> new AssetPayload()
                .setUrn(tuple3.getT1().getUrn())
                .setAsset(assetBuilder.setAssetSummary(tuple3.getT1())
                        .build(tuple3.getT1().getProvider())) //
                .putAllMetadata(tuple3.getT2())
                .putAllSources(tuple3.getT3()));
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
        return learnerAssetGateway.fetchMathAssetById(assetId)
                .map(assetSummary -> {
                         MathAssetData mathAssetData = new MathAssetData()
                                 .setId(assetSummary.getId())
                                 .setMathML(assetSummary.getMathML())
                                 .setAltText(assetSummary.getAltText())
                                 .setHash(assetSummary.getHash())
                                 .setSvgShape(assetSummary.getSvgShape())
                                 .setSvgText(assetSummary.getSvgText());

                         HashMap<String, Object> map = new HashMap<>();
                         map.put("mathML", assetSummary.getMathML());
                         map.put("altText", assetSummary.getAltText());
                         map.put("svgText", assetSummary.getSvgText());
                         map.put("svgShape", assetSummary.getSvgShape());

                         AssetPayload assetPayload = new AssetPayload();
                         assetPayload.setAsset(assetBuilder
                                                       .setAssetSummary(new AssetSummary()
                                                                                .setId(assetId)
                                                                                .setProvider(AssetProvider.MATH)
                                                                                .setHash(assetSummary.getHash()))
                                                       .setMathAssetData(mathAssetData)
                                                       .build(AssetProvider.MATH))
                                 .putAllSources(map)
                                 .setUrn(String.format("urn:%s:%s", AssetProvider.MATH.getLabel(), assetSummary.getId()));
                         return assetPayload;
                     }
                )
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Return the external source for the asset as a map
     *
     * @param assetId the asset id to get the source for
     * @return a mono map of the external source payload or an empty stream when the external source is not found
     */
    @Trace(async = true)
    Mono<Map<String, Object>> getExternalSource(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");

        return learnerAssetGateway.fetchExternalSource(assetId)
                .flatMap(externalSource -> externalAssetService.buildPublicUrl(externalSource.getUrl())
                        .map(signedUrl -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("url", signedUrl);
                            return map;
                        }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }


    /**
     * Collect all image sources for the asset into map with ImageSourceName as keys.
     * For example if the asset has two images sources small and original the result map will look like this:
     * {
     * "small": {
     * "width":72,
     * "height":72,
     * "url":"..."
     * },
     * "original": {
     * "width":100,
     * "height":100,
     * "url":"..."
     * }
     * }
     *
     * @param assetId the asset id
     * @return the mono with map, mono with empty map if there are no sources for the asset
     */
    @Trace(async = true)
    Mono<Map<String, Object>> getImageSourcePayload(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");
        return learnerAssetGateway.fetchImageSources(assetId)
                .flatMap(imageSource -> bronteAssetService.buildPublicUrl(imageSource.getUrl())
                        .map(signedUrl -> Pair.of(imageSource, signedUrl)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .collectMap(pair -> pair.getLeft().getName().getLabel(), pair -> {
                    HashMap<String, Object> map = new HashMap<>();
                    ImageSource imageSource = pair.getLeft();
                    map.put("url", pair.getRight());
                    map.put("width", imageSource.getWidth());
                    map.put("height", imageSource.getHeight());
                    return map;
                });
    }


    /**
     * Collect all video sources and subtitles for the asset into a map.
     * For example if the asset has a video source and subtitles the result map will look like this:
     * {
     * "original": {
     * "resolution":720p,
     * "url":"..."
     * },
     * "subtitles": {
     * "en_US":"https://...",
     * "en_AU":"https://..."
     * }
     * }
     *
     * @param assetId the asset id
     * @return the mono with map or mono with empty map if there are no sources/subtitles for the asset
     */
    @Trace(async = true)
    Mono<Map<String, Object>> getVideoSourcePayload(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");
        Mono<Map<String, Object>> sources = learnerAssetGateway.fetchVideoSources(assetId)
                .flatMap(videoSource -> bronteAssetService.buildPublicUrl(videoSource.getUrl())
                        .map(signedUrl -> Pair.of(videoSource, signedUrl)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .collectMap(pair -> pair.getLeft().getName().getLabel(), pair -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("url", pair.getRight());
                    map.put("resolution", pair.getLeft().getResolution());
                    return map;
                });

        Mono<Map<String, Object>> subtitles = learnerAssetGateway.fetchVideoSubtitles(assetId)
                .flatMap(videoSubtitle -> bronteAssetService.buildPublicUrl(videoSubtitle.getUrl())
                        .map(signedUrl -> Pair.of(videoSubtitle, signedUrl)))
                .collectMap(pair -> pair.getLeft().getLang(), Pair::getRight);

        return Mono.zip(sources, subtitles).map(tuple2 -> {
            if (!tuple2.getT2().isEmpty()) {
                tuple2.getT1().put("subtitles", tuple2.getT2());
            }
            return tuple2.getT1();
        });
    }

    /**
     * Return document source for the asset as a map.
     * For example if the asset has two images sources small and original the result map will look like this:
     * {
     * "url": "https://..."
     * }
     *
     * @param assetId the asset id
     * @return the mono with map or mono with empty map if there is no document source for the asset
     */
    @Trace(async = true)
    Mono<Map<String, Object>> getDocumentSourcePayload(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");
        return learnerAssetGateway.fetchDocumentSource(assetId)
                .flatMap(doc -> {
                    return bronteAssetService.buildPublicUrl(doc.getUrl())
                            .map(signedUrl -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("url", signedUrl);
                                return map;
                            });
                }).defaultIfEmpty(new HashMap<>())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Return audio source for the asset as a map.
     *
     * @param assetId the asset id
     * @return a mono map of the audio source payload or an empty mono when audio source not found
     */
    @Trace(async = true)
    Mono<Map<String, Object>> getAudioSourcePayload(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");
        return learnerAssetGateway.fetchAudioSources(assetId)
                .flatMap(audioSource -> bronteAssetService.buildPublicUrl(audioSource.getUrl())
                        .map(signedUrl -> Pair.of(audioSource, signedUrl)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .collectMap(pair -> pair.getLeft().getName().getLabel(), pair -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("url", pair.getRight());
                    return map;
                });
    }

    /**
     * Collect all icon sources for the asset into map with IconSourceName as keys.
     * For example if the asset has two icon sources small and original the result map will look like this:
     * {
     * "small": {
     * "width":72,
     * "height":72,
     * "url":"..."
     * },
     * "original": {
     * "width":100,
     * "height":100,
     * "url":"..."
     * }
     * }
     *
     * @param assetId the asset id
     * @return the mono with map, mono with empty map if there are no sources for the asset
     */
    @Trace(async = true)
    Mono<Map<String, Object>> getIconSourcePayload(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");
        return learnerAssetGateway.fetchIconSources(assetId)
                .flatMap(iconSource -> bronteAssetService.buildPublicUrl(iconSource.getUrl())
                        .map(signedUrl -> Pair.of(iconSource, signedUrl)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .collectMap(pair -> pair.getLeft().getName().getLabel(), pair -> {
                    HashMap<String, Object> map = new HashMap<>();
                    IconSource iconSource = pair.getLeft();
                    map.put("url", pair.getRight());
                    map.put("width", iconSource.getWidth());
                    map.put("height", iconSource.getHeight());
                    return map;
                });
    }

}
