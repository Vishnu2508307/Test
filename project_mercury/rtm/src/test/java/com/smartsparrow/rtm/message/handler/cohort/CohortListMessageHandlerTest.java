package com.smartsparrow.rtm.message.handler.cohort;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.cohort.payload.CohortSummaryPayload;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.cohort.CohortListMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CohortListMessageHandlerTest {

    @Mock
    private CohortService cohortService;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @InjectMocks
    private CohortListMessageHandler handler;

    private CohortListMessage message;

    private static final UUID cohortId = UUID.randomUUID();
    private static final String messageId = "Broly";
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(CohortListMessage.class);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        account = mock(Account.class);

        when(message.getId()).thenReturn(messageId);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(UUID.randomUUID());

    }

    @Test
    void handle() throws WriteResponseException {
        final UUID cohortIdTwo = UUID.randomUUID();
        when(cohortService.fetchCohorts(account.getId())).thenReturn(Flux.just(cohortId, cohortIdTwo));
        when(cohortService.getCohortSummaryPayload(cohortId)).thenReturn(Mono.just(new CohortSummaryPayload()));
        when(cohortService.getCohortSummaryPayload(cohortIdTwo)).thenReturn(Mono.just(new CohortSummaryPayload()));

        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.cohort.list.ok\"," +
                "\"response\":{\"cohorts\":[{},{}]},\"replyTo\":\"Broly\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
