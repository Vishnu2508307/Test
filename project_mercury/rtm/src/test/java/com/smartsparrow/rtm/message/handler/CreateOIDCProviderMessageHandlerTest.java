package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.iam.IamTestUtils.mockAuthenticationContextProvider;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.CreateOIDCProviderMessageHandler.IAM_OIDC_PROVIDER_CREATE_ERROR;
import static com.smartsparrow.rtm.message.handler.CreateOIDCProviderMessageHandler.IAM_OIDC_PROVIDER_CREATE_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.CreateOIDCProviderMessage;
import com.smartsparrow.sso.service.OpenIDConnectRelyingPartyCredential;
import com.smartsparrow.sso.service.OpenIDConnectService;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class CreateOIDCProviderMessageHandlerTest {

    @InjectMocks
    private CreateOIDCProviderMessageHandler handler;

    @Mock
    private CreateOIDCProviderMessage message;
    @Mock
    private OpenIDConnectService openIDConnectService;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    private Session session;

    private static final UUID subscriptionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getIssuerUrl()).thenReturn("http://google.com");
        when(message.getClientId()).thenReturn("clientId_123");
        when(message.getClientSecret()).thenReturn("client_secret");
        when(message.getRequestScope()).thenReturn("openid email");

        mockAuthenticationContextProvider(authenticationContextProvider, new Account().setSubscriptionId(subscriptionId));
    }

    @Test
    void validate_noIssuerUrl() {
        when(message.getIssuerUrl()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("issuerUrl is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());

        when(message.getIssuerUrl()).thenReturn("");

        t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("issuerUrl is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void validate_noClientId() {
        when(message.getClientId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("clientId is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());

        when(message.getClientId()).thenReturn("");

        t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("clientId is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void validate_noClientSecret() {
        when(message.getClientSecret()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("clientSecret is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());

        when(message.getClientSecret()).thenReturn("");

        t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("clientSecret is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void validate_noRequestScope() throws RTMValidationException {
        when(message.getRequestScope()).thenReturn(null);

        handler.validate(message);
    }

    @Test
    void validate_invalidRequestScope() {
        when(message.getRequestScope()).thenReturn("");

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("invalid requestScope", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());

    }


    @SuppressWarnings("unchecked")
    @Test
    void handle() throws IOException {
        OpenIDConnectRelyingPartyCredential expected =
                new OpenIDConnectRelyingPartyCredential()
                        .setRelyingPartyId(UUID.randomUUID())
                        .setSubscriptionId(subscriptionId);
        when(openIDConnectService.addCredential(subscriptionId, "http://google.com", "clientId_123",
                "client_secret", "openid email")).thenReturn(Mono.just(expected));

        handler.handle(session, message);

        verifySentMessage(session, response -> {
            assertEquals(IAM_OIDC_PROVIDER_CREATE_OK, response.getType());
            Map<String, Object> credential = (Map<String, Object>) response.getResponse().get("credential");
            assertEquals(2, credential.entrySet().size());
            assertEquals(expected.getRelyingPartyId().toString(), credential.get("relyingPartyId"));
            assertEquals(expected.getSubscriptionId().toString(), credential.get("subscriptionId"));
        });
    }

    @Test
    void handle_error() {
        TestPublisher<OpenIDConnectRelyingPartyCredential> publisher =
                TestPublisher.<OpenIDConnectRelyingPartyCredential>create().error(new RuntimeException("some exception"));
        when(openIDConnectService.addCredential(any(), any(), any(), any(), any())).thenReturn(publisher.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + IAM_OIDC_PROVIDER_CREATE_ERROR + "\",\"code\":500," +
                "\"message\":\"unhandled error occurred to message processing\"}");
    }
}
