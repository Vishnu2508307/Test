package com.smartsparrow.rtm.message.handler.courseware.interactive;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.CreateInteractiveMessageHandler.AUTHOR_INTERACTIVE_CREATE;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.CreateInteractiveMessageHandler.AUTHOR_INTERACTIVE_CREATE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.interactive.CreateInteractiveMessageHandler.AUTHOR_INTERACTIVE_CREATE_OK;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.interactive.CreateInteractiveMessage;
import com.smartsparrow.rtm.subscription.courseware.created.InteractiveCreatedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CreateInteractiveMessageHandlerTest {

    private CreateInteractiveMessageHandler handler;
    @Mock(name = "authenticationContextProvider")
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private Account account;
    @Mock
    private PluginService pluginService;
    @Mock
    private InteractiveService interactiveService;
    @Mock
    private PathwayService pathwayService;
    @Mock
    private CoursewareService coursewareService;
    @Mock
    private InteractiveCreatedRTMProducer interactiveCreatedRTMProducer;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    @Mock(name = "rtmEventBrokerProvider")
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    @Mock(name = "rtmEventBroker")
    private RTMEventBroker rtmEventBroker;
    @Mock
    private CreateInteractiveMessage message;
    private Session session;

    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String pluginVersion = "1.*";
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        handler = new CreateInteractiveMessageHandler(authenticationContextProvider, pluginService,
                interactiveService, pathwayService, rtmEventBrokerProvider, coursewareService, rtmClientContextProvider, interactiveCreatedRTMProducer);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(interactiveCreatedRTMProducer.buildInteractiveCreatedRTMConsumable(rtmClientContext, rootElementId, interactiveId, pathwayId))
                .thenReturn(interactiveCreatedRTMProducer);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPathwayId()).thenReturn(pathwayId);
        when(message.getPluginVersionExpr()).thenReturn(pluginVersion);
        when(coursewareService.getRootElementId(pathwayId, PATHWAY)).thenReturn(Mono.just(rootElementId));

        session = RTMWebSocketTestUtils.mockSession();

        when(coursewareService.saveConfigurationFields(eq(interactiveId), anyString())).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void validate_noPluginId() {
        when(message.getPluginId()).thenReturn(null);

        RTMValidationException response = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("missing pluginId parameter", response.getErrorMessage());
        assertEquals(400, response.getStatusCode());
        assertEquals(AUTHOR_INTERACTIVE_CREATE_ERROR, response.getType());
    }

    @Test
    void validate_noPathwayId() {
        when(message.getPathwayId()).thenReturn(null);

        RTMValidationException response = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("missing pathwayId parameter", response.getErrorMessage());
        assertEquals(400, response.getStatusCode());
        assertEquals(AUTHOR_INTERACTIVE_CREATE_ERROR, response.getType());
    }

    @Test
    void validate_noPluginVersion() {
        when(message.getPluginVersionExpr()).thenReturn(null);

        RTMValidationException response = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("missing pluginVersion parameter", response.getErrorMessage());
        assertEquals(400, response.getStatusCode());
        assertEquals(AUTHOR_INTERACTIVE_CREATE_ERROR, response.getType());
    }

    @SuppressWarnings("unchecked")
    @Test
    void validate_noPlugin() {
        Mono mono = TestPublisher.create().error(new PluginNotFoundFault("no plugin")).mono();
        when(pluginService.findLatestVersion(eq(pluginId), eq(pluginVersion))).thenReturn(mono);

        RTMValidationException response = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("no plugin", response.getErrorMessage());
        assertEquals(400, response.getStatusCode());
        assertEquals(AUTHOR_INTERACTIVE_CREATE_ERROR, response.getType());
    }

    @SuppressWarnings("unchecked")
    @Test
    void validate_pathwayNotFound() throws VersionParserFault {
        Mono mono = TestPublisher.create().error(new PathwayNotFoundException(pathwayId)).mono();
        when(pathwayService.findById(eq(pathwayId))).thenReturn(mono);
        when(pluginService.findLatestVersion(eq(pluginId), eq(pluginVersion))).thenReturn(Mono.empty());

        RTMValidationException response = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("invalid pathway", response.getErrorMessage());
        assertEquals(400, response.getStatusCode());
        assertEquals(AUTHOR_INTERACTIVE_CREATE_ERROR, response.getType());
    }

    @Test
    void handle() throws IOException {
        Interactive interactive = new Interactive()
                .setId(interactiveId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersion);

        PluginSummary pluginSummary = new PluginSummary().setId(pluginId);

        InteractivePayload expected = InteractivePayload.from(interactive,
                                                              pluginSummary,
                                                              new InteractiveConfig(),
                                                              pathwayId,
                                                              Lists.newArrayList(),
                                                              Lists.newArrayList(),
                                                              new CoursewareElementDescription(),
                                                              new ArrayList<>());

        when(interactiveService.create(eq(accountId), eq(pathwayId), eq(pluginId), eq(pluginVersion)))
                .thenReturn(Mono.just(interactive));

        when(interactiveService.getInteractivePayload(interactive)).thenReturn(Mono.just(expected));

        handler.handle(session, message);

        verify(interactiveCreatedRTMProducer, atLeastOnce()).buildInteractiveCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(interactiveId), eq(pathwayId));
        verify(interactiveCreatedRTMProducer, atLeastOnce()).produce();

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_INTERACTIVE_CREATE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("interactive"));
                assertEquals(interactiveId.toString(), responseMap.get("interactiveId"));
                assertEquals(pluginId.toString(), ((LinkedHashMap) responseMap.get("plugin")).get("pluginId"));
                assertEquals(pluginVersion, ((LinkedHashMap) responseMap.get("plugin")).get("version"));
                assertEquals(pathwayId.toString(), responseMap.get("parentPathwayId"));
                assertNull(responseMap.get("config"));
            });
        });

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_INTERACTIVE_CREATE), captor.capture());
        assertEquals(CoursewareAction.CREATED, captor.getValue().getAction());
        assertEquals(interactiveId, captor.getValue().getElement().getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, captor.getValue().getElement().getElementType());
        assertEquals(pathwayId, captor.getValue().getParentElement().getElementId());
        assertEquals(PATHWAY, captor.getValue().getParentElement().getElementType());
    }

    @Test
    void handle_withConfig() throws IOException {
        String config = "{interactive config}";
        when(message.getConfig()).thenReturn(config);
        Interactive interactive = new Interactive()
                .setId(interactiveId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersion);
        InteractiveConfig intConfig = new InteractiveConfig().setConfig(config);
        InteractivePayload expected = InteractivePayload.from(interactive,
                                                              new PluginSummary(),
                                                              intConfig,
                                                              pathwayId,
                                                              Lists.newArrayList(),
                                                              Lists.newArrayList(),
                                                              new CoursewareElementDescription(),
                                                              Lists.newArrayList());

        when(interactiveService.create(eq(accountId), eq(pathwayId), eq(pluginId), eq(pluginVersion)))
                .thenReturn(Mono.just(interactive));
        when(interactiveService.replaceConfig(accountId, interactiveId, config)).thenReturn(Mono.just(intConfig));

        when(interactiveService.getInteractivePayload(interactive)).thenReturn(Mono.just(expected));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_INTERACTIVE_CREATE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("interactive"));
                assertEquals(interactiveId.toString(), responseMap.get("interactiveId"));
                assertEquals(pathwayId.toString(), responseMap.get("parentPathwayId"));
                assertEquals(config, responseMap.get("config"));
            });
        });

        verify(coursewareService).saveConfigurationFields(interactiveId, message.getConfig());
    }

    @SuppressWarnings("unchecked")
    @Test
    void handle_versionParserException() throws IOException {
        Mono mono = TestPublisher.create().error(new VersionParserFault("versionParserException")).mono();
        when(interactiveService.create(eq(accountId), eq(pathwayId), eq(pluginId), eq(pluginVersion))).thenReturn(mono);

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"author.interactive.create.error\",\"code\":422,\"message\":\"Unable to create interactive\"}");
        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(interactiveCreatedRTMProducer, never()).produce();
    }

}
