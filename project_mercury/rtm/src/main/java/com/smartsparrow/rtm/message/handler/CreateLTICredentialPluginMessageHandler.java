package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.CreateLTIPluginCredentialMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class CreateLTICredentialPluginMessageHandler implements MessageHandler<CreateLTIPluginCredentialMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateLTICredentialPluginMessageHandler.class);

    public static final String LTI_CREDENTIAL_CREATE = "lti.credentials.create";
    private static final String LTI_CREDENTIAL_CREATE_OK = "lti.credentials.create.ok";
    private static final String LTI_CREDENTIAL_CREATE_ERROR = "lti.credentials.create.error";

    private final PluginService pluginService;

    @Inject
    public CreateLTICredentialPluginMessageHandler(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @Override
    public void validate(CreateLTIPluginCredentialMessage message) throws RTMValidationException {

        affirmArgumentNotNullOrEmpty(message.getKey(), "key is required");
        affirmArgumentNotNullOrEmpty(message.getSecret(), "secret is required");
        affirmNotNull(message.getPluginId(), "pluginId is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = LTI_CREDENTIAL_CREATE)
    @Override
    public void handle(Session session, CreateLTIPluginCredentialMessage message) {
        pluginService.createLTIProviderCredential(message.getKey(), message.getSecret(), message.getPluginId(),
                message.getWhiteListedFields())
                .doOnEach(log.reactiveDebugSignal("creating lti credentials"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(credential -> {
                            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                    LTI_CREDENTIAL_CREATE_OK,
                                    message.getId());
                            basicResponseMessage.addField("credentials", credential);
                            Responses.writeReactive(session, basicResponseMessage);
                        },
                        ex -> {
                            log.debug("error creating lti credentials", ex);
                            Responses.errorReactive(session, message.getId(), LTI_CREDENTIAL_CREATE_ERROR, ex);
                        }
                );
    }


}
