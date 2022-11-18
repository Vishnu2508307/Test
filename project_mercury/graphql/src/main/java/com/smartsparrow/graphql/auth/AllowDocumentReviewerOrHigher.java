package com.smartsparrow.graphql.auth;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.service.DocumentPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;

public class AllowDocumentReviewerOrHigher {

    private final DocumentPermissionService documentPermissionService;

    @Inject
    public AllowDocumentReviewerOrHigher(DocumentPermissionService documentPermissionService) {
        this.documentPermissionService = documentPermissionService;
    }

    /**
     * Test the permission for a document
     * @param authenticationContext - the authentication context of the user
     * @param documentId - the documentId to test the permissions for
     * @return true if the user has access else false
     */
    public boolean test(AuthenticationContext authenticationContext, UUID documentId) {
        Account account = authenticationContext.getAccount();
        affirmArgument(account != null, "account cannot be null");
        affirmArgument(documentId != null, "documentId is required");

        PermissionLevel requesterPermissionLevel = documentPermissionService
                .findHighestPermissionLevel(account.getId(), documentId)
                .block();

        if (requesterPermissionLevel != null) {
            return requesterPermissionLevel.isEqualOrHigherThan(PermissionLevel.REVIEWER);
        }
        return false;
    }

    /**
     * Test the permissions for the list of documents
     * @param authenticationContext - the authentication context of the user
     * @param documentList - list of documents for which access is requested
     * @return true if the user has access, else false
     */
    public boolean test(AuthenticationContext authenticationContext, List<Document> documentList) {
        return documentList
                .stream()
                .map(Document::getId)
                .allMatch(document -> test(authenticationContext, document));
    }
}
