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
import com.smartsparrow.rtm.message.recv.courseware.theme.AssociateActivityIconLibraryMessage;
import com.smartsparrow.workspace.data.IconLibrary;
import com.smartsparrow.workspace.data.ActivityThemeIconLibrary;
import com.smartsparrow.workspace.data.IconLibraryState;

import reactor.test.publisher.TestPublisher;

public class AssociateActivityThemeIconLibraryMessageHandlerTest {

    private Session session;

    @InjectMocks
    AssociateActivityThemeIconLibraryMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private AssociateActivityIconLibraryMessage message;

    private static final UUID activityId = UUID.randomUUID();
    List<IconLibrary> iconLibraryList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();
        when(message.getActivityId()).thenReturn(activityId);
        iconLibraryList.add(new IconLibrary().setName("FONTAWESOME").setStatus(IconLibraryState.SELECTED));
        iconLibraryList.add(new IconLibrary().setName("MICROSOFT"));
        when(message.getIconLibraries()).thenReturn(iconLibraryList);

        handler = new AssociateActivityThemeIconLibraryMessageHandler(themeService);
    }

    @Test
    void validate_noThemeId() {
        when(message.getActivityId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing activityId", ex.getMessage());
    }

    @Test
    void validate_noIconLibrary() {
        when(message.getIconLibraries()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing icon library info", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        TestPublisher<ActivityThemeIconLibrary> publisher = TestPublisher.create();
        publisher.complete();

        when(themeService.saveActivityThemeIconLibraries(message.getActivityId(), message.getIconLibraries()))
                .thenReturn(publisher.flux());

        handler.handle(session, message);

        String expected = "{\"type\":\"author.activity.icon.library.associate.ok\"," +
                "\"response\":{\"activityThemeIconLibraries\":[]}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ActivityThemeIconLibrary> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));


        when(themeService.saveActivityThemeIconLibraries(message.getActivityId(), message.getIconLibraries()))
                .thenReturn(error.flux());

        handler.handle(session, message);

        String expected = "{\"type\":\"author.activity.icon.library.associate.error\"," +
                "\"code\":422," +
                "\"message\":\"Unable to save an activity and icon library association\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
