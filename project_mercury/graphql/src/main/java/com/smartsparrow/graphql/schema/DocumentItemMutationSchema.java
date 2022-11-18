package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.competency.data.DocumentItem;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentAction;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowDocumentContributor;
import com.smartsparrow.graphql.type.mutation.CreateDocumentItemInput;
import com.smartsparrow.graphql.type.mutation.DeleteDocumentItemInput;
import com.smartsparrow.graphql.type.mutation.DocumentItemMutationPayload;
import com.smartsparrow.graphql.type.mutation.UpdateDocumentItemInput;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.execution.ResolutionEnvironment;

@Singleton
public class DocumentItemMutationSchema {

    private final AllowDocumentContributor allowDocumentContributor;
    private final DocumentItemService documentItemService;
    private final DocumentService documentService;

    @Inject
    public DocumentItemMutationSchema(AllowDocumentContributor allowDocumentContributor,
                                      DocumentItemService documentItemService,
                                      DocumentService documentService) {
        this.allowDocumentContributor = allowDocumentContributor;
        this.documentItemService = documentItemService;
        this.documentService = documentService;
    }

    /**
     * Mutation api for creating a document item
     *
     * @param input the input object containing the allowed fields on creation
     * @return a document item
     */
    @SuppressWarnings("Duplicates")
    @GraphQLMutation(name = "competencyDocumentItemCreate", description = "Allows to create a document item for a competency document")
    public CompletableFuture<DocumentItemMutationPayload> createDocumentItem(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                             @GraphQLArgument(name = "input") CreateDocumentItemInput input){

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // validate the arguments
        affirmArgument(input != null, "input is required");
        affirmArgument(input.getDocumentId() != null, "input.documentId is required");

        // check the permission
        affirmPermission(allowDocumentContributor.test(context.getAuthenticationContext(), input.getDocumentId()), "not allowed to create the document item");

        // get the account and the clientId
        final AuthenticationContext authenticationContext = context.getAuthenticationContext();
        final Account account = authenticationContext.getAccount();

        // prepare a provided document item to supply to the service for the creation
        DocumentItem provided = new DocumentItem()
                .setFullStatement(input.getFullStatement())
                .setAbbreviatedStatement(input.getAbbreviatedStatement())
                .setHumanCodingScheme(input.getHumanCodingScheme());

        // create the document item
        return documentItemService.create(account.getId(), input.getDocumentId(), provided)
                .flatMap(payload -> {
                    // prepare a broadcast message
                    CompetencyDocumentBroadcastMessage broadcastMessage = new CompetencyDocumentBroadcastMessage()
                            .setAction(CompetencyDocumentAction.DOCUMENT_ITEM_CREATED)
                            .setDocumentItemId(payload.getId())
                            .setDocumentId(payload.getDocumentId());

                    // emit the broadcast message to listening subscriptions
                    return documentService.emitEvent(broadcastMessage)
                            // return the document item payload
                            .thenReturn(new DocumentItemMutationPayload()
                                    .setDocumentItem(payload));
                }).toFuture();
    }

    /**
     * Mutation api for updating a document item
     *
     * @param input the inputs object containing the allowed fields on update
     * @return a document item
     */
    @SuppressWarnings("Duplicates")
    @GraphQLMutation(name = "CompetencyDocumentItemUpdate", description = "Update the old fields with the one provided in documentItem body")
    public CompletableFuture<DocumentItemMutationPayload> updateDocumentItem(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                             @GraphQLArgument(name = "input") UpdateDocumentItemInput input) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // check that the required arguments are supplied
        affirmArgument(input != null, "input is required");
        affirmArgument(input.getDocumentId() != null, "input.documentId is required");
        affirmArgument(input.getId() != null, "input.id is required");

        affirmPermission(allowDocumentContributor.test(context.getAuthenticationContext(), input.getDocumentId()), "not allowed to create the document item");

        // get the account and the clientId
        final AuthenticationContext authenticationContext = context.getAuthenticationContext();
        final Account account = authenticationContext.getAccount();

        // prepare a provided document item to supply to the service for the update
        DocumentItem provided = new DocumentItem()
                .setFullStatement(input.getFullStatement())
                .setAbbreviatedStatement(input.getAbbreviatedStatement())
                .setHumanCodingScheme(input.getHumanCodingScheme());

        return documentItemService.update(account.getId(), input.getDocumentId(), input.getId(), provided)
                .then(documentItemService.getDocumentItemPayload(input.getId()))
                .flatMap(payload -> {
                    // prepare a broadcast message
                    CompetencyDocumentBroadcastMessage broadcastMessage = new CompetencyDocumentBroadcastMessage()
                            .setAction(CompetencyDocumentAction.DOCUMENT_ITEM_UPDATED)
                            .setDocumentItemId(payload.getId())
                            .setDocumentId(payload.getDocumentId());

                    // emit the broadcast message to listening subscriptions
                    return documentService.emitEvent(broadcastMessage)
                            // return the document item payload
                            .thenReturn(new DocumentItemMutationPayload()
                                    .setDocumentItem(payload));

                })
                .toFuture();
    }

    /**
     * Mutation api for deleting a document item
     *
     * @param input the inputs object containing the allowed fields on delete
     * @return the deleted document item (not all fields included see docs at
     * {@link DocumentItemService#delete(UUID, UUID, UUID)})
     */
    @SuppressWarnings("Duplicates")
    @GraphQLMutation(name = "competencyDocumentItemDelete", description = "Delete the document item")
    public CompletableFuture<DocumentItemMutationPayload> deleteDocumentItem(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                             @GraphQLArgument(name = "input") DeleteDocumentItemInput input) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // check that the required arguments are supplied
        affirmArgument(input != null, "input is required");
        affirmArgument(input.getDocumentId() != null, "input.documentId is required");
        affirmArgument(input.getId() != null, "input.id is required");

        affirmPermission(allowDocumentContributor.test(context.getAuthenticationContext(), input.getDocumentId()), "not allowed to create the document item");

        // get the account and the clientId
        final AuthenticationContext authenticationContext = context.getAuthenticationContext();
        final Account account = authenticationContext.getAccount();

        return documentItemService.delete(account.getId(), input.getDocumentId(), input.getId())
                .flatMap(documentItem -> {
                    // prepare a broadcast message
                    CompetencyDocumentBroadcastMessage broadcastMessage = new CompetencyDocumentBroadcastMessage()
                            .setAction(CompetencyDocumentAction.DOCUMENT_ITEM_DELETED)
                            .setDocumentId(documentItem.getDocumentId())
                            .setDocumentItemId(documentItem.getId())
                            .setDocumentId(documentItem.getDocumentId());

                    // emit the broadcast message to listening subscriptions
                    return documentService.emitEvent(broadcastMessage)
                            // return the document item payload
                            .thenReturn(new DocumentItemMutationPayload()
                                    .setDocumentItem(DocumentItemPayload.from(documentItem)));
                })
                .toFuture();
    }
}
