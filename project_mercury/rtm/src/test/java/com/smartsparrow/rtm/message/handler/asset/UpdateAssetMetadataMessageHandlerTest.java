package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.asset.UpdateAssetMetadataMessageHandler.AUTHOR_ASSET_METADATA_UPDATE_ERROR;
import static com.smartsparrow.rtm.message.handler.asset.UpdateAssetMetadataMessageHandler.AUTHOR_ASSET_METADATA_UPDATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.courseware.service.WorkspaceAssetService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.asset.UpdateAssetMetadataMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class UpdateAssetMetadataMessageHandlerTest {

    private Session session;

    @InjectMocks
    private UpdateAssetMetadataMessageHandler handler;

    @Mock
    private WorkspaceAssetService workspaceAssetService;

    @Mock
    private UpdateAssetMetadataMessage message;

    private final static String assetUrn = "urn:aero:" + UUID.randomUUID();
    private final static UUID assetId = UUID.randomUUID();
    private final static String metadataKey = "altText";
    private final static String metadataValue = "small image";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();

        when(message.getAssetUrn()).thenReturn(assetUrn);
        when(message.getKey()).thenReturn(metadataKey);
        when(message.getValue()).thenReturn(metadataValue);

        handler = new UpdateAssetMetadataMessageHandler(workspaceAssetService);


    }

    @Test
    void validate_noAssetUrn() {
        when(message.getAssetUrn()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("assetUrn is required", ex.getMessage());
    }

    @Test
    void validate_noMetadataKey() {
        when(message.getKey()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("metadata key is missing", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(workspaceAssetService.updateAssetMetadata(anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(new AssetMetadata()
                                              .setAssetId(assetId)
                                              .setKey(metadataKey)
                                              .setValue(metadataValue)
                ));


        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ASSET_METADATA_UPDATE_OK, response.getType());
                Map assetMetadata = (Map) response.getResponse().get("assetMetadata");
                assertNotNull(assetMetadata);
                assertEquals(assetId.toString(), assetMetadata.get("assetId"));
                assertEquals(metadataKey, assetMetadata.get("key"));
                assertEquals(metadataValue, assetMetadata.get("value"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(workspaceAssetService.updateAssetMetadata(assetUrn, metadataKey, metadataValue))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_ASSET_METADATA_UPDATE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to update asset metadata\"}");
    }

}
