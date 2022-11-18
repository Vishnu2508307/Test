package com.smartsparrow.rtm.message.handler.plugin;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.payload.PluginPayload;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.GetPublishedPluginMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import java.util.HashMap;

public class GetPublishedPluginMessageHandler implements MessageHandler<GetPublishedPluginMessage> {

    public static final String AUTHOR_PLUGIN_GET = "author.plugin.get";
    private static final String AUTHOR_PLUGIN_GET_OK = "author.plugin.get.ok";
    private static final String AUTHOR_PLUGIN_GET_ERROR = "author.plugin.get.error";

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetPublishedPluginMessageHandler.class);

    private final PluginService pluginService;

    @Inject
    public GetPublishedPluginMessageHandler(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_PLUGIN_GET)
    @Override
    public void handle(Session session, GetPublishedPluginMessage message) throws WriteResponseException {

        PluginPayload plugin;
        try {

            if (message.getView() != null) {
                plugin = pluginService.findPluginByIdAndView(message.getPluginId(), message.getView(), message.getVersion())
                        .doOnEach(ReactiveTransaction.linkOnNext())
                        .doOnEach(ReactiveTransaction.expireOnComplete())
                        .subscriberContext(ReactiveMonitoring.createContext())
                        .block();
            } else {
                plugin = pluginService.findPlugin(message.getPluginId(), message.getVersion())
                        .doOnEach(ReactiveTransaction.linkOnNext())
                        .doOnEach(ReactiveTransaction.expireOnComplete())
                        .subscriberContext(ReactiveMonitoring.createContext())
                        .block();
            }

        } catch (PluginNotFoundFault | VersionParserFault e) {
            log.jsonDebug("error fetching published plugin", new HashMap<String, Object>() {
                {
                    put("pluginId", message.getPluginId());
                    put("view", message.getView());
                    put("version", message.getVersion());
                    put("error", e.getStackTrace());
                }
            });
            emitFailure(session, message.getId(), e.getMessage());
            return;
        }

        BasicResponseMessage responseMessage = new BasicResponseMessage(AUTHOR_PLUGIN_GET_OK, message.getId());
        responseMessage.addField("plugin", plugin);

        Responses.write(session, responseMessage);
    }

    @Override
    public void validate(GetPublishedPluginMessage message) throws RTMValidationException {
        if (message.getPluginId() == null) {
            throw new RTMValidationException("pluginId is required", message.getId(), AUTHOR_PLUGIN_GET_ERROR);
        }
    }

    private void emitFailure(Session session, String inMessageId, String details) throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(AUTHOR_PLUGIN_GET_ERROR, inMessageId);
        responseMessage.setCode(HttpStatus.SC_BAD_REQUEST);
        responseMessage.addField("reason", "Unable to fetch plugin: " + details);

        Responses.write(session, responseMessage);
    }
}
