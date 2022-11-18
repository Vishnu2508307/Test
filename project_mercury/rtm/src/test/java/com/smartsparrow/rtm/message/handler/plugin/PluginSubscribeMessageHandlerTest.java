package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.message.handler.plugin.PluginSubscribeMessageHandler.WORKSPACE_PLUGIN_PERMISSION_SUBSCRIBE_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.plugin.PluginGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionRTMSubscription;
import com.smartsparrow.rtm.subscription.plugin.PluginPermissionRTMSubscription.PluginPermissionRTMSubscriptionFactory;

import reactor.core.publisher.Mono;

class PluginSubscribeMessageHandlerTest {

    @InjectMocks
    private PluginSubscribeMessageHandler handler;

    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;

    @Mock
    private RTMSubscriptionManager rtmSubscriptionManager;

    @Mock
    private PluginPermissionRTMSubscription pluginPermissionRTMSubscription;

    @Mock
    private PluginPermissionRTMSubscriptionFactory pluginPermissionRTMSubscriptionFactory;

    @Mock
    private PluginService pluginService;

    @Mock
    private PluginGenericMessage message;

    private static final UUID pluginId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getPluginId()).thenReturn(pluginId);
        when(rtmSubscriptionManagerProvider.get()).thenReturn(rtmSubscriptionManager);

        handler = new PluginSubscribeMessageHandler(rtmSubscriptionManagerProvider, pluginPermissionRTMSubscriptionFactory,
                                                    pluginService);
        pluginPermissionRTMSubscription = new PluginPermissionRTMSubscription(pluginId);
        when(pluginPermissionRTMSubscriptionFactory.create(pluginId)).thenReturn(pluginPermissionRTMSubscription);

    }

    @Test
    void validate_missingPluginId() {
        when(message.getPluginId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, ()-> handler.validate(message));

        assertEquals("pluginId is required", t.getMessage());
    }

    @Test
    void validate_pluginNotFound(){
        when(pluginService.fetchById(pluginId)).thenReturn(Mono.empty());

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, ()-> handler.validate(message));

        assertEquals("plugin " + pluginId + " not found", t.getMessage());
    }

    @Test
    void handle_success() throws WriteResponseException, SubscriptionLimitExceeded, SubscriptionAlreadyExists {
        when(rtmSubscriptionManager.add(any(PluginPermissionRTMSubscription.class))).thenReturn(Mono.just(1));

        handler.handle(session, message);

        final String expected = "{\"type\":\"" + WORKSPACE_PLUGIN_PERMISSION_SUBSCRIBE_OK + "\","+
        "\"response\":{\"rtmSubscriptionId\":\"" + pluginPermissionRTMSubscription.getId() + "\"}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmSubscriptionManager).add(pluginPermissionRTMSubscription);
    }

    @Test
    void handle_subscriptionLimitError() {
        when(rtmSubscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionLimitExceeded()));
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.handle(session, message));
        assertEquals("Maximum number of subscriptions reached", t.getMessage());
    }

    @Test
    void handle_subscriptionExistsError() {
        when(rtmSubscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionAlreadyExists()));
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.handle(session, message));
        assertEquals("Subscription already exists", t.getMessage());
    }

}
