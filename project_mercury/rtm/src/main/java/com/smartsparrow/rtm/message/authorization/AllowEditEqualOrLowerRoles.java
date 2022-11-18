package com.smartsparrow.rtm.message.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.recv.iam.EditRoleMessage;
import com.smartsparrow.util.Enums;

public class AllowEditEqualOrLowerRoles implements AuthorizationPredicate<EditRoleMessage> {

    private static final Logger log = LoggerFactory.getLogger(AllowEditEqualOrLowerRoles.class);

    @Override
    public String getErrorMessage() {
        return "Higher role required";
    }

    @Override
    public boolean test(AuthenticationContext authenticationContext, EditRoleMessage editRoleMessage) {

        Account account = authenticationContext.getAccount();
        // find the highest account role for the authenticated account
        AccountRole highestAccountRole = account.getRoles()
                .stream()
                .reduce((prev, next) -> {
                    if (prev.isEqualOrHigherThan(next)) {
                        return prev;
                    }
                    return next;
                })
                .orElse(null);

        // this should never occur however it is a possibility and it will be handled by not allowing the request to proceed
        if (highestAccountRole == null) {
            if (log.isWarnEnabled()) {
                log.warn("account {} has no account roles. User not allowed to proceed", account.getId());
            }
            return false;
        }

        AccountRole providedAccountRole = Enums.of(AccountRole.class, editRoleMessage.getRole());

        return highestAccountRole.isEqualOrHigherThan(providedAccountRole);
    }
}
