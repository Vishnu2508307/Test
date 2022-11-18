package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.theme.WorkspaceGetThemeVariantMessageHandler.WORKSPACE_THEME_VARIANT_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.theme.WorkspaceGetThemeVariantMessageHandler.WORKSPACE_THEME_VARIANT_GET_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.theme.GetThemeVariantMessage;
import com.smartsparrow.workspace.data.ThemeVariant;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class WorkspaceGetThemeVariantMessageHandlerTest {
    private Session session;

    @InjectMocks
    WorkspaceGetThemeVariantMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private GetThemeVariantMessage message;

    private static final UUID themeId = UUID.randomUUID();
    private static final UUID variantId = UUID.randomUUID();
    private static final String variantName = "Day";
    private static final String config = " {\"color\":\"orange\", \"margin\":\"20\"}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getThemeId()).thenReturn(themeId);
        when(message.getVariantId()).thenReturn(variantId);

        session = mockSession();

        handler = new WorkspaceGetThemeVariantMessageHandler(themeService);
    }

    @Test
    void validate_noVariantId() {
        when(message.getVariantId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing variantId", ex.getMessage());
    }

    @Test
    void validate_noThemeId() {
        when(message.getThemeId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing themeId", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(themeService.getThemeVariant(themeId, variantId))
                .thenReturn(Mono.just(new ThemeVariant()
                                              .setThemeId(themeId)
                                              .setVariantId(variantId)
                                              .setVariantName(variantName)
                                              .setConfig(config)
                ));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_THEME_VARIANT_GET_OK, response.getType());
                Map theme= (Map) response.getResponse().get("themeVariant");
                assertNotNull(theme);
                assertEquals(variantName, theme.get("variantName"));
                assertEquals(config, theme.get("config"));
            });
        });

    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(themeService.getThemeVariant(themeId, variantId))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + WORKSPACE_THEME_VARIANT_GET_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to get a theme variant\"}");
    }
}
