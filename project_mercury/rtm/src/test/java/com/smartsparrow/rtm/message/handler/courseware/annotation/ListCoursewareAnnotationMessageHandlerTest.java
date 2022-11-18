package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.ListCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_LIST_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.ListCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_LIST_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashMap;
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
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.CoursewareAnnotationPayload;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.annotation.ListCoursewareAnnotationMessage;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class ListCoursewareAnnotationMessageHandlerTest {

    private Session session;

    @InjectMocks
    ListCoursewareAnnotationMessageHandler handler;

    @Mock
    AnnotationService annotationService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private ListCoursewareAnnotationMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final Motivation motivation = Motivation.identifying;
    private static final UUID creatorId = UUID.randomUUID();
    private static final String body = "{\n\t\"body\": \"body\"\n}";
    private static final String target = "{\n\t\"target\": \"target\"\n}";

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
    void validate_noMotivation() {
        when(message.getMotivation()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing motivation", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(annotationService.fetchCoursewareAnnotation(any(),any(),any(),any()))
                .thenReturn(Flux.just(new CoursewareAnnotationPayload(new CoursewareAnnotation()
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
                assertEquals(AUTHOR_ANNOTATION_LIST_OK, response.getType());
                List responseList = ((List) response.getResponse().get("coursewareAnnotation"));
                assertEquals(rootElementId.toString(), ((LinkedHashMap)responseList.get(0)).get("rootElementId"));
                assertEquals(motivation.toString(), ((LinkedHashMap)responseList.get(0)).get("motivation"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<CoursewareAnnotationPayload> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(annotationService.fetchCoursewareAnnotation(any(),any(),any(),any()))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + AUTHOR_ANNOTATION_LIST_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to fetch courseware annotation\"}");
    }
}
