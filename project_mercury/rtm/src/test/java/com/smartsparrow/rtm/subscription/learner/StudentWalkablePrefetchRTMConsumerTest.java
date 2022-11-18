package com.smartsparrow.rtm.subscription.learner;

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

import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.pubsub.subscriptions.learner.StudentWalkablePrefetchBroadcastMessage;
import com.smartsparrow.pubsub.subscriptions.learner.StudentWalkablePrefetchConsumable;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class StudentWalkablePrefetchRTMConsumerTest {

    @InjectMocks
    private StudentWalkablePrefetchRTMConsumer studentWalkablePrefetchRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private StudentWalkablePrefetchConsumable studentWalkablePrefetchConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private StudentWalkablePrefetchBroadcastMessage message;

    private static final String broadcastType = "learner.student.walkable.prefetch.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID accountId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();
    private static final Object walkable = new LearnerActivity().setId(activityId);
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(studentWalkablePrefetchConsumable.getContent()).thenReturn(message);
        when(studentWalkablePrefetchConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(studentWalkablePrefetchConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getStudentId()).thenReturn(accountId);
        when(message.getWalkable()).thenReturn(walkable);
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");

        studentWalkablePrefetchRTMConsumer.accept(rtmClient, studentWalkablePrefetchConsumable);

        String walkableString = "{\"id\":\"" + activityId + "\",\"coursewareElementType\":\"ACTIVITY\"}";

        final String expected = "{" +
                "\"type\":\"learner.student.walkable.prefetch.broadcast\"," +
                "\"response\":{" +
                    "\"studentId\":\"" + accountId + "\"," +
                    "\"rtmEvent\":\"STUDENT_WALKABLE_PREFETCH\"," +
                    "\"walkable\":" + walkableString + "," +
                    "\"action\":\"STUDENT_WALKABLE_PREFETCH\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
