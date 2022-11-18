package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.theme.DeleteThemeVariantMessageHandler.AUTHOR_THEME_VARIANT_DELETE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.theme.DeleteThemeVariantMessageHandler.AUTHOR_THEME_VARIANT_DELETE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.theme.DeleteThemeVariantMessage;
import com.smartsparrow.workspace.data.ThemeVariant;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class DeleteThemeVariantMessageHandlerTest {
    private Session session;

    @InjectMocks
    DeleteThemeVariantMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private DeleteThemeVariantMessage message;

    private static final UUID themeId = UUID.randomUUID();
    private static final String variantName = "Day";
    private static final UUID variantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getThemeId()).thenReturn(themeId);

        session = mockSession();
        when(message.getVariantId()).thenReturn(variantId);

        when(message.getThemeId()).thenReturn(themeId);
        handler = new DeleteThemeVariantMessageHandler(themeService);
    }

    @Test
    void validate_noThemeId() {
        when(message.getThemeId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing themeId", ex.getMessage());
    }

    @Test
    void validate_noVariantId() {
        when(message.getVariantId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing variantId", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(themeService.deleteThemeVariant(themeId, variantId))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_THEME_VARIANT_DELETE_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Flux flux = TestPublisher.create().error(new RuntimeException("someException")).flux();
        when(themeService.deleteThemeVariant(themeId, variantId))
                .thenReturn(flux);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_THEME_VARIANT_DELETE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error deleting theme variant\"}");
    }
}
