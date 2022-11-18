package com.smartsparrow.rtm.subscription.learner.studentscope;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.smartsparrow.learner.data.StudentScopeEntry;
import com.smartsparrow.pubsub.subscriptions.studentscope.StudentScopeBroadcastMessage;
import com.smartsparrow.pubsub.subscriptions.studentscope.StudentScopeConsumable;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

public class StudentScopeRTMConsumerTest {

    @InjectMocks
    private StudentScopeRTMConsumer studentScopeRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private StudentScopeConsumable studentScopeConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private StudentScopeBroadcastMessage message;

    private static final String broadcastType = "learner.progress.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID studentScopeUrn = UUIDs.timeBased();
    private static final StudentScopeEntry studentScopeEntry = new StudentScopeEntry();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(studentScopeConsumable.getContent()).thenReturn(message);
        when(studentScopeConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(studentScopeConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getStudentId()).thenReturn(studentId);
        when(message.getDeploymentId()).thenReturn(deploymentId);
        when(message.getStudentScopeUrn()).thenReturn(studentScopeUrn);
        when(message.getStudentScopeEntry()).thenReturn(studentScopeEntry);
    }

    @Test
    @DisplayName("It should not filter out the same client producer")
    void accept_filterOutSameClientProducer() {

        studentScopeRTMConsumer.accept(rtmClient, studentScopeConsumable);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");

        studentScopeRTMConsumer.accept(rtmClient, studentScopeConsumable);

        final String expected = "{" +
                "\"type\":\"learner.student.scope.broadcast\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"STUDENT_SCOPE\"," +
                "\"deploymentId\":\"" + message.getDeploymentId() + "\"," +
                "\"studentScopeURN\":\"" + message.getStudentScopeUrn() + "\"," +
                "\"studentScope\":\"" + message.getStudentScopeEntry() + "\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));

        verify(session.getRemote()).sendStringByFuture(expected);
    }

}
