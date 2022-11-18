package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.rtm.message.handler.asset.ListAssetMessageHandler.AUTHOR_ASSET_LIST_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.service.WorkspaceAssetService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.asset.ListAssetMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class ListAssetMessageHandlerTest {

    private Session session;

    @InjectMocks
    private ListAssetMessageHandler handler;

    @Mock
    private WorkspaceAssetService workspaceAssetService;

    @Mock
    private ListAssetMessage message;

    private final String assetUrn = "urn:aero:" + UUID.randomUUID();
    private final String assetUrn_one = "urn:aero:" + UUID.randomUUID();
    List<String> assetUrns = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        assetUrns.add(assetUrn);
        assetUrns.add(assetUrn_one);

        when(message.getAssetUrns()).thenReturn(assetUrns);
    }

    @Test
    void validate_missingUrn() {
        when(message.getAssetUrns()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("urn is required", ex.getMessage());
    }

    @Test
    void validate_emptyUrnList() {
        when(message.getAssetUrns()).thenReturn(new ArrayList<>());
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("urn is required", ex.getMessage());
    }

    @Test
    void validate_limitSizeExceedMAX() {
        when(message.getLimit()).thenReturn(21);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("limit size exceeds the max limit size 20", ex.getMessage());
    }

    @Test
    void validate_missingLimit() {
        when(message.getLimit()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("limit is required", ex.getMessage());
    }

    @Test
    void validate_ExceedLimitSize() {
        when(message.getAssetUrns()).thenReturn(assetUrns);
        when(message.getLimit()).thenReturn(1);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("asset urn list exceeds the limit size", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(workspaceAssetService.getAssetPayload(assetUrns))
                .thenReturn(Flux.just(new AssetPayload()
                                              .setUrn(assetUrn)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ASSET_LIST_OK, response.getType());
                List assetPayloads = (List) response.getResponse().get("assetPayloads");
                assertNotNull(assetPayloads);
                assertEquals(1, assetPayloads.size());
                assertEquals(assetUrn, ((Map) assetPayloads.get(0)).get("urn"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        Flux flux = TestPublisher.create().error(new RuntimeException("someException")).flux();
        when(workspaceAssetService.getAssetPayload(assetUrns))
                .thenReturn(flux);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + ListAssetMessageHandler.AUTHOR_ASSET_LIST_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to fetch asset payloads by urns\"}");
    }

}
