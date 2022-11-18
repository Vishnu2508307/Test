package com.smartsparrow.rtm.message.handler.courseware.component;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.rtm.message.handler.courseware.component.CreateInteractiveComponentMessageHandler.AUTHOR_INTERACTIVE_COMPONENT_CREATE_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.component.CreateInteractiveComponentMessage;
import com.smartsparrow.rtm.subscription.courseware.created.ComponentCreatedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

public class CreateInteractiveComponentMessageHandlerTest {

    private Session session;

    CreateInteractiveComponentMessageHandler createInteractiveComponentMessageHandler;

    @Mock
    ComponentService componentService;

    @Mock
    InteractiveService interactiveService;

    @Mock
    PluginService pluginService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private ComponentCreatedRTMProducer componentCreatedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private RTMEventBroker rtmEventBroker;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final String pluginVersionExpr = "v1.2.3";
    private static final String config = "some config";

    private CreateInteractiveComponentMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        message = mock(CreateInteractiveComponentMessage.class);
        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getPluginVersionExpr()).thenReturn(pluginVersionExpr);
        when(message.getConfig()).thenReturn(config);
        session = RTMWebSocketTestUtils.mockSession();

        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> mockRTMEventBroker =
                RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);

        rtmEventBroker = mockRTMEventBroker.getT2();
        Provider<RTMEventBroker> rtmEventBrokerProvider = mockRTMEventBroker.getT1();

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(coursewareService.saveConfigurationFields(any(UUID.class), any(String.class))).thenReturn(Flux.just(new Void[]{}));
        when(coursewareService.getRootElementId(interactiveId, INTERACTIVE)).thenReturn(Mono.just(rootElementId));

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);

        createInteractiveComponentMessageHandler = new CreateInteractiveComponentMessageHandler(
                componentService,
                interactiveService,
                pluginService,
                rtmEventBrokerProvider,
                coursewareService,
                authenticationContextProvider,
                rtmClientContextProvider,
                componentCreatedRTMProducer);
    }

    @Test
    void validateMissingInteractiveId() {
        RTMValidationException response =
                assertThrows(RTMValidationException.class, () ->
                        createInteractiveComponentMessageHandler.validate(new CreateInteractiveComponentMessage()));
        assertEquals("interactiveId is required", response.getErrorMessage());
    }

    @Test
    void validateMissingPluginId() {
        CreateInteractiveComponentMessage m = mock(CreateInteractiveComponentMessage.class);
        when(m.getInteractiveId()).thenReturn(interactiveId);
        RTMValidationException response =
                assertThrows(RTMValidationException.class, () ->
                        createInteractiveComponentMessageHandler.validate(m));
        assertEquals("pluginId is required", response.getErrorMessage());
    }

    @Test
    void validateMissingPluginVersionExpr() {
        CreateInteractiveComponentMessage m = mock(CreateInteractiveComponentMessage.class);
        when(m.getInteractiveId()).thenReturn(interactiveId);
        when(m.getPluginId()).thenReturn(pluginId);
        RTMValidationException response =
                assertThrows(RTMValidationException.class, () ->
                        createInteractiveComponentMessageHandler.validate(m));
        assertEquals("pluginVersion is required", response.getErrorMessage());
    }

    @Test
    void validateSuccess() throws RTMValidationException, VersionParserFault {
        CreateInteractiveComponentMessage m = mock(CreateInteractiveComponentMessage.class);
        when(m.getInteractiveId()).thenReturn(interactiveId);
        when(m.getPluginId()).thenReturn(pluginId);
        when(m.getPluginVersionExpr()).thenReturn(pluginVersionExpr);
        when(interactiveService.findById(eq(interactiveId))).thenReturn(Mono.empty());
        when(pluginService.findLatestVersion(eq(pluginId), eq(pluginVersionExpr))).thenReturn(Mono.empty());

        createInteractiveComponentMessageHandler.validate(m);
    }

    @SuppressWarnings("unchecked")
    @Test
    void validate_pluginNotFound() throws VersionParserFault {
        CreateInteractiveComponentMessage m = mock(CreateInteractiveComponentMessage.class);
        when(m.getInteractiveId()).thenReturn(interactiveId);
        when(m.getPluginId()).thenReturn(pluginId);
        when(m.getPluginVersionExpr()).thenReturn(pluginVersionExpr);
        when(interactiveService.findById(eq(interactiveId))).thenReturn(Mono.empty());
        Mono mono = TestPublisher.create().error(new PluginNotFoundFault("no plugin")).mono();
        when(pluginService.findLatestVersion(eq(pluginId), eq(pluginVersionExpr))).thenReturn(mono);

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> createInteractiveComponentMessageHandler.validate(m));
        assertEquals(400, ex.getStatusCode());
        assertEquals(AUTHOR_INTERACTIVE_COMPONENT_CREATE_ERROR, ex.getType());
        assertEquals("invalid plugin", ex.getErrorMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void validate_interactiveNotFound() throws VersionParserFault {
        CreateInteractiveComponentMessage m = mock(CreateInteractiveComponentMessage.class);
        when(m.getInteractiveId()).thenReturn(interactiveId);
        when(m.getPluginId()).thenReturn(pluginId);
        when(m.getPluginVersionExpr()).thenReturn(pluginVersionExpr);
        Mono mono = TestPublisher.create().error(new InteractiveNotFoundException(interactiveId)).mono();
        when(interactiveService.findById(eq(interactiveId))).thenReturn(mono);
        when(pluginService.findLatestVersion(eq(pluginId), eq(pluginVersionExpr))).thenReturn(Mono.empty());

        RTMValidationException ex = assertThrows(RTMValidationException.class, () -> createInteractiveComponentMessageHandler.validate(m));
        assertEquals(400, ex.getStatusCode());
        assertEquals(AUTHOR_INTERACTIVE_COMPONENT_CREATE_ERROR, ex.getType());
        assertEquals("invalid interactive", ex.getErrorMessage());
    }

    @Test
    void handle() throws WriteResponseException, VersionParserFault {
        UUID parentId = UUID.randomUUID();

        Component component = new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        PluginSummary plugin = new PluginSummary()
                .setId(pluginId)
                .setType(PluginType.COMPONENT)
                .setName("z650");

        ParentByComponent parent = new ParentByComponent()
                .setComponentId(componentId)
                .setParentType(CoursewareElementType.INTERACTIVE)
                .setParentId(parentId);

        ComponentPayload payload = ComponentPayload.from(component, "some config", plugin, parent, new CoursewareElementDescription(), new ArrayList<>());

        when(componentService.createForInteractive(any(UUID.class), any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(Mono.just(component));
        when(componentService.getComponentPayload(any(UUID.class))).thenReturn(Mono.just(payload));
        when(componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext, rootElementId, componentId))
                .thenReturn(componentCreatedRTMProducer);

        createInteractiveComponentMessageHandler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.interactive.component.create.ok\"," +
                "\"response\":{" +
                "\"component\":{" +
                "\"componentId\":\"" + componentId + "\"," +
                "\"config\":\"some config\"," +
                "\"plugin\":{" +
                "\"pluginId\":\"" + pluginId + "\"," +
                "\"name\":\"z650\"," +
                "\"type\":\"component\"," +
                "\"version\":\"v1.2.3\"" +
                "}," +
                "\"parentId\":\"" + parentId + "\"," +
                "\"parentType\":\"INTERACTIVE\"}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker, times(1)).broadcast(eq(message.getType()), captor.capture());
        verify(coursewareService).saveConfigurationFields(any(UUID.class), any(String.class));

        assertEquals(CoursewareAction.CREATED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(CoursewareElementType.COMPONENT, captor.getValue().getElement().getElementType());
        assertEquals(componentId, captor.getValue().getElement().getElementId());
        assertEquals(interactiveId, captor.getValue().getParentElement().getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, captor.getValue().getParentElement().getElementType());

        verify(componentCreatedRTMProducer, atLeastOnce()).buildComponentCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(componentId));
        verify(componentCreatedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_success_withComponentId() throws WriteResponseException {
        when(message.getComponentId()).thenReturn(componentId);
        when(componentService.createForInteractive(interactiveId, pluginId, pluginVersionExpr, config, componentId))
                .thenReturn(Mono.just(new Component().setId(UUID.randomUUID())));

        when(componentService.getComponentPayload(any(UUID.class))).thenReturn(Mono.just(new ComponentPayload()));
        when(componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext, rootElementId, null))
                .thenReturn(componentCreatedRTMProducer);

        createInteractiveComponentMessageHandler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.interactive.component.create.ok\"," +
                "\"response\":{" +
                "\"component\":{}" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_withConfig() throws WriteResponseException, VersionParserFault {
        UUID parentId = UUID.randomUUID();

        Component component = new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        PluginSummary plugin = new PluginSummary()
                .setId(pluginId)
                .setType(PluginType.COMPONENT)
                .setName("z650");

        ParentByComponent parent = new ParentByComponent()
                .setComponentId(componentId)
                .setParentType(CoursewareElementType.INTERACTIVE)
                .setParentId(parentId);

        ComponentPayload payload = ComponentPayload.from(component, config, plugin, parent, new CoursewareElementDescription(), new ArrayList<>());

        when(componentService.createForInteractive(any(UUID.class), any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(Mono.just(component));
        when(componentService.getComponentPayload(any(UUID.class))).thenReturn(Mono.just(payload));
        when(componentCreatedRTMProducer.buildComponentCreatedRTMConsumable(rtmClientContext, rootElementId, componentId))
                .thenReturn(componentCreatedRTMProducer);

        createInteractiveComponentMessageHandler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.interactive.component.create.ok\"," +
                "\"response\":{" +
                "\"component\":{" +
                "\"componentId\":\"" + componentId + "\"," +
                "\"config\":\"" + config + "\"," +
                "\"plugin\":{" +
                "\"pluginId\":\"" + pluginId + "\"," +
                "\"name\":\"z650\"," +
                "\"type\":\"component\"," +
                "\"version\":\"v1.2.3\"" +
                "}," +
                "\"parentId\":\"" + parentId + "\"," +
                "\"parentType\":\"INTERACTIVE\"}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(coursewareService).saveConfigurationFields(any(UUID.class), eq(config));
    }

    @Test
    void handle_pluginNotFoundException() throws WriteResponseException, VersionParserFault {
        TestPublisher<Component> componentPublisher = TestPublisher.create();
        componentPublisher.error(new PluginNotFoundFault("Plugin not found"));
        when(componentService.createForInteractive(any(UUID.class), any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(componentPublisher.mono());

        createInteractiveComponentMessageHandler.handle(session, message);

        String expected = "{\"type\":\"author.interactive.component.create.error\",\"code\":400,\"message\":\"Plugin not found\"}";

        MessageHandlerTestUtils.verifySentMessage(session, expected);
        verify(componentCreatedRTMProducer, never()).produce();
    }

    @Test
    void handle_versionNotParsed() throws WriteResponseException {
        TestPublisher<Component> componentPublisher = TestPublisher.create();
        componentPublisher.error(new VersionParserFault("version can't be parsed"));
        when(componentService.createForInteractive(any(UUID.class), any(UUID.class), any(String.class), any(String.class)))
                .thenReturn(componentPublisher.mono());

        createInteractiveComponentMessageHandler.handle(session, message);

        String expected = "{\"type\":\"author.interactive.component.create.error\",\"code\":400,\"message\":\"version can't be parsed\"}";

        MessageHandlerTestUtils.verifySentMessage(session, expected);
    }

}
