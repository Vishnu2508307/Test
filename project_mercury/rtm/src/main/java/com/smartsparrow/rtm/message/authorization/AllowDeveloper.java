package com.smartsparrow.rtm.message.authorization;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.ReceivedMessage;

public class AllowDeveloper implements AuthorizationPredicate<ReceivedMessage> {

    @Override
    public boolean test(AuthenticationContext authenticationContext, ReceivedMessage receivedMessage) {
        Account account = authenticationContext.getAccount();
        return account != null && account.getRoles().stream()
                .anyMatch(one -> one.equals(AccountRole.DEVELOPER));
    }

    @Override
    public String getErrorMessage() {
        return "Forbidden";
    }
}
