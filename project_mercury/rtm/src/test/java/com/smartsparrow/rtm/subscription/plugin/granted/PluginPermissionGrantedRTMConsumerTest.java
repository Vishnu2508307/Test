package com.smartsparrow.rtm.subscription.plugin.granted;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.plugin.data.PluginAccountCollaborator;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionBroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

public class PluginPermissionGrantedRTMConsumerTest {

    @InjectMocks
    private PluginPermissionGrantedRTMConsumer grantedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private PluginPermissionGrantedRTMConsumable pluginPermissionGrantedRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private PluginPermissionBroadcastMessage message;

    @Mock
    private AccountService accountService;

    @Mock
    private PluginService pluginService;

    private static final String broadcastType = "workspace.plugin.permission.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID pluginId = UUIDs.timeBased();
    private static final UUID accountId = UUIDs.timeBased();
    private static final UUID teamId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(pluginPermissionGrantedRTMConsumable.getContent()).thenReturn(message);
        when(pluginPermissionGrantedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(pluginPermissionGrantedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getPluginId()).thenReturn(pluginId);
        when(message.getAccountId()).thenReturn(accountId);
        when(message.getTeamId()).thenReturn(teamId);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(pluginPermissionGrantedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        grantedRTMConsumer.accept(rtmClient, pluginPermissionGrantedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket for an account")
    void accept_writesToWebSocket_onGranted_account() throws IOException {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(pluginPermissionGrantedRTMConsumable.getRTMClientContext()).thenReturn(producer);
        when(pluginService.findAccountCollaborator(pluginId, accountId))
                .thenReturn(Mono.just(new PluginAccountCollaborator().setPermissionLevel(PermissionLevel.CONTRIBUTOR)));
        when(accountService.getCollaboratorPayload(accountId, PermissionLevel.CONTRIBUTOR))
                .thenReturn(Mono.just(AccountCollaboratorPayload.from(new AccountPayload().setAccountId(accountId),
                                                                      PermissionLevel.CONTRIBUTOR)));

        grantedRTMConsumer.accept(rtmClient, pluginPermissionGrantedRTMConsumable);

        verifySentMessage(rtmClient.getSession(), response -> assertAll(
                () -> assertEquals("workspace.plugin.permission.broadcast", response.getType()),
                () -> assertEquals(subscriptionId.toString(), response.getReplyTo()),
                () -> assertEquals(pluginId.toString(), response.getResponse().get("pluginId")),
                () -> assertEquals("GRANTED", response.getResponse().get("action")),
                () -> assertEquals("PLUGIN_PERMISSION_GRANTED", response.getResponse().get("rtmEvent")),
                () -> {
                    Map collaborator = (Map) response.getResponse().get("collaborator");
                    assertEquals("CONTRIBUTOR", collaborator.get("permissionLevel"));
                    Map account = (Map) collaborator.get("account");
                    assertEquals(accountId.toString(), account.get("accountId"));
                    assertNull(collaborator.get("team"));
                }));
    }
}
