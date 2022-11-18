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

import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.asset.service.AssetService;
import com.smartsparrow.asset.service.AssetUtils;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.GetAssetMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GetLearnerAssetMessageHandlerTest {

    @InjectMocks
    private GetLearnerAssetMessageHandler handler;

    @Mock
    private AssetService assetService;

    @Mock
    private GetAssetMessage message;

    private static final AssetSummary assetSummary = new AssetSummary()
            .setId(UUID.randomUUID())
            .setProvider(AssetProvider.EXTERNAL);

    private static final String urn = AssetUtils.buildURN(assetSummary);
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getUrn()).thenReturn(urn);

        when(assetService.getAssetPayload(urn)).thenReturn(Mono.just(new AssetPayload()));
    }

    @Test
    void validate_nullOrEmptyURN() {
        when(message.getUrn()).thenReturn(null);

        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("urn is required", f1.getMessage());

        when(message.getUrn()).thenReturn("");

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("urn is required", f2.getMessage());
    }

    @Test
    void validate() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<AssetPayload> assetPayloadTestPublisher = TestPublisher.create();
        assetPayloadTestPublisher.error(new RuntimeException());

        when(assetService.getAssetPayload(urn)).thenReturn(assetPayloadTestPublisher.mono());

        handler.handle(session, message);

        final String expected = "{\"type\":\"learner.asset.get.error\",\"code\":422,\"message\":\"Unable to fetch asset\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle() throws WriteResponseException {
        handler.handle(session, message);

        final String expected = "{\"type\":\"learner.asset.get.ok\",\"response\":{\"asset\":{}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}