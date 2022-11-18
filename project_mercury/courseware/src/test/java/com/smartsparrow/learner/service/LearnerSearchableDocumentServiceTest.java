package com.smartsparrow.learner.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.HttpMethod;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementAncestry;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.wiring.CsgConfig;
import com.smartsparrow.ext_http.service.ExternalHttpRequestService;
import com.smartsparrow.ext_http.service.Request;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.ext_http.service.RequestPurpose;
import com.smartsparrow.iam.wiring.IesSystemToSystemIdentityProvider;
import com.smartsparrow.learner.data.DeploymentGateway;
import com.smartsparrow.learner.data.LearnerElement;
import com.smartsparrow.learner.data.LearnerSearchableDocument;
import com.smartsparrow.learner.data.LearnerSearchableDocumentGateway;
import com.smartsparrow.learner.data.LearnerSearchableDocumentIdentity;
import com.smartsparrow.learner.route.CSGIndexRoute;
import com.smartsparrow.learner.searchable.CsgIndexRequestBuilder;
import com.smartsparrow.learner.searchable.LearnerSearchableFieldSelector;
import com.smartsparrow.learner.searchable.LearnerSearchableFieldValue;
import com.smartsparrow.plugin.data.PluginGateway;
import com.smartsparrow.plugin.data.PluginSearchableField;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class LearnerSearchableDocumentServiceTest {

    @InjectMocks
    private LearnerSearchableDocumentService learnerSearchableDocumentService;

    @Mock
    private LearnerSearchableDocumentGateway learnerSearchableDocumentGateway;

    @Mock
    private PluginGateway pluginGateway;

    @Mock
    private PluginService pluginService;

    @Mock
    private CohortService cohortService;

    @Mock
    private LearnerSearchableFieldSelector learnerSearchableFieldSelector;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private LearnerElement learnerElement;

    @Mock
    private LearnerSearchableFieldValue selected;

    @Mock
    private IesSystemToSystemIdentityProvider identityProvider;

    @Mock
    CsgConfig csgConfig;

    @Mock
    DeploymentGateway deploymentGateway;

    @Mock
    private ExternalHttpRequestService httpRequestService;

    @Mock
    private CamelReactiveStreamsService camel;

    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID parentId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID previousChangeId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String version = "1.2.1";
    private static final String versionExpr = "1.*";
    private static final String config = "{\"some\":\"config\"}";
    private static final String productId = "x-urn:bronte:12345tf";
    private static final String piToken = "respect mah authoritah";
    private static final String applicationId = "bronte";
    private static final String uri = "http://domain.tld/whatever";

    @BeforeEach
    void setUp(){
        MockitoAnnotations.initMocks(this);

        when(learnerElement.getId()).thenReturn(elementId);
        when(learnerElement.getDeploymentId()).thenReturn(deploymentId);
        when(learnerElement.getChangeId()).thenReturn(changeId);
        when(learnerElement.getPluginId()).thenReturn(pluginId);
        when(learnerElement.getPluginVersionExpr()).thenReturn(versionExpr);
        when(learnerElement.getElementType()).thenReturn(CoursewareElementType.INTERACTIVE);
        when(learnerElement.getConfig()).thenReturn(config);

        when(identityProvider.getPiTokeReactive()).thenReturn(Mono.just(piToken));

        when(pluginService.findLatestVersion(pluginId, versionExpr))
                .thenReturn(Mono.just(version));

        when(cohortService.fetchCohortSettings(cohortId))
                .thenReturn(Mono.just(new CohortSettings()
                        .setProductId(productId)));

        when(coursewareService.findCoursewareElementAncestry(elementId))
                .thenReturn(Mono.just(new CoursewareElementAncestry()
                        .setElementId(elementId)
                        .setType(CoursewareElementType.INTERACTIVE)
                        .setAncestry(Lists.newArrayList(CoursewareElement.from(parentId, CoursewareElementType.PATHWAY)))));

        when(pluginGateway.fetchSearchableFieldByPlugin(pluginId, version))
                .thenReturn(Flux.just(
                        new PluginSearchableField()
                                .setId(UUID.randomUUID())
                                .setContentType("contentType")
                                .setSummary(Sets.newLinkedHashSet("some")),
                        new PluginSearchableField()
                                .setId(UUID.randomUUID())
                                .setContentType("contentType2")
                                .setBody(Sets.newTreeSet("some"))
                        ));


        when(learnerSearchableFieldSelector.select(any(PluginSearchableField.class), anyString()))
                .thenReturn(selected);

        when(selected.isEmpty()).thenReturn(false);
        when(selected.getSummary()).thenReturn("the summary");
        when(selected.getBody()).thenReturn("the body");
        when(learnerSearchableDocumentGateway.persistLearnerSearchable(any(LearnerSearchableDocument.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(csgConfig.getApplicationId()).thenReturn(applicationId);
        when(csgConfig.getIndexUri()).thenReturn(uri);
        when(csgConfig.isEnabled()).thenReturn(true);

        when(httpRequestService.submit(any(RequestPurpose.class), any(Request.class), isNull()))
                .thenReturn(Mono.just(new RequestNotification()));

        when(deploymentGateway.findLatestChangeIds(eq(deploymentId), eq(2)))
                .thenReturn(Flux.just(changeId, previousChangeId));

    }

    @Test
    void persistSearchable_nullConfig() {
        when(learnerElement.getConfig()).thenReturn(null);

        List<LearnerSearchableDocument> persisted = learnerSearchableDocumentService.publishSearchableDocuments(learnerElement, cohortId)
                .collectList()
                .block();

        assertNotNull(persisted);
        assertEquals(0, persisted.size());

        verify(learnerSearchableDocumentGateway, never()).persistLearnerSearchable(any(LearnerSearchableDocument.class));
        verify(httpRequestService, never()).submit(any(RequestPurpose.class), any(Request.class), any());
    }

    @Test
    void persistSearchable_nullProductId() {
        when(cohortService.fetchCohortSettings(cohortId))
                .thenReturn(Mono.just(new CohortSettings()));

        ArgumentCaptor<LearnerSearchableDocument> captor = ArgumentCaptor.forClass(LearnerSearchableDocument.class);

        TestPublisher<Exchange> testPublisher = TestPublisher.create();
        testPublisher.next(mock(Exchange.class));
        when(camel.toStream(eq(CSGIndexRoute.CSG_SUBMIT_REQUEST), any(CsgIndexRequestBuilder.class)))
                .thenReturn(testPublisher.complete());

        List<LearnerSearchableDocument> persisted = learnerSearchableDocumentService.publishSearchableDocuments(learnerElement, cohortId)
                .collectList()
                .block();

        assertNotNull(persisted);
        assertEquals(2, persisted.size());

        verify(learnerSearchableDocumentGateway, times(2)).persistLearnerSearchable(captor.capture());

        final List<LearnerSearchableDocument> captured = captor.getAllValues();

        assertAll(() -> {
            assertNotNull(captured);
            assertEquals(2, captured.size());
            assertEquals(captured, persisted);

            LearnerSearchableDocument first = persisted.get(0);

            assertNotNull(first);
            assertEquals(deploymentId, first.getDeploymentId());
            assertEquals(cohortId, first.getCohortId());
            assertEquals(changeId, first.getChangeId());
            assertEquals("", first.getProductId());
            assertEquals(elementId, first.getElementId());
            assertEquals(CoursewareElementType.INTERACTIVE, first.getElementType());
            assertNotNull(first.getElementPath());
            assertEquals(1, first.getElementPath().size());
        });
    }

    @Test
    void persistSearchable_emptyDocument() {
        when(selected.isEmpty()).thenReturn(true);

        List<LearnerSearchableDocument> persisted = learnerSearchableDocumentService.publishSearchableDocuments(learnerElement, cohortId)
                .collectList()
                .block();

        assertNotNull(persisted);
        assertEquals(0, persisted.size());

        verify(learnerSearchableDocumentGateway, never()).persistLearnerSearchable(any(LearnerSearchableDocument.class));
        verify(httpRequestService, never()).submit(any(RequestPurpose.class), any(Request.class), any());
    }

    @Test
    void persistSearchable() throws IOException {
        ArgumentCaptor<LearnerSearchableDocument> captor = ArgumentCaptor.forClass(LearnerSearchableDocument.class);

        TestPublisher<Exchange> testPublisher = TestPublisher.create();
        testPublisher.next(mock(Exchange.class));
        when(camel.toStream(eq(CSGIndexRoute.CSG_SUBMIT_REQUEST), any(CsgIndexRequestBuilder.class)))
                .thenReturn(testPublisher.complete());

        List<LearnerSearchableDocument> persisted = learnerSearchableDocumentService
                .publishSearchableDocuments(learnerElement, cohortId)
                .collectList()
                .block();

        assertNotNull(persisted);
        assertEquals(2, persisted.size());

        verify(learnerSearchableDocumentGateway, times(2)).persistLearnerSearchable(captor.capture());

        final List<LearnerSearchableDocument> captured = captor.getAllValues();

        assertAll(() -> {
            assertNotNull(captured);
            assertEquals(2, captured.size());
            assertEquals(captured, persisted);

            LearnerSearchableDocument first = persisted.get(0);

            assertNotNull(first);
            assertEquals(deploymentId, first.getDeploymentId());
            assertEquals(cohortId, first.getCohortId());
            assertEquals(changeId, first.getChangeId());
            assertEquals(productId, first.getProductId());
            assertEquals(elementId, first.getElementId());
            assertEquals(CoursewareElementType.INTERACTIVE, first.getElementType());
            assertNotNull(first.getElementPath());
            assertEquals(1, first.getElementPath().size());
        });
    }

    @Test
    void pruneIndex() {
        LearnerSearchableDocumentIdentity del1 = getRandomSearchableIdentity(previousChangeId, UUIDs.timeBased());
        LearnerSearchableDocumentIdentity del2 = getRandomSearchableIdentity(previousChangeId, UUIDs.timeBased());
        LearnerSearchableDocumentIdentity notDel = getRandomSearchableIdentity(previousChangeId, UUIDs.timeBased());
        LearnerSearchableDocumentIdentity id1 = getRandomSearchableIdentity(changeId, UUIDs.timeBased());
        LearnerSearchableDocumentIdentity id2 = getRandomSearchableIdentity(changeId, notDel.getElementId());
        when(learnerSearchableDocumentGateway.fetchElementIds(eq(deploymentId)))
                .thenReturn(Flux.just(del1, del2, notDel, id1, id2));

        ArgumentCaptor<Request> reqCaptor = ArgumentCaptor.forClass(Request.class);

        learnerSearchableDocumentService.pruneIndex(deploymentId).block();

        verify(httpRequestService, times(1)).submit(any(RequestPurpose.class), reqCaptor.capture(), any());
        Request reqCaptured = reqCaptor.getValue();
        ObjectNode received = reqCaptured.getParams();
        assertAll(() -> {
            assertEquals(csgConfig.getIndexUri(), received.get("uri").asText());
            assertEquals(HttpMethod.DELETE, received.get("method").asText());
            assertTrue(received.get("json").asBoolean());
            assertTrue(String.format("[%s, %s]", del1.getId(), del2.getId()).equals(received.get("body").asText())
                               ||
                               String.format("[%s, %s]", del2.getId(), del1.getId()).equals(received.get("body").asText())
            );
        });
    }

    @Test
    void findDeletedSearchableDocuments() {

        LearnerSearchableDocumentIdentity idcCur1 = getRandomSearchableIdentity(changeId, UUIDs.timeBased());
        LearnerSearchableDocumentIdentity idcCur2 = getRandomSearchableIdentity(changeId, UUIDs.timeBased());
        LearnerSearchableDocumentIdentity idcPrev1 = getRandomSearchableIdentity(previousChangeId, idcCur1.getElementId());
        LearnerSearchableDocumentIdentity idcPrev2 = getRandomSearchableIdentity(previousChangeId, idcCur2.getElementId());
        LearnerSearchableDocumentIdentity idcPrev3 = getRandomSearchableIdentity(previousChangeId, UUIDs.timeBased());
        LearnerSearchableDocumentIdentity idcPrev4 = getRandomSearchableIdentity(previousChangeId, UUIDs.timeBased());
        LearnerSearchableDocumentIdentity nopeId1 = getRandomSearchableIdentity(UUIDs.timeBased(), UUIDs.timeBased());
        LearnerSearchableDocumentIdentity nopeId2 = getRandomSearchableIdentity(UUIDs.timeBased(), UUIDs.timeBased());
        LearnerSearchableDocumentIdentity nopeId3 = getRandomSearchableIdentity(UUIDs.timeBased(), UUIDs.timeBased());

        when(learnerSearchableDocumentGateway.fetchElementIds(eq(deploymentId)))
                .thenReturn(Flux.just(idcCur1, nopeId1, idcCur1, nopeId2, nopeId3, idcPrev1, idcPrev2, idcCur2,
                                      idcPrev4, idcPrev3));

        Set<LearnerSearchableDocumentIdentity> res = learnerSearchableDocumentService
                .findDeletedSearchableDocuments(deploymentId).block();
        assertNotNull(res);
        assertEquals(2, res.size());
        assertTrue(res.contains(idcPrev3));
        assertTrue(res.contains(idcPrev4));
    }

    private LearnerSearchableDocumentIdentity getRandomSearchableIdentity(final UUID previousChangeId,
                                                                          final UUID elementId) {
        return new LearnerSearchableDocumentIdentity()
                .setDeploymentId(deploymentId)
                .setChangeId(previousChangeId)
                .setElementId(elementId);
    }

    @Test
    void findDeletedSearchableDocuments_lessThan2ChangeIds() {
        when(deploymentGateway.findLatestChangeIds(eq(deploymentId), eq(2)))
                .thenReturn(Flux.just(changeId));

        // Stupid spotBugs went crazy thinking there would be NPEs here, despite nulls being illegal in Mono
        Mono<Set<LearnerSearchableDocumentIdentity>> res = learnerSearchableDocumentService
                .findDeletedSearchableDocuments(deploymentId);
        assertNotNull(res);
        Mono<Boolean> val = res.hasElement();
        assertNotNull(val);
        assertFalse(val.block());
    }
}
