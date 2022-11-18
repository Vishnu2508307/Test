package com.smartsparrow.rtm.message.handler.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.inject.Provider;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.iam.IamAccountProvisionRTMSubscription;

import reactor.core.publisher.Mono;

class AccountProvisionSubscribeHandlerTest {

    private Session session;

    private AccountProvisionSubscribeHandler accountProvisionSubscribeHandler;
    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    @Mock
    private RTMSubscriptionManager rtmSubscriptionManager;
    @Mock
    private IamAccountProvisionRTMSubscription iamAccountProvisionRTMSubscription;
    @Mock
    private IamAccountProvisionRTMSubscription.IamAccountProvisionRTMSubscriptionFactory iamAccountProvisionRTMSubscriptionFactory;
    @Mock
    private EmptyReceivedMessage emptyReceivedMessage;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private Account account;

    private UUID accountSubscriptionId = UUIDs.timeBased();
    private UUID subscriptionId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(rtmSubscriptionManagerProvider.get()).thenReturn(rtmSubscriptionManager);

        when(iamAccountProvisionRTMSubscription.getName()).thenReturn("iam.account.provision/subscription/" + accountSubscriptionId);
        when(iamAccountProvisionRTMSubscription.getId()).thenReturn(subscriptionId);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getSubscriptionId()).thenReturn(accountSubscriptionId);

        accountProvisionSubscribeHandler = new AccountProvisionSubscribeHandler(rtmSubscriptionManagerProvider,iamAccountProvisionRTMSubscriptionFactory,
                                                                                 authenticationContextProvider);
        iamAccountProvisionRTMSubscription = new IamAccountProvisionRTMSubscription(accountSubscriptionId);
        when(rtmSubscriptionManager.add(iamAccountProvisionRTMSubscription)).thenReturn(Mono.just(1));
        when(iamAccountProvisionRTMSubscriptionFactory.create(accountSubscriptionId)).thenReturn(iamAccountProvisionRTMSubscription);
    }

    @Test
    void handle_success() throws Exception {

        accountProvisionSubscribeHandler.handle(session, emptyReceivedMessage);

        ArgumentCaptor<IamAccountProvisionRTMSubscription> captor = ArgumentCaptor.forClass(IamAccountProvisionRTMSubscription.class);
        verify(rtmSubscriptionManager).add(captor.capture());
        assertEquals("iam.account.provision/subscription/" + accountSubscriptionId, captor.getValue().getName());

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"iam.account.provision.subscribe.ok\"," +
                "\"response\":{\"rtmSubscriptionId\":\"" + captor.getValue().getId() + "\"}}");

        verify(rtmSubscriptionManager).add(iamAccountProvisionRTMSubscription);
    }

    @Test
    void handle_limitExceeded() {
        when(rtmSubscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionLimitExceeded()));
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> accountProvisionSubscribeHandler.handle(session, emptyReceivedMessage));
        assertEquals("Maximum number of subscriptions reached", t.getMessage());
    }

    @Test
    void handle_subscriptionExistsError() {
        when(rtmSubscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionAlreadyExists()));
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> accountProvisionSubscribeHandler.handle(session, emptyReceivedMessage));
        assertEquals("Subscription already exists", t.getMessage());
    }
}
