package com.smartsparrow.rtm.message.handler.asset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetSignature;
import com.smartsparrow.asset.data.AssetSignatureStrategyType;
import com.smartsparrow.asset.service.AssetSignatureService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.asset.CreateAssetSignatureConfigMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CreateAssetSignatureConfigMessageHandlerTest {

    @InjectMocks
    private CreateAssetSignatureConfigMessageHandler handler;

    @Mock
    private CreateAssetSignatureConfigMessage message;

    @Mock
    private AssetSignatureService assetSignatureService;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final String host = "host";
    private static final String path = "path";
    private static final String config = "config";
    private static final AssetSignatureStrategyType type = AssetSignatureStrategyType.AKAMAI_TOKEN_AUTHENTICATION;
    private static final UUID signatureId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(assetSignatureService.create(host, path, config, type))
                .thenReturn(Mono.just(new AssetSignature()
                        .setAssetSignatureStrategyType(type)
                        .setHost(host)
                        .setPath(path)
                        .setConfig(config)
                        .setId(signatureId)));

        when(message.getHost()).thenReturn(host);
        when(message.getPath()).thenReturn(path);
        when(message.getConfig()).thenReturn(config);
        when(message.getStrategyType()).thenReturn(type);
    }

    @Test
    void validate_noHost() {
        when(message.getHost()).thenReturn(null);

        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("host is required", f1.getMessage());

        when(message.getHost()).thenReturn("");

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("host is required", f2.getMessage());

    }

    @Test
    void validate_noPath() {
        when(message.getPath()).thenReturn(null);

        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("path is required", f1.getMessage());
    }

    @Test
    void validate_noConfig() {
        when(message.getConfig()).thenReturn(null);

        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("config is required", f1.getMessage());

        when(message.getConfig()).thenReturn("");

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("config is required", f2.getMessage());
    }

    @Test
    void validate_noType() {
        when(message.getStrategyType()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("signatureStrategy is required", f.getMessage());
    }

    @Test
    void validate() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<AssetSignature> publisher = TestPublisher.create();
        publisher.error(new RuntimeException());
        when(assetSignatureService.create(host, path, config, type)).thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"asset.signature.config.create.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error creating the asset signature config\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle() throws WriteResponseException {
        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"asset.signature.config.create.ok\"," +
                            "\"response\":{" +
                                "\"assetSignature\":{" +
                                    "\"id\":\"" + signatureId + "\"," +
                                    "\"assetSignatureStrategyType\":\"AKAMAI_TOKEN_AUTHENTICATION\"," +
                                    "\"host\":\"host\"," +
                                    "\"path\":\"path\"," +
                                    "\"config\":\"config\"" +
                                "}" +
                            "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}