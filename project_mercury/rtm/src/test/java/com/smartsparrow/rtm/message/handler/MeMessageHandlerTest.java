package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import com.smartsparrow.iam.service.MutableAuthenticationContext;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;

import com.google.inject.Provider;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.Region;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.MeMessage;

import reactor.core.publisher.Mono;

class MeMessageHandlerTest {

    @InjectMocks
    private MeMessageHandler meMessageHandler;

    @Mock
    private Provider<MutableAuthenticationContext> authenticationContextProvider;

    @Mock
    private AccountService accountService;

    private Session session;
    private MutableAuthenticationContext authenticationContext;
    private static final UUID accountId = UUID.randomUUID();
    @Mock
    private MeMessage meMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        session = RTMWebSocketTestUtils.mockSession();
        authenticationContext = mock(MutableAuthenticationContext.class);
        Account account = mock(Account.class);
        AccountPayload accountPayload = new AccountPayload()
                .setAccountId(accountId)
                .setPrimaryEmail("an@email.dev")
                .setGivenName("Will")
                .setFamilyName("Turner")
                .setHonorificPrefix("Mr.");

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(authenticationContext.getAuthenticationType()).thenReturn(AuthenticationType.BRONTE);
        when(account.getId()).thenReturn(accountId);
        when(account.getRoles()).thenReturn(Sets.newSet(AccountRole.INSTRUCTOR, AccountRole.STUDENT));
        when(account.getIamRegion()).thenReturn(Region.GLOBAL);

        when(accountService.getAccountPayload(account)).thenReturn(Mono.just(accountPayload));
    }

    @Test
    void handle() throws WriteResponseException {

        meMessageHandler.handle(session, meMessage);
        String expectedMessage = "{\"type\":\"me.get.ok\"," +
                "\"response\":{\"account\":{" +
                "\"accountId\":\""+accountId+"\"," +
                "\"honorificPrefix\":\"Mr.\"," +
                "\"givenName\":\"Will\"," +
                "\"familyName\":\"Turner\"," +
                "\"primaryEmail\":\"an@email.dev\"}}}";
        verifySentMessage(session, expectedMessage);
    }

    @Test
    void handle_notAuthorized() throws IOException {
        when(meMessage.getId()).thenReturn("messageId");
        when(authenticationContext.getAccount()).thenReturn(null);
        meMessageHandler.handle(session, meMessage);

        verifySentMessage(session, "{\"type\":\"me.get.error\",\"code\":401," +
                "\"response\":{\"reason\":\"Unauthorized\"},\"replyTo\":\"messageId\"}");
    }

}
