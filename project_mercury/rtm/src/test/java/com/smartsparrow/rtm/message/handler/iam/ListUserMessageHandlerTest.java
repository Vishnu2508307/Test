package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.google.inject.Provider;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ListUserMessageHandlerTest {

    @InjectMocks
    private ListUserMessageHandler listUserMessageHandler;
    @Mock
    private AccountService accountService;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    private Session session;

    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final UUID SUBSCRIPTION_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        AuthenticationContext context = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(context);
        when(context.getAccount()).thenReturn(
                new Account().setId(ACCOUNT_ID).setSubscriptionId(SUBSCRIPTION_ID));
        session = RTMWebSocketTestUtils.mockSession();
    }

    @Test
    void handle_noAccounts() throws WriteResponseException {
        when(accountService.findBySubscription(eq(SUBSCRIPTION_ID))).thenReturn(Flux.empty());

        listUserMessageHandler.handle(session, new EmptyReceivedMessage());

        verifySentMessage(session, "{\"type\":\"iam.subscription.user.list.ok\",\"response\":{\"accounts\":[]}}");
    }

    @Test
    void handle_success() throws IOException {
        UUID otherAccountId1 = UUID.randomUUID();
        Account account = new Account()
                .setId(otherAccountId1)
                .setRoles(Sets.newHashSet(AccountRole.STUDENT, AccountRole.AERO_INSTRUCTOR));

        when(accountService.findBySubscription(eq(SUBSCRIPTION_ID))).thenReturn(Flux.just(account));
        when(accountService.getAccountPayload(account))
                .thenReturn(Mono.just(new AccountPayload()
                        .setAccountId(account.getId())
                        .setRoles(account.getRoles())
                        .setAvatarSmall("data:type;base64,data")
                        .setPrimaryEmail("email1@dev.com")
                        .setGivenName("GivenName")
                        .setFamilyName("FamilyName")
                        .setHonorificPrefix("pre")
                        .setHonorificSuffix("suf")));

        listUserMessageHandler.handle(session, new EmptyReceivedMessage());

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("iam.subscription.user.list.ok", response.getType());
                List accounts = ((List) response.getResponse().get("accounts"));
                assertEquals(1, accounts.size());
                Map accountPayload = (Map) (accounts.get(0));
                assertEquals(otherAccountId1.toString(), accountPayload.get("accountId"));
                assertEquals("email1@dev.com", accountPayload.get("primaryEmail"));
                assertEquals("GivenName", accountPayload.get("givenName"));
                assertEquals("FamilyName", accountPayload.get("familyName"));
                assertEquals("pre", accountPayload.get("honorificPrefix"));
                assertEquals("suf", accountPayload.get("honorificSuffix"));
                assertEquals(2, ((List)accountPayload.get("roles")).size());
                assertTrue(((List)accountPayload.get("roles")).contains("STUDENT"));
                assertTrue(((List)accountPayload.get("roles")).contains("AERO_INSTRUCTOR"));
                assertEquals("data:type;base64,data", accountPayload.get("avatarSmall"));
            });
        });
    }

    @Test
    void handle_severalUsers() throws IOException {
        UUID otherAccountId1 = UUID.randomUUID();
        UUID otherAccountId2 = UUID.randomUUID();

        Account accountOne = new Account().setId(otherAccountId1).setRoles(Sets.newHashSet(AccountRole.AERO_INSTRUCTOR));
        Account accountTwo = new Account().setId(ACCOUNT_ID).setRoles(Sets.newHashSet(AccountRole.DEVELOPER));
        Account accountThree = new Account().setId(otherAccountId2).setRoles(Sets.newHashSet(AccountRole.DEVELOPER));

        when(accountService.findBySubscription(eq(SUBSCRIPTION_ID)))
                .thenReturn(Flux.just(accountOne, accountTwo, accountThree));

        when(accountService.getAccountPayload(accountOne)).thenReturn(Mono.just(new AccountPayload()
                .setPrimaryEmail("email1@dev.com")
                .setAccountId(accountOne.getId())));

        when(accountService.getAccountPayload(accountTwo)).thenReturn(Mono.just(new AccountPayload()
                .setPrimaryEmail("email@dev.com")
                .setAccountId(accountTwo.getId())));

        when(accountService.getAccountPayload(accountThree)).thenReturn(Mono.just(new AccountPayload()
                .setPrimaryEmail("email2@dev.com")
                .setAccountId(accountThree.getId())));

        listUserMessageHandler.handle(session, new EmptyReceivedMessage());

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("iam.subscription.user.list.ok", response.getType());
                List accounts = ((List) response.getResponse().get("accounts"));
                assertEquals(3, accounts.size());
                assertEquals(otherAccountId1.toString(), ((Map) (accounts.get(0))).get("accountId"));
                assertEquals("email1@dev.com", ((Map) (accounts.get(0))).get("primaryEmail"));
                assertEquals(ACCOUNT_ID.toString(), ((Map) (accounts.get(1))).get("accountId"));
                assertEquals("email@dev.com", ((Map) (accounts.get(1))).get("primaryEmail"));
                assertEquals(otherAccountId2.toString(), ((Map) (accounts.get(2))).get("accountId"));
                assertEquals("email2@dev.com", ((Map) (accounts.get(2))).get("primaryEmail"));
            });
        });
    }

    @Test
    void handle_excludeStudents() throws IOException {
        UUID instructorAccountId = UUID.randomUUID();
        UUID studentAccountId = UUID.randomUUID();

        Account instructorAccount = new Account().setId(instructorAccountId)
                .setRoles(Sets.newHashSet(AccountRole.INSTRUCTOR, AccountRole.AERO_INSTRUCTOR, AccountRole.STUDENT));
        Account studentAccount = new Account().setId(studentAccountId).setRoles(Sets.newHashSet(AccountRole.STUDENT));

        when(accountService.findBySubscription(eq(SUBSCRIPTION_ID))).thenReturn(
                Flux.just(instructorAccount, studentAccount));

        when(accountService.getAccountPayload(instructorAccount))
                .thenReturn(Mono.just(new AccountPayload()
                .setAccountId(instructorAccount.getId())
                .setPrimaryEmail("instructor@dev.dev")));

        when(accountService.getAccountPayload(studentAccount))
                .thenReturn(Mono.just(new AccountPayload()
                .setAccountId(studentAccount.getId())
                .setPrimaryEmail("student@dev.dev")));

        listUserMessageHandler.handle(session, new EmptyReceivedMessage());

        verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals("iam.subscription.user.list.ok", response.getType());
                List accounts = ((List) response.getResponse().get("accounts"));
                assertEquals(1, accounts.size());
                assertEquals(instructorAccountId.toString(), ((Map) (accounts.get(0))).get("accountId"));
                assertEquals("instructor@dev.dev", ((Map) (accounts.get(0))).get("primaryEmail"));
            });
        });
    }
}
