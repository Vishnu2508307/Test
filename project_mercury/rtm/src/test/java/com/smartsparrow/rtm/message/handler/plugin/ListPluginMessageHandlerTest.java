package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import com.smartsparrow.plugin.payload.PluginSummaryPayload;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ListPluginMessageHandlerTest {

    @InjectMocks
    private ListPluginMessageHandler listPluginMessageHandler;
    @Mock
    private PluginService pluginService;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;

    private Session session;

    private static final UUID CURRENT_ACCOUNT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Account account = mock(Account.class);
        session = RTMWebSocketTestUtils.mockSession();

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(CURRENT_ACCOUNT_ID);
    }

    @Test
    void handle() throws IOException {
        when(pluginService.fetchPlugins(eq(CURRENT_ACCOUNT_ID))).thenReturn(Flux.just(
                new PluginSummary().setCreatorId(CURRENT_ACCOUNT_ID).setId(UUIDs.timeBased()),
                new PluginSummary().setCreatorId(CURRENT_ACCOUNT_ID).setId(UUIDs.timeBased())));

        when(pluginService.getPluginSummaryPayload(any(PluginSummary.class)))
                .thenAnswer((Answer<Mono<PluginSummaryPayload>>) invocation -> Mono.just(
                        PluginSummaryPayload.from(((PluginSummary) invocation.getArguments()[0]), new AccountPayload())));

        listPluginMessageHandler.handle(session, new EmptyReceivedMessage());

        verifyListedPlugins(2);

    }

    @Test
    void handle_noPlugins() throws IOException {
        when(pluginService.fetchPlugins(eq(CURRENT_ACCOUNT_ID))).thenReturn(Flux.empty());

        listPluginMessageHandler.handle(session, new EmptyReceivedMessage());

        verifyListedPlugins(0);
    }

    @Test
    void handle_nullAccountInContext() throws WriteResponseException {
        when(authenticationContext.getAccount()).thenReturn(null);

        listPluginMessageHandler.handle(session, new EmptyReceivedMessage());

        String expected = "{" +
                "\"type\":\"workspace.plugin.list.error\"," +
                "\"code\":422," +
                "\"message\":\"error listing plugins\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_fetchPluginsError() throws WriteResponseException {
        TestPublisher<PluginSummary> error = TestPublisher.create();
        error.error(new RuntimeException("error fetching the summary"));

        when(pluginService.fetchPlugins(CURRENT_ACCOUNT_ID)).thenReturn(error.flux());

        listPluginMessageHandler.handle(session, new EmptyReceivedMessage());

        String expected = "{" +
                "\"type\":\"workspace.plugin.list.error\"," +
                "\"code\":422," +
                "\"message\":\"error listing plugins\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    private void verifyListedPlugins(int expectedSize) throws IOException {
        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("workspace.plugin.list.ok", response.getType());
                List responseList = ((List) response.getResponse().get("plugins"));
                assertEquals(expectedSize, responseList.size());
            });
        });
    }
}
