package com.smartsparrow.rtm;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;

import com.google.inject.Provider;
import com.smartsparrow.dataevent.eventmessage.EventMessage;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.event.SimpleEventPublisher;
import com.smartsparrow.rtm.ws.RTMClient;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class RTMWebSocketTestUtils {

    /**
     * Mock the websocket Session. This mock can later be used to verify the send message via
     * {@link Session#getRemote()} and {@link RemoteEndpoint#sendStringByFuture(String)}
     * @return a {@link Session} mock
     */
    public static Session mockSession() {
        Session session = mock(Session.class);
        RemoteEndpoint remoteEndpoint = mock(RemoteEndpoint.class);
        InetAddress inetAddress = mock(InetAddress.class);
        InetSocketAddress remoteAddress = new InetSocketAddress(inetAddress, 0);
        Future<Void> future = genericMock(Future.class);

        when(session.isOpen()).thenReturn(Boolean.TRUE);
        when(session.getRemote()).thenReturn(remoteEndpoint);
        when(remoteEndpoint.sendStringByFuture(anyString())).thenReturn(future);
        when(session.getRemoteAddress()).thenReturn(remoteAddress);
        return session;
    }

    public static ReceivedMessage buildMessage(String type) {
        return new ReceivedMessage() {
            @Override
            public String getType() {
                return type;
            }
        };
    }

    /**
     * Mocks generic classes.
     * This method helps to reduce usage of @SuppressWarnings around the code.
     *
     * @param classToMock
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T genericMock(Class<? super T> classToMock) {
        return (T) mock(classToMock);
    }

    /**
     * Convenience method: Mocks a pair of {@link Provider}<T> and the provided mock obtained on a call of its
     * {@link Provider#get()}
     *
      * @param classToMock The class which will be mocked
     *
     * @return a Tuple with Provider<T> in {@link Tuple2#t1} and the result of its {@link Provider#get()}
     * in {@link Tuple2#t2}
     */
    public static <T> Tuple2<Provider<T>, T> mockProvidedClass(Class<? super T> classToMock) {
        Provider<T> mockProvider = RTMWebSocketTestUtils.genericMock(Provider.class);
        @SuppressWarnings("unchecked")
        T providedMock = (T) mock(classToMock);
        when(mockProvider.get()).thenReturn(providedMock);

        return Tuples.of(mockProvider, providedMock);
    }

    /**
     * Convenience method to mock the {@link CamelReactiveStreamsService}
     * @param route the string value of the camel route to mock
     * @param eventMessageClass any subclass of {@link EventMessage}
     * @return a {@link CamelReactiveStreamsService} mock
     */
    public static CamelReactiveStreamsService mockCamelStream(String route, Class<? extends EventMessage> eventMessageClass) {
        CamelReactiveStreamsService camel = mock(CamelReactiveStreamsService.class);

        when(camel.toStream(eq(route), any(eventMessageClass)))
                .thenReturn(o -> Mono.just(mock(Publisher.class)));

        return camel;
    }

    /**
     * Mock an {@link RTMClient} object with encapsulated fields
     *
     * @param session the {@link Session} mock. It should use the {@link RTMWebSocketTestUtils#mockSession()}.
     *                the session is taken as an argument in case the tester has to perform some verifications on
     *                the session mock later in the test.
     * @param clientId a {@link String} that represents the client id
     * @return a {@link RTMClient} mock
     */
    public static RTMClient mockRTMClient(Session session, String clientId) {
        RTMClient rtmClient = mock(RTMClient.class);
        RTMClientContext rtmClientContext = mock(RTMClientContext.class);

        when(rtmClientContext.getClientId()).thenReturn(clientId);
        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClient.getSession()).thenReturn(session);

        return rtmClient;
    }

    @SuppressWarnings({"unchecked", "Duplicates"}) //todo refactor to use IamTestUtils.mockAuthenticationContext
    public static AuthenticationContext mockAuthenticationContext(UUID accountId) {
        return mockAuthenticationContext(accountId, null);
    }

    @SuppressWarnings({"unchecked", "Duplicates"}) //todo refactor to use IamTestUtils.mockAuthenticationContext
    public static AuthenticationContext mockAuthenticationContext(UUID accountId, UUID subscriptionId) {
        Account account = mock(Account.class);
        AuthenticationContext context = mock(AuthenticationContext.class);

        when(account.getId()).thenReturn(accountId);
        when(account.getSubscriptionId()).thenReturn(subscriptionId);
        when(context.getAccount()).thenReturn(account);
        return context;
    }

    /**
     * Mock the super method {@link SimpleEventPublisher#getCamel()} that returns the injected camel reactive stream
     * service. The method also provides a generic mocking of {@link CamelReactiveStreamsService#toStream(String, Object)}
     * that returns a test publisher exchange.
     *
     * @param eventPublisher the event publisher to spy on
     * @param <T> the type of {@link SimpleEventPublisher}
     * @return a spy of the supplied {@param eventPublisher} with access to the injected camel service
     */
    @SuppressWarnings("unchecked")
    public static <T extends SimpleEventPublisher> T mockSimpleEventPublisher(T eventPublisher) {
        CamelReactiveStreamsService camel = mock(CamelReactiveStreamsService.class);
        TestPublisher<Exchange> exchangePublisher = TestPublisher.create();
        when(camel.toStream(anyString(), any(Object.class))).thenReturn(exchangePublisher);

        SimpleEventPublisher spy = Mockito.spy(eventPublisher);
        when(spy.getCamel()).thenReturn(camel);

        return (T) spy;
    }

    public static ReceivedMessage mockReceivedMessage() {
        return new ReceivedMessage() {
            @Override
            public String getType() {
                return "test.type";
            }
        };
    }
}
