package com.smartsparrow.rtm.message.handler.courseware.pathway;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.pathway.PathwayMock.mockPathway;
import static com.smartsparrow.rtm.message.handler.courseware.pathway.CreatePathwayMessageHandler.AUTHOR_PATHWAY_CREATE_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.PathwayConfig;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.PreloadPathway;
import com.smartsparrow.courseware.payload.PathwayPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.pathway.CreatePathwayMessage;
import com.smartsparrow.rtm.subscription.courseware.created.PathwayCreatedRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

class CreatePathwayMessageHandlerTest {

    private CreatePathwayMessageHandler handler;

    @Mock
    private PathwayService pathwayService;
    @Mock
    private ActivityService activityService;
    @Mock
    private CoursewareService coursewareService;
    @Mock
    private Account account;
    @Mock
    private PathwayCreatedRTMProducer pathwayCreatedRTMProducer;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;

    private RTMEventBroker rtmEventBroker;
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> mockRTMEventBroker =
                RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);

        rtmEventBroker = mockRTMEventBroker.getT2();
        Provider<RTMEventBroker> rtmEventBrokerProvider = mockRTMEventBroker.getT1();

        Tuple2<Provider<AuthenticationContext>, AuthenticationContext> mockAuthenticationContextProvider = RTMWebSocketTestUtils
                .mockProvidedClass(AuthenticationContext.class);

        Provider<AuthenticationContext> authenticationContextProvider = mockAuthenticationContextProvider.getT1();
        AuthenticationContext authenticationContext = mockAuthenticationContextProvider.getT2();

        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(coursewareService.getRootElementId(activityId, ACTIVITY)).thenReturn(Mono.just(rootElementId));
        when(pathwayCreatedRTMProducer.buildPathwayCreatedRTMConsumable(rtmClientContext, rootElementId, pathwayId))
                .thenReturn(pathwayCreatedRTMProducer);

        handler = new CreatePathwayMessageHandler(authenticationContextProvider,
                                                  pathwayService,
                                                  activityService,
                                                  coursewareService,
                                                  rtmEventBrokerProvider,
                                                  rtmClientContextProvider,
                                                  pathwayCreatedRTMProducer);
    }

    @Test
    void validate_noActivityId() {
        CreatePathwayMessage message = mock(CreatePathwayMessage.class);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(AUTHOR_PATHWAY_CREATE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("missing activityId", ex.getErrorMessage());
    }

    @Test
    void validate_noPathwayType() {
        CreatePathwayMessage message = mock(CreatePathwayMessage.class);
        when(message.getActivityId()).thenReturn(activityId);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(AUTHOR_PATHWAY_CREATE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("missing pathwayType", ex.getErrorMessage());
    }

    @Test
    void validate_activityNotFound() {
        CreatePathwayMessage message = mock(CreatePathwayMessage.class);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getPathwayType()).thenReturn(PathwayType.LINEAR);
        when(activityService.findById(message.getActivityId())).thenThrow(new ActivityNotFoundException(activityId));

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals(AUTHOR_PATHWAY_CREATE_ERROR, ex.getType());
        assertEquals(400, ex.getStatusCode());
        assertEquals("invalid activityId", ex.getErrorMessage());
    }

    @Test
    void handle() throws IOException {
        CreatePathwayMessage message = mock(CreatePathwayMessage.class);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getPathwayType()).thenReturn(PathwayType.LINEAR);
        when(message.getPathwayId()).thenReturn(pathwayId);
        Pathway pathway = mockPathway(pathwayId);
        PathwayPayload payload = PathwayPayload.from(pathway, message.getActivityId(), new ArrayList<>(), null,
                new CoursewareElementDescription());

        when(pathwayService.create(eq(accountId), eq(activityId), eq(PathwayType.LINEAR), eq(pathwayId), eq(null))).thenReturn(Mono.just(pathway));
        when(pathwayService.getPathwayPayload(pathway)).thenReturn(Mono.just(payload));
        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"author.pathway.create.ok\"," +
                            "\"response\":{" +
                                "\"pathway\":{" +
                                    "\"pathwayId\":\"" + pathway.getId() + "\"," +
                                    "\"pathwayType\":\"" + PathwayType.LINEAR.name() + "\"," +
                                    "\"parentActivityId\":\"" + message.getActivityId() + "\"" +
                            "}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(pathwayCreatedRTMProducer, atLeastOnce()).buildPathwayCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(payload.getPathwayId()));
        verify(pathwayCreatedRTMProducer, atLeastOnce()).produce();
        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());
        verify(pathwayService, never()).replaceConfig(any(UUID.class), anyString());

        assertEquals(CoursewareAction.CREATED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.PATHWAY, captor.getValue().getElement().getElementType());
        assertEquals(pathway.getId(), captor.getValue().getElement().getElementId());
        assertEquals(activityId, captor.getValue().getParentElement().getElementId());
        assertEquals(ACTIVITY, captor.getValue().getParentElement().getElementType());
    }

    @Test
    void handle_withConfig() throws WriteResponseException {
        TestPublisher<PathwayConfig> configPublisher = TestPublisher.create();
        configPublisher.complete();

        String config = "{\"foo\":\"bar\"}";
        CreatePathwayMessage message = mock(CreatePathwayMessage.class);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getPathwayType()).thenReturn(PathwayType.LINEAR);
        when(message.getPathwayId()).thenReturn(pathwayId);
        when(message.getConfig()).thenReturn(config);
        Pathway pathway = mockPathway(pathwayId);
        PathwayPayload payload = PathwayPayload.from(pathway, message.getActivityId(), new ArrayList<>(), null,
                new CoursewareElementDescription());

        when(pathwayService.replaceConfig(any(UUID.class), eq(config))).thenReturn(configPublisher.mono());
        when(pathwayService.create(eq(accountId), eq(activityId), eq(PathwayType.LINEAR), eq(pathwayId), eq(null))).thenReturn(Mono.just(pathway));
        when(pathwayService.getPathwayPayload(pathway)).thenReturn(Mono.just(payload));
        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.pathway.create.ok\"," +
                "\"response\":{" +
                "\"pathway\":{" +
                "\"pathwayId\":\"" + pathway.getId() + "\"," +
                "\"pathwayType\":\"" + PathwayType.LINEAR.name() + "\"," +
                "\"parentActivityId\":\"" + message.getActivityId() + "\"" +
                "}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(pathwayCreatedRTMProducer, atLeastOnce()).buildPathwayCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(payload.getPathwayId()));
        verify(pathwayCreatedRTMProducer, atLeastOnce()).produce();
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), any(CoursewareElementBroadcastMessage.class));
        verify(pathwayService).replaceConfig(pathway.getId(), config);
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_exception() throws IOException {
        CreatePathwayMessage message = mock(CreatePathwayMessage.class);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getPathwayType()).thenReturn(PathwayType.LINEAR);
        when(message.getPathwayId()).thenReturn(pathwayId);
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(pathwayService.create(eq(accountId), eq(activityId), eq(PathwayType.LINEAR), eq(pathwayId), eq(null))).thenReturn(mono);

        handler.handle(session, message);

        verify(session.getRemote(), times(1))
                .sendStringByFuture("{" +
                                            "\"type\":\"author.pathway.create.error\"," +
                                            "\"code\":422," +
                                            "\"message\":\"Unable to create pathway\"}");

    }

    @Test
    void handle_withPreloadPathway() throws IOException {
        CreatePathwayMessage message = mock(CreatePathwayMessage.class);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getPathwayType()).thenReturn(PathwayType.LINEAR);
        when(message.getPathwayId()).thenReturn(pathwayId);
        when(message.getPreloadPathway()).thenReturn(PreloadPathway.ALL);
        Pathway pathway = mockPathway(pathwayId);
        PathwayPayload payload = PathwayPayload.from(pathway, message.getActivityId(), new ArrayList<>(), null,
                                                     new CoursewareElementDescription());

        when(pathwayService.create(eq(accountId), eq(activityId), eq(PathwayType.LINEAR), eq(pathwayId), eq(
                PreloadPathway.ALL))).thenReturn(Mono.just(pathway));
        when(pathwayService.getPathwayPayload(pathway)).thenReturn(Mono.just(payload));
        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.pathway.create.ok\"," +
                "\"response\":{" +
                "\"pathway\":{" +
                "\"pathwayId\":\"" + pathway.getId() + "\"," +
                "\"pathwayType\":\"" + PathwayType.LINEAR.name() + "\"," +
                "\"parentActivityId\":\"" + message.getActivityId() + "\"" +
                "}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(pathwayCreatedRTMProducer, atLeastOnce()).buildPathwayCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(payload.getPathwayId()));
        verify(pathwayCreatedRTMProducer, atLeastOnce()).produce();
        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, atLeastOnce()).broadcast(eq(message.getType()), captor.capture());
        verify(pathwayService, never()).replaceConfig(any(UUID.class), anyString());

        assertEquals(CoursewareAction.CREATED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.PATHWAY, captor.getValue().getElement().getElementType());
        assertEquals(pathway.getId(), captor.getValue().getElement().getElementId());
        assertEquals(activityId, captor.getValue().getParentElement().getElementId());
        assertEquals(ACTIVITY, captor.getValue().getParentElement().getElementType());
    }
}
