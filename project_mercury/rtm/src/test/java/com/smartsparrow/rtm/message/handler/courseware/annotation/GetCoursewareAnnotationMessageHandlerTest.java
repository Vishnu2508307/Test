package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.GetCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_GET_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.GetCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_GET_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.CoursewareAnnotationPayload;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.annotation.GetCoursewareAnnotationMessage;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class GetCoursewareAnnotationMessageHandlerTest {

    private Session session;

    @InjectMocks
    GetCoursewareAnnotationMessageHandler handler;

    @Mock
    AnnotationService annotationService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private GetCoursewareAnnotationMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID annotationId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final Motivation motivation = Motivation.identifying;
    private static final UUID creatorId = UUID.randomUUID();
    private static final String body = "{\n\t\"body\": \"body\"\n}";
    private static final String target = "{\n\t\"target\": \"target\"\n}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(message.getAnnotationId()).thenReturn(annotationId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(CoursewareElementType.ACTIVITY);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        session = mockSession();
    }

    @Test
    void validate_noAnnotationId() {
        when(message.getAnnotationId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing annotationId", ex.getMessage());
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
        when(annotationService.findCoursewareAnnotation(any(), any()))
                .thenReturn(Mono.just(new CoursewareAnnotationPayload(new CoursewareAnnotation()
                .setId(UUIDs.timeBased())
                .setCreatorAccountId(creatorId)
                .setMotivation(motivation)
                .setRootElementId(rootElementId)
                .setBodyJson(Json.toJsonNode(body))
                .setTargetJson(Json.toJsonNode(target))
                .setVersion(UUIDs.timeBased())
                .setElementId(elementId)
                .setResolved(false))
                .setRead(false)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ANNOTATION_GET_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("coursewareAnnotation"));
                assertEquals(rootElementId.toString(), responseMap.get("rootElementId"));
                assertEquals(motivation.toString(), responseMap.get("motivation"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<CoursewareAnnotationPayload> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(annotationService.findCoursewareAnnotation(any(), any()))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + AUTHOR_ANNOTATION_GET_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to fetch courseware annotation\"}");
    }
}
