package com.smartsparrow.rtm.message.handler;

import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.sso.service.LTIConsumerKey;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.sso.service.LTIv11Service;

public class LTIConsumerKeyCreateMessageHandler implements MessageHandler<EmptyReceivedMessage> {

    public static final String IAM_LTI_CONSUMER_KEY_CREATE = "iam.ltiConsumerKey.create";
    public static final String IAM_LTI_CONSUMER_KEY_CREATE_OK = "iam.ltiConsumerKey.create.ok";

    private final LTIv11Service ltIv11Service;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public LTIConsumerKeyCreateMessageHandler(LTIv11Service ltIv11Service,
                                              Provider<AuthenticationContext> authenticationContextProvider) {
        this.ltIv11Service = ltIv11Service;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void handle(Session session, EmptyReceivedMessage message) throws WriteResponseException {
        UUID subscriptionId = authenticationContextProvider.get().getAccount().getSubscriptionId();
        LTIConsumerKey ltiConsumerKey = ltIv11Service.createLTIConsumerKey(subscriptionId, null);
        emitSuccess(session, message.getId(), ltiConsumerKey);
    }

    private void emitSuccess(Session session, String inMessageId, LTIConsumerKey ltiConsumerKey)
            throws WriteResponseException {
        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(IAM_LTI_CONSUMER_KEY_CREATE_OK, inMessageId);
        basicResponseMessage.addField("ltiConsumerKey", ltiConsumerKey);
        Responses.write(session, basicResponseMessage);
    }

}
