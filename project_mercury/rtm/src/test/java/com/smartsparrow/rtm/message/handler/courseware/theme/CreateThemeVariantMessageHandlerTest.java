package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.theme.CreateThemeVariantMessageHandler.AUTHOR_THEME_VARIANT_CREATE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.theme.CreateThemeVariantMessageHandler.AUTHOR_THEME_VARIANT_CREATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
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
import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.theme.CreateThemeVariantMessage;
import com.smartsparrow.workspace.data.ThemeVariant;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class CreateThemeVariantMessageHandlerTest {
    private Session session;

    @InjectMocks
    CreateThemeVariantMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private CreateThemeVariantMessage message;

    private static final UUID themeId = UUID.randomUUID();
    private static final String variantName = "Day";
    private static final String config = " {\"colo\":\"orange\", \"margin\":\"20\"}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getThemeId()).thenReturn(themeId);
        when(message.getVariantName()).thenReturn(variantName);
        when(message.getConfig()).thenReturn(config);
        when(message.getState()).thenReturn(ThemeState.DEFAULT);

        session = mockSession();

        handler = new CreateThemeVariantMessageHandler(themeService);
    }

    @Test
    void validate_noVariantName() {
        when(message.getVariantName()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing variant name", ex.getMessage());
    }

    @Test
    void validate_noThemeId() {
        when(message.getThemeId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing themeId", ex.getMessage());
    }

    @Test
    void validate_noConfig() {
        when(message.getConfig()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing config", ex.getMessage());
    }


    @Test
    void handle() throws IOException {
        when(themeService.createThemeVariant(themeId, variantName, config, ThemeState.DEFAULT))
                .thenReturn(Mono.just(new ThemeVariant()
                                              .setThemeId(themeId)
                        .setVariantName(variantName)
                        .setConfig(config)
                       ));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_THEME_VARIANT_CREATE_OK, response.getType());
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
        when(themeService.createThemeVariant(themeId, variantName, config, ThemeState.DEFAULT))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_THEME_VARIANT_CREATE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to create a theme variant\"}");
    }
}
