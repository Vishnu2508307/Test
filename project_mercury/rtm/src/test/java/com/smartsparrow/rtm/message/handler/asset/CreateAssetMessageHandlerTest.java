package com.smartsparrow.rtm.message.handler.asset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetVisibility;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.asset.CreateAssetMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CreateAssetMessageHandlerTest {

    @InjectMocks
    private CreateAssetMessageHandler handler;

    @Mock
    private BronteAssetService bronteAssetService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private CreateAssetMessage message;

    private static final String url = "http://some-asset.tdl/document.txt";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getAssetProvider()).thenReturn(AssetProvider.EXTERNAL);
        when(message.getUrl()).thenReturn(url);
        when(message.getMediaType()).thenReturn(AssetMediaType.DOCUMENT);
        when(message.getMetadata()).thenReturn(new HashMap<>());
        when(message.getAssetVisibility()).thenReturn(AssetVisibility.GLOBAL);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account());
    }

    @Test
    void validate_noUrl() {
        when(message.getUrl()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("url is required", f.getMessage());
    }

    @Test
    void validate_noMediaType() {
        when(message.getMediaType()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("mediaType is required", f.getMessage());
    }

    @Test
    void validate_noAssetProvider() {
        when(message.getAssetProvider()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("assetProvider is required", f.getMessage());
    }

    @Test
    void validate_noVisibility() {
        when(message.getAssetVisibility()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("assetVisibility is required", f.getMessage());
    }

    @Test
    void validate_assetProviderAERO() {
        when(message.getAssetProvider()).thenReturn(AssetProvider.AERO);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("AERO provider not supported by this message", f.getMessage());
    }

    @Test
    void validate() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<AssetPayload> payloadTestPublisher = TestPublisher.create();
        payloadTestPublisher.error(new RuntimeException("fubar"));

        when(bronteAssetService.create(eq(url), eq(AssetVisibility.GLOBAL), any(Account.class), eq(AssetMediaType.DOCUMENT),
                any(Map.class), eq(AssetProvider.EXTERNAL))).thenReturn(payloadTestPublisher.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"author.asset.create.error\",\"code\":422,\"message\":\"error creating the asset\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success() throws WriteResponseException {

        when(bronteAssetService.create(eq(url), eq(AssetVisibility.GLOBAL), any(Account.class), eq(AssetMediaType.DOCUMENT),
                any(Map.class), eq(AssetProvider.EXTERNAL))).thenReturn(Mono.just(new AssetPayload()));

        handler.handle(session, message);

        String expected = "{\"type\":\"author.asset.create.ok\",\"response\":{\"asset\":{}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}