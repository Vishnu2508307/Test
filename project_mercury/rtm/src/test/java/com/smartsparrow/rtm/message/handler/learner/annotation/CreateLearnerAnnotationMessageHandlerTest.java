package com.smartsparrow.rtm.message.handler.learner.annotation;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.learner.annotation.CreateLearnerAnnotationMessage;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.UUIDs;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.learner.annotation.CreateLearnerAnnotationMessageHandler.LEARNER_ANNOTATION_CREATE_ERROR;
import static com.smartsparrow.rtm.message.handler.learner.annotation.CreateLearnerAnnotationMessageHandler.LEARNER_ANNOTATION_CREATE_OK;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateLearnerAnnotationMessageHandlerTest {

    private Session session;

    @InjectMocks
    CreateLearnerAnnotationMessageHandler handler;
    @Mock
    AnnotationService annotationService;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private CreateLearnerAnnotationMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID creatorId = UUID.randomUUID();
    private static final CoursewareElementType coursewareElementType = CoursewareElementType.ACTIVITY;
    private static final Motivation motivation = Motivation.commenting;
    private static final String body = "{\n\t\"body\": \"body\"\n}";
    private static final String target = "{\n\t\"target\": \"target\"\n}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getDeploymentId()).thenReturn(deploymentId);
        when(message.getElementType()).thenReturn(coursewareElementType);
        when(message.getMotivation()).thenReturn(motivation);
        when(message.getBody()).thenReturn(body);
        when(message.getTarget()).thenReturn(target);

        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));
    }

    @Test
    void validate_noRootElementId() {
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
    void validate_noBody() {
        when(message.getBody()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing body", ex.getMessage());
    }

    @Test
    void validate_noTarget() {
        when(message.getTarget()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing target", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(annotationService.create(any(), any(), any(), any(), any(), any()))
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
                assertEquals(LEARNER_ANNOTATION_CREATE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("learnerAnnotation"));
                assertEquals(elementId.toString(), responseMap.get("elementId"));
                assertEquals(deploymentId.toString(), responseMap.get("deploymentId"));
                assertEquals(motivation.toString(), responseMap.get("motivation"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<CoursewareAnnotation> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(annotationService.create(any(), any(), any(), any(), any(), any())).
                thenReturn(Mono.error(new RuntimeException()));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + LEARNER_ANNOTATION_CREATE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to create the learner annotation\"}");
    }
}
