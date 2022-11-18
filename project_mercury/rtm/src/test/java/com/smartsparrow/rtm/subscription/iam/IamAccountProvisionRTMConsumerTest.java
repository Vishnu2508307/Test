package com.smartsparrow.rtm.subscription.iam;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

class IamAccountProvisionRTMConsumerTest {

    @InjectMocks
    private IamAccountProvisionRTMConsumer iamAccountProvisionRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private IamAccountProvisionRTMConsumable iamAccountProvisionRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;
    @Mock
    private AccountService accountService;
    @Mock
    private IamAccountBroadcastMessage message;

    private static final String broadcastType = "workspace.cohort.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID accountId = UUIDs.timeBased();
    private static final UUID accountSubscriptionId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(iamAccountProvisionRTMConsumable.getContent()).thenReturn(message);
        when(iamAccountProvisionRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(iamAccountProvisionRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getAccountId()).thenReturn(accountId);
        when(accountService.getAccountPayload(accountId)).thenReturn(Mono.just(new AccountPayload()
                                                                                       .setAccountId(accountId)
                                                                                       .setSubscriptionId(subscriptionId)));
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(iamAccountProvisionRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        iamAccountProvisionRTMConsumer.accept(rtmClient, iamAccountProvisionRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(iamAccountProvisionRTMConsumable.getRTMClientContext()).thenReturn(producer);

        iamAccountProvisionRTMConsumer.accept(rtmClient, iamAccountProvisionRTMConsumable);

        final String expected = "{" +
                "\"type\":\"iam.account.provision.broadcast\"," +
                "\"response\":{"
                + "\"account\":" +
                "{" +
                "\"accountId\":\"" + accountId + "\","
                + "\"subscriptionId\":\"" + accountSubscriptionId + "\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
