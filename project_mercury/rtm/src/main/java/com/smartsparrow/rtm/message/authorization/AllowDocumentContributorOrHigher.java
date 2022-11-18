package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.competency.service.DocumentPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.competency.DocumentMessage;

public class AllowDocumentContributorOrHigher implements AuthorizationPredicate<DocumentMessage> {

    private static final Logger logger = LoggerFactory.getLogger(AllowDocumentContributorOrHigher.class);

    private final DocumentPermissionService documentPermissionService;

    @Inject
    public AllowDocumentContributorOrHigher(DocumentPermissionService documentPermissionService) {
        this.documentPermissionService = documentPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Unauthorized permission level";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, DocumentMessage documentMessage) {
        Account requesterAccount = authenticationContext.getAccount();

        if (documentMessage.getDocumentId() != null) {
            if (requesterAccount != null) {
                PermissionLevel requesterPermissionLevel = documentPermissionService
                        .findHighestPermissionLevel(requesterAccount.getId(), documentMessage.getDocumentId())
                        .block();
                return requesterPermissionLevel != null &&
                        requesterPermissionLevel.isEqualOrHigherThan(PermissionLevel.CONTRIBUTOR);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Could not find account");
            }
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("documentId was not supplied with the message {}", documentMessage);
        }
        return false;
    }
}
