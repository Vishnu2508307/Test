package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class DocumentGateway {

    private static final Logger log = LoggerFactory.getLogger(DocumentGateway.class);

    private final Session session;

    private final DocumentMutator documentMutator;
    private final DocumentMaterializer documentMaterializer;
    private final DocumentByWorkspaceMutator documentByWorkspaceMutator;
    private final DocumentByWorkspaceMaterializer documentByWorkspaceMaterializer;
    private final AccountDocumentCollaboratorMaterializer accountDocumentCollaboratorMaterializer;
    private final AccountDocumentCollaboratorMutator accountDocumentCollaboratorMutator;
    private final TeamDocumentCollaboratorMaterializer teamDocumentCollaboratorMaterializer;
    private final TeamDocumentCollaboratorMutator teamDocumentCollaboratorMutator;
    private final DocumentByAccountMaterializer documentByAccountMaterializer;
    private final DocumentByAccountMutator documentByAccountMutator;
    private final DocumentByTeamMaterializer documentByTeamMaterializer;
    private final DocumentByTeamMutator documentByTeamMutator;
    private final DocumentVersionMutator documentVersionMutator;
    private final DocumentVersionMaterializer documentVersionMaterializer;

    @Inject
    public DocumentGateway(Session session,
                           DocumentMutator documentMutator,
                           DocumentMaterializer documentMaterializer,
                           DocumentByWorkspaceMutator documentByWorkspaceMutator,
                           DocumentByWorkspaceMaterializer documentByWorkspaceMaterializer,
                           AccountDocumentCollaboratorMaterializer accountDocumentCollaboratorMaterializer,
                           AccountDocumentCollaboratorMutator accountDocumentCollaboratorMutator,
                           TeamDocumentCollaboratorMaterializer teamDocumentCollaboratorMaterializer,
                           TeamDocumentCollaboratorMutator teamDocumentCollaboratorMutator,
                           DocumentByAccountMaterializer documentByAccountMaterializer,
                           DocumentByAccountMutator documentByAccountMutator,
                           DocumentByTeamMaterializer documentByTeamMaterializer,
                           DocumentByTeamMutator documentByTeamMutator,
                           DocumentVersionMutator documentVersionMutator,
                           DocumentVersionMaterializer documentVersionMaterializer) {
        this.session = session;
        this.documentMutator = documentMutator;
        this.documentMaterializer = documentMaterializer;
        this.documentByWorkspaceMutator = documentByWorkspaceMutator;
        this.documentByWorkspaceMaterializer = documentByWorkspaceMaterializer;
        this.accountDocumentCollaboratorMaterializer = accountDocumentCollaboratorMaterializer;
        this.accountDocumentCollaboratorMutator = accountDocumentCollaboratorMutator;
        this.teamDocumentCollaboratorMaterializer = teamDocumentCollaboratorMaterializer;
        this.teamDocumentCollaboratorMutator = teamDocumentCollaboratorMutator;
        this.documentByAccountMaterializer = documentByAccountMaterializer;
        this.documentByAccountMutator = documentByAccountMutator;
        this.documentByTeamMaterializer = documentByTeamMaterializer;
        this.documentByTeamMutator = documentByTeamMutator;
        this.documentVersionMutator = documentVersionMutator;
        this.documentVersionMaterializer = documentVersionMaterializer;
    }

    /**
     * Persists CASE document and its relationship with workspace
     *
     * @param document the document to persist
     */
    public Flux<Void> persist(Document document) {
        return Mutators.execute(session, Mutators.upsert(
                Lists.newArrayList(documentMutator, documentByWorkspaceMutator), document));
    }

    /**
     * Save an account collaborating on a document
     *
     * @param accountDocumentCollaborator the collaborator account
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(AccountDocumentCollaborator accountDocumentCollaborator) {
        Flux<? extends Statement> stmt = Mutators.upsert(
                Lists.newArrayList(accountDocumentCollaboratorMutator), accountDocumentCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving document permission for an account %s",
                            accountDocumentCollaborator), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save a team collaborating on a document
     *
     * @param teamDocumentCollaborator the collaborating team
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(TeamDocumentCollaborator teamDocumentCollaborator) {
        Flux<? extends Statement> stmt = Mutators.upsert(
                Lists.newArrayList(teamDocumentCollaboratorMutator), teamDocumentCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving document permission for a team %s",
                            teamDocumentCollaborator), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete an account from list of document collaborators.
     * This only requires accountId and documentId to be defined when deleting.
     *
     * @param accountDocumentCollaborator the collaborator to delete
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(AccountDocumentCollaborator accountDocumentCollaborator) {
        Flux<? extends Statement> stmt = Mutators.delete(
                Lists.newArrayList(accountDocumentCollaboratorMutator), accountDocumentCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting document permission for account %s",
                            accountDocumentCollaborator), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete a team from list of document collaborators.
     * This only requires teamId and documentId to be defined when deleting.
     *
     * @param teamDocumentCollaborator the collaborator to delete
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(TeamDocumentCollaborator teamDocumentCollaborator) {
        Flux<? extends Statement> stmt = Mutators.delete(
                Lists.newArrayList(teamDocumentCollaboratorMutator), teamDocumentCollaborator);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting document permission for team %s",
                            teamDocumentCollaborator), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save a document access for an account
     *
     * @param documentAccount the account document access to save
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(DocumentAccount documentAccount) {
        Flux<? extends Statement> stmt = Mutators.upsert(
                Lists.newArrayList(documentByAccountMutator), documentAccount);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving document account %s",
                            documentAccount), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Save a document access for a team
     *
     * @param documentTeam the teams document access to save
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> persist(DocumentTeam documentTeam) {
        Flux<? extends Statement> stmt = Mutators.upsert(
                Lists.newArrayList(documentByTeamMutator), documentTeam);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving document team %s",
                            documentTeam), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete a document access for an account.
     *
     * @param documentAccount the account document access to delete
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(DocumentAccount documentAccount) {
        Flux<? extends Statement> stmt = Mutators.delete(
                Lists.newArrayList(documentByAccountMutator), documentAccount);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting document account %s",
                            documentAccount), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete a document access for an team.
     *
     * @param documentTeam the teams document access to delete
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> delete(DocumentTeam documentTeam) {
        Flux<? extends Statement> stmt = Mutators.delete(
                Lists.newArrayList(documentByTeamMutator), documentTeam);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error(String.format("error while deleting document team %s",
                            documentTeam), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all account collaborators for a document
     *
     * @param documentId the document to search the collaborators for
     * @return a {@link Flux} of {@link AccountDocumentCollaborator}
     */
    public Flux<AccountDocumentCollaborator> findAccountCollaborators(UUID documentId) {
        return ResultSets.query(session, accountDocumentCollaboratorMaterializer.fetchAccounts(documentId))
                .flatMapIterable(row -> row)
                .map(accountDocumentCollaboratorMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching collaborators for document %s", documentId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all team collaborators for a document
     *
     * @param documentId the document to search the collaborators for
     * @return a {@link Flux} of {@link TeamDocumentCollaborator}
     */
    public Flux<TeamDocumentCollaborator> findTeamCollaborators(UUID documentId) {
        return ResultSets.query(session, teamDocumentCollaboratorMaterializer.findTeams(documentId))
                .flatMapIterable(row -> row)
                .map(teamDocumentCollaboratorMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching collaborators for document %s", documentId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the documents an account has access to
     *
     * @param accountId the account to search the documents for
     * @return a {@link Flux} of document ids
     */
    @Trace(async = true)
    public Flux<UUID> findDocumentsByAccount(UUID accountId) {
        return ResultSets.query(session, documentByAccountMaterializer.fetchDocuments(accountId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(documentByAccountMaterializer::fromRow)
                .map(DocumentAccount::getDocumentId)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching documents for account %s", accountId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the documents a team has access to
     *
     * @param teamId the team to search the documents for
     * @return a {@link Flux} of document ids
     */
    public Flux<UUID> findDocumentsByTeam(UUID teamId) {
        return ResultSets.query(session, documentByTeamMaterializer.findDocuments(teamId))
                .flatMapIterable(row -> row)
                .map(documentByTeamMaterializer::fromRow)
                .map(DocumentTeam::getDocumentId)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching documents for team %s", teamId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all documents by workspace id
     *
     * @param workspaceId - the workspace id to find all associated documents for
     * @return a {@link Flux<Document>}
     */
    public Flux<Document> findDocumentsByWorkspace(UUID workspaceId) {
        return ResultSets.query(session, documentByWorkspaceMaterializer.fetchByWorkspace(workspaceId))
                .flatMapIterable(row -> row)
                .map(documentByWorkspaceMaterializer::fromRow)
                .flatMap(this::findById)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching document for workspace %s", workspaceId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find a document by id
     *
     * @param documentId
     * @return a {@link Mono<Document>}
     */
    @Trace(async = true)
    public Mono<Document> findById(UUID documentId) {
        return ResultSets.query(session, documentMaterializer.fetchById(documentId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .map(documentMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Create new version for the document and update modification data for the document
     *
     * @param documentId   the document was modified
     * @param modifiedById the id of the account who modified the document
     * @param modifiedAt   the time UUID when the document was modified
     */
    public Flux<Void> updateVersion(UUID documentId, UUID modifiedById, UUID modifiedAt) {
        return Mutators.execute(session, Flux.just(
                // update the document version
                documentVersionMutator.updateVersion(documentId, modifiedById, modifiedAt),
                // update the document modification fields
                documentMutator.updateDocumentEdit(documentId, modifiedById, modifiedAt)
        ));
    }

    /**
     * Find the latest document version
     *
     * @param documentId the id of the document to find the version for
     * @return a mono of document version
     */
    public Mono<DocumentVersion> findVersion(UUID documentId) {
        return ResultSets.query(session, documentVersionMaterializer.fetchLatest(documentId))
                .flatMapIterable(row->row)
                .map(documentVersionMaterializer::fromRow)
                .singleOrEmpty();
    }

    /**
     * Delete a document
     *
     * @param document the document to delete
     * @return a flux of void
     */
    public Flux<Void> delete(Document document){
        return Mutators.execute(session, Flux.just(
                documentMutator.delete(document),
                documentByWorkspaceMutator.delete(document)
        ));
    }

    /**
     * Update a document
     *
     * @param document the updated document
     * @return a flux of void
     */
    public Flux<Void> update(Document document) {
        return Mutators.execute(session, Flux.just(
                // update the document
                documentMutator.upsert(document),
                // update the document version
                documentVersionMutator.updateVersion(document.getId(), document.getModifiedBy(), document.getModifiedAt())
        )).doOnError(throwable -> {
            log.error(String.format("error while updating document for document id %s", document.getId()), throwable);
            throw Exceptions.propagate(throwable);
        });
    }
}
