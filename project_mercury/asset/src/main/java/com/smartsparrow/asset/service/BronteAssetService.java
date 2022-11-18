package com.smartsparrow.asset.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.lang3.tuple.Pair;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetBuilder;
import com.smartsparrow.asset.data.IconAssetSummary;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSource;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.asset.data.AssetVisibility;
import com.smartsparrow.asset.data.AudioSource;
import com.smartsparrow.asset.data.AudioSourceName;
import com.smartsparrow.asset.data.DocumentSource;
import com.smartsparrow.asset.data.ExternalSource;
import com.smartsparrow.asset.data.IconSource;
import com.smartsparrow.asset.data.IconSourceName;
import com.smartsparrow.asset.data.IconsByLibrary;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.asset.data.ImageSourceName;
import com.smartsparrow.asset.data.VideoSource;
import com.smartsparrow.asset.data.VideoSourceName;
import com.smartsparrow.asset.data.VideoSubtitle;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.lang.UnsupportedAssetException;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.util.ImageDimensions;
import com.smartsparrow.util.Images;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class BronteAssetService implements AssetService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(BronteAssetService.class);

    private final AssetGateway assetGateway;
    private final AssetConfig assetConfig;
    private final AssetSignatureService assetSignatureService;
    private final AssetBuilder assetBuilder;
    private final ExternalAssetService externalAssetService;

    @Inject
    public BronteAssetService(AssetGateway assetGateway,
                              AssetConfig assetConfig,
                              AssetSignatureService assetSignatureService,
                              AssetBuilder assetBuilder,
                              ExternalAssetService externalAssetService) {
        this.assetGateway = assetGateway;
        this.assetConfig = assetConfig;
        this.assetSignatureService = assetSignatureService;
        this.assetBuilder = assetBuilder;
        this.externalAssetService = externalAssetService;
    }

    /**
     * Create an asset for an external source. This method does not support {@link AssetProvider#AERO} type
     *
     * @param url             the asset url
     * @param assetVisibility the asset visibility
     * @param creator         the account creating the asset
     * @param mediaType       the asset media type
     * @param metadata        metadata associated to the asset
     * @param assetProvider   the entity providing the asset
     * @return a mono with the payload of the created asset
     * @throws com.smartsparrow.exception.IllegalArgumentFault when the supplied provider is not supported
     */
    @Trace(async = true)
    public Mono<AssetPayload> create(final String url, final AssetVisibility assetVisibility,
                                     final Account creator, final AssetMediaType mediaType,
                                     final Map<String, String> metadata,
                                     final AssetProvider assetProvider) {
        affirmArgument(!assetProvider.equals(AssetProvider.AERO), "AERO provider not supported by this service");
        final UUID assetId = UUIDs.timeBased();
        AssetSummary assetSummary = new AssetSummary()
                .setUrn(new AssetUrn(assetId, assetProvider).toString())
                // set the hash as null
                .setHash(null)
                .setId(assetId)
                .setOwnerId(creator.getId())
                .setVisibility(assetVisibility)
                // the provider is always external
                .setProvider(assetProvider)
                .setSubscriptionId(creator.getSubscriptionId())
                .setMediaType(mediaType);
        Mono<Void> metadataResponse = metadata == null ? Mono.empty() : saveMetadata(assetSummary.getId(), metadata);
        // save the summary
        return assetGateway.persistExternal(assetSummary)
                .singleOrEmpty()
                // persist the external source
                .then(assetGateway.persist(new ExternalSource()
                        .setUrl(url)
                        .setAssetId(assetSummary.getId()))
                        .singleOrEmpty())
                // persist the metadata
                .then(metadataResponse)
                // then return the account payload
                .thenReturn(externalAssetService.getAssetPayload(assetSummary.getId()))
                .doOnEach(log.reactiveErrorThrowable("error fetching asset payload"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMap(one -> one);
    }

    /**
     * Save asset metadata. If the asset metadata supplied in the method argument is <code>null</code> an empty mono is
     * returned. Otherwise the metadata map is converted into a list of {@link AssetMetadata} objects and the persist
     * statement mono is returned.
     *
     * @param assetId  the asset id to save the metadata for
     * @param metadata a map of metadata
     * @return a mono of void
     */
    @Trace(async = true)
    public Mono<Void> saveMetadata(final UUID assetId, Map<String, String> metadata) {

        List<AssetMetadata> assetMetadata = metadata.entrySet().stream()
                .map(one -> new AssetMetadata()
                        .setAssetId(assetId)
                        .setKey(one.getKey())
                        .setValue(one.getValue()))
                .collect(Collectors.toList());

        return assetGateway.persist(assetMetadata.toArray(new AssetMetadata[]{}))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .singleOrEmpty();
    }

    /**
     * Saves the asset source. The db operation changes based on the {@link AssetMediaType} provided by the summary
     *
     * @param assetSummary the asset summary to build the source for
     * @param assetUrl     the asset url the asset source will refer to
     * @return a mono of an asset source
     */
    public Mono<AssetSource> saveAssetSource(final AssetSummary assetSummary, final File tmpFile, final String assetUrl, final String mimeType) {

        switch (assetSummary.getMediaType()) {
            case IMAGE:
                ImageDimensions dimensions;
                try {
                    dimensions = Images.getImageDimensions(tmpFile, mimeType);
                } catch (IOException | ImageReadException e) {
                    return Mono.error(new UnsupportedAssetException("image asset detected, but failed to parse"));
                }
                ImageSource imageSource = new ImageSource()
                        .setAssetId(assetSummary.getId())
                        .setName(ImageSourceName.ORIGINAL)
                        .setUrl(assetUrl)
                        .setHeight(dimensions.getHeight())
                        .setWidth(dimensions.getWidth());

                return assetGateway.persist(imageSource)
                        .then(Mono.just(imageSource));
            case DOCUMENT:
                final DocumentSource documentSource = new DocumentSource()
                        .setAssetId(assetSummary.getId())
                        .setUrl(assetUrl);

                return assetGateway.persist(documentSource)
                        .then(Mono.just(documentSource));
            case VIDEO:
                VideoSource videoSource = new VideoSource()
                        .setAssetId(assetSummary.getId())
                        .setName(VideoSourceName.ORIGINAL)
                        .setResolution(null)
                        .setUrl(assetUrl);

                return assetGateway.persist(videoSource)
                        .then(Mono.just(videoSource));
            case AUDIO:
                AudioSource audioSource = new AudioSource()
                        .setAssetId(assetSummary.getId())
                        .setName(AudioSourceName.ORIGINAL)
                        .setUrl(assetUrl);

                return assetGateway.persist(audioSource)
                        .then(Mono.just(audioSource));
            case ICON:
                ImageDimensions iconImageDimensions;
                try {
                    iconImageDimensions = Images.getIconImageDimensions(tmpFile, mimeType);
                } catch (IOException | ImageReadException e) {
                    return Mono.error(new UnsupportedAssetException("icon image asset detected, but failed to parse"));
                }
                IconSource iconSource = new IconSource()
                        .setAssetId(assetSummary.getId())
                        .setName(IconSourceName.ORIGINAL)
                        .setUrl(assetUrl)
                        .setHeight(iconImageDimensions.getHeight())
                        .setWidth(iconImageDimensions.getWidth());

                return assetGateway.persist(iconSource)
                        .then(Mono.just(iconSource));
            default:
                return Mono.error(new UnsupportedAssetException("invalid asset source"));
        }
    }

    /**
     * Fetch asset payload by URN
     *
     * @param urn the uniform resource name which is uniquely identifying the asset
     * @return mono with asset info, or empty mono if no asset for the urn
     * @throws AssetURNParseException if urn can not be parsed
     */
    @Override
    @SuppressWarnings("Duplicates")
    public Mono<AssetPayload> getAssetPayload(final String urn) {
        checkArgument(!Strings.isNullOrEmpty(urn), "urn is required");
        // try fetching the asset id for this urn
        return assetGateway.findAssetId(urn)
                .map(AssetIdByUrn::getAssetId)
                // get the payload
                .flatMap(this::getAssetPayload)
                // log a line if anything goes wrong, thank you
                .doOnEach(log.reactiveErrorThrowable("failed to get asset payload", throwable -> new HashMap<String, Object>(){
                    {put("assetUrn", urn);}
                }));
    }

    /**
     * Fetch asset payload by asset id
     *
     * @param assetId the asset it
     * @return mono with asset info, or empty mono if no asset for the urn
     */
    @SuppressWarnings("Duplicates")
    public Mono<AssetPayload> getAssetPayload(final UUID assetId) {
        checkArgument(assetId != null, "assetId is required");
        Mono<AssetSummary> summary = assetGateway.fetchAssetById(assetId);
        Mono<Map<String, String>> metadata = assetGateway.fetchMetadata(assetId)
                .collectMap(AssetMetadata::getKey, AssetMetadata::getValue);

        Mono<Map<String, Object>> sources = summary.flatMap(asset -> {

            switch (asset.getMediaType()) {
                case IMAGE:
                    return getImageSourcePayload(assetId);
                case VIDEO:
                    return getVideoSourcePayload(assetId);
                case DOCUMENT:
                    return getDocumentSourcePayload(assetId);
                case AUDIO:
                    return getAudioSourcePayload(assetId);
                case ICON:
                    return getIconSourcePayload(assetId);
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
    Mono<Map<String, Object>> getImageSourcePayload(final UUID assetId) {
        checkArgument(assetId != null, "assetId is required");
        return assetGateway.fetchImageSources(assetId)
                .flatMap(imageSource -> buildPublicUrl(imageSource.getUrl())
                        .map(signedUrl -> Pair.of(imageSource, signedUrl)))
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
    Mono<Map<String, Object>> getVideoSourcePayload(final UUID assetId) {
        checkArgument(assetId != null, "assetId is required");
        Mono<Map<String, Object>> sources = assetGateway.fetchVideoSources(assetId)
                .flatMap(videoSource -> buildPublicUrl(videoSource.getUrl())
                        .map(signedUrl -> Pair.of(videoSource, signedUrl)))
                .collectMap(pair -> pair.getLeft().getName().getLabel(), pair -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("url", pair.getRight());
                    map.put("resolution", pair.getLeft().getResolution());
                    return map;
                });

        Mono<Map<String, Object>> subtitles = assetGateway.fetchVideoSubtitles(assetId)
                .flatMap(videoSubtitle -> buildPublicUrl(videoSubtitle.getUrl())
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
    Mono<Map<String, Object>> getDocumentSourcePayload(final UUID assetId) {
        checkArgument(assetId != null, "assetId is required");
        return assetGateway.fetchDocumentSource(assetId)
                .flatMap(doc -> {
                    return buildPublicUrl(doc.getUrl())
                            .map(signedUrl -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("url", signedUrl);
                                return map;
                            });
                }).defaultIfEmpty(new HashMap<>());
    }

    /**
     * Return audio source for the asset as a map.
     *
     * @param assetId the asset id
     * @return a mono map of the audio source payload or an empty mono when audio source not found
     */
    Mono<Map<String, Object>> getAudioSourcePayload(final UUID assetId) {
        checkArgument(assetId != null, "assetId is required");
        return assetGateway.fetchAudioSources(assetId)
                .flatMap(audioSource -> buildPublicUrl(audioSource.getUrl())
                        .map(signedUrl -> Pair.of(audioSource, signedUrl)))
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
    Mono<Map<String, Object>> getIconSourcePayload(final UUID assetId) {
        checkArgument(assetId != null, "assetId is required");
        return assetGateway.fetchIconSources(assetId)
                .flatMap(iconSource -> buildPublicUrl(iconSource.getUrl())
                        .map(signedUrl -> Pair.of(iconSource, signedUrl)))
                .collectMap(pair -> pair.getLeft().getName().getLabel(), pair -> {
                    HashMap<String, Object> map = new HashMap<>();
                    IconSource iconSource = pair.getLeft();
                    map.put("url", pair.getRight());
                    map.put("width", iconSource.getWidth());
                    map.put("height", iconSource.getHeight());
                    return map;
                });
    }

    /**
     * Build and sign the public url when required.
     *
     * @param relativeUrl asset source url
     * @return the full url to asset source
     */
    @Trace(async = true)
    public Mono<String> buildPublicUrl(final String relativeUrl) {
        final String fullUrl = String.format("%s/%s", assetConfig.getPublicUrl(), relativeUrl);
        return assetSignatureService.signUrl(fullUrl)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch asset summary by asset id
     *
     * @param assetId the asset it
     * @return mono with asset summary
     */
    @Trace(async = true)
    public Mono<AssetSummary> getAssetSummary(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");
        return assetGateway.fetchAssetById(assetId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch asset metadata by asset id
     *
     * @param assetId the asset it
     * @return flux with asset metadata
     */
    public Flux<AssetMetadata> getAssetMetadata(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");
        return assetGateway.fetchMetadata(assetId);
    }

    /**
     * Fetch asset audio source by asset id
     *
     * @param assetId the asset it
     * @return flux with audio source
     */
    public Flux<AudioSource> getAudioSource(final UUID assetId) {
        return assetGateway.fetchAudioSources(assetId);
    }

    /**
     * Fetch asset document source by asset id
     *
     * @param assetId the asset it
     * @return mono with document source
     */
    public Mono<DocumentSource> getDocumentSource(final UUID assetId) {
        return assetGateway.fetchDocumentSource(assetId);
    }

    /**
     * Fetch asset external source by asset id
     *
     * @param assetId the asset it
     * @return flux with external source
     */
    public Mono<ExternalSource> getExternalSourceRecord(final UUID assetId) {
        return assetGateway.fetchExternalSource(assetId);
    }


    /**
     * Fetch asset image source by asset id
     *
     * @param assetId the asset it
     * @return flux with image source
     */
    public Flux<ImageSource> getImageSource(final UUID assetId) {
        return assetGateway.fetchImageSources(assetId);
    }

    /**
     * Fetch asset video source by asset id
     *
     * @param assetId the asset it
     * @return flux with video source
     */
    public Flux<VideoSource> getVideoSource(final UUID assetId) {
        return assetGateway.fetchVideoSources(assetId);
    }

    /**
     * Fetch asset video subtitle by asset id
     *
     * @param assetId the asset it
     * @return flux with video subtitle
     */
    public Flux<VideoSubtitle> getVideoSubtitle(final UUID assetId) {
        return assetGateway.fetchVideoSubtitles(assetId);
    }

    /**
     * Find the last associated asset id to this asset urn
     *
     * @param assetUrn the urn to find the asset id for
     * @return a mono containing the AssetIdByUrn object
     */
    @Trace(async = true)
    public Mono<AssetIdByUrn> findAssetId(final String assetUrn) {
        affirmArgumentNotNullOrEmpty(assetUrn, "assetUrn is required");
        return assetGateway.findAssetId(assetUrn)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch asset icon source by asset id
     *
     * @param assetId the asset it
     * @return flux with icon source
     */
    public Flux<IconSource> getIconSource(final UUID assetId) {
        return assetGateway.fetchIconSources(assetId);
    }

    /**
     * Fetch icon assets by icon libraries
     *
     * @param iconLibraries the list of icon libraries
     * @return flux of icon asset info by library
     */
    @Trace(async = true)
    public Flux<IconAssetSummary> fetchIconAssetsByLibrary(final List<String> iconLibraries) {
        return iconLibraries.stream()
                .map(iconLibrary ->
                             assetGateway.fetchAssetsByIconLibrary(iconLibrary)
                                     .flatMap(this::getIconLibraryWithMetadata)
                                     .doOnEach(ReactiveTransaction.linkOnNext()))
                .reduce(Flux::mergeWith)
                .orElse(Flux.empty());
    }

    /**
     *  Duplicate an asset summary, resource, and metadata. Copy of the selected asset with new asset id, urn, owner id,
     *  and subscription id. Only change asset provider to AERO if selected asset provider is ALFRESCO
     *
     * @param oldAssetId        the original asset id
     * @param ownerId           the account id duplicating the asset
     * @param subscriptionId    the account subscription id
     * @return a mono of asset summary
     */
    public Mono<AssetSummary> duplicate(final UUID oldAssetId, final UUID ownerId, final UUID subscriptionId) {
        affirmArgument(oldAssetId != null, "assetId is required");
        affirmArgument(ownerId != null, "ownerId is required");
        affirmArgument(subscriptionId != null, "subscriptionId is required");

        return getAssetSummary(oldAssetId)
                .switchIfEmpty(Mono.error(
                        new NotFoundFault(String.format("cannot find asset summary by asset id: %s", oldAssetId))))
                .map(assetSummary -> {
                    // create a new asset id
                    final UUID newAssetId = UUIDs.timeBased();

                    // change new asset provider to AERO if original asset provider is ALFRESCO
                    AssetProvider assetProvider = assetSummary.getProvider() == AssetProvider.ALFRESCO ?
                            AssetProvider.AERO : assetSummary.getProvider();

                    // duplicate original asset with new asset id, owner id, and subscription id
                    return new AssetSummary()
                            .setId(newAssetId)
                            .setUrn(new AssetUrn(newAssetId, assetProvider).toString())
                            .setHash(assetSummary.getHash())
                            .setOwnerId(ownerId)
                            .setVisibility(assetSummary.getVisibility())
                            .setProvider(assetProvider)
                            .setSubscriptionId(subscriptionId)
                            .setMediaType(assetSummary.getMediaType());
                })
                .flatMap(newAssetSummary -> {
                    if (newAssetSummary.getProvider() == AssetProvider.EXTERNAL) {
                        return assetGateway.persistExternal(newAssetSummary)
                                .doOnEach(log.reactiveInfoSignal("Bronte asset service: duplicated external asset summary saved"))
                                .thenMany(duplicateExternalSource(oldAssetId, newAssetSummary.getId()))
                                .doOnEach(log.reactiveInfoSignal("Bronte asset service: duplicated external asset source saved"))
                                .thenMany(duplicateAssetMetadata(oldAssetId, newAssetSummary.getId()))
                                .doOnEach(log.reactiveInfoSignal("Bronte asset service: duplicated external asset metadata saved"))
                                .singleOrEmpty()
                                .thenReturn(newAssetSummary);
                    }

                    return assetGateway.persist(newAssetSummary)
                            .doOnEach(log.reactiveInfoSignal("Bronte asset service: duplicated asset summary saved"))
                            .thenMany(duplicateAssetSource(newAssetSummary.getMediaType(), oldAssetId, newAssetSummary.getId()))
                            .doOnEach(log.reactiveInfoSignal("Bronte asset service: duplicated asset source saved"))
                            .thenMany(duplicateAssetMetadata(oldAssetId, newAssetSummary.getId()))
                            .doOnEach(log.reactiveInfoSignal("Bronte asset service: duplicated asset metadata saved"))
                            .singleOrEmpty()
                            .thenReturn(newAssetSummary);
                });
    }

    /**
     * Duplicate asset resources. Copy of the selected asset resource with new asset id
     *
     * @param mediaType     the asset media type
     * @param oldAssetId    the original asset id
     * @param newAssetId    the new asset id
     * @return a flux void to add to the reactive chain
     */
    private Flux<Void> duplicateAssetSource(final AssetMediaType mediaType, final UUID oldAssetId, final UUID newAssetId) {
        switch (mediaType) {
            case IMAGE:
                return getImageSource(oldAssetId)
                        .switchIfEmpty(Flux.error(new NotFoundFault(String.format("cannot find image source by asset id: %s", oldAssetId))))
                        .map(originalImageSource -> new ImageSource()
                                .setAssetId(newAssetId)
                                .setName(originalImageSource.getName())
                                .setUrl(originalImageSource.getUrl())
                                .setHeight(originalImageSource.getHeight())
                                .setWidth(originalImageSource.getWidth()))
                        .flatMap(assetGateway::persist);
            case DOCUMENT:
                return getDocumentSource(oldAssetId)
                        .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find document source by asset id: %s", oldAssetId))))
                        .map(originalDocumentSource -> new DocumentSource()
                                .setAssetId(newAssetId)
                                .setUrl(originalDocumentSource.getUrl())
                        )
                        .flatMapMany(newDocumentSource -> assetGateway.persist(newDocumentSource));
            case VIDEO:
                return getVideoSource(oldAssetId)
                        .switchIfEmpty(Flux.error(new NotFoundFault(String.format("cannot find video source by asset id: %s", oldAssetId))))
                        .map(originalVideoSource -> new VideoSource()
                                .setAssetId(newAssetId)
                                .setName(originalVideoSource.getName())
                                .setResolution(originalVideoSource.getResolution())
                                .setUrl(originalVideoSource.getUrl()))
                        .flatMap(assetGateway::persist);
            case AUDIO:
                return getAudioSource(oldAssetId)
                        .switchIfEmpty(Flux.error(new NotFoundFault(String.format("cannot find audio source by asset id: %s", oldAssetId))))
                        .map(originalAudioSource -> new AudioSource()
                                .setAssetId(newAssetId)
                                .setName(originalAudioSource.getName())
                                .setUrl(originalAudioSource.getUrl()))
                        .flatMap(assetGateway::persist);
            case ICON:
                return getIconSource(oldAssetId)
                        .switchIfEmpty(Flux.error(new NotFoundFault(String.format("cannot find icon source by asset id: %s", oldAssetId))))
                        .map(originalIcon -> new IconSource()
                                .setAssetId(newAssetId)
                                .setName(originalIcon.getName())
                                .setUrl(originalIcon.getUrl())
                                .setHeight(originalIcon.getHeight())
                                .setWidth(originalIcon.getWidth()))
                        .flatMap(assetGateway::persist);
            default:
                return Flux.error(new UnsupportedAssetException("invalid asset source"));
        }
    }

    /**
     * Duplicate external sources. Copy of the selected asset external resource with new asset id
     *
     * @param oldAssetId    the original asset id
     * @param newAssetId    the new asset id
     * @return a flux void to add to the reactive chain
     */
    private Flux<Void> duplicateExternalSource(final UUID oldAssetId, final UUID newAssetId) {
        return getExternalSourceRecord(oldAssetId)
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find external source by asset id: %s", oldAssetId))))
                .map(originalExternalSource -> new ExternalSource()
                        .setAssetId(newAssetId)
                        .setUrl(originalExternalSource.getUrl()))
                .flatMapMany(assetGateway::persist);
    }

    /**
     * Duplicate asset metadata. Copy of the selected asset metadata with new asset id
     * Note: it will remove alfresco specific data like alfresco path but not asset specific data
     * like alt text, long description etc
     *
     * @param oldAssetId    the original asset id
     * @param newAssetId    the new asset id
     * @return a flux void to add to the reactive chain
     */
    private Flux<Void> duplicateAssetMetadata(final UUID oldAssetId, final UUID newAssetId) {
        return getAssetMetadata(oldAssetId)
                .filter(assetMetadata -> !assetMetadata.getKey().startsWith("alfresco")) // remove alfresco specific data
                .map(originalAssetMetadata -> new AssetMetadata()
                        .setAssetId(newAssetId)
                        .setKey(originalAssetMetadata.getKey())
                        .setValue(originalAssetMetadata.getValue()))
                .flatMap(assetGateway::persist);
    }

    /**
     * Get icon asset info
     * @param iconsByLibrary the icons by library
     * @return mono of icon asset info by library
     */
    private Mono<IconAssetSummary> getIconLibraryWithMetadata(final IconsByLibrary iconsByLibrary) {
        return findAssetId(iconsByLibrary.getAssetUrn())
                .flatMap(assetIdByUrn -> assetGateway.fetchMetadata(assetIdByUrn.getAssetId())
                        .doOnEach(ReactiveTransaction.linkOnNext())
                        .collectList()
                        .defaultIfEmpty(Collections.emptyList())
                        .flatMap(assetMetadata -> Mono.just(new IconAssetSummary()
                                                                    .setIconLibrary(iconsByLibrary.getIconLibrary())
                                                                    .setAssetUrn(iconsByLibrary.getAssetUrn())
                                                                    .setMetadata(assetMetadata)))
                );
    }

    /**
     * Delete icon assets by library
     * @param iconLibrary the icon library
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> deleteIconAssetsByLibrary(final String iconLibrary) {
        affirmArgumentNotNullOrEmpty(iconLibrary, "iconLibrary is required");

        return assetGateway.deleteIconAssets(new IconsByLibrary()
                                                     .setIconLibrary(iconLibrary))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
