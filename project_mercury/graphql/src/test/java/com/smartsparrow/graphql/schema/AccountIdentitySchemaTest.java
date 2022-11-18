package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.sso.service.IESService;
import com.smartsparrow.sso.service.IdentityProfile;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

class AccountIdentitySchemaTest {

    @InjectMocks
    private AccountIdentitySchema accountIdentitySchema;

    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    @Mock
    private AccountService accountService;

    @Mock
    private IESService iesService;

    @Mock
    private Account account;

    private ResolutionEnvironment resolutionEnvironment;

    private static final UUID accountId = UUID.randomUUID();
    private static final String pearsonToken = "token";
    private static final String pearsonUid = "pUid";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(account.getId()).thenReturn(accountId);

        when(mutableAuthenticationContext.getPearsonToken()).thenReturn(pearsonToken);
        when(mutableAuthenticationContext.getPearsonUid()).thenReturn(pearsonUid);

        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(mutableAuthenticationContext)).build(),
                                  null,
                                  null,
                                  null,
                                  null);

        when(accountService.findIdentityById(accountId))
                .thenReturn(Mono.just(new AccountIdentityAttributes()
                                              .setPrimaryEmail("email@dev.dev")
                                              .setFamilyName("Smith")
                                              .setGivenName("Bob")));
    }

    @Test
    void identityFromAccount_nonIES() {
        when(mutableAuthenticationContext.getAuthenticationType())
                .thenReturn(AuthenticationType.BRONTE);
        AccountIdentityAttributes result;

        result = accountIdentitySchema.identityFromAccount(resolutionEnvironment, account).join();

        assertNotNull(result);
        assertEquals("email@dev.dev", result.getPrimaryEmail());
        assertEquals("Bob", result.getGivenName());
        assertEquals("Smith", result.getFamilyName());

        // no mycloud impl yet so handle this as mercury behaviour
        when(mutableAuthenticationContext.getAuthenticationType())
                .thenReturn(AuthenticationType.MYCLOUD);

        result = accountIdentitySchema.identityFromAccount(resolutionEnvironment, account).join();

        assertNotNull(result);
        assertEquals("email@dev.dev", result.getPrimaryEmail());
        assertEquals("Bob", result.getGivenName());
        assertEquals("Smith", result.getFamilyName());
    }

    @Test
    void identityFromAccount_IESNotFound() {
        when(mutableAuthenticationContext.getAuthenticationType())
                .thenReturn(AuthenticationType.IES);

        when(iesService.getProfile(pearsonUid, pearsonToken))
                .thenReturn(Mono.empty());

        accountIdentitySchema.identityFromAccount(resolutionEnvironment, account)
                .handle((attr, ex) -> {
                    assertNotNull(ex);
                    assertEquals(NotFoundFault.class, ex.getClass());
                    assertEquals("identity profile not found", ex.getMessage());
                    return attr;
                });
    }

    @Test
    void identityFromAccount_IES() {
        when(mutableAuthenticationContext.getAuthenticationType())
                .thenReturn(AuthenticationType.IES);

        when(iesService.getProfile(pearsonUid, pearsonToken))
                .thenReturn(Mono.just(new IdentityProfile()
                                              .setFamilyName("Test")
                                              .setGivenName("User")
                                              .setId("ank")
                                              .setPrimaryEmail("dev@dev.dev")));

        AccountIdentityAttributes attr = accountIdentitySchema.identityFromAccount(resolutionEnvironment, account).join();

        assertAll(() -> {
            assertNotNull(attr);
            assertEquals(accountId, attr.getAccountId());
            assertEquals("User", attr.getGivenName());
            assertEquals("Test", attr.getFamilyName());
            assertEquals("dev@dev.dev", attr.getPrimaryEmail());
            assertEquals(1, attr.getEmail().size());
        });
    }

}
