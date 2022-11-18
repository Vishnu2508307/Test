package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.GetAssetMessageHandler.AUTHOR_ASSET_GET_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.service.WorkspaceAssetService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.GetAssetMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GetAssetMessageHandlerTest {

    @InjectMocks
    private GetAssetMessageHandler handler;

    @Mock
    private GetAssetMessage message;
    @Mock
    private WorkspaceAssetService workspaceAssetService;
    private Session session;

    private static final String urn = "urn:aero:uuid";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        when(message.getUrn()).thenReturn(urn);
    }

    @Test
    void validate_noUrn() {
        when(message.getUrn()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("urn is required", t.getErrorMessage());
        assertEquals(AUTHOR_ASSET_GET_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void handle() {
        AssetPayload payload = new AssetPayload()
                .setUrn(urn)
                .putSource("original", new HashMap<String, Object>() {
                    {
                        put("url", "url for original image");
                        put("width", 72.0);
                        put("height", 100.0);
                    }
                }).putMetadata("mime-type", "image/jpeg");
        when(workspaceAssetService.getAssetPayload(urn)).thenReturn(Mono.just(payload));

        handler.handle(session, message);

        verifySentMessage(session, "{" +
                "\"type\":\"author.asset.get.ok\"," +
                "\"response\":{" +
                    "\"asset\":{" +
                        "\"urn\":\"urn:aero:uuid\"," +
                        "\"source\":{" +
                            "\"original\":{" +
                                "\"width\":72.0," +
                                "\"url\":\"url for original image\"," +
                                "\"height\":100.0" +
                            "}" +
                        "}," +
                        "\"metadata\":{" +
                            "\"mime-type\":\"image/jpeg\"" +
                        "}" +
                    "}" +
                "}}");
    }

    @Test
    void handle_invalidUrn() {
        when(workspaceAssetService.getAssetPayload(urn)).thenThrow(new AssetURNParseException(urn));

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_ASSET_GET_ERROR + "\",\"code\":400," +
                "\"message\":\"invalid URN\"}");
    }

    @Test
    void handle_error() {
        TestPublisher<AssetPayload> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));
        when(workspaceAssetService.getAssetPayload(urn)).thenReturn(publisher.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_ASSET_GET_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to fetch asset\"}");
    }
}
