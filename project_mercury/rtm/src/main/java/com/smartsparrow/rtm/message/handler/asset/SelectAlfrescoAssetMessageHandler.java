package com.smartsparrow.rtm.message.handler.asset;

import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetVisibility;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.asset.SelectAlfrescoAssetMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import reactor.core.publisher.Mono;

import javax.inject.Inject;

import java.util.UUID;

import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

public class SelectAlfrescoAssetMessageHandler implements MessageHandler<SelectAlfrescoAssetMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SelectAlfrescoAssetMessageHandler.class);

    public static final String AUTHOR_ALFRESCO_ASSET_SELECT = "author.asset.alfresco.select";
    private static final String AUTHOR_ALFRESCO_ASSET_SELECT_OK = "author.asset.alfresco.select.ok";
    private static final String AUTHOR_ALFRESCO_ASSET_SELECT_ERROR = "author.asset.alfresco.select.error";

    private final AlfrescoAssetService alfrescoAssetService;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    public SelectAlfrescoAssetMessageHandler(final AlfrescoAssetService alfrescoAssetService,
                                             final AuthenticationContextProvider authenticationContextProvider) {
        this.alfrescoAssetService = alfrescoAssetService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(SelectAlfrescoAssetMessage message) throws RTMValidationException {
        affirmArgumentNotNullOrEmpty(message.getAlfrescoNodeId(), "alfresco node id is required");
    }

    @Override
    public void handle(Session session, SelectAlfrescoAssetMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();
        final String myCloudToken = authenticationContextProvider.get().getPearsonToken();

        final String alfrescoNodeId = message.getAlfrescoNodeId();

        alfrescoAssetService.save(alfrescoNodeId, account, myCloudToken)
                .doOnEach(log.reactiveErrorThrowable("error selecting alfresco asset"))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(assetPayload -> {
                    Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_ALFRESCO_ASSET_SELECT_OK, message.getId())
                            .addField("asset", assetPayload));
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), AUTHOR_ALFRESCO_ASSET_SELECT_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error selecting alfresco asset");
                });
    }
}
