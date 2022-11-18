package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.theme.CreateElementThemeMessageHandler.AUTHOR_ELEMENT_THEME_CREATE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.theme.CreateElementThemeMessageHandler.AUTHOR_ELEMENT_THEME_CREATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ThemeCoursewareElement;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.theme.CreateElementThemeMessage;
import com.smartsparrow.rtm.subscription.courseware.elementthemecreate.ElementThemeCreateRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class CreateElementThemeMessageHandlerTest {
    private Session session;

    @InjectMocks
    CreateElementThemeMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private CreateElementThemeMessage message;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private ElementThemeCreateRTMProducer elementThemeCreateRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID themeId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = ACTIVITY;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getThemeId()).thenReturn(themeId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);

        session = mockSession();
        Account account = mock(Account.class);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(elementThemeCreateRTMProducer.buildElementThemeCreateRTMConsumable(rtmClientContext, rootElementId, elementId, ACTIVITY, themeId))
                .thenReturn(elementThemeCreateRTMProducer);

        handler = new CreateElementThemeMessageHandler(themeService, coursewareService, authenticationContextProvider, rtmEventBrokerProvider, rtmClientContextProvider, elementThemeCreateRTMProducer);
    }

    @Test
    void validate_noThemeId() {
        when(message.getThemeId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing themeId", ex.getMessage());
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing elementId", ex.getMessage());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing elementType", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(themeService.saveThemeByElement(themeId, elementId, elementType))
                .thenReturn(Mono.just(new ThemeCoursewareElement()
                                              .setElementId(elementId)
                        .setElementType(elementType)
                        .setThemeId(themeId)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ELEMENT_THEME_CREATE_OK, response.getType());
                Map theme= (Map) response.getResponse().get("elementTheme");
                assertNotNull(theme);
                assertEquals(themeId.toString(), theme.get("themeId"));
            });
        });

        verify(elementThemeCreateRTMProducer, atLeastOnce()).buildElementThemeCreateRTMConsumable(eq(rtmClientContext),eq(rootElementId), eq(elementId), eq(ACTIVITY), eq(themeId));
        verify(elementThemeCreateRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(themeService.saveThemeByElement(themeId, elementId, elementType))
                      .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_ELEMENT_THEME_CREATE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to create an element theme association\"}");
        verify(elementThemeCreateRTMProducer, never()).produce();
    }
}
