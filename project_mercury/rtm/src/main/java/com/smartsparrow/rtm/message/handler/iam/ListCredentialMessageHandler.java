package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.CredentialMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

public class ListCredentialMessageHandler implements MessageHandler<CredentialMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListCredentialMessageHandler.class);

    public static final String IAM_CREDENTIAL_LIST = "iam.credential.list";
    public static final String IAM_CREDENTIAL_LIST_OK = "iam.credential.list.ok";
    public static final String IAM_CREDENTIAL_LIST_ERROR = "iam.credential.list.error";

    private final CredentialService credentialService;

    @Inject
    public ListCredentialMessageHandler(final CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @Override
    public void validate(CredentialMessage message) throws RTMValidationException {
        affirmArgument(message.getEmail() != null, "missing email");
    }

    @Override
    public void handle(Session session, CredentialMessage message) throws WriteResponseException {

        credentialService.fetchCredentialTypeByHash(message.getEmail())
                .doOnEach(log.reactiveErrorThrowable("error fetching credential types ",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("email", message.getEmail());
                                                         }
                                                     }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .collectList()
                .subscribe(credentialTypes -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       IAM_CREDENTIAL_LIST_OK,
                                       message.getId());
                               basicResponseMessage.addField("credentialTypes", credentialTypes);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonDebug("Unable to fetch credential types", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       IAM_CREDENTIAL_LIST_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to fetch credential types");
                           }
                );
    }
}
