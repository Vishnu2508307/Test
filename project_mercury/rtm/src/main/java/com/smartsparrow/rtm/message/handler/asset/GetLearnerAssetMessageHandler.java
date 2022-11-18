package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.service.AssetService;
import com.smartsparrow.learner.service.LearnerAssetService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.GetAssetMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class GetLearnerAssetMessageHandler implements MessageHandler<GetAssetMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetLearnerAssetMessageHandler.class);

    public static final String LEARNER_ASSET_GET = "learner.asset.get";
    private static final String LEARNER_ASSET_GET_OK = "learner.asset.get.ok";
    private static final String LEARNER_ASSET_GET_ERROR = "learner.asset.get.error";

    private final AssetService assetService;

    @Inject
    public GetLearnerAssetMessageHandler(@Named("LearnerAssetService") AssetService assetService) {
        this.assetService = assetService;
    }

    @Override
    public void validate(GetAssetMessage message) throws RTMValidationException {
        affirmArgumentNotNullOrEmpty(message.getUrn(), "urn is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_ASSET_GET)
    @Override
    public void handle(Session session, GetAssetMessage message) throws WriteResponseException {
        try {
            assetService.getAssetPayload(message.getUrn())
                    // log a debug line on signal emitted
                    .doOnEach(log.reactiveDebugSignal("fetching asset by urn"))
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .subscribe(assetPayload -> {
                        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(LEARNER_ASSET_GET_OK, message.getId());
                        basicResponseMessage.addField("asset", assetPayload);
                        Responses.writeReactive(session, basicResponseMessage);
                    }, ex -> {
                        Responses.errorReactive(session, message.getId(), LEARNER_ASSET_GET_ERROR,
                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to fetch asset");
                    });
        } catch (AssetURNParseException e) {
            Responses.errorReactive(session, message.getId(), LEARNER_ASSET_GET_ERROR,
                    HttpStatus.SC_BAD_REQUEST, "invalid URN");
        }
    }
}
