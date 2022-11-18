package com.smartsparrow.rtm.message.handler.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.iam.AccountSubscriptionMigrateMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AccountSubscriptionMigrateMessageHandlerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @InjectMocks
    private AccountSubscriptionMigrateMessageHandler handler;
    private Session session;
    private AccountSubscriptionMigrateMessage message;
    private Account accountToMigrate;
    private Account migratedAccount;
    private static final UUID subscruptionId = UUID.randomUUID();
    private static final Set<AccountRole> roles = Sets.newHashSet(AccountRole.DEVELOPER);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(AccountSubscriptionMigrateMessage.class);
        session = RTMWebSocketTestUtils.mockSession();
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        Account loggedAccount = mock(Account.class);
        accountToMigrate = mock(Account.class);
        migratedAccount = mock(Account.class);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(loggedAccount);
        when(message.getAccountId()).thenReturn(UUID.randomUUID());
        when(message.getRoles()).thenReturn(roles);

        when(loggedAccount.getSubscriptionId()).thenReturn(subscruptionId);
        when(migratedAccount.getSubscriptionId()).thenReturn(subscruptionId);
        when(accountService.findById(message.getAccountId())).thenReturn(Flux.just(accountToMigrate));
        when(accountService.migrateAccountTo(accountToMigrate, subscruptionId, roles)).thenReturn(Mono.just(migratedAccount));
        when(accountService.getAccountPayload(migratedAccount)).thenReturn(Mono.just(new AccountPayload()));
    }

    @Test
    void validate_noAccountId() {
        when(message.getAccountId()).thenReturn(null);

        Throwable t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("accountId is required", ((RTMValidationException) t).getErrorMessage());
    }

    @Test
    @DisplayName("It should return an error with invalid roles when the message contains the INSTRUCTOR role")
    void validate_supportrRoleNotSupported() {
        when(message.getRoles()).thenReturn(Sets.newHashSet(AccountRole.SUPPORT));

        RTMValidationException t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("Invalid role/s supplied", t.getErrorMessage());
    }

    @Test
    @DisplayName("It should return an error with invalid roles when the message contains the INSTRUCTOR role")
    void validate_instructorNotSupported() {
        when(message.getRoles()).thenReturn(Sets.newHashSet(AccountRole.INSTRUCTOR));

        RTMValidationException t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("Invalid role/s supplied", t.getErrorMessage());
    }

    @Test
    void validate_noRoles() {
        when(message.getRoles()).thenReturn(null);

        Throwable t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("roles field required and should provide at least 1 role",
                ((RTMValidationException) t).getErrorMessage());
    }

    @Test
    void validate_emptyRoles() {
        when(message.getRoles()).thenReturn(new HashSet<>());

        Throwable t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("roles field required and should provide at least 1 role",
                ((RTMValidationException) t).getErrorMessage());
    }

    @Test
    void handle_accountNotFound() throws WriteResponseException {
        when(accountService.findById(message.getAccountId())).thenReturn(Flux.empty());
        handler.handle(session, message);
        String expected = "{\"type\":\"iam.account.subscription.migrate.error\"," +
                "\"code\":404," +
                "\"response\":{" +
                "\"reason\":\"account not found for id `"+message.getAccountId()+"`\"}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_unexpectedError() throws WriteResponseException {
        when(accountService.migrateAccountTo(accountToMigrate, subscruptionId, roles)).thenReturn(Mono.empty());
        handler.handle(session, message);
        String expected = "{\"type\":\"iam.account.subscription.migrate.error\"," +
                "\"code\":422," +
                "\"response\":{\"reason\":\"An unexpected error occurred\"}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle() throws WriteResponseException {
        when(accountService.getAccountPayload(migratedAccount)).thenReturn(Mono.just(new AccountPayload()
                .setPrimaryEmail("email@dev.dev")
                .setSubscriptionId(subscruptionId)
                .setRoles(Sets.newHashSet(AccountRole.ADMIN))));

        handler.handle(session, message);

        String expected = "{\"type\":\"iam.account.subscription.migrate.ok\"," +
                "\"response\":{\"account\":{" +
                "\"subscriptionId\":\""+subscruptionId+"\"," +
                "\"primaryEmail\":\"email@dev.dev\"," +
                "\"roles\":[\"ADMIN\"]}}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_migrationFailure() throws WriteResponseException {
        when(accountService.migrateAccountTo(accountToMigrate, subscruptionId, roles))
                .thenThrow(new IllegalArgumentException("something blew up"));

        handler.handle(session, message);

        String expected = "{\"type\":\"iam.account.subscription.migrate.error\"," +
                "\"code\":400," +
                "\"response\":{\"reason\":\"something blew up\"}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
