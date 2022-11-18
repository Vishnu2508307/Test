package com.smartsparrow.rtm.subscription.courseware.updated;

import static com.smartsparrow.courseware.data.CoursewareElementType.SCENARIO;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ScenarioUpdatedBroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class ScenarioUpdatedRTMConsumerTest {

    @InjectMocks
    private ScenarioUpdatedRTMConsumer updatedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private ScenarioUpdatedRTMConsumable scenarioUpdatedRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private ScenarioUpdatedBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID parentElementId = UUIDs.timeBased();
    private static final CoursewareElementType parentElementType = CoursewareElementType.SCENARIO;
    private static final ScenarioLifecycle lifecycle = ScenarioLifecycle.ACTIVITY_COMPLETE;
    private static final UUID scenarioId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(scenarioUpdatedRTMConsumable.getContent()).thenReturn(message);
        when(scenarioUpdatedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(scenarioUpdatedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getElementId()).thenReturn(scenarioId);
        when(message.getElementType()).thenReturn(CoursewareElementType.SCENARIO);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(scenarioUpdatedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        updatedRTMConsumer.accept(rtmClient, scenarioUpdatedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(scenarioUpdatedRTMConsumable.getRTMClientContext()).thenReturn(producer);

        updatedRTMConsumer.accept(rtmClient, scenarioUpdatedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"activity.broadcast\"," +
                "\"response\":{" +
                    "\"elementId\":\"" + scenarioId + "\"," +
                    "\"elementType\":\"" + SCENARIO + "\"," +
                    "\"parentElementId\":\"" + parentElementId + "\"," +
                    "\"parentElementType\":\"" + parentElementType + "\"," +
                    "\"lifecycle\":\"" + lifecycle + "\"," +
                    "\"rtmEvent\":\"SCENARIO_UPDATED\"," +
                    "\"action\":\"UPDATED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
