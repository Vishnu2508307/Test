package com.smartsparrow.rtm.message.handler.math;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.math.service.MathAssetService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.math.MathAssetRemoveMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class RemoveMathAssetMessageHandler implements MessageHandler<MathAssetRemoveMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RemoveMathAssetMessageHandler.class);

    public static final String AUTHOR_MATH_ASSET_REMOVE = "author.math.asset.remove";
    public static final String AUTHOR_MATH_ASSET_REMOVE_OK = "author.math.asset.remove.ok";
    public static final String AUTHOR_MATH_ASSET_REMOVE_ERROR = "author.math.asset.remove.error";

    private final MathAssetService mathAssetService;

    @Inject
    public RemoveMathAssetMessageHandler(MathAssetService mathAssetService) {
        this.mathAssetService = mathAssetService;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void validate(MathAssetRemoveMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "elementId is required");
        affirmArgument(message.getElementType() != null, "elementType is required");
        affirmArgument(message.getAssetUrn() != null, "assetUrn is required");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_MATH_ASSET_REMOVE)
    public void handle(Session session, MathAssetRemoveMessage message) {
        mathAssetService.removeMathAsset(message.getElementId(), message.getAssetUrn())
                .singleOrEmpty()
                .doOnEach(log.reactiveErrorThrowable("error while removing math asset"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(v -> {
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), AUTHOR_MATH_ASSET_REMOVE_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error removing Math asset");
                }, () -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(AUTHOR_MATH_ASSET_REMOVE_OK, message.getId()));
                });
    }
}
