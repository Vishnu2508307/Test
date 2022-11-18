package com.smartsparrow.rtm.message.handler.learner.annotation;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.learner.annotation.DeleteLearnerAnnotationMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.util.UUID;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;

import static com.smartsparrow.rtm.message.handler.learner.annotation.DeleteLearnerAnnotationMessageHandler.LEARNER_ANNOTATION_DELETE_ERROR;
import static com.smartsparrow.rtm.message.handler.learner.annotation.DeleteLearnerAnnotationMessageHandler.LEARNER_ANNOTATION_DELETE_OK;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DeleteLearnerAnnotationMessageHandlerTest {

    @InjectMocks
    DeleteLearnerAnnotationMessageHandler handler;
    @Mock
    AnnotationService annotationService;
    @Mock
    private DeleteLearnerAnnotationMessage message;
    @Mock
    private LearnerAnnotation learnerAnnotation;

    private Session session;

    private static final UUID annotationId = UUID.randomUUID();


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(message.getAnnotationId()).thenReturn(annotationId);
        when(annotationService.findLearnerAnnotation(annotationId)).thenReturn(Mono.just(learnerAnnotation));
        session = mockSession();

    }

    @Test
    void validate_noAnnotationId() {
        when(message.getAnnotationId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing annotationId", ex.getMessage());
    }

    @Test
    void handle() throws IOException {

        when(annotationService.deleteAnnotation(any(LearnerAnnotation.class)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(LEARNER_ANNOTATION_DELETE_OK, response.getType());
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new RuntimeException("can't delete"));
        when(annotationService.deleteAnnotation(any(LearnerAnnotation.class)))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + LEARNER_ANNOTATION_DELETE_ERROR + "\",\"code\":422," +
                "\"message\":\"error deleting learner annotation\"}");
    }
}
