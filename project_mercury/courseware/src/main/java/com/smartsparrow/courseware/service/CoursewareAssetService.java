package com.smartsparrow.courseware.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.AssetUtils;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.courseware.data.AssetByRootActivity;
import com.smartsparrow.courseware.data.AssetUrnByRootActivity;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.CoursewareGateway;
import com.smartsparrow.math.service.MathAssetService;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CoursewareAssetService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareAssetService.class);

    private final CoursewareGateway coursewareGateway;
    private final BronteAssetService bronteAssetService;
    private final WorkspaceAssetService workspaceAssetService;
    private final CoursewareAssetConfigService coursewareAssetConfigService;
    private final MathAssetService mathAssetService;

    @Inject
    public CoursewareAssetService(final CoursewareGateway coursewareGateway,
                                  final BronteAssetService bronteAssetService,
                                  final WorkspaceAssetService workspaceAssetService,
                                  final CoursewareAssetConfigService coursewareAssetConfigService,
                                  final MathAssetService mathAssetService) {
        this.coursewareGateway = coursewareGateway;
        this.bronteAssetService = bronteAssetService;
        this.workspaceAssetService = workspaceAssetService;
        this.coursewareAssetConfigService = coursewareAssetConfigService;
        this.mathAssetService = mathAssetService;
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

        return coursewareGateway.findAssetUrn(elementId)
                .flatMap(bronteAssetService::findAssetId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find assets used in the given courseware element
     *
     * @param elementId the element id
     * @return mono with list of asset payloads; empty list if no assets used
     */
    @Trace(async = true)
    public Mono<List<AssetPayload>> getAssetPayloads(final UUID elementId) {
        affirmArgument(elementId != null, "elementId is required");

        // try using the assetUrn/elementId association
        final Flux<AssetPayload> resolvedAssets = getAssetsFor(elementId)
                .map(AssetIdByUrn::getAssetUrn)
                .flatMap(workspaceAssetService::getAssetPayload)
                .doOnEach(ReactiveTransaction.linkOnNext());


        // return the resolved asset
        return resolvedAssets
                // collect to a list
                .collectList();
    }

    /**
     * Get all assets by root activity id
     *
     * @param rootElementId the root element id
     * @return flux of asset by root activity
     */
    @Deprecated
    public Flux<AssetByRootActivity> getAssetsByRootActivity(final UUID rootElementId) {
        affirmArgument(rootElementId != null, "rootElementId is required");
        return coursewareGateway.getAssetsByRootActivity(rootElementId);
    }

    /**
     * Get all the asset urn that are associated with any element in the tree starting from the rootElement
     *
     * @param rootActivityId the root activity id to find the asset urn for
     * @return a flux of assetUrnByRootActivity
     */
    public Flux<AssetUrnByRootActivity> getAssetUrnByRootActivity(final UUID rootActivityId) {
        affirmArgument(rootActivityId != null, "rootActivityId is required");
        return coursewareGateway.getAssetsUrnByRootActivity(rootActivityId);
    }

    /**
     * Get all the asset summary for a rootActivityId
     *
     * @param rootActivityId the root activity to find all the summaries for
     * @return a flux of asset summary
     */
    public Flux<AssetSummary> getAllSummariesForRootActivity(final UUID rootActivityId) {
        // find all the asset urn for a rootActivity (and below)
        return getAssetUrnByRootActivity(rootActivityId)
                // for each assetUrn find the latest assetId
                .flatMap(assetUrnByRootActivity -> bronteAssetService.findAssetId(assetUrnByRootActivity.getAssetUrn())
                        .map(AssetIdByUrn::getAssetId)
                        // finally fetch the summary
                        .flatMap(bronteAssetService::getAssetSummary));
    }


    /**
     * Get all elements by asset id
     *
     * @param assetId the asset id
     * @return flux of courseware elements by asset
     */
    public Flux<CoursewareElement> getCoursewareElementsByAsset(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");
        return coursewareGateway.getCoursewareByAsset(assetId);
    }

    /**
     * Get all assets by root activity id and asset provider
     *
     * @param rootElementId the root element id
     * @param provider      the asset provider
     * @return flux of asset by root activity
     */
    public Flux<AssetByRootActivity> getAssetsByRootActivityAndProvider(final UUID rootElementId, final AssetProvider provider) {
        affirmArgument(rootElementId != null, "rootElementId is required");
        affirmArgument(provider != null, "provider is required");
        return coursewareGateway.getAssetsByRootActivityAndProvider(rootElementId, Enums.asString(provider));
    }

    /**
     * Add an asset to a courseware
     *
     * @param elementId   the element id of the courseware
     * @param elementType the element type of the courseware
     * @param assetUrn    the asset URN
     * @throws AssetURNParseException if asset URN is invalid
     */
    public Flux<Void> addAsset(final UUID elementId, final CoursewareElementType elementType, final String assetUrn, final UUID rootElementId) {
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(elementType != null, "elementType is required");
        affirmArgument(!Strings.isNullOrEmpty(assetUrn), "assetUrn is required");
        affirmArgument(rootElementId != null, "root elementId is required");

        return coursewareGateway.persist(new CoursewareElement(elementId, elementType),
                AssetUtils.parseURN(assetUrn),
                rootElementId);
    }

    /**
     * Remove an asset from a courseware
     *
     * @param elementId the courseware element id
     * @param assetUrn  the asset URN
     * @throws AssetURNParseException if asset URN is invalid
     */
    @Trace(async = true)
    public Flux<Void> removeAsset(final UUID elementId, final String assetUrn, final UUID rootElementId) {
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(!Strings.isNullOrEmpty(assetUrn), "assetUrn is required");
        affirmArgument(rootElementId != null, "root elementId is required");

        return coursewareGateway.remove(elementId,
                AssetUtils.parseURN(assetUrn),
                rootElementId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Remove multiple assets from a courseware
     *
     * @param elementId the courseware element id
     * @param assetURNs list of assertUrn's
     * @param rootElementId the root element id
     * @throws AssetURNParseException if asset URN is invalid
     */
    @Trace(async = true)
    public Flux<Void> removeAssets(final UUID elementId, final List<String> assetURNs, final UUID rootElementId) {
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(rootElementId != null, "root elementId is required");
        affirmArgument(assetURNs != null, "assetURNs is required");

        return coursewareGateway.removeAssets(elementId,  assetURNs, rootElementId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicates assets from one courseware element to another.
     * Note: If required asset id in the context is false, it does not duplicate the whole asset. It just duplicates
     * associations asset-courseware by asset urn. Otherwise, duplicate the whole asset with a new asset id
     *
     * @param elementId     the element to duplicate assets from
     * @param newElementId  the element to duplicate assets to
     * @param elementType   the element type of courseware element
     * @param context       the duplication context
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> duplicateAssets(final UUID elementId,
                                      final UUID newElementId,
                                      final CoursewareElementType elementType,
                                      final DuplicationContext context) {
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(newElementId != null, "newElementId is required");
        affirmArgument(elementType != null, "elementType is required");
        affirmArgument(context != null, "context is required");
        affirmArgument(context.getNewRootElementId() != null, "newRootElementId is required");

        // try duplicating the assets
        return getAssetsFor(elementId)
                .flatMap(assetIdByUrn -> {
                    // it duplicates asset with a new asset id if the required new asset id is true
                    if (context.getRequireNewAssetId()) {

                        return bronteAssetService.duplicate(assetIdByUrn.getAssetId(),
                                                            context.getDuplicatorAccount(),
                                                            context.getDuplicatorSubscriptionId())
                                .map(assetSummary -> new AssetIdByUrn()
                                        .setAssetId(assetSummary.getId())
                                        .setAssetUrn(assetSummary.getUrn()))
                                .flatMap(newAssetIdByUrn ->
                                        // update new asset urn in config
                                        coursewareAssetConfigService.updateAssetUrn(newElementId,
                                                                                        elementType,
                                                                                        assetIdByUrn.getAssetUrn(),
                                                                                        newAssetIdByUrn.getAssetUrn())
                                        .then(Mono.just(newAssetIdByUrn)));
                    }
                    // otherwise, it duplicates associations asset-courseware by asset urn
                    return Mono.just(assetIdByUrn);
                })
                // for each asset urn/id persist the association with the duplicated element
                .flatMap(assetIdByUrn -> coursewareGateway.persist(CoursewareElement.from(newElementId, elementType),
                                new AssetUrn(assetIdByUrn.getAssetUrn()),
                                context.getNewRootElementId())
                        .doOnEach(ReactiveTransaction.linkOnNext()));
    }


    /**
     * Fetch math assets for an elementId
     *
     * @param elementId the elementId to fetch the assets for
     * @return a flux of AssetPayload{@link Flux<AssetPayload>}
     */
    public Mono<List<AssetPayload>> fetchMathAssetsForElement(final UUID elementId) {
        // fetch the asset urn associated to this element
        return mathAssetService.getAssetsFor(elementId)
                // for each urn find the asset id
                .map(com.smartsparrow.math.data.AssetIdByUrn::getAssetId)
                .flatMap(mathAssetService::getMathAssetPayload)
                .collectList();
    }
}
