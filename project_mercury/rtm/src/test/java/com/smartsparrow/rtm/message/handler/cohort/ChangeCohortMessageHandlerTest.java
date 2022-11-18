package com.smartsparrow.rtm.message.handler.cohort;


import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.cohort.ChangeCohortMessageHandler.WORKSPACE_COHORT_CHANGE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.inject.Provider;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.eventmessage.CohortSummaryBroadcastMessage;
import com.smartsparrow.cohort.payload.CohortPayload;
import com.smartsparrow.cohort.payload.CohortSettingsPayload;
import com.smartsparrow.cohort.payload.CohortSummaryPayload;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.cohort.service.LtiConsumerCredential;
import com.smartsparrow.cohort.wiring.LTIConfig;
import com.smartsparrow.exception.DateTimeFault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.cohort.ChangeCohortMessage;
import com.smartsparrow.rtm.subscription.cohort.changed.CohortChangedRTMProducer;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

class ChangeCohortMessageHandlerTest {

    private ChangeCohortMessageHandler changeCohortMessageHandler;
    @Mock
    private CohortService cohortService;
    private Session session;
    private Provider<RTMClientContext> rtmClientContextProvider;
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    private RTMEventBroker rtmEventBroker;
    private RTMClientContext rtmClientContext;

    @Mock
    private WorkspaceService workspaceService;
    @Mock
    private CohortChangedRTMProducer cohortChangedRTMProducer;

    @Mock
    private LTIConfig ltiConfig;

    UUID workspaceId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> brokerProvider = RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);
        rtmEventBrokerProvider = brokerProvider.getT1();
        rtmEventBroker = brokerProvider.getT2();
        Tuple2<Provider<RTMClientContext>, RTMClientContext> clientProvider = RTMWebSocketTestUtils.mockProvidedClass(RTMClientContext.class);
        rtmClientContextProvider = clientProvider.getT1();
        rtmClientContext = clientProvider.getT2();
        session = RTMWebSocketTestUtils.mockSession();

        changeCohortMessageHandler = new ChangeCohortMessageHandler(cohortService, rtmClientContextProvider, rtmEventBrokerProvider, workspaceService, cohortChangedRTMProducer,
                                                                    ltiConfig);

        Workspace w = new Workspace().setId(workspaceId).setSubscriptionId(UUIDs.timeBased());
        when(workspaceService.fetchById(workspaceId)).thenReturn(Mono.just(w));
    }

    @Test
    void validate_noCohortId() {
        RTMValidationException t = assertThrows(RTMValidationException.class, () -> changeCohortMessageHandler.validate(new ChangeCohortMessage()));
        assertEquals("cohortId is required", t.getErrorMessage());
        assertEquals(400, t.getStatusCode());
        assertEquals("workspace.cohort.change.error", t.getType());
    }

    @Test
    void validate_emptyProductId() {
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);
        when(message.getCohortId()).thenReturn(UUID.randomUUID());
        when(message.getName()).thenReturn("a name");
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.OPEN);
        when(message.getProductId()).thenReturn("");

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> changeCohortMessageHandler.validate(message));
        assertEquals("productId must not be empty", f.getMessage());
    }

    @Test
    void validate_noName() {
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);
        when(message.getCohortId()).thenReturn(UUID.randomUUID());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> changeCohortMessageHandler.validate(message));
        assertEquals("name is required", t.getErrorMessage());
        assertEquals(400, t.getStatusCode());
        assertEquals("workspace.cohort.change.error", t.getType());
    }

    @Test
    void validate_noType() {
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);
        when(message.getCohortId()).thenReturn(UUID.randomUUID());
        when(message.getName()).thenReturn("name");

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> changeCohortMessageHandler.validate(message));
        assertEquals("enrollmentType is required", t.getErrorMessage());
        assertEquals(400, t.getStatusCode());
        assertEquals("workspace.cohort.change.error", t.getType());
    }

    @Test
    void validate_invalidType() {
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);
        when(message.getCohortId()).thenReturn(UUID.randomUUID());
        when(message.getName()).thenReturn("name");
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.EXTENSION);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> changeCohortMessageHandler.validate(message));

        assertEquals("enrollmentType not supported", t.getMessage());
    }

    @Test
    void handle_successUpdate() throws IOException {
        ArgumentCaptor<CohortSummaryBroadcastMessage> broadcastMessageArgumentCaptor = ArgumentCaptor
                .forClass(CohortSummaryBroadcastMessage.class);

        UUID cohortId = UUID.randomUUID();
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);
        when(message.getCohortId()).thenReturn(cohortId);
        when(message.getName()).thenReturn("name");
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.OPEN);
        when(message.getStartDate()).thenReturn("Wed, 22 Aug 2018 07:15:00 GMT");
        when(message.getEndDate()).thenReturn("Sat, 22 Sep 2018 07:15:00 GMT");
        when(message.getColor()).thenReturn("red");
        when(message.getBannerImage()).thenReturn("bannerImage");
        when(message.getBannerPattern()).thenReturn("bannerPattern");
        when(message.getProductId()).thenReturn("A103000103955");

        when(cohortService.updateCohort(any(), any())).thenReturn(Flux.empty());

        CohortPayload cohortPayload = new CohortPayload()
                .setSummaryPayload(new CohortSummaryPayload().setCohortId(cohortId).setName("name"))
                .setSettingsPayload(new CohortSettingsPayload().setColor("red"));
        when(cohortService.getCohortPayload(eq(cohortId))).thenReturn(Mono.just(cohortPayload));
        when(cohortChangedRTMProducer.buildCohortChangedRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortChangedRTMProducer);

        changeCohortMessageHandler.handle(session, message);

        ArgumentCaptor<CohortSummary> summaryCaptor = ArgumentCaptor.forClass(CohortSummary.class);
        ArgumentCaptor<CohortSettings> settingsCaptor = ArgumentCaptor.forClass(CohortSettings.class);
        verify(cohortService).updateCohort(summaryCaptor.capture(), settingsCaptor.capture());

        assertEquals("name", summaryCaptor.getValue().getName());
        assertEquals(cohortId, summaryCaptor.getValue().getId());
        assertEquals(EnrollmentType.OPEN, summaryCaptor.getValue().getType());
        assertEquals(Long.valueOf(1534922100000L), summaryCaptor.getValue().getStartDate());
        assertEquals(Long.valueOf(1537600500000L), summaryCaptor.getValue().getEndDate());
        assertEquals("red", settingsCaptor.getValue().getColor());
        assertEquals("bannerImage", settingsCaptor.getValue().getBannerImage());
        assertEquals("bannerPattern", settingsCaptor.getValue().getBannerPattern());
        assertEquals("A103000103955", settingsCaptor.getValue().getProductId());

        verify(rtmEventBroker).broadcast(eq(WORKSPACE_COHORT_CHANGE), broadcastMessageArgumentCaptor.capture());
        CohortSummaryBroadcastMessage broadcastMessage = broadcastMessageArgumentCaptor.getValue();
        assertNotNull(broadcastMessage);
        assertNotNull(broadcastMessage.getCohortId());
        assertEquals(cohortId, broadcastMessage.getCohortId());
        verify(cohortChangedRTMProducer).buildCohortChangedRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortChangedRTMProducer).produce();
    }

    @Test
    void handle_successPayload() throws IOException {
        ArgumentCaptor<CohortSummaryBroadcastMessage> broadcastMessageArgumentCaptor = ArgumentCaptor
                .forClass(CohortSummaryBroadcastMessage.class);

        UUID cohortId = UUID.randomUUID();
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);
        when(message.getCohortId()).thenReturn(cohortId);

        CohortPayload cohortPayload = new CohortPayload()
                .setSummaryPayload(new CohortSummaryPayload().setCohortId(cohortId).setName("name"))
                .setSettingsPayload(new CohortSettingsPayload().setColor("red"));

        when(cohortService.updateCohort(any(), any())).thenReturn(Flux.empty());
        when(cohortService.getCohortPayload(eq(cohortId))).thenReturn(Mono.just(cohortPayload));
        when(cohortChangedRTMProducer.buildCohortChangedRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortChangedRTMProducer);

        changeCohortMessageHandler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals("workspace.cohort.change.ok", response.getType());
            Map responseMap = (Map) response.getResponse().get("cohort");
            Map summaryMap = (Map) responseMap.get("summary");
            assertEquals(2, summaryMap.size());
            assertEquals("name", summaryMap.get("name"));
            assertEquals(cohortId.toString(), summaryMap.get("cohortId"));
            Map settingsMap = (Map) responseMap.get("settings");
            assertEquals(1, settingsMap.size());
            assertEquals("red", settingsMap.get("color"));
        }));

        verify(rtmEventBroker).broadcast(eq(WORKSPACE_COHORT_CHANGE), broadcastMessageArgumentCaptor.capture());
        CohortSummaryBroadcastMessage broadcastMessage = broadcastMessageArgumentCaptor.getValue();
        assertNotNull(broadcastMessage);
        assertNotNull(broadcastMessage.getCohortId());
        assertEquals(cohortId, broadcastMessage.getCohortId());
        verify(cohortChangedRTMProducer).buildCohortChangedRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortChangedRTMProducer).produce();
    }

    @Test
    void validate_invalidStartDate() {
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);
        when(message.getStartDate()).thenReturn("invalid");

        DateTimeFault e = assertThrows(DateTimeFault.class, () -> changeCohortMessageHandler.validate(message));
        assertNotNull(e);
        assertEquals(e.getMessage(),"Invalid startDate");
    }

    @Test
    void validate_invalidEndDate() throws IOException {
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);
        when(message.getEndDate()).thenReturn("invalid");

        DateTimeFault e = assertThrows(DateTimeFault.class, () -> changeCohortMessageHandler.validate(message));
        assertNotNull(e);
        assertEquals(e.getMessage(),"Invalid endDate");
    }

    @Test
    void validate_invalidWorkspaceIdForLtiEnrollmentType() throws IOException {
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);

        when(message.getCohortId()).thenReturn(UUID.randomUUID());
        when(message.getName()).thenReturn("name");
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.LTI);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> changeCohortMessageHandler.validate(message));

        assertEquals("workspaceId is required", t.getErrorMessage());
    }

    @Test
    void validate_invalidNoCredentialForLtiEnrollmentType() {
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);

        when(message.getCohortId()).thenReturn(UUID.randomUUID());
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.LTI);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getName()).thenReturn("name");

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> changeCohortMessageHandler.validate(message));

        assertEquals("LTI consumer credential object is required", t.getErrorMessage());
    }

    @Test
    void validate_invalidNoCredentialKeyForLtiEnrollmentType() {
        LtiConsumerCredential ltiConsumerCredential = mock(LtiConsumerCredential.class);
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);

        when(message.getCohortId()).thenReturn(UUID.randomUUID());
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.LTI);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getName()).thenReturn("name");

        when(message.getLtiConsumerCredential()).thenReturn(ltiConsumerCredential);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> changeCohortMessageHandler.validate(message));

        assertEquals("consumer key is required", t.getErrorMessage());
    }

    @Test
    void handle_successAddCredentialForLtiEnrollmentType() throws IOException {
        ArgumentCaptor<CohortSummaryBroadcastMessage> broadcastMessageArgumentCaptor = ArgumentCaptor
                .forClass(CohortSummaryBroadcastMessage.class);

        String ltiKey = "ltiKey";
        String ltiSecrect = "ltiSecret";

        LtiConsumerCredential ltiConsumerCredential = new LtiConsumerCredential()
                .setKey(ltiKey)
                .setSecret(ltiSecrect);

        UUID cohortId = UUID.randomUUID();
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);
        when(message.getCohortId()).thenReturn(cohortId);
        when(message.getName()).thenReturn("name");
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.LTI);
        when(message.getStartDate()).thenReturn("Wed, 22 Aug 2018 07:15:00 GMT");
        when(message.getEndDate()).thenReturn("Sat, 22 Sep 2018 07:15:00 GMT");
        when(message.getColor()).thenReturn("red");
        when(message.getBannerImage()).thenReturn("bannerImage");
        when(message.getBannerPattern()).thenReturn("bannerPattern");
        when(message.getProductId()).thenReturn("A103000103955");
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getLtiConsumerCredential()).thenReturn(ltiConsumerCredential);

        when(cohortService.fetchLtiConsumerKeys(eq(workspaceId), eq(cohortId))).thenReturn(Mono.just(Arrays.asList()));
        when(cohortService.saveLTIConsumerKey(any(), eq(ltiKey), eq(ltiSecrect))).thenReturn(Mono.empty());
        when(cohortService.updateCohort(any(), any())).thenReturn(Flux.empty());
        when(cohortService.getCohortPayload(eq(cohortId))).thenReturn(Mono.just(new CohortPayload()));
        when(cohortChangedRTMProducer.buildCohortChangedRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortChangedRTMProducer);
        when(ltiConfig.getKey()).thenReturn("ltiKey");
        when(ltiConfig.getSecret()).thenReturn("ltiSecret");

        changeCohortMessageHandler.handle(session, message);

        ArgumentCaptor<CohortSummary> summaryCaptor = ArgumentCaptor.forClass(CohortSummary.class);
        ArgumentCaptor<CohortSettings> settingsCaptor = ArgumentCaptor.forClass(CohortSettings.class);

        verify(cohortService).fetchLtiConsumerKeys(eq(workspaceId), eq(cohortId));
        verify(cohortService).updateCohort(summaryCaptor.capture(), settingsCaptor.capture());
        verify(cohortService).saveLTIConsumerKey(eq(summaryCaptor.getValue()), eq(ltiKey), eq(ltiSecrect));

        assertEquals("name", summaryCaptor.getValue().getName());
        assertEquals(cohortId, summaryCaptor.getValue().getId());
        assertEquals(EnrollmentType.LTI, summaryCaptor.getValue().getType());
        assertEquals(Long.valueOf(1534922100000L), summaryCaptor.getValue().getStartDate());
        assertEquals(Long.valueOf(1537600500000L), summaryCaptor.getValue().getEndDate());
        assertEquals("red", settingsCaptor.getValue().getColor());
        assertEquals("bannerImage", settingsCaptor.getValue().getBannerImage());
        assertEquals("bannerPattern", settingsCaptor.getValue().getBannerPattern());
        assertEquals("A103000103955", settingsCaptor.getValue().getProductId());

        verify(rtmEventBroker).broadcast(eq(WORKSPACE_COHORT_CHANGE), broadcastMessageArgumentCaptor.capture());
        CohortSummaryBroadcastMessage broadcastMessage = broadcastMessageArgumentCaptor.getValue();
        assertNotNull(broadcastMessage);
        assertNotNull(broadcastMessage.getCohortId());
        assertEquals(cohortId, broadcastMessage.getCohortId());
        verify(cohortChangedRTMProducer).buildCohortChangedRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortChangedRTMProducer).produce();
    }

    @Test
    void handle_successUpdate_noAddCredentialForLtiEnrollmentType() throws IOException {
        ArgumentCaptor<CohortSummaryBroadcastMessage> broadcastMessageArgumentCaptor = ArgumentCaptor
                .forClass(CohortSummaryBroadcastMessage.class);

        String ltiKey = "ltiKey";
        String ltiSecrect = "ltiSecret";

        LtiConsumerCredential ltiConsumerCredential = new LtiConsumerCredential()
                .setKey(ltiKey)
                .setSecret(ltiSecrect);

        UUID cohortId = UUID.randomUUID();
        ChangeCohortMessage message = mock(ChangeCohortMessage.class);
        when(message.getCohortId()).thenReturn(cohortId);
        when(message.getName()).thenReturn("name");
        when(message.getEnrollmentType()).thenReturn(EnrollmentType.LTI);
        when(message.getStartDate()).thenReturn("Wed, 22 Aug 2018 07:15:00 GMT");
        when(message.getEndDate()).thenReturn("Sat, 22 Sep 2018 07:15:00 GMT");
        when(message.getColor()).thenReturn("red");
        when(message.getBannerImage()).thenReturn("bannerImage");
        when(message.getBannerPattern()).thenReturn("bannerPattern");
        when(message.getProductId()).thenReturn("A103000103955");
        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(message.getLtiConsumerCredential()).thenReturn(ltiConsumerCredential);

        when(cohortService.fetchLtiConsumerKeys(eq(workspaceId), eq(cohortId))).thenReturn(Mono.just(Arrays.asList(ltiConsumerCredential)));
        when(cohortService.saveLTIConsumerKey(any(), eq(ltiKey), eq(ltiSecrect))).thenReturn(Mono.empty());
        when(cohortService.updateCohort(any(), any())).thenReturn(Flux.empty());
        when(cohortService.getCohortPayload(eq(cohortId))).thenReturn(Mono.just(new CohortPayload()));
        when(cohortChangedRTMProducer.buildCohortChangedRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortChangedRTMProducer);

        changeCohortMessageHandler.handle(session, message);

        ArgumentCaptor<CohortSummary> summaryCaptor = ArgumentCaptor.forClass(CohortSummary.class);
        ArgumentCaptor<CohortSettings> settingsCaptor = ArgumentCaptor.forClass(CohortSettings.class);

        verify(cohortService).fetchLtiConsumerKeys(eq(workspaceId), eq(cohortId));
        verify(cohortService).updateCohort(summaryCaptor.capture(), settingsCaptor.capture());
        verify(cohortService, never()).saveLTIConsumerKey(eq(summaryCaptor.getValue()), eq(ltiKey), eq(ltiSecrect));

        assertEquals("name", summaryCaptor.getValue().getName());
        assertEquals(cohortId, summaryCaptor.getValue().getId());
        assertEquals(EnrollmentType.LTI, summaryCaptor.getValue().getType());
        assertEquals(Long.valueOf(1534922100000L), summaryCaptor.getValue().getStartDate());
        assertEquals(Long.valueOf(1537600500000L), summaryCaptor.getValue().getEndDate());
        assertEquals("red", settingsCaptor.getValue().getColor());
        assertEquals("bannerImage", settingsCaptor.getValue().getBannerImage());
        assertEquals("bannerPattern", settingsCaptor.getValue().getBannerPattern());
        assertEquals("A103000103955", settingsCaptor.getValue().getProductId());

        verify(rtmEventBroker).broadcast(eq(WORKSPACE_COHORT_CHANGE), broadcastMessageArgumentCaptor.capture());
        CohortSummaryBroadcastMessage broadcastMessage = broadcastMessageArgumentCaptor.getValue();
        assertNotNull(broadcastMessage);
        assertNotNull(broadcastMessage.getCohortId());
        assertEquals(cohortId, broadcastMessage.getCohortId());
        verify(cohortChangedRTMProducer).buildCohortChangedRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortChangedRTMProducer).produce();
    }
}
