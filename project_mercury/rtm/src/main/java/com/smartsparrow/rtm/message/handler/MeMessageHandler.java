package com.smartsparrow.rtm.message.handler;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.MeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.sso.service.IESService;
import com.smartsparrow.sso.service.MyCloudService;

public class MeMessageHandler implements MessageHandler<MeMessage> {

    public static final String ME_GET = "me.get";
    private static final String ME_GET_OK = "me.get.ok";
    private static final String ME_GET_ERROR = "me.get.error";

    private Provider<MutableAuthenticationContext> authenticationContextProvider;
    private final AccountService accountService;
    private final IESService iesService;
    private final MyCloudService mycloudService;

    @Inject
    MeMessageHandler(Provider<MutableAuthenticationContext> authenticationContextProvider,
                     AccountService accountService,
                     IESService iesService,
                     MyCloudService mycloudService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.accountService = accountService;
        this.iesService = iesService;
        this.mycloudService = mycloudService;
    }

    @Override
    public void handle(Session session, MeMessage message) throws WriteResponseException {
        MutableAuthenticationContext authCtx = authenticationContextProvider.get();

        //
        Account _account = authCtx.getAccount();
        if (_account == null) {
            // user is not authenticated
            emitError(session, message.getId());
            return;
        }

        // TODO the WebTokenType enum should be driving the retrieval of account identity
        AuthenticationType authenticationType = authCtx.getAuthenticationType();
        switch (authenticationType) {
            case IES: {
                emitSuccess(session, message, iesService
                        .getAccountPayload(authCtx.getPearsonUid(), authCtx.getPearsonToken(), _account)
                        .block());
                break;
            }
            case MYCLOUD: {
                emitSuccess(session, message, mycloudService
                        .getAccountPayload(authCtx.getPearsonUid(), authCtx.getPearsonToken(), _account)
                        .block());
                break;
            }
            case BRONTE: {
                emitSuccess(session, message, accountService.getAccountPayload(_account).block());
                break;
            }
            default: {
                throw new IllegalArgumentFault(String.format("authenticationType %s not supported", authenticationType));
            }
        }

    }

    private void emitSuccess(Session session, MeMessage message, AccountPayload accountPayload) throws WriteResponseException {
        BasicResponseMessage response = new BasicResponseMessage(ME_GET_OK, message.getId());
        response.addField("account", accountPayload);
        Responses.write(session, response);
    }

    private void emitError(Session session, String inMessageId) throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(ME_GET_ERROR, inMessageId);
        responseMessage.addField("reason", "Unauthorized");
        responseMessage.setCode(Response.Status.UNAUTHORIZED.getStatusCode());
        Responses.write(session, responseMessage);
    }
}
