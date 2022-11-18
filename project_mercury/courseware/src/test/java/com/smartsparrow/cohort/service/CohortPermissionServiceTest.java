package com.smartsparrow.cohort.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.data.AccountCohortCollaborator;
import com.smartsparrow.cohort.data.CohortAccount;
import com.smartsparrow.cohort.data.CohortGateway;
import com.smartsparrow.cohort.data.TeamCohortCollaborator;
import com.smartsparrow.iam.data.permission.cohort.AccountCohortPermission;
import com.smartsparrow.iam.data.permission.cohort.CohortPermissionGateway;
import com.smartsparrow.iam.data.permission.cohort.TeamCohortPermission;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CohortPermissionServiceTest {

    @Mock
    private CohortPermissionGateway cohortPermissionGateway;

    @Mock
    private CohortGateway cohortGateway;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private CohortPermissionService cohortPermissionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void saveAccountPermissions() {
        UUID accountId = UUID.randomUUID();
        UUID cohortId = UUID.randomUUID();
        PermissionLevel level = PermissionLevel.CONTRIBUTOR;
        when(cohortPermissionGateway.persist(any(AccountCohortPermission.class))).thenReturn(Flux.empty());
        when(cohortGateway.persist(any(CohortAccount.class))).thenReturn(Flux.empty());
        when(cohortGateway.persist(any(AccountCohortCollaborator.class))).thenReturn(Flux.empty());

        cohortPermissionService.saveAccountPermissions(accountId, cohortId, level);

        verify(cohortPermissionGateway).persist(eq(
                new AccountCohortPermission().setAccountId(accountId).setCohortId(cohortId).setPermissionLevel(level)));
        verify(cohortGateway).persist(eq(
                new CohortAccount().setAccountId(accountId).setCohortId(cohortId)));
        verify(cohortGateway).persist(eq(
                new AccountCohortCollaborator().setAccountId(accountId).setCohortId(cohortId).setPermissionLevel(level)));
    }

    @Test
    void saveAccountPermissions_list() {
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();
        UUID cohortId = UUID.randomUUID();
        PermissionLevel level = PermissionLevel.CONTRIBUTOR;
        when(cohortPermissionGateway.persist(any(AccountCohortPermission.class))).thenReturn(Flux.empty());
        when(cohortGateway.persist(any(CohortAccount.class))).thenReturn(Flux.empty());
        when(cohortGateway.persist(any(AccountCohortCollaborator.class))).thenReturn(Flux.empty());

        cohortPermissionService.saveAccountPermissions(Lists.newArrayList(accountId1, accountId2), cohortId, level);

        verify(cohortPermissionGateway).persist(eq(
                new AccountCohortPermission().setAccountId(accountId1).setCohortId(cohortId).setPermissionLevel(level)));
        verify(cohortGateway).persist(eq(
                new CohortAccount().setAccountId(accountId1).setCohortId(cohortId)));
        verify(cohortGateway).persist(eq(
                new AccountCohortCollaborator().setAccountId(accountId1).setCohortId(cohortId).setPermissionLevel(level)));
        verify(cohortPermissionGateway).persist(eq(
                new AccountCohortPermission().setAccountId(accountId2).setCohortId(cohortId).setPermissionLevel(level)));
        verify(cohortGateway).persist(eq(
                new CohortAccount().setAccountId(accountId2).setCohortId(cohortId)));
        verify(cohortGateway).persist(eq(
                new AccountCohortCollaborator().setAccountId(accountId2).setCohortId(cohortId).setPermissionLevel(level)));
    }

    @Test
    void deleteAccountPermissions() {
        UUID accountId = UUID.randomUUID();
        UUID cohortId = UUID.randomUUID();

        when(cohortPermissionGateway.delete(any(AccountCohortPermission.class))).thenReturn(Flux.empty());
        when(cohortGateway.delete(any(CohortAccount.class))).thenReturn(Flux.empty());
        when(cohortGateway.delete(any(AccountCohortCollaborator.class))).thenReturn(Flux.empty());

        cohortPermissionService.deleteAccountPermissions(accountId, cohortId).blockLast();

        verify(cohortPermissionGateway).delete(eq(
                new AccountCohortPermission().setAccountId(accountId).setCohortId(cohortId)));
        verify(cohortGateway).delete(eq(
                new CohortAccount().setAccountId(accountId).setCohortId(cohortId)));
        verify(cohortGateway).delete(eq(
                new AccountCohortCollaborator().setAccountId(accountId).setCohortId(cohortId)));
    }

    @Test
    void saveTeamPermissions() {
        UUID teamId = UUID.randomUUID();
        UUID cohortId = UUID.randomUUID();
        PermissionLevel level = PermissionLevel.CONTRIBUTOR;
        when(cohortPermissionGateway.persist(any(TeamCohortPermission.class))).thenReturn(Flux.empty());
        when(cohortGateway.persist(any(), any())).thenReturn(Flux.empty());
        when(cohortGateway.persist(any(TeamCohortCollaborator.class))).thenReturn(Flux.empty());

        cohortPermissionService.saveTeamPermissions(teamId, cohortId, level);

        verify(cohortPermissionGateway).persist(eq(new TeamCohortPermission().setTeamId(teamId).setCohortId(cohortId)
                .setPermissionLevel(level)));
        verify(cohortGateway).persist(eq(cohortId), eq(teamId));
        verify(cohortGateway).persist(eq(new TeamCohortCollaborator().setTeamId(teamId).setCohortId(cohortId)
                .setPermissionLevel(level)));
    }

    @Test
    void deleteTeamPermissions() {
        UUID teamId = UUID.randomUUID();
        UUID cohortId = UUID.randomUUID();

        when(cohortPermissionGateway.delete(any(TeamCohortPermission.class))).thenReturn(Flux.empty());
        when(cohortGateway.delete(any(), any())).thenReturn(Flux.empty());
        when(cohortGateway.delete(any(TeamCohortCollaborator.class))).thenReturn(Flux.empty());

        cohortPermissionService.deleteTeamPermissions(teamId, cohortId).blockLast();

        verify(cohortPermissionGateway).delete(eq(new TeamCohortPermission().setTeamId(teamId).setCohortId(cohortId)));
        verify(cohortGateway).delete(eq(cohortId), eq(teamId));
        verify(cohortGateway).delete(eq(new TeamCohortCollaborator().setTeamId(teamId).setCohortId(cohortId)));
    }

    @Test
    void saveTeamPermissions_list() {
        UUID teamId1 = UUID.randomUUID();
        UUID teamId2 = UUID.randomUUID();
        UUID cohortId = UUID.randomUUID();
        PermissionLevel level = PermissionLevel.CONTRIBUTOR;
        when(cohortPermissionGateway.persist(any(TeamCohortPermission.class))).thenReturn(Flux.empty());
        when(cohortGateway.persist(any(), any())).thenReturn(Flux.empty());
        when(cohortGateway.persist(any(TeamCohortCollaborator.class))).thenReturn(Flux.empty());

        cohortPermissionService.saveTeamPermissions(Lists.newArrayList(teamId1, teamId2), cohortId, level);

        verify(cohortPermissionGateway).persist(eq(new TeamCohortPermission().setTeamId(teamId1).setCohortId(cohortId)
                .setPermissionLevel(level)));
        verify(cohortGateway).persist(eq(cohortId), eq(teamId1));
        verify(cohortGateway).persist(eq(new TeamCohortCollaborator().setTeamId(teamId1).setCohortId(cohortId)
                .setPermissionLevel(level)));
        verify(cohortPermissionGateway).persist(eq(new TeamCohortPermission().setTeamId(teamId2).setCohortId(cohortId)
                .setPermissionLevel(level)));
        verify(cohortGateway).persist(eq(cohortId), eq(teamId2));
        verify(cohortGateway).persist(eq(new TeamCohortCollaborator().setTeamId(teamId2).setCohortId(cohortId)
                .setPermissionLevel(level)));
    }

    @Test
    void findHighestPermissionLevel() {
        UUID accountId = UUID.randomUUID();
        UUID teamId1 = UUID.randomUUID();
        UUID teamId2 = UUID.randomUUID();
        UUID cohortId = UUID.randomUUID();
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(
                new TeamAccount().setTeamId(teamId1).setAccountId(accountId),
                new TeamAccount().setTeamId(teamId2).setAccountId(accountId)));
        when(cohortPermissionGateway.findTeamPermission(teamId1, cohortId)).thenReturn(Mono.just(
                new TeamCohortPermission().setPermissionLevel(PermissionLevel.CONTRIBUTOR)));
        when(cohortPermissionGateway.findTeamPermission(teamId2, cohortId)).thenReturn(Mono.just(
                new TeamCohortPermission().setPermissionLevel(PermissionLevel.REVIEWER)));
        when(cohortPermissionGateway.findAccountPermission(accountId, cohortId)).thenReturn(Mono.just(
                new AccountCohortPermission().setPermissionLevel(PermissionLevel.OWNER)));

        PermissionLevel result = cohortPermissionService.findHighestPermissionLevel(accountId, cohortId).block();

        assertEquals(PermissionLevel.OWNER, result);
    }

    @Test
    void findHighestPermissionLevel_noTeams() {
        UUID accountId = UUID.randomUUID();
        UUID cohortId = UUID.randomUUID();
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());
        when(cohortPermissionGateway.findAccountPermission(accountId, cohortId)).thenReturn(Mono.just(
                new AccountCohortPermission().setPermissionLevel(PermissionLevel.CONTRIBUTOR)));

        PermissionLevel result = cohortPermissionService.findHighestPermissionLevel(accountId, cohortId).block();

        assertEquals(PermissionLevel.CONTRIBUTOR, result);
    }

    @Test
    void findHighestPermissionLevel_noPermission() {
        UUID accountId = UUID.randomUUID();
        UUID cohortId = UUID.randomUUID();
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());
        when(cohortPermissionGateway.findAccountPermission(accountId, cohortId)).thenReturn(Mono.empty());

        PermissionLevel result = cohortPermissionService.findHighestPermissionLevel(accountId, cohortId).block();

        assertNull(result);
    }

    @Test
    void findHighestPermissionLevel_noTeamPermission() {
        UUID accountId = UUID.randomUUID();
        UUID teamId1 = UUID.randomUUID();
        UUID cohortId = UUID.randomUUID();
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(
                new TeamAccount().setTeamId(teamId1).setAccountId(accountId)));
        when(cohortPermissionGateway.findTeamPermission(teamId1, cohortId)).thenReturn(Mono.empty());
        when(cohortPermissionGateway.findAccountPermission(accountId, cohortId)).thenReturn(Mono.just(
                new AccountCohortPermission().setPermissionLevel(PermissionLevel.REVIEWER)));

        PermissionLevel result = cohortPermissionService.findHighestPermissionLevel(accountId, cohortId).block();

        assertEquals(PermissionLevel.REVIEWER, result);
    }

}
