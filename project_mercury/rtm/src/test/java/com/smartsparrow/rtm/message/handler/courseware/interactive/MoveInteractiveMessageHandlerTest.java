package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.MoveInteractiveMessageHandler.AUTHOR_INTERACTIVE_MOVE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.MoveInteractiveMessageHandler.AUTHOR_INTERACTIVE_MOVE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.interactive.MoveInteractiveMessage;
import com.smartsparrow.rtm.subscription.courseware.moved.InteractiveMovedRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class MoveInteractiveMessageHandlerTest {

    @Mock
    private InteractiveService interactiveService;
    @Mock
    private CoursewareService coursewareService;
    @Mock
    private MoveInteractiveMessage message;
    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    @Mock
    private RTMEventBroker rtmEventBroker;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private InteractiveMovedRTMProducer interactiveMovedRTMProducer;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    private MoveInteractiveMessageHandler handler;
    private Session session;

    private static final UUID accountId = UUIDs.timeBased();
    private static final UUID rootElementId = UUIDs.timeBased();
    private static final UUID interactiveId = UUIDs.timeBased();
    private static final UUID pathwayId = UUIDs.timeBased();
    private static final UUID oldParentPathwayId = UUIDs.timeBased();
    private static final int index = 1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Account account = mock(Account.class);
        session = RTMWebSocketTestUtils.mockSession();
        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(message.getPathwayId()).thenReturn(pathwayId);
        when(message.getIndex()).thenReturn(index);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(coursewareService.getRootElementId(pathwayId, PATHWAY)).thenReturn(Mono.just(rootElementId));
        when(interactiveMovedRTMProducer.buildInteractiveMovedRTMConsumable(rtmClientContext,
                                                                            rootElementId,
                                                                            interactiveId,
                                                                            oldParentPathwayId,
                                                                            pathwayId))
                .thenReturn(interactiveMovedRTMProducer);

        handler = new MoveInteractiveMessageHandler(
                interactiveService,
                coursewareService,
                authenticationContextProvider,
                rtmEventBrokerProvider,
                rtmClientContextProvider,
                interactiveMovedRTMProducer);

        when(interactiveService.findParentPathwayId(message.getInteractiveId())).thenReturn(Mono.just(oldParentPathwayId));
    }

    @Test
    void validate_noInteractiveId() {
        when(message.getInteractiveId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("interactiveId is required", f.getMessage());
    }

    @Test
    void validate_noDestinationPathwayId() {
        when(message.getPathwayId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("pathwayId is required", f.getMessage());
    }

    @Test
    void validate_negativeIndex() {
        when(interactiveService.getInteractivePayload(message.getInteractiveId())).thenReturn(Mono.just(new InteractivePayload()));
        when(interactiveService.findParentPathwayId(message.getInteractiveId())).thenReturn(Mono.just(UUIDs.timeBased()));
        when(message.getIndex()).thenReturn(-1);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("index should be >= 0", f.getMessage());
    }

    @Test
    void validate_interactiveNotFound() {
        when(interactiveService.getInteractivePayload(message.getInteractiveId())).thenReturn(Mono.empty());

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("interactive payload not found", f.getMessage());
    }

    @Test
    void validate_parentPathwayNotFound() {
        when(interactiveService.getInteractivePayload(message.getInteractiveId())).thenReturn(Mono.just(new InteractivePayload()));
        when(interactiveService.findParentPathwayId(message.getInteractiveId())).thenReturn(Mono.empty());

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("parent pathway not found", f.getMessage());
    }

    @Test
    void validate() throws RTMValidationException {
        when(interactiveService.getInteractivePayload(message.getInteractiveId())).thenReturn(Mono.just(new InteractivePayload()));
        when(interactiveService.findParentPathwayId(message.getInteractiveId())).thenReturn(Mono.just(UUIDs.timeBased()));

        handler.validate(message);
    }

    @Test
    void handle() throws IOException {
        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        Interactive newInteractive = new Interactive().setId(interactiveId);
        InteractivePayload expectedPayload = InteractivePayload.from(newInteractive, new PluginSummary(),
                                                                     new InteractiveConfig(), pathwayId,
                                                                     new ArrayList<>(), new ArrayList<>(),
                                                                     new CoursewareElementDescription(),
                                                                     new ArrayList<>());
        when(interactiveService.move(interactiveId, pathwayId, index, oldParentPathwayId)).thenReturn(Mono.just(expectedPayload));
        when(interactiveService.getInteractivePayload(interactiveId)).thenReturn(Mono.just(expectedPayload));

        handler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals(AUTHOR_INTERACTIVE_MOVE_OK, response.getType());
            Map responseMap = ((Map) response.getResponse().get("interactive"));
            assertEquals(interactiveId.toString(), responseMap.get("interactiveId"));
        }));

        verify(rtmEventBroker).broadcast(eq("author.interactive.move"), captor.capture());
        final CoursewareElementBroadcastMessage broadcastMessage = captor.getValue();

        assertNotNull(broadcastMessage);
        assertEquals(CoursewareAction.INTERACTIVE_MOVED, broadcastMessage.getAction());
        assertEquals(CoursewareElement.from(pathwayId, PATHWAY), broadcastMessage.getParentElement());
        assertEquals(CoursewareElement.from(oldParentPathwayId, PATHWAY), broadcastMessage.getOldParentElement());
        assertEquals(CoursewareElement.from(interactiveId, CoursewareElementType.INTERACTIVE), broadcastMessage.getElement());

        verify(interactiveMovedRTMProducer, atLeastOnce()).buildInteractiveMovedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(interactiveId), eq(oldParentPathwayId), eq(pathwayId));
        verify(interactiveMovedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_indexIsOutOfRange() throws WriteResponseException {
        TestPublisher<InteractivePayload> error = TestPublisher.<InteractivePayload>create().error(new IndexOutOfBoundsException());
        when(interactiveService.move(interactiveId, pathwayId, 1, oldParentPathwayId)).thenReturn(error.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_INTERACTIVE_MOVE_ERROR + "\",\"code\":400," +
                "\"message\":\"Index is out of range\"}");
        verify(rtmEventBroker, never()).broadcast(anyString(), any(CoursewareElementBroadcastMessage.class));
        verify(interactiveMovedRTMProducer, never()).produce();
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<InteractivePayload> error = TestPublisher.<InteractivePayload>create().error(new RuntimeException("some exception"));
        when(interactiveService.move(interactiveId, pathwayId, 1, oldParentPathwayId)).thenReturn(error.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_INTERACTIVE_MOVE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to move interactive\"}");
        verify(rtmEventBroker, never()).broadcast(anyString(), any(CoursewareElementBroadcastMessage.class));
        verify(interactiveMovedRTMProducer, never()).produce();
    }

}
