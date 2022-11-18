package com.smartsparrow.rtm.message.handler.math;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.math.MathAssetGetMessageHandler.AUTHOR_MATH_ASSET_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.math.MathAssetGetMessageHandler.AUTHOR_MATH_ASSET_GET_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.math.data.AssetSummary;
import com.smartsparrow.math.service.MathAssetService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.math.MathAssetGetMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class MathAssetGetMessageHandlerTest {

    @InjectMocks
    private MathAssetGetMessageHandler handler;

    @Mock
    private MathAssetService mathAssetService;

    @Mock
    private MathAssetGetMessage message;

    @Mock
    private AssetSummary assetSummary;

    private Session session;

    private static final String assetUrn = "urn:math:6c535950-01b9-11e9-8b4b-9bcfd203ee5e";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();

        when(message.getUrn()).thenReturn(assetUrn);
    }

    @Test
    void validate_noUrn() {
        when(message.getUrn()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("urn is required", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(mathAssetService.getMathAssetSummary(eq(assetUrn)))
                .thenReturn(Mono.just(assetSummary));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_MATH_ASSET_GET_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<AssetSummary> error = TestPublisher.create();
        error.error(new RuntimeException("can't fetch"));
        when(mathAssetService.getMathAssetSummary(eq(assetUrn)))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_MATH_ASSET_GET_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error fetching Math asset\"}");
    }
}
