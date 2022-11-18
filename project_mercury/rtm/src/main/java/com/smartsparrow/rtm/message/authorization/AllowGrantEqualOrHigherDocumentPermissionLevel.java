package com.smartsparrow.rtm.message.authorization;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.competency.service.DocumentPermissionService;
import com.smartsparrow.iam.data.permission.competency.AccountDocumentPermission;
import com.smartsparrow.iam.data.permission.competency.TeamDocumentPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.competency.GrantDocumentPermissionMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AllowGrantEqualOrHigherDocumentPermissionLevel implements AuthorizationPredicate<GrantDocumentPermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowGrantEqualOrHigherDocumentPermissionLevel.class);
    private final DocumentPermissionService documentPermissionService;

    @Inject
    public AllowGrantEqualOrHigherDocumentPermissionLevel(DocumentPermissionService documentPermissionService) {
        this.documentPermissionService = documentPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Higher permission level required";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, GrantDocumentPermissionMessage grantDocumentPermissionMessage) {
        Account requesterAccount = authenticationContext.getAccount();

        PermissionLevel requesterPermission = documentPermissionService
                .findHighestPermissionLevel(requesterAccount.getId(), grantDocumentPermissionMessage.getDocumentId())
                .block();
        if (requesterPermission == null) {
            return false;
        }

        List<UUID> notAllowedTargetPermission =
                getIdsWithHigherPermission(requesterPermission, grantDocumentPermissionMessage)
                        .block();

        if (notAllowedTargetPermission != null && !notAllowedTargetPermission.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Targets have higher permission level. Cannot override with permission {}",
                        grantDocumentPermissionMessage.getPermissionLevel());
            }
            return false;
        }
        return requesterPermission.isEqualOrHigherThan(grantDocumentPermissionMessage.getPermissionLevel());
    }

    /**
     * Find the team document permission for each team id in the list
     *
     * @param teamIds    the team ids to find the permissions for
     * @param documentId the document the permissions refer to
     * @return a flux of team document permission
     */
    protected Flux<TeamDocumentPermission> getTeamsPermissionLevelFor(List<UUID> teamIds, final UUID documentId) {
        return Flux.just(teamIds.toArray(new UUID[0]))
                .flatMap(teamId -> documentPermissionService.fetchTeamPermission(teamId, documentId)
                        .flatMap(permissionLevel -> Mono.just(new TeamDocumentPermission()
                                .setTeamId(teamId)
                                .setDocumentId(documentId)
                                .setPermissionLevel(permissionLevel))));
    }

    /**
     * Find the account document permission for each account in the list
     *
     * @param accountIds the account ids to find the permissions for
     * @param documentId the document the permissions refer to
     * @return a flux of account document permission
     */
    protected Flux<AccountDocumentPermission> getAccountsPermissionLevelFor(List<UUID> accountIds, final UUID documentId) {
        return Flux.just(accountIds.toArray(new UUID[0]))
                .flatMap(accountId -> documentPermissionService.fetchAccountPermission(accountId, documentId)
                        .flatMap(permissionLevel -> Mono.just(new AccountDocumentPermission()
                                .setAccountId(accountId)
                                .setDocumentId(documentId)
                                .setPermissionLevel(permissionLevel))));
    }

    /**
     * Get the list of teams/accounts that have higher permission.
     * The resulting is an unauthorized action
     *
     * @param permissionLevel PermissionLevel of the requester
     * @param message         incoming GrandDocumentPermissionMessage{@link GrantDocumentPermissionMessage}
     * @return list of ids whose permissions should not be overridden {@link Mono<List<UUID>>}
     */
    private Mono<List<UUID>> getIdsWithHigherPermission(PermissionLevel permissionLevel,
                                                        GrantDocumentPermissionMessage message) {
        if (message.getAccountIds() != null) {
            return getAccountsPermissionLevelFor(message.getAccountIds(), message.getDocumentId())
                    .filter(permission -> permissionLevel.isLowerThan(permission.getPermissionLevel()))
                    .map(AccountDocumentPermission::getAccountId)
                    .collectList();
        } else {
            return getTeamsPermissionLevelFor(message.getTeamIds(), message.getDocumentId())
                    .filter(permission -> permissionLevel.isLowerThan(permission.getPermissionLevel()))
                    .map(TeamDocumentPermission::getTeamId)
                    .collectList();
        }

    }
}
