package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import com.smartsparrow.competency.service.DocumentPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.competency.DocumentMessage;

public class AllowDocumentReviewerOrHigher implements AuthorizationPredicate<DocumentMessage> {

    private final DocumentPermissionService documentPermissionService;

    @Inject
    public AllowDocumentReviewerOrHigher(DocumentPermissionService documentPermissionService) {
        this.documentPermissionService = documentPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, DocumentMessage documentMessage) {

        Account account = authenticationContext.getAccount();

        PermissionLevel permissionLevel = documentPermissionService
                .findHighestPermissionLevel(account.getId(), documentMessage.getDocumentId()).block();

        return permissionLevel != null && permissionLevel.isEqualOrHigherThan(PermissionLevel.REVIEWER);
    }
}
