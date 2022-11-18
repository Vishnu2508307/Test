package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.courseware.pathway.PathwayMock.mockPathway;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.DuplicateInteractiveMessageHandler.AUTHOR_INTERACTIVE_DUPLICATE;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.DuplicateInteractiveMessageHandler.AUTHOR_INTERACTIVE_DUPLICATE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.DuplicateInteractiveMessageHandler.AUTHOR_INTERACTIVE_DUPLICATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.interactive.DuplicateInteractiveMessage;
import com.smartsparrow.rtm.subscription.courseware.duplicated.InteractiveDuplicatedRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class DuplicateInteractiveMessageHandlerTest {

    private DuplicateInteractiveMessageHandler handler;

    @Mock
    private InteractiveService interactiveService;

    @Mock
    private PathwayService pathwayService;

    @Mock
    private CoursewareService coursewareService;

    private Session session;

    @Mock
    private DuplicateInteractiveMessage message;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private InteractiveDuplicatedRTMProducer interactiveDuplicatedRTMProducer;

    @Mock
    private com.google.inject.Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;


    private static final UUID accountId = UUIDs.timeBased();
    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID pathwayId = UUIDs.timeBased();
    private static final UUID rootElementId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(message.getPathwayId()).thenReturn(pathwayId);
        when(message.getIndex()).thenReturn(null);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(coursewareService.getRootElementId(pathwayId, PATHWAY)).thenReturn(Mono.just(rootElementId));

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));

        handler = new DuplicateInteractiveMessageHandler(
                interactiveService,
                pathwayService,
                coursewareService,
                rtmEventBrokerProvider,
                authenticationContextProvider,
                rtmClientContextProvider,
                interactiveDuplicatedRTMProducer
        );
    }

    @Test
    void validate_noInteractiveId() {
        when(message.getInteractiveId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("missing interactiveId", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_DUPLICATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_interactiveNotFound() {
        Mono<Interactive> mono = TestPublisher.<Interactive>create().error(new InteractiveNotFoundException(interactiveId)).mono();
        when(interactiveService.findById(message.getInteractiveId())).thenReturn(mono);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("interactive not found", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_DUPLICATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_noParentPathwayId() {
        when(message.getPathwayId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("missing pathwayId", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_DUPLICATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_parentPathwayNotFound() {
        when(interactiveService.findById(message.getInteractiveId())).thenReturn(Mono.just(new Interactive()));
        Mono<Pathway> mono = TestPublisher.<Pathway>create().error(new PathwayNotFoundException(pathwayId)).mono();
        when(pathwayService.findById(message.getPathwayId())).thenReturn(mono);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("pathway not found", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_DUPLICATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_negativeIndex() {
        when(interactiveService.findById(message.getInteractiveId())).thenReturn(Mono.just(new Interactive()));
        Pathway pathway = mockPathway();
        when(pathwayService.findById(message.getPathwayId())) //
                .thenReturn(Mono.just(pathway));
        when(message.getIndex()).thenReturn(-1);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("index should be >= 0", t.getErrorMessage());
        assertEquals(AUTHOR_INTERACTIVE_DUPLICATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_positiveIndex() throws RTMValidationException {
        when(interactiveService.findById(message.getInteractiveId())).thenReturn(Mono.just(new Interactive()));
        Pathway pathway = mockPathway();
        when(pathwayService.findById(message.getPathwayId())) //
                .thenReturn(Mono.just(pathway));
        when(message.getIndex()).thenReturn(1);

        handler.validate(message);
    }

    @Test
    void validate() throws RTMValidationException {
        when(interactiveService.findById(message.getInteractiveId())).thenReturn(Mono.just(new Interactive()));
        Pathway pathway = mockPathway();
        when(pathwayService.findById(message.getPathwayId())) //
                .thenReturn(Mono.just(pathway));

        handler.validate(message);
    }

    @Test
    void handle() throws IOException {
        UUID newInteractiveId = UUIDs.timeBased();
        Interactive newInteractive = new Interactive().setId(newInteractiveId);
        InteractivePayload expectedPayload = InteractivePayload.from(newInteractive,
                                                                     new PluginSummary(),
                                                                     new InteractiveConfig(),
                                                                     pathwayId,
                                                                     new ArrayList<>(),
                                                                     new ArrayList<>(),
                                                                     new CoursewareElementDescription(),
                                                                     Lists.newArrayList());
        when(coursewareService.duplicateInteractive(interactiveId, pathwayId, accountId)).thenReturn(Mono.just(
                newInteractive));
        when(interactiveService.getInteractivePayload(newInteractive)).thenReturn(Mono.just(expectedPayload));
        when(interactiveDuplicatedRTMProducer.buildInteractiveDuplicatedRTMConsumable(rtmClientContext, rootElementId, newInteractiveId, pathwayId))
                .thenReturn(interactiveDuplicatedRTMProducer);

        handler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals(AUTHOR_INTERACTIVE_DUPLICATE_OK, response.getType());
            Map responseMap = ((Map) response.getResponse().get("interactive"));
            assertEquals(newInteractiveId.toString(), responseMap.get("interactiveId"));
        }));

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_INTERACTIVE_DUPLICATE), captor.capture());
        assertEquals(CoursewareAction.DUPLICATED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.INTERACTIVE, captor.getValue().getElement().getElementType());
        assertEquals(newInteractiveId, captor.getValue().getElement().getElementId());
        assertEquals(pathwayId, captor.getValue().getParentElement().getElementId());
        assertEquals(PATHWAY, captor.getValue().getParentElement().getElementType());

        verify(interactiveDuplicatedRTMProducer, atLeastOnce()).buildInteractiveDuplicatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(newInteractiveId), eq(pathwayId));
        verify(interactiveDuplicatedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_indexIsOutOfRange() {
        TestPublisher<Interactive> error = TestPublisher.<Interactive>create().error(new IndexOutOfBoundsException());
        when(coursewareService.duplicateInteractive(interactiveId, pathwayId, accountId)).thenReturn(error.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_INTERACTIVE_DUPLICATE_ERROR + "\",\"code\":400," +
                "\"message\":\"Index is out of range\"}");
        verify(interactiveDuplicatedRTMProducer, never()).produce();
    }

    @Test
    void handle_withIndex() throws IOException {
        when(message.getIndex()).thenReturn(1);
        UUID newInteractiveId = UUIDs.timeBased();
        Interactive newInteractive = new Interactive().setId(newInteractiveId);
        when(coursewareService.duplicateInteractive(interactiveId, pathwayId, 1)).thenReturn(Mono.just(newInteractive));
        when(interactiveService.getInteractivePayload(newInteractive)).thenReturn(Mono.just(new InteractivePayload()));
        when(interactiveDuplicatedRTMProducer.buildInteractiveDuplicatedRTMConsumable(rtmClientContext, rootElementId, null, pathwayId))
                .thenReturn(interactiveDuplicatedRTMProducer);

        handler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals(AUTHOR_INTERACTIVE_DUPLICATE_OK, response.getType());
        }));
    }

    @Test
    void handle_error() {
        TestPublisher<Interactive> error = TestPublisher.<Interactive>create().error(new RuntimeException("some exception"));
        when(coursewareService.duplicateInteractive(interactiveId, pathwayId, accountId)).thenReturn(error.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_INTERACTIVE_DUPLICATE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to duplicate interactive\"}");
        verify(interactiveDuplicatedRTMProducer, never()).produce();
    }
}
