package com.smartsparrow.rtm.message.handler;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.DeveloperKey;
import com.smartsparrow.iam.service.DeveloperKeyService;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

public class DeveloperKeyProvisionMessageHandler implements MessageHandler<ReceivedMessage> {

    public final static String IAM_DEVKEY_CREATE = "iam.developerKey.create";
    public final static String IAM_DEVKEY_CREATE_OK = "iam.developerKey.create.ok";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final DeveloperKeyService developerKeyService;

    @Inject
    public DeveloperKeyProvisionMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                               DeveloperKeyService developerKeyService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.developerKeyService = developerKeyService;
    }

    @Override
    public void handle(Session session, ReceivedMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();
        DeveloperKey developerKey = developerKeyService.createKey(account.getSubscriptionId(), account.getId());
        BasicResponseMessage responseMessage = new BasicResponseMessage(IAM_DEVKEY_CREATE_OK, message.getId());
        responseMessage.addField("developerKey", developerKey);
        Responses.write(session, responseMessage);
    }
}
