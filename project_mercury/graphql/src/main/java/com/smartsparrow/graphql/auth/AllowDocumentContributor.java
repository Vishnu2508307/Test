package com.smartsparrow.graphql.auth;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.smartsparrow.competency.service.DocumentPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;

public class AllowDocumentContributor {

    private final DocumentPermissionService documentPermissionService;

    @Inject
    public AllowDocumentContributor(DocumentPermissionService documentPermissionService) {
        this.documentPermissionService = documentPermissionService;
    }

    /**
     * Allows a document {@link PermissionLevel#CONTRIBUTOR} or higher
     * @param authenticationContext the authenticationContext of the user
     * @param documentId the document id to check the permission for
     * @return <code>true</code> when the user has the required permission.
     * <br><code>false</code> when the user lacks the required permission
     */
    public boolean test(AuthenticationContext authenticationContext,  @Nonnull UUID documentId) {
        Account requesterAccount = authenticationContext.getAccount();

        if (requesterAccount != null) {
            PermissionLevel requesterPermissionLevel = documentPermissionService
                    .findHighestPermissionLevel(requesterAccount.getId(), documentId)
                    .block();
            return requesterPermissionLevel != null &&
                    requesterPermissionLevel.isEqualOrHigherThan(PermissionLevel.CONTRIBUTOR);
        }

        return false;
   }
}
