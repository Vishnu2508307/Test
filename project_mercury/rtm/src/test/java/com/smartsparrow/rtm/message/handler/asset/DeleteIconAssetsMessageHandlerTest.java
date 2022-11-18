package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.rtm.message.handler.asset.DeleteIconAssetsMessageHandler.AUTHOR_ICON_ASSET_DELETE_ERROR;
import static com.smartsparrow.rtm.message.handler.asset.DeleteIconAssetsMessageHandler.AUTHOR_ICON_ASSET_DELETE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.asset.DeleteIconAssetMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class DeleteIconAssetsMessageHandlerTest {

    private Session session;

    @InjectMocks
    private DeleteIconAssetsMessageHandler handler;

    @Mock
    private BronteAssetService bronteAssetService;

    @Mock
    private DeleteIconAssetMessage message;

    private static final String iconLibrary = "Microsoft Icon";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getIconLibrary()).thenReturn(iconLibrary);
    }

    @Test
    void validate_missingIconLibrary() {
        when(message.getIconLibrary()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing icon library", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(bronteAssetService.deleteIconAssetsByLibrary(iconLibrary))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ICON_ASSET_DELETE_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        Flux flux = TestPublisher.create().error(new RuntimeException("someException")).flux();
        when(bronteAssetService.deleteIconAssetsByLibrary(iconLibrary))
                .thenReturn(flux);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_ICON_ASSET_DELETE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error deleting icon assets by library\"}");
    }

}
