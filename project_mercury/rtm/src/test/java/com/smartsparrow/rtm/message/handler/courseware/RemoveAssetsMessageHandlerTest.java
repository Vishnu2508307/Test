package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.RemoveAssetsMessageHandler.AUTHOR_COURSEWARE_ASSETS_REMOVE;
import static com.smartsparrow.rtm.message.handler.courseware.RemoveAssetsMessageHandler.AUTHOR_COURSEWARE_ASSETS_REMOVE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.RemoveAssetsMessageHandler.AUTHOR_COURSEWARE_ASSETS_REMOVE_OK;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareAssetService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.RemoveAssetsMessage;
import com.smartsparrow.rtm.subscription.courseware.assetsremoved.AssetsRemovedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class RemoveAssetsMessageHandlerTest {

    private RemoveAssetsMessageHandler handler;

    @Mock
    private CoursewareAssetService coursewareAssetService;
    @Mock
    private CoursewareService coursewareService;

    @Mock
    private RemoveAssetsMessage message;
    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    @Mock
    private RTMEventBroker rtmEventBroker;
    @Mock
    private AssetsRemovedRTMProducer assetsRemovedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private Session session;
    private AuthenticationContextProvider authenticationContextProvider;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final List<String> assetUrn = Collections.emptyList();
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        when(message.getElementId()).thenReturn(elementId);
        when(message.getAssetURN()).thenReturn(assetUrn);
        when(message.getElementType()).thenReturn(elementType);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        authenticationContextProvider = mock(AuthenticationContextProvider.class);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                                                                    .setId(accountId));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(assetsRemovedRTMProducer.buildAssetsRemovedRTMConsumable(rtmClientContext, rootElementId, elementId, elementType))
                .thenReturn(assetsRemovedRTMProducer);

        handler = new RemoveAssetsMessageHandler(
                coursewareAssetService,
                rtmEventBrokerProvider,
                authenticationContextProvider,
                coursewareService,
                rtmClientContextProvider,
                assetsRemovedRTMProducer
        );
    }

    @SuppressWarnings("Duplicates")
    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> handler.validate(message));
        assertEquals("elementId is required", e.getMessage());
    }

    @SuppressWarnings("Duplicates")
    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> handler.validate(message));
        assertEquals("elementType is required", e.getMessage());
    }

    @SuppressWarnings("Duplicates")
    @Test
    void validate_noAssetUrn() {
        when(message.getAssetURN()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> handler.validate(message));
        assertEquals("assetURN is required", e.getMessage());


        when(message.getAssetURN()).thenReturn(Collections.emptyList());
        assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
    }

    @Test
    void handle() {
        when(coursewareAssetService.removeAssets(elementId, assetUrn, rootElementId)).thenReturn(Flux.empty());
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_COURSEWARE_ASSETS_REMOVE_OK + "\"}");
    }

    @Test
    void handle_broadcastMessage() {
        when(coursewareAssetService.removeAssets(elementId, assetUrn, rootElementId)).thenReturn(Flux.empty());
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        handler.handle(session, message);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_COURSEWARE_ASSETS_REMOVE), captor.capture());
        assertEquals(CoursewareAction.ASSETS_REMOVED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(elementId, captor.getValue().getElement().getElementId());
        assertEquals(elementType, captor.getValue().getElement().getElementType());
        assertNull(captor.getValue().getParentElement());

        verify(assetsRemovedRTMProducer, atLeastOnce()).buildAssetsRemovedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(elementId), eq(elementType));
        verify(assetsRemovedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_invalidUrn() {
        when(coursewareAssetService.removeAssets(elementId, assetUrn, rootElementId)).thenThrow(new AssetURNParseException(assetUrn));
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_COURSEWARE_ASSETS_REMOVE_ERROR + "\",\"code\":400," +
                "\"message\":\"Invalid asset URN\"}");
        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(assetsRemovedRTMProducer, never()).produce();
    }


    @Test
    void handle_error() {
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));
        when(coursewareAssetService.removeAssets(elementId, assetUrn, rootElementId)).thenReturn(publisher.flux());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_COURSEWARE_ASSETS_REMOVE_ERROR + "\",\"code\":500," +
                "\"message\":\"Unable to remove assets\"}");
        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(assetsRemovedRTMProducer, never()).produce();
    }
}
