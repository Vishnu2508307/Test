package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.MethodNotAllowedFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.DeletePluginVersionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class DeletePluginVersionMessageHandler implements MessageHandler<DeletePluginVersionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeletePluginVersionMessageHandler.class);
    public final static String AUTHOR_PLUGIN_VERSION_DELETE = "author.plugin.version.delete";
    private final static String AUTHOR_PLUGIN_VERSION_DELETE_OK = "author.plugin.version.delete.ok";
    private final static String AUTHOR_PLUGIN_VERSION_DELETE_ERROR = "author.plugin.version.delete.error";

    private final PluginService pluginService;

    @Inject
    public DeletePluginVersionMessageHandler(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @Override
    public void validate(DeletePluginVersionMessage message) throws RTMValidationException {
        affirmNotNull(message.getPluginId(), "pluginId is required");
        affirmNotNull(message.getVersion(), "plugin version is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_PLUGIN_VERSION_DELETE)
    @Override
    public void handle(Session session, DeletePluginVersionMessage message) throws WriteResponseException {
        pluginService.deletePluginVersion(message.getPluginId(), message.getVersion())
                .doOnEach(log.reactiveErrorThrowable("Error while deleting the plugin version",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("message", message.toString());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(aVoid -> {
                               // nothing should happen here
                           },
                           ex -> {
                               log.jsonDebug("Unable to delete plugin version", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               if (ex instanceof MethodNotAllowedFault) {
                                   Responses.errorReactive(session, message.getId(), AUTHOR_PLUGIN_VERSION_DELETE_ERROR,
                                                           HttpStatus.SC_METHOD_NOT_ALLOWED, ex.getMessage());
                               }
                               else {
                                   Responses.errorReactive(session, message.getId(), AUTHOR_PLUGIN_VERSION_DELETE_ERROR,
                                                           HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                           "Unable to delete plugin version");
                               }
                           },
                           () -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_PLUGIN_VERSION_DELETE_OK,
                                       message.getId());
                               Responses.writeReactive(session, basicResponseMessage);
                           });
    }
}
