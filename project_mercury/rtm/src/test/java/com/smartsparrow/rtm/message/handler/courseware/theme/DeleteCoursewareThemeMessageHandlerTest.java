package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.theme.DeleteCoursewareThemeMessageHandler.AUTHOR_THEME_DELETE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.theme.DeleteCoursewareThemeMessageHandler.AUTHOR_THEME_DELETE_OK;
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
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.theme.GenericThemeMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class DeleteCoursewareThemeMessageHandlerTest {
    private Session session;

    @InjectMocks
    DeleteCoursewareThemeMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private GenericThemeMessage message;

    private static final UUID themeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getThemeId()).thenReturn(themeId);

        session = mockSession();

        handler = new DeleteCoursewareThemeMessageHandler(themeService);
    }

    @Test
    void validate_noThemeId() {
        when(message.getThemeId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing themeId", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(themeService.deleteTheme(themeId))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_THEME_DELETE_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Flux flux = TestPublisher.create().error(new RuntimeException("someException")).flux();
        when(themeService.deleteTheme(themeId))
                .thenReturn(flux);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_THEME_DELETE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error deleting theme\"}");
    }
}
