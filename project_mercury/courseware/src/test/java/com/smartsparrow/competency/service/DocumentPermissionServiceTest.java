package com.smartsparrow.competency.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.smartsparrow.competency.data.AccountDocumentCollaborator;
import com.smartsparrow.competency.data.DocumentAccount;
import com.smartsparrow.competency.data.DocumentGateway;
import com.smartsparrow.competency.data.DocumentTeam;
import com.smartsparrow.competency.data.TeamDocumentCollaborator;
import com.smartsparrow.iam.data.permission.competency.AccountDocumentPermission;
import com.smartsparrow.iam.data.permission.competency.DocumentPermissionGateway;
import com.smartsparrow.iam.data.permission.competency.TeamDocumentPermission;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DocumentPermissionServiceTest {

    @InjectMocks
    DocumentPermissionService documentPermissionService;

    @Mock
    DocumentPermissionGateway documentPermissionGateway;

    @Mock
    DocumentGateway documentGateway;

    @Mock
    TeamService teamService;

    UUID accountId = UUID.randomUUID();
    UUID documentId = UUID.randomUUID();
    UUID teamId = UUID.randomUUID();
    UUID teamId1 = UUID.randomUUID();
    UUID teamId2 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void fetchAccountPermission_NotFound() {
        when(documentPermissionGateway.findAccountPermission(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.empty());

        PermissionLevel permissionLevel = documentPermissionService
                .fetchAccountPermission(accountId, documentId)
                .block();
        assertNull(permissionLevel);
    }

    @Test
    void fetchAccountPermission_Valid() {
        when(documentPermissionGateway.findAccountPermission(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(new AccountDocumentPermission()
                        .setDocumentId(documentId)
                        .setAccountId(accountId)
                        .setPermissionLevel(PermissionLevel.OWNER)));

        PermissionLevel permissionLevel = documentPermissionService
                .fetchAccountPermission(accountId, documentId)
                .block();
        assertNotNull(permissionLevel);
        assertEquals(PermissionLevel.OWNER, permissionLevel);
    }

    @Test
    void fetchTeamPermission_NotFound() {
        when(documentPermissionGateway.findTeamPermission(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.empty());

        PermissionLevel permissionLevel = documentPermissionService
                .fetchTeamPermission(accountId, documentId)
                .block();
        assertNull(permissionLevel);
    }

    @Test
    void fetchTeamPermission_Valid() {
        when(documentPermissionGateway.findTeamPermission(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(new TeamDocumentPermission()
                        .setDocumentId(documentId)
                        .setTeamId(teamId)
                        .setPermissionLevel(PermissionLevel.OWNER)));

        PermissionLevel permissionLevel = documentPermissionService
                .fetchTeamPermission(teamId, documentId)
                .block();

        assertNotNull(permissionLevel);
        assertEquals(PermissionLevel.OWNER, permissionLevel);
    }

    @Test
    void findHighestPermissionLevel_noTeams() {
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());
        when(documentPermissionGateway.findAccountPermission(accountId, documentId))
                .thenReturn(Mono.just(new AccountDocumentPermission()
                        .setPermissionLevel(PermissionLevel.CONTRIBUTOR)));

        PermissionLevel pl = documentPermissionService.findHighestPermissionLevel(accountId, documentId).block();

        assertEquals(PermissionLevel.CONTRIBUTOR, pl);
    }

    @Test
    void findHighestPermissionLevel_noPermission() {
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());
        when(documentPermissionGateway.findAccountPermission(accountId, documentId)).thenReturn(Mono.empty());

        PermissionLevel result = documentPermissionService.findHighestPermissionLevel(accountId, documentId).block();

        assertNull(result);
    }

    @Test
    void findHighestPermissionLevel_noTeamPermission() {
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.just(
                new TeamAccount().setTeamId(teamId).setAccountId(accountId)));
        when(documentPermissionGateway.findTeamPermission(teamId, documentId)).thenReturn(Mono.empty());
        when(documentPermissionGateway.findAccountPermission(accountId, documentId)).thenReturn(Mono.just(
                new AccountDocumentPermission().setPermissionLevel(PermissionLevel.REVIEWER)));

        PermissionLevel result = documentPermissionService.findHighestPermissionLevel(accountId, documentId).block();

        assertEquals(PermissionLevel.REVIEWER, result);
    }

    @Test
    void saveAccountPermissions() {
        PermissionLevel level = PermissionLevel.CONTRIBUTOR;
        when(documentPermissionGateway.persist(any(AccountDocumentPermission.class))).thenReturn(Flux.empty());
        when(documentGateway.persist(any(DocumentAccount.class))).thenReturn(Flux.empty());
        when(documentGateway.persist(any(AccountDocumentCollaborator.class))).thenReturn(Flux.empty());

        documentPermissionService.saveAccountPermissions(accountId, documentId, level);

        verify(documentPermissionGateway).persist(eq(
                new AccountDocumentPermission().setAccountId(accountId).setDocumentId(documentId).setPermissionLevel(level)));
        verify(documentGateway).persist(eq(
                new DocumentAccount().setAccountId(accountId).setDocumentId(documentId)));
        verify(documentGateway).persist(eq(
                new AccountDocumentCollaborator().setAccountId(accountId).setDocumentId(documentId).setPermissionLevel(level)));
    }

    @Test
    void saveAccountPermissions_list() {
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        PermissionLevel level = PermissionLevel.CONTRIBUTOR;
        when(documentPermissionGateway.persist(any(AccountDocumentPermission.class))).thenReturn(Flux.empty());
        when(documentGateway.persist(any(DocumentAccount.class))).thenReturn(Flux.empty());
        when(documentGateway.persist(any(AccountDocumentCollaborator.class))).thenReturn(Flux.empty());

        documentPermissionService.saveAccountPermissions(Lists.newArrayList(accountId1, accountId2), documentId, level);

        verify(documentPermissionGateway).persist(eq(
                new AccountDocumentPermission().setAccountId(accountId1).setDocumentId(documentId).setPermissionLevel(level)));
        verify(documentGateway).persist(eq(
                new DocumentAccount().setAccountId(accountId1).setDocumentId(documentId)));
        verify(documentGateway).persist(eq(
                new AccountDocumentCollaborator().setAccountId(accountId1).setDocumentId(documentId).setPermissionLevel(level)));
        verify(documentPermissionGateway).persist(eq(
                new AccountDocumentPermission().setAccountId(accountId2).setDocumentId(documentId).setPermissionLevel(level)));
        verify(documentGateway).persist(eq(
                new DocumentAccount().setAccountId(accountId2).setDocumentId(documentId)));
        verify(documentGateway).persist(eq(
                new AccountDocumentCollaborator().setAccountId(accountId2).setDocumentId(documentId).setPermissionLevel(level)));
    }

    @Test
    void deleteAccountPermissions() {
        UUID accountId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        when(documentPermissionGateway.delete(any(AccountDocumentPermission.class))).thenReturn(Flux.empty());
        when(documentGateway.delete(any(DocumentAccount.class))).thenReturn(Flux.empty());
        when(documentGateway.delete(any(AccountDocumentCollaborator.class))).thenReturn(Flux.empty());

        documentPermissionService.deleteAccountPermissions(accountId, documentId).blockLast();

        verify(documentPermissionGateway).delete(eq(
                new AccountDocumentPermission().setAccountId(accountId).setDocumentId(documentId)));
        verify(documentGateway).delete(eq(
                new DocumentAccount().setAccountId(accountId).setDocumentId(documentId)));
        verify(documentGateway).delete(eq(
                new AccountDocumentCollaborator().setAccountId(accountId).setDocumentId(documentId)));
    }

    @Test
    void saveTeamPermissions() {
        PermissionLevel level = PermissionLevel.CONTRIBUTOR;
        when(documentPermissionGateway.persist(any(TeamDocumentPermission.class))).thenReturn(Flux.empty());
        when(documentGateway.persist(any(TeamDocumentCollaborator.class))).thenReturn(Flux.empty());

        documentPermissionService.saveTeamPermissions(teamId, documentId, level);

        verify(documentPermissionGateway).persist(eq(new TeamDocumentPermission().setTeamId(teamId).setDocumentId(documentId)
                .setPermissionLevel(level)));
        verify(documentGateway).persist(eq(new TeamDocumentCollaborator().setTeamId(teamId).setDocumentId(documentId)
                .setPermissionLevel(level)));
    }

    @Test
    void deleteTeamPermissions() {

        when(documentPermissionGateway.delete(any(TeamDocumentPermission.class))).thenReturn(Flux.empty());
        when(documentGateway.delete(any(DocumentTeam.class))).thenReturn(Flux.empty());
        when(documentGateway.delete(any(TeamDocumentCollaborator.class))).thenReturn(Flux.empty());

        documentPermissionService.deleteTeamPermissions(teamId, documentId).blockLast();

        verify(documentPermissionGateway).delete(eq(new TeamDocumentPermission().setTeamId(teamId).setDocumentId(documentId)));
        verify(documentGateway).delete(eq(new DocumentTeam().setTeamId(teamId).setDocumentId(documentId)));
        verify(documentGateway).delete(eq(new TeamDocumentCollaborator().setTeamId(teamId).setDocumentId(documentId)));
    }

    @Test
    void saveTeamPermissions_list() {
        PermissionLevel level = PermissionLevel.CONTRIBUTOR;
        when(documentPermissionGateway.persist(any(TeamDocumentPermission.class))).thenReturn(Flux.empty());
        when(documentGateway.persist(any(TeamDocumentCollaborator.class))).thenReturn(Flux.empty());
        when(documentGateway.persist(any(DocumentTeam.class))).thenReturn(Flux.empty());

        documentPermissionService.saveTeamPermissions(Lists.newArrayList(teamId1, teamId2), documentId, level);

        verify(documentPermissionGateway).persist(eq(new TeamDocumentPermission().setTeamId(teamId1).setDocumentId(documentId)
                .setPermissionLevel(level)));
        verify(documentGateway).persist(eq(new TeamDocumentCollaborator().setTeamId(teamId1).setDocumentId(documentId)
                .setPermissionLevel(level)));
        verify(documentPermissionGateway).persist(eq(new TeamDocumentPermission().setTeamId(teamId2).setDocumentId(documentId)
                .setPermissionLevel(level)));
        verify(documentGateway).persist(eq(new TeamDocumentCollaborator().setTeamId(teamId2).setDocumentId(documentId)
                .setPermissionLevel(level)));
    }

}
