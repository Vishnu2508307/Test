package com.smartsparrow.rtm.message.handler.courseware.pathway;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.pathway.ReorderWalkableMessageHandler.AUTHOR_WALKABLE_REORDER;
import static com.smartsparrow.rtm.message.handler.courseware.pathway.ReorderWalkableMessageHandler.AUTHOR_WALKABLE_REORDER_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.pathway.ReorderWalkableMessageHandler.AUTHOR_WALKABLE_REORDER_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.pathway.ReorderWalkableMessage;
import com.smartsparrow.rtm.subscription.courseware.pathwayreordered.PathwayReOrderedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ReorderWalkableMessageHandlerTest {

    @InjectMocks
    private ReorderWalkableMessageHandler handler;

    @Mock
    private ReorderWalkableMessage message;

    private Session session;

    @Mock
    private PathwayService pathwayService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private PathwayReOrderedRTMProducer pathwayReOrderedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID walkableId1 = UUID.randomUUID();
    private static final UUID walkableId2 = UUID.randomUUID();
    private static final UUID walkableId3 = UUID.randomUUID();
    private static final List<UUID> walkableIds = Lists.newArrayList(walkableId1, walkableId2, walkableId3);
    private static final List<WalkableChild> walkables = Lists.newArrayList(new WalkableChild().setElementId(
            walkableId1), new WalkableChild().setElementId(walkableId2), new WalkableChild().setElementId(walkableId3));

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(coursewareService.getRootElementId(pathwayId, PATHWAY)).thenReturn(Mono.just(rootElementId));

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(message.getPathwayId()).thenReturn(pathwayId);
        when(message.getWalkableIds()).thenReturn(walkableIds);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(UUID.randomUUID()));
        when(pathwayReOrderedRTMProducer.buildPathwayReOrderedRTMConsumable(rtmClientContext, rootElementId, pathwayId, walkables))
                .thenReturn(pathwayReOrderedRTMProducer);
        when(pathwayService.getOrderedWalkableChildren(pathwayId)).thenReturn(Mono.just(walkables));

        handler = new ReorderWalkableMessageHandler(pathwayService,
                                                    coursewareService,
                                                    rtmEventBrokerProvider,
                                                    authenticationContextProvider,
                                                    rtmClientContextProvider,
                                                    pathwayReOrderedRTMProducer);
    }

    @SuppressWarnings("Duplicates")
    @Test
    void validate_noActivityId() {
        when(message.getPathwayId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("pathwayId is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void validate_noPathwayIds() {
        when(message.getWalkableIds()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("walkableIds is required and can not be empty", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void validate_emptyPathwayIds() {
        when(message.getWalkableIds()).thenReturn(new ArrayList<>());

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("walkableIds is required and can not be empty", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void validate() throws RTMValidationException {
        handler.validate(message);
    }

    @Test
    void handle() {
        when(pathwayService.reorder(pathwayId, walkableIds))
                .thenReturn(Flux.empty());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_WALKABLE_REORDER_OK + "\"}");
    }

    @Test
    void handle_eventPublish() {
        when(pathwayService.reorder(pathwayId, walkableIds)).thenReturn(Flux.empty());

        handler.handle(session, message);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_WALKABLE_REORDER), captor.capture());
        assertEquals(CoursewareAction.PATHWAY_REORDERED, captor.getValue().getAction());
        assertEquals(pathwayId, captor.getValue().getElement().getElementId());
        assertEquals(PATHWAY, captor.getValue().getElement().getElementType());
        assertNull(captor.getValue().getParentElement());
        assertNull(captor.getValue().getScenarioLifecycle());

        verify(pathwayReOrderedRTMProducer, atLeastOnce()).buildPathwayReOrderedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(pathwayId), eq(walkables));
        verify(pathwayReOrderedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_error() {
        TestPublisher<Void> publisher = TestPublisher.<Void>create().error(new RuntimeException("some exception"));
        when(pathwayService.reorder(pathwayId, walkableIds))
                .thenReturn(publisher.flux());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_WALKABLE_REORDER_ERROR + "\",\"code\":500," +
                "\"message\":\"unhandled error occurred to message processing\"}");

        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(pathwayReOrderedRTMProducer, never()).produce();
    }

    @Test
    void handle_invalidWalkables() {
        TestPublisher<Void> publisher = TestPublisher.<Void>create().error(new IllegalArgumentFault("Invalid walkables"));
        when(pathwayService.reorder(pathwayId, walkableIds))
                .thenReturn(publisher.flux());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_WALKABLE_REORDER_ERROR + "\",\"code\":400," +
                "\"message\":\"Invalid walkables\"}");

        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(pathwayReOrderedRTMProducer, never()).produce();
    }
}
