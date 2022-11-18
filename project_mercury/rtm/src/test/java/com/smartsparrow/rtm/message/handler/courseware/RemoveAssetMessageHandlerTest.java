package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.RemoveAssetMessageHandler.AUTHOR_COURSEWARE_ASSET_REMOVE;
import static com.smartsparrow.rtm.message.handler.courseware.RemoveAssetMessageHandler.AUTHOR_COURSEWARE_ASSET_REMOVE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.RemoveAssetMessageHandler.AUTHOR_COURSEWARE_ASSET_REMOVE_OK;
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
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.RemoveAssetMessage;
import com.smartsparrow.rtm.subscription.courseware.assetremoved.AssetRemovedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class RemoveAssetMessageHandlerTest {

    private RemoveAssetMessageHandler handler;

    @Mock
    private CoursewareAssetService coursewareAssetService;
    @Mock
    private CoursewareService coursewareService;

    @Mock
    private RemoveAssetMessage message;
    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    @Mock
    private RTMEventBroker rtmEventBroker;
    @Mock
    private AssetRemovedRTMProducer assetRemovedRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private Session session;
    private AuthenticationContextProvider authenticationContextProvider;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = ACTIVITY;
    private static final String assetUrn = "urn:aero:assetId";
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
        when(assetRemovedRTMProducer.buildAssetRemovedRTMConsumable(rtmClientContext, rootElementId, elementId, elementType))
                .thenReturn(assetRemovedRTMProducer);

        handler = new RemoveAssetMessageHandler(
                coursewareAssetService,
                rtmEventBrokerProvider,
                authenticationContextProvider,
                coursewareService,
                rtmClientContextProvider,
                assetRemovedRTMProducer
        );
    }

    @SuppressWarnings("Duplicates")
    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("elementId is required", t.getErrorMessage());
        assertEquals(AUTHOR_COURSEWARE_ASSET_REMOVE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @SuppressWarnings("Duplicates")
    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("elementType is required", t.getErrorMessage());
        assertEquals(AUTHOR_COURSEWARE_ASSET_REMOVE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @SuppressWarnings("Duplicates")
    @Test
    void validate_noAssetUrn() {
        when(message.getAssetURN()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("assetURN is required", t.getErrorMessage());
        assertEquals(AUTHOR_COURSEWARE_ASSET_REMOVE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());

        when(message.getAssetURN()).thenReturn("");
        assertThrows(RTMValidationException.class, () -> handler.validate(message));
    }

    @Test
    void handle() {
        when(coursewareAssetService.removeAsset(elementId, assetUrn, rootElementId)).thenReturn(Flux.empty());
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_COURSEWARE_ASSET_REMOVE_OK + "\"}");
    }

    @Test
    void handle_broadcastMessage() {
        when(coursewareAssetService.removeAsset(elementId, assetUrn, rootElementId)).thenReturn(Flux.empty());
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        handler.handle(session, message);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);
        verify(rtmEventBroker).broadcast(eq(AUTHOR_COURSEWARE_ASSET_REMOVE), captor.capture());
        assertEquals(CoursewareAction.ASSET_REMOVED, captor.getValue().getAction());
        assertEquals(accountId, captor.getValue().getAccountId());
        assertEquals(elementId, captor.getValue().getElement().getElementId());
        assertEquals(elementType, captor.getValue().getElement().getElementType());
        assertNull(captor.getValue().getParentElement());

        verify(assetRemovedRTMProducer, atLeastOnce()).buildAssetRemovedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(elementId), eq(elementType));
        verify(assetRemovedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_invalidUrn() {
        when(coursewareAssetService.removeAsset(elementId, assetUrn, rootElementId)).thenThrow(new AssetURNParseException(assetUrn));
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_COURSEWARE_ASSET_REMOVE_ERROR + "\",\"code\":500," +
                "\"message\":\"Unable to remove asset\"}");
        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(assetRemovedRTMProducer, never()).produce();
    }


    @Test
    void handle_error() {
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));
        when(coursewareAssetService.removeAsset(elementId, assetUrn, rootElementId)).thenReturn(publisher.flux());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_COURSEWARE_ASSET_REMOVE_ERROR + "\",\"code\":500," +
                "\"message\":\"Unable to remove asset\"}");
        verify(rtmEventBroker, never()).broadcast(any(), any());
        verify(assetRemovedRTMProducer, never()).produce();
    }
}
