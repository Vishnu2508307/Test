package com.smartsparrow.rtm.message.handler.plugin;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.plugin.lang.PluginLogException;
import com.smartsparrow.plugin.service.PluginLogService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.plugin.PluginLogConfigUpdateMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class PluginLogConfigUpdateMessageHandler implements MessageHandler<PluginLogConfigUpdateMessage> {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(PluginLogConfigUpdateMessageHandler.class);

    public static final String PLUGIN_LOG_CONFIG_UPDATE = "plugin.log.config.update";
    protected static final String PLUGIN_LOG_CONFIG_UPDATE_OK = "plugin.log.config.update.ok";
    protected static final String PLUGIN_LOG_CONFIG_UPDATE_ERROR = "plugin.log.config.update.error";

    public static final Long MIN_RECORD_PER_BUCKET = 2L;
    public static final Long MAX_RECORD_PER_BUCKET = 50000L;

    public static final Long MIN_BUCKET_PER_TABLE = 2L;
    public static final Long MAX_BUCKET_PER_TABLE = 10L;

    private final PluginLogService pluginLogService;

    @Inject
    public PluginLogConfigUpdateMessageHandler(PluginLogService pluginLogService) {
        this.pluginLogService = pluginLogService;
    }

    @Override
    public void validate(PluginLogConfigUpdateMessage message) throws RTMValidationException {
        affirmNotNull(message.getTableName(), "missing tableName");
        affirmNotNull(message.getEnabled(), "missing enabled");

        affirmArgument(message.getMaxRecordCount() >= MIN_RECORD_PER_BUCKET,
                       "maxRecordCount is less than or equal " + MIN_RECORD_PER_BUCKET);
        affirmArgument(message.getMaxRecordCount() <= MAX_RECORD_PER_BUCKET,
                       "maxRecordCount is more than or equal " + MAX_RECORD_PER_BUCKET);

        affirmArgument(message.getLogBucketInstances() >= MIN_BUCKET_PER_TABLE,
                       "logBucketInstances is less than or equal " + MIN_BUCKET_PER_TABLE);
        affirmArgument(message.getLogBucketInstances() <= MAX_BUCKET_PER_TABLE,
                       "logBucketInstances is more than or equal " + MAX_BUCKET_PER_TABLE);
    }

    /**
     * @param session the websocket session
     * @param message the newly arrived message
     * @throws WriteResponseException
     */
    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, PluginLogConfigUpdateMessage message) throws WriteResponseException {
        try {
            pluginLogService.updatePluginLogConfig(message.getTableName(),
                                                   message.getEnabled(),
                                                   message.getMaxRecordCount(),
                                                   message.getRetentionPolicy(),
                                                   message.getLogBucketInstances())
                    .doOnEach(logger.reactiveInfo("Updating plugin log config"))
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .subscribe(pluginLogConfig -> {
                                   BasicResponseMessage responseMessage = new BasicResponseMessage(PLUGIN_LOG_CONFIG_UPDATE_OK,
                                                                                                   message.getId());
                                   Responses.writeReactive(session, responseMessage);
                               },
                               ex -> {
                                   logger.jsonError("Unable to update PluginLogConfig",
                                                    new HashMap<String, Object>() {
                                                        {
                                                            put("message", message.toString());
                                                            put("error", ex.getStackTrace());
                                                        }
                                                    }, ex);
                                   Responses.errorReactive(session,
                                                           message.getId(),
                                                           PLUGIN_LOG_CONFIG_UPDATE_ERROR,
                                                           HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                           "error updating the PluginLogConfig");
                               });
        } catch (PluginLogException exception) {
            logger.jsonError("Error finding tableName", new HashMap<String, Object>() {
                {
                    put("tableName", message.getTableName());
                    put("error", exception.getStackTrace());
                }
            }, exception);
            emitFailure(session, message.getId(), exception.toString());
        }

    }

    private void emitFailure(Session session, String inMessageId, String details) throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(PLUGIN_LOG_CONFIG_UPDATE_ERROR, inMessageId);
        responseMessage.setCode(HttpStatus.SC_BAD_REQUEST);
        responseMessage.addField("reason", "Unable to find tableName: " + details);

        Responses.write(session, responseMessage);
    }

}
