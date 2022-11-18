package com.smartsparrow.rtm.message.authorization;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.data.permission.subscription.AccountSubscriptionPermission;
import com.smartsparrow.iam.data.permission.subscription.TeamSubscriptionPermission;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.iam.GrantSubscriptionPermissionMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AllowGrantEqualOrHigherSubscriptionPermission implements AuthorizationPredicate<GrantSubscriptionPermissionMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowGrantEqualOrHigherSubscriptionPermission.class);

    private final SubscriptionPermissionService subscriptionPermissionService;

    @Inject
    public AllowGrantEqualOrHigherSubscriptionPermission(SubscriptionPermissionService subscriptionPermissionService) {
        this.subscriptionPermissionService = subscriptionPermissionService;
    }

    @Override
    public String getErrorMessage() {
        return "Higher permission level required";
    }

    /**
     * Check that the requesting account has higher permission level over the subscriptionId supplied in the message
     * than any accountId/teamId the permission should be granted to. If any accountId/teamId in the list is found
     * to have an higher permission level than the requesting account than the authorizer returns <code>false</code>
     *
     * @param authenticationContext the context containing the account performing the request
     * @param message the webSocket message
     * @return <code>true</code> if the request is permitted or <code>false</code> when the request is not permitted
     */
    @Override
    public boolean test(AuthenticationContext authenticationContext, GrantSubscriptionPermissionMessage message) {
        Account account = authenticationContext.getAccount();

        final PermissionLevel requestingPermission = subscriptionPermissionService
                .findHighestPermissionLevel(account.getId(), message.getSubscriptionId())
                .block();

        if (requestingPermission == null) {
            return false;
        }

        List<UUID> notAllowedTargetPermission = getNotPermitted(message, requestingPermission)
                .block();

        if (notAllowedTargetPermission != null && !notAllowedTargetPermission.isEmpty()) {
            String entity = message.getAccountIds() != null ? "accounts" : "teams";
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s %s have an higher permission level over subscription %s that cannot be overridden by %s",
                        entity,
                        notAllowedTargetPermission.toString(),
                        message.getSubscriptionId(),
                        account.getId()));
            }
            // at least one account in the list has an higher permission level than the requesting account
            return false;
        }

        // all accounts in the list have either no permission at all or a lower permission than the requesting account
        return requestingPermission.isEqualOrHigherThan(message.getPermissionLevel());

    }

    /**
     * Get a list of UUID representing entities id that have an higher permission level than the requester. The id could
     * either represent an accountId or a teamId depending upon what the client supplied in the message.
     *
     * @param message the incoming webSocket message
     * @param requestingPermission the requesting account permission
     * @return a mono list of ids that are not allowed to be overridden
     */
    private Mono<List<UUID>> getNotPermitted(GrantSubscriptionPermissionMessage message, PermissionLevel requestingPermission) {
        if (message.getAccountIds() != null) {
            return getAccountsPermissionLevelFor(message.getAccountIds(), message.getSubscriptionId())
                    .filter(permission -> requestingPermission.isLowerThan(permission.getPermissionLevel()))
                    .map(AccountSubscriptionPermission::getAccountId)
                    .collectList();
        } else {
            return getTeamsPermissionLevelFor(message.getTeamIds(), message.getSubscriptionId())
                    .filter(permission -> requestingPermission.isLowerThan(permission.getPermissionLevel()))
                    .map(TeamSubscriptionPermission::getTeamId)
                    .collectList();
        }
    }

    /**
     * Find the account permission level for a list of accounts over a subscription
     *
     * @param accountIds the account ids to find the highest permission for
     * @param subscriptionId the subscription the permission refers to
     * @return a flux of account subscription permissions
     */
    private Flux<AccountSubscriptionPermission> getAccountsPermissionLevelFor(List<UUID> accountIds, final UUID subscriptionId) {
        return Flux.just(accountIds.toArray(new UUID[0]))
                .flatMap(accountId -> subscriptionPermissionService.findAccountPermission(accountId, subscriptionId));
    }

    /**
     * Find the team permission level for a list of teams over a subscription
     *
     * @param teamIds the list of team ids to find the permission for
     * @param subscriptionId the subscription the permission refers to
     * @return a flux of team subscription permission
     */
    private Flux<TeamSubscriptionPermission> getTeamsPermissionLevelFor(List<UUID> teamIds, final UUID subscriptionId) {
        return Flux.just(teamIds.toArray(new UUID[0]))
                .flatMap(teamId -> subscriptionPermissionService.findTeamPermission(teamId, subscriptionId)
                        .flatMap(permissionLevel -> Mono.just(new TeamSubscriptionPermission()
                                .setTeamId(teamId)
                                .setPermissionLevel(permissionLevel)
                                .setSubscriptionId(subscriptionId))));
    }
}
