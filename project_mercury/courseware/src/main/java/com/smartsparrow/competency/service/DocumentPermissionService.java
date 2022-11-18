package com.smartsparrow.competency.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.competency.data.AccountDocumentCollaborator;
import com.smartsparrow.competency.data.DocumentAccount;
import com.smartsparrow.competency.data.DocumentGateway;
import com.smartsparrow.competency.data.DocumentTeam;
import com.smartsparrow.competency.data.TeamDocumentCollaborator;
import com.smartsparrow.iam.data.permission.competency.AccountDocumentPermission;
import com.smartsparrow.iam.data.permission.competency.DocumentPermissionGateway;
import com.smartsparrow.iam.data.permission.competency.TeamDocumentPermission;
import com.smartsparrow.iam.service.HighestPermissionLevel;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class DocumentPermissionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentPermissionService.class);

    private final DocumentPermissionGateway documentPermissionGateway;
    private final DocumentGateway documentGateway;
    private final TeamService teamService;

    @Inject
    public DocumentPermissionService(final DocumentPermissionGateway documentPermissionGateway,
                                     final DocumentGateway documentGateway,
                                     final TeamService teamService) {
        this.documentPermissionGateway = documentPermissionGateway;
        this.documentGateway = documentGateway;
        this.teamService = teamService;
    }

    /**
     * Fetch account permission for a document
     *
     * @param accountId  the account id to fetch the permission for
     * @param documentId the document entity the permission relates to
     * @return a {@link Mono} of {@link PermissionLevel}
     */
    public Mono<PermissionLevel> fetchAccountPermission(final UUID accountId, final UUID documentId) {
        return documentPermissionGateway.findAccountPermission(accountId, documentId)
                .map(AccountDocumentPermission::getPermissionLevel);
    }

    /**
     * Finds the highest permission level for the account over the document.
     * The methods finds both account and team specific permissions over the document,
     * then the highest permission level is returned.
     *
     * @param accountId  the account id to search the permissions for
     * @param documentId the document id the account should have permission over
     * @return a mono of permission level
     */
    public Mono<PermissionLevel> findHighestPermissionLevel(final UUID accountId, final UUID documentId) {
        return teamService.findTeamsForAccount(accountId)
                .flatMap(teamAccount -> fetchTeamPermission(teamAccount.getTeamId(), documentId))
                .mergeWith(fetchAccountPermission(accountId, documentId))
                .reduce(new HighestPermissionLevel());
    }

    /**
     * Fetch team permission over a document
     *
     * @param teamId     the team id to fetch the permission for
     * @param documentId the document entity the permission relates to
     * @return a {@link Mono} of {@link PermissionLevel}
     */
    public Mono<PermissionLevel> fetchTeamPermission(final UUID teamId, final UUID documentId) {
        return documentPermissionGateway.findTeamPermission(teamId, documentId)
                .map(TeamDocumentPermission::getPermissionLevel);
    }

    /**
     * Save account permissions for document
     *
     * @param accountId       account id to be granted with permission
     * @param documentId      document id
     * @param permissionLevel permission level
     */
    public Flux<Void> saveAccountPermissions(final UUID accountId, final UUID documentId, final PermissionLevel permissionLevel) {
        checkNotNull(accountId, "accountId is required");
        checkNotNull(documentId, "documentId is required");
        checkNotNull(permissionLevel, "permissionLevel is required");

        return Flux.merge(
                documentPermissionGateway.persist(new AccountDocumentPermission()
                        .setAccountId(accountId)
                        .setDocumentId(documentId)
                        .setPermissionLevel(permissionLevel)),
                documentGateway.persist(new DocumentAccount()
                        .setAccountId(accountId)
                        .setDocumentId(documentId)),
                documentGateway.persist(new AccountDocumentCollaborator()
                        .setAccountId(accountId)
                        .setDocumentId(documentId)
                        .setPermissionLevel(permissionLevel)));
    }

    /**
     * Save multiple account permissions
     *
     * @param accountIds      the list of account ids to be granted with permission
     * @param documentId      document id
     * @param permissionLevel the permission level
     */
    public Flux<Void> saveAccountPermissions(final List<UUID> accountIds, final UUID documentId, final PermissionLevel permissionLevel) {
        checkNotNull(accountIds, "accountIds is required");
        return accountIds.stream().reduce(Flux.empty(),
                (flux, accountId) -> flux.mergeWith(saveAccountPermissions(accountId, documentId, permissionLevel)),
                Flux::mergeWith);
    }

    /**
     * Save team permissions for document
     *
     * @param teamId          team id to be granted with permission
     * @param documentId      document id
     * @param permissionLevel permission level
     */
    public Flux<Void> saveTeamPermissions(final UUID teamId, final UUID documentId, final PermissionLevel permissionLevel) {
        checkNotNull(teamId, "teamId is required");
        checkNotNull(documentId, "documentId is required");
        checkNotNull(permissionLevel, "permissionLevel is required");

        return Flux.merge(
                documentPermissionGateway.persist(new TeamDocumentPermission()
                        .setTeamId(teamId)
                        .setDocumentId(documentId)
                        .setPermissionLevel(permissionLevel)),
                documentGateway.persist(new DocumentTeam()
                        .setDocumentId(documentId)
                        .setTeamId(teamId)),
                documentGateway.persist(new TeamDocumentCollaborator()
                        .setTeamId(teamId)
                        .setDocumentId(documentId)
                        .setPermissionLevel(permissionLevel)));
    }

    /**
     * Save multiple team permissions
     *
     * @param teamIds         the list of team ids to be granted with permission
     * @param documentId      document id
     * @param permissionLevel the permission level
     */
    public Flux<Void> saveTeamPermissions(final List<UUID> teamIds, final UUID documentId, final PermissionLevel permissionLevel) {
        checkNotNull(teamIds, "teamIds is required");
        return teamIds.stream().reduce(Flux.empty(),
                (flux, teamId) -> flux.mergeWith(saveTeamPermissions(teamId, documentId, permissionLevel)),
                Flux::mergeWith);
    }

    /**
     * Delete the account permissions over a document entity.
     *
     * @param accountId  the account to delete the permission for
     * @param documentId the document the permission relates to
     */
    public Flux<Void> deleteAccountPermissions(final UUID accountId, final UUID documentId) {
        checkNotNull(accountId, "accountId is required");
        checkNotNull(documentId, "documentId is required");

        return Flux.merge(
                documentPermissionGateway.delete(new AccountDocumentPermission()
                        .setAccountId(accountId)
                        .setDocumentId(documentId)),
                documentGateway.delete(new DocumentAccount()
                        .setAccountId(accountId)
                        .setDocumentId(documentId)),
                documentGateway.delete(new AccountDocumentCollaborator()
                        .setAccountId(accountId)
                        .setDocumentId(documentId))
        ).doOnError(throwable -> log.error(
                String.format("error while deleting permissions for account %s over document %s",
                        accountId, documentId), throwable));
    }

    /**
     * Delete multiple account permissions
     *
     * @param accountIds the list of account ids to be revoked
     * @param documentId document id
     */
    public Flux<Void> deleteAccountPermissions(final List<UUID> accountIds, final UUID documentId) {
        checkNotNull(accountIds, "accountIds is required");
        return accountIds
                .stream()
                .reduce(Flux.empty(),
                        (flux, accountId) -> flux.mergeWith(deleteAccountPermissions(accountId, documentId)),
                        Flux::mergeWith);
    }

    /**
     * Delete the team permissions over a document entity.
     *
     * @param teamId     the team to delete the permission for
     * @param documentId the document the permission relates to
     */
    public Flux<Void> deleteTeamPermissions(final UUID teamId, final UUID documentId) {
        checkNotNull(teamId, "teamId is required");
        checkNotNull(documentId, "documentId is required");

        return Flux.merge(
                documentPermissionGateway.delete(new TeamDocumentPermission()
                        .setTeamId(teamId)
                        .setDocumentId(documentId)),
                documentGateway.delete(new DocumentTeam()
                        .setDocumentId(documentId)
                        .setTeamId(teamId)),
                documentGateway.delete(new TeamDocumentCollaborator()
                        .setTeamId(teamId)
                        .setDocumentId(documentId))
        ).doOnError(throwable -> log.error(
                String.format("error while deleting permissions for team %s over document %s",
                        teamId, documentId), throwable));
    }

    /**
     * Delete multiple account permissions
     *
     * @param teamIds    the list of team ids to be revoked
     * @param documentId document id
     */
    public Flux<Void> deleteTeamPermissions(final List<UUID> teamIds, final UUID documentId) {
        checkNotNull(teamIds, "teamIds is required");
        return teamIds
                .stream()
                .reduce(Flux.empty(),
                        (flux, teamId) -> flux.mergeWith(deleteTeamPermissions(teamId, documentId)),
                        Flux::mergeWith);
    }
}
