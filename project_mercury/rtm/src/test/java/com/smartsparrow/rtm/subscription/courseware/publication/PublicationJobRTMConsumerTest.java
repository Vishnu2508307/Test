package com.smartsparrow.rtm.subscription.courseware.publication;

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

import com.smartsparrow.courseware.data.PublicationJobStatus;
import com.smartsparrow.courseware.eventmessage.PublicationJobBroadcastMessage;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class PublicationJobRTMConsumerTest {

    @InjectMocks
    private PublicationJobRTMConsumer publicationJobRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private PublicationJobRTMConsumable publicationJobRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private PublicationJobBroadcastMessage message;

    private static final String broadcastType = "publication.job.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID publicationId = UUIDs.timeBased();
    private static final UUID jobId = UUIDs.timeBased();
    private static final PublicationJobStatus status = PublicationJobStatus.STARTED;
    private static final String statusMessage = "Epub published successfully";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(rtmClient.getSession()).thenReturn(session);
        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(publicationJobRTMConsumable.getContent()).thenReturn(message);
        when(publicationJobRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(publicationJobRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);
        when(message.getPublicationId()).thenReturn(publicationId);
        when(message.getPublicationJobStatus()).thenReturn(status);
        when(message.getJobId()).thenReturn(jobId);
        when(message.getStatusMessage()).thenReturn(statusMessage);
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {

        publicationJobRTMConsumer.accept(rtmClient, publicationJobRTMConsumable);

        final String expected = "{" +
                "\"type\":\"publication.job.broadcast\"," +
                "\"response\":{" +
                "\"jobId\":\"" + jobId + "\"," +
                    "\"rtmEvent\":\"PUBLICATION_JOB\"," +
                "\"publicationJobStatus\":\"" + status + "\"," +
                    "\"action\":\"PUBLICATION_JOB\"," +
                    "\"publicationId\":\"" + publicationId + "\"," +
                "\"statusMessage\":\"" + statusMessage + "\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}