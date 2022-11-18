package com.smartsparrow.rtm.subscription.learner.studentprogress;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressBroadcastMessage;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMConsumable;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class StudentProgressRTMConsumerTest {

    @InjectMocks
    private StudentProgressRTMConsumer studentProgressRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private StudentProgressRTMConsumable studentProgressRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private StudentProgressBroadcastMessage message;

    private static final String broadcastType = "learner.progress.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID elementId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(studentProgressRTMConsumable.getContent()).thenReturn(message);
        when(studentProgressRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(studentProgressRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        ActivityProgress progress = new ActivityProgress()
                .setDeploymentId(deploymentId)
                .setStudentId(studentId)
                .setCoursewareElementId(elementId);

        when(message.getProgress()).thenReturn(progress);
    }

    @Test
    @DisplayName("It should not filter out the same client producer")
    void accept_filterOutSameClientProducer() {

        studentProgressRTMConsumer.accept(rtmClient, studentProgressRTMConsumable);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");

        studentProgressRTMConsumer.accept(rtmClient, studentProgressRTMConsumable);

        final String expected = "{" +
                "\"type\":\"learner.progress.broadcast\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"STUDENT_PROGRESS\"," +
                "\"progress\":\"" + message + "\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
