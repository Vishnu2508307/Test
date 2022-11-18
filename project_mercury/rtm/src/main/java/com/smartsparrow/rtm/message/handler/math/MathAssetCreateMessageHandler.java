package com.smartsparrow.rtm.message.handler.math;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.math.service.MathAssetService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.math.MathAssetCreateMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class MathAssetCreateMessageHandler implements MessageHandler<MathAssetCreateMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MathAssetCreateMessageHandler.class);

    public static final String AUTHOR_MATH_ASSET_CREATE = "author.math.asset.create";
    public static final String AUTHOR_MATH_ASSET_CREATE_OK = "author.math.asset.create.ok";
    public static final String AUTHOR_MATH_ASSET_CREATE_ERROR = "author.math.asset.create.error";

    private final AuthenticationContextProvider authenticationContextProvider;
    private final MathAssetService mathAssetService;

    @Inject
    public MathAssetCreateMessageHandler(final MathAssetService mathMLAssetService,
                                         final AuthenticationContextProvider authenticationContextProvider) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.mathAssetService = mathMLAssetService;
    }

    @Override
    public void validate(MathAssetCreateMessage message) throws RTMValidationException {
        affirmArgument(message.getMathML() != null, "mathML is required");
        affirmArgument(message.getElementId() != null, "elementId is required");
        affirmArgument(message.getElementType() != null, "elementType is required");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_MATH_ASSET_CREATE)
    public void handle(Session session, MathAssetCreateMessage message) throws WriteResponseException {
        UUID accountId = authenticationContextProvider.get().getAccount().getId();

        mathAssetService.createMathAsset(message.getMathML(),
                                         message.getAltText(),
                                         message.getElementId(),
                                         accountId)
                .doOnEach(log.reactiveErrorThrowable("error while creating math asset"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(assetUrn -> {
                    Responses.writeReactive(session,
                                            new BasicResponseMessage(AUTHOR_MATH_ASSET_CREATE_OK, message.getId())
                                                    .addField("assetUrn", assetUrn.toString()));
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), AUTHOR_MATH_ASSET_CREATE_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error creating Math asset");
                });
    }
}
