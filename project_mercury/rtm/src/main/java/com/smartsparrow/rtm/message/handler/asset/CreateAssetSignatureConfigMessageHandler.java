package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import javax.inject.Inject;

import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.service.AssetSignatureService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.asset.CreateAssetSignatureConfigMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class CreateAssetSignatureConfigMessageHandler implements MessageHandler<CreateAssetSignatureConfigMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateAssetSignatureConfigMessageHandler.class);

    public static final String ASSET_SIGNATURE_CONFIG_CREATE = "asset.signature.config.create";
    private static final String ASSET_SIGNATURE_CONFIG_CREATE_OK = "asset.signature.config.create.ok";
    private static final String ASSET_SIGNATURE_CONFIG_CREATE_ERROR = "asset.signature.config.create.error";

    private final AssetSignatureService assetSignatureService;

    @Inject
    public CreateAssetSignatureConfigMessageHandler(AssetSignatureService assetSignatureService) {
        this.assetSignatureService = assetSignatureService;
    }

    @Override
    public void validate(CreateAssetSignatureConfigMessage message) throws RTMValidationException {
        affirmArgumentNotNullOrEmpty(message.getHost(), "host is required");
        affirmArgument(message.getPath() != null, "path is required");
        affirmArgumentNotNullOrEmpty(message.getConfig(), "config is required");
        affirmArgument(message.getStrategyType() != null, "signatureStrategy is required");
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, CreateAssetSignatureConfigMessage message) throws WriteResponseException {

        assetSignatureService.create(message.getHost(), message.getPath(), message.getConfig(), message.getStrategyType())
                .doOnEach(log.reactiveInfo("creating asset signature config"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(assetSignature -> {
                    Responses.writeReactive(session, new BasicResponseMessage(ASSET_SIGNATURE_CONFIG_CREATE_OK, message.getId())
                            .addField("assetSignature", assetSignature));
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), ASSET_SIGNATURE_CONFIG_CREATE_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error creating the asset signature config");
                });

    }
}
