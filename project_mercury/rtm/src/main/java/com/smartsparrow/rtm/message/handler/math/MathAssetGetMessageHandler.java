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

public class MathAssetGetMessageHandler implements MessageHandler<MathAssetGetMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MathAssetGetMessageHandler.class);

    public static final String AUTHOR_MATH_ASSET_GET = "author.math.asset.get";
    public static final String AUTHOR_MATH_ASSET_GET_OK = "author.math.asset.get.ok";
    public static final String AUTHOR_MATH_ASSET_GET_ERROR = "author.math.asset.get.error";

    private final MathAssetService mathAssetService;

    @Inject
    public MathAssetGetMessageHandler(final MathAssetService mathMLAssetService) {
        this.mathAssetService = mathMLAssetService;
    }

    @Override
    public void validate(MathAssetGetMessage message) throws RTMValidationException {
        affirmArgument(message.getUrn() != null, "urn is required");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_MATH_ASSET_GET)
    public void handle(Session session, MathAssetGetMessage message) throws WriteResponseException {

        mathAssetService.getMathAssetSummary(message.getUrn())
                .doOnEach(log.reactiveErrorThrowable("error while fetching Math asset"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(assetSummary -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(AUTHOR_MATH_ASSET_GET_OK, message.getId())
                                                    .addField("asset", assetSummary));
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), AUTHOR_MATH_ASSET_GET_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching Math asset");
                });
    }
}
