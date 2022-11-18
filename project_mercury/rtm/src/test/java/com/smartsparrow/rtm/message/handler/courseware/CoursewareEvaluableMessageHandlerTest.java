package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.EvaluationMode.DEFAULT;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.CoursewareEvaluableMessageHandler.AUTHOR_EVALUABLE_SET;
import static com.smartsparrow.rtm.message.handler.courseware.CoursewareEvaluableMessageHandler.AUTHOR_EVALUABLE_SET_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.CoursewareEvaluableMessageHandler.AUTHOR_EVALUABLE_SET_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.WalkableService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareEvaluableMessage;
import com.smartsparrow.rtm.subscription.courseware.evaluableset.EvaluableSetRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class CoursewareEvaluableMessageHandlerTest {

    private static final UUID elementId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = ACTIVITY;
    private static final EvaluationMode evaluationMode = DEFAULT;
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID accountId = UUID.randomUUID();

    @InjectMocks
    private CoursewareEvaluableMessageHandler handler;

    @Mock
    private WalkableService walkableService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private CoursewareEvaluableMessage message;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private Account account;

    @Mock
    private EvaluableSetRTMProducer evaluableSetRTMProducer;

    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;

    @Mock
    private RTMClientContext rtmClientContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        handler = new CoursewareEvaluableMessageHandler(walkableService, coursewareService, rtmEventBrokerProvider, authenticationContextProvider, rtmClientContextProvider, evaluableSetRTMProducer);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getEvaluationMode()).thenReturn(evaluationMode);
        when(message.getType()).thenReturn(AUTHOR_EVALUABLE_SET);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(evaluableSetRTMProducer.buildEvaluableSetRTMConsumable(rtmClientContext, rootElementId, elementId, elementType, evaluationMode))
                .thenReturn(evaluableSetRTMProducer);
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing elementId", f.getMessage());
        assertEquals("BAD_REQUEST", f.getType());
        assertEquals(400, f.getResponseStatusCode());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing elementType", f.getMessage());
        assertEquals("BAD_REQUEST", f.getType());
        assertEquals(400, f.getResponseStatusCode());
    }

    @Test
    void validate_noEvaluationMode() {
        when(message.getEvaluationMode()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing evaluationMode", f.getMessage());
        assertEquals("BAD_REQUEST", f.getType());
        assertEquals(400, f.getResponseStatusCode());
    }

    @Test
    void validate_success() throws RTMValidationException {
        handler.validate(message);
    }

    @Test
    void handle_success() throws IOException {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();

        when(walkableService.updateEvaluationMode(elementId, elementType, evaluationMode))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        verify(walkableService).updateEvaluationMode(elementId, elementType, evaluationMode);

        String expected = "{\"type\":\"" + AUTHOR_EVALUABLE_SET_OK + "\"," +
                "\"response\":{" +
                "\"evaluable\":{" +
                "\"elementId\":\"" + elementId + "\"," +
                "\"elementType\":\"" + elementType + "\"," +
                "\"evaluationMode\":\"" + evaluationMode + "\"}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);

        verify(rtmEventBroker).broadcast(eq(AUTHOR_EVALUABLE_SET), captor.capture());
        final CoursewareElementBroadcastMessage broadcastMessage = captor.getValue();

        assertNotNull(broadcastMessage);
        assertEquals(CoursewareAction.EVALUABLE_SET, broadcastMessage.getAction());
        assertEquals(CoursewareElement.from(elementId, elementType), broadcastMessage.getElement());

        verify(evaluableSetRTMProducer, atLeastOnce()).buildEvaluableSetRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(elementId), eq(elementType), eq(evaluationMode));
        verify(evaluableSetRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));
        when(walkableService.updateEvaluationMode(elementId, elementType, evaluationMode)).thenReturn(publisher.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + AUTHOR_EVALUABLE_SET_ERROR + "\",\"code\":422," +
                "\"message\":\"could not create evaluable\"}");
        verify(evaluableSetRTMProducer, never()).produce();
    }

}
