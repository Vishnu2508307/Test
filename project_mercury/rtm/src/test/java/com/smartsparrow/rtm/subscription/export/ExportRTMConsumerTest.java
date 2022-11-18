package com.smartsparrow.rtm.subscription.export;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.ExportProgress;
import com.smartsparrow.export.data.ExportSummary;
import com.smartsparrow.export.service.ExportService;
import com.smartsparrow.export.subscription.ExportBroadcastMessage;
import com.smartsparrow.export.subscription.ExportConsumable;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;


class ExportRTMConsumerTest {

    @InjectMocks
    private ExportRTMConsumer exportRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private ExportConsumable exportConsumable;

    @Mock
    private ExportService exportService;

    @Mock
    private ExportSummary exportSummary;

    @Mock
    private ExportBroadcastMessage message;

    private static final String broadcastType = "learner.progress.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID exportId = UUIDs.timeBased();
    private static final String ambrosiaUrl = "ambrosiaUrl";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(exportConsumable.getContent()).thenReturn(message);
        when(exportConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(exportConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getExportId()).thenReturn(exportId);
        when(message.getExportProgress()).thenReturn(ExportProgress.COMPLETE);
        when(exportSummary.getAmbrosiaUrl()).thenReturn(ambrosiaUrl);
        when(exportService.findById(exportId)).thenReturn(Mono.just(exportSummary));
    }

    @Test
    @DisplayName("It should not filter out the same client producer")
    void accept_filterOutSameClientProducer() {

        exportRTMConsumer.accept(rtmClient, exportConsumable);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");

        exportRTMConsumer.accept(rtmClient, exportConsumable);

        final String expected = "{" +
                "\"type\":\"author.export.broadcast\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"EXPORT\"," +
                "\"exportId\":\"" + message.getExportId() + "\"," +
                "\"progress\":\"" + message.getExportProgress() + "\"," +
                "\"ambrosiaUrl\":\"" + exportSummary.getAmbrosiaUrl() + "\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        Assert.assertNotNull(session.getRemote().sendStringByFuture(expected));

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
