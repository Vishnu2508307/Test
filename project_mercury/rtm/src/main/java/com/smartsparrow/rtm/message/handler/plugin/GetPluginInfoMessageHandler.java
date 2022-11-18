package com.smartsparrow.rtm.message.handler.plugin;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

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
import com.smartsparrow.rtm.message.recv.plugin.GetPluginInfoMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.message.send.ErrorMessage;
import com.smartsparrow.rtm.util.Responses;

public class GetPluginInfoMessageHandler implements MessageHandler<GetPluginInfoMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetPluginInfoMessageHandler.class);

    public static final String WORKSPACE_PLUGIN_GET = "workspace.plugin.get";
    private static final String WORKSPACE_PLUGIN_GET_OK = "workspace.plugin.get.ok";
    private static final String WORKSPACE_PLUGIN_GET_ERROR = "workspace.plugin.get.error";

    private final PluginService pluginService;

    @Inject
    public GetPluginInfoMessageHandler(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @Override
    public void validate(GetPluginInfoMessage message) throws RTMValidationException {
        if (message.getPluginId() == null) {
            throw new RTMValidationException("pluginId is required", message.getId(), WORKSPACE_PLUGIN_GET_ERROR);
        }
    }

    @Override
    public void handle(Session session, GetPluginInfoMessage message) throws WriteResponseException {
        PluginPayload plugin;
        try {
            plugin = pluginService.findPluginInfo(message.getPluginId(), message.getVersion()).block();
        } catch (PluginNotFoundFault | VersionParserFault e) {
            log.jsonDebug("error retrieving plugin info", new HashMap<String, Object>() {
                {
                    put("pluginId",message.getPluginId());
                    put("error",e.getStackTrace());
                }
            });
            int code = (e instanceof PluginNotFoundFault) ? HttpStatus.SC_NOT_FOUND : HttpStatus.SC_BAD_REQUEST;
            emitFailure(session, message.getId(), message.getPluginId(), e.getMessage(), code);
            return;
        }

        BasicResponseMessage responseMessage = new BasicResponseMessage(WORKSPACE_PLUGIN_GET_OK, message.getId());
        responseMessage.addField("plugin", plugin);

        Responses.write(session, responseMessage);

    }

    private void emitFailure(Session session, String replyTo, UUID pluginId, String details, int code)
            throws WriteResponseException {
        ErrorMessage responseMessage = new ErrorMessage(WORKSPACE_PLUGIN_GET_ERROR);
        responseMessage.setCode(code);
        responseMessage.setMessage(String.format("Unable to fetch plugin info for pluginId '%s': %s", pluginId, details));
        responseMessage.setReplyTo(replyTo);

        Responses.write(session, responseMessage);
    }
}
