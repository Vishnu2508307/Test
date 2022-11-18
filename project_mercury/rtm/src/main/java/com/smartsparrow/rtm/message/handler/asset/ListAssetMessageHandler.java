package com.smartsparrow.rtm.message.handler.asset;


import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.WorkspaceAssetService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.asset.ListAssetMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ListAssetMessageHandler implements MessageHandler<ListAssetMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListAssetMessageHandler.class);
    private static final int MAX_LIMIT = 20;

    public static final String AUTHOR_ASSET_LIST = "author.asset.list";
    public static final String AUTHOR_ASSET_LIST_OK = "author.asset.list.ok";
    public static final String AUTHOR_ASSET_LIST_ERROR = "author.asset.list.error";

    private final WorkspaceAssetService workspaceAssetService;

    @Inject
    public ListAssetMessageHandler(final WorkspaceAssetService workspaceAssetService) {
        this.workspaceAssetService = workspaceAssetService;
    }

    @Override
    public void validate(ListAssetMessage message) throws RTMValidationException {
        affirmArgumentNotNullOrEmpty(message.getAssetUrns(), "urn is required");
        affirmArgument(message.getLimit() != null, "limit is required");
        affirmArgument(message.getLimit().intValue() <= MAX_LIMIT,
                       "limit size exceeds the max limit size " + MAX_LIMIT);
        affirmArgument((message.getAssetUrns().size() <= message.getLimit().intValue()),
                       "asset urn list exceeds the limit size");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_ASSET_LIST)
    @Override
    public void handle(Session session, ListAssetMessage message) throws WriteResponseException {

        workspaceAssetService.getAssetPayload(message.getAssetUrns())
                .doOnEach(log.reactiveErrorThrowable("error fetching asset payloads by urns"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .subscribe(assetsByIconLibraries -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_ASSET_LIST_OK,
                                       message.getId());
                               basicResponseMessage.addField("assetPayloads", assetsByIconLibraries);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.jsonDebug("Unable to fetch asset payloads by urns", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_ASSET_LIST_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to fetch asset payloads by urns");
                           }
                );

    }
}
