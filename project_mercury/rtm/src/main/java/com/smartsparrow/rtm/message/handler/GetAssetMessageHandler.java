package com.smartsparrow.rtm.message.handler;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.service.AssetService;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.courseware.service.WorkspaceAssetService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.GetAssetMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class GetAssetMessageHandler implements MessageHandler<GetAssetMessage> {

    private static final Logger log = LoggerFactory.getLogger(GetAssetMessageHandler.class);

    public static final String AUTHOR_ASSET_GET = "author.asset.get";
    public static final String AUTHOR_ASSET_GET_OK = "author.asset.get.ok";
    public static final String AUTHOR_ASSET_GET_ERROR = "author.asset.get.error";

    private final WorkspaceAssetService workspaceAssetService;

    @Inject
    public GetAssetMessageHandler(WorkspaceAssetService workspaceAssetService) {
        this.workspaceAssetService = workspaceAssetService;
    }

    @Override
    public void validate(GetAssetMessage message) throws RTMValidationException {
        try {
            checkArgument(!Strings.isNullOrEmpty(message.getUrn()), "urn is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), AUTHOR_ASSET_GET_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ASSET_GET)
    public void handle(Session session, GetAssetMessage message) {
        try {
            workspaceAssetService.getAssetPayload(message.getUrn())
                    // link each signal to the current transaction token
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    // expire the transaction token on completion
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    // create a reactive context that enables all supported reactive monitoring
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .subscribe(
                    asset -> {
                        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(AUTHOR_ASSET_GET_OK, message.getId());
                        basicResponseMessage.addField("asset", asset);
                        Responses.writeReactive(session, basicResponseMessage);
                    },
                    ex -> {
                        if (log.isDebugEnabled()) {
                            log.debug("Unable to fetch asset for urn '{}' {}", message.getUrn(), ex.getMessage());
                        }
                        Responses.errorReactive(session, message.getId(), AUTHOR_ASSET_GET_ERROR,
                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "Unable to fetch asset");
                    });
        } catch (AssetURNParseException e) {
            log.warn("urn '{}' can not be parsed", message.getUrn());
            Responses.errorReactive(session, message.getId(), AUTHOR_ASSET_GET_ERROR,
                    HttpStatus.SC_BAD_REQUEST, "invalid URN");
        }

    }
}
