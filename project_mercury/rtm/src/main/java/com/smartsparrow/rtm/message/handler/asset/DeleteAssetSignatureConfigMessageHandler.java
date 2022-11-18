package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.service.AssetSignatureService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.asset.DeleteAssetSignatureConfigMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class DeleteAssetSignatureConfigMessageHandler implements MessageHandler<DeleteAssetSignatureConfigMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteAssetSignatureConfigMessageHandler.class);

    public static final String ASSET_SIGNATURE_CONFIG_DELETE = "asset.signature.config.delete";
    private static final String ASSET_SIGNATURE_CONFIG_DELETE_OK = "asset.signature.config.delete.ok";
    private static final String ASSET_SIGNATURE_CONFIG_DELETE_ERROR = "asset.signature.config.delete.error";

    private final AssetSignatureService assetSignatureService;

    @Inject
    public DeleteAssetSignatureConfigMessageHandler(AssetSignatureService assetSignatureService) {
        this.assetSignatureService = assetSignatureService;
    }

    @Override
    public void validate(DeleteAssetSignatureConfigMessage message) throws RTMValidationException {
        affirmArgumentNotNullOrEmpty(message.getHost(), "host is required");
        affirmArgument(message.getPath() != null, "path is required");
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, DeleteAssetSignatureConfigMessage message) throws WriteResponseException {
        assetSignatureService.delete(message.getHost(), message.getPath())
                .doOnEach(log.reactiveInfoSignal("deleting asset signature config"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(aVoid -> {
                    // do nothing here
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), ASSET_SIGNATURE_CONFIG_DELETE_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting the asset signature config");
                }, () -> {
                    Responses.writeReactive(session, new BasicResponseMessage(ASSET_SIGNATURE_CONFIG_DELETE_OK, message.getId()));
        });
    }
}
