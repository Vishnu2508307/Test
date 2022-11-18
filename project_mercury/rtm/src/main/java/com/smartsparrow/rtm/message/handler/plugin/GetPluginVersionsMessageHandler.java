package com.smartsparrow.rtm.message.handler.plugin;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.plugin.data.PluginVersion;
import com.smartsparrow.plugin.payload.PluginVersionPayload;
import com.smartsparrow.plugin.semver.SemVersion;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.GetPluginVersionsMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.DateFormat;

public class GetPluginVersionsMessageHandler implements MessageHandler<GetPluginVersionsMessage> {

    public static final String WORKSPACE_PLUGIN_VERSION_LIST = "workspace.plugin.version.list";
    public static final String WORKSPACE_PLUGIN_VERSION_LIST_OK = "workspace.plugin.version.list.ok";
    public static final String WORKSPACE_PLUGIN_VERSION_LIST_ERROR = "workspace.plugin.version.list.error";

    private final PluginService pluginService;

    @Inject
    public GetPluginVersionsMessageHandler(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @Override
    public void handle(Session session, GetPluginVersionsMessage message) throws WriteResponseException {
        if (message.getPluginId() == null) {
            emitFailure(session, message.getId(), "pluginId is missing");
            return;
        }

        List<PluginVersionPayload> versions = pluginService.getPluginVersions(message.getPluginId())
                .map(this::mapVersionToDto)
                .collectList().block();

        BasicResponseMessage responseMessage = new BasicResponseMessage(WORKSPACE_PLUGIN_VERSION_LIST_OK, message.getId());
        responseMessage.addField("versions", versions);

        Responses.write(session, responseMessage);
    }

    private void emitFailure(Session session, String inMessageId, String details) throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(WORKSPACE_PLUGIN_VERSION_LIST_ERROR, inMessageId);
        responseMessage.addField("reason", "Unable to fetch plugin versions: " + details);
        responseMessage.setCode(Response.Status.BAD_REQUEST.getStatusCode());
        Responses.write(session, responseMessage);
    }

    private PluginVersionPayload mapVersionToDto(PluginVersion pluginVersion) {
        return new PluginVersionPayload()
                .setVersion(SemVersion.from(pluginVersion).toString())
                .setReleaseDate(DateFormat.asRFC1123(pluginVersion.getReleaseDate()))
                .setUnpublished(pluginVersion.getUnpublished());
    }
}
