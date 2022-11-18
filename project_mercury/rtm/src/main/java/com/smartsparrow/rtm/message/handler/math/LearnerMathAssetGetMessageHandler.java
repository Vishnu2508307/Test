package com.smartsparrow.rtm.message.handler.math;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.math.service.MathAssetService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.math.MathAssetGetMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class LearnerMathAssetGetMessageHandler implements MessageHandler<MathAssetGetMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerMathAssetGetMessageHandler.class);

    public static final String LEARNER_MATH_ASSET_GET = "learner.math.asset.get";
    public static final String LEARNER_MATH_ASSET_GET_OK = "learner.math.asset.get.ok";
    public static final String LEARNER_MATH_ASSET_GET_ERROR = "learner.math.asset.get.error";

    private final MathAssetService mathAssetService;

    @Inject
    public LearnerMathAssetGetMessageHandler(final MathAssetService mathMLAssetService) {
        this.mathAssetService = mathMLAssetService;
    }

    @Override
    public void validate(MathAssetGetMessage message) throws RTMValidationException {
        affirmArgument(message.getUrn() != null, "urn is required");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_MATH_ASSET_GET)
    public void handle(Session session, MathAssetGetMessage message) throws WriteResponseException {

        mathAssetService.getMathAssetSummary(message.getUrn())
                .doOnEach(log.reactiveErrorThrowable("error while fetching learner Math asset"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(assetSummary -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(LEARNER_MATH_ASSET_GET_OK, message.getId())
                                                    .addField("asset", assetSummary));
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), LEARNER_MATH_ASSET_GET_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching learner Math asset");
                });
    }
}
