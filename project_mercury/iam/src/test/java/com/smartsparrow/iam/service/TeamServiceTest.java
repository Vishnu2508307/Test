package com.smartsparrow.iam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.data.permission.team.TeamPermission;
import com.smartsparrow.iam.data.permission.team.TeamPermissionGateway;
import com.smartsparrow.iam.data.team.AccountTeamCollaborator;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.data.team.TeamBySubscription;
import com.smartsparrow.iam.data.team.TeamGateway;
import com.smartsparrow.iam.data.team.TeamSummary;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.iam.payload.TeamPayload;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class TeamServiceTest {

    @InjectMocks
    TeamService teamService;

    @Mock
    TeamGateway teamGateway;
    @Mock
    TeamPermissionGateway teamPermissionGateway;
    @Mock
    AccountService accountService;

    private final static UUID teamId = UUID.randomUUID();
    private final static UUID teamId1 = UUID.randomUUID();
    private final static String name = "awesome-team";
    private final static String description = "awesome-team";
    private final static UUID subscriptionId = UUID.randomUUID();
    private final static String thumbnail = "awesome-team";
    private final static UUID accountId = UUID.randomUUID();
    private final static UUID accountId1 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createTeam_CreatorIdNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.createTeam(null, name, description, thumbnail, subscriptionId));
        assertEquals("creatorId is required", throwable.getMessage());
    }

    @Test
    void createTeam_NameNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.createTeam(accountId, null, null, null, null));
        assertEquals("name is required", throwable.getMessage());
    }

    @Test
    void createTeam_SubscriptionNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.createTeam(accountId, name, description, null, null));
        assertEquals("subscriptionId is required", throwable.getMessage());
    }

    @Test
    void createTeam_success() {
        when(teamGateway.persist(any(TeamSummary.class))).thenReturn(Mono.empty());
        when(teamGateway.persist(any(TeamBySubscription.class))).thenReturn(Mono.empty());
        when(teamPermissionGateway.persist(any())).thenReturn(Flux.empty());
        when(teamGateway.persist(any(AccountTeamCollaborator.class))).thenReturn(Mono.empty());
        when(teamGateway.persist(any(TeamAccount.class))).thenReturn(Mono.empty());

        TeamSummary expected = teamService.createTeam(accountId, name, description, thumbnail, subscriptionId).block();

        assertNotNull(expected);
        assertNotNull(expected.getId());
        assertEquals(expected.getName(), name);
        assertEquals(expected.getSubscriptionId(), subscriptionId);
        assertEquals(expected.getDescription(), description);
        assertEquals(expected.getThumbnail(), thumbnail);
    }

    @Test
    void createTeam_successOnlyRequiredFields() {
        when(teamGateway.persist(any(TeamSummary.class))).thenReturn(Mono.empty());
        when(teamGateway.persist(any(TeamBySubscription.class))).thenReturn(Mono.empty());
        when(teamPermissionGateway.persist(any())).thenReturn(Flux.empty());
        when(teamGateway.persist(any(AccountTeamCollaborator.class))).thenReturn(Mono.empty());
        when(teamGateway.persist(any(TeamAccount.class))).thenReturn(Mono.empty());

        TeamSummary expected = teamService.createTeam(accountId, name, null, null, subscriptionId).block();

        assertNotNull(expected);
        assertNotNull(expected.getId());
        assertEquals(expected.getName(), name);
        assertEquals(expected.getSubscriptionId(), subscriptionId);
        assertNull(expected.getDescription());
        assertNull(expected.getThumbnail());
    }

    @Test
    void createTeam_savePermissions() {
        when(teamGateway.persist(any(TeamSummary.class))).thenReturn(Mono.empty());
        when(teamGateway.persist(any(TeamBySubscription.class))).thenReturn(Mono.empty());
        when(teamPermissionGateway.persist(any())).thenReturn(Flux.empty());
        when(teamGateway.persist(any(AccountTeamCollaborator.class))).thenReturn(Mono.empty());
        when(teamGateway.persist(any(TeamAccount.class))).thenReturn(Mono.empty());

        teamService.createTeam(accountId, name, null, null, subscriptionId).block();

        ArgumentCaptor<TeamPermission> permissionCaptor = ArgumentCaptor.forClass(TeamPermission.class);
        verify(teamPermissionGateway).persist(permissionCaptor.capture());
        assertNotNull(permissionCaptor.getValue().getTeamId());
        assertEquals(accountId, permissionCaptor.getValue().getAccountId());
        assertEquals(PermissionLevel.OWNER, permissionCaptor.getValue().getPermissionLevel());

        ArgumentCaptor<AccountTeamCollaborator> collaboratorCaptor = ArgumentCaptor.forClass(AccountTeamCollaborator.class);
        verify(teamGateway).persist(collaboratorCaptor.capture());
        assertNotNull(collaboratorCaptor.getValue().getTeamId());
        assertEquals(accountId, collaboratorCaptor.getValue().getAccountId());
        assertEquals(PermissionLevel.OWNER, collaboratorCaptor.getValue().getPermissionLevel());

        ArgumentCaptor<TeamAccount> teamByAccountCaptor = ArgumentCaptor.forClass(TeamAccount.class);
        verify(teamGateway).persist(teamByAccountCaptor.capture());
        assertNotNull(teamByAccountCaptor.getValue().getTeamId());
        assertEquals(accountId, teamByAccountCaptor.getValue().getAccountId());
    }

    @Test
    void updateTeam_TeamNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.updateTeam(null, null, null, null));
        assertEquals("teamId is required", throwable.getMessage());
    }

    @Test
    void updateTeam_success() {
        when(teamGateway.persist(any(TeamSummary.class))).thenReturn(Mono.empty());

        StepVerifier.create(teamService.updateTeam(teamId, name, description, thumbnail)).verifyComplete();

        ArgumentCaptor<TeamSummary> teamCaptor = ArgumentCaptor.forClass(TeamSummary.class);
        verify(teamGateway).persist(teamCaptor.capture());
        assertEquals(teamId, teamCaptor.getValue().getId());
        assertEquals(name, teamCaptor.getValue().getName());
        assertNull(teamCaptor.getValue().getSubscriptionId());
        assertEquals(description, teamCaptor.getValue().getDescription());
        assertEquals(thumbnail, teamCaptor.getValue().getThumbnail());
    }

    @Test
    void updateTeam_nullFields() {
        when(teamGateway.persist(any(TeamSummary.class))).thenReturn(Mono.empty());

        StepVerifier.create(teamService.updateTeam(teamId, null, null, null)).verifyComplete();

        ArgumentCaptor<TeamSummary> teamCaptor = ArgumentCaptor.forClass(TeamSummary.class);
        verify(teamGateway).persist(teamCaptor.capture());
        assertEquals(teamId, teamCaptor.getValue().getId());
        assertNull(teamCaptor.getValue().getName());
        assertNull(teamCaptor.getValue().getSubscriptionId());
        assertNull(teamCaptor.getValue().getDescription());
        assertNull(teamCaptor.getValue().getThumbnail());
    }

    @Test
    void deleteTeam_TeamIdNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.deleteTeam(null));
        assertEquals("teamId is required", throwable.getMessage());
    }

    @Test
    void deleteTeam_success() {
        when(teamGateway.delete(any(TeamSummary.class))).thenReturn(Flux.empty());
        teamService.deleteTeam(teamId);
        verify(teamGateway).delete(eq(new TeamSummary().setId(teamId)));
    }

    @Test
    void deleteTeamAccount_TeamIdNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.deleteTeamAccount(null, accountId));
        assertEquals("teamId is required", throwable.getMessage());
    }

    @Test
    void deleteTeamAccount_AccountIdNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.deleteTeamAccount(teamId, null));
        assertEquals("accountId is required", throwable.getMessage());
    }

    @Test
    void deleteTeamAccount_success() {
        when(teamPermissionGateway.delete(any(TeamPermission.class))).thenReturn(Flux.empty());
        when(teamGateway.delete(any(TeamAccount.class))).thenReturn(Flux.empty());
        when(teamGateway.delete(any(AccountTeamCollaborator.class))).thenReturn(Flux.empty());

        teamService.deleteTeamAccount(teamId, accountId);

        verify(teamPermissionGateway).delete(eq(new TeamPermission()
                .setAccountId(accountId)
                .setTeamId(teamId)));
        verify(teamGateway).delete(eq(new TeamAccount()
                .setTeamId(teamId)
                .setAccountId(accountId)));
        verify(teamGateway).delete(eq(new AccountTeamCollaborator()
                .setTeamId(teamId)
                .setAccountId(accountId)));
    }

    @Test
    void deleteTeamSubscription_TeamIdNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.deleteTeamSubscription(null, subscriptionId));
        assertEquals("teamId is required", throwable.getMessage());
    }

    @Test
    void deleteTeamSubscription_SubscriptionNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.deleteTeamSubscription(teamId, null));
        assertEquals("subscriptionId is required", throwable.getMessage());
    }

    @Test
    void deleteTeamSubscription_success() {
        when(teamGateway.delete(any(TeamBySubscription.class))).thenReturn(Flux.empty());

        teamService.deleteTeamSubscription(teamId, subscriptionId);

        verify(teamGateway).delete(eq(new TeamBySubscription()
                .setTeamId(teamId)
                .setSubscriptionId(subscriptionId)));
    }

    @Test
    void findTeam_NullTeamId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.findTeam(null).block());
        assertEquals("teamId is required", throwable.getMessage());
    }

    @Test
    void findTeam_Success() {
        TeamSummary expected = new TeamSummary()
                .setId(teamId)
                .setDescription(description)
                .setName(name)
                .setSubscriptionId(subscriptionId)
                .setThumbnail(thumbnail);

        when(teamGateway.findTeam(any(UUID.class)))
                .thenReturn(Mono.just(expected));

        TeamSummary actual = teamService.findTeam(teamId).block();

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void findAllTeamsBySubscription_nullSubscriptionId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.findAllTeamsBySubscription(null).blockLast());
        assertEquals("subscriptionId is required", throwable.getMessage());
    }

    @Test
    void findAllTeamsBySubscription_success() {
        TeamBySubscription team1 = new TeamBySubscription().setTeamId(teamId).setSubscriptionId(subscriptionId);
        TeamBySubscription team2 = new TeamBySubscription().setTeamId(teamId1).setSubscriptionId(subscriptionId);
        when(teamGateway.findTeamsForSubscription(any(UUID.class))).thenReturn(Flux.just(team1, team2));
        TeamBySubscription actual = teamService.findAllTeamsBySubscription(subscriptionId).blockLast();
        assertNotNull(actual);
        assertEquals(actual, team2);
    }

    @Test
    void findAllCollaboratorsForATeam_nullTeam() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.findAllCollaboratorsForATeam(null).blockLast());
        assertEquals("teamId is required", throwable.getMessage());
    }

    @Test
    void findAllCollaboratorsForATeam_success() {
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();
        AccountTeamCollaborator collaborator1 = new AccountTeamCollaborator().setTeamId(teamId).setAccountId(accountId1);
        AccountTeamCollaborator collaborator2 = new AccountTeamCollaborator().setTeamId(teamId).setAccountId(accountId2);
        when(teamGateway.findAccountCollaborators(any(UUID.class))).thenReturn(Flux.just(collaborator1, collaborator2));
        AccountTeamCollaborator actual = teamService.findAllCollaboratorsForATeam(teamId).blockFirst();
        assertNotNull(actual);
        assertEquals(actual, collaborator1);
    }

    @Test
    void findTeamsForAccount_NullAccountId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.findTeamsForAccount(null).blockLast());
        assertEquals("accountId is required", throwable.getMessage());
    }

    @Test
    void findTeamsForAccount_success() {
        TeamAccount teamAccount1 = new TeamAccount().setTeamId(teamId1).setAccountId(accountId);
        TeamAccount teamAccount2 = new TeamAccount().setTeamId(teamId).setAccountId(accountId);
        when(teamGateway.findTeamsForAccount(any(UUID.class))).thenReturn(Flux.just(teamAccount1, teamAccount2));
        TeamAccount actual = teamService.findTeamsForAccount(accountId).blockFirst();
        assertNotNull(actual);
        assertEquals(actual, teamAccount1);
    }

    @Test
    void savePermission_nullAccountId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.savePermission(null, null, null).blockLast());
        assertEquals("accountId is required", throwable.getMessage());
    }

    @Test
    void savePermission_nullTeamId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.savePermission(accountId, null, null).blockLast());
        assertEquals("teamId is required", throwable.getMessage());
    }

    @Test
    void savePermission_nullPermissionLevel() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.savePermission(accountId, teamId, null).blockLast());
        assertEquals("permissionLevel is required", throwable.getMessage());
    }

    @Test
    void savePermission_success() {
        when(teamPermissionGateway.persist(any(TeamPermission.class))).thenReturn(Flux.empty());
        when(teamGateway.persist(any(AccountTeamCollaborator.class))).thenReturn(Mono.empty());
        when(teamGateway.persist(any(TeamAccount.class))).thenReturn(Mono.empty());

        teamService.savePermission(accountId, teamId, PermissionLevel.OWNER);

        verify(teamPermissionGateway, atLeastOnce()).persist(any(TeamPermission.class));
        verify(teamGateway, atLeastOnce()).persist(any(AccountTeamCollaborator.class));
        verify(teamGateway, atLeastOnce()).persist(any(TeamAccount.class));

    }

    @Test
    void deletePermission_nullAccountId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.deletePermission(null, null).blockLast());
        assertEquals("accountId is required", throwable.getMessage());
    }

    @Test
    void deletePermission_nullTeamId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.deletePermission(accountId, null).blockLast());
        assertEquals("teamId is required", throwable.getMessage());
    }

    @Test
    void deletePermission_success() {
        when(teamPermissionGateway.delete(any(TeamPermission.class))).thenReturn(Flux.empty());
        when(teamGateway.delete(any(AccountTeamCollaborator.class))).thenReturn(Flux.empty());
        when(teamGateway.delete(any(TeamAccount.class))).thenReturn(Flux.empty());

        teamService.deletePermission(accountId, teamId);

        verify(teamPermissionGateway, atLeastOnce()).delete(any(TeamPermission.class));
        verify(teamGateway, atLeastOnce()).delete(any(AccountTeamCollaborator.class));
        verify(teamGateway, atLeastOnce()).delete(any(TeamAccount.class));

    }

    @Test
    void fetchPermission_nullAccountId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.fetchPermission(null, null).block());
        assertEquals("accountId is required", throwable.getMessage());
    }

    @Test
    void fetchPermissions_nullAccountId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.fetchPermissions(null, null).blockLast());
        assertEquals("accountIds is required", throwable.getMessage());
    }

    @Test
    void fetchPermission_nullTeamId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.fetchPermission(accountId, null).block());
        assertEquals("teamId is required", throwable.getMessage());
    }

    @Test
    void fetchPermissions_nullTeamId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.fetchPermissions(Arrays.asList(accountId), null).blockLast());
        assertEquals("teamId is required", throwable.getMessage());
    }

    @Test
    void fetchPermission_success() {
        TeamPermission tp = new TeamPermission()
                .setAccountId(accountId)
                .setPermissionLevel(PermissionLevel.OWNER)
                .setTeamId(teamId);
        when(teamPermissionGateway.findPermission(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(tp));

        TeamPermission teamPermission = teamService.fetchPermission(accountId, teamId).block();

        assertNotNull(teamPermission);
        assertEquals(teamPermission, tp);
        verify(teamPermissionGateway, atLeastOnce()).findPermission(any(UUID.class), any(UUID.class));

    }

    @Test
    void fetchPermissions_success() {
        TeamPermission tp = new TeamPermission()
                .setAccountId(accountId)
                .setPermissionLevel(PermissionLevel.OWNER)
                .setTeamId(teamId);
        when(teamPermissionGateway.findPermission(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(tp));

        TeamPermission teamPermission = teamService.fetchPermissions(Arrays.asList(accountId), teamId).blockLast();

        assertNotNull(teamPermission);
        assertEquals(teamPermission, tp);
        verify(teamPermissionGateway, atLeastOnce()).findPermission(any(UUID.class), any(UUID.class));

    }


    @Test
    void getTeamPayload_NullTeamId() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.getTeamPayload(null, null));
        assertEquals("teamId is required", throwable.getMessage());
    }

    @Test
    void getTeamPayload_NullCollaboratorTeamLimit() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                teamService.getTeamPayload(UUID.randomUUID(), null));
        assertEquals("collaboratorTeamLimit is required", throwable.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getTeamPayload_success() {

        Integer limit = 2;

        AccountTeamCollaborator collaborator1 = new AccountTeamCollaborator()
                .setAccountId(accountId)
                .setPermissionLevel(PermissionLevel.OWNER)
                .setTeamId(teamId);

        AccountTeamCollaborator collaborator2 = new AccountTeamCollaborator()
                .setTeamId(teamId)
                .setPermissionLevel(PermissionLevel.OWNER)
                .setAccountId(accountId1);

        AccountSummaryPayload accountSummaryPayload = new AccountSummaryPayload()
                .setAccountId(accountId)
                .setSubscriptionId(subscriptionId);

        AccountSummaryPayload accountSummaryPayloadOne = new AccountSummaryPayload()
                .setAccountId(accountId1)
                .setSubscriptionId(subscriptionId);

        when(teamGateway.findTeam(any(UUID.class))).thenReturn(Mono.just(new TeamSummary()
                .setId(teamId)
                .setDescription(description)
                .setName(name)
                .setSubscriptionId(subscriptionId)
                .setThumbnail(thumbnail)));

        when(teamGateway.findAccountCollaborators(any(UUID.class)))
                .thenReturn(Flux.just(collaborator1, collaborator2));

        when(accountService.getAccountSummaryPayloads(eq(limit), any(Flux.class)))
                .thenReturn(Flux.just(accountSummaryPayload, accountSummaryPayloadOne));

        TeamPayload teamPayload = teamService.getTeamPayload(teamId, limit).block();

        assertNotNull(teamPayload);
        assertEquals(teamPayload.getCount(), limit);
        assertEquals(teamPayload.getName(), name);
        assertNotNull(teamPayload.getAccountSummaryPayloads());
        assertEquals(teamPayload.getAccountSummaryPayloads().get(0), accountSummaryPayload);
    }
}
