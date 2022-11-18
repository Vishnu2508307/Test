package com.smartsparrow.rtm.message.handler.team;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.iam.data.team.TeamBySubscription;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.iam.payload.TeamPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.Region;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.ListMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ListSubscriptionTeamsMessageHandlerTest {

    @InjectMocks
    ListSubscriptionTeamsMessageHandler handler;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private TeamService teamService;

    private ListMessage message;

    private static final Session session = RTMWebSocketTestUtils.mockSession();

    private static final UUID subscriptionId = UUID.randomUUID();

    private static final UUID team1 = UUID.randomUUID();

    private static final Account account1 = new Account()
            .setId(UUID.randomUUID())
            .setSubscriptionId(subscriptionId)
            .setIamRegion(Region.GLOBAL);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(ListMessage.class);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);

        when(message.getCollaboratorLimit()).thenReturn(3);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account1);
    }

    @Test
    void handle() throws WriteResponseException {

        AccountSummaryPayload accountSummaryPayload1 = new AccountSummaryPayload()
                .setAccountId(account1.getId())
                .setSubscriptionId(subscriptionId)
                .setAvatarSmall("some avatar")
                .setFamilyName("last name")
                .setGivenName("first name");


        TeamPayload tp = new TeamPayload()
                .setName("myteam")
                .setAccountSummaryPayloads(Arrays
                        .asList(accountSummaryPayload1));

        TeamBySubscription t1 = new TeamBySubscription()
                .setSubscriptionId(subscriptionId)
                .setTeamId(team1);

        when(teamService.findAllTeamsBySubscription(any(UUID.class))).thenReturn(Flux.just(t1));
        when(teamService.getTeamPayload(any(UUID.class), any(Integer.class))).thenReturn(Mono.just(tp));

        handler.handle(session, message);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(
                "{\"type\":\"iam.subscription.team.list.ok\"," +
                        "\"response\":{\"teams\":[{\"name\":\"myteam\"," +
                        "\"accountSummaries\":[{\"accountId\":\"" + account1.getId() + "\"," +
                        "\"subscriptionId\":\"" + subscriptionId + "\"," +
                        "\"givenName\":\"first name\"," +
                        "\"familyName\":\"last name\"," +
                        "\"avatarSmall\":\"some avatar\"" +
                        "}]}]}}");
    }

    @Test
    void handle_fail() throws WriteResponseException {

        TestPublisher<TeamBySubscription> error = TestPublisher.create();
        error.error(new NoSuchElementException("test"));

        when(teamService.findAllTeamsBySubscription(any(UUID.class))).thenReturn(error.flux());

        handler.handle(session, message);

        verify(teamService, atLeastOnce()).findAllTeamsBySubscription(subscriptionId);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture("" +
                "{\"type\":\"iam.subscription.team.list.error\"," +
                "\"code\":422," +
                "\"message\":\"Unable to list teams\"}");

    }


    @Test
    void validate_limitIsANegativeInteger() {
        when(message.getCollaboratorLimit()).thenReturn(-300);
        when(message.getId()).thenReturn("messageId");

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals("collaboratorLimit should be a positive integer", e.getErrorMessage());
            assertEquals("iam.subscription.team.list.error", e.getType());
            assertEquals("messageId", e.getReplyTo());
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        });
    }
}
