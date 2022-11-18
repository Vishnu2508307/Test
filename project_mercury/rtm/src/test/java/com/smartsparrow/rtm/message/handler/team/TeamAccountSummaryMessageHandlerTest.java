package com.smartsparrow.rtm.message.handler.team;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.team.AccountTeamCollaborator;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.team.TeamAccountSummaryMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class TeamAccountSummaryMessageHandlerTest {

    @InjectMocks
    private TeamAccountSummaryMessageHandler handler;

    @Mock
    private TeamService teamService;

    @Mock
    private AccountService accountService;

    private TeamAccountSummaryMessage message;
    private static final UUID teamId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final String messageId = "messageId";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(TeamAccountSummaryMessage.class);

        when(message.getTeamId()).thenReturn(teamId);
        when(message.getId()).thenReturn(messageId);
        when(message.getLimit()).thenReturn(null);
        when(teamService.findTeam(teamId)).thenReturn(Mono.just(new TeamSummary()));
        when(accountService.getCollaboratorPayload(any(UUID.class), any(PermissionLevel.class)))
                .thenReturn(Mono.just(new AccountCollaboratorPayload()));
    }

    @Test
    void validate_teamIdNotSupplied() {
        when(message.getTeamId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals("teamId is required", e.getErrorMessage());
            assertEquals("iam.team.account.summary.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        });
    }

    @Test
    void validate_teamNotFound() {
        when(teamService.findTeam(teamId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals(String.format("team %s not found", teamId), e.getErrorMessage());
            assertEquals("iam.team.account.summary.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        });
    }

    @Test
    void validate_limitIsZero() {

        when(message.getLimit()).thenReturn(-100);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals("limit should be a positive integer", e.getErrorMessage());
            assertEquals("iam.team.account.summary.error", e.getType());
            assertEquals(messageId, e.getReplyTo());
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        });
    }

    @Test
    void handle_success_limitNotSupplied() throws WriteResponseException {
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();
        AccountTeamCollaborator collaboratorOne = new AccountTeamCollaborator()
                .setAccountId(accountIdOne)
                .setPermissionLevel(PermissionLevel.REVIEWER);
        AccountTeamCollaborator collaboratorTwo = new AccountTeamCollaborator()
                .setAccountId(accountIdTwo)
                .setPermissionLevel(PermissionLevel.CONTRIBUTOR);

        when(teamService.findAllCollaboratorsForATeam(teamId)).thenReturn(Flux.just(collaboratorOne, collaboratorTwo));
        when(accountService.getCollaboratorPayload(eq(accountIdOne), any(PermissionLevel.class)))
                .thenReturn(Mono.just(buildCollaboratorPayload(accountIdOne, PermissionLevel.REVIEWER)));
        when(accountService.getCollaboratorPayload(eq(accountIdTwo), any(PermissionLevel.class)))
                .thenReturn(Mono.just(buildCollaboratorPayload(accountIdTwo, PermissionLevel.CONTRIBUTOR)));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"iam.team.account.summary.ok\"," +
                            "\"response\":{" +
                                "\"total\":2," +
                                "\"collaborators\":[{" +
                                    "\"permissionLevel\":\"REVIEWER\"," +
                                    "\"account\":{" +
                                        "\"accountId\":\""+accountIdOne+"\"" +
                                    "}" +
                                "},{" +
                                    "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                    "\"account\":{" +
                                        "\"accountId\":\""+accountIdTwo+"\"" +
                                    "}" +
                                "}]" +
                            "},\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success_limitSupplied() throws WriteResponseException {
        when(message.getLimit()).thenReturn(1);
        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();
        AccountTeamCollaborator collaboratorOne = new AccountTeamCollaborator()
                .setAccountId(accountIdOne)
                .setPermissionLevel(PermissionLevel.REVIEWER);
        AccountTeamCollaborator collaboratorTwo = new AccountTeamCollaborator()
                .setAccountId(accountIdTwo)
                .setPermissionLevel(PermissionLevel.CONTRIBUTOR);

        when(teamService.findAllCollaboratorsForATeam(teamId)).thenReturn(Flux.just(collaboratorOne, collaboratorTwo));
        when(accountService.getCollaboratorPayload(eq(accountIdOne), any(PermissionLevel.class)))
                .thenReturn(Mono.just(buildCollaboratorPayload(accountIdOne, PermissionLevel.REVIEWER)));

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"iam.team.account.summary.ok\"," +
                    "\"response\":{" +
                        "\"total\":2," +
                        "\"collaborators\":[{" +
                            "\"permissionLevel\":\"REVIEWER\"," +
                            "\"account\":{" +
                                "\"accountId\":\""+accountIdOne+"\"" +
                            "}" +
                        "}]" +
                    "},\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(accountService, never()).getCollaboratorPayload(eq(accountIdTwo), any(PermissionLevel.class));
    }

    @Test
    void handle_errorFindingCollaborators() throws WriteResponseException {
        TestPublisher<AccountTeamCollaborator> accountTeamPublisher = TestPublisher.create();

        Flux<AccountTeamCollaborator> error = accountTeamPublisher
                .error(new RuntimeException("error while fetching the collaborators")).flux();

        when(teamService.findAllCollaboratorsForATeam(teamId)).thenReturn(error);

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"iam.team.account.summary.error\"," +
                            "\"code\":422," +
                            "\"message\":\"Error while fetching collaborators\"," +
                            "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_errorGettingCollaboratorPayload() throws WriteResponseException {
        TestPublisher<AccountCollaboratorPayload> collaboratorPayloadPublisher = TestPublisher.create();
        Mono<AccountCollaboratorPayload> error = collaboratorPayloadPublisher
                .error(new RuntimeException("error while building payload")).mono();

        UUID accountIdOne = UUID.randomUUID();
        UUID accountIdTwo = UUID.randomUUID();
        AccountTeamCollaborator collaboratorOne = new AccountTeamCollaborator()
                .setAccountId(accountIdOne)
                .setPermissionLevel(PermissionLevel.REVIEWER);
        AccountTeamCollaborator collaboratorTwo = new AccountTeamCollaborator()
                .setAccountId(accountIdTwo)
                .setPermissionLevel(PermissionLevel.CONTRIBUTOR);

        when(teamService.findAllCollaboratorsForATeam(teamId)).thenReturn(Flux.just(collaboratorOne, collaboratorTwo));
        when(accountService.getCollaboratorPayload(eq(accountIdOne), any(PermissionLevel.class)))
                .thenReturn(error);
        when(accountService.getCollaboratorPayload(eq(accountIdTwo), any(PermissionLevel.class)))
                .thenReturn(Mono.just(buildCollaboratorPayload(accountIdTwo, PermissionLevel.CONTRIBUTOR)));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"iam.team.account.summary.ok\"," +
                            "\"response\":{" +
                                "\"total\":2," +
                                "\"collaborators\":[{" +
                                    "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                    "\"account\":{" +
                                        "\"accountId\":\""+accountIdTwo+"\"" +
                                    "}" +
                                "}]},\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(accountService, atLeastOnce()).getCollaboratorPayload(eq(accountIdTwo), any(PermissionLevel.class));
    }

    private AccountCollaboratorPayload buildCollaboratorPayload(UUID accountId, PermissionLevel permissionLevel) {
        return AccountCollaboratorPayload.from(new AccountPayload().setAccountId(accountId), permissionLevel);
    }
}
