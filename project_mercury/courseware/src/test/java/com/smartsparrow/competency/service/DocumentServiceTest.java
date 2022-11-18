package com.smartsparrow.competency.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.data.AccountDocumentCollaborator;
import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.data.DocumentGateway;
import com.smartsparrow.competency.data.TeamDocumentCollaborator;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DocumentServiceTest {

    @InjectMocks
    DocumentService documentService;

    @Mock
    DocumentGateway documentGateway;

    @Mock
    DocumentPermissionService documentPermissionService;

    @Mock
    TeamService teamService;

    UUID documentId = UUID.randomUUID();
    UUID documentId1 = UUID.randomUUID();
    UUID documentId2 = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID teamId = UUID.randomUUID();

    private static final UUID workspaceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void fetchAccountCollaborators_Empty() {

        when(documentGateway.findAccountCollaborators(any(UUID.class))).thenReturn(Flux.empty());
        List<AccountDocumentCollaborator> block = documentService
                .fetchAccountCollaborators(documentId)
                .collectList()
                .block();
        assertNotNull(block);
        assertEquals(0, block.size());
    }

    @Test
    void fetchAccountCollaborators_Valid() {
        AccountDocumentCollaborator accountDocumentCollaborator = new AccountDocumentCollaborator()
                .setAccountId(accountId)
                .setDocumentId(documentId)
                .setPermissionLevel(PermissionLevel.OWNER);
        when(documentGateway.findAccountCollaborators(any(UUID.class)))
                .thenReturn(Flux.just(accountDocumentCollaborator));

        AccountDocumentCollaborator block = documentService
                .fetchAccountCollaborators(documentId)
                .blockLast();
        assertNotNull(block);
        assertEquals(accountDocumentCollaborator, block);
    }

    @Test
    void fetchTeamCollaborators_Empty() {

        when(documentGateway.findTeamCollaborators(any(UUID.class))).thenReturn(Flux.empty());
        List<TeamDocumentCollaborator> block = documentService
                .fetchTeamCollaborators(documentId)
                .collectList()
                .block();
        assertNotNull(block);
        assertEquals(0, block.size());
    }

    @Test
    void fetchTeamCollaborators_Valid() {
        TeamDocumentCollaborator teamDocumentCollaborator = new TeamDocumentCollaborator()
                .setTeamId(teamId)
                .setDocumentId(documentId)
                .setPermissionLevel(PermissionLevel.OWNER);
        when(documentGateway.findTeamCollaborators(any(UUID.class)))
                .thenReturn(Flux.just(teamDocumentCollaborator));

        TeamDocumentCollaborator block = documentService
                .fetchTeamCollaborators(documentId)
                .blockLast();
        assertNotNull(block);
        assertEquals(teamDocumentCollaborator, block);
    }

    @Test
    void fetchDocuments_Empty() {
        when(documentGateway.findDocumentsByAccount(any(UUID.class)))
                .thenReturn(Flux.empty());
        when(teamService.findTeamsForAccount(any(UUID.class))).thenReturn(Flux.empty());

        List<Document> block = documentService.fetchDocuments(accountId).collectList().block();
        assertNotNull(block);
        assertEquals(0, block.size());
    }

    @Test
    void fetchDocuments_EmptyTeam() {
        when(documentGateway.findDocumentsByAccount(any(UUID.class)))
                .thenReturn(Flux.just(documentId));
        when(teamService.findTeamsForAccount(any(UUID.class))).thenReturn(Flux.empty());
        when(documentGateway.findById(any(UUID.class))).thenReturn(Mono.just(new Document()
                .setId(documentId)));

        Document block = documentService.fetchDocuments(accountId).blockLast();
        assertNotNull(block);
        assertEquals(documentId, block.getId());
    }

    @Test
    void fetchDocuments_BothTeamAndAccount() {
        List<Document> documentList = Arrays.asList(new Document()
                        .setId(documentId), new Document()
                        .setId(documentId1),
                new Document()
                        .setId(documentId2));

        when(documentGateway.findDocumentsByAccount(any(UUID.class)))
                .thenReturn(Flux.just(documentId, documentId1));

        when(teamService.findTeamsForAccount(any(UUID.class))).thenReturn(Flux.just(new TeamAccount()
                .setTeamId(teamId).setAccountId(accountId)));

        when(documentGateway.findById(documentId)).thenReturn(Mono.just(new Document()
                .setId(documentId)));

        when(documentGateway.findById(documentId1)).thenReturn(Mono.just(new Document()
                .setId(documentId1)));

        when(documentGateway.findById(documentId2)).thenReturn(Mono.just(new Document()
                .setId(documentId2)));

        when(documentGateway.findDocumentsByTeam(any(UUID.class))).thenReturn(Flux.just(documentId, documentId2));

        List<Document> block = documentService.fetchDocuments(accountId).collectList().block();
        assertNotNull(block);
        assertEquals(3, block.size());

        assertTrue(CollectionUtils.isEqualCollection(new HashSet<>(documentList), new HashSet<>(block)));
    }

    @Test
    void create() {
        when(documentGateway.persist(any(Document.class))).thenReturn(Flux.empty());
        when(documentPermissionService.saveAccountPermissions(any(UUID.class), any(UUID.class), any(PermissionLevel.class))).thenReturn(Flux.empty());

        Document result = documentService.create("Chemistry Knowledge Map", workspaceId, accountId).block();

        assertNotNull(result);
        assertEquals("Chemistry Knowledge Map", result.getTitle());
        assertEquals(workspaceId, result.getWorkspaceId());
        assertEquals(accountId, result.getCreatedBy());
        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());
        assertNull(result.getModifiedAt());
        assertNull(result.getModifiedBy());
        verify(documentGateway).persist(result);
        verify(documentPermissionService).saveAccountPermissions(accountId, result.getId(), PermissionLevel.OWNER);
    }

    @Test
    void delete() {
        when(documentGateway.delete(any(Document.class))).thenReturn(Flux.empty());

        Document deletedDocument = new Document().setId(documentId).setWorkspaceId(workspaceId);

        Document result = documentService.delete(deletedDocument).block();

        assertNotNull(result);
        verify(documentGateway).delete(deletedDocument);
    }

    @Test
    void update() {
        when(documentGateway.update(any(Document.class))).thenReturn(Flux.empty());

        Document updatedDocument = new Document().setId(documentId).setWorkspaceId(workspaceId).setTitle("update");

        Document result = documentService.update(updatedDocument).block();

        assertNotNull(result);
        verify(documentGateway, atMostOnce()).update(updatedDocument);
    }

    @Test
    void deleteAccountCollaborators() {
        AccountDocumentCollaborator documentCollaborator1 = new AccountDocumentCollaborator()
                                                                .setAccountId(UUID.randomUUID())
                                                                .setDocumentId(documentId);
        AccountDocumentCollaborator documentCollaborator2 = new AccountDocumentCollaborator()
                                                                .setAccountId(UUID.randomUUID())
                                                                .setDocumentId(documentId);

        when(documentGateway.findAccountCollaborators(documentId))
                .thenReturn(Flux.just(documentCollaborator1, documentCollaborator2));

        when(documentPermissionService.deleteAccountPermissions(any(UUID.class), eq(documentId)))
                .thenReturn(Flux.empty());

        documentService.deleteAccountCollaborators(documentId).blockFirst();

        verify(documentPermissionService, times(2))
                    .deleteAccountPermissions(any(UUID.class), eq(documentId));
    }

    @Test
    void deleteTeamCollaborators() {
        TeamDocumentCollaborator team1 = new TeamDocumentCollaborator()
                .setTeamId(UUID.randomUUID())
                .setDocumentId(documentId);

        when(documentGateway.findTeamCollaborators(documentId))
                .thenReturn(Flux.just(team1));

        when(documentPermissionService.deleteTeamPermissions(any(UUID.class), eq(documentId)))
                .thenReturn(Flux.empty());

        documentService.deleteTeamCollaborators(documentId).blockFirst();

        verify(documentPermissionService, atMostOnce())
                    .deleteTeamPermissions(any(UUID.class), eq(documentId));
    }
}
