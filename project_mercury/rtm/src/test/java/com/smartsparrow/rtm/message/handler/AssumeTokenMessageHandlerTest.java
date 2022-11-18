package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.iam.IamTestUtils.mockAuthenticationContextProvider;
import static com.smartsparrow.iam.service.AccountRole.ADMIN;
import static com.smartsparrow.iam.service.AccountRole.INSTRUCTOR;
import static com.smartsparrow.iam.service.AccountRole.STUDENT;
import static com.smartsparrow.iam.service.AccountRole.SUPPORT;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.AssumeTokenMessageHandler.IAM_ASSUME_TOKEN_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableSet;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountLogEntry;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.iam.AssumeTokenMessage;
import com.smartsparrow.util.Tokens;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AssumeTokenMessageHandlerTest {

    @InjectMocks
    private AssumeTokenMessageHandler assumeTokenMessageHandler;

    @Mock
    private AccountService accountService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private CredentialService credentialService;

    private static final Session session = RTMWebSocketTestUtils.mockSession();

    private UUID requestorId = UUIDs.random();

    // an account id with no existing account
    private UUID nonExistingAccountId = UUIDs.random();

    // a normal account
    private UUID accountIdTypical = UUIDs.random();
    private Account accountTypical = new Account().setId(accountIdTypical)
            .setRoles(ImmutableSet.of(INSTRUCTOR, ADMIN, STUDENT));

    // an account with support role
    private UUID accountIdWithSupport = UUIDs.random();
    private Account accountWithSupport = new Account().setId(accountIdWithSupport)
            .setRoles(ImmutableSet.of(SUPPORT, INSTRUCTOR, ADMIN, STUDENT));

    @BeforeEach
    private void beforeEach() {
        MockitoAnnotations.initMocks(this);

        //
        when(accountService.findById(eq(nonExistingAccountId))).thenReturn(Flux.empty());
        when(accountService.findById(eq(accountIdTypical))).thenReturn(Flux.just(accountTypical));
        when(accountService.findById(eq(accountIdWithSupport))).thenReturn(Flux.just(accountWithSupport));

        //
        mockAuthenticationContextProvider(authenticationContextProvider, new Account().setId(requestorId));

        when(credentialService.createWebSessionToken(any(UUID.class),
                                                     any(TemporalAmount.class),
                                                     isNull(),
                                                     isNull())) //
                .thenAnswer(invocation -> Mono.just(new WebSessionToken()
                                                            .setAccountId(invocation.getArgument(0))
                                                            .setValidUntilTs(Instant.now().plus(invocation.getArgument(1)).toEpochMilli())
                                                            .setToken(Tokens.generate())
                                                            .setCreatedTs(System.currentTimeMillis())));
    }

    @Test
    void validate() throws Exception {
        AssumeTokenMessage message = mock(AssumeTokenMessage.class);
        when(message.getAccountId()).thenReturn(accountIdTypical);

        assumeTokenMessageHandler.validate(message);
    }

    @Test
    void validate_nullAccountId() {
        AssumeTokenMessage message = mock(AssumeTokenMessage.class);
        when(message.getAccountId()).thenReturn(null);

        assertThrows(IllegalArgumentFault.class, () -> assumeTokenMessageHandler.validate(message));
    }

    @Test
    void validate_invalidAccountId() {
        AssumeTokenMessage message = mock(AssumeTokenMessage.class);
        when(message.getAccountId()).thenReturn(nonExistingAccountId);

        assertThrows(RTMValidationException.class, () -> assumeTokenMessageHandler.validate(message));
    }

    @Test
    void validate_notAsUserWithSupport() {
        AssumeTokenMessage message = mock(AssumeTokenMessage.class);
        when(message.getAccountId()).thenReturn(accountIdWithSupport);

        assertThrows(RTMValidationException.class, () -> assumeTokenMessageHandler.validate(message));
    }

    @Test
    void handle() throws Exception {
        AssumeTokenMessage message = mock(AssumeTokenMessage.class);
        when(message.getAccountId()).thenReturn(accountIdTypical);

        assumeTokenMessageHandler.handle(session, message);

        verify(accountService, times(2)) //
                .addLogEntry(any(UUID.class),
                             eq(AccountLogEntry.Action.BEARER_TOKEN_GENERATED),
                             any(UUID.class),
                             anyString());

        verifySentMessage(session, response -> {
            assertEquals(IAM_ASSUME_TOKEN_OK, response.getType());
            Map<String, Object> fields = (Map<String, Object>) response.getResponse();
            assertEquals(2, fields.entrySet().size());
            assertTrue(fields.containsKey("bearerToken"));
            assertTrue(fields.containsKey("expiry"));
        });
    }

}