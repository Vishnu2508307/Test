package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.asset.CreateAssetMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class CreateAssetMessageHandler implements MessageHandler<CreateAssetMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateAssetMessageHandler.class);

    public static final String AUTHOR_ASSET_CREATE = "author.asset.create";
    private static final String AUTHOR_ASSET_CREATE_OK = "author.asset.create.ok";
    private static final String AUTHOR_ASSET_CREATE_ERROR = "author.asset.create.error";

    private final BronteAssetService bronteAssetService;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    public CreateAssetMessageHandler(final BronteAssetService bronteAssetService,
                                     final AuthenticationContextProvider authenticationContextProvider) {
        this.bronteAssetService = bronteAssetService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(CreateAssetMessage message) throws RTMValidationException {
        affirmArgumentNotNullOrEmpty(message.getUrl(), "url is required");
        affirmArgument(message.getMediaType() != null, "mediaType is required");
        affirmArgument(message.getAssetProvider() != null, "assetProvider is required");
        affirmArgument(message.getAssetVisibility() != null, "assetVisibility is required");
        affirmArgument(!message.getAssetProvider().equals(AssetProvider.AERO), "AERO provider not supported by this message");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ASSET_CREATE)
    public void handle(Session session, CreateAssetMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();

        bronteAssetService.create(message.getUrl(), message.getAssetVisibility(), account, message.getMediaType(),
                message.getMetadata(), message.getAssetProvider())
                .doOnEach(log.reactiveErrorThrowable("error creating the asset"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(assetPayload -> {
                    Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_ASSET_CREATE_OK, message.getId())
                            .addField("asset", assetPayload));
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), AUTHOR_ASSET_CREATE_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error creating the asset");
                });
    }
}
