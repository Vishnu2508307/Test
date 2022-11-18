package com.smartsparrow.rtm.message.handler.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import com.smartsparrow.rtm.lang.RTMWebSocketHandlerException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.plugin.CreatePluginMessage;

import reactor.core.publisher.Mono;

class CreatePluginMessageHandlerTest {

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private PluginService pluginService;

    private CreatePluginMessageHandler createPluginMessageHandler;
    private CreatePluginMessage createPluginMessage;
    private Session session;
    private UUID creatorId = UUID.randomUUID();
    private final PluginType type = PluginType.COMPONENT;
    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        session = RTMWebSocketTestUtils.mockSession();
        createPluginMessageHandler = new CreatePluginMessageHandler(authenticationContextProvider, pluginService);
        createPluginMessage = mock(CreatePluginMessage.class);

        account = mock(Account.class);
        PluginSummary pluginSummary = new PluginSummary().setCreatorId(creatorId).setId(UUIDs.timeBased());
        UUID accountId = UUID.randomUUID();


        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        PluginType type = PluginType.COMPONENT;
        String name = "kamehameha";

        when(createPluginMessage.getId()).thenReturn("someId");
        when(createPluginMessage.getName()).thenReturn(name);

        when(createPluginMessage.getPluginType()).thenReturn(type);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(account.getSubscriptionId()).thenReturn(UUID.randomUUID());
        when(pluginService.createPluginSummary(name, type, null, account)).thenReturn(Mono.just(pluginSummary));
        when(pluginService.createPluginSummary(name, null, null, account)).thenReturn(Mono.just(pluginSummary));

        when(pluginService.getPluginSummaryPayload(any(PluginSummary.class)))
                .thenAnswer((Answer<Mono<PluginSummaryPayload>>) invocation -> Mono.just(
                        PluginSummaryPayload.from(((PluginSummary) invocation.getArguments()[0]), new AccountPayload())));
    }

    @Test
    void handle_noPluginType() throws WriteResponseException {
        when(createPluginMessage.getPluginType()).thenReturn(null);

        createPluginMessageHandler.handle(session, createPluginMessage);

        verify(pluginService, atLeastOnce())
                .createPluginSummary(createPluginMessage.getName(), null, null, account);
        verify(session.getRemote(), never()).sendStringByFuture(contains("error"));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(contains("create.ok"));
    }

    @Test
    void handle_withPluginType() throws WriteResponseException {
        when(createPluginMessage.getPluginType()).thenReturn(type);

        createPluginMessageHandler.handle(session, createPluginMessage);

        verify(pluginService, atLeastOnce()).createPluginSummary(createPluginMessage.getName(), type, null, account);
        verify(session.getRemote(), never()).sendStringByFuture(contains("error"));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(contains("create.ok"));
    }

    @Test
    void validate_noNameSupplied() {
        when(createPluginMessage.getName()).thenReturn("");

        RTMWebSocketHandlerException t = assertThrows(RTMWebSocketHandlerException.class, () ->
                createPluginMessageHandler.validate(createPluginMessage));
        assertEquals("name is required", t.getErrorMessage());
    }
}
