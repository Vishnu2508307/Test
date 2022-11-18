package com.smartsparrow.competency.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.dataevent.RouteUri.COMPETENCY_DOCUMENT_UPDATE;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Exchange;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newrelic.api.agent.Trace;
import com.rometools.utils.Strings;
import com.smartsparrow.competency.data.AccountDocumentCollaborator;
import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.data.DocumentGateway;
import com.smartsparrow.competency.data.DocumentVersion;
import com.smartsparrow.competency.data.TeamDocumentCollaborator;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentAction;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.dataevent.RouteUri;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentGateway documentGateway;
    private final DocumentPermissionService documentPermissionService;
    private final TeamService teamService;
    private final CamelReactiveStreamsService camel;

    @Inject
    public DocumentService(final DocumentGateway documentGateway,
                           final DocumentPermissionService documentPermissionService,
                           final TeamService teamService,
                           final CamelReactiveStreamsService camel) {
        this.documentGateway = documentGateway;
        this.documentPermissionService = documentPermissionService;
        this.teamService = teamService;
        this.camel = camel;
    }

    /**
     * Create a document inside a workspace and save creator as an OWNER
     *
     * @param title       the document title
     * @param workspaceId the workspace id
     * @param creatorId   the creator account id
     * @return created document
     */
    public Mono<Document> create(final String title, final UUID workspaceId, final UUID creatorId) {
        checkArgument(workspaceId != null, "missing workspaceId");
        checkArgument(Strings.isNotEmpty(title), "missing title");
        checkArgument(creatorId != null, "missing creatorId");

        UUID id = UUIDs.random();
        Document document = new Document()
                .setId(id)
                .setTitle(title)
                .setCreatedAt(UUIDs.timeBased())
                .setCreatedBy(creatorId)
                .setWorkspaceId(workspaceId)
                .setOrigin("AERO");

        return documentGateway.persist(document)
                .thenMany(documentPermissionService.saveAccountPermissions(creatorId, id, PermissionLevel.OWNER))
                .then(Mono.just(document));
    }

    /**
     * Fetch the account collaborators for a document
     *
     * @param documentId the document id to search the collaborators for
     * @return a {@link Flux} of {@link AccountDocumentCollaborator}
     */
    public Flux<AccountDocumentCollaborator> fetchAccountCollaborators(final UUID documentId) {
        return documentGateway.findAccountCollaborators(documentId);
    }

    /**
     * Fetch the team collaborators for a document
     *
     * @param documentId the document id to search the collaborators for
     * @return a {@link Flux} of {@link TeamDocumentCollaborator}
     */
    public Flux<TeamDocumentCollaborator> fetchTeamCollaborators(final UUID documentId) {
        return documentGateway.findTeamCollaborators(documentId);
    }

    /**
     * Find all the documents an account has access to (direct access or as a team member)
     *
     * @param accountId the account to search the documents for
     * @return a flux of documents
     */
    @Trace(async = true)
    public Flux<Document> fetchDocuments(final UUID accountId) {
        return documentGateway.findDocumentsByAccount(accountId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .mergeWith(teamService.findTeamsForAccount(accountId)
                        .flatMap(team -> documentGateway.findDocumentsByTeam(team.getTeamId())))
                .distinct()
                .flatMap(documentGateway::findById);
    }

    /**
     * Find a document by account id and document id
     *
     * @param documentId the document id to find
     * @return a {@link Mono<Document>}
     */
    @Trace(async = true)
    public Mono<Document> fetchDocument(final UUID documentId) {
        return documentGateway.findById(documentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Save new version for the document
     *
     * @param documentId the document
     * @param accountId  the account who changed the document
     * @return
     */
    public Flux<Void> updateVersion(final UUID documentId, final UUID accountId) {
        checkArgument(documentId != null, "missing documentId");
        checkArgument(accountId != null, "missing accountId");

        return documentGateway.updateVersion(documentId, accountId, UUIDs.timeBased());
    }

    /**
     * Emit a competency document broadcast message on the {@link RouteUri#COMPETENCY_DOCUMENT_UPDATE} route so that
     * other subscribed client can retrive the changes
     *
     * @param broadcastMessage the message that holds the information to be broadcast
     * @return a mono of publisher exchange
     */
    public Mono<Publisher<Exchange>> emitEvent(final CompetencyDocumentBroadcastMessage broadcastMessage) {

        return Mono.just(broadcastMessage) //
                .map(event -> camel.toStream(COMPETENCY_DOCUMENT_UPDATE, event)); //
    }

    /**
     * Find the latest version of a document
     *
     * @param documentId the id of the document to find the version for
     * @return a mono of document version
     */
    public Mono<DocumentVersion> findLatestVersion(final UUID documentId) {
        return documentGateway.findVersion(documentId);
    }

    /**
     * Delete a document
     *
     * @param document the document
     * @return a mono of document
     */
    @Trace(async = true)
    public Mono<Document> delete(Document document){
        return documentGateway.delete(document)
                .then(Mono.just(document))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Update a document
     *
     * @param updatedDocument the updated document
     * @return a mono of document
     */
    @Trace(async = true)
    public Mono<Document> update(Document updatedDocument) {
        affirmArgument(updatedDocument != null, "updated document is required");
        affirmArgument(updatedDocument.getId() != null, "updated documentId is required");

        return documentGateway.update(updatedDocument)
                .then(Mono.just(updatedDocument))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Broadcast document message
     *
     * @param documentId the document id
     * @param action     the document actioni
     * @return a mono of void
     */
    public Mono<Void> broadcastDocumentMessage(UUID documentId, CompetencyDocumentAction action){
        affirmArgument(documentId != null, "documentId is required");
        affirmArgument(action != null, "action is required");

        // prepare a broadcast message
        CompetencyDocumentBroadcastMessage broadcastMessage = new CompetencyDocumentBroadcastMessage()
                .setAction(action)
                .setDocumentId(documentId);

        return emitEvent(broadcastMessage).then();
    }

    /**
     * Delete all accounts associated with the document
     *
     * @param documentId the document id
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> deleteAccountCollaborators(UUID documentId) {
        affirmArgument(documentId != null, "documentId is required");

        return documentGateway.findAccountCollaborators(documentId)
                .flatMap(accountDocumentCollaborator ->
                        documentPermissionService.deleteAccountPermissions(accountDocumentCollaborator.getAccountId(),
                                                                           accountDocumentCollaborator.getDocumentId())
                ).doOnEach(ReactiveTransaction.linkOnNext());

    }

    /**
     * Delete all teams associated with the document
     *
     * @param documentId the document id
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> deleteTeamCollaborators(UUID documentId) {
        affirmArgument(documentId != null, "documentId is required");

        return documentGateway.findTeamCollaborators(documentId)
                .flatMap(teamDocumentCollaborator ->
                        documentPermissionService.deleteTeamPermissions(teamDocumentCollaborator.getTeamId(),
                                teamDocumentCollaborator.getDocumentId())
                ).doOnEach(ReactiveTransaction.linkOnNext());

    }
}
