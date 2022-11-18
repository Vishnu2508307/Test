package com.smartsparrow.asset.data;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class AssetGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AssetGateway.class);

    private final Session session;

    private final AssetSummaryMaterializer assetSummaryMaterializer;
    private final AssetSummaryMutator assetSummaryMutator;
    private final AssetByHashMaterializer assetByHashMaterializer;
    private final AssetByHashMutator assetByHashMutator;
    private final ImageSourceByAssetMaterializer imageSourceByAssetMaterializer;
    private final ImageSourceByAssetMutator imageSourceByAssetMutator;
    private final DocumentSourceByAssetMaterializer documentSourceByAssetMaterializer;
    private final DocumentSourceByAssetMutator documentSourceByAssetMutator;
    private final VideoSourceByAssetMaterializer videoSourceByAssetMaterializer;
    private final VideoSourceByAssetMutator videoSourceByAssetMutator;
    private final VideoSubtitleByAssetMaterializer videoSubtitleByAssetMaterializer;
    private final AudioSourceByAssetMaterializer audioSourceByAssetMaterializer;
    private final AudioSourceByAssetMutator audioSourceByAssetMutator;
    private final ExternalSourceByAssetMutator externalSourceByAssetMutator;
    private final ExternalSourceByAssetMaterializer externalSourceByAssetMaterializer;
    private final ErrorNotificationByAssetMutator errorNotificationByAssetMutator;
    private final ErrorNotificationByAssetMaterializer errorNotificationByAssetMaterializer;
    private final AssetRetryNotificationMutator assetRetryNotificationMutator;
    private final ResultNotificationByAssetMutator resultNotificationByAssetMutator;
    private final RequestNotificationByAssetMutator requestNotificationByAssetMutator;
    private final AlfrescoAssetDataMutator alfrescoAssetDataMutator;
    private final AlfrescoAssetDataMaterializer alfrescoAssetDataMaterializer;
    private final AssetMetadataMaterializer assetMetadataMaterializer;
    private final AssetMetadataMutator assetMetadataMutator;
    private final AssetIdByUrnMaterializer assetIdByUrnMaterializer;
    private final AssetIdByUrnMutator assetIdByUrnMutator;
    private final AssetByIconLibraryMutator assetByIconLibraryMutator;
    private final AssetByIconLibraryMaterializer assetByIconLibraryMaterializer;
    private final IconSourceByAssetMutator iconSourceByAssetMutator;
    private final IconSourceByAssetMaterializer iconSourceByAssetMaterializer;

    @Inject
    public AssetGateway(Session session,
                        AssetSummaryMaterializer assetSummaryMaterializer,
                        AssetSummaryMutator assetSummaryMutator,
                        AssetByHashMaterializer assetByHashMaterializer,
                        AssetByHashMutator assetByHashMutator,
                        ImageSourceByAssetMaterializer imageSourceByAssetMaterializer,
                        ImageSourceByAssetMutator imageSourceByAssetMutator,
                        DocumentSourceByAssetMaterializer documentSourceByAssetMaterializer,
                        DocumentSourceByAssetMutator documentSourceByAssetMutator,
                        VideoSourceByAssetMaterializer videoSourceByAssetMaterializer,
                        VideoSourceByAssetMutator videoSourceByAssetMutator,
                        VideoSubtitleByAssetMaterializer videoSubtitleByAssetMaterializer,
                        AudioSourceByAssetMaterializer audioSourceByAssetMaterializer,
                        AudioSourceByAssetMutator audioSourceByAssetMutator,
                        ExternalSourceByAssetMutator externalSourceByAssetMutator,
                        ExternalSourceByAssetMaterializer externalSourceByAssetMaterializer,
                        ErrorNotificationByAssetMutator errorNotificationByAssetMutator,
                        ErrorNotificationByAssetMaterializer errorNotificationByAssetMaterializer,
                        AssetRetryNotificationMutator assetRetryNotificationMutator,
                        ResultNotificationByAssetMutator resultNotificationByAssetMutator,
                        RequestNotificationByAssetMutator requestNotificationByAssetMutator,
                        AlfrescoAssetDataMutator alfrescoAssetDataMutator,
                        AlfrescoAssetDataMaterializer alfrescoAssetDataMaterializer,
                        AssetMetadataMaterializer assetMetadataMaterializer,
                        AssetMetadataMutator assetMetadataMutator,
                        AssetIdByUrnMaterializer assetIdByUrnMaterializer,
                        AssetIdByUrnMutator assetIdByUrnMutator,
                        final AssetByIconLibraryMutator assetByIconLibraryMutator,
                        final AssetByIconLibraryMaterializer assetByIconLibraryMaterializer,
                        final IconSourceByAssetMutator iconSourceByAssetMutator,
                        final IconSourceByAssetMaterializer iconSourceByAssetMaterializer) {
        this.session = session;
        this.assetSummaryMaterializer = assetSummaryMaterializer;
        this.assetSummaryMutator = assetSummaryMutator;
        this.assetByHashMaterializer = assetByHashMaterializer;
        this.assetByHashMutator = assetByHashMutator;
        this.imageSourceByAssetMaterializer = imageSourceByAssetMaterializer;
        this.imageSourceByAssetMutator = imageSourceByAssetMutator;
        this.documentSourceByAssetMaterializer = documentSourceByAssetMaterializer;
        this.documentSourceByAssetMutator = documentSourceByAssetMutator;
        this.videoSourceByAssetMaterializer = videoSourceByAssetMaterializer;
        this.videoSourceByAssetMutator = videoSourceByAssetMutator;
        this.videoSubtitleByAssetMaterializer = videoSubtitleByAssetMaterializer;
        this.audioSourceByAssetMaterializer = audioSourceByAssetMaterializer;
        this.audioSourceByAssetMutator = audioSourceByAssetMutator;
        this.externalSourceByAssetMutator = externalSourceByAssetMutator;
        this.externalSourceByAssetMaterializer = externalSourceByAssetMaterializer;
        this.errorNotificationByAssetMutator = errorNotificationByAssetMutator;
        this.errorNotificationByAssetMaterializer = errorNotificationByAssetMaterializer;
        this.assetRetryNotificationMutator = assetRetryNotificationMutator;
        this.resultNotificationByAssetMutator = resultNotificationByAssetMutator;
        this.requestNotificationByAssetMutator = requestNotificationByAssetMutator;
        this.alfrescoAssetDataMutator = alfrescoAssetDataMutator;
        this.alfrescoAssetDataMaterializer = alfrescoAssetDataMaterializer;
        this.assetMetadataMaterializer = assetMetadataMaterializer;
        this.assetMetadataMutator = assetMetadataMutator;
        this.assetIdByUrnMaterializer = assetIdByUrnMaterializer;
        this.assetIdByUrnMutator = assetIdByUrnMutator;
        this.assetByIconLibraryMutator = assetByIconLibraryMutator;
        this.assetByIconLibraryMaterializer = assetByIconLibraryMaterializer;
        this.iconSourceByAssetMutator = iconSourceByAssetMutator;
        this.iconSourceByAssetMaterializer = iconSourceByAssetMaterializer;
    }

    /**
     * Persist asset into summary table and all related filtering tables (asset_by_hash)
     *
     * @param assetSummary the asset summary to persist
     */
    public Flux<Void> persist(final AssetSummary assetSummary) {
        AssetByHash assetByHash = new AssetByHash()
                .setAssetId(assetSummary.getId())
                .setHash(assetSummary.getHash())
                .setProvider(assetSummary.getProvider())
                .setSubscriptionId(assetSummary.getSubscriptionId())
                .setOwnerId(assetSummary.getOwnerId());

        AssetUrn assetUrn = new AssetUrn(assetSummary.getId(), assetSummary.getProvider());

        Flux<? extends Statement> iter = Flux.just(
                assetSummaryMutator.upsert(assetSummary),
                assetByHashMutator.upsert(assetByHash),
                assetIdByUrnMutator.persist(assetUrn.toString(), assetSummary.getId()));
        return Mutators.execute(session, iter);
    }

    /**
     * Persist asset into summary table and all related filtering tables
     * <br>This method is to be used only when the asset provider is of type
     * {@link AssetProvider#EXTERNAL} as the hash value is expected to be empty
     *
     * @param assetSummary the asset summary to persist
     */
    @Trace(async = true)
    public Flux<Void> persistExternal(final AssetSummary assetSummary) {
        AssetUrn assetUrn = new AssetUrn(assetSummary.getId(), assetSummary.getProvider());

        Flux<? extends Statement> iter = Flux.just(
                assetSummaryMutator.upsert(assetSummary),
                assetIdByUrnMutator.persist(assetUrn.toString(), assetSummary.getId()))
                .doOnEach(ReactiveTransaction.linkOnNext());
        return Mutators.execute(session, iter);
    }

    /**
     * Persist asset metadata objects
     *
     * @param assetMetadata asset metadata objects to persist
     */
    @Trace(async = true)
    public Flux<Void> persist(final AssetMetadata... assetMetadata) {
        Flux<? extends Statement> iter = Mutators.upsert(assetMetadataMutator, assetMetadata)
                .doOnEach(ReactiveTransaction.linkOnNext());
        return Mutators.execute(session, iter);
    }

    /**
     * Persist image sources
     *
     * @param imageSources image sources to persist
     */
    public Flux<Void> persist(final ImageSource... imageSources) {
        Flux<? extends Statement> iter = Mutators.upsert(imageSourceByAssetMutator, imageSources);
        return Mutators.execute(session, iter);
    }

    /**
     * Persist document source
     *
     * @param documentSource document source to persist
     */
    public Flux<Void> persist(final DocumentSource documentSource) {
        Flux<? extends Statement> iter = Mutators.upsert(documentSourceByAssetMutator, documentSource);
        return Mutators.execute(session, iter);
    }

    /**
     * Persist video sources
     *
     * @param videoSources video sources to persist
     */
    public Flux<Void> persist(final VideoSource... videoSources) {
        Flux<? extends Statement> iter = Mutators.upsert(videoSourceByAssetMutator, videoSources);
        return Mutators.execute(session, iter);
    }

    /**
     * Persist audio sources
     *
     * @param audioSources audio sources to persist
     */
    public Flux<Void> persist(final AudioSource... audioSources) {
        Flux<? extends Statement> iter = Mutators.upsert(audioSourceByAssetMutator, audioSources);
        return Mutators.execute(session, iter);
    }

    /**
     * Persist external sources
     *
     * @param externalSources external sources to persist
     */
    @Trace(async = true)
    public Flux<Void> persist(final ExternalSource... externalSources) {
        Flux<? extends Statement> iter = Mutators.upsert(externalSourceByAssetMutator, externalSources)
                .doOnEach(ReactiveTransaction.linkOnNext());
        return Mutators.execute(session, iter);
    }

    /**
     * Fetch an asset summary by id
     *
     * @param id the asset id
     * @return mono with AssetSummary, empty mono if asset is not found
     */
    @Trace(async = true)
    public Mono<AssetSummary> fetchAssetById(final UUID id) {
        return ResultSets.query(session, assetSummaryMaterializer.fetchAssetBy(id))
                .flatMapIterable(row -> row)
                .map(assetSummaryMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch asset info by hash
     *
     * @param hash the hash
     * @return mono with AssetByHash, empty mono if hash is not found
     */
    public Mono<AssetByHash> fetchAssetByHash(final String hash) {
        return ResultSets.query(session, assetByHashMaterializer.findByHash(hash))
                .flatMapIterable(row -> row)
                .map(assetByHashMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Fetch all metadata for asset
     *
     * @param assetId the asset id
     * @return all AssetMetadata objects for the asset, empty flux if no metadata for the asset
     */
    @Trace(async = true)
    public Flux<AssetMetadata> fetchMetadata(final UUID assetId) {
        return ResultSets.query(session, assetMetadataMaterializer.fetchAllBy(assetId))
                .flatMapIterable(row -> row)
                .map(assetMetadataMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all image sources for asset
     *
     * @param assetId the asset id
     * @return flux with ImageSource, empty flux if no image sources for the asset
     */
    public Flux<ImageSource> fetchImageSources(final UUID assetId) {
        return ResultSets.query(session, imageSourceByAssetMaterializer.findAllBy(assetId))
                .flatMapIterable(row -> row)
                .map(imageSourceByAssetMaterializer::fromRow);
    }

    /**
     * Fetch document source for asset
     *
     * @param assetId the asset id
     * @return flux with DocumentSource, empty flux if no document sources for the asset
     */
    public Mono<DocumentSource> fetchDocumentSource(final UUID assetId) {
        return ResultSets.query(session, documentSourceByAssetMaterializer.findBy(assetId))
                .flatMapIterable(row -> row)
                .map(documentSourceByAssetMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Fetch all video sources for asset
     *
     * @param assetId the asset id
     * @return flux with VideoSource, empty flux if no video sources for the asset
     */
    public Flux<VideoSource> fetchVideoSources(final UUID assetId) {
        return ResultSets.query(session, videoSourceByAssetMaterializer.fetchAllBy(assetId))
                .flatMapIterable(row -> row)
                .map(videoSourceByAssetMaterializer::fromRow);
    }

    /**
     * Fetch all video subtitles for asset
     *
     * @param assetId th asset id
     * @return flux with VideoSubtitle, empty flux if no subtitles found
     */
    public Flux<VideoSubtitle> fetchVideoSubtitles(final UUID assetId) {
        return ResultSets.query(session, videoSubtitleByAssetMaterializer.fetchBy(assetId))
                .flatMapIterable(row -> row)
                .map(videoSubtitleByAssetMaterializer::fromRow);
    }

    /**
     * Fetch all audio sources for asset
     *
     * @param assetId the asset id
     * @return flux with AudioSource, empty flux if no audio sources for the asset
     */
    public Flux<AudioSource> fetchAudioSources(final UUID assetId) {
        return ResultSets.query(session, audioSourceByAssetMaterializer.findAll(assetId))
                .flatMapIterable(row -> row)
                .map(audioSourceByAssetMaterializer::fromRow);
    }

    /**
     * Fetch all audio sources for asset
     *
     * @param assetId the asset id
     * @return flux with AudioSource, empty flux if no audio sources for the asset
     */
    public Mono<ExternalSource> fetchExternalSource(final UUID assetId) {
        return ResultSets.query(session, externalSourceByAssetMaterializer.findBy(assetId))
                .flatMapIterable(row -> row)
                .singleOrEmpty()
                .map(externalSourceByAssetMaterializer::fromRow);
    }

    /**
     * Save the courseware asset resize notification error log
     *
     * @param errorNotification courseware export error
     */
    public Flux<Void> persist(final AssetErrorNotification errorNotification) {
        return Mutators.execute(session, Flux.just(
                errorNotificationByAssetMutator.upsert(errorNotification)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving courseware asset error log %s",
                    errorNotification), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * fetch the courseware export error log
     *
     * @param notificationId the notification id.
     * @return flux of export error log
     */
    public Flux<AssetErrorNotification> fetchAssetErrorLog(final UUID notificationId) {
        return ResultSets.query(session, errorNotificationByAssetMaterializer.findById(notificationId))
                .flatMapIterable(row -> row)
                .map(errorNotificationByAssetMaterializer::fromRow);
    }


    /**
     * Save the courseware asset resize notification retry log
     *
     * @param errorNotification courseware resize error
     */
    public Flux<Void> persist(final AssetRetryNotification errorNotification) {
        return Mutators.execute(session, Flux.just(
                assetRetryNotificationMutator.upsert(errorNotification)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving courseware asset retry log %s",
                    errorNotification), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Save the courseware asset resize notification result log
     *
     * @param notification courseware resize result
     */
    public Flux<Void> persist(final AssetResultNotification notification) {
        return Mutators.execute(session, Flux.just(
                resultNotificationByAssetMutator.upsert(notification)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving courseware asset result log %s",
                    notification), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Save the courseware asset resize notification request log
     *
     * @param notification courseware resize result
     */
    public Flux<Void> persist(final AssetRequestNotification notification) {
        return Mutators.execute(session, Flux.just(
                requestNotificationByAssetMutator.upsert(notification)
        )).doOnError(throwable -> {
            log.error(String.format("error while saving courseware asset request log %s",
                    notification), throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Fetch an Alfresco asset by id
     *
     * @param assetId the asset id
     * @return mono with AlfrescoAsset, empty mono if asset is not found
     */
    public Mono<AlfrescoAssetData> fetchAlfrescoAssetById(final UUID assetId) {
        return ResultSets.query(session, alfrescoAssetDataMaterializer.fetchBy(assetId))
                .flatMapIterable(row -> row)
                .map(alfrescoAssetDataMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Persist Alfresco asset into alfresco asset and asset by alfresco tables
     *
     * @param alfrescoAssetData Alfresco asset objects to persist
     */
    public Flux<Void> persist(final AlfrescoAssetData alfrescoAssetData) {

        Flux<? extends Statement> iter = Flux.just(
                alfrescoAssetDataMutator.upsert(alfrescoAssetData));

        return Mutators.execute(session, iter);
    }

    /**
     * Find the latest assetId that was associated to this assetUrn
     *
     * @param assetUrn the asset urn to find the asset id for
     * @return a mono containing an assetIdByUrn object
     */
    @Trace(async = true)
    public Mono<AssetIdByUrn> findAssetId(final String assetUrn) {
        return ResultSets.query(session, assetIdByUrnMaterializer.findAssetId(assetUrn))
                .flatMapIterable(row -> row)
                .map(assetIdByUrnMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist IconsByLibrary into asset by icon library table
     *
     * @param assetByIconLibrary IconsByLibrary object to persist
     */
    @Trace(async = true)
    public Flux<Void> persist(final IconsByLibrary assetByIconLibrary) {

        Flux<? extends Statement> iter = Flux.just(
                assetByIconLibraryMutator.upsert(assetByIconLibrary))
                .doOnEach(ReactiveTransaction.linkOnNext());

        return Mutators.execute(session, iter);
    }

    /**
     * Find all the assets with asset urn and metadata by icon library name
     *
     * @param iconLibrary the icon library name
     * @return a flux containing assetByIconLibrary objects
     */
    @Trace(async = true)
    public Flux<IconsByLibrary> fetchAssetsByIconLibrary(final String iconLibrary) {
        return ResultSets.query(session, assetByIconLibraryMaterializer.findByIconLibrary(iconLibrary))
                .flatMapIterable(row -> row)
                .map(assetByIconLibraryMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist icon image sources
     *
     * @param iconSource icon image sources to persist
     */
    public Flux<Void> persist(final IconSource iconSource) {
        Flux<? extends Statement> iter = Mutators.upsert(iconSourceByAssetMutator, iconSource);
        return Mutators.execute(session, iter);
    }

    /**
     * Fetch all icon image sources for an asset
     *
     * @param assetId the asset id
     * @return flux with ImageSource, empty flux if no image sources for the asset
     */
    public Flux<IconSource> fetchIconSources(final UUID assetId) {
        return ResultSets.query(session, iconSourceByAssetMaterializer.findAllBy(assetId))
                .flatMapIterable(row -> row)
                .map(iconSourceByAssetMaterializer::fromRow);
    }

    /**
     * Delete icon assets by library
     *
     * @param iconsByLibrary the icon library object
     */
    @Trace(async = true)
    public Flux<Void> deleteIconAssets(final IconsByLibrary iconsByLibrary) {
        Flux<? extends Statement> stmt = Flux.just(
                assetByIconLibraryMutator.delete(iconsByLibrary));

        return Mutators.execute(session, stmt)
                .doOnEach(log.reactiveErrorThrowable("error while deleting asset icons by library",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("iconLibrary", iconsByLibrary.getIconLibrary());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
