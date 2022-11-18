package com.smartsparrow.rtm.message.handler.learner.annotation;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.learner.ListLearnerAnnotationMessage;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.UUIDs;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.learner.annotation.ListDeploymentAnnotationMessageHandler.LEARNER_ANNOTATION_LIST_ERROR;
import static com.smartsparrow.rtm.message.handler.learner.annotation.ListDeploymentAnnotationMessageHandler.LEARNER_ANNOTATION_LIST_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ListDeploymentAnnotationMessageHandlerTest {

    private Session session;

    @InjectMocks
    ListDeploymentAnnotationMessageHandler handler;

    @Mock
    AnnotationService annotationService;

    @Mock
    private ListLearnerAnnotationMessage message;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final Motivation motivation = Motivation.identifying;
    private static final UUID creatorId = UUID.randomUUID();
    private static final String body = "{\n\t\"body\": \"body\"\n}";
    private static final String target = "{\n\t\"target\": \"target\"\n}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(message.getDeploymentId()).thenReturn(deploymentId);
        when(message.getCreatorAccountId()).thenReturn(creatorId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getMotivation()).thenReturn(motivation);
        when(message.getElementType()).thenReturn(CoursewareElementType.ACTIVITY);

        session = mockSession();
    }

    @Test
    void validate_noDeploymentId() {
        when(message.getDeploymentId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing deploymentId", ex.getMessage());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing elementType", ex.getMessage());
    }

    @Test
    void validate_noCreatorAccountId() {
        when(message.getCreatorAccountId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing creatorAccountId", ex.getMessage());
    }

    @Test
    void handle_elementIdSet() throws IOException {
        when(annotationService.findLearnerAnnotation(any(),any(),any(),any()))
                .thenReturn(Flux.just(new LearnerAnnotation()
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
                assertEquals(LEARNER_ANNOTATION_LIST_OK, response.getType());
                List responseList = ((List) response.getResponse().get("learnerAnnotation"));
                assertEquals(deploymentId.toString(), ((LinkedHashMap)responseList.get(0)).get("deploymentId"));
                assertEquals(motivation.toString(), ((LinkedHashMap)responseList.get(0)).get("motivation"));
            });
        });
    }

    @Test
    void handle_elementIdNotSet() throws IOException {
        when(message.getElementId()).thenReturn(null);
        when(annotationService.findLearnerAnnotation(any(UUID.class),any(UUID.class),any(Motivation.class)))
                .thenReturn(Flux.just(new LearnerAnnotation()
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
                assertEquals(LEARNER_ANNOTATION_LIST_OK, response.getType());
                List responseList = ((List) response.getResponse().get("learnerAnnotation"));
                assertEquals(deploymentId.toString(), ((LinkedHashMap)responseList.get(0)).get("deploymentId"));
                assertEquals(motivation.toString(), ((LinkedHashMap)responseList.get(0)).get("motivation"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<LearnerAnnotation> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(annotationService.findLearnerAnnotation(any(),any(),any(),any()))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + LEARNER_ANNOTATION_LIST_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to fetch deployment annotation\"}");
    }
}
