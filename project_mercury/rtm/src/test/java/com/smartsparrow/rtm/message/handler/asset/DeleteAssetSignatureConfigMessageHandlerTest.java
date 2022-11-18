package com.smartsparrow.rtm.message.handler.asset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.service.AssetSignatureService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.asset.DeleteAssetSignatureConfigMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class DeleteAssetSignatureConfigMessageHandlerTest {

    @InjectMocks
    private DeleteAssetSignatureConfigMessageHandler handler;

    @Mock
    private AssetSignatureService assetSignatureService;

    @Mock
    private DeleteAssetSignatureConfigMessage message;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final String host = "host";
    private static final String path = "path";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(assetSignatureService.delete(host, path))
                .thenReturn(Flux.just(new Void[]{}));

        when(message.getHost()).thenReturn(host);
        when(message.getPath()).thenReturn(path);
    }

    @Test
    void validate_nullOrEmtpyHost() {
        when(message.getHost()).thenReturn(null);

        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("host is required", f1.getMessage());

        when(message.getHost()).thenReturn("");

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("host is required", f2.getMessage());
    }

    @Test
    void validate_nullPath() {
        when(message.getPath()).thenReturn(null);

        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("path is required", f1.getMessage());
    }

    @Test
    void validate() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException());
        when(assetSignatureService.delete(host, path)).thenReturn(publisher.flux());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"asset.signature.config.delete.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error deleting the asset signature config\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle() throws WriteResponseException {
        handler.handle(session, message);

        String expected = "{\"type\":\"asset.signature.config.delete.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}