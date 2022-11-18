package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.theme.ListCoursewareThemeMessageHandler.AUTHOR_THEME_LIST_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.theme.ListCoursewareThemeMessageHandler.AUTHOR_THEME_LIST_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;
import com.smartsparrow.workspace.data.Theme;
import com.smartsparrow.workspace.data.ThemePayload;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;


public class ListCoursewareThemeMessageHandlerTest {

    private Session session;

    @InjectMocks
    ListCoursewareThemeMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private EmptyReceivedMessage message;

    @Mock
    private WebSessionToken webSessionToken;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID themeId = UUID.randomUUID();
    private static final String name = "Theme_one";
    private static final String config = " {\"colo\":\"orange\", \"margin\":\"20\"}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Account account = mock(Account.class);

        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        handler = new ListCoursewareThemeMessageHandler(themeService, authenticationContextProvider);
    }

    @Test
    void handle() throws IOException {
        when(themeService.fetchThemes(accountId))
                .thenReturn(Flux.just(new ThemePayload()
                                              .setId(themeId)
                .setName(name)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_THEME_LIST_OK, response.getType());
                List theme= (List) response.getResponse().get("themePayload");
                assertNotNull(theme);
                assertEquals(1, theme.size());
                assertEquals(name, ((Map)theme.get(0)).get("name"));
                assertEquals(themeId.toString(), ((Map)theme.get(0)).get("id"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Flux flux = TestPublisher.create().error(new RuntimeException("someException")).flux();
        when(themeService.fetchThemes(accountId))
                .thenReturn(flux);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_THEME_LIST_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to fetch theme payload\"}");
    }
}
