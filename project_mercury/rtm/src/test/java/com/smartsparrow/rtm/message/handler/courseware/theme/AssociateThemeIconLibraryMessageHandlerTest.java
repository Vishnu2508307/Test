package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.message.recv.courseware.theme.AssociateThemeIconLibraryMessage;
import com.smartsparrow.workspace.data.IconLibrary;
import com.smartsparrow.workspace.data.IconLibraryByTheme;
import com.smartsparrow.workspace.data.IconLibraryState;
import com.smartsparrow.workspace.data.Theme;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class AssociateThemeIconLibraryMessageHandlerTest {

    private Session session;

    @InjectMocks
    AssociateThemeIconLibraryMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private AssociateThemeIconLibraryMessage message;

    private static final UUID themeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getThemeId()).thenReturn(themeId);
        when(themeService.fetchThemeById(themeId)).thenReturn(Mono.just(new Theme()));

        session = mockSession();

        handler = new AssociateThemeIconLibraryMessageHandler(themeService);
    }

    @Test
    void validate_noThemeId() {
        when(message.getThemeId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing themeId", ex.getMessage());
    }

    @Test
    void validate_noIconLibrary() {
        when(message.getIconLibraries()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing icon library info", ex.getMessage());
    }

    @Test
    void validate_themeNotFound() {
        when(themeService.fetchThemeById(themeId)).thenReturn(Mono.empty());
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals(String.format("theme %s not found", themeId), ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        List<IconLibrary> iconLibraryList = new ArrayList<>();
        iconLibraryList.add(new IconLibrary().setName("FONTAWESOME").setStatus(IconLibraryState.SELECTED));
        iconLibraryList.add(new IconLibrary().setName("MICROSOFT"));

        TestPublisher<IconLibraryByTheme> publisher = TestPublisher.create();
        publisher.complete();

        when(message.getIconLibraries()).thenReturn(iconLibraryList);
        when(themeService.saveThemeIconLibraries(message.getThemeId(), message.getIconLibraries()))
                .thenReturn(publisher.flux());

        handler.handle(session, message);

        String expected = "{\"type\":\"author.theme.icon.library.associate.ok\",\"response\":{\"iconLibrariesByTheme\":[]}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<IconLibraryByTheme> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));

        List<IconLibrary> iconLibraryList = new ArrayList<>();
        iconLibraryList.add(new IconLibrary().setName("FONTAWESOME").setStatus(IconLibraryState.SELECTED));
        iconLibraryList.add(new IconLibrary().setName("MICROSOFT"));

        when(message.getIconLibraries()).thenReturn(iconLibraryList);
        when(themeService.saveThemeIconLibraries(themeId, iconLibraryList))
                .thenReturn(error.flux());

        handler.handle(session, message);

        verify(themeService, atLeastOnce()).saveThemeIconLibraries(themeId, iconLibraryList);

        String expected = "{\"type\":\"author.theme.icon.library.associate.error\"," +
                "\"code\":422," +
                "\"message\":\"Unable to save theme and icon library association\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
