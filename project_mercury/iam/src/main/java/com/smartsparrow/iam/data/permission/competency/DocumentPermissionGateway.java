package com.smartsparrow.iam.data.permission.competency;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DocumentPermissionGateway {

    private static final Logger log = LoggerFactory.getLogger(DocumentPermissionGateway.class);

    private final Session session;

    private final DocumentAccountPermissionMaterializer documentAccountPermissionMaterializer;
    private final DocumentAccountPermissionMutator documentAccountPermissionMutator;
    private final DocumentTeamPermissionMaterializer documentTeamPermissionMaterializer;
    private final DocumentTeamPermissionMutator documentTeamPermissionMutator;

    @Inject
    public DocumentPermissionGateway(Session session,
                                     DocumentAccountPermissionMaterializer documentAccountPermissionMaterializer,
                                     DocumentAccountPermissionMutator documentAccountPermissionMutator,
                                     DocumentTeamPermissionMaterializer documentTeamPermissionMaterializer,
                                     DocumentTeamPermissionMutator documentTeamPermissionMutator) {
        this.session = session;

        this.documentAccountPermissionMaterializer = documentAccountPermissionMaterializer;
        this.documentAccountPermissionMutator = documentAccountPermissionMutator;
        this.documentTeamPermissionMaterializer = documentTeamPermissionMaterializer;
        this.documentTeamPermissionMutator = documentTeamPermissionMutator;
    }

    /**
     * Save account document permission
     *
     * @param accountDocumentPermission the permission to save
     * @return Flux<Void> {@link Flux<Void>}
     */
    public Flux<Void> persist(AccountDocumentPermission accountDocumentPermission) {
        Flux<? extends Statement> stmt = Mutators.upsert(documentAccountPermissionMutator, accountDocumentPermission);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving document permission on iam_global for account %s",
                            accountDocumentPermission), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save a team document permission
     *
     * @param teamDocumentPermission the permission to save
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(TeamDocumentPermission teamDocumentPermission) {
        Flux<? extends Statement> stmt = Mutators.upsert(documentTeamPermissionMutator, teamDocumentPermission);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving document permission on iam_global for team %s",
                            teamDocumentPermission), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete an account document permission. The {@link AccountDocumentPermission#getPermissionLevel()} supplied by the
     * argument can be <code>null</code>. This only requires accountId and documentId to be defined when deleting.
     *
     * @param accountDocumentPermission the permission to delete
     */
    public Flux<Void> delete(AccountDocumentPermission accountDocumentPermission) {
        Flux<? extends Statement> stmt = Mutators.delete(documentAccountPermissionMutator, accountDocumentPermission);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting document permission on iam_global for account %s",
                            accountDocumentPermission), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete a team document permission. The {@link TeamDocumentPermission#getPermissionLevel()} supplied by the
     * argument can be <code>null</code>. This only requires teamId and documentId to be defined when deleting.
     *
     * @param teamDocumentPermission the permission to delete
     */
    public Flux<Void> delete(TeamDocumentPermission teamDocumentPermission) {
        Flux<? extends Statement> stmt = Mutators.delete(documentTeamPermissionMutator, teamDocumentPermission);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting document permission on iam_global for team %s",
                            teamDocumentPermission), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find the permission level for an account over a given document
     *
     * @param accountId  the account to fetch the permission for
     * @param documentId the document entity the permission refers to
     * @return a {@link Mono} of {@link AccountDocumentPermission}
     */
    public Mono<AccountDocumentPermission> findAccountPermission(UUID accountId, UUID documentId) {
        return ResultSets.query(session, documentAccountPermissionMaterializer.fetchPermission(accountId, documentId))
                .flatMapIterable(row -> row)
                .map(documentAccountPermissionMaterializer::fromRow)
                .singleOrEmpty()
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching permission for account %s and document %s",
                            accountId, documentId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find the permission level for a team over a given document
     *
     * @param teamId     the team to fetch the permission for
     * @param documentId the document entity the permission refers to
     * @return a {@link Mono} of {@link TeamDocumentPermission}
     */
    public Mono<TeamDocumentPermission> findTeamPermission(UUID teamId, UUID documentId) {
        return ResultSets.query(session, documentTeamPermissionMaterializer.fetchPermission(teamId, documentId))
                .flatMapIterable(row -> row)
                .map(documentTeamPermissionMaterializer::fromRow)
                .singleOrEmpty()
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching permission for team %s and document %s",
                            teamId, documentId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the documents an account has access to
     *
     * @param accountId the account id to search the document permissions for
     * @return a {@link Flux} of {@link AccountDocumentPermission}
     */
    public Flux<AccountDocumentPermission> findAccountPermissions(UUID accountId) {
        return ResultSets.query(session, documentAccountPermissionMaterializer.fetchPermission(accountId))
                .flatMapIterable(row -> row)
                .map(documentAccountPermissionMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching permission for account %s", accountId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the documents an account has access to
     *
     * @param teamId the team id to search the document permissions for
     * @return a {@link Flux} of {@link TeamDocumentPermission}
     */
    public Flux<TeamDocumentPermission> findTeamPermissions(UUID teamId) {
        return ResultSets.query(session, documentTeamPermissionMaterializer.fetchPermission(teamId))
                .flatMapIterable(row -> row)
                .map(documentTeamPermissionMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching permission for team %s", teamId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }


}
