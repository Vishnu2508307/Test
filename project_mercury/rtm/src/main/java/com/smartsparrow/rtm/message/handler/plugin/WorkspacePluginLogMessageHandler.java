package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.WorkspacePluginLogMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class WorkspacePluginLogMessageHandler implements MessageHandler<WorkspacePluginLogMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(WorkspacePluginLogMessageHandler.class);

    public final static String WORKSPACE_PLUGIN_LOG = "workspace.plugin.log";
    public final static String WORKSPACE_PLUGIN_LOG_OK = "workspace.plugin.log.ok";
    public final static String WORKSPACE_PLUGIN_LOG_ERROR = "workspace.plugin.log.error";

    @Inject
    public WorkspacePluginLogMessageHandler() {}

    @Override
    public void validate(WorkspacePluginLogMessage message) throws RTMValidationException {
        affirmArgument(message.getPluginId() != null, "missing plugin id");
        affirmArgument(message.getVersion() != null, "missing version");
        affirmArgument(message.getLevel() != null, "missing level");
        affirmArgument(message.getMessage() != null, "missing message");
        affirmArgument(message.getArgs() != null, "missing args");
        affirmArgument(message.getPluginContext() != null, "missing plugin context");
        affirmArgument(message.getElementId() != null, "missing element id");
        affirmArgument(message.getElementType() != null, "missing element type");
        affirmArgument(message.getProjectId() != null, "missing project id");
        affirmArgument(message.getEventId() != null, "missing event id");
    }

    /**
     * Log the workspace plugin
     *
     * @param session the websocket session
     * @param message the newly arrived WorkspacePluginLogMessage message
     * @throws WriteResponseException when failing to write to the websocket
     */
    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, WorkspacePluginLogMessage message) throws WriteResponseException {
        Map<String, Object> fieldsMap = new HashedMap<>() {
            {
                put("pluginId", message.getPluginId());
                put("version", message.getVersion());
                put("level", message.getLevel());
                put("message", message.getMessage());
                put("args", message.getArgs());
                put("pluginContext", message.getPluginContext());
                put("elementId", message.getElementId());
                put("elementType", message.getElementType());
                put("projectId", message.getProjectId());
                put("eventId", message.getEventId());
                put("transactionId", message.getTransactionId());
                put("transactionName", message.getTransactionName());
                put("transactionSequence", message.getTransactionSequence());
                put("segmentId", message.getSegmentId());
                put("segmentName", message.getSegmentName());
            }
        };
        try {
            logger.jsonDebug("Workspace Plugin Log message", fieldsMap);
            emitSuccess(session, message.getId());
        } catch (Exception e) {
            logger.jsonError("Exception while logging workspace plugin log message", fieldsMap, e);
            emitFailure(session, message.getId(), e.getMessage());
        }
    }

    private void emitFailure(Session session, String inMessageId, String details) throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(WORKSPACE_PLUGIN_LOG_ERROR, inMessageId);
        responseMessage.setCode(HttpStatus.SC_BAD_REQUEST);
        responseMessage.addField("reason", "Unable to find WORKSPACE_LOG_STATEMENT_BY_PLUGIN: " + details);

        Responses.write(session, responseMessage);
    }

    private void emitSuccess(Session session, String inMessageId) throws WriteResponseException {
        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(WORKSPACE_PLUGIN_LOG_OK, inMessageId);
        Responses.write(session, basicResponseMessage);
    }
}
