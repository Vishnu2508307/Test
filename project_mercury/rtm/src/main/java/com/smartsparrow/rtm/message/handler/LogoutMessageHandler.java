package com.smartsparrow.rtm.message.handler;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.BearerToken;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.service.WebSessionToken;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.LogoutMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

public class LogoutMessageHandler implements MessageHandler<LogoutMessage> {

    private static final Logger logger = LoggerFactory.getLogger(LogoutMessageHandler.class);

    public static final String ME_LOGOUT = "me.logout";
    public static final String ME_LOGOUT_OK = "me.logout.ok";
    public static final String ME_LOGOUT_ERROR = "me.logout.error";

    private final CredentialService credentialService;
    private final Provider<MutableAuthenticationContext> authenticationContextProvider;

    @Inject
    public LogoutMessageHandler(CredentialService credentialService,
                                Provider<MutableAuthenticationContext> authenticationContextProvider) {
        this.credentialService = credentialService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void handle(Session session, LogoutMessage message) throws WriteResponseException {
        String token = message.getBearerToken();

        if (logger.isDebugEnabled()) {
            logger.debug("invalidate token '{}'", token);
        }

        if (Strings.isNullOrEmpty(token)) {
            emitFailure(session, message.getId(), "bearerToken is required");
            return;
        }

        Account account = authenticationContextProvider.get().getAccount();

        /*we need to check that supplied token is valid (is not expired).
        there are two options:
        1. check that token exists in bearer token table
        2. check expiration date on web session token
        I chose the first option for now. can be changed later
        */
        BearerToken bearerToken = credentialService.findBearerToken(token);

        if (bearerToken == null) {
            emitFailure(session, message.getId(), "supplied bearer token is not valid");
            return;
        }

        WebSessionToken webToken = credentialService.findWebSessionToken(token);

        if (webToken == null || !webToken.getAccountId().equals(account.getId())) {
            emitFailure(session, message.getId(), String.format("token '%s' doesn't belong to the current user", token));
            return;
        }

        credentialService.invalidate(webToken);

        authenticationContextProvider.get().setAccount(null);

        emitSuccess(session, message.getId());
    }

    private void emitSuccess(Session session, String inMessageId)
            throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(ME_LOGOUT_OK, inMessageId);

        Responses.write(session, responseMessage);
    }

    private void emitFailure(Session session, String inMessageId, String reason) throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(ME_LOGOUT_ERROR, inMessageId);
        responseMessage.addField("reason", "Unable to logout: " + reason);
        responseMessage.setCode(Response.Status.BAD_REQUEST.getStatusCode());
        Responses.write(session, responseMessage);
    }

}
