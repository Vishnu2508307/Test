package com.smartsparrow.rtm.message.handler.asset;


import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.WorkspaceAssetService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.asset.UpdateAssetMetadataMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class UpdateAssetMetadataMessageHandler implements MessageHandler<UpdateAssetMetadataMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UpdateAssetMetadataMessageHandler.class);

    public static final String AUTHOR_ASSET_METADATA_UPDATE = "author.asset.metadata.update";
    public static final String AUTHOR_ASSET_METADATA_UPDATE_OK = "author.asset.metadata.update.ok";
    public static final String AUTHOR_ASSET_METADATA_UPDATE_ERROR = "author.asset.metadata.update.error";

    private final WorkspaceAssetService workspaceAssetService;

    @Inject
    public UpdateAssetMetadataMessageHandler(final WorkspaceAssetService workspaceAssetService) {
        this.workspaceAssetService = workspaceAssetService;
    }

    @Override
    public void validate(UpdateAssetMetadataMessage message) throws RTMValidationException {
        affirmArgument(message.getAssetUrn() != null, "assetUrn is required");
        affirmArgument(message.getKey() != null, "metadata key is missing");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ASSET_METADATA_UPDATE)
    @Override
    public void handle(Session session, UpdateAssetMetadataMessage message) throws WriteResponseException {

        workspaceAssetService.updateAssetMetadata(message.getAssetUrn(),
                                                  message.getKey(),
                                                  message.getValue())
                .doOnEach(log.reactiveErrorThrowable("error updating asset metadata",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("assetUrn", message.getAssetUrn());
                                                             put("key", message.getKey());
                                                         }
                                                     }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(assetMetadata -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_ASSET_METADATA_UPDATE_OK,
                                       message.getId());
                               basicResponseMessage.addField("assetMetadata", assetMetadata);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonDebug("Unable to update asset metadata", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ASSET_METADATA_UPDATE_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to update asset metadata");
                           }
                );

    }
}
