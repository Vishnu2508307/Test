package com.smartsparrow.rtm.message.handler.cohort;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.data.AccountCohortCollaborator;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.TeamCohortCollaborator;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.cohort.CohortCollaboratorSummaryMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CohortCollaboratorSummaryMessageHandlerTest {

    @Mock
    private CohortService cohortService;

    @Mock
    private AccountService accountService;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private CohortAccountSummaryMessageHandler handler;
    private Session session;

    private CohortCollaboratorSummaryMessage message;
    private static final UUID cohortId = UUID.randomUUID();
    private static final String messageId = "mozzarella";
    private static final Integer limit = 3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        message = mock(CohortCollaboratorSummaryMessage.class);

        when(message.getId()).thenReturn(messageId);
        when(message.getCohortId()).thenReturn(cohortId);
        when(message.getLimit()).thenReturn(limit);

        when(cohortService.fetchCohortSummary(cohortId)).thenReturn(Mono.just(new CohortSummary()));
    }

    @Test
    void validate_cohortIdNotSupplied() {
        when(message.getCohortId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("cohortId is required", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("workspace.cohort.collaborator.summary.error", e.getType());
    }

    @Test
    void validate_cohortNotFound() {
        when(cohortService.fetchCohortSummary(cohortId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals(String.format("cohort not found for id %s", cohortId), e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("workspace.cohort.collaborator.summary.error", e.getType());
    }

    @Test
    void validate_limitLessThanZero() {
        when(message.getLimit()).thenReturn(-1);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("limit should be >= 0", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("workspace.cohort.collaborator.summary.error", e.getType());
    }

    @Test
    void handle_noCollaborators() throws WriteResponseException {
        when(cohortService.fetchAccountCollaborators(cohortId)).thenReturn(Flux.empty());
        when(cohortService.fetchTeamCollaborators(cohortId)).thenReturn(Flux.empty());

        handler.handle(session, message);
        // @formatter:off
        String expected = "{" +
                            "\"type\":\"workspace.cohort.collaborator.summary.ok\"," +
                            "\"response\":{" +
                                "\"total\":0," +
                                "\"collaborators\":{}" +
                            "}," +
                            "\"replyTo\":\"mozzarella" +
                        "\"}";
        // @formatter:on
        verifySentMessage(session, expected);
    }

    @Test
    void handle_withLimit_onlyAccounts() throws WriteResponseException {
        when(cohortService.fetchAccountCollaborators(cohortId))
                .thenReturn(Flux.just(
                        buildAccountCollaborator(cohortId),
                        buildAccountCollaborator(cohortId),
                        buildAccountCollaborator(cohortId),
                        buildAccountCollaborator(cohortId)));
        when(cohortService.fetchTeamCollaborators(cohortId)).thenReturn(Flux.empty());

        when(accountService.getCollaboratorPayload(any(UUID.class), eq(PermissionLevel.CONTRIBUTOR)))
                .thenReturn(Mono.just(AccountCollaboratorPayload.from(new AccountPayload(), PermissionLevel.CONTRIBUTOR)));

        handler.handle(session, message);
        // @formatter:off
        String expected = "{" +
                            "\"type\":\"workspace.cohort.collaborator.summary.ok\"," +
                            "\"response\":{" +
                                "\"total\":4," +
                                "\"collaborators\":{" +
                                    "\"accounts\":[{" +
                                        "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                        "\"account\":{}" +
                                    "},{" +
                                        "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                        "\"account\":{}" +
                                    "},{" +
                                        "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                        "\"account\":{}" +
                                    "}]" +
                                "}}," +
                            "\"replyTo\":\"mozzarella\"}";
        // @formatter:on
        verifySentMessage(session, expected);
    }

    @Test
    void handle_withLimit_onlyTeams() throws WriteResponseException {
        when(cohortService.fetchAccountCollaborators(cohortId)).thenReturn(Flux.empty());
        when(cohortService.fetchTeamCollaborators(cohortId)).thenReturn(Flux.just(
                buildTeamCollaborator(cohortId),
                buildTeamCollaborator(cohortId),
                buildTeamCollaborator(cohortId),
                buildTeamCollaborator(cohortId)
        ));

        when(teamService.getTeamCollaboratorPayload(any(UUID.class), eq(PermissionLevel.REVIEWER)))
                .thenReturn(Mono.just(TeamCollaboratorPayload.from(new TeamSummary(), PermissionLevel.REVIEWER)));

        handler.handle(session, message);
        // @formatter:off
        String expected = "{" +
                           "\"type\":\"workspace.cohort.collaborator.summary.ok\"," +
                            "\"response\":{" +
                                "\"total\":4," +
                                "\"collaborators\":{" +
                                    "\"teams\":[{" +
                                        "\"permissionLevel\":\"REVIEWER\"," +
                                        "\"team\":{}" +
                                    "},{" +
                                        "\"permissionLevel\":\"REVIEWER\"," +
                                        "\"team\":{}" +
                                    "},{" +
                                        "\"permissionLevel\":\"REVIEWER\"," +
                                        "\"team\":{}" +
                                    "}]" +
                                "}}," +
                            "\"replyTo\":\"mozzarella\"}";
        // @formatter:on
        verifySentMessage(session, expected);
    }

    @Test
    void handle_withLimit_teamsAlwaysFirst() throws WriteResponseException {
        when(cohortService.fetchAccountCollaborators(cohortId))
                .thenReturn(Flux.just(
                        buildAccountCollaborator(cohortId),
                        buildAccountCollaborator(cohortId),
                        buildAccountCollaborator(cohortId)));
        when(cohortService.fetchTeamCollaborators(cohortId))
                .thenReturn(Flux.just(
                        buildTeamCollaborator(cohortId),
                        buildTeamCollaborator(cohortId)));

        when(accountService.getCollaboratorPayload(any(UUID.class), eq(PermissionLevel.CONTRIBUTOR)))
                .thenReturn(Mono.just(AccountCollaboratorPayload.from(new AccountPayload(), PermissionLevel.CONTRIBUTOR)));

        when(teamService.getTeamCollaboratorPayload(any(UUID.class), eq(PermissionLevel.REVIEWER)))
                .thenReturn(Mono.just(TeamCollaboratorPayload.from(new TeamSummary(), PermissionLevel.REVIEWER)));

        handler.handle(session, message);
        // @formatter:off
        String expected = "{" +
                            "\"type\":\"workspace.cohort.collaborator.summary.ok\"," +
                            "\"response\":{" +
                                "\"total\":5," +
                                "\"collaborators\":{" +
                                    "\"teams\":[{" +
                                        "\"permissionLevel\":\"REVIEWER\"," +
                                        "\"team\":{}" +
                                    "},{" +
                                        "\"permissionLevel\":\"REVIEWER\"," +
                                        "\"team\":{}" +
                                    "}]," +
                                    "\"accounts\":[{" +
                                        "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                        "\"account\":{}" +
                                    "}]" +
                                "}}," +
                            "\"replyTo\":\"mozzarella\"}";
        // @formatter:on
        verifySentMessage(session, expected);
    }

    @Test
    void handle_noLimit() throws WriteResponseException {
        when(message.getLimit()).thenReturn(null);

        when(cohortService.fetchAccountCollaborators(cohortId))
                .thenReturn(Flux.just(
                        buildAccountCollaborator(cohortId),
                        buildAccountCollaborator(cohortId),
                        buildAccountCollaborator(cohortId)));
        when(cohortService.fetchTeamCollaborators(cohortId))
                .thenReturn(Flux.just(
                        buildTeamCollaborator(cohortId),
                        buildTeamCollaborator(cohortId)));

        when(accountService.getCollaboratorPayload(any(UUID.class), eq(PermissionLevel.CONTRIBUTOR)))
                .thenReturn(Mono.just(AccountCollaboratorPayload.from(new AccountPayload(), PermissionLevel.CONTRIBUTOR)));

        when(teamService.getTeamCollaboratorPayload(any(UUID.class), eq(PermissionLevel.REVIEWER)))
                .thenReturn(Mono.just(TeamCollaboratorPayload.from(new TeamSummary(), PermissionLevel.REVIEWER)));

        handler.handle(session, message);
        // @formatter:off
        String expected = "{" +
                            "\"type\":\"workspace.cohort.collaborator.summary.ok\"," +
                            "\"response\":{" +
                                "\"total\":5," +
                                "\"collaborators\":{" +
                                    "\"teams\":[{" +
                                        "\"permissionLevel\":\"REVIEWER\"," +
                                        "\"team\":{}" +
                                    "},{" +
                                        "\"permissionLevel\":\"REVIEWER\"," +
                                        "\"team\":{}" +
                                    "}]," +
                                    "\"accounts\":[{" +
                                        "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                        "\"account\":{}" +
                                    "},{" +
                                        "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                        "\"account\":{}" +
                                    "},{" +
                                        "\"permissionLevel\":\"CONTRIBUTOR\"," +
                                        "\"account\":{}" +
                                    "}]" +
                                "}}," +
                            "\"replyTo\":\"mozzarella\"}";
        // @formatter:on
        verifySentMessage(session, expected);
    }

    private AccountCohortCollaborator buildAccountCollaborator(UUID cohortId) {
        return new AccountCohortCollaborator()
                .setCohortId(cohortId)
                .setPermissionLevel(PermissionLevel.CONTRIBUTOR)
                .setAccountId(UUID.randomUUID());
    }

    private TeamCohortCollaborator buildTeamCollaborator(UUID cohortId) {
        return new TeamCohortCollaborator()
                .setCohortId(cohortId)
                .setPermissionLevel(PermissionLevel.REVIEWER)
                .setTeamId(UUID.randomUUID());
    }
}
