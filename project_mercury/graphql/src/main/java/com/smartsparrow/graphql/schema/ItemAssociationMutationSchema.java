package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Exchange;
import org.reactivestreams.Publisher;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentAction;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.competency.service.ItemAssociationService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowDocumentContributor;
import com.smartsparrow.graphql.type.mutation.ItemAssociationCreateInput;
import com.smartsparrow.graphql.type.mutation.ItemAssociationCreatePayload;
import com.smartsparrow.graphql.type.mutation.ItemAssociationDeleteInput;
import com.smartsparrow.graphql.type.mutation.ItemAssociationDeletePayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

@Singleton
public class ItemAssociationMutationSchema {

    private final ItemAssociationService itemAssociationService;
    private final AllowDocumentContributor allowDocumentContributor;
    private final DocumentItemService documentItemService;
    private final DocumentService documentService;

    @Inject
    public ItemAssociationMutationSchema(ItemAssociationService itemAssociationService,
                                         AllowDocumentContributor allowDocumentContributor,
                                         DocumentItemService documentItemService,
                                         DocumentService documentService) {
        this.itemAssociationService = itemAssociationService;
        this.allowDocumentContributor = allowDocumentContributor;
        this.documentItemService = documentItemService;
        this.documentService = documentService;
    }

    /**
     * Create an association between two competency items
     *
     * @param input the input
     * @return the payload for created document
     */
    @GraphQLMutation(name = "competencyItemAssociationCreate", description = "Create an association between two competency items")
    public CompletableFuture<ItemAssociationCreatePayload> createItemAssociation(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                 @GraphQLArgument(name = "input") ItemAssociationCreateInput input) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        affirmArgument(input != null, "missing input");
        affirmArgument(input.getDocumentId() != null, "missing documentId");
        affirmArgument(input.getOriginItemId() != null, "missing originItemId");
        affirmArgument(input.getDestinationItemId() != null, "missing destinationItemId");
        affirmArgument(input.getAssociationType() != null, "missing associationType");

        affirmPermission(allowDocumentContributor.test(context.getAuthenticationContext(), input.getDocumentId()),
                "User does not have permissions to change document");

        affirmArgument(documentItemService.findById(input.getOriginItemId()).block() != null,
                "originItemId does not exist");
        affirmArgument(documentItemService.findById(input.getDestinationItemId()).block() != null,
                "destinationItemId does not exist");

        final AuthenticationContext authenticationContext = context.getAuthenticationContext();
        final Account account = authenticationContext.getAccount();

        // @formatter:off
        return itemAssociationService.create(input.getDocumentId(),
                                             input.getOriginItemId(),
                                             input.getDestinationItemId(),
                                             input.getAssociationType(),
                                             account.getId())
         // @formatter:on
                .flatMap(result -> {

                    CompetencyDocumentBroadcastMessage broadcastMessage = new CompetencyDocumentBroadcastMessage()
                            .setAction(CompetencyDocumentAction.ASSOCIATION_CREATED)
                            .setAssociationId(result.getId())
                            .setDocumentId(result.getDocumentId());

                    return documentService.emitEvent(broadcastMessage)
                            .thenReturn(new ItemAssociationCreatePayload().setAssociation(result));
                })
                .toFuture();
    }

    /**
     * Delete an association between two competency items
     *
     * @param input the input
     * @return the payload for created document
     */
    @GraphQLMutation(name = "competencyItemAssociationDelete", description = "Delete an item association")
    public CompletableFuture<ItemAssociationDeletePayload> deleteItemAssociation(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                 @GraphQLArgument(name = "input") ItemAssociationDeleteInput input) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        affirmArgument(input != null, "missing input");
        affirmArgument(input.getDocumentId() != null, "missing documentId");
        affirmArgument(input.getAssociationId() != null, "missing associationId");

        affirmPermission(allowDocumentContributor.test(context.getAuthenticationContext(), input.getDocumentId()),
                "User does not have permissions to change document");

        final AuthenticationContext authenticationContext = context.getAuthenticationContext();
        final Account account = authenticationContext.getAccount();

        Mono<Publisher<Exchange>> publisherMono = itemAssociationService.findById(input.getAssociationId())
                .flatMap(association -> {
                    CompetencyDocumentBroadcastMessage broadcastMessage = new CompetencyDocumentBroadcastMessage()
                            .setAction(CompetencyDocumentAction.ASSOCIATION_DELETED)
                            .setAssociationId(association.getId())
                            .setDocumentId(association.getDocumentId());

                    return itemAssociationService.delete(association, account.getId())
                            .then(documentService.emitEvent(broadcastMessage));
                });

        ItemAssociationDeletePayload itemAssociationDeletePayload = new ItemAssociationDeletePayload()
                .setDocumentId(input.getDocumentId())
                .setAssociationId(input.getAssociationId());

        return publisherMono.thenReturn(itemAssociationDeletePayload).toFuture();

    }

}
