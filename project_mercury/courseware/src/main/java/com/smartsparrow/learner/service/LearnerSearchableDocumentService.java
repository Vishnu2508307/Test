package com.smartsparrow.learner.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.google.common.annotations.VisibleForTesting;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementAncestry;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.wiring.CsgConfig;
import com.smartsparrow.ext_http.service.ExternalHttpRequestService;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.ext_http.service.RequestPurpose;
import com.smartsparrow.iam.wiring.IesSystemToSystemIdentityProvider;
import com.smartsparrow.learner.data.DeploymentGateway;
import com.smartsparrow.learner.data.LearnerElement;
import com.smartsparrow.learner.data.LearnerSearchableDocument;
import com.smartsparrow.learner.data.LearnerSearchableDocumentGateway;
import com.smartsparrow.learner.data.LearnerSearchableDocumentIdentity;
import com.smartsparrow.learner.route.CSGIndexRoute;
import com.smartsparrow.learner.searchable.CsgDeleteRequestBuilder;
import com.smartsparrow.learner.searchable.CsgIndexRequestBuilder;
import com.smartsparrow.learner.searchable.LearnerSearchableDocumentDeploymentDiff;
import com.smartsparrow.learner.searchable.LearnerSearchableFieldSelector;
import com.smartsparrow.learner.searchable.LearnerSearchableFieldValue;
import com.smartsparrow.plugin.data.PluginGateway;
import com.smartsparrow.plugin.data.PluginSearchableField;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class LearnerSearchableDocumentService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerSearchableDocumentService.class);

    private final LearnerSearchableDocumentGateway learnerSearchableDocumentGateway;
    private final PluginGateway pluginGateway;
    private final PluginService pluginService;
    private final CoursewareService coursewareService;
    private final CohortService cohortService;
    private final LearnerSearchableFieldSelector learnerSearchableFieldSelector;
    private final CsgConfig csgConfig;
    private final IesSystemToSystemIdentityProvider identityProvider;
    private final ExternalHttpRequestService httpRequestService;
    private final DeploymentGateway deploymentGateway;
    private final CamelReactiveStreamsService camel;

    @Inject
    public LearnerSearchableDocumentService(final LearnerSearchableDocumentGateway learnerSearchableDocumentGateway,
                                            final PluginGateway pluginGateway,
                                            final PluginService pluginService,
                                            final CoursewareService coursewareService,
                                            final CohortService cohortService,
                                            final LearnerSearchableFieldSelector learnerSearchableFieldSelector,
                                            final CsgConfig csgConfig,
                                            final IesSystemToSystemIdentityProvider identityProvider,
                                            final ExternalHttpRequestService httpRequestService,
                                            final DeploymentGateway deploymentGateway,
                                            final CamelReactiveStreamsService camel) {
        this.learnerSearchableDocumentGateway = learnerSearchableDocumentGateway;
        this.pluginGateway = pluginGateway;
        this.pluginService = pluginService;
        this.coursewareService = coursewareService;
        this.cohortService = cohortService;
        this.learnerSearchableFieldSelector = learnerSearchableFieldSelector;
        this.csgConfig = csgConfig;
        this.identityProvider = identityProvider;
        this.httpRequestService = httpRequestService;
        this.deploymentGateway = deploymentGateway;
        this.camel = camel;
    }

    /**
     * Persists the learner searchable documents for a learner element on a cohort
     * and then indexes it to CSG.
     *
     * @param learnerElement the activity/interactive/component to create
     * @param cohortId the cohortId to deploy the activity/interactive to
     * @return a flux of learner searchable documents
     */
    public Flux<LearnerSearchableDocument> publishSearchableDocuments(final LearnerElement learnerElement,
                                                                      final UUID cohortId) {

        affirmArgument(learnerElement != null, "learnerElement is required");
        affirmArgument(cohortId != null, "cohortId is required");

        // fetching the latest version
        // FIXME once BRNT-407 is merged in pluginVersionExpr could be a locked version and there is no
        // need to fetch the version
        final Mono<String> versionMono = pluginService.findLatestVersion(learnerElement.getPluginId(),
                learnerElement.getPluginVersionExpr());

        // fetching the settings
        final Mono<CohortSettings> settingsMono = cohortService.fetchCohortSettings(cohortId)
                // return an empty settings object when not found
                .defaultIfEmpty(new CohortSettings());
        // get the ancestry
        final Mono<List<CoursewareElement>> ancestryMono = getAncestry(learnerElement.getId());

        // zip the sources
        Flux<LearnerSearchableDocument> searchables = Flux.zip(versionMono, settingsMono, ancestryMono)
                .flatMap(tuple3 -> {
                    // prepare required variables
                    final String version = tuple3.getT1();
                    final CohortSettings cohortSettings = tuple3.getT2();
                    final List<CoursewareElement> ancestry = tuple3.getT3();

                    // find the searchable fields
                    return pluginGateway.fetchSearchableFieldByPlugin(learnerElement.getPluginId(), version)
                            // persist the searchable
                            .flatMap(pluginSearchableField -> persist(pluginSearchableField, ancestry, learnerElement,
                                    cohortId, cohortSettings.getProductId())
                                    .doOnEach(log.reactiveDebugSignal("document persisted for plugin searchable field"))
                                    // do not interrupt the persisting of following documents if this one is empty
                                    .defaultIfEmpty(new LearnerSearchableDocument()));
                })
                // filter out empty objects
                .filter(doc -> doc.getElementId() != null)
                .cache();

        if(csgConfig.isEnabled()) {
            return indexSearchableDocuments(searchables, identityProvider, csgConfig, learnerElement)
                    .thenMany(searchables);
        }
        return searchables;
    }

    /**
     * Create the learner searchable document from the plugin searchable field and persist it to the database.
     * The document is not created when either the config is null or all the selected values from the config are empty.
     *
     * @param pluginSearchableField the field to find the values in the config for
     * @param ancestry the element ancestry
     * @param learnerElement the learner element to persist a document for
     * @param cohortId the cohort id
     * @param productId the product id
     * @return a mono with the persisted document or empty when the doc is not created
     */
    private Mono<LearnerSearchableDocument> persist(final PluginSearchableField pluginSearchableField,
                                                    final List<CoursewareElement> ancestry,
                                                    final LearnerElement learnerElement,
                                                    final UUID cohortId,
                                                    final String productId) {

        if (learnerElement.getConfig() == null) {
            // there is nothing here, do not even attempt at creating a document
            return Mono.empty();
        }

        // select the fields
        LearnerSearchableFieldValue selected = learnerSearchableFieldSelector.select(pluginSearchableField,
                learnerElement.getConfig());

        // do not persist an empty document
        if (selected.isEmpty()) {
            return Mono.empty();
        }

        final List<UUID> ancestryIds = ancestry.stream().map(CoursewareElement::getElementId).collect(Collectors.toList());
        final List<String> ancestryStrings = ancestry.stream()
                .map(element -> element.getElementType() + ":" + element.getElementId()).collect(Collectors.toList());

        LearnerSearchableDocument learnerSearchableDocument = new LearnerSearchableDocument()
                .setContentType(pluginSearchableField.getContentType())
                .setBody(selected.getBody())
                .setTag(selected.getTag())
                .setPreview(selected.getPreview())
                .setSummary(selected.getSummary())
                .setSource(selected.getSource())
                .setDeploymentId(learnerElement.getDeploymentId())
                .setSearchableFieldId(pluginSearchableField.getId())
                .setChangeId(learnerElement.getChangeId())
                .setCohortId(cohortId)
                .setElementId(learnerElement.getId())
                .setElementType(learnerElement.getElementType())
                .setElementPath(ancestryIds)
                .setElementPathType(ancestryStrings)
                // We don't want to be inserting null values, so replace with empty string if null.
                .setProductId(productId != null ? productId : "");

        return learnerSearchableDocumentGateway.persistLearnerSearchable(learnerSearchableDocument)
                .singleOrEmpty()
                .thenReturn(learnerSearchableDocument);
    }

    private Mono<List<CoursewareElement>> getAncestry(final UUID elementId) {
        return coursewareService.findCoursewareElementAncestry(elementId)
                .map(CoursewareElementAncestry::getAncestry);
    }

    /**
     * Use ext_http module apparatus to submit a rest request to CSG and add the documents to the index.
     *
     * See https://pearsoneducationinc-my.sharepoint.com/:p:/r/personal/brian_weck_pearson_com/Documents/Engineering/Architecture%20Docs/aero%20External%20HTTP%20Push%20Events.pptx?d=w816a2f9112de42da94346832e5c512f8&csf=1&web=1&e=qlNLTi
     *
     * @param searchable document to be indexed in CSG
     * @param identityProvider Ies system to system identity provider
     * @param csgConfig csg config instance
     * @return Request notification with relevant ids and rendered list of parameters used for request
     *
     */
    private Mono<Exchange> indexSearchableDocuments(final Flux<LearnerSearchableDocument> searchable,
                                                    IesSystemToSystemIdentityProvider identityProvider,
                                                    CsgConfig csgConfig,
                                                    final LearnerElement learnerElement) {

        // prepare builder with common fields once piToken is available
        Mono<CsgIndexRequestBuilder> reqBuilder = identityProvider
                .getPiTokeReactive()
                .map(token -> new CsgIndexRequestBuilder()
                        .sethUri(csgConfig.getIndexUri())
                        .setPiToken(token)
                        .setApplicationId(this.csgConfig.getApplicationId())
                        .setDeploymentId(learnerElement.getDeploymentId())
                        .setChangeId(learnerElement.getChangeId()));

        return reqBuilder
                // collect all emitted LearnerSearchableDocument into same request builder
                .flatMap(builder -> searchable.reduce(builder, CsgIndexRequestBuilder::addLearnerDocument))
                // build request
                .flatMap(builder -> {
                    if (builder.isEmpty()) {
                        // skip making request if there is nothing to publish
                        return Mono.empty();
                    }
                    return Mono.just(builder);
                })
                // Just send it \m/
                .flatMap(builder -> Mono.from(camel.toStream(CSGIndexRoute.CSG_SUBMIT_REQUEST, builder)))
                .doOnEach(log.reactiveDebugSignal("sent searchable documents to csg index"));
    }

    /**
     * Remove from CSG index documents which were removed from current deployment, in relation to immediately previous
     * deployment version.
     *
     * @return
     */
    Mono<RequestNotification> pruneIndex(final UUID deploymentId){

        if(csgConfig.isEnabled()) {
            Mono<CsgDeleteRequestBuilder> reqBuilder = identityProvider
                    .getPiTokeReactive()
                    .map(token ->
                                 new CsgDeleteRequestBuilder()
                                         .sethUri(csgConfig.getIndexUri())
                                         .setPiToken(token)
                                         .setApplicationId(this.csgConfig.getApplicationId())
                    );

            return reqBuilder
                    .flatMap(builder -> findDeletedSearchableDocuments(deploymentId)
                            .map(builder::addLearnerDocuments)
                            .thenReturn(builder.build()))
                    .flatMap(request -> httpRequestService.submit(RequestPurpose.CSG_DELETE, request, null))
                    .doOnEach(log.reactiveDebugSignal("sent searchable documents csg DELETE request"));
        } else {
            return Mono.empty();
        }
    }

    /**
     * Lists all published LearnerSearchableDocuments and compares the ones present in the current changeId with
     * the documents present in immediate previous changeId in order to determine if any element was removed from
     * the courseware.
     *
     * These documents need to be removed from CSG index.
     *
     * @param deploymentId deploymentId where search will happen
     * @return a Set containing all the ElementId of searc
     */
    @VisibleForTesting
    Mono<Set<LearnerSearchableDocumentIdentity>> findDeletedSearchableDocuments(final UUID deploymentId) {

        // Find the latest 2 changeIds: first one is most recent, second is the publishing previous
        return deploymentGateway.findLatestChangeIds(deploymentId, 2)
                .collectList()
                .flatMap(latestChangeIds -> {

                    // If there's no 2 changeIds, it is still first deployment and checking for deleted elemements in
                    // between deployments can be skipped
                    if (latestChangeIds.size() < 2) {
                        return Mono.empty();
                    }

                    // Get all searchable documents in deployment and keep only the ones present in latest 2 changeIds
                    return learnerSearchableDocumentGateway.fetchElementIds(deploymentId)
                            .filter(identity -> latestChangeIds.contains(identity.getChangeId()))
                            // Split elements by changeId into accumulator pojo. Simpler to reduce than use a .groupBy()
                            .reduce(new LearnerSearchableDocumentDeploymentDiff(),
                                    (accumulator, searchableIdentity) -> {

                                        UUID changeId = searchableIdentity.getChangeId();
                                        // changeId is the latest
                                        if (changeId.equals(latestChangeIds.get(0))) {
                                            accumulator
                                                    .getCurrentDeploymentElements()
                                                    .add(searchableIdentity);
                                        // changeId is the previous one
                                        } else {
                                            accumulator
                                                    .getPreviousDeploymentElements()
                                                    .add(searchableIdentity);
                                        }
                                        return accumulator;
                                    });

                })
                .map(LearnerSearchableDocumentDeploymentDiff::getDiff);
    }
}
