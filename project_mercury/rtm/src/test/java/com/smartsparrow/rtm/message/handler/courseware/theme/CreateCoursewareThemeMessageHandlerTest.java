package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.theme.CreateCoursewareThemeMessageHandler.AUTHOR_THEME_CREATE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.theme.CreateCoursewareThemeMessageHandler.AUTHOR_THEME_CREATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.google.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.theme.CreateCoursewareThemeMessage;
import com.smartsparrow.workspace.data.Theme;


import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class CreateCoursewareThemeMessageHandlerTest {
    private Session session;

    @InjectMocks
    CreateCoursewareThemeMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private CreateCoursewareThemeMessage message;

    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final String name = "Theme_one";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Account account = mock(Account.class);

        when(message.getName()).thenReturn(name);
        when(message.getWorkspaceId()).thenReturn(workspaceId);

        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        handler = new CreateCoursewareThemeMessageHandler(themeService, authenticationContextProvider);
    }

    @Test
    void validate_noName() {
        when(message.getName()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing theme name", ex.getMessage());
    }

    @Test
    void validate_noWorkspaceId() {
        when(message.getWorkspaceId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing workspaceId", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(themeService.create(accountId, name))
                .thenReturn(Mono.just(new Theme()
                                              .setId(UUID.randomUUID())
                .setName(name)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_THEME_CREATE_OK, response.getType());
                Map theme= (Map) response.getResponse().get("theme");
                assertNotNull(theme);
                assertEquals(name, theme.get("name"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(themeService.create(accountId, name))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_THEME_CREATE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to create a theme\"}");
    }
}
