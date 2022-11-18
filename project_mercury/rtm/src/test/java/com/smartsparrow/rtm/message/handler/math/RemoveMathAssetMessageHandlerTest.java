package com.smartsparrow.rtm.message.handler.math;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.math.RemoveMathAssetMessageHandler.AUTHOR_MATH_ASSET_REMOVE_ERROR;
import static com.smartsparrow.rtm.message.handler.math.RemoveMathAssetMessageHandler.AUTHOR_MATH_ASSET_REMOVE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.math.service.MathAssetService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.math.MathAssetRemoveMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class RemoveMathAssetMessageHandlerTest {

    @InjectMocks
    private RemoveMathAssetMessageHandler handler;

    @Mock
    private MathAssetService mathAssetService;

    @Mock
    private MathAssetRemoveMessage message;

    private Session session;

    private static final String assetUrn = "urn:math:6c535950-01b9-11e9-8b4b-9bcfd203ee5e";
    private static final UUID elementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();

        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(ACTIVITY);
        when(message.getAssetUrn()).thenReturn(assetUrn);
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementId is required", ex.getMessage());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementType is required", ex.getMessage());
    }

    @Test
    void validate_noAssetUrn() {
        when(message.getAssetUrn()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("assetUrn is required", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(mathAssetService.removeMathAsset(eq(elementId), eq(assetUrn)))
                .thenReturn(Flux.empty());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_MATH_ASSET_REMOVE_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new RuntimeException("can't fetch"));
        when(mathAssetService.removeMathAsset(eq(elementId), eq(assetUrn)))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_MATH_ASSET_REMOVE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error removing Math asset\"}");
    }
}
