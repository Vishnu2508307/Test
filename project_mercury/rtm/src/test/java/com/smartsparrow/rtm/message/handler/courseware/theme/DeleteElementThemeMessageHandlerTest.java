package com.smartsparrow.rtm.message.handler.courseware.theme;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.theme.DeleteElementThemeMessageHandler.AUTHOR_ELEMENT_THEME_DELETE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.theme.DeleteElementThemeMessageHandler.AUTHOR_ELEMENT_THEME_DELETE_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.ThemeService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.theme.DeleteElementThemeMessage;
import com.smartsparrow.rtm.subscription.courseware.elementthemedelete.ElementThemeDeleteRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class DeleteElementThemeMessageHandlerTest {

    private Session session;

    @InjectMocks
    DeleteElementThemeMessageHandler handler;

    @Mock
    private ThemeService themeService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private DeleteElementThemeMessage message;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private ElementThemeDeleteRTMProducer elementThemeDeleteRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID elementId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = ACTIVITY;
    private static final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);

        session = mockSession();

        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        Account account = mock(Account.class);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(elementThemeDeleteRTMProducer.buildElementThemeDeleteRTMConsumable(rtmClientContext, rootElementId, elementId, elementType))
                .thenReturn(elementThemeDeleteRTMProducer);

        handler = new DeleteElementThemeMessageHandler(themeService, coursewareService, authenticationContextProvider, rtmEventBrokerProvider, rtmClientContextProvider, elementThemeDeleteRTMProducer);
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
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();
        when(themeService.deleteThemeByElement(elementId, elementType)).thenReturn(publisher.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> assertEquals(AUTHOR_ELEMENT_THEME_DELETE_OK, response.getType()));
        verify(elementThemeDeleteRTMProducer, atLeastOnce()).buildElementThemeDeleteRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(elementId), eq(elementType));
        verify(elementThemeDeleteRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_exception() throws IOException {
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(themeService.deleteThemeByElement(elementId, elementType))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_ELEMENT_THEME_DELETE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to delete an element theme association\"}");
        verify(elementThemeDeleteRTMProducer, never()).produce();
    }
}
