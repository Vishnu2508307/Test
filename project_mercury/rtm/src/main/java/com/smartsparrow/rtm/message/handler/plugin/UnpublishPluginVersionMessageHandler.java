package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.PluginVersionUnpublishMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class UnpublishPluginVersionMessageHandler implements MessageHandler<PluginVersionUnpublishMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UnpublishPluginVersionMessageHandler.class);
    private final PluginService pluginService;

    public static final String AUTHOR_PLUGIN_VERSION_UNPUBLISH = "author.plugin.version.unpublish";
    public static final String AUTHOR_PLUGIN_VERSION_UNPUBLISH_OK = "author.plugin.version.unpublish.ok";
    public static final String AUTHOR_PLUGIN_VERSION_UNPUBLISH_ERROR = "author.plugin.version.unpublish.error";

    @Inject
    public UnpublishPluginVersionMessageHandler(PluginService pluginService) { this.pluginService = pluginService; }

    @Override
    public void validate(PluginVersionUnpublishMessage message) throws RTMValidationException {
        affirmArgument(message.getPluginId() != null, "pluginId is required");
        affirmArgument(message.getMajor() != null, "major version is required");
        affirmArgument(message.getMinor() != null, "minor version is required");
        affirmArgument(message.getPatch() != null, "patch version is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_PLUGIN_VERSION_UNPUBLISH)
    @Override
    public void handle(Session session, PluginVersionUnpublishMessage message) throws WriteResponseException {
        pluginService.unPublishPluginVersion(message.getPluginId(),
                                             message.getMajor(),
                                             message.getMinor(),
                                             message.getPatch())
                .doOnEach(log.reactiveErrorThrowable("error while unpublishing the plugin version",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("pluginId", message.getPluginId());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(pluginSummary -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_PLUGIN_VERSION_UNPUBLISH_OK,
                                       message.getId());
                               basicResponseMessage.addField("pluginSummary", pluginSummary);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonDebug("Unable to unpublish the plugin version", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_PLUGIN_VERSION_UNPUBLISH_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to unpublish the plugin version");
                           }
                );

    }
}
