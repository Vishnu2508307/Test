package com.smartsparrow.rtm.ws;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.genericMock;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMWebSocketHandlerException;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.MessageType;
import com.smartsparrow.rtm.message.ReceivedMessage;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RTMWebSocketAuthorizerTest {

    @Mock
    private Map<String, Collection<Provider<AuthorizationPredicate<? extends MessageType>>>> authorizerPredicates;

    private RTMWebSocketAuthorizer rtmWebSocketAuthorizer;
    private ReceivedMessage invalid;
    private ReceivedMessage unauthorized;
    private ReceivedMessage authorized;

    @BeforeAll
    void setUp() {
        MockitoAnnotations.initMocks(this);
        rtmWebSocketAuthorizer = new RTMWebSocketAuthorizer(authorizerPredicates);

        invalid = RTMWebSocketTestUtils.buildMessage("invalid");
        unauthorized = RTMWebSocketTestUtils.buildMessage("unauthorized");
        authorized = RTMWebSocketTestUtils.buildMessage("authorized");

    }

    @Test
    void hasAuthorizer() {
        when(authorizerPredicates.containsKey(invalid.getType())).thenReturn(false);
        when(authorizerPredicates.containsKey(unauthorized.getType())).thenReturn(true);
        when(authorizerPredicates.containsKey(authorized.getType())).thenReturn(true);

        assertAll("do messages have authorizers?",
                () -> assertFalse(rtmWebSocketAuthorizer.hasAuthorizer(invalid)),
                () -> assertTrue(rtmWebSocketAuthorizer.hasAuthorizer(unauthorized)),
                () -> assertTrue(rtmWebSocketAuthorizer.hasAuthorizer(authorized))
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void isAuthorized() throws RTMWebSocketHandlerException {
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);

        AuthorizationPredicate authorizedPredicate = genericMock(AuthorizationPredicate.class);
        Provider<AuthorizationPredicate<? extends MessageType>> authorizerPredicateProvider = genericMock(Provider.class);
        List<Provider<AuthorizationPredicate<? extends MessageType>>> authorizedProviders = Lists.newArrayList(authorizerPredicateProvider);


        when(authorizerPredicates.get(authorized.getType())).thenReturn(authorizedProviders);
        when(authorizerPredicateProvider.get()).thenReturn(authorizedPredicate);
        when(authorizedPredicate.test(authenticationContext, authorized)).thenReturn(true);

        rtmWebSocketAuthorizer.authorize(authorized, authenticationContext);
    }

    @SuppressWarnings("unchecked")
    @Test
    void isNotAuthorized() {
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);

        AuthorizationPredicate unauthorizedPredicate = genericMock(AuthorizationPredicate.class);
        Provider<AuthorizationPredicate<? extends MessageType>> unauthorizedPredicateProvider = genericMock(Provider.class);
        List<Provider<AuthorizationPredicate<? extends MessageType>>> unauthorizedProviders = Lists.newArrayList(unauthorizedPredicateProvider);


        when(authorizerPredicates.get(unauthorized.getType())).thenReturn(unauthorizedProviders);
        when(unauthorizedPredicateProvider.get()).thenReturn(unauthorizedPredicate);
        when(unauthorizedPredicate.test(authenticationContext, authorized)).thenReturn(false);

        assertThrows(RTMWebSocketHandlerException.class, ()->  rtmWebSocketAuthorizer.authorize(unauthorized, authenticationContext));
    }
}
