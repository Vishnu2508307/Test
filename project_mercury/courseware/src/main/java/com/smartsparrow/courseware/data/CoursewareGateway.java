package com.smartsparrow.courseware.data;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.asset.service.AssetUtils;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CoursewareGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareGateway.class);

    private final Session session;

    private final CoursewareElementByAssetMaterializer coursewareElementByAssetMaterializer;
    private final StudentScopeRegistryMutator studentScopeRegistryMutator;
    private final StudentScopeRegistryByCoursewareElementMutator studentScopeRegistryByCoursewareElementMutator;
    private final StudentScopeRegistryByCoursewareElementMaterializer studentScopeRegistryByCoursewareElementMaterializer;
    private final WalkableByStudentScopeMaterializer walkableByStudentScopeMaterializer;
    private final StudentScopeRegistryMaterializer studentScopeRegistryMaterializer;
    private final CoursewareConfigurationFieldMaterializer coursewareConfigurationFieldMaterializer;
    private final CoursewareConfigurationFieldMutator coursewareConfigurationFieldMutator;
    private final CoursewareElementMetaInformationMaterializer coursewareElementMetaInformationMaterializer;
    private final CoursewareElementMetaInformationMutator coursewareElementMetaInformationMutator;
    private final AssetByCoursewareRootActivityMaterializer assetByCoursewareActivityMaterializer;
    private final AssetUrnByCoursewareMaterializer assetUrnByCoursewareMaterializer;
    private final AssetUrnByCoursewareMutator assetUrnByCoursewareMutator;
    private final CoursewareElementByAssetUrnMaterializer coursewareElementByAssetUrnMaterializer;
    private final CoursewareElementByAssetUrnMutator coursewareElementByAssetUrnMutator;
    private final AssetUrnByRootActivityMaterializer assetUrnByRootActivityMaterializer;
    private final AssetUrnByRootActivityMutator assetUrnByRootActivityMutator;
    private final ElementMaterializer elementMaterializer;
    private final ElementMutator elementMutator;

    @Inject
    public CoursewareGateway(final Session session,
                             final CoursewareElementByAssetMaterializer coursewareElementByAssetMaterializer,
                             final StudentScopeRegistryMutator studentScopeRegistryMutator,
                             final StudentScopeRegistryByCoursewareElementMutator studentScopeRegistryByCoursewareElementMutator,
                             final StudentScopeRegistryByCoursewareElementMaterializer studentScopeRegistryByCoursewareElementMaterializer,
                             final WalkableByStudentScopeMaterializer walkableByStudentScopeMaterializer,
                             final StudentScopeRegistryMaterializer studentScopeRegistryMaterializer,
                             final CoursewareConfigurationFieldMaterializer coursewareConfigurationFieldMaterializer,
                             final CoursewareConfigurationFieldMutator coursewareConfigurationFieldMutator,
                             final CoursewareElementMetaInformationMaterializer coursewareElementMetaInformationMaterializer,
                             final CoursewareElementMetaInformationMutator coursewareElementMetaInformationMutator,
                             final AssetByCoursewareRootActivityMaterializer assetByCoursewareActivityMaterializer,
                             final AssetUrnByCoursewareMaterializer assetUrnByCoursewareMaterializer,
                             final AssetUrnByCoursewareMutator assetUrnByCoursewareMutator,
                             final CoursewareElementByAssetUrnMaterializer coursewareElementByAssetUrnMaterializer,
                             final CoursewareElementByAssetUrnMutator coursewareElementByAssetUrnMutator,
                             final AssetUrnByRootActivityMaterializer assetUrnByRootActivityMaterializer,
                             final AssetUrnByRootActivityMutator assetUrnByRootActivityMutator,
                             final ElementMaterializer elementMaterializer,
                             final ElementMutator elementMutator) {
        this.session = session;
        this.coursewareElementByAssetMaterializer = coursewareElementByAssetMaterializer;
        this.studentScopeRegistryMutator = studentScopeRegistryMutator;
        this.studentScopeRegistryByCoursewareElementMutator = studentScopeRegistryByCoursewareElementMutator;
        this.studentScopeRegistryByCoursewareElementMaterializer = studentScopeRegistryByCoursewareElementMaterializer;
        this.walkableByStudentScopeMaterializer = walkableByStudentScopeMaterializer;
        this.studentScopeRegistryMaterializer = studentScopeRegistryMaterializer;
        this.coursewareConfigurationFieldMaterializer = coursewareConfigurationFieldMaterializer;
        this.coursewareConfigurationFieldMutator = coursewareConfigurationFieldMutator;
        this.coursewareElementMetaInformationMaterializer = coursewareElementMetaInformationMaterializer;
        this.coursewareElementMetaInformationMutator = coursewareElementMetaInformationMutator;
        this.assetByCoursewareActivityMaterializer = assetByCoursewareActivityMaterializer;
        this.assetUrnByCoursewareMaterializer = assetUrnByCoursewareMaterializer;
        this.assetUrnByCoursewareMutator = assetUrnByCoursewareMutator;
        this.coursewareElementByAssetUrnMaterializer = coursewareElementByAssetUrnMaterializer;
        this.coursewareElementByAssetUrnMutator = coursewareElementByAssetUrnMutator;
        this.assetUrnByRootActivityMaterializer = assetUrnByRootActivityMaterializer;
        this.assetUrnByRootActivityMutator = assetUrnByRootActivityMutator;
        this.elementMaterializer = elementMaterializer;
        this.elementMutator = elementMutator;
    }

    /**
     * Persist association between courseware element and asset
     *
     * @param element       the courseware element
     * @param assetUrn      contains asset id and asset provider details
     * @param rootElementId the root element id
     */
    @Trace(async = true)
    public Flux<Void> persist(final CoursewareElement element, final AssetUrn assetUrn, final UUID rootElementId) {

        final CoursewareElementByAssetUrn coursewareElementByAssetUrn = new CoursewareElementByAssetUrn()
                .setAssetUrn(assetUrn.toString())
                .setCoursewareElement(element);

        final AssetUrnByRootActivity assetUrnByRootActivity = new AssetUrnByRootActivity()
                .setAssetUrn(assetUrn.toString())
                .setRootActivityId(rootElementId)
                .setCoursewareElement(element);

        return Mutators.execute(session, Flux.just(
                assetUrnByCoursewareMutator.upsert(coursewareElementByAssetUrn),
                coursewareElementByAssetUrnMutator.upsert(coursewareElementByAssetUrn),
                assetUrnByRootActivityMutator.upsert(assetUrnByRootActivity))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveErrorThrowable("error while saving asset details",
                        throwable -> new HashMap<String, Object>() {
                            {
                                put("rootElementId", rootElementId);
                                put("elementId", element.getElementId());
                            }
                        })));
    }

    /**
     * Remove association between courseware element and asset
     *
     * @param assetUrn      contains asset id and asset provider details
     * @param rootElementId the root element id
     */
    @Trace(async = true)
    public Flux<Void> remove(final UUID elementId, final AssetUrn assetUrn, final UUID rootElementId) {
        final CoursewareElementByAssetUrn coursewareElementByAssetUrn = new CoursewareElementByAssetUrn()
                .setAssetUrn(assetUrn.toString())
                .setCoursewareElement(new CoursewareElement()
                        // it's safe not to set the type since that is not part of the key
                        .setElementId(elementId));

        final AssetUrnByRootActivity assetUrnByRootActivity = new AssetUrnByRootActivity()
                .setAssetUrn(assetUrn.toString())
                .setRootActivityId(rootElementId)
                .setCoursewareElement(new CoursewareElement()
                        // it's safe not to set the type since that is not part of the key
                        .setElementId(elementId));

        return Mutators.execute(session, Flux.just(
                assetUrnByCoursewareMutator.delete(coursewareElementByAssetUrn),
                coursewareElementByAssetUrnMutator.delete(coursewareElementByAssetUrn),
                assetUrnByRootActivityMutator.delete(assetUrnByRootActivity))
                .doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Remove association between courseware element and asset for multiple assertUrn's
     *
     * @param assetURNs contains list of assetUrn's
     * @param rootElementId the root element id
     */
    @Trace(async = true)
    public Flux<Void> removeAssets(final UUID elementId, final List<String> assetURNs, final UUID rootElementId) {

        return assetURNs.stream()
                .map(assetURN -> {
                    AssetUrn assetUrn = AssetUtils.parseURN(assetURN);
                    final CoursewareElementByAssetUrn coursewareElementByAssetUrn = new CoursewareElementByAssetUrn()
                            .setAssetUrn(assetUrn.toString())
                            .setCoursewareElement(new CoursewareElement()
                                                          // it's safe not to set the type since that is not part of the key
                                                          .setElementId(elementId));

                    final AssetUrnByRootActivity assetUrnByRootActivity = new AssetUrnByRootActivity()
                            .setAssetUrn(assetUrn.toString())
                            .setRootActivityId(rootElementId)
                            .setCoursewareElement(new CoursewareElement()
                                                          // it's safe not to set the type since that is not part of the key
                                                          .setElementId(elementId));

                    return Mutators.execute(session, Flux.just(
                            assetUrnByCoursewareMutator.delete(coursewareElementByAssetUrn),
                            coursewareElementByAssetUrnMutator.delete(coursewareElementByAssetUrn),
                            assetUrnByRootActivityMutator.delete(assetUrnByRootActivity)));
                })
                .reduce(Flux::mergeWith)
                .orElse(Flux.empty())
                .doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Fetch all the asset URNs associated with an element
     *
     * @param elementId the id of the element to find the asset URNs for
     * @return a flux of string representing the asset urn
     */
    @Trace(async = true)
    public Flux<String> findAssetUrn(final UUID elementId) {
        return ResultSets.query(session, assetUrnByCoursewareMaterializer.findAssetUrnFor(elementId))
                .flatMapIterable(row -> row)
                .map(assetUrnByCoursewareMaterializer::fromRow)
                .map(CoursewareElementByAssetUrn::getAssetUrn)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a registered reference to a student scope
     *
     * @param reference an object containing the plugin reference, studentScope and coursewareElement
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final ScopeReference reference) {
        return Mutators.execute(session, Flux.just(
                studentScopeRegistryMutator.upsert(reference),
                studentScopeRegistryByCoursewareElementMutator.upsert(reference)
        )).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete a persisted registered reference to a student scope
     *
     * @param reference contains the scope reference info
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> delete(final ScopeReference reference) {
        return Mutators.execute(session, Flux.just(
                studentScopeRegistryMutator.delete(reference),
                studentScopeRegistryByCoursewareElementMutator.delete(reference)
        ).doOnEach(ReactiveTransaction.linkOnNext()));
    }

    /**
     * Find the registered reference
     *
     * @param elementId       the elementId that registered to the student scope
     * @param studentScopeURN the student scope the element should be registered to
     * @return a mono of registered reference
     */
    public Mono<ScopeReference> find(final UUID elementId, final UUID studentScopeURN) {
        return ResultSets.query(session, studentScopeRegistryByCoursewareElementMaterializer.find(elementId, studentScopeURN))
                .flatMapIterable(row -> row)
                .map(studentScopeRegistryByCoursewareElementMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find a courseware element by its student scope urn
     *
     * @param studentScopeURN the student scope urn to find the courseware element for
     * @return a mono of courseware element
     */
    public Mono<CoursewareElement> findElementBy(final UUID studentScopeURN) {
        return ResultSets.query(session, walkableByStudentScopeMaterializer.findWalkable(studentScopeURN))
                .flatMapIterable(row -> row)
                .map(walkableByStudentScopeMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Find all the registered elements to a student scope urn
     *
     * @param studentScopeURN the student scope urn to find the registered items for
     * @return a flux of scope reference
     */
    @Trace(async = true)
    public Flux<ScopeReference> findRegisteredElements(final UUID studentScopeURN) {
        return ResultSets.query(session, studentScopeRegistryMaterializer.findAllRegistered(studentScopeURN))
                .flatMapIterable(row -> row)
                .map(studentScopeRegistryMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a configuration field for a courseware element
     *
     * @param configurationField the configuration field to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final CoursewareElementConfigurationField configurationField) {
        return Mutators.execute(session, Flux.just(
                coursewareConfigurationFieldMutator.upsert(configurationField)
        )).doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch a specific configuration field for a courseware element
     *
     * @param elementId the element id to find the configuration field for
     * @param fieldName the name of the configuration field to find
     * @return a mono of configuration field
     */
    @Trace(async = true)
    public Mono<ConfigurationField> fetchConfigurationField(final UUID elementId, final String fieldName) {
        return ResultSets.query(session, coursewareConfigurationFieldMaterializer.fetchField(elementId, fieldName))
                .flatMapIterable(row -> row)
                .map(coursewareConfigurationFieldMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all the extracted configuration fields for a courseware element
     *
     * @param elementId the element id to find all the configuration fields for
     * @return a flux of configuration field
     */
    @Trace(async = true)
    public Flux<ConfigurationField> fetchConfigurationFields(final UUID elementId) {
        return ResultSets.query(session, coursewareConfigurationFieldMaterializer.fetchAll(elementId))
                .flatMapIterable(row -> row)
                .map(coursewareConfigurationFieldMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist a courseware element meta information
     *
     * @param coursewareElementMetaInformation the obj to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final CoursewareElementMetaInformation coursewareElementMetaInformation) {
        return Mutators.execute(session, Flux.just(
                coursewareElementMetaInformationMutator.upsert(coursewareElementMetaInformation)
        )).doOnEach(ReactiveTransaction.linkOnNext())
        .doOnError(throwable -> {
            log.error("error while persisting courseware element meta information", throwable);
            throw Exceptions.propagate(throwable);
        });
    }

    /**
     * Fetch courseware element meta information by element id and key
     *
     * @param elementId the element id to find the meta info for
     * @param key       the name of the meta information to fetch fot the element
     * @return a mono with the meta information or an empty mono when not found
     */
    @Trace(async = true)
    public Mono<CoursewareElementMetaInformation> fetchMetaInformation(final UUID elementId, final String key) {
        return ResultSets.query(session, coursewareElementMetaInformationMaterializer.findMetaInformation(elementId, key))
                .flatMapIterable(row -> row)
                .map(coursewareElementMetaInformationMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch courseware element meta information by element id and key
     *
     * @param elementId the element id to find the meta info for
     * @return a flux with the meta information or an empty flux when not found
     */
    @Trace(async = true)
    public Flux<CoursewareElementMetaInformation> fetchAllMetaInformation(final UUID elementId) {
        return ResultSets.query(session, coursewareElementMetaInformationMaterializer.findAllMetaInformation(elementId))
                .flatMapIterable(row -> row)
                .map(coursewareElementMetaInformationMaterializer::fromRow)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all assets by root activity
     *
     * @param rootElementId the root activity id
     * @return mono of Asset by activity object
     */
    @Deprecated
    public Flux<AssetByRootActivity> getAssetsByRootActivity(final UUID rootElementId) {
        return ResultSets.query(session, assetByCoursewareActivityMaterializer.findById(rootElementId))
                .flatMapIterable(row -> row)
                .map(assetByCoursewareActivityMaterializer::fromRow);
    }

    /**
     * Find all the asset urn for a root activity
     *
     * @param rootActivityId the root activity to find the urns for
     * @return a flux of assetUrnByRootActivity objects
     */
    public Flux<AssetUrnByRootActivity> getAssetsUrnByRootActivity(final UUID rootActivityId) {
        return ResultSets.query(session, assetUrnByRootActivityMaterializer.findAssetUrnFor(rootActivityId))
                .flatMapIterable(row -> row)
                .map(assetUrnByRootActivityMaterializer::fromRow);
    }

    /**
     * Fetch all assets by root activity and asset provider
     *
     * @param rootElementId the root activity id
     * @param assetProvider the asset provider
     * @return mono of Asset by activity object
     */
    public Flux<AssetByRootActivity> getAssetsByRootActivityAndProvider(final UUID rootElementId, final String assetProvider) {
        return ResultSets.query(session, assetByCoursewareActivityMaterializer.findByIdAndProvider(rootElementId, assetProvider))
                .flatMapIterable(row -> row)
                .map(assetByCoursewareActivityMaterializer::fromRow);
    }


    /**
     * Fetch courseware element an asset is assigned to
     *
     * @param assetId the asset id
     * @return flux of element ids, empty flux if no elements found
     */
    public Flux<CoursewareElement> getCoursewareByAsset(final UUID assetId) {
        return ResultSets.query(session, coursewareElementByAssetMaterializer.fetchAll(assetId))
                .flatMapIterable(row -> row)
                .map(coursewareElementByAssetMaterializer::fromRow);
    }

    /**
     * Persist a courseware element
     *
     * @param coursewareElement the obj to persist
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final CoursewareElement coursewareElement) {
        return Mutators.execute(session, Flux.just(
                elementMutator.upsert(coursewareElement)
        )).doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error("error while persisting courseware element", throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find a courseware element by its id
     *
     * @param elementId the element id
     * @return a mono of courseware element
     */
    @Trace(async = true)
    public Mono<CoursewareElement> findElementById(final UUID elementId) {
        return ResultSets.query(session, elementMaterializer.fetchById(elementId))
                .flatMapIterable(row -> row)
                .map(elementMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
