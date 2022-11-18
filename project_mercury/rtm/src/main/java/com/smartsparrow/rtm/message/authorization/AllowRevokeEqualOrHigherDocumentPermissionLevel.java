package com.smartsparrow.rtm.message.authorization;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.competency.service.DocumentPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.competency.RevokeDocumentPermissionMessage;

public class AllowRevokeEqualOrHigherDocumentPermissionLevel implements AuthorizationPredicate<RevokeDocumentPermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowRevokeEqualOrHigherDocumentPermissionLevel.class);

    private final DocumentPermissionService documentPermissionService;

    @Inject
    public AllowRevokeEqualOrHigherDocumentPermissionLevel(DocumentPermissionService documentPermissionService) {
        this.documentPermissionService = documentPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Higher permission level required";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, RevokeDocumentPermissionMessage revokeDocumentPermissionMessage) {
        Account requesterAccount = authenticationContext.getAccount();

        PermissionLevel requesterPermission = documentPermissionService
                .findHighestPermissionLevel(requesterAccount.getId(), revokeDocumentPermissionMessage.getDocumentId())
                .block();

        if (requesterPermission == null) {
            return false;
        }

        if (revokeDocumentPermissionMessage.getTeamIds() != null) {
            return revokeDocumentPermissionMessage
                    .getTeamIds()
                    .stream()
                    .map(teamId -> {
                        PermissionLevel targetPermission = documentPermissionService
                                .fetchTeamPermission(teamId, revokeDocumentPermissionMessage.getDocumentId())
                                .block();
                        if (targetPermission == null) {
                            return false;
                        }
                        return requesterPermission.isEqualOrHigherThan(targetPermission);
                    })
                    .allMatch(one -> one.equals(true));
        } else {
            return revokeDocumentPermissionMessage
                    .getAccountIds()
                    .stream()
                    .map(accountId -> {
                        PermissionLevel targetPermission = documentPermissionService
                                .fetchAccountPermission(accountId, revokeDocumentPermissionMessage.getDocumentId())
                                .block();
                        if (targetPermission == null) {
                            return false;
                        }
                        return requesterPermission.isEqualOrHigherThan(targetPermission);
                    })
                    .allMatch(one -> one.equals(true));

        }
    }

}
