package com.smartsparrow.rtm.message.handler.learner.theme;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.learner.theme.GetSelectedThemeMessageHandler.LEARNER_SELECTED_THEME_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.learner.theme.GetSelectedThemeMessageHandler.LEARNER_SELECTED_THEME_GET_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerSelectedThemePayload;
import com.smartsparrow.learner.data.LearnerThemeVariant;
import com.smartsparrow.learner.service.LearnerThemeService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.learner.theme.GetSelectedThemeMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class GetSelectedThemeMessageHandlerTest {
    private Session session;

    @InjectMocks
    GetSelectedThemeMessageHandler handler;

    @Mock
    private LearnerThemeService themeService;

    @Mock
    private GetSelectedThemeMessage message;

    private static final UUID themeId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID variantId = UUID.randomUUID();
    private static final String variantName = "Day";
    private static final String themeName = "theme_one";
    private static final String config = " {\"colo\":\"orange\", \"margin\":\"20\"}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getElementId()).thenReturn(elementId);

        session = mockSession();

        handler = new GetSelectedThemeMessageHandler(themeService);
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementId is required", ex.getMessage());
    }


    @Test
    void handle() throws IOException {
        when(themeService.fetchSelectedTheme(elementId))
                .thenReturn(Mono.just(new LearnerSelectedThemePayload()
                                              .setElementId(elementId)
                                              .setThemeId(themeId)
                                              .setThemeName("theme_one")
                                              .setThemeVariants(Arrays.asList(new LearnerThemeVariant()
                                                                                      .setVariantId(variantId)
                                                                                      .setVariantName(variantName)
                                                                                      .setConfig(config)))));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(LEARNER_SELECTED_THEME_GET_OK, response.getType());
                Map themePayload = (Map) response.getResponse().get("selectedThemePayload");
                assertNotNull(themePayload);
                assertEquals(elementId.toString(), themePayload.get("elementId"));
                assertEquals(themeName, themePayload.get("themeName"));
            });
        });

    }

    @Test
    void handle_exception() throws IOException {
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();

        when(themeService.fetchSelectedTheme(elementId))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + LEARNER_SELECTED_THEME_GET_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to fetch learner selected theme payload\"}");
    }
}
