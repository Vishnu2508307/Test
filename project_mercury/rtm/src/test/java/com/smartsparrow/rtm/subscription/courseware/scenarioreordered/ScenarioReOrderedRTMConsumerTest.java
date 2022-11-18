package com.smartsparrow.rtm.subscription.courseware.scenarioreordered;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ScenarioReOrderedBroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class ScenarioReOrderedRTMConsumerTest {

    @InjectMocks
    private ScenarioReOrderedRTMConsumer reOrderedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private ScenarioReOrderedRTMConsumable scenarioReOrderedRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private ScenarioReOrderedBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final ScenarioLifecycle lifecycle = ScenarioLifecycle.ACTIVITY_COMPLETE;
    private static final UUID activityId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID idOne = UUIDs.timeBased();
    private static final UUID idTwo = UUIDs.timeBased();
    private static final List<UUID> scenarioIds = Lists.newArrayList(idOne, idTwo);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(scenarioReOrderedRTMConsumable.getContent()).thenReturn(message);
        when(scenarioReOrderedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(scenarioReOrderedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getElementId()).thenReturn(activityId);
        when(message.getElementType()).thenReturn(ACTIVITY);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(scenarioReOrderedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        reOrderedRTMConsumer.accept(rtmClient, scenarioReOrderedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(scenarioReOrderedRTMConsumable.getRTMClientContext()).thenReturn(producer);

        reOrderedRTMConsumer.accept(rtmClient, scenarioReOrderedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"activity.broadcast\"," +
                "\"response\":{" +
                "\"elementId\":\"" + activityId + "\"," +
                "\"elementType\":\"" + ACTIVITY + "\"," +
                "\"scenarioIds\":\"" + scenarioIds + "\"," +
                "\"lifecycle\":\"" + lifecycle + "\"," +
                "\"rtmEvent\":\"SCENARIO_REORDERED\"," +
                "\"action\":\"SCENARIO_REORDERED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
