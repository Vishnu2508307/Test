package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.competency.data.DocumentItemReference;
import com.smartsparrow.competency.payload.DocumentItemLinkPayload;
import com.smartsparrow.competency.payload.DocumentItemReferencePayload;
import com.smartsparrow.competency.service.DocumentItemLinkService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCoursewareElementContributorOrHigher;
import com.smartsparrow.graphql.auth.AllowDocumentReviewerOrHigher;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentItemLinkInput;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentItemUnlinkInput;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentLinkInput;
import com.smartsparrow.graphql.type.mutation.CompetencyDocumentLinkPayload;
import com.smartsparrow.graphql.type.mutation.DocumentItemInput;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.AuthenticationContext;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.execution.ResolutionEnvironment;

@Singleton
public class DocumentItemTagMutationSchema {

    private final DocumentItemLinkService documentItemLinkService;
    private final AllowDocumentReviewerOrHigher allowDocumentReviewerOrHigher;
    private final AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher;

    @Inject
    public DocumentItemTagMutationSchema(DocumentItemLinkService documentItemLinkService,
                                         AllowDocumentReviewerOrHigher allowDocumentReviewerOrHigher,
                                         AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher) {
        this.documentItemLinkService = documentItemLinkService;
        this.allowDocumentReviewerOrHigher = allowDocumentReviewerOrHigher;
        this.allowCoursewareElementContributorOrHigher = allowCoursewareElementContributorOrHigher;
    }

    @SuppressWarnings("Duplicates")
    @GraphQLMutation(name = "competencyDocumentItemLink", description = "Link a competency document to a courseware element")
    public CompletableFuture<CompetencyDocumentLinkPayload> linkCoursewareElement(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                  @GraphQLArgument(name = "input") CompetencyDocumentItemLinkInput input) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        // validate required arguments
        validateMutationArguments(input);

        // build the document courseware element to be linked
        final CoursewareElement element = new CoursewareElement()
                .setElementType(input.getElementType())
                .setElementId(input.getElementId());

        // check the permissions are valid
        checkPermissions(context.getAuthenticationContext(), input.getDocumentItems(), element);

        // build the list of documentItems to link to the courseware element
        List<DocumentItemReference> documentItems = buildDocumentItemReferences(input.getDocumentItems());

        // link the courseware element with the document items
        return documentItemLinkService.link(element, documentItems)
                .map(DocumentItemReferencePayload::from).collectList()
                // return the payload
                .map(all -> new CompetencyDocumentLinkPayload()
                        .setDocumentLink(DocumentItemLinkPayload.from(element, all)))
                .toFuture();
    }

    @SuppressWarnings("Duplicates")
    @GraphQLMutation(name = "competencyDocumentItemUnlink", description = "Unlink a competency document from a courseware element")
    public CompletableFuture<CompetencyDocumentLinkPayload> unlinkCoursewareElement(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                    @GraphQLArgument(name = "input") CompetencyDocumentItemUnlinkInput input) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        // validate required arguments
        validateMutationArguments(input);

        // build the document courseware element to unlink
        final CoursewareElement element = new CoursewareElement()
                .setElementType(input.getElementType())
                .setElementId(input.getElementId());

        // check permissions are valid
        checkPermissions(context.getAuthenticationContext(), input.getDocumentItems(), element);

        // build the list of documentItems to unlink from the courseware element
        List<DocumentItemReference> documentItems = buildDocumentItemReferences(input.getDocumentItems());

        // unlink the courseware element from the document items
        return documentItemLinkService.unlink(element, documentItems)
                .map(DocumentItemReferencePayload::from).collectList()
                // return the payload
                .map(all -> new CompetencyDocumentLinkPayload()
                        .setDocumentLink(DocumentItemLinkPayload.from(element, all)))
                .toFuture();
    }

    /**
     * Check that the user has permission to mutate the document item as well as the courseware element
     *
     * @param documentItems the list of document items to check the permission for
     * @param element the courseware element to check the permission for
     * @throws PermissionFault when the user dos not have the required permission over a document or the courseware element
     */
    private void checkPermissions(AuthenticationContext authenticationContext, List<DocumentItemInput> documentItems, CoursewareElement element) {
        // check permission on document items
        documentItems.forEach(one-> {
            affirmPermission(allowDocumentReviewerOrHigher.test(authenticationContext, one.getDocumentId()),
                    String.format("Permission level missing or too low over document %s, item %s", one.getDocumentId(),
                            one.getDocumentItemId()));
        });
        // check permission on courseware element
        affirmPermission(allowCoursewareElementContributorOrHigher.test(authenticationContext, element.getElementId(), element.getElementType()),
                String.format("Permission level missing or too low over element %s, type %s",
                        element.getElementId(), element.getElementType()));
    }

    /**
     * Check that the supplied argument is valid and contains valid values
     *
     * @param input the object to validate the values for
     * @throws IllegalArgumentFault when any of the required argument is <code>null</code> or invalid
     */
    private void validateMutationArguments(CompetencyDocumentLinkInput input) {
        affirmArgument(input != null, "input is required");
        affirmArgument(input.getDocumentItems() != null, "documentItems is required");
        affirmArgument(input.getDocumentItems().size() > 0, "at least 1 document item is required");
        affirmArgument(input.getElementId() != null, "elementId is required");
        affirmArgument(input.getElementType() != null, "elementType is required");
    }

    /**
     * Convenience method to transform a list of {@link DocumentItemInput} to a list of {@link DocumentItemReference}
     *
     * @param documentItems the list to transform
     * @return the transformed list
     */
    private List<DocumentItemReference> buildDocumentItemReferences(List<DocumentItemInput> documentItems) {
        return documentItems
                .stream()
                .map(one -> new DocumentItemReference()
                        .setDocumentId(one.getDocumentId())
                        .setDocumentItemId(one.getDocumentItemId()))
                .collect(Collectors.toList());
    }
}
