package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.ReplaceInteractiveConfigMessageHandler.AUTHOR_INTERACTIVE_CONFIG_REPLACE;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.ReplaceInteractiveConfigMessageHandler.AUTHOR_INTERACTIVE_CONFIG_REPLACE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.ReplaceInteractiveConfigMessageHandler.AUTHOR_INTERACTIVE_CONFIG_REPLACE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.interactive.ReplaceInteractiveConfigMessage;
import com.smartsparrow.rtm.subscription.courseware.configchange.InteractiveConfigChangeRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ReplaceInteractiveConfigMessageHandlerTest {

    private ReplaceInteractiveConfigMessageHandler handler;
    @Mock
    private InteractiveService interactiveService;
    @Mock(name = "authenticationContextProvider")
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private CoursewareService coursewareService;
    @Mock
    private Account account;
    @Mock
    private ReplaceInteractiveConfigMessage message;
    private Session session;
    @Mock
    private InteractiveConfigChangeRTMProducer interactiveConfigChangeRTMProducer;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    @Mock(name = "rtmEventBrokerProvider")
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    @Mock(name = "rtmEventBroker")
    private RTMEventBroker rtmEventBroker;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new ReplaceInteractiveConfigMessageHandler(authenticationContextProvider,
                                                             interactiveService,
                                                             rtmEventBrokerProvider,
                                                             coursewareService,
                                                             rtmClientContextProvider,
                                                             interactiveConfigChangeRTMProducer);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(coursewareService.getRootElementId(interactiveId, INTERACTIVE)).thenReturn(Mono.just(rootElementId));

        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(message.getConfig()).thenReturn("configuration");

        session = RTMWebSocketTestUtils.mockSession();

        when(coursewareService.saveConfigurationFields(eq(interactiveId), anyString()))
                .thenReturn(Flux.just(new Void[]{}));
        when(interactiveConfigChangeRTMProducer.buildInteractiveConfigChangeRTMConsumable(rtmClientContext,
                                                                                          rootElementId,
                                                                                          interactiveId,
                                                                                          "configuration"))
                .thenReturn(interactiveConfigChangeRTMProducer);
    }

    @Test
    void validate_noInteractiveId() {
        when(message.getInteractiveId()).thenReturn(null);

        IllegalArgumentFault response = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("missing interactiveId", response.getMessage());
        assertEquals(400, response.getResponseStatusCode());
    }

    @Test
    void validate_noConfig() {
        when(message.getConfig()).thenReturn(null);
        when(interactiveService.findById(eq(interactiveId))).thenReturn(Mono.empty());
        IllegalArgumentFault response = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("missing config", response.getMessage());
        assertEquals(400, response.getResponseStatusCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    void validate_interactiveNotFound() {
        Mono mono = TestPublisher.create().error(new InteractiveNotFoundException(interactiveId)).mono();
        when(interactiveService.findById(eq(interactiveId))).thenReturn(mono);

        RTMValidationException response = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("no interactive with id " + interactiveId, response.getErrorMessage());
        assertEquals(400, response.getStatusCode());
        assertEquals(AUTHOR_INTERACTIVE_CONFIG_REPLACE_ERROR, response.getType());
    }

    @Test
    void handle() throws IOException {
        InteractiveConfig expected = new InteractiveConfig()
                .setId(UUID.randomUUID())
                .setInteractiveId(interactiveId)
                .setConfig("configuration");
        when(interactiveService.replaceConfig(eq(accountId), eq(interactiveId), eq("configuration"))).thenReturn(Mono.just(expected));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_INTERACTIVE_CONFIG_REPLACE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("config"));
                assertEquals(expected.getId().toString(), responseMap.get("id"));
                assertEquals(expected.getConfig(), responseMap.get("config"));
                assertEquals(interactiveId.toString(), responseMap.get("interactiveId"));
            });
        });

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_INTERACTIVE_CONFIG_REPLACE), captor.capture());
        assertEquals(CoursewareAction.CONFIG_CHANGE, captor.getValue().getAction());
        assertEquals(interactiveId, captor.getValue().getElement().getElementId());
        assertEquals(INTERACTIVE, captor.getValue().getElement().getElementType());
        assertNull(captor.getValue().getParentElement());

        verify(interactiveConfigChangeRTMProducer, atLeastOnce()).buildInteractiveConfigChangeRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(interactiveId), eq("configuration"));
        verify(interactiveConfigChangeRTMProducer, atLeastOnce()).produce();

        verify(coursewareService).saveConfigurationFields(interactiveId, "configuration");
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_exception() throws IOException {
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(interactiveService.replaceConfig(eq(accountId), eq(interactiveId), eq("configuration"))).thenReturn(mono);

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"author.interactive.config.replace.error\",\"code\":422," +
                "\"message\":\"Unable to save configuration\"}");
        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(interactiveConfigChangeRTMProducer, never()).produce();
    }
}
