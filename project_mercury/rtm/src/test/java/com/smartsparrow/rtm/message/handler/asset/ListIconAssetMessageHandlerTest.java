package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.rtm.message.handler.asset.ListIconAssetMessageHandler.AUTHOR_ICON_ASSET_LIST_ERROR;
import static com.smartsparrow.rtm.message.handler.asset.ListIconAssetMessageHandler.AUTHOR_ICON_ASSET_LIST_OK;
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

import com.smartsparrow.asset.data.IconAssetSummary;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.asset.ListIconAssetMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class ListIconAssetMessageHandlerTest {

    private Session session;

    @InjectMocks
    private ListIconAssetMessageHandler handler;

    @Mock
    private BronteAssetService bronteAssetService;

    @Mock
    private ListIconAssetMessage message;

    private final String assetUrn = "urn:aero:" + UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        List<String> assetUrns = new ArrayList<>();
        assetUrns.add("MICROSOFT");
        assetUrns.add("FONTAWESOME");

        when(message.getIconLibraries()).thenReturn(assetUrns);
    }

    @Test
    void validate_noIconLibraries() {
        when(message.getIconLibraries()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("icon library is required", ex.getMessage());
    }

    @Test
    void validate_emptyIconLibraries() {
        when(message.getIconLibraries()).thenReturn(new ArrayList<>());
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("icon library is required", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(bronteAssetService.fetchIconAssetsByLibrary(message.getIconLibraries()))
                .thenReturn(Flux.just(new IconAssetSummary()
                                              .setAssetUrn(assetUrn)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ICON_ASSET_LIST_OK, response.getType());
                List assetsByIconLibraries = (List) response.getResponse().get("iconAssetSummaries");
                assertNotNull(assetsByIconLibraries);
                assertEquals(1, assetsByIconLibraries.size());
                assertEquals(assetUrn, ((Map) assetsByIconLibraries.get(0)).get("assetUrn"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        Flux flux = TestPublisher.create().error(new RuntimeException("someException")).flux();
        when(bronteAssetService.fetchIconAssetsByLibrary(message.getIconLibraries()))
                .thenReturn(flux);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_ICON_ASSET_LIST_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to fetch icon assets by library\"}");
    }

}
