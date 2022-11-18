package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.UpdatePluginMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

public class UpdatePluginMessageHandler implements MessageHandler<UpdatePluginMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UpdatePluginMessageHandler.class);

    public static final String WORKSPACE_PLUGIN_UPDATE = "workspace.plugin.update";
    public static final String WORKSPACE_PLUGIN_UPDATE_OK = "workspace.plugin.update.ok";
    public static final String WORKSPACE_PLUGIN_UPDATE_ERROR = "workspace.plugin.update.error";

    private final PluginService pluginService;

    @Inject
    public UpdatePluginMessageHandler(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @Override
    public void validate(UpdatePluginMessage message) throws RTMValidationException {
        affirmArgument(message.getPluginId() != null, "missing pluginId");
        affirmArgument(message.getPublishMode() != null, "missing publish mode");
    }

    @Override
    public void handle(Session session, UpdatePluginMessage message) throws WriteResponseException {

        pluginService.updatePluginSummary(message.getPluginId(),
                                          message.getPublishMode())
                .doOnEach(log.reactiveErrorThrowable("error updating plugin",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("pluginId", message.getPluginId());
                                                         }
                                                     }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(pluginSummary -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       WORKSPACE_PLUGIN_UPDATE_OK,
                                       message.getId());
                               basicResponseMessage.addField("pluginSummary", pluginSummary);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonDebug("Unable to update plugin", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       WORKSPACE_PLUGIN_UPDATE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to update plugin");
                           }
                );
    }
}
