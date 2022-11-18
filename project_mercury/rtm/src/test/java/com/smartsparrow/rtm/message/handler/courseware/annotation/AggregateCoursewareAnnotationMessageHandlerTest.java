package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.AggregateCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_AGGREGATE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.AggregateCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_AGGREGATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotationAggregate;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.annotation.ListCoursewareAnnotationMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class AggregateCoursewareAnnotationMessageHandlerTest {
    private Session session;

    @InjectMocks
    AggregateCoursewareAnnotationMessageHandler handler;

    @Mock
    AnnotationService annotationService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private ListCoursewareAnnotationMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final Motivation motivation = Motivation.commenting;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(message.getRootElementId()).thenReturn(rootElementId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getMotivation()).thenReturn(motivation);
        when(message.getElementType()).thenReturn(CoursewareElementType.ACTIVITY);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        session = mockSession();
    }

    @Test
    void validate_noRootElementId() {
        when(message.getRootElementId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing rootElementId", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(annotationService.aggregateCoursewareAnnotation(any(), any(), any()))
                .thenReturn(Mono.just(new CoursewareAnnotationAggregate(2, 2, 1, 3)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ANNOTATION_AGGREGATE_OK, response.getType());
                LinkedHashMap annotationAggregate = ((LinkedHashMap) response.getResponse().get(
                        "coursewareAnnotationAggregate"));
                assertEquals(4, annotationAggregate.get("total"));
                assertEquals(2, annotationAggregate.get("read"));
                assertEquals(2, annotationAggregate.get("unRead"));
                assertEquals(1, annotationAggregate.get("resolved"));
                assertEquals(3, annotationAggregate.get("unResolved"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<CoursewareAnnotationAggregate> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(annotationService.aggregateCoursewareAnnotation(any(), any(), any()))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_ANNOTATION_AGGREGATE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"Unable to aggregate courseware annotation\"}");
    }
}