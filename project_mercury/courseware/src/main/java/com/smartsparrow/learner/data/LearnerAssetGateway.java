package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.AudioSource;
import com.smartsparrow.asset.data.DocumentSource;
import com.smartsparrow.asset.data.ExternalSource;
import com.smartsparrow.asset.data.IconSource;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.asset.data.VideoSource;
import com.smartsparrow.asset.data.VideoSubtitle;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerAssetGateway {

    private final Session session;

    private final LearnerAssetByCoursewareMaterializer learnerAssetByCoursewareMaterializer;
    private final LearnerAssetByCoursewareMutator learnerAssetByCoursewareMutator;
    private final LearnerCoursewareByAssetMaterializer learnerCoursewareByAssetMaterializer;
    private final LearnerCoursewareByAssetMutator learnerCoursewareByAssetMutator;
    private final AssetByDeploymentMaterializer assetByDeploymentMaterializer;
    private final AssetByDeploymentMutator assetByDeploymentMutator;
    private final DeploymentByAssetMaterializer deploymentByAssetMaterializer;
    private final DeploymentByAssetMutator deploymentByAssetMutator;
    private final AssetSummaryMaterializer assetSummaryMaterializer;
    private final AssetSummaryMutator assetSummaryMutator;
    private final AudioSourceByAssetMaterializer audioSourceByAssetMaterializer;
    private final AudioSourceByAssetMutator audioSourceByAssetMutator;
    private final DocumentSourceByAssetMaterializer documentSourceByAssetMaterializer;
    private final DocumentSourceByAssetMutator documentSourceByAssetMutator;
    private final ExternalSourceByAssetMaterializer externalSourceByAssetMaterializer;
    private final ExternalSourceByAssetMutator externalSourceByAssetMutator;
    private final ImageSourceByAssetMaterializer imageSourceByAssetMaterializer;
    private final ImageSourceByAssetMutator imageSourceByAssetMutator;
    private final VideoSourceByAssetMaterializer videoSourceByAssetMaterializer;
    private final VideoSourceByAssetMutator videoSourceByAssetMutator;
    private final VideoSubtitleByAssetMaterializer videoSubtitleByAssetMaterializer;
    private final VideoSubtitleByAssetMutator videoSubtitleByAssetMutator;
    private final AssetMetadataMaterializer assetMetadataMaterializer;
    private final AssetMetadataMutator assetMetadataMutator;
    private final LearnerAssetUrnByCoursewareMaterializer learnerAssetUrnByCoursewareMaterializer;
    private final LearnerAssetUrnByCoursewareMutator learnerAssetUrnByCoursewareMutator;
    private final LearnerCoursewareByAssetUrnMaterializer learnerCoursewareByAssetUrnMaterializer;
    private final LearnerCoursewareByAssetUrnMutator learnerCoursewareByAssetUrnMutator;
    private final LearnerAssetUrnByDeploymentMaterializer learnerAssetUrnByDeploymentMaterializer;
    private final LearnerAssetUrnByDeploymentMutator learnerAssetUrnByDeploymentMutator;
    private final LearnerAssetIdByUrnMaterializer learnerAssetIdByUrnMaterializer;
    private final LearnerAssetIdByUrnMutator learnerAssetIdByUrnMutator;
    private final DeploymentByAssetUrnMaterializer deploymentByAssetUrnMaterializer;
    private final DeploymentByAssetUrnMutator deploymentByAssetUrnMutator;
    private final LearnerIconSourceByAssetMutator learnerIconSourceByAssetMutator;
    private final LearnerIconSourceByAssetMaterializer learnerIconSourceByAssetMaterializer;
    private final LearnerMathAssetUrnByElementMutator learnerMathAssetUrnByElementMutator;
    private final LearnerMathAssetUrnByElementMaterializer learnerMathAssetUrnByElementMaterializer;
    private final LearnerMathAssetIdByUrnMutator learnerMathAssetIdByUrnMutator;
    private final LearnerMathAssetIdByUrnMaterializer learnerMathAssetIdByUrnMaterializer;
    private final LearnerAssetSummaryMaterializer learnerAssetSummaryMaterializer;
    private final LearnerAssetSummaryMutator learnerAssetSummaryMutator;

    @Inject
    public LearnerAssetGateway(Session session,
                               LearnerAssetByCoursewareMaterializer learnerAssetByCoursewareMaterializer,
                               LearnerAssetByCoursewareMutator learnerAssetByCoursewareMutator,
                               LearnerCoursewareByAssetMaterializer learnerCoursewareByAssetMaterializer,
                               LearnerCoursewareByAssetMutator learnerCoursewareByAssetMutator,
                               AssetByDeploymentMaterializer assetByDeploymentMaterializer,
                               AssetByDeploymentMutator assetByDeploymentMutator,
                               DeploymentByAssetMaterializer deploymentByAssetMaterializer,
                               DeploymentByAssetMutator deploymentByAssetMutator,
                               AssetSummaryMaterializer assetSummaryMaterializer,
                               AssetSummaryMutator assetSummaryMutator,
                               AudioSourceByAssetMaterializer audioSourceByAssetMaterializer,
                               AudioSourceByAssetMutator audioSourceByAssetMutator,
                               DocumentSourceByAssetMaterializer documentSourceByAssetMaterializer,
                               DocumentSourceByAssetMutator documentSourceByAssetMutator,
                               ExternalSourceByAssetMaterializer externalSourceByAssetMaterializer,
                               ExternalSourceByAssetMutator externalSourceByAssetMutator,
                               ImageSourceByAssetMaterializer imageSourceByAssetMaterializer,
                               ImageSourceByAssetMutator imageSourceByAssetMutator,
                               VideoSourceByAssetMaterializer videoSourceByAssetMaterializer,
                               VideoSourceByAssetMutator videoSourceByAssetMutator,
                               VideoSubtitleByAssetMaterializer videoSubtitleByAssetMaterializer,
                               VideoSubtitleByAssetMutator videoSubtitleByAssetMutator,
                               AssetMetadataMaterializer assetMetadataMaterializer,
                               AssetMetadataMutator assetMetadataMutator,
                               LearnerAssetUrnByCoursewareMaterializer learnerAssetUrnByCoursewareMaterializer,
                               LearnerAssetUrnByCoursewareMutator learnerAssetUrnByCoursewareMutator,
                               LearnerCoursewareByAssetUrnMaterializer learnerCoursewareByAssetUrnMaterializer,
                               LearnerCoursewareByAssetUrnMutator learnerCoursewareByAssetUrnMutator,
                               LearnerAssetUrnByDeploymentMaterializer learnerAssetUrnByDeploymentMaterializer,
                               LearnerAssetUrnByDeploymentMutator learnerAssetUrnByDeploymentMutator,
                               LearnerAssetIdByUrnMaterializer learnerAssetIdByUrnMaterializer,
                               LearnerAssetIdByUrnMutator learnerAssetIdByUrnMutator,
                               DeploymentByAssetUrnMaterializer deploymentByAssetUrnMaterializer,
                               DeploymentByAssetUrnMutator deploymentByAssetUrnMutator,
                               final LearnerIconSourceByAssetMutator learnerIconSourceByAssetMutator,
                               final LearnerIconSourceByAssetMaterializer learnerIconSourceByAssetMaterializer,
                               LearnerMathAssetUrnByElementMutator learnerMathAssetUrnByElementMutator,
                               LearnerMathAssetUrnByElementMaterializer learnerMathAssetUrnByElementMaterializer,
                               LearnerMathAssetIdByUrnMutator learnerMathAssetIdByUrnMutator,
                               LearnerMathAssetIdByUrnMaterializer learnerMathAssetIdByUrnMaterializer,
                               LearnerAssetSummaryMaterializer learnerAssetSummaryMaterializer,
                               LearnerAssetSummaryMutator learnerAssetSummaryMutator) {
        this.session = session;
        this.learnerAssetByCoursewareMaterializer = learnerAssetByCoursewareMaterializer;
        this.learnerAssetByCoursewareMutator = learnerAssetByCoursewareMutator;
        this.learnerCoursewareByAssetMaterializer = learnerCoursewareByAssetMaterializer;
        this.learnerCoursewareByAssetMutator = learnerCoursewareByAssetMutator;
        this.assetByDeploymentMaterializer = assetByDeploymentMaterializer;
        this.assetByDeploymentMutator = assetByDeploymentMutator;
        this.deploymentByAssetMaterializer = deploymentByAssetMaterializer;
        this.deploymentByAssetMutator = deploymentByAssetMutator;
        this.assetSummaryMaterializer = assetSummaryMaterializer;
        this.assetSummaryMutator = assetSummaryMutator;
        this.audioSourceByAssetMaterializer = audioSourceByAssetMaterializer;
        this.audioSourceByAssetMutator = audioSourceByAssetMutator;
        this.documentSourceByAssetMaterializer = documentSourceByAssetMaterializer;
        this.documentSourceByAssetMutator= documentSourceByAssetMutator;
        this.externalSourceByAssetMaterializer = externalSourceByAssetMaterializer;
        this.externalSourceByAssetMutator = externalSourceByAssetMutator;
        this.imageSourceByAssetMaterializer = imageSourceByAssetMaterializer;
        this.imageSourceByAssetMutator = imageSourceByAssetMutator;
        this.videoSourceByAssetMaterializer = videoSourceByAssetMaterializer;
        this.videoSourceByAssetMutator = videoSourceByAssetMutator;
        this.videoSubtitleByAssetMaterializer = videoSubtitleByAssetMaterializer;
        this.videoSubtitleByAssetMutator = videoSubtitleByAssetMutator;
        this.assetMetadataMaterializer = assetMetadataMaterializer;
        this.assetMetadataMutator = assetMetadataMutator;
        this.learnerAssetUrnByCoursewareMaterializer = learnerAssetUrnByCoursewareMaterializer;
        this.learnerAssetUrnByCoursewareMutator = learnerAssetUrnByCoursewareMutator;
        this.learnerCoursewareByAssetUrnMaterializer = learnerCoursewareByAssetUrnMaterializer;
        this.learnerCoursewareByAssetUrnMutator = learnerCoursewareByAssetUrnMutator;
        this.learnerAssetUrnByDeploymentMaterializer = learnerAssetUrnByDeploymentMaterializer;
        this.learnerAssetUrnByDeploymentMutator = learnerAssetUrnByDeploymentMutator;
        this.learnerAssetIdByUrnMaterializer = learnerAssetIdByUrnMaterializer;
        this.learnerAssetIdByUrnMutator = learnerAssetIdByUrnMutator;
        this.deploymentByAssetUrnMaterializer = deploymentByAssetUrnMaterializer;
        this.deploymentByAssetUrnMutator = deploymentByAssetUrnMutator;
        this.learnerIconSourceByAssetMutator = learnerIconSourceByAssetMutator;
        this.learnerIconSourceByAssetMaterializer = learnerIconSourceByAssetMaterializer;
        this.learnerMathAssetUrnByElementMutator = learnerMathAssetUrnByElementMutator;
        this.learnerMathAssetUrnByElementMaterializer = learnerMathAssetUrnByElementMaterializer;
        this.learnerMathAssetIdByUrnMutator = learnerMathAssetIdByUrnMutator;
        this.learnerMathAssetIdByUrnMaterializer = learnerMathAssetIdByUrnMaterializer;
        this.learnerAssetSummaryMaterializer = learnerAssetSummaryMaterializer;
        this.learnerAssetSummaryMutator = learnerAssetSummaryMutator;
    }


    /**
     * Persist the asset courseware element relationship predicated on the asset urn
     *
     * @param assetIdByUrn the assetIdByUrn object holding the relationship
     * @param element the element to associate the asset to
     * @param deployment the deployment the element and the asset belongs to
     * @return a flux of void
     */
    public Flux<Void> persist(final AssetIdByUrn assetIdByUrn, final CoursewareElement element, final Deployment deployment) {
        return Mutators.execute(session, Flux.just(
                learnerAssetUrnByCoursewareMutator.upsert(element.getElementId(), deployment.getChangeId(), assetIdByUrn.getAssetUrn()),
                learnerCoursewareByAssetUrnMutator.upsert(element, deployment, assetIdByUrn.getAssetUrn()),
                learnerAssetUrnByDeploymentMutator.upsert(assetIdByUrn.getAssetUrn(), deployment),
                deploymentByAssetUrnMutator.upsert(assetIdByUrn.getAssetUrn(), deployment),
                learnerAssetIdByUrnMutator.upsert(assetIdByUrn.getAssetUrn(), assetIdByUrn.getAssetId())
        ));
    }

    /**
     * Persist the math asset courseware element relationship predicated on the asset urn
     *
     * @param assetIdByUrn the assetIdByUrn object holding the relationship
     * @param element the element to associate the asset to
     * @param deployment the deployment the element and the asset belongs to
     * @return a flux of void
     */
    public Flux<Void> persist(final com.smartsparrow.math.data.AssetIdByUrn assetIdByUrn,
                              final CoursewareElement element,
                              final Deployment deployment) {
        return Mutators.execute(session, Flux.just(
                learnerMathAssetUrnByElementMutator.upsert(element.getElementId(),
                                                           deployment.getChangeId(),
                                                           assetIdByUrn.getAssetUrn(),
                                                           element.getElementType()),
                learnerMathAssetIdByUrnMutator.upsert(assetIdByUrn.getAssetUrn(), assetIdByUrn.getAssetId())
        ));
    }

    /**
     * Persist the asset data on deployment
     *
     * @param summary the asset summary
     * @return a flux of void
     */
    public Flux<Void> persist(AssetSummary summary) {
        return Mutators.execute(session, Flux.just(assetSummaryMutator.upsert(summary)));
    }

    /**
     * Persist the math asset data on deployment
     *
     * @param summary the math asset summary
     * @return a flux of void
     */
    public Flux<Void> persist(com.smartsparrow.math.data.AssetSummary summary) {
        return Mutators.execute(session, Flux.just(learnerAssetSummaryMutator.upsert(summary)));
    }

    /**
     * Persist the audio source data on deployment
     *
     * @param source the audio source
     * @return a flux of void
     */
    public Flux<Void> persist(AudioSource source) {
        return Mutators.execute(session, Flux.just(audioSourceByAssetMutator.upsert(source)));
    }

    /**
     * Persist the document source data on deployment
     *
     * @param source the document source
     * @return a flux of void
     */
    public Flux<Void> persist(DocumentSource source) {
        return Mutators.execute(session, Flux.just(documentSourceByAssetMutator.upsert(source)));
    }

    /**
     * Persist the external source data on deployment
     *
     * @param source the external source
     * @return a flux of void
     */
    public Flux<Void> persist(ExternalSource source) {
        return Mutators.execute(session, Flux.just(externalSourceByAssetMutator.upsert(source)));
    }

    /**
     * Persist the image source data on deployment
     *
     * @param source the image source
     * @return a flux of void
     */
    public Flux<Void> persist(ImageSource source) {
        return Mutators.execute(session, Flux.just(imageSourceByAssetMutator.upsert(source)));
    }

    /**
     * Persist the meta data on deployment
     *
     * @param data the meta data
     * @return a flux of void
     */
    public Flux<Void> persist(AssetMetadata data) {
        return Mutators.execute(session, Flux.just(assetMetadataMutator.upsert(data)));
    }

    /**
     * Persist the video source data on deployment
     *
     * @param source the video source
     * @return a flux of void
     */
    public Flux<Void> persist(VideoSource source) {
        return Mutators.execute(session, Flux.just(videoSourceByAssetMutator.upsert(source)));
    }

    /**
     * Persist the video subtitle data on deployment
     *
     * @param subtitle the video subtitle
     * @return a flux of void
     */
    public Flux<Void> persist(VideoSubtitle subtitle) {
        return Mutators.execute(session, Flux.just(videoSubtitleByAssetMutator.upsert(subtitle)));
    }

    /**
     * Persist the icon source data on deployment
     *
     * @param source the icon source
     * @return a flux of void
     */
    public Flux<Void> persist(IconSource source) {
        return Mutators.execute(session, Flux.just(learnerIconSourceByAssetMutator.upsert(source)));
    }

    /**
     * Find all courseware elements associated with an asset
     *
     * @param assetId the asset id to find the elements for
     * @return a flux of courseware elements
     */
    public Flux<CoursewareElement> findElements(UUID assetId, UUID deploymentId, UUID changeId) {
        return ResultSets.query(session, learnerCoursewareByAssetMaterializer.findElements(assetId, deploymentId, changeId))
                .flatMapIterable(row->row)
                .map(learnerCoursewareByAssetMaterializer::fromRow);
    }

    /**
     * Find all asset ids associated with a specific courseware element. To get the correct changeId get the top level
     * parent of the element then get the latest changeId for each deployment
     *
     * @param elementId the element to find the assets for
     * @param changeId the changeId of the deployment
     * @return a flux of asset ids
     */
    @Deprecated
    public Flux<UUID> findAssets(UUID elementId, UUID changeId) {
        return ResultSets.query(session, learnerAssetByCoursewareMaterializer.fetchAll(elementId, changeId))
                .flatMapIterable(row->row)
                .map(learnerAssetByCoursewareMaterializer::fromRow);
    }

    /**
     * Find all the assetUrn associated to an element
     *
     * @param elementId the elementId to find the associated asset urn for
     * @param changeId the change id of the element
     * @return a flux of string representing the urns
     */
    public Flux<String> findAssetsUrn(final UUID elementId, final UUID changeId) {
        return ResultSets.query(session, learnerAssetUrnByCoursewareMaterializer.fetchAll(elementId, changeId))
                .flatMapIterable(row->row)
                .map(learnerAssetUrnByCoursewareMaterializer::fromRow);
    }

    /**
     * Find all the math assetUrn associated to an element
     *
     * @param elementId the elementId to find the associated asset urn for
     * @param changeId the change id of the element
     * @return a flux of string representing the urns
     */
    public Flux<String> findMathAssetsUrn(final UUID elementId, final UUID changeId) {
        return ResultSets.query(session, learnerMathAssetUrnByElementMaterializer.fetchAll(elementId, changeId))
                .flatMapIterable(row->row)
                .map(learnerMathAssetUrnByElementMaterializer::fromRow);
    }

    /**
     * Find all assets that are used in a deployment
     *
     * @param deployment the deployment to find the assets for
     * @return a flux of asset ids
     */
    public Flux<UUID> findAssets(Deployment deployment) {
        return ResultSets.query(session, assetByDeploymentMaterializer.findAssets(deployment))
                .flatMapIterable(row->row)
                .map(assetByDeploymentMaterializer::fromRow);
    }

    /**
     * Find all deployments an asset is used in
     *
     * @param assetId the asset id to look for
     * @return a flux of deployments
     */
    public Flux<Deployment> findDeployments(UUID assetId) {
        return ResultSets.query(session, deploymentByAssetMaterializer.findDeployments(assetId))
                .flatMapIterable(row->row)
                .map(deploymentByAssetMaterializer::fromRow);
    }

    /**
     * Fetch an asset summary by id
     *
     * @param id the asset id
     * @return mono with AssetSummary, empty mono if asset is not found
     */
    @Trace(async = true)
    public Mono<AssetSummary> fetchAssetById(UUID id) {
        return ResultSets.query(session, assetSummaryMaterializer.fetchAssetBy(id))
                .flatMapIterable(row -> row)
                .map(assetSummaryMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch a math asset summary by id
     *
     * @param id the asset id
     * @return mono with AssetSummary, empty mono if asset is not found
     */
    @Trace(async = true)
    public Mono<com.smartsparrow.math.data.AssetSummary> fetchMathAssetById(UUID id) {
        return ResultSets.query(session, learnerAssetSummaryMaterializer.findById(id))
                .flatMapIterable(row -> row)
                .map(learnerAssetSummaryMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all metadata for asset
     *
     * @param assetId the asset id
     * @return all AssetMetadata objects for the asset, empty flux if no metadata for the asset
     */
    @Trace(async = true)
    public Flux<AssetMetadata> fetchMetadata(UUID assetId) {
        return ResultSets.query(session, assetMetadataMaterializer.fetchAllBy(assetId))
                .flatMapIterable(row -> row)
                .map(assetMetadataMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all audio sources for asset
     *
     * @param assetId the asset id
     * @return flux with AudioSource, empty flux if no audio sources for the asset
     */
    @Trace(async = true)
    public Mono<ExternalSource> fetchExternalSource(UUID assetId) {
        return ResultSets.query(session, externalSourceByAssetMaterializer.findBy(assetId))
                .flatMapIterable(row -> row)
                .singleOrEmpty()
                .map(externalSourceByAssetMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all image sources for asset
     *
     * @param assetId the asset id
     * @return flux with ImageSource, empty flux if no image sources for the asset
     */
    @Trace(async = true)
    public Flux<ImageSource> fetchImageSources(UUID assetId) {
        return ResultSets.query(session, imageSourceByAssetMaterializer.findAllBy(assetId))
                .flatMapIterable(row -> row)
                .map(imageSourceByAssetMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all video sources for asset
     *
     * @param assetId the asset id
     * @return flux with VideoSource, empty flux if no video sources for the asset
     */
    @Trace(async = true)
    public Flux<VideoSource> fetchVideoSources(UUID assetId) {
        return ResultSets.query(session, videoSourceByAssetMaterializer.fetchAllBy(assetId))
                .flatMapIterable(row -> row)
                .map(videoSourceByAssetMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all video subtitles for asset
     * @param assetId th asset id
     * @return flux with VideoSubtitle, empty flux if no subtitles found
     */
    public Flux<VideoSubtitle> fetchVideoSubtitles(UUID assetId) {
        return ResultSets.query(session, videoSubtitleByAssetMaterializer.fetchBy(assetId))
                .flatMapIterable(row -> row)
                .map(videoSubtitleByAssetMaterializer::fromRow);
    }

    /**
     * Fetch document source for asset
     *
     * @param assetId the asset id
     * @return flux with DocumentSource, empty flux if no document sources for the asset
     */
    @Trace(async = true)
    public Mono<DocumentSource> fetchDocumentSource(UUID assetId) {
        return ResultSets.query(session, documentSourceByAssetMaterializer.findBy(assetId))
                .flatMapIterable(row -> row)
                .map(documentSourceByAssetMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all audio sources for asset
     *
     * @param assetId the asset id
     * @return flux with AudioSource, empty flux if no audio sources for the asset
     */
    @Trace(async = true)
    public Flux<AudioSource> fetchAudioSources(UUID assetId) {
        return ResultSets.query(session, audioSourceByAssetMaterializer.findAll(assetId))
                .flatMapIterable(row -> row)
                .map(audioSourceByAssetMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the latest assetId that was associated to this assetUrn
     *
     * @param assetUrn the asset urn to find the asset id for
     * @return a mono containing an assetIdByUrn object
     */
    @Trace(async = true)
    public Mono<AssetIdByUrn> findAssetId(final String assetUrn) {
        return ResultSets.query(session, learnerAssetIdByUrnMaterializer.findAssetId(assetUrn))
                .flatMapIterable(row -> row)
                .map(learnerAssetIdByUrnMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the latest assetId that was associated to this assetUrn
     *
     * @param assetUrn the asset urn to find the asset id for
     * @return a mono containing an assetIdByUrn object
     */
    @Trace(async = true)
    public Mono<com.smartsparrow.math.data.AssetIdByUrn> findMathAssetId(final String assetUrn) {
        return ResultSets.query(session, learnerMathAssetIdByUrnMaterializer.findAssetId(assetUrn))
                .flatMapIterable(row -> row)
                .map(learnerMathAssetIdByUrnMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all icon sources for asset
     *
     * @param assetId the asset id
     * @return flux with IconSource, empty flux if no icon sources for the asset
     */
    public Flux<IconSource> fetchIconSources(UUID assetId) {
        return ResultSets.query(session, learnerIconSourceByAssetMaterializer.findAllBy(assetId))
                .flatMapIterable(row -> row)
                .map(learnerIconSourceByAssetMaterializer::fromRow);
    }
}
