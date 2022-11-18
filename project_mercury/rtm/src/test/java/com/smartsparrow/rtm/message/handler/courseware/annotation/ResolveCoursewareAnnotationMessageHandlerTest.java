package com.smartsparrow.rtm.message.handler.courseware.annotation;


import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.ResolveCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_RESOLVE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.ResolveCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_RESOLVE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotationKey;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.annotation.ResolveCoursewareAnnotationMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class ResolveCoursewareAnnotationMessageHandlerTest {

    @InjectMocks
    private ResolveCoursewareAnnotationMessageHandler handler;

    @Mock
    private AnnotationService annotationService;

    @Mock
    private ResolveCoursewareAnnotationMessage message;

    private Session session;
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID annotationId = UUID.randomUUID();
    private static final List<CoursewareAnnotationKey> coursewareAnnotationKeys = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = mockSession();
        CoursewareAnnotationKey coursewareAnnotationKey = new CoursewareAnnotationKey()
                .setId(annotationId);
        coursewareAnnotationKeys.add(coursewareAnnotationKey);

        when(message.getRootElementId()).thenReturn(rootElementId);
        when(message.getResolved()).thenReturn(true);
        when(message.getCoursewareAnnotationKeys()).thenReturn(coursewareAnnotationKeys);
    }

    @Test
    void validate_noRootElementId() {
        when(message.getRootElementId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("rootElementId is required", ex.getMessage());
    }

    @Test
    void validate_noResolved() {
        when(message.getResolved()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("resolved is required", ex.getMessage());
    }

    @Test
    void validate_noAnnotationIds() {
        when(message.getCoursewareAnnotationKeys()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("coursewareAnnotationKeys is required", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(annotationService.resolveComments(eq(coursewareAnnotationKeys), eq(true)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ANNOTATION_RESOLVE_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new RuntimeException("can't fetch"));
        when(annotationService.resolveComments(eq(coursewareAnnotationKeys), eq(true)))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session,
                                                  "{\"type\":\"" + AUTHOR_ANNOTATION_RESOLVE_ERROR + "\",\"code\":422," +
                                                          "\"message\":\"error resolving comment annotations\"}");
    }
}