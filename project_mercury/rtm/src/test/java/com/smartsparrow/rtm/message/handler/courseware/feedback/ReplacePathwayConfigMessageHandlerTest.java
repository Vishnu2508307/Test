package com.smartsparrow.rtm.message.handler.courseware.feedback;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.courseware.pathway.PathwayMock.mockPathway;
import static com.smartsparrow.rtm.message.handler.courseware.feedback.ReplacePathwayConfigMessageHandler.AUTHOR_PATHWAY_CONFIG_REPLACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.PathwayConfig;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.pathway.ReplacePathwayConfigMessage;
import com.smartsparrow.rtm.subscription.courseware.configchange.PathwayConfigChangeRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ReplacePathwayConfigMessageHandlerTest {

    @InjectMocks
    private ReplacePathwayConfigMessageHandler handler;

    @Mock
    private PathwayService pathwayService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private PathwayConfigChangeRTMProducer pathwayConfigChangeRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock(name = "rtmEventBrokerProvider")
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock(name = "rtmEventBroker")
    private RTMEventBroker rtmEventBroker;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final String config = "{\"foo\":\"bar\"}";
    private static final String messageId = "messageId";
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private ReplacePathwayConfigMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        message = mock(ReplacePathwayConfigMessage.class);

        when(message.getId()).thenReturn(messageId);
        when(message.getConfig()).thenReturn(config);
        when(message.getPathwayId()).thenReturn(pathwayId);

        Pathway pathway = mockPathway(pathwayId);

        when(pathwayService.findById(pathwayId)).thenReturn(Mono.just(pathway));
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(coursewareService.getRootElementId(pathwayId, PATHWAY)).thenReturn(Mono.just(rootElementId));

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(UUID.randomUUID()));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);

        when(pathwayConfigChangeRTMProducer.buildPathwayConfigChangeRTMConsumable(rtmClientContext,
                                                                                rootElementId,
                                                                                pathwayId,
                                                                                config))
                .thenReturn(pathwayConfigChangeRTMProducer);

        handler = new ReplacePathwayConfigMessageHandler(pathwayService,
                                                         coursewareService,
                                                         rtmEventBrokerProvider,
                                                         authenticationContextProvider,
                                                         rtmClientContextProvider,
                                                         pathwayConfigChangeRTMProducer);
    }

    @Test
    void validate_pathwayIdNotSupplied() {
        when(message.getPathwayId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(e);
        assertEquals("pathwayId is required", e.getMessage());
    }

    @Test
    void validate_configNotSupplied() {
        when(message.getConfig()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(e);
        assertEquals("config is required", e.getMessage());
    }

    @Test
    void validate_pathwayNotFound() {
        TestPublisher<Pathway> publisher = TestPublisher.create();
        publisher.error(new PathwayNotFoundException(pathwayId));

        when(pathwayService.findById(pathwayId)).thenReturn(publisher.mono());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertNotNull(e);
        assertEquals(messageId, e.getReplyTo());
        assertEquals("author.pathway.config.replace.error", e.getType());
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<PathwayConfig> publisher = TestPublisher.create();
        publisher.error(new RuntimeException());

        when(pathwayService.replaceConfig(pathwayId, config)).thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.pathway.config.replace.error\"," +
                "\"code\":422," +
                "\"message\":\"Unable to save configuration\"," +
                "\"replyTo\":\"messageId\"" +
                "}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmEventBroker, never()).broadcast(anyString(), any(BroadcastMessage.class));
        verify(pathwayConfigChangeRTMProducer, never()).produce();
    }

    @Test
    void handle_success() throws WriteResponseException {

        PathwayConfig pathwayConfig = new PathwayConfig()
                .setId(UUID.randomUUID())
                .setPathwayId(pathwayId)
                .setConfig(config);

        when(pathwayService.replaceConfig(pathwayId, config)).thenReturn(Mono.just(pathwayConfig));

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.pathway.config.replace.ok\"," +
                "\"response\":{" +
                "\"config\":{" +
                "\"id\":\"" + pathwayConfig.getId() + "\"," +
                "\"pathwayId\":\"" + pathwayId + "\"," +
                "\"config\":\"" + StringEscapeUtils.escapeJava(config) + "\"" +
                "}" +
                "}," +
                "\"replyTo\":\"" + messageId + "\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_PATHWAY_CONFIG_REPLACE), captor.capture());
        assertEquals(CoursewareAction.CONFIG_CHANGE, captor.getValue().getAction());
        assertEquals(pathwayId, captor.getValue().getElement().getElementId());
        assertEquals(PATHWAY, captor.getValue().getElement().getElementType());
        assertNull(captor.getValue().getParentElement());
        verify(pathwayConfigChangeRTMProducer, atLeastOnce()).buildPathwayConfigChangeRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(pathwayId), eq(config));
        verify(pathwayConfigChangeRTMProducer, atLeastOnce()).produce();
    }
}
