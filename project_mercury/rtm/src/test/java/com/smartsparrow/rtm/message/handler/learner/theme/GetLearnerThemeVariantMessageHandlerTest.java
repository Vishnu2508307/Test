package com.smartsparrow.rtm.message.handler.learner.theme;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.learner.theme.GetLearnerThemeVariantMessageHandler.LEARNER_THEME_VARIANT_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.learner.theme.GetLearnerThemeVariantMessageHandler.LEARNER_THEME_VARIANT_GET_OK;
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

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerThemeVariant;
import com.smartsparrow.learner.service.LearnerThemeService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.learner.theme.GetLearnerThemeVariantMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class GetLearnerThemeVariantMessageHandlerTest {
    private Session session;

    @InjectMocks
    GetLearnerThemeVariantMessageHandler handler;

    @Mock
    private LearnerThemeService learnerThemeService;

    @Mock
    private GetLearnerThemeVariantMessage message;

    private static final UUID themeId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID variantId = UUID.randomUUID();
    private static final String variantName = "Day";
    private static final String themeName = "theme_one";
    private static final String config = " {\"colo\":\"orange\", \"margin\":\"20\"}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getThemeId()).thenReturn(themeId);
        when(message.getVariantId()).thenReturn(variantId);

        session = mockSession();

        handler = new GetLearnerThemeVariantMessageHandler(learnerThemeService);
    }

    @Test
    void validate_noThemeId() {
        when(message.getThemeId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("themeId is required", ex.getMessage());
    }

    @Test
    void validate_noVariantId() {
        when(message.getVariantId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("variantId is required", ex.getMessage());
    }


    @Test
    void handle() throws IOException {
        when(learnerThemeService.fetchThemeVariant(themeId, variantId))
                .thenReturn(Mono.just(new LearnerThemeVariant()
                                              .setVariantName(variantName)
                                              .setConfig(config)
                                              .setThemeId(themeId)
                                              .setVariantId(variantId)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(LEARNER_THEME_VARIANT_GET_OK, response.getType());
                Map themeVariant = (Map) response.getResponse().get("themeVariant");
                assertNotNull(themeVariant);
                assertEquals(variantName, themeVariant.get("variantName"));
                assertEquals(themeId.toString(), themeVariant.get("themeId"));
                assertEquals(variantId.toString(), themeVariant.get("variantId"));
            });
        });

    }

    @Test
    void handle_exception() throws IOException {
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();

        when(learnerThemeService.fetchThemeVariant(themeId, variantId))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + LEARNER_THEME_VARIANT_GET_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to fetch learner theme variant\"}");
    }
}
