package com.smartsparrow.rtm.message.handler.cohort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.service.CohortPermissionService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.GrantCohortPermissionMessage;
import com.smartsparrow.rtm.subscription.cohort.granted.CohortGrantedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class GrantCohortPermissionMessageHandlerTest {

    @Mock
    private CohortService cohortService;

    @Mock
    private AccountService accountService;
    @Mock
    private TeamService teamService;

    @Mock
    private CohortPermissionService cohortPermissionService;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    @Mock
    private CohortGrantedRTMProducer cohortGrantedRTMProducer;

    @InjectMocks
    private GrantCohortPermissionMessageHandler handler;
    @Mock
    private GrantCohortPermissionMessage accountMessage;
    @Mock
    private GrantCohortPermissionMessage teamMessage;

    private static final String messageId = "Kratos";
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        session = RTMWebSocketTestUtils.mockSession();

        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);

        when(accountMessage.getCohortId()).thenReturn(cohortId);
        when(accountMessage.getAccountIds()).thenReturn(Lists.newArrayList(accountId));
        when(accountMessage.getTeamIds()).thenReturn(null);
        when(accountMessage.getId()).thenReturn(messageId);
        when(accountMessage.getPermissionLevel()).thenReturn(PermissionLevel.REVIEWER);

        when(teamMessage.getCohortId()).thenReturn(cohortId);
        when(teamMessage.getTeamIds()).thenReturn(Lists.newArrayList(teamId));
        when(teamMessage.getAccountIds()).thenReturn(null);
        when(teamMessage.getId()).thenReturn(messageId);
        when(teamMessage.getPermissionLevel()).thenReturn(PermissionLevel.REVIEWER);

        when(accountService.findById(accountId))
                .thenReturn(Flux.just(new Account()
                        .setId(accountId)));

        when(cohortService.fetchCohortSummary(cohortId))
                .thenReturn(Mono.just(new CohortSummary()
                        .setId(cohortId)));
    }

    @Test
    void validate_noCohortId() {
        when(accountMessage.getCohortId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(accountMessage));

        assertEquals("workspace.cohort.permission.grant.error", t.getType());
        assertEquals("cohortId is required", t.getErrorMessage());
        assertEquals("Kratos", t.getReplyTo());
    }

    @Test
    void validate_noAccountIds() {
        when(accountMessage.getAccountIds()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(accountMessage));

        assertEquals("workspace.cohort.permission.grant.error", t.getType());
        assertEquals("either accountIds or teamIds is required", t.getErrorMessage());
        assertEquals("Kratos", t.getReplyTo());
    }

    @Test
    void validate_emptyAccountIds() {
        when(accountMessage.getAccountIds()).thenReturn(Lists.newArrayList());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(accountMessage));

        assertEquals("workspace.cohort.permission.grant.error", t.getType());
        assertEquals("either accountIds or teamIds is required", t.getErrorMessage());
        assertEquals("Kratos", t.getReplyTo());
    }

    @Test
    void validate_noTeamIds() {
        when(teamMessage.getTeamIds()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(teamMessage));

        assertEquals("workspace.cohort.permission.grant.error", t.getType());
        assertEquals("either accountIds or teamIds is required", t.getErrorMessage());
        assertEquals("Kratos", t.getReplyTo());
    }

    @Test
    void validate_emptyTeamIds() {
        when(teamMessage.getTeamIds()).thenReturn(Lists.newArrayList());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(teamMessage));

        assertEquals("workspace.cohort.permission.grant.error", t.getType());
        assertEquals("either accountIds or teamIds is required", t.getErrorMessage());
        assertEquals("Kratos", t.getReplyTo());
    }

    @Test
    void validate_noPermissionLevel() {
        when(teamMessage.getPermissionLevel()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(teamMessage));

        assertEquals("workspace.cohort.permission.grant.error", t.getType());
        assertEquals("permissionLevel is required", t.getErrorMessage());
        assertEquals("Kratos", t.getReplyTo());
    }

    @Test
    void validate_cohortNotFound() {
        when(cohortService.fetchCohortSummary(cohortId)).thenReturn(Mono.empty());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(accountMessage));

        assertEquals("workspace.cohort.permission.grant.error", t.getType());
        assertEquals(String.format("cohort not found for id %s", cohortId), t.getErrorMessage());
        assertEquals("Kratos", t.getReplyTo());
    }

    @Test
    void validate_accountNotFound() {
        when(accountService.findById(accountId)).thenReturn(Flux.empty());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(accountMessage));

        assertEquals("workspace.cohort.permission.grant.error", t.getType());
        assertEquals(String.format("account not found for id %s", accountId), t.getErrorMessage());
        assertEquals("Kratos", t.getReplyTo());
    }

    @Test
    void validate_teamNotFound() {
        when(teamService.findTeam(teamId)).thenReturn(Mono.empty());

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(teamMessage));

        assertEquals("workspace.cohort.permission.grant.error", t.getType());
        assertEquals(String.format("team not found for id %s", teamId), t.getErrorMessage());
        assertEquals("Kratos", t.getReplyTo());
    }

    @Test
    void handle_accountPermissions() throws WriteResponseException {
        when(cohortPermissionService.saveAccountPermissions(Lists.newArrayList(accountId), cohortId, accountMessage.getPermissionLevel()))
                .thenReturn(Flux.just(new Void[]{}));
        when(cohortGrantedRTMProducer.buildCohortGrantedRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortGrantedRTMProducer);

        handler.handle(session, accountMessage);

        String expected = "{" +
                "\"type\":\"workspace.cohort.permission.grant.ok\"," +
                "\"response\":{" +
                "\"cohortId\":\"" + cohortId + "\"," +
                "\"permissionLevel\":\"REVIEWER\"," +
                "\"accountIds\":[\"" + accountId + "\"]" +
                "}," +
                "\"replyTo\":\"Kratos\"" +
                "}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(cohortGrantedRTMProducer).buildCohortGrantedRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortGrantedRTMProducer).produce();
    }

    @Test
    void handle_teamPermissions() throws WriteResponseException {
        when(cohortPermissionService.saveTeamPermissions(Lists.newArrayList(teamId), cohortId, teamMessage.getPermissionLevel()))
                .thenReturn(Flux.just(new Void[]{}));
        when(cohortGrantedRTMProducer.buildCohortGrantedRTMConsumable(rtmClientContext, cohortId)).thenReturn(cohortGrantedRTMProducer);

        handler.handle(session, teamMessage);

        String expected = "{" +
                "\"type\":\"workspace.cohort.permission.grant.ok\"," +
                "\"response\":{" +
                "\"cohortId\":\"" + cohortId + "\"," +
                "\"permissionLevel\":\"REVIEWER\"," +
                "\"teamIds\":[\"" + teamId + "\"]" +
                "}," +
                "\"replyTo\":\"Kratos\"" +
                "}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(cohortGrantedRTMProducer).buildCohortGrantedRTMConsumable(eq(rtmClientContext), eq(cohortId));
        verify(cohortGrantedRTMProducer).produce();
    }

}
