package com.smartsparrow.iam.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.collaborator.AccountCollaborator;
import com.smartsparrow.iam.collaborator.CollaboratorResult;
import com.smartsparrow.iam.collaborator.Collaborators;
import com.smartsparrow.iam.collaborator.TeamCollaborator;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.payload.AccountCollaboratorPayload;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.payload.TeamCollaboratorPayload;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CollaboratorServiceTest {

    @InjectMocks
    private CollaboratorService collaboratorService;

    @Mock
    private AccountService accountService;

    @Mock
    private TeamService teamService;

    private Flux<? extends AccountCollaborator> accounts;
    private Flux<? extends TeamCollaborator> teams;
    private final UUID accountId = UUID.randomUUID();
    private final UUID teamId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        accounts = Flux.just(new AccountCollaborator() {
            @Override
            public UUID getAccountId() {
                return accountId;
            }

            @Override
            public PermissionLevel getPermissionLevel() {
                return PermissionLevel.CONTRIBUTOR;
            }
        });

        teams = Flux.just(new TeamCollaborator() {
            @Override
            public UUID getTeamId() {
                return teamId;
            }

            @Override
            public PermissionLevel getPermissionLevel() {
                return PermissionLevel.REVIEWER;
            }
        });

        when(accountService.getCollaboratorPayload(accountId, PermissionLevel.CONTRIBUTOR)).thenReturn(Mono.just(
                AccountCollaboratorPayload.from(new AccountPayload(), PermissionLevel.CONTRIBUTOR)
        ));

        when(teamService.getTeamCollaboratorPayload(teamId, PermissionLevel.REVIEWER)).thenReturn(Mono.just(
                TeamCollaboratorPayload.from(new TeamSummary(), PermissionLevel.REVIEWER)
        ));

    }

    @Test
    void getCollaborators_noLimit() {
        CollaboratorResult result = collaboratorService.getCollaborators(teams, accounts)
                .block();

        assertNotNull(result);
        assertEquals(Long.valueOf(2), result.getTotal());

        Collaborators collaborators = result.getCollaborators();

        assertNotNull(collaborators);
        assertEquals(1, collaborators.getAccounts().size());
        assertEquals(1, collaborators.getTeams().size());
    }

    @Test
    void getCollaborators_withLimit() {
        Integer limit = 1;
        CollaboratorResult result = collaboratorService.getCollaborators(teams, accounts, limit)
                .block();

        assertNotNull(result);
        assertEquals(Long.valueOf(2), result.getTotal());

        Collaborators collaborators = result.getCollaborators();

        assertNotNull(collaborators);
        assertNull(collaborators.getAccounts());
        assertEquals(1, collaborators.getTeams().size());
    }

    @Test
    void getCollaborators_noAccountCollaborators() {
        CollaboratorResult result = collaboratorService.getCollaborators(teams, Flux.empty())
                .block();

        assertNotNull(result);
        assertEquals(Long.valueOf(1), result.getTotal());

        Collaborators collaborators = result.getCollaborators();

        assertNotNull(collaborators);
        assertNull(collaborators.getAccounts());
        assertEquals(1, collaborators.getTeams().size());
    }

    @Test
    void getCollaborators_noTeamCollaborators() {
        CollaboratorResult result = collaboratorService.getCollaborators(Flux.empty(), accounts, 1)
                .block();

        assertNotNull(result);
        assertEquals(Long.valueOf(1), result.getTotal());

        Collaborators collaborators = result.getCollaborators();

        assertNotNull(collaborators);
        assertEquals(1, collaborators.getAccounts().size());
        assertNull(collaborators.getTeams());
    }

    @Test
    void getCollaborators_noCollaborators() {
        CollaboratorResult result = collaboratorService.getCollaborators(Flux.empty(), Flux.empty())
                .block();

        assertNotNull(result);
        assertEquals(Long.valueOf(0), result.getTotal());

        Collaborators collaborators = result.getCollaborators();

        assertNotNull(collaborators);
        assertNull(collaborators.getAccounts());
        assertNull(collaborators.getTeams());
    }

}
