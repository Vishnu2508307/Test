package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.rometools.utils.Strings;
import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentAction;
import com.smartsparrow.competency.payload.DocumentPayload;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowDocumentContributor;
import com.smartsparrow.graphql.auth.AllowWorkspaceContributorOrHigher;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentCreateInput;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentCreatePayload;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentDeleteInput;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentMutationPayload;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentUpdateInput;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

@Singleton
public class DocumentMutationSchema {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DocumentService.class);

    private final DocumentService documentService;
    private final AllowWorkspaceContributorOrHigher allowWorkspaceContributorOrHigher;
    private final AllowDocumentContributor allowDocumentContributor;
    private final DocumentItemService documentItemService;

    @Inject
    public DocumentMutationSchema(DocumentService documentService,
                                  AllowWorkspaceContributorOrHigher allowWorkspaceContributorOrHigher,
                                  DocumentItemService documentItemService,
                                  AllowDocumentContributor allowDocumentContributor) {
        this.documentService = documentService;
        this.allowWorkspaceContributorOrHigher = allowWorkspaceContributorOrHigher;
        this.documentItemService = documentItemService;
        this.allowDocumentContributor = allowDocumentContributor;
    }

    /**
     * Create a competency document
     *
     * @param input the input
     * @return the payload for created document
     */
    @GraphQLMutation(name = "competencyDocumentCreate", description = "Create a competency document")
    public CompletableFuture<CompetencyDocumentCreatePayload> createCompetencyDocument(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                       @GraphQLArgument(name = "input") CompetencyDocumentCreateInput input) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        affirmArgument(input != null, "missing document");
        affirmArgument(input.getWorkspaceId() != null, "missing workspaceId");
        affirmArgument(Strings.isNotEmpty(input.getTitle()), "missing title");

        affirmPermission(allowWorkspaceContributorOrHigher.test(context.getAuthenticationContext(), input.getWorkspaceId()),
                "User does not have permissions to change workspace");

        UUID accountId = context.getAuthenticationContext().getAccount().getId();

        return documentService.create(input.getTitle(), input.getWorkspaceId(), accountId)
                .map(DocumentPayload::from)
                .map(result -> new CompetencyDocumentCreatePayload().setDocument(result))
                .toFuture();
    }

    /**
     * Delete a competency document
     * Note: Not deleted the document in the DB. It deletes the association between the document and account and team.
     *
     * @param input     the input
     * @return the payload for the deleted document
     * @throws IllegalArgumentFault when any of the required argument is null or any document items have been linked or published
     * @throws NotFoundFault if a deleted document cannot be found
     */
    @GraphQLMutation(name = "competencyDocumentDelete", description = "Delete a competency document")
    public CompletableFuture<CompetencyDocumentMutationPayload> deleteCompetencyDocument(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                         @GraphQLArgument(name = "input") CompetencyDocumentDeleteInput input) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        affirmArgument(input != null, "input is required");
        affirmArgument(input.getWorkspaceId() != null, "workspaceId is required");
        affirmArgument(input.getDocumentId() != null, "documentId is required");

        // check if a user has document contributor or higher permission
        affirmPermission(allowDocumentContributor.test(context.getAuthenticationContext(), input.getDocumentId()),
                "User does not have permissions to delete the document");

        // check if the deleted document exist and any document items linked or published
        Document deletedDocument = getDocument(input.getDocumentId())
                                    .doOnNext(document -> checkAnyDocumentItemsLinkedOrPublished(document.getId()))
                                    .block();

        affirmArgument(deletedDocument != null, "Cannot find the deleted document");

        // get a user account
        final Account account = context.getAuthenticationContext().getAccount();

        // delete all accounts associated with the deleted document
        return documentService.deleteAccountCollaborators(deletedDocument.getId())
                // delete all teams associated with the deleted document
                .thenMany(documentService.deleteTeamCollaborators(deletedDocument.getId()))
                // save user account and modified time
                .then(documentService.update(deletedDocument.setModifiedBy(account.getId())
                                                            .setModifiedAt(UUIDs.timeBased())))
                // broadcast deleted document message to listening subscriptions
                .then(documentService.broadcastDocumentMessage(deletedDocument.getId(),
                        CompetencyDocumentAction.DOCUMENT_DELETED))
                .doOnEach(log.reactiveInfoSignal("Document service: competency document deleted",
                        ignored -> new HashMap<String, Object>() {
                            {
                                put("documentId", deletedDocument.getId());
                                put("accountId", account.getId());
                            }
                        }))
                .thenReturn(new CompetencyDocumentMutationPayload().setDocument(DocumentPayload.from(deletedDocument)))
                .toFuture();
    }

    /**
     * Update a competency document
     *
     * @param input     the input
     * @return the payload for the updated document
     * @throws IllegalArgumentFault when any of the required argument is null
     */
    @GraphQLMutation(name = "competencyDocumentUpdate", description = "Update a competency document")
    public CompletableFuture<CompetencyDocumentMutationPayload> updateCompetencyDocument(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                         @GraphQLArgument(name = "input") CompetencyDocumentUpdateInput input) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        affirmArgument(input != null, "input is required");
        affirmArgument(input.getWorkspaceId() != null, "workspaceId is required");
        affirmArgument(input.getDocumentId() != null, "documentId is required");
        affirmArgument(input.getTitle() != null, "document title is required");

        //check user document permission
        affirmPermission(allowDocumentContributor.test(context.getAuthenticationContext(),
                        input.getDocumentId()),
                "User does not have permissions to update the document");

        // get a user account
        final Account account = context.getAuthenticationContext().getAccount();

        // get updated document
        final Document oldDocument = getDocument(input.getDocumentId()).block();

        affirmArgument(oldDocument != null, "Cannot find the updated document");

        //setup updated document
        Document newDocument = new Document().setId(oldDocument.getId())
                                                .setTitle(input.getTitle())
                                                .setCreatedAt(oldDocument.getCreatedAt())
                                                .setCreatedBy(oldDocument.getCreatedBy())
                                                .setModifiedAt(UUIDs.timeBased())
                                                .setModifiedBy(account.getId())
                                                .setWorkspaceId(oldDocument.getWorkspaceId())
                                                .setOrigin(oldDocument.getOrigin());
        // update the document
        return documentService.update(newDocument)
                // broadcast document updated message to listening subscriptions
                .flatMap(document -> documentService.broadcastDocumentMessage(document.getId(),
                                                            CompetencyDocumentAction.DOCUMENT_UPDATED)
                                                    .thenReturn(new CompetencyDocumentMutationPayload()
                                                            .setDocument(DocumentPayload.from(newDocument))))
                .toFuture();
    }

    /**
     * Find a document by document id
     *
     * @param documentId document id to find
     * @return a mono of document
     * @throws NotFoundFault if a document cannot be found
     */
    Mono<Document> getDocument(UUID documentId){
        return documentService.fetchDocument(documentId)
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find the document by document id: %s",
                documentId))));
    }

    /**
     * Check if any document items have been linked or published
     *
     * @param documentId document id to find
     * @return void
     * @throws IllegalArgumentFault when any document items have been linked or published
     */
    private void checkAnyDocumentItemsLinkedOrPublished(UUID documentId) {
         documentItemService.findByDocumentId(documentId)
            .flatMap(documentItem -> documentItemService.isItemPublished(documentItem.getId())
                    .doOnNext(isItemPublished -> {
                        if (isItemPublished)
                            throw new IllegalArgumentFault("Published document item can not be deleted");
                    }).then(documentItemService.isItemLinked(documentItem.getId())
                            .doOnNext(isItemLinked -> {
                                if (isItemLinked)
                                    throw new IllegalArgumentFault("Linked document item can not be deleted");
                            })))
            .blockLast();
    }
}
