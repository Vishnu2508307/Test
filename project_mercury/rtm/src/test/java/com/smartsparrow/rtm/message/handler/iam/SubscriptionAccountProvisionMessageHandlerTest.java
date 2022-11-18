package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.UUID;

import com.smartsparrow.iam.service.AuthenticationType;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.google.inject.Provider;
import com.smartsparrow.exception.ConflictException;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.service.Region;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.iam.AccountSubscriptionProvisionMessage;
import com.smartsparrow.rtm.subscription.iam.IamAccountProvisionRTMProducer;
import com.smartsparrow.util.Enums;

class SubscriptionAccountProvisionMessageHandlerTest {

    @Mock
    private AccountService accountService;
    @Mock
    private IamAccountProvisionRTMProducer iamAccountProvisionRTMProducer;

    private SubscriptionAccountProvisionMessageHandler handler;

    private AccountSubscriptionProvisionMessage message;
    private String validEmail = "citrus@dev.dev";
    private String authenticationType = AuthenticationType.BRONTE.toString();
    private Session session;
    private Account account;
    private UUID accountId = UUID.randomUUID();
    private UUID subscriptionId = UUID.randomUUID();

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {

        MockitoAnnotations.initMocks(this);

        message = mock(AccountSubscriptionProvisionMessage.class);
        session = RTMWebSocketTestUtils.mockSession();

        account = mock(Account.class);

        Provider<MutableAuthenticationContext> authenticationContextProvider = (Provider<MutableAuthenticationContext>) mock(Provider.class);
        MutableAuthenticationContext mutableAuthenticationContext = mock(MutableAuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(mutableAuthenticationContext);
        when(mutableAuthenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(account.getSubscriptionId()).thenReturn(subscriptionId);
        when(account.getIamRegion()).thenReturn(Region.AU);

        Provider<RTMClientContext> rtmClientContextProvider = (Provider<RTMClientContext>) mock(Provider.class);
        RTMClientContext rtmClientContext = mock(RTMClientContext.class);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        String clientId = "client-id";
        when(rtmClientContext.getClientId()).thenReturn(clientId);

        handler = new SubscriptionAccountProvisionMessageHandler(accountService, authenticationContextProvider,
                rtmClientContextProvider, iamAccountProvisionRTMProducer);

        when(iamAccountProvisionRTMProducer.buildAccountProvisionedRTMConsumable(rtmClientContext, subscriptionId, accountId))
                .thenReturn(iamAccountProvisionRTMProducer);
    }

    @Test
    void validate_noEmail() {
        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("Email and password are required",((RTMValidationException) t).getErrorMessage());
    }

    @Test
    void validate_noPassword() {
        when(message.getEmail()).thenReturn(validEmail);
        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("Email and password are required",((RTMValidationException) t).getErrorMessage());
    }

    @Test
    void validate_invalidEmail() {
        when(message.getEmail()).thenReturn("Kamehameha");
        when(message.getPassword()).thenReturn("final flash");
        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("Email is not valid",((RTMValidationException) t).getErrorMessage());
    }

    @Test
    void validate_noRoles() {
        when(message.getEmail()).thenReturn(validEmail);
        when(message.getPassword()).thenReturn("Makankosappo");
        when(message.getRoles()).thenReturn(new HashSet<>());

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("At least one role is required",((RTMValidationException) t).getErrorMessage());
    }

    @Test
    void validate_invalidRole() {
        when(message.getEmail()).thenReturn(validEmail);
        when(message.getPassword()).thenReturn("Masenko");
        when(message.getRoles()).thenReturn(Sets.newHashSet("UNKOWN"));

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("Unknown `UNKOWN` role supplied",
                ((RTMValidationException) t).getErrorMessage());
    }

    @Test
    @DisplayName("It should return an error with invalid roles when the message contains the INSTRUCTOR role")
    void validate_instructorRoleNotSupported() {
        when(message.getEmail()).thenReturn(validEmail);
        when(message.getPassword()).thenReturn("Sub-Zero");
        when(message.getRoles()).thenReturn(Sets.newHashSet("INSTRUCTOR", "ADMIN"));

        RTMValidationException t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("Invalid role/s supplied", t.getErrorMessage());
    }

    @Test
    @DisplayName("It should return an error with invalid roles when the message contains the INSTRUCTOR role")
    void validate_supportrRoleNotSupported() {
        when(message.getEmail()).thenReturn(validEmail);
        when(message.getPassword()).thenReturn("Sub-Zero");
        when(message.getRoles()).thenReturn(Sets.newHashSet("SUPPORT"));

        RTMValidationException t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("Invalid role/s supplied", t.getErrorMessage());
    }

    @Test
    void validate_tooManyRoles() {
        when(message.getEmail()).thenReturn(validEmail);
        when(message.getPassword()).thenReturn("Kaioken");
        when(message.getRoles()).thenReturn(Sets.newHashSet("ADMIN","SUPPORT","STUDENT","STUDENT_GUEST","ff","DEVELOPER", "AERO_INSTRUCTOR", "DEV"));

        Throwable t = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("Too many roles supplied", ((RTMValidationException) t).getErrorMessage());
    }

    @Test
    void handle_success() throws WriteResponseException, ConflictException {
        when(message.getEmail()).thenReturn(validEmail);
        when(message.getPassword()).thenReturn("Big bang attack");
        when(message.getRoles()).thenReturn(Sets.newHashSet("DEVELOPER"));
        AccountIdentityAttributes identity = new AccountIdentityAttributes().setPrimaryEmail(validEmail);
        AccountAdapter accountAdapter = mock(AccountAdapter.class);
        when(accountAdapter.getIdentityAttributes()).thenReturn(identity);
        when(accountAdapter.getAccount()).thenReturn(account);


        when(accountService.provision(AccountProvisionSource.RTM,
                account.getSubscriptionId(),
                Sets.newHashSet(Enums.of(AccountRole.class, "DEVELOPER")),
                null,
                null,
                null,
                null,
                message.getEmail(),
                message.getPassword(),
                false,
                null,
                null, AuthenticationType.BRONTE)).thenReturn(accountAdapter);

        handler.handle(session, message);
        String expected = "{\"type\":\"iam.subscription.user.provision.ok\"," +
                "\"response\":{" +
                "\"account\":{" +
                "\"accountId\":\""+accountId+"\"," +
                "\"subscriptionId\":\""+subscriptionId+"\"," +
                "\"iamRegion\":\"AU\"," +
                "\"primaryEmail\":\"citrus@dev.dev\"," +
                "\"authenticationType\":\""+authenticationType+"\"}}}";
        verifySentMessage(session, expected);
    }

    @Test
    void handle_fails() throws ConflictException, WriteResponseException {
        when(message.getEmail()).thenReturn(validEmail);
        when(message.getPassword()).thenReturn("Burning attack");
        when(message.getRoles()).thenReturn(Sets.newHashSet("DEVELOPER"));

        when(accountService.provision(AccountProvisionSource.RTM,
                account.getSubscriptionId(),
                Sets.newHashSet(Enums.of(AccountRole.class, "DEVELOPER")),
                null,
                null,
                null,
                null,
                message.getEmail(),
                message.getPassword(),
                false,
                null,
                null, AuthenticationType.BRONTE)).thenThrow(ConflictException.class);

        handler.handle(session, message);
        String expected = "{\"type\":\"iam.subscription.user.provision.error\",\"code\":409}";
        verifySentMessage(session, expected);
    }
}
