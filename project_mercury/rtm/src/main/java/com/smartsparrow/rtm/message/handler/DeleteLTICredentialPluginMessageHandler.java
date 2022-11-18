package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.LTIPluginMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class DeleteLTICredentialPluginMessageHandler implements MessageHandler<LTIPluginMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteLTICredentialPluginMessageHandler.class);

    public static final String LTI_CREDENTIAL_DELETE = "lti.credentials.delete";
    public static final String LTI_CREDENTIAL_DELETE_OK = "lti.credentials.delete.ok";
    private static final String LTI_CREDENTIAL_DELETE_ERROR = "lti.credentials.delete.error";

    private final PluginService pluginService;

    @Inject
    public DeleteLTICredentialPluginMessageHandler(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @Override
    public void validate(LTIPluginMessage message) throws RTMValidationException {
        affirmArgumentNotNullOrEmpty(message.getKey(), "key is required");
        affirmNotNull(message.getPluginId(), "pluginId is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = LTI_CREDENTIAL_DELETE)
    @Override
    public void handle(Session session, LTIPluginMessage message) {
        pluginService.deleteLTIProviderCredential(message.getKey(), message.getPluginId())
                .doOnEach(log.reactiveDebugSignal("deleting lti credentials"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(aVoid -> {
                    // nothing to do here
                }, ex -> {
                    log.debug("error deleting the credentials");
                    Responses.errorReactive(session, message.getId(), LTI_CREDENTIAL_DELETE_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                            "error deleting the credentials");

                }, () -> {
                    Responses.writeReactive(session, new BasicResponseMessage(LTI_CREDENTIAL_DELETE_OK, message.getId()));
                });
    }

}
