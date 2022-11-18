package com.smartsparrow.rtm.message.handler.learner.annotation;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.learner.annotation.UpdateLearnerAnnotationMessageHandler.LEARNER_ANNOTATION_UPDATE_ERROR;
import static com.smartsparrow.rtm.message.handler.learner.annotation.UpdateLearnerAnnotationMessageHandler.LEARNER_ANNOTATION_UPDATE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.learner.annotation.UpdateLearnerAnnotationMessage;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class UpdateLearnerAnnotationMessageHandlerTest {

    private Session session;

    @InjectMocks
    UpdateLearnerAnnotationMessageHandler handler;

    @Mock
    AnnotationService annotationService;

    @Mock
    private UpdateLearnerAnnotationMessage message;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final Motivation motivation = Motivation.identifying;
    private static final UUID annotationId = UUIDs.timeBased();
    private static final UUID creatorId = UUID.randomUUID();
    private static final String body = "{\n\t\"body\": \"body\"\n}";
    private static final String target = "{\n\t\"target\": \"target\"\n}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(message.getDeploymentId()).thenReturn(deploymentId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(CoursewareElementType.ACTIVITY);
        when(message.getAnnotationId()).thenReturn(annotationId);
        when(message.getMotivation()).thenReturn(motivation);
        when(message.getBody()).thenReturn(body);
        when(message.getTarget()).thenReturn(target);

        session = mockSession();
    }

    @Test
    void validate_noDeploymentId() {
        when(message.getDeploymentId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing deploymentId", ex.getMessage());
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
    void validate_noMotivation() {
        when(message.getMotivation()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing motivation", ex.getMessage());
    }

    @Test
    void validate_noAnnotationBody() {
        when(message.getBody()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing annotation body", ex.getMessage());
    }

    @Test
    void validate_noAnnotationTarget() {
        when(message.getTarget()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing annotation target", ex.getMessage());
    }

    @Test
    void validate_noAnnotationId() {
        when(message.getAnnotationId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing annotation id", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(annotationService.updateLearnerAnnotation(any(), any(), any()))
                .thenReturn(Mono.just(new LearnerAnnotation()
                        .setId(UUIDs.timeBased())
                        .setCreatorAccountId(creatorId)
                        .setMotivation(motivation)
                        .setDeploymentId(deploymentId)
                        .setBodyJson(Json.toJsonNode(body))
                        .setTargetJson(Json.toJsonNode(target))
                        .setVersion(UUIDs.timeBased())
                        .setElementId(elementId)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(LEARNER_ANNOTATION_UPDATE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("learnerAnnotation"));
                assertEquals(deploymentId.toString(), responseMap.get("deploymentId"));
                assertEquals(motivation.toString(), responseMap.get("motivation"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<LearnerAnnotation> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(annotationService.updateLearnerAnnotation(any(), any(), any()))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + LEARNER_ANNOTATION_UPDATE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to update learner annotation\"}");
    }
}
