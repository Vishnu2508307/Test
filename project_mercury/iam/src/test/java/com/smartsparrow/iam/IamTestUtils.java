package com.smartsparrow.iam;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;

public class IamTestUtils {

    @SuppressWarnings({"Duplicates"})
    public static AuthenticationContext mockAuthenticationContext(UUID accountId) {
        Account account = mock(Account.class);
        AuthenticationContext context = mock(AuthenticationContext.class);

        when(account.getId()).thenReturn(accountId);
        when(context.getAccount()).thenReturn(account);
        return context;
    }

    public static AuthenticationContext mockAuthenticationContext(Account account) {
        AuthenticationContext context = mock(AuthenticationContext.class);
        when(context.getAccount()).thenReturn(account);
        return context;
    }

    public static AuthenticationContextProvider mockAuthenticationContextProvider(AuthenticationContextProvider providerMock, Account account) {
        AuthenticationContext context = mock(AuthenticationContext.class);
        when(context.getAccount()).thenReturn(account);
        when(providerMock.get()).thenReturn(context);
        return providerMock;
    }

    public static AuthenticationContextProvider mockAuthenticationContextProvider(AuthenticationContextProvider providerMock, UUID accountId) {
        AuthenticationContext context = mock(AuthenticationContext.class);
        when(context.getAccount()).thenReturn(new Account().setId(accountId));
        when(providerMock.get()).thenReturn(context);
        return providerMock;
    }
}
