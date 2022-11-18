package com.smartsparrow.rtm.message.handler.courseware.annotation;


import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.ReadCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_READ_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.ReadCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_READ_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.annotation.ReadCoursewareAnnotationMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class ReadCoursewareAnnotationMessageHandlerTest {

    @InjectMocks
    private ReadCoursewareAnnotationMessageHandler handler;

    @Mock
    private AnnotationService annotationService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private ReadCoursewareAnnotationMessage message;

    private Session session;
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID annotationId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final List<UUID> coursewareAnnotationIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();

        Account account = mock(Account.class);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        coursewareAnnotationIds.add(annotationId);

        when(message.getRootElementId()).thenReturn(rootElementId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getAnnotationIds()).thenReturn(coursewareAnnotationIds);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getRead()).thenReturn(true);
    }

    @Test
    void validate_noRootElementId() {
        when(message.getRootElementId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("rootElementId is required", ex.getMessage());
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementId is required", ex.getMessage());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("elementType is required", ex.getMessage());
    }

    @Test
    void validate_noRead() {
        when(message.getRead()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("read is required", ex.getMessage());
    }

    @Test
    void validate_noAnnotationIds() {
        when(message.getAnnotationIds()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("annotationIds is required", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(annotationService.readComments(eq(rootElementId), eq(elementId), eq(coursewareAnnotationIds), eq(true), eq(accountId)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ANNOTATION_READ_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new RuntimeException("can't fetch"));
        when(annotationService.readComments(eq(rootElementId), eq(elementId), eq(coursewareAnnotationIds), eq(true), eq(accountId)))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_ANNOTATION_READ_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error marking comment annotations as read\"}");
    }
}
