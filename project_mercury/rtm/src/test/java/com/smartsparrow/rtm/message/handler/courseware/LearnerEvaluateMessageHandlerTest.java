package com.smartsparrow.rtm.message.handler.courseware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.config.ConfigurableFeatureValues;
import com.smartsparrow.eval.service.EvaluationServiceAdapter;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.lang.LearnerEvaluationException;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.courseware.LearnerEvaluateMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class LearnerEvaluateMessageHandlerTest {

    @InjectMocks
    private LearnerEvaluateMessageHandler handler;

    @Mock
    private EvaluationServiceAdapter evaluationServiceAdapter;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID timeId = UUID.randomUUID();
    private static final String producingClientId = "clientId";
    private LearnerEvaluateMessage message;
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        message = mock(LearnerEvaluateMessage.class);
        when(message.getInteractiveId()).thenReturn(interactiveId);
        when(message.getDeploymentId()).thenReturn(deploymentId);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(studentId));
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);

        RTMClientContext clientContext = mock(RTMClientContext.class);
        when(clientContext.getClientId()).thenReturn(producingClientId);
        when(rtmClientContextProvider.get()).thenReturn(clientContext);

        when(authenticationContext.getAccountShadowAttribute(ConfigurableFeatureValues.EVALUATION))
                .thenReturn(null);
    }

    @Test
    void validate_nullDeployment() {
        when(message.getDeploymentId()).thenReturn(null);
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> handler.validate(message));
        assertEquals("deploymentId is required", e.getMessage());
    }

    @Test
    void validate_nullInteractive() {
        when(message.getInteractiveId()).thenReturn(null);
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> handler.validate(message));
        assertEquals("interactiveId is required", e.getMessage());
    }

    @Test
    void handle_genericException() throws WriteResponseException {
        TestPublisher<EvaluationResult> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("ops"));

        when(evaluationServiceAdapter.evaluate(deploymentId, interactiveId, producingClientId, studentId, null))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{\"type\":\"learner.evaluate.error\",\"code\":500,\"message\":\"unhandled error occurred to message processing\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    @DisplayName("It should include the evaluationId in the error context when defined in the exception")
    void handle_learnerEvaluationException() throws WriteResponseException {
        final UUID evaluationId = UUID.randomUUID();
        TestPublisher<EvaluationResult> publisher = TestPublisher.create();
        publisher.error(new LearnerEvaluationException("ops", null, evaluationId));

        when(evaluationServiceAdapter.evaluate(deploymentId, interactiveId, producingClientId, studentId, null))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = String.format("{\"type\":\"learner.evaluate.error\",\"code\":422,\"message\":\"Unable to evaluate\",\"context\":{\"evaluationId\":\"%s\"}}", evaluationId);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(evaluationServiceAdapter.evaluate(deploymentId, interactiveId, producingClientId, studentId, null))
                .thenReturn(Mono.just(new EvaluationResult()));

        handler.handle(session, message);

        String expected = "{\"type\":\"learner.evaluate.ok\",\"response\":{\"evaluationResult\":{\"interactiveComplete\":false}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success_timeId() throws WriteResponseException {
        when(evaluationServiceAdapter.evaluate(deploymentId, interactiveId, producingClientId, studentId, null, timeId))
                .thenReturn(Mono.just(new EvaluationResult()));
        when(message.getTimeId()).thenReturn(timeId);

        handler.handle(session, message);

        String expected = "{\"type\":\"learner.evaluate.ok\",\"response\":{\"evaluationResult\":{\"interactiveComplete\":false}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
