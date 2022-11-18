package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.datastax.driver.core.utils.UUIDs;
import com.google.inject.Provider;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.payload.PluginSummaryPayload;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.plugin.ListPluginsMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class GetPluginsListMessageHandlerTest {

    @Mock
    private PluginService pluginService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @InjectMocks
    private GetPluginsListMessageHandler getPluginsListMessageHandler;

    private ListPluginsMessage listPluginsMessage;
    private UUID accountId = UUID.randomUUID();
    private Account account;
    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        listPluginsMessage = mock(ListPluginsMessage.class);
        when(listPluginsMessage.getPluginType()).thenReturn(null);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        account = mock(Account.class);
        session = RTMWebSocketTestUtils.mockSession();

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
    }

    @Test
    void handle_noPluginsFound() throws WriteResponseException {
        when(pluginService.fetchPublishedPlugins(account.getId(), listPluginsMessage.getPluginType())).thenReturn(Flux.empty());

        getPluginsListMessageHandler.handle(session, listPluginsMessage);

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);

        verify(session.getRemote()).sendStringByFuture(responseCaptor.capture());

        String expected = "{\"type\":\"author.plugin.list.ok\",\"response\":{\"plugins\":[]}}";
        assertEquals(expected, responseCaptor.getValue());
    }

    @Test
    void handle_pluginsFound() throws IOException {
        PluginType pluginType = PluginType.COMPONENT;
        PluginSummary summary1 = buildSummary();
        PluginSummary summary2 = buildSummary();
        PluginSummary summary3 = buildSummary();

        when(listPluginsMessage.getPluginType()).thenReturn(pluginType);
        when(pluginService.fetchPublishedPlugins(account.getId(), listPluginsMessage.getPluginType()))
                .thenReturn(Flux.just(summary1, summary2, summary3));
        when(pluginService.getPluginSummaryPayload(any(PluginSummary.class)))
                .thenAnswer((Answer<Mono<PluginSummaryPayload>>) invocation -> Mono.just(
                        PluginSummaryPayload.from(((PluginSummary) invocation.getArguments()[0]), new AccountPayload())));

        List<PluginSummaryPayload> payloads = getPluginSummaryPayloads(summary1, summary2, summary3);
        when(pluginService.filterPluginSummaryPayloads(any(),any())).thenReturn(payloads);

        getPluginsListMessageHandler.handle(session, listPluginsMessage);

        verifySentMessage(session, response -> {
            assertEquals(GetPluginsListMessageHandler.AUTHOR_PLUGIN_LIST_OK, response.getType());
            assertEquals(3, ((List) response.getResponse().get("plugins")).size());
        });
    }

    private List<PluginSummaryPayload> getPluginSummaryPayloads(final PluginSummary summary1,
                                                                final PluginSummary summary2,
                                                                final PluginSummary summary3) {
        List<PluginSummaryPayload> payloads = new ArrayList<>();
        PluginSummaryPayload from_one = PluginSummaryPayload
                .from(summary1, new AccountPayload());
        PluginSummaryPayload from_two = PluginSummaryPayload
                .from(summary2, new AccountPayload());
        PluginSummaryPayload from_three = PluginSummaryPayload
                .from(summary3, new AccountPayload());
        payloads.add(from_one);
        payloads.add(from_two);
        payloads.add(from_three);
        return payloads;
    }

    @Test
    void handle_success() throws IOException {
        PluginType pluginType = PluginType.COMPONENT;
        PluginSummary summary1 = buildSummary();

        AccountPayload accountPayload = new AccountPayload()
                .setAccountId(summary1.getCreatorId());

        PluginSummaryPayload from = PluginSummaryPayload
                .from(summary1, accountPayload);

        when(listPluginsMessage.getPluginType()).thenReturn(pluginType);
        when(pluginService.fetchPublishedPlugins(account.getId(), listPluginsMessage.getPluginType()))
                .thenReturn(Flux.just(summary1));
        when(pluginService.getPluginSummaryPayload(summary1)).thenReturn(
                Mono.just(PluginSummaryPayload
                        .from(summary1, accountPayload)));
        List<PluginSummaryPayload> payloads = new ArrayList<>();
        payloads.add(from);

        when(pluginService.filterPluginSummaryPayloads(any(),any())).thenReturn(payloads);

        getPluginsListMessageHandler.handle(session, listPluginsMessage);

        verifySentMessage(session, response -> {
            assertEquals(GetPluginsListMessageHandler.AUTHOR_PLUGIN_LIST_OK, response.getType());
            assertEquals(1, ((List) response.getResponse().get("plugins")).size());
            Map plugin = (Map)((List) response.getResponse().get("plugins")).get(0);

            assertEquals(summary1.getId().toString(), plugin.get("pluginId"));
            assertEquals(summary1.getLatestVersion(), plugin.get("latestVersion"));
            assertEquals(summary1.getType().toString(), plugin.get("type"));
            assertEquals(summary1.getCreatorId().toString(), ((Map)plugin.get("creator")).get("accountId"));
        });
    }

    private PluginSummary buildSummary() {
        return new PluginSummary()
                .setLatestVersion("1.0.0")
                .setId(UUIDs.timeBased())
                .setType(PluginType.COMPONENT)
                .setCreatorId(UUID.randomUUID());
    }
}
