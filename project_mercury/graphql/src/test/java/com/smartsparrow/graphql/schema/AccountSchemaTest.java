package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowSupportRole;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAccess;
import com.smartsparrow.iam.service.AccountLogEntry;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AccountShadowAttribute;
import com.smartsparrow.iam.service.AccountShadowAttributeName;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class AccountSchemaTest {

    @InjectMocks
    private AccountSchema accountSchema;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private AccountService accountService;

    @Mock
    private AllowSupportRole allowSupportRole;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private ResolutionEnvironment resolutionEnvironment;

    @Mock
    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(authenticationContext)).build(),
                                                         null,
                                                         null,
                                                         null,
                                                         null);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(UUID.randomUUID());
    }

    @Test
    void getAccountByEmail_nullEmail() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> accountSchema.getAccountByEmail(resolutionEnvironment, null));

        assertEquals("email argument is required", f.getMessage());
    }

    @Test
    void getAccountByEmail_notAllowed() {
        when(allowSupportRole.test(authenticationContext)).thenReturn(false);

        PermissionFault f = assertThrows(PermissionFault.class, () -> accountSchema.getAccountByEmail(resolutionEnvironment, "dev@dev.dev"));

        assertEquals("Not allowed", f.getMessage());
    }

    @Test
    void getAccountByEmail() {
        String email = "dev@dev.dev";

        when(allowSupportRole.test(authenticationContext)).thenReturn(true);
        when(accountService.findByEmail(email)).thenReturn(Flux.just(new Account()
                .setId(UUID.randomUUID())));

        Account account = accountSchema.getAccountByEmail(resolutionEnvironment,email).join();

        assertNotNull(account);
        verify(accountService).findByEmail(email);

        verify(accountService).addLogEntry(
                eq(authenticationContext.getAccount().getId()),
                eq(AccountLogEntry.Action.ACCOUNT_INFO_REQUESTED),
                eq(null),
                anyString()
        );

        verify(accountService).addLogEntry(
                eq(account.getId()),
                eq(AccountLogEntry.Action.ACCOUNT_INFO_REQUESTED),
                eq(null),
                anyString()
        );
    }

    @Test
    void getAccountAccess_notAllowed() {
        when(allowSupportRole.test(authenticationContext)).thenReturn(false);

        PermissionFault f = assertThrows(PermissionFault.class, () -> accountSchema.getAccountAccess(resolutionEnvironment, account));

        assertEquals("Not allowed", f.getMessage());
    }

    @Test
    void getAccountAccess_noAeroAccess() {
        when(allowSupportRole.test(authenticationContext)).thenReturn(true);
        TestPublisher<AccountShadowAttribute> publisher = TestPublisher.create();
        publisher.error(new NotFoundFault("not found"));

        when(accountService.findShadowAttribute(account, AccountShadowAttributeName.AERO_ACCESS)).thenReturn(publisher.mono());

        AccountAccess accountAccess = accountSchema.getAccountAccess(resolutionEnvironment,account).join();

        assertNotNull(accountAccess);
        assertNotNull(accountAccess.getAeroAccess());
        assertFalse(accountAccess.getAeroAccess());
    }

    @Test
    void getAccountAccess_hasAeroAccess() {
        when(allowSupportRole.test(authenticationContext)).thenReturn(true);
        when(accountService.findShadowAttribute(account, AccountShadowAttributeName.AERO_ACCESS))
                .thenReturn(Mono.just(new AccountShadowAttribute()));

        AccountAccess accountAccess = accountSchema.getAccountAccess(resolutionEnvironment,account).join();

        assertNotNull(accountAccess);
        assertNotNull(accountAccess.getAeroAccess());
        assertTrue(accountAccess.getAeroAccess());

    }
}
