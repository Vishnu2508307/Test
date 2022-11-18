package com.smartsparrow.rtm.message.handler.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.iam.EditRoleMessage;

import reactor.core.publisher.Flux;

class RemoveRoleMessageHandlerTest {
    @Mock
    private Provider<MutableAuthenticationContext> authenticationContextProvider;

    @Mock
    private AccountService accountService;

    private Session session;
    private RemoveRoleMessageHandler removeRoleMessageHandler;
    private EditRoleMessage invalidMessageNoAccount;
    private EditRoleMessage invalidMessageNoRole;
    private EditRoleMessage validMessage;
    private UUID accountId;
    private static final String role = String.valueOf(AccountRole.AERO_INSTRUCTOR);
    private Account target;
    private static final UUID subscriptionId = UUID.fromString("61a11563-1631-4df4-bd5a-b3c68ea466f5");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        session = RTMWebSocketTestUtils.mockSession();
        removeRoleMessageHandler = new RemoveRoleMessageHandler(authenticationContextProvider, accountService);
        accountId = UUID.randomUUID();
        MutableAuthenticationContext mutableAuthenticationContext = mock(MutableAuthenticationContext.class);
        Account account = new Account().setSubscriptionId(subscriptionId);
        target = new Account().setSubscriptionId(subscriptionId).setId(accountId);
        Set<AccountRole> roles = new HashSet<>();
        roles.add(Enum.valueOf(AccountRole.class, role));
        roles.add(AccountRole.STUDENT);
        target.setRoles(roles);
        invalidMessageNoAccount = mockInvalid();
        when(invalidMessageNoAccount.getRole()).thenReturn(String.valueOf(AccountRole.INSTRUCTOR));
        invalidMessageNoRole = mockInvalid();
        when(invalidMessageNoRole.getAccountId()).thenReturn(UUID.randomUUID());
        validMessage = mockValid(accountId);
        when(authenticationContextProvider.get()).thenReturn(mutableAuthenticationContext);
        when(mutableAuthenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    void validate_noRoleSupplied() {
        Throwable t = assertThrows(RTMValidationException.class, ()-> removeRoleMessageHandler.validate(invalidMessageNoRole));
        RTMValidationException e = (RTMValidationException) t;
        assertEquals(e.getErrorMessage(), "role is required");
    }

    @Test
    void validate_invalidRole() {
        when(validMessage.getRole()).thenReturn("Johnny Cage");
        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> removeRoleMessageHandler.validate(validMessage));
        assertEquals("Unknown `Johnny Cage` role supplied", e.getErrorMessage());
    }

    @Test
    void validate_instructorRole() {
        when(validMessage.getRole()).thenReturn("INSTRUCTOR");
        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> removeRoleMessageHandler.validate(validMessage));
        assertEquals("Deprecated `INSTRUCTOR` role supplied", e.getErrorMessage());
    }

    @Test
    void validate_noAccountIdSupplied() {
        Throwable t = assertThrows(RTMValidationException.class, ()-> removeRoleMessageHandler.validate(invalidMessageNoAccount));
        RTMValidationException e = (RTMValidationException) t;
        assertEquals(e.getErrorMessage(), "accountId is required");
    }

    @Test
    void validate_accountNotFound() {
        when(accountService.findById(validMessage.getAccountId())).thenReturn(Flux.empty());
        Throwable t = assertThrows(RTMValidationException.class, ()-> removeRoleMessageHandler.validate(validMessage));
        RTMValidationException e = (RTMValidationException) t;
        assertTrue(e.getErrorMessage().contains("account not found"));
    }

    @Test
    void handle_accountNotFound() throws WriteResponseException {

        when(accountService.findById(accountId)).thenReturn(Flux.empty());

        removeRoleMessageHandler.handle(session, validMessage);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(stringArgumentCaptor.capture());
        String responseMessage = stringArgumentCaptor.getValue();
        assertTrue(responseMessage.contains(String.valueOf(RemoveRoleMessageHandler.IAM_REMOVE_ROLE_ERROR)));
        assertTrue(responseMessage.contains("not found"));
        assertTrue(responseMessage.contains(String.valueOf(HttpStatus.NOT_FOUND_404)));
    }

    @Test
    void handle_doesNotContainRole() throws WriteResponseException {
        Set<AccountRole> roles = new HashSet<>();
        roles.add(AccountRole.STUDENT);
        target.setRoles(roles);
        when(accountService.findById(accountId)).thenReturn(Flux.just(target));

        removeRoleMessageHandler.handle(session, validMessage);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(stringArgumentCaptor.capture());
        String responseMessage = stringArgumentCaptor.getValue();

        assertTrue(responseMessage.contains(String.valueOf(RemoveRoleMessageHandler.IAM_REMOVE_ROLE_ERROR)));
        assertTrue(responseMessage.contains("does not contain role"));
        assertTrue(responseMessage.contains(String.valueOf(HttpStatus.BAD_REQUEST_400)));
    }

    @Test
    void handle_cannotRemoveWhenOnlyOneRole() throws WriteResponseException {
        Set<AccountRole> roles = new HashSet<>();
        roles.add(Enum.valueOf(AccountRole.class, role));
        target.setRoles(roles);
        when(accountService.findById(accountId)).thenReturn(Flux.just(target));

        removeRoleMessageHandler.handle(session, validMessage);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(stringArgumentCaptor.capture());
        String responseMessage = stringArgumentCaptor.getValue();

        assertTrue(responseMessage.contains(String.valueOf(RemoveRoleMessageHandler.IAM_REMOVE_ROLE_ERROR)));
        assertTrue(responseMessage.contains("cannot remove role"));
        assertTrue(responseMessage.contains(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY_422)));
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(accountService.findById(accountId)).thenReturn(Flux.just(target));

        removeRoleMessageHandler.handle(session, validMessage);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(stringArgumentCaptor.capture());
        String responseMessage = stringArgumentCaptor.getValue();
        System.out.println(responseMessage);
        assertTrue(responseMessage.contains(String.valueOf(RemoveRoleMessageHandler.IAM_REOMVE_ROLE_OK)));
        assertTrue(responseMessage.contains(String.valueOf(AccountRole.STUDENT)));
    }

    private EditRoleMessage mockValid(UUID accountId) {
        EditRoleMessage editRoleMessage = mock(EditRoleMessage.class);
        when(editRoleMessage.getAccountId()).thenReturn(accountId);
        when(editRoleMessage.getRole()).thenReturn(role);
        return editRoleMessage;
    }

    private EditRoleMessage mockInvalid() {
        EditRoleMessage editRoleMessage = mock(EditRoleMessage.class);
        when(editRoleMessage.getAccountId()).thenReturn(null);
        when(editRoleMessage.getRole()).thenReturn(null);
        return editRoleMessage;
    }
}
